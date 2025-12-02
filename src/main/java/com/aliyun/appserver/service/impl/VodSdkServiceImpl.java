package com.aliyun.appserver.service.impl;

import com.aliyun.appserver.config.VodConfig;
import com.aliyun.appserver.service.VodSdkService;
import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.profile.DefaultProfile;
import com.aliyuncs.vod.model.v20170321.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * VOD SDK 访问服务实现类
 *
 * <p>
 * 封装了对阿里云 VOD OpenAPI 的 Java SDK 调用，包括：
 * <ul>
 *     <li>实体 / 实体属性 / 实体媒资相关接口</li>
 *     <li>播放信息、图片信息查询</li>
 *     <li>播单增删改查等能力</li>
 * </ul>
 * 该类不包含业务拼装逻辑，仅作为远程调用适配层使用。
 * </p>
 *
 * @author pxc
 * @date 2025/10/14
 */
@Service  // 标记为 Spring Service Bean
public class VodSdkServiceImpl implements VodSdkService {
    static {
        System.setProperty("com.aliyuncs.sdk.protocol", "https");
    }

    private static final Logger log = LoggerFactory.getLogger(VodSdkServiceImpl.class);

    private final IAcsClient vodClient;

    /**
     * 构造函数：初始化 VOD SDK 客户端
     *
     * <p>从 {@link VodConfig} 中读取配置信息，包括：
     * <ul>
     *     <li>AccessKey ID / Secret：用于身份认证</li>
     *     <li>Region ID：VOD 服务地域标识，从配置中读取，如果为空则使用默认值 cn-shanghai</li>
     * </ul>
     *
     * @param vodConfig VOD 配置对象，包含 ak、sk、region 等配置信息
     * @throws IllegalStateException 如果客户端初始化失败
     */
    public VodSdkServiceImpl(VodConfig vodConfig) {
        try {
            // 从配置中读取 regionId，如果配置为空则使用默认值
            String regionId = vodConfig.getRegion() != null && !vodConfig.getRegion().trim().isEmpty()
                    ? vodConfig.getRegion()
                    : "cn-shanghai"; // 默认值，与 JwtConstants.DEFAULT_REGION_ID 保持一致
            DefaultProfile profile = DefaultProfile.getProfile(regionId, vodConfig.getAk(), vodConfig.getSk());
            this.vodClient = new DefaultAcsClient(profile);
            log.info("VOD 客户端初始化成功，Region: {}", regionId);
        } catch (Exception e) {
            log.error("VOD 客户端初始化失败", e);
            throw new IllegalStateException("Failed to initialize VOD client", e);
        }
    }

    @Override
    public CreateEntityResponse createEntity(String entityName, String attributeIds, String syncAttributeIds) {
        CreateEntityRequest request = new CreateEntityRequest();
        request.setEntityName(entityName);
        request.setAttributeIds(attributeIds);
        request.setSyncAttributeIds(syncAttributeIds);
        CreateEntityResponse response = new CreateEntityResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 CreateEntity 失败: Code={}, Message={}", e.getErrCode(), e.getErrMsg());
            return response;
        }
    }

    @Override
    public DeleteEntityResponse deleteEntity(String entityId) {
        DeleteEntityRequest request = new DeleteEntityRequest();
        request.setEntityId(entityId);

        DeleteEntityResponse response = new DeleteEntityResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 DeleteEntity 失败 [EntityId={}]: {}", entityId, e.getErrMsg());

            return response;
        }
    }

    @Override
    public UpdateEntityResponse updateEntity(String entityId, String attributeIds, String syncAttributeIds) {
        UpdateEntityRequest request = new UpdateEntityRequest();
        request.setEntityId(entityId);
        request.setAttributeIds(attributeIds);
        request.setSyncAttributeIds(syncAttributeIds);

        UpdateEntityResponse response = new UpdateEntityResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 UpdateEntity 失败 [EntityId={}]: {}", entityId, e.getErrMsg());
            throw new RuntimeException("UpdateEntity failed", e);
        }
    }

    @Override
    public GetEntityResponse getEntity(String entityId) {
        GetEntityRequest request = new GetEntityRequest();
        request.setEntityId(entityId);
        GetEntityResponse response = new GetEntityResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetEntity 失败 [EntityId={}]: {}", entityId, e.getErrMsg());
            throw new RuntimeException("GetEntity failed", e);
        }
    }

    @Override
    public CreateEntityAttributeResponse createEntityAttribute(String attributeName, Long type, Long length, String defaultValue) {
        CreateEntityAttributeRequest request = new CreateEntityAttributeRequest();
        request.setAttributeName(attributeName);
        request.setType(type);
        request.setLength(length);
        request.setDefaultValue(defaultValue);
        CreateEntityAttributeResponse response = new CreateEntityAttributeResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 CreateEntityAttribute 失败: Code={}, Message={}", e.getErrCode(), e.getErrMsg());
            return response;
        }
    }

    @Override
    public DeleteEntityAttributeResponse deleteEntityAttribute(String attributeId) {
        DeleteEntityAttributeRequest request = new DeleteEntityAttributeRequest();
        request.setAttributeId(attributeId);

        DeleteEntityAttributeResponse response = new DeleteEntityAttributeResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 DeleteEntityAttribute 失败 [AttributeId={}]: {}", attributeId, e.getErrMsg());
            return response;
        }
    }

    @Override
    public GetEntityAttributeResponse getEntityAttribute(String attributeId) {
        GetEntityAttributeRequest request = new GetEntityAttributeRequest();
        request.setAttributeId(attributeId);

        GetEntityAttributeResponse response = new GetEntityAttributeResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetEntityAttribute 失败 [AttributeId={}]: {}", attributeId, e.getErrMsg());
            return response;
        }
    }

    @Override
    public RegisterEntityMediaResponse registerEntityMedia(String entityId, String dynamicMetaData) {
        RegisterEntityMediaRequest request = new RegisterEntityMediaRequest();
        request.setEntityId(entityId);
        request.setDynamicMetaData(dynamicMetaData);

        RegisterEntityMediaResponse response = new RegisterEntityMediaResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 RegisterEntityMedia 失败: Code={}, Message={}", e.getErrCode(), e.getErrMsg());
            return response;
        }
    }

    @Override
    public DeleteEntityMediasResponse deleteEntityMedias(String entityMediaIds, Boolean forceDelete) {
        if (entityMediaIds == null || entityMediaIds.trim().isEmpty()) {
            throw new IllegalArgumentException("entityMediaIds 不能为空");
        }

        DeleteEntityMediasRequest request = new DeleteEntityMediasRequest();
        request.setEntityMediaIds(entityMediaIds);
        request.setForceDelete(forceDelete != null ? forceDelete : false);

        DeleteEntityMediasResponse response = new DeleteEntityMediasResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 DeleteEntityMedias 失败: Code={}, Message={}", e.getErrCode(), e.getErrMsg());
            return response;
        }
    }

    @Override
    public UpdateEntityMediaResponse updateEntityMedia(String entityMediaId, String entityId, String dynamicMetaData) {
        UpdateEntityMediaRequest request = new UpdateEntityMediaRequest();
        request.setEntityMediaId(entityMediaId);
        request.setEntityId(entityId);
        request.setDynamicMetaData(dynamicMetaData);

        UpdateEntityMediaResponse response = new UpdateEntityMediaResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 UpdateEntityMedia 失败 [EntityMediaId={}]: {}", entityMediaId, e.getErrMsg());
            return response;
        }
    }

    @Override
    public GetEntityMediaResponse getEntityMedia(String entityMediaId, Boolean needParent, Boolean needSub) {
        if (entityMediaId == null || entityMediaId.trim().isEmpty()) {
            throw new IllegalArgumentException("entityMediaId 不能为空");
        }

        GetEntityMediaRequest request = new GetEntityMediaRequest();
        request.setEntityMediaId(entityMediaId);
        request.setNeedParentEntityMedia(needParent != null ? needParent : false);
        request.setNeedSubEntityMedia(needSub == null || needSub); // 默认 true

        GetEntityMediaResponse response = new GetEntityMediaResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetEntityMedia 失败 [EntityMediaId={}]: {}", entityMediaId, e.getErrMsg());
            return response;
        }
    }

    @Override
    public GetEntityAttributeListResponse getEntityAttributeList(Integer pageNo, Integer pageSize, String sortBy) {
        GetEntityAttributeListRequest request = new GetEntityAttributeListRequest();

        request.setPageNo(pageNo != null ? pageNo : 1);
        request.setPageSize(pageSize != null ? pageSize : 10);
        request.setSortBy(sortBy); // 可为 null，默认 desc

        GetEntityAttributeListResponse response = new GetEntityAttributeListResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetEntityAttributeList 失败: Code={}, Message={}", e.getErrCode(), e.getErrMsg());
            return response;
        }
    }

    @Override
    public GetEntityMediaListResponse getEntityMediaList(String entityId, Integer pageNo, Integer pageSize, String sortBy) {
        if (entityId == null || entityId.trim().isEmpty()) {
            throw new IllegalArgumentException("entityId 不能为空");
        }

        GetEntityMediaListRequest request = new GetEntityMediaListRequest();
        request.setEntityId(entityId);
        request.setPageNo(pageNo != null ? pageNo : 1);
        request.setPageSize(pageSize != null ? pageSize : 10);
        request.setSortBy(sortBy); // 默认 desc

        GetEntityMediaListResponse response = new GetEntityMediaListResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetEntityMediaList 失败 [EntityId={}]: {}", entityId, e.getErrMsg());
            return response;
        }
    }

    @Override
    public GetEntityListResponse getEntityList(Integer pageNo, Integer pageSize, String sortBy) {
        GetEntityListRequest request = new GetEntityListRequest();
        request.setPageNo(pageNo != null ? pageNo : 1);
        request.setPageSize(pageSize != null ? pageSize : 10);
        request.setSortBy(sortBy); // 可为 null，默认 desc

        GetEntityListResponse response = new GetEntityListResponse();

        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetEntityListResponse 失败: Code={}, Message={}", e.getErrCode(), e.getErrMsg());
            return response;
        }
    }

    @Override
    public GetAppPlayKeyResponse GetAppPlayKey(String appId) {
        GetAppPlayKeyRequest request = new GetAppPlayKeyRequest();
        request.setAppId(appId);
        GetAppPlayKeyResponse response = new GetAppPlayKeyResponse();
        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetAppPlayKey 失败 [AppId={}]: {}", appId, e.getErrMsg());
            return response;
        }
    }

    @Override
    public SetAppPlayKeyResponse SetAppPlayKey(String appId, String playKey) {
        SetAppPlayKeyRequest request = new SetAppPlayKeyRequest();
        request.setAppId(appId);
        request.setPlayKey(playKey);
        SetAppPlayKeyResponse response = new SetAppPlayKeyResponse();
        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 SetAppPlayKey 失败 [AppId={}]: {}", appId, e.getErrMsg());
            return response;
        }
    }

    @Override
    public GetPlayInfoResponse GetPlayInfo(String videoId) {
        GetPlayInfoRequest request = new GetPlayInfoRequest();
        request.setVideoId(videoId);
        GetPlayInfoResponse response = new GetPlayInfoResponse();
        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetPlayInfo 失败 [VideoId={}]: {}", videoId, e.getErrMsg());
            return response;
        }
    }

    @Override
    public GetVideoInfosResponse GetVideoInfos(String videoIds) {
        GetVideoInfosRequest request = new GetVideoInfosRequest();
        request.setVideoIds(videoIds);
        GetVideoInfosResponse response = new GetVideoInfosResponse();
        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetVideoInfos 失败 [VideoIds={}]: {}", videoIds, e.getErrMsg());
            return response;
        }
    }

    @Override
    public SubmitTranscodeJobsResponse SubmitTranscodeJobs(String videoId, String templateGroupId) {
        SubmitTranscodeJobsRequest request = new SubmitTranscodeJobsRequest();
        request.setVideoId(videoId);
        request.setTemplateGroupId(templateGroupId);
        SubmitTranscodeJobsResponse response = new SubmitTranscodeJobsResponse();
        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 SubmitTranscodeJobs 失败 [VideoId={}]: {}", videoId, e.getErrMsg());
            return response;
        }
    }

    @Override
    public GetImageInfosResponse GetImageInfos(String imageIds) {

        GetImageInfosRequest request = new GetImageInfosRequest();
        request.setImageIds(imageIds);
        GetImageInfosResponse response = new GetImageInfosResponse();
        try {
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetImageInfos 失败 [ImageIds={}]: {}", imageIds, e.getErrMsg());
            return response;
        }
    }

    /**
     * 创建播单
     * 播单是一种视频内容组织形式，可以将多个视频按顺序组织成一个播放列表
     *
     * @param request 创建播单请求对象
     *                - PlaylistName: 播单名称（必填）
     *                - PlaylistDescribe: 播单描述
     *                - PlaylistTags: 播单标签
     *                - PlaylistCoverUrl: 播单封面图URL
     *                - PlaylistVideos: 初始视频列表（JSON格式）
     * @return 创建播单响应，包含播单ID等信息
     */
    @Override
    public CreatePlaylistResponse CreatePlaylist(CreatePlaylistRequest request) {
        CreatePlaylistResponse response = new CreatePlaylistResponse();
        try {
            // 调用阿里云VOD API创建播单
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 CreatePlaylist 失败:  Code={}, Message={}", e.getErrCode(), e.getErrMsg());
            return response;
        }
    }

    /**
     * 删除播单
     * 默认只允许删除空播单，含内容的播单需设置强制删除标志
     * 注意：只会删除播单与视频的绑定关系，不会删除实际的视频媒资
     *
     * @param request 删除播单请求对象
     *                - PlaylistIds: 播单ID列表（逗号分隔）
     *                - ForceDelete: 是否强制删除（true:强制删除包含视频的播单）
     * @return 删除播单响应，包含删除结果信息
     */
    @Override
    public DeletePlaylistsResponse deletePlaylists(DeletePlaylistsRequest request) {
        DeletePlaylistsResponse response = new DeletePlaylistsResponse();

        try {
            // 调用阿里云VOD API删除播单
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 DeletePlaylists 失败: Code={}, Message={}", e.getErrCode(), e.getErrMsg());
            return response;
        }
    }

    /**
     * 查询单个播单详情
     * 获取指定播单的完整信息，包括播单基本信息和包含的视频列表
     *
     * @param request 查询播单请求对象
     *                - PlaylistId: 播单ID（必填）
     * @return 播单详情响应，包含：
     * - 播单基本信息（名称、描述、状态、标签等）
     * - 包含的视频列表及顺序
     * - 创建和修改时间
     */
    @Override
    public GetPlaylistResponse getPlaylist(GetPlaylistRequest request) {
        GetPlaylistResponse response = new GetPlaylistResponse();

        try {
            // 调用阿里云VOD API获取播单详情
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetPlaylist 失败 [PlaylistId={}]: {}", request.getPlaylistId(), e.getErrMsg());
            return response;
        }
    }

    /**
     * 查询播单列表（分页）
     * 获取当前账号下的所有播单列表，支持分页和排序
     *
     * @param request 查询播单列表请求对象
     *                - PageNo: 页码（默认1）
     *                - PageSize: 每页数量（默认10，最大100）
     *                - SortBy: 排序方式（CreationTime:Desc/Asc 按创建时间排序）
     * @return 播单列表响应，包含：
     * - 播单列表（基本信息）
     * - 总数量
     * - 分页信息
     */
    @Override
    public GetPlaylistsResponse getPlaylists(GetPlaylistsRequest request) {
        GetPlaylistsResponse response = new GetPlaylistsResponse();

        try {
            // 调用阿里云VOD API获取播单列表
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 GetPlaylists 失败: Code={}, Message={}", e.getErrCode(), e.getErrMsg());
            return response;
        }
    }


    /**
     * 更新播单基本信息
     * 修改播单的名称、描述、状态、标签、封面图等基本属性
     * 不影响播单中包含的视频列表
     *
     * @param request 更新播单基本信息请求对象
     *                - PlaylistId: 播单ID（必填）
     *                - PlaylistName: 播单名称
     *                - PlaylistDescribe: 播单描述
     *                - PlaylistStatus: 播单状态（Normal:正常, Disabled:禁用）
     *                - PlaylistTags: 播单标签（多个用逗号分隔）
     *                - PlaylistCoverUrl: 播单封面图URL
     *                - PlaylistOrderBy: 视频排序方式
     *                - PlaylistExtension: 扩展信息（JSON格式）
     * @return 更新播单基本信息响应
     */
    @Override
    public UpdatePlaylistBasicInfoResponse updatePlaylistBasicInfo(UpdatePlaylistBasicInfoRequest request) {
        UpdatePlaylistBasicInfoResponse response = new UpdatePlaylistBasicInfoResponse();

        try {
            // 调用阿里云VOD API更新播单基本信息
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 UpdatePlaylistBasicInfo 失败 [PlaylistId={}]: {}", request.getPlaylistId(), e.getErrMsg());
            return response;
        }
    }

    /**
     * 更新播单中指定视频的基本信息
     * 修改播单中某个视频的显示标题、描述、封面等信息
     * 可用于个性化展示，不影响原视频的实际属性
     *
     * @param request 更新播单视频基本信息请求对象
     *                - PlaylistId: 播单ID（必填）
     *                - OriginalVideoId: 原视频ID（必填）
     *                - NewVideoId: 新视频ID（用于替换视频）
     *                - Title: 视频标题（在播单中显示的标题）
     *                - Description: 视频描述
     *                - CoverUrl: 封面图URL
     * @return 更新播单视频基本信息响应
     */
    @Override
    public UpdatePlaylistVideoBasicInfoResponse updatePlaylistVideoBasicInfo(UpdatePlaylistVideoBasicInfoRequest request) {
        UpdatePlaylistVideoBasicInfoResponse response = new UpdatePlaylistVideoBasicInfoResponse();

        try {
            // 调用阿里云VOD API更新播单视频基本信息
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 UpdatePlaylistVideoBasicInfo 失败 [PlaylistId={}, OriginalVideoId={}]: {}", request.getPlaylistId(), request.getOriginalVideoId(), e.getErrMsg());
            return response;
        }
    }

    /**
     * 更新播单包含的视频列表
     * 完全替换播单中的视频列表，自动处理新增和删除操作
     * 按传入的视频ID顺序更新排序键
     *
     * @param request 更新播单视频列表请求对象
     *                - PlaylistId: 播单ID（必填）
     *                - VideoIds: 视频ID列表（逗号分隔，按顺序排列）
     *                系统会自动：
     *                1. 删除不在新列表中的视频
     *                2. 添加新列表中的新视频
     *                3. 更新所有视频的排序键
     * @return 更新播单视频列表响应
     */
    @Override
    public UpdatePlaylistVideosResponse updatePlaylistVideos(UpdatePlaylistVideosRequest request) {
        UpdatePlaylistVideosResponse response = new UpdatePlaylistVideosResponse();

        try {
            // 调用阿里云VOD API更新播单视频列表
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 UpdatePlaylistVideos 失败 [PlaylistId={}]: {}", request.getPlaylistId(), e.getErrMsg());
            return response;
        }
    }

    /**
     * 向播单中添加视频
     * 在指定位置插入一个或多个视频到播单中，不影响其他视频
     *
     * @param request 添加播单视频请求对象
     *                - PlaylistId: 播单ID（必填）
     *                - PreVideoId: 前置视频ID（新视频将插入到此视频之后，为空则添加到末尾）
     *                - PlaylistVideos: 要添加的视频列表（JSON格式）
     *                格式：[{"VideoId":"xxx","Title":"xxx","Description":"xxx"},...]
     * @return 添加播单视频响应
     */
    @Override
    public AddPlaylistVideosResponse addPlaylistVideos(AddPlaylistVideosRequest request) {
        AddPlaylistVideosResponse response = new AddPlaylistVideosResponse();

        try {
            // 调用阿里云VOD API添加播单视频
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 AddPlaylistVideos 失败 [PlaylistId={}]: {}", request.getPlaylistId(), e.getErrMsg());
            return response;
        }
    }

    /**
     * 从播单中删除视频
     * 删除播单中指定的一个或多个视频，不影响其他视频的顺序
     * 注意：只删除播单与视频的关联关系，不删除实际的视频媒资
     *
     * @param request 删除播单视频请求对象
     *                - PlaylistId: 播单ID（必填）
     *                - VideoIds: 要删除的视频ID列表（逗号分隔）
     * @return 删除播单视频响应
     */
    @Override
    public DeletePlaylistVideosResponse deletePlaylistVideos(DeletePlaylistVideosRequest request) {
        DeletePlaylistVideosResponse response = new DeletePlaylistVideosResponse();

        try {
            // 调用阿里云VOD API删除播单视频
            response = vodClient.getAcsResponse(request);
            return response;
        } catch (ClientException e) {
            log.error("调用 DeletePlaylistVideos 失败 [PlaylistId={}]: {}", request.getPlaylistId(), e.getErrMsg());
            return response;
        }
    }
}