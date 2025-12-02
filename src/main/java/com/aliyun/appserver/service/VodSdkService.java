package com.aliyun.appserver.service;

import com.aliyuncs.vod.model.v20170321.*;

/**
 * VOD SDK 访问服务接口
 *
 * <p>对阿里云 VOD OpenAPI 的 Java SDK 进行一层封装，统一由该接口对外暴露。</p>
 *
 * <p>注意：本接口主要偏向网关/适配层，建议在上层 Service 中进行业务聚合与领域转换。</p>
 *
 * @author: pxc
 * @date: 2025/10/14 14:54
 */
public interface VodSdkService {

    /**
     * 1. 创建实体
     */
    CreateEntityResponse createEntity(String entityName, String attributeIds, String syncAttributeIds);

    /**
     * 2. 删除实体
     */
    DeleteEntityResponse deleteEntity(String entityId);

    /**
     * 3. 更新实体
     */
    UpdateEntityResponse updateEntity(String entityId, String attributeIds, String syncAttributeIds);

    /**
     * 4. 获取实体
     */
    GetEntityResponse getEntity(String entityId);

    /**
     * 5. 创建实体属性
     */
    CreateEntityAttributeResponse createEntityAttribute(String attributeName,  Long type, Long length, String defaultValue);

    /**
     * 6. 删除实体属性
     */
    DeleteEntityAttributeResponse deleteEntityAttribute(String attributeId);

    /**
     * 7. 获取实体属性
     */
    GetEntityAttributeResponse getEntityAttribute(String attributeId);

    /**
     * 8. 注册实体媒资
     */
    RegisterEntityMediaResponse registerEntityMedia(String entityId, String dynamicMetaData);

    /**
     * 9. 删除实体媒资（多个）
     */
    DeleteEntityMediasResponse deleteEntityMedias(String entityMediaIds, Boolean forceDelete);

    /**
     * 10. 更新实体媒资
     */
    UpdateEntityMediaResponse updateEntityMedia(String entityMediaId, String entityId, String dynamicMetaData);

    /**
     * 11. 获取实体媒资
     */
    GetEntityMediaResponse getEntityMedia(String entityMediaId, Boolean needParent, Boolean needSub);

    /**
     * 12. 获取实体属性列表（分页）
     */
    GetEntityAttributeListResponse getEntityAttributeList(Integer pageNo, Integer pageSize, String sortBy);

    /**
     * 13. 获取实体媒资列表（分页）
     */
    GetEntityMediaListResponse getEntityMediaList(String entityId, Integer pageNo, Integer pageSize, String sortBy);

    /**
     * 14. 获取实体列表（分页）
     */
    GetEntityListResponse getEntityList(Integer pageNo, Integer pageSize, String sortBy);


    GetAppPlayKeyResponse GetAppPlayKey(String appId);

    SetAppPlayKeyResponse SetAppPlayKey(String appId, String playKey);

    GetPlayInfoResponse GetPlayInfo(String videoId);

    GetVideoInfosResponse GetVideoInfos(String videoIds);

    SubmitTranscodeJobsResponse SubmitTranscodeJobs(String videoId, String templateGroupId);

    GetImageInfosResponse GetImageInfos(String imageIds);

    CreatePlaylistResponse CreatePlaylist(CreatePlaylistRequest request);

    /**
     * 15. 删除播单
     * 默认只允许删除空播单，含内容的播单需设置强制删除
     * 只会删除视频与播单的绑定关系，不会删除实际媒资
     */
    DeletePlaylistsResponse deletePlaylists(DeletePlaylistsRequest request);

    /**
     * 16. 查询单个播单
     */
    GetPlaylistResponse getPlaylist(GetPlaylistRequest request);

    /**
     * 17. 查询播单列表
     */
    GetPlaylistsResponse getPlaylists(GetPlaylistsRequest request);


    /**
     * 18. 修改播单基本信息
     */
    UpdatePlaylistBasicInfoResponse updatePlaylistBasicInfo(UpdatePlaylistBasicInfoRequest request);

    /**
     * 19. 修改播单Video基本信息
     */
    UpdatePlaylistVideoBasicInfoResponse updatePlaylistVideoBasicInfo(UpdatePlaylistVideoBasicInfoRequest request);

    /**
     * 20. 更新播单包含的Video
     * 校验已有/新增/删除的播单video，新增/删除对应的播单video及关系
     * 按序更新排序键
     */
    UpdatePlaylistVideosResponse updatePlaylistVideos(UpdatePlaylistVideosRequest request);

    /**
     * 21. 添加播单videos
     */
    AddPlaylistVideosResponse addPlaylistVideos(AddPlaylistVideosRequest request);

    /**
     * 22. 删除播单videos
     */
    DeletePlaylistVideosResponse deletePlaylistVideos(DeletePlaylistVideosRequest request);


}
