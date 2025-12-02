package com.aliyun.appserver.service;

import com.aliyun.appserver.result.CallResult;
import com.aliyuncs.vod.model.v20170321.GetPlaylistsRequest;

/**
 * 播单业务服务接口
 *
 * <p>
 * 封装基于 VOD 播单能力的业务逻辑，提供：
 * <ul>
 *     <li>播单详情查询（含视频列表与播放凭证）</li>
 *     <li>播单列表查询（含预览视频与封面处理）</li>
 * </ul>
 * 返回统一的 {@link CallResult} 结果模型。
 * </p>
 *
 * @author: pxc
 * @date: 2025/11/18 16:03
 */
public interface PlayListService {

    /**
     * 获取单个播单详情（含视频列表和播放凭证等增强信息）
     *
     * @param playListId 播单 ID，可为空；为空时由实现决定默认行为
     * @return 统一封装的业务结果
     */
    CallResult getPlaylistInfo(String playListId);

    /**
     * 分页获取播单列表（含预览视频和播放凭证等增强信息）
     *
     * @param request 播单列表查询请求参数
     * @return 统一封装的业务结果
     */
    CallResult getPlaylistVideos(GetPlaylistsRequest request);
}
