package com.aliyun.appserver.entity;

import com.aliyuncs.vod.model.v20170321.GetPlaylistResponse;
import com.aliyuncs.vod.model.v20170321.GetPlaylistsResponse;
import lombok.Data;

import java.util.List;

/**
 * 播单实体（业务视图）
 *
 * <p>
 * 在阿里云 VOD 播单基础信息 {@link GetPlaylistsResponse.PlaylistDO} 的基础上，
 * 增加了 {@link #playlistVideos} 字段，用于承载当前播单下的视频列表。
 * </p>
 *
 * <p>
 * 注意：当前实现直接继承了 SDK 的 PlaylistDO 类型，便于 Demo 直观展示返回结果，
 * 生产环境中可根据实际情况将其解耦为独立领域模型。
 * </p>
 *
 * @author: pxc
 * @date: 2025/11/18 13:21
 */
@Data
public class PlayList extends GetPlaylistsResponse.PlaylistDO {

    /**
     * 播单包含的视频列表（增强字段）
     */
    private List<PlaylistItemDto> playlistVideos;

    private Integer total;

    /**
     * 由 {@link GetPlaylistResponse} 构造播单实体
     */
    public PlayList(GetPlaylistResponse getPlaylistResponse) {
        this.setPlaylistId(getPlaylistResponse.getPlaylistId());
        this.setPlaylistName(getPlaylistResponse.getPlaylistName());
        this.setPlaylistDescribe(getPlaylistResponse.getPlaylistDescription());
        this.setPlaylistStatus(getPlaylistResponse.getPlaylistStatus());
        this.setPlaylistTags(getPlaylistResponse.getPlaylistTags());
        this.setPlaylistCoverUrl(getPlaylistResponse.getPlaylistCoverUrl());
        this.setPlaylistOrderBy(getPlaylistResponse.getPlaylistOrderBy());
        this.setPlaylistExtension(getPlaylistResponse.getPlaylistExtension());
        this.setCreateTime(getPlaylistResponse.getCreateTime());
        this.setModifyTime(getPlaylistResponse.getModifyTime());
        this.setTotal(getPlaylistResponse.getTotal());
    }

    /**
     * 由 {@link GetPlaylistsResponse.PlaylistDO} 构造播单实体
     */
    public PlayList(GetPlaylistsResponse.PlaylistDO playlistDO) {
        this.setPlaylistId(playlistDO.getPlaylistId());
        this.setPlaylistName(playlistDO.getPlaylistName());
        this.setPlaylistDescribe(playlistDO.getPlaylistDescribe());
        this.setPlaylistStatus(playlistDO.getPlaylistStatus());
        this.setPlaylistTags(playlistDO.getPlaylistTags());
        this.setPlaylistCoverUrl(playlistDO.getPlaylistCoverUrl());
        this.setPlaylistOrderBy(playlistDO.getPlaylistOrderBy());
        this.setPlaylistExtension(playlistDO.getPlaylistExtension());
        this.setCreateTime(playlistDO.getCreateTime());
        this.setModifyTime(playlistDO.getModifyTime());
    }
}
