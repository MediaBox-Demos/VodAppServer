package com.aliyun.appserver.entity;

import com.aliyuncs.vod.model.v20170321.GetPlaylistResponse;
import com.aliyuncs.vod.model.v20170321.GetVideoInfosResponse;
import lombok.Data;

/**
 * 播单视频条目 DTO（业务视图）
 *
 * <p>
 * 在阿里云 VOD 播单明细 {@link GetPlaylistResponse.PlaylistItemDO} 的基础上，
 * 增加了 {@link #playAuth} 字段，用于承载生成的视频播放凭证。
 * </p>
 *
 * <p>
 * 注意：当前实现直接继承了 SDK 的 PlaylistItemDO 类型，主要用于 Demo 使用，
 * 生产环境建议根据需要拆分为独立 DTO，以降低对 SDK 的耦合。
 * </p>
 *
 * @author: pxc
 * @date: 2025/11/18 13:25
 */
@Data
public class PlaylistItemDto extends GetPlaylistResponse.PlaylistItemDO {

    /**
     * 播放凭证（JWT Token），由服务端生成，用于安全播放当前视频
     */
    private String playAuth;

    /**
     * 由 VOD 播单明细构造条目对象
     */
    public PlaylistItemDto(GetPlaylistResponse.PlaylistItemDO playlistItemDO) {
        this.setPlaylistId(playlistItemDO.getPlaylistId());
        this.setVideoId(playlistItemDO.getVideoId());
        this.setTitle(playlistItemDO.getTitle());
        this.setDescription(playlistItemDO.getDescription());
        this.setCoverUrl(playlistItemDO.getCoverUrl());
    }

    /**
     * 由 VOD 视频信息构造条目对象
     */
    public PlaylistItemDto(GetVideoInfosResponse.Video video) {
        this.setVideoId(video.getVideoId());
        this.setTitle(video.getTitle());
        this.setDescription(video.getDescription());
        this.setCoverUrl(video.getCoverURL());
    }
}
