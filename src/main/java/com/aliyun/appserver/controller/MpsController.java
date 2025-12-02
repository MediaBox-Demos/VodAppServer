package com.aliyun.appserver.controller;

import com.aliyun.appserver.service.VodSdkService;
import com.aliyuncs.vod.model.v20170321.SubmitTranscodeJobsResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 媒体处理控制器（MPS）
 *
 * <p>当前主要用于提交转码任务，将转码请求转发给 VOD 媒体处理服务。</p>
 *
 * @author: pxc
 * @date: 2025/11/6 13:47
 */
@RestController
@Validated
@CrossOrigin(origins = "*", allowedHeaders = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS})
public class MpsController {

    @Autowired
    private VodSdkService vodSdkService;

    /**
     * 提交转码任务
     *
     * <p>
     * 对应 VOD OpenAPI：SubmitTranscodeJobs。<br/>
     * {@code templateGroupId} 为转码模板组 ID，可在 VOD 控制台
     * 「配置管理 → 媒体处理配置 → 转码模板组」中查看。
     * </p>
     *
     * @param videoId         待转码的视频 ID
     * @param templateGroupId 使用的转码模板组 ID
     * @return 转码任务提交结果
     */
    @RequestMapping(value = "/submitTransCodeJob", method = {RequestMethod.GET, RequestMethod.POST})
    public SubmitTranscodeJobsResponse submitTransCodeJob(@RequestParam("videoId") String videoId, @RequestParam("templateGroupId") String templateGroupId) {
        //templateGroupId视频转码时使用的转码组 ID。使用指定的模板组进行转码，您可以登录点播控制台，选择配置管理 >媒体处理配置 > 转码模板组查看模版组 ID。
        return vodSdkService.SubmitTranscodeJobs(videoId, templateGroupId);
    }
}
