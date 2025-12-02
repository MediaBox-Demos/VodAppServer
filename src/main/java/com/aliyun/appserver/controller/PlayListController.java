package com.aliyun.appserver.controller;

import com.aliyun.appserver.result.CallResult;
import com.aliyun.appserver.result.ResultCode;
import com.aliyun.appserver.service.PlayListService;
import com.aliyun.appserver.service.VodSdkService;
import com.aliyuncs.vod.model.v20170321.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * Playlist 播单控制器
 *
 * <p>
 * 提供播单创建、删除、查询、更新等接口，分为两类：
 * <ul>
 *     <li>直接透传到 VOD OpenAPI 的原子能力（返回 SDK Response）</li>
 *     <li>基于业务封装的增强能力（返回统一的 {@link CallResult}）</li>
 * </ul>
 * 控制器本身不做复杂业务处理，仅负责参数绑定与服务路由。
 * </p>
 *
 * @author: pxc
 * @date: 2025/11/18 09:57
 */
@RestController
@Validated
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class PlayListController {

    @Autowired
    private VodSdkService vodSdkService;

    @Autowired
    private PlayListService playListService;

    /**
     * 创建播单
     *
     * <p>对应 VOD OpenAPI：CreatePlaylist</p>
     */
    @RequestMapping(value = "/appServer/createPlaylist", method = {RequestMethod.GET, RequestMethod.POST})
    public CreatePlaylistResponse CreatePlaylist(@RequestBody CreatePlaylistRequest request) {
        return vodSdkService.CreatePlaylist(request);
    }

    /**
     * 删除播单
     *
     * <p>默认仅允许删除空播单，如需删除包含视频的播单需在请求中开启强制删除。</p>
     */
    @RequestMapping(value = "/appServer/deletePlaylists", method = {RequestMethod.GET, RequestMethod.POST})
    public DeletePlaylistsResponse deletePlaylists(@RequestBody DeletePlaylistsRequest request) {
        return vodSdkService.deletePlaylists(request);
    }

    /**
     * 查询单个播单详情
     *
     * <p>直接返回 VOD SDK 的 {@link GetPlaylistResponse}。</p>
     */
    @RequestMapping(value = "/appServer/getPlaylist", method = {RequestMethod.GET, RequestMethod.POST})
    public GetPlaylistResponse getPlaylist(@RequestBody GetPlaylistRequest request) {
        return vodSdkService.getPlaylist(request);
    }

    /**
     * 分页查询播单列表
     *
     * <p>直接返回 VOD SDK 的 {@link GetPlaylistsResponse}。</p>
     */
    @RequestMapping(value = "/appServer/getPlaylists", method = {RequestMethod.GET, RequestMethod.POST})
    public GetPlaylistsResponse getPlaylists(@RequestBody GetPlaylistsRequest request) {
        return vodSdkService.getPlaylists(request);
    }

    /**
     * 更新播单基础信息
     */
    @RequestMapping(value = "/appServer/updatePlaylistBasicInfo", method = {RequestMethod.GET, RequestMethod.POST})
    public UpdatePlaylistBasicInfoResponse updatePlaylistBasicInfo(@RequestBody UpdatePlaylistBasicInfoRequest request) {
        return vodSdkService.updatePlaylistBasicInfo(request);
    }

    /**
     * 更新播单中某个视频的基础信息
     */
    @RequestMapping(value = "/appServer/updatePlaylistVideoBasicInfo", method = {RequestMethod.GET, RequestMethod.POST})
    public UpdatePlaylistVideoBasicInfoResponse updatePlaylistVideoBasicInfo(@RequestBody UpdatePlaylistVideoBasicInfoRequest request) {
        return vodSdkService.updatePlaylistVideoBasicInfo(request);
    }

    /**
     * 覆盖更新播单包含的视频列表
     */
    @RequestMapping(value = "/appServer/updatePlaylistVideos", method = {RequestMethod.GET, RequestMethod.POST})
    public UpdatePlaylistVideosResponse updatePlaylistVideos(@RequestBody UpdatePlaylistVideosRequest request) {
        return vodSdkService.updatePlaylistVideos(request);
    }

    /**
     * 向播单中添加视频
     */
    @RequestMapping(value = "/appServer/addPlaylistVideos", method = {RequestMethod.GET, RequestMethod.POST})
    public AddPlaylistVideosResponse addPlaylistVideos(@RequestBody AddPlaylistVideosRequest request) {
        return vodSdkService.addPlaylistVideos(request);
    }

    /**
     * 从播单中删除指定视频
     */
    @RequestMapping(value = "/appServer/deletePlaylistVideos", method = {RequestMethod.GET, RequestMethod.POST})
    public DeletePlaylistVideosResponse deletePlaylistVideos(@RequestBody DeletePlaylistVideosRequest request) {
        return vodSdkService.deletePlaylistVideos(request);
    }

    /**
     * 获取播单详情
     *
     * <p>在基础播单信息上补充封面图 URL、视频播放凭证等业务字段，返回统一包装结果。</p>
     *
     * @param playListId 播单 ID，可为空；为空时由服务层返回默认播单。
     */
    @RequestMapping(value = "/appServer/getPlaylistInfo", method = {RequestMethod.GET, RequestMethod.POST})
    public CallResult getPlaylistInfo(@RequestParam(value = "playListId", required = false) String playListId) {
        return playListService.getPlaylistInfo(playListId);
    }

    /**
     * 获取播单列表
     *
     * <p>每个播单将携带一个预览视频及其播放凭证，返回统一包装结果。</p>
     */
    @RequestMapping(value = "/appServer/getPlaylistVideos", method = {RequestMethod.GET, RequestMethod.POST})
    public CallResult getPlaylistVideos(@RequestBody GetPlaylistsRequest request) {
        return playListService.getPlaylistVideos(request);
    }

    /**
     * 健康检查接口
     *
     * <p>用于检查服务是否正常运行</p>
     *
     * @return 健康检查结果
     */
    @RequestMapping(value = "/appServer/health", method = {RequestMethod.GET, RequestMethod.POST})
    public CallResult health() {
        CallResult result = new CallResult();

        result.setCode(ResultCode.SUCCESS)
                .setHttpCode("200");
        result.setMessage("服务响应成功");
        return result;
    }
}
