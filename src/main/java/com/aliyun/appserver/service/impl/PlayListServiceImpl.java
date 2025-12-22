package com.aliyun.appserver.service.impl;

import com.aliyun.appserver.config.VodConfig;
import com.aliyun.appserver.entity.PlayList;
import com.aliyun.appserver.entity.PlaylistItemDto;
import com.aliyun.appserver.jwt.JwtConstants;
import com.aliyun.appserver.jwt.JwtUtil;
import com.aliyun.appserver.result.CallResult;
import com.aliyun.appserver.result.ResponseResult;
import com.aliyun.appserver.result.ResultCode;
import com.aliyun.appserver.service.PlayListService;
import com.aliyun.appserver.service.VodSdkService;
import com.aliyuncs.vod.model.v20170321.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 播单业务服务实现类
 * 提供播单详情查询、播单列表查询等业务功能
 * 包含视频播放凭证生成、封面图处理等增强功能
 *
 * @author: pxc
 * @date: 2025/11/18 16:03
 */

@Service
public class PlayListServiceImpl implements PlayListService {

    @Autowired
    private VodSdkService vodSdkService;

    @Autowired
    private VodConfig vodConfig;

    // 缓存播放密钥，减少重复获取
    private final Map<String, CachedPlayKey> playKeyCache = new ConcurrentHashMap<>();

    // 播放密钥缓存类
    private static class CachedPlayKey {
        private final String playKey;
        private final long expireTime;

        public CachedPlayKey(String playKey, long expireTime) {
            this.playKey = playKey;
            this.expireTime = expireTime;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }

        public String getPlayKey() {
            return playKey;
        }
    }

    /**
     * 获取播单详情（含视频列表和播放凭证）
     * 功能说明：
     * 1. 如果未指定播单ID，则返回第一个播单
     * 2. 获取播单基本信息和视频列表
     * 3. 处理播单封面图（将imageId转换为实际URL）
     * 4. 为每个视频生成本地签名播放凭证（JWTPlayAuth）
     *
     * @param playListId 播单ID（可为空，为空时返回第一个播单）
     * @return 播单详情结果，包含：
     * - 播单基本信息（ID、名称、描述、状态等）
     * - 视频列表（含播放凭证字段 playAuth，值为 JWTPlayAuth）
     * - 封面图实际URL
     */
    @Override
    public CallResult getPlaylistInfo(String playListId) {
        CallResult result = new CallResult();

        // 1. 参数处理：如果播单ID为空，获取第一个播单
        if (playListId == null || playListId.isEmpty()) {
            List<GetPlaylistsResponse.PlaylistDO> playlists = vodSdkService.getPlaylists(new GetPlaylistsRequest()).getPlaylists();
            if (playlists != null && !playlists.isEmpty()) {
                playListId = playlists.get(0).getPlaylistId();
            } else {
                return ResponseResult.makeErrRsp("播放列表为空");
            }
        }

        // 确保playListId是final或实际上final的，以便在lambda表达式中使用
        final String finalPlayListId = playListId;

        // 异步获取播单详细信息和播放密钥
        CompletableFuture<GetPlaylistResponse> playlistFuture = CompletableFuture.supplyAsync(() -> {
            GetPlaylistRequest getPlaylistRequest = new GetPlaylistRequest();
            getPlaylistRequest.setPlaylistId(finalPlayListId);
            return vodSdkService.getPlaylist(getPlaylistRequest);
        });

        CompletableFuture<String> playKeyFuture = CompletableFuture.supplyAsync(() -> getPlayKey());

        // 等待异步操作完成
        GetPlaylistResponse getPlaylistResponse;
        String playKey;
        try {
            getPlaylistResponse = playlistFuture.get();
            playKey = playKeyFuture.get();
        } catch (Exception e) {
            return ResponseResult.makeErrRsp("获取播单信息或播放密钥失败: " + e.getMessage());
        }

        if (getPlaylistResponse == null || getPlaylistResponse.getPlaylistId() == null) {
            return ResponseResult.makeErrRsp("播单不存在");
        }

        // 3. 构建播单对象
        PlayList playList = new PlayList(getPlaylistResponse);

        // 4. 异步处理播单封面图：将imageId转换为实际URL
        CompletableFuture<Void> coverImageFuture = CompletableFuture.runAsync(() -> {
            if (playList.getPlaylistCoverUrl() != null) {
                GetImageInfosResponse getImageInfosResponse = vodSdkService.GetImageInfos(playList.getPlaylistCoverUrl());
                playList.setPlaylistCoverUrl(getImageInfosResponse != null && getImageInfosResponse.getImageInfo() != null
                        ? getImageInfosResponse.getImageInfo().get(0).getURL()
                        : null);
            }
        });

        // 5. 处理视频列表：为每个视频生成播放凭证
        if (getPlaylistResponse.getPlaylistVideos() != null && !getPlaylistResponse.getPlaylistVideos().isEmpty()) {
            if (playKey == null || playKey.trim().isEmpty()) {
                return ResponseResult.makeErrRsp("播放密钥不能为空");
            }

            // 5.2 为每个视频生成 JWTPlayAuth（字段名：playAuth）
            final String regionId = vodConfig.getRegion() != null && !vodConfig.getRegion().trim().isEmpty()
                    ? vodConfig.getRegion()
                    : JwtConstants.DEFAULT_REGION_ID;

            List<PlaylistItemDto> playListVideos = getPlaylistResponse.getPlaylistVideos().parallelStream().map(playlistItemDo -> {
                PlaylistItemDto playlistItemDto = new PlaylistItemDto(playlistItemDo);
                // 生成播放凭证（避免 videoId 为空导致的 NPE）
                String videoId = playlistItemDto.getVideoId();
                if (videoId != null && !videoId.trim().isEmpty()) {
                    String newPlayAuth = JwtUtil.getPlayAuthToken(videoId.trim(), playKey, regionId);
                    playlistItemDto.setPlayAuth(newPlayAuth);
                }
                return playlistItemDto;
            }).collect(Collectors.toList());

            playList.setPlaylistVideos(playListVideos);
        }

        // 等待封面图处理完成
        try {
            coverImageFuture.get();
        } catch (Exception e) {
            // 忽略封面图处理异常，不影响主流程
        }

        // 6. 设置成功响应
        result.setCode(ResultCode.SUCCESS.code);
        result.setHttpCode("200");
        result.setSuccess(true);
        result.setMessage("success");
        result.setData(playList);
        return result;
    }

    /**
     * 获取播放密钥，带缓存机制
     * @return playKey
     */
    private String getPlayKey() {
        CachedPlayKey cached = playKeyCache.get(JwtConstants.DEFAULT_APP_ID);
        if (cached != null && !cached.isExpired()) {
            return cached.getPlayKey();
        }

        try {
            GetAppPlayKeyResponse getAppPlayKeyResponse = vodSdkService.GetAppPlayKey(JwtConstants.DEFAULT_APP_ID);
            String playKey = getAppPlayKeyResponse != null && getAppPlayKeyResponse.getAppPlayKey() != null
                    ? getAppPlayKeyResponse.getAppPlayKey().getPlayKey()
                    : null;

            if (playKey != null) {
                // 缓存10分钟
                playKeyCache.put(JwtConstants.DEFAULT_APP_ID, new CachedPlayKey(playKey, System.currentTimeMillis() + 600000));
            }
            return playKey;
        } catch (Exception e) {
            throw new RuntimeException("获取播放密钥失败", e);
        }
    }

    /**
     * 获取播单列表（分页，含首个视频和播放凭证）
     * 功能说明：
     * 1. 获取播单列表（支持分页）
     * 2. 为每个播单获取第一个视频作为预览
     * 3. 批量处理播单封面图
     * 4. 为预览视频生成本地签名播放凭证（JWTPlayAuth）
     *
     * @param request 查询播单列表请求（pageNo, pageSize, sortBy）
     * @return 播单列表结果，每个播单包含：
     * - 播单基本信息
     * - 第一个视频（作为预览，含本地签名播放凭证 JWTPlayAuth）
     * - 封面图实际URL
     */
    @Override
    public CallResult getPlaylistVideos(GetPlaylistsRequest request) {
        CallResult result = new CallResult();

        // 1. 获取播单列表
        GetPlaylistsResponse getPlaylistsResponse = vodSdkService.getPlaylists(request);

        // 参数校验
        if (getPlaylistsResponse == null
                || getPlaylistsResponse.getPlaylists() == null
                || getPlaylistsResponse.getPlaylists().isEmpty()) {
            return ResponseResult.makeErrRsp("播放列表为空");
        }

        // 2. 获取播放密钥（用于生成视频播放凭证）
        String playKey = getPlayKey();
        if (playKey == null || playKey.trim().isEmpty()) {
            return ResponseResult.makeErrRsp("播放密钥不能为空");
        }

        Set<String> previewVideoIdSet = new HashSet<>();
        Map<String, String> previewVideoIdToPlayListId = new HashMap<>();
        // 3. 处理每个播单：获取第一个视频作为预览
        List<PlayList> playLists = getPlaylistsResponse.getPlaylists().stream().map(playlistDO -> {
            PlayList playList = new PlayList(playlistDO);
            //PlaylistExtension是一个json形式字符串，解析其中字段previewVideoId，存储到previewVideoIdSet中
            try {
                if (playlistDO.getPlaylistExtension() != null && !playlistDO.getPlaylistExtension().trim().isEmpty()) {
                    ObjectMapper objectMapper = new ObjectMapper();
                    JsonNode jsonNode = objectMapper.readTree(playlistDO.getPlaylistExtension());
                    JsonNode previewVideoIdNode = jsonNode.get("previewVideoId");
                    if (previewVideoIdNode != null && !previewVideoIdNode.asText().trim().isEmpty()) {
                        previewVideoIdSet.add(previewVideoIdNode.asText());
                        previewVideoIdToPlayListId.put(playlistDO.getPlaylistId(), previewVideoIdNode.asText());
                    }
                }
            } catch (Exception e) {
                // 解析JSON异常处理
                ResponseResult.makeErrRsp("解析PlaylistExtension失败: {}" + e);
            }
            return playList;
        }).collect(Collectors.toList());

        // 异步执行封面图和视频信息的获取
        CompletableFuture<Map<String, GetImageInfosResponse.Image>> mediaMapFuture = CompletableFuture.supplyAsync(() -> {
            // 4. 批量处理封面图：将imageId转换为实际URL
            Set<String> videoIdSet = playLists.stream()
                    .map(PlayList::getPlaylistCoverUrl)
                    .filter(id -> id != null && !id.trim().isEmpty())
                    .collect(Collectors.toSet());

            Map<String, GetImageInfosResponse.Image> mediaMap = new HashMap<>();
            if (!videoIdSet.isEmpty()) {
                List<String> videoIdList = new ArrayList<>(videoIdSet);
                String ids = String.join(",", videoIdList);
                // 批量查询图片信息
                GetImageInfosResponse response = vodSdkService.GetImageInfos(ids);
                if (response != null && response.getImageInfo() != null) {
                    // 构建 imageId -> Image 映射
                    response.getImageInfo().forEach(image -> {
                        if (image.getImageId() != null) {
                            mediaMap.put(image.getImageId(), image);
                        }
                    });
                }
            }
            return mediaMap;
        });

        CompletableFuture<Map<String, GetVideoInfosResponse.Video>> previewVideoMapFuture = CompletableFuture.supplyAsync(() -> {
            GetVideoInfosResponse getVideoInfosResponse = vodSdkService.GetVideoInfos(String.join(",", previewVideoIdSet));
            Map<String, GetVideoInfosResponse.Video> previewVideoIdToVideo = new HashMap<>();
            if (getVideoInfosResponse != null && getVideoInfosResponse.getVideoList() != null) {
                getVideoInfosResponse.getVideoList().forEach(
                        video -> previewVideoIdToVideo.put(video.getVideoId(), video)
                );
            }
            return previewVideoIdToVideo;
        });

        Map<String, GetImageInfosResponse.Image> mediaMap;
        Map<String, GetVideoInfosResponse.Video> previewVideoIdToVideo;
        try {
            mediaMap = mediaMapFuture.get();
            previewVideoIdToVideo = previewVideoMapFuture.get();
        } catch (Exception e) {
            return ResponseResult.makeErrRsp("获取封面图或视频信息失败: " + e.getMessage());
        }

        // 5. 为每个播单设置预览视频和封面图
        String regionId = vodConfig.getRegion() != null && !vodConfig.getRegion().trim().isEmpty()
                ? vodConfig.getRegion()
                : JwtConstants.DEFAULT_REGION_ID;

        // 使用并行流提高处理效率
        playLists.parallelStream().forEach(playList -> {
            // 5.1 构建预览视频并生成 JWTPlayAuth（字段名：playAuth）
            if (previewVideoIdToPlayListId.containsKey(playList.getPlaylistId()) && previewVideoIdToVideo.containsKey(previewVideoIdToPlayListId.get(playList.getPlaylistId()))) {
                PlaylistItemDto playlistItemDto = new PlaylistItemDto(previewVideoIdToVideo.get(previewVideoIdToPlayListId.get(playList.getPlaylistId())));
                playlistItemDto.setPlayAuth(JwtUtil.getPlayAuthToken(playlistItemDto.getVideoId(), playKey, regionId));
                playlistItemDto.setPlaylistId(playList.getPlaylistId());
                // 5.2 将预览视频设置到播单
                List<PlaylistItemDto> playlistItemDtos = new ArrayList<>();
                playlistItemDtos.add(playlistItemDto);
                playList.setPlaylistVideos(playlistItemDtos);
            }
            // 5.3 设置封面图实际URL
            if (mediaMap.containsKey(playList.getPlaylistCoverUrl())) {
                playList.setPlaylistCoverUrl(mediaMap.get(playList.getPlaylistCoverUrl()).getURL());
            }
        });

        // 6. 设置成功响应
        result.setCode(ResultCode.SUCCESS.code);
        result.setHttpCode("200");
        result.setSuccess(true);
        result.setMessage("success");
        result.setData(playLists);
        return result;
    }
}
