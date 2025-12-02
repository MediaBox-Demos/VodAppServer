# 上传与媒体处理

本文档详细介绍 VodAppServer 的上传功能和媒体处理能力。

## 目录

- [上传功能](#上传功能)
- [媒体处理](#媒体处理)

---

## 上传功能

VodAppServer 支持多种媒资文件上传方式，包括音视频、图片和辅助媒资文件。上传功能基于阿里云 VOD 上传 SDK 实现，提供稳定高效的文件上传服务。

> 💡 **说明**：本文档展示的是**服务端 SDK 调用示例**，不是 HTTP 接口。如需在服务端集成上传功能，请参考示例代码：`src/main/java/com/aliyun/appserver/sample/upload/UploadVideoDemo.java`

### 音视频上传

支持4种上传方式：

1. **本地文件上传**：支持分片上传和断点续传，适合大文件上传
2. **网络流上传**：可指定文件URL进行上传，支持断点续传
3. **文件流上传**：可指定本地文件进行上传，不支持断点续传
4. **流式上传**：可指定输入流进行上传，支持文件流和网络流

#### 本地文件上传示例

```java
UploadVideoRequest request = new UploadVideoRequest(accessKeyId, accessKeySecret, title, fileName);
/* 可指定分片上传时每个分片的大小，默认为2M字节 */
request.setPartSize(2 * 1024 * 1024L);
/* 可指定分片上传时的并发线程数，默认为1 */
request.setTaskNum(1);
/* 是否开启断点续传 */
request.setEnableCheckpoint(true);

UploadVideoImpl uploader = new UploadVideoImpl();
UploadVideoResponse response = uploader.uploadVideo(request);

if (response.isSuccess()) {
    System.out.print("VideoId=" + response.getVideoId() + "\n");
} else {
    System.out.print("ErrorCode=" + response.getCode() + "\n");
    System.out.print("ErrorMessage=" + response.getMessage() + "\n");
}
```

### 图片上传

支持2种上传方式：

1. **本地文件上传**：不支持断点续传
2. **流式上传**：支持文件流和网络流

#### 图片上传示例

```java
// 图片类型（必选）取值范围：default（默认)，cover（封面），watermark（水印）
String imageType = "cover";
UploadImageRequest request = new UploadImageRequest(accessKeyId, accessKeySecret, imageType);
request.setImageType("cover");
// 图片标题（可选）
request.setTitle("短剧封面");
// 本地文件路径
String fileName = "/Users/test/image/cover.png";
request.setFileName(fileName);

UploadImageImpl uploadImage = new UploadImageImpl();
UploadImageResponse response = uploadImage.upload(request);

if (response.isSuccess()) {
    System.out.print("ImageId=" + response.getImageId() + "\n");
    System.out.print("ImageURL=" + response.getImageURL() + "\n");
} else {
    System.out.print("ErrorCode=" + response.getCode() + "\n");
    System.out.print("ErrorMessage=" + response.getMessage() + "\n");
}
```

### M3U8文件上传

支持2种上传方式：

1. **本地M3U8文件上传**：需要指定本地M3U8索引文件和所有分片文件
2. **网络M3U8文件上传**：需要指定M3U8索引文件和分片文件的URL地址

### 辅助媒资上传

支持2种上传方式：

1. **本地文件上传**：不支持断点续传
2. **流式上传**：支持文件流和网络流

### 上传进度回调

上传SDK支持进度回调通知：

1. **默认上传进度回调**：SDK内部默认开启上传进度回调函数
2. **自定义上传进度回调**：可根据业务场景重新定义事件处理方式

### STS方式上传

支持使用STS方式上传，需要实现VoDRefreshSTSTokenListener接口的onRefreshSTSToken方法，用于生成STS信息。当文件上传时间超过STS过期时间时，SDK内部会定期调用此方法刷新STS信息。

### 内网上传优化

可指定上传脚本部署的ECS区域，如果与点播存储（OSS）区域相同，则自动使用内网上传文件至存储，上传更快且更省公网流量。

---

## 媒体处理

VodAppServer 集成了阿里云媒体处理服务（MPS），提供视频转码、截图、水印等丰富的媒体处理功能。

### 转码处理

通过 SubmitTranscodeJobs 接口提交转码任务，支持使用自定义转码模板组对视频进行转码处理。

#### 转码示例

```java
@RestController
public class MpsController {
    @Autowired
    private VodSdkService vodSdkService;
    
    /**
     * 提交转码任务
     *
     * @param videoId         待转码的视频 ID
     * @param templateGroupId 使用的转码模板组 ID
     * @return 转码任务提交结果
     */
    @RequestMapping(value = "/submitTransCodeJob", method = {RequestMethod.GET, RequestMethod.POST})
    public SubmitTranscodeJobsResponse submitTransCodeJob(
            @RequestParam("videoId") String videoId, 
            @RequestParam("templateGroupId") String templateGroupId) {
        return vodSdkService.SubmitTranscodeJobs(videoId, templateGroupId);
    }
}
```

#### 使用说明

1. 登录[VOD控制台](https://vod.console.aliyun.com/)
2. 进入：`配置管理 → 媒体处理配置 → 转码模板组`
3. 选择或创建转码模板组，获取模板组ID
4. 调用接口提交转码任务

### 转码模板组配置

转码模板组包含多个转码模板，可一次性生成多种清晰度的视频输出：

- **流畅**：320x240, 15fps, 100-200kbps
- **标清**：640x360, 25fps, 400-600kbps
- **高清**：1280x720, 30fps, 1000-1500kbps
- **超清**：1920x1080, 30fps, 2000-3000kbps

### 其他媒体处理功能

VOD媒体处理服务还支持以下功能：

- **视频截图**：按时间点截图、按帧截图、雪碧图生成
- **内容审核**：色情、暴恐、广告、二维码检测
- **智能处理**：智能封面、智能标签、语音识别
- **水印处理**：图片水印、文字水印、动态水印

### 分批处理策略

```java
public void processBatch(List<String> allIds) {
    int batchSize = 50;  // API 限制每次50个
    
    for (int i = 0; i < allIds.size(); i += batchSize) {
        List<String> batch = allIds.subList(
            i, 
            Math.min(i + batchSize, allIds.size())
        );
        
        String ids = String.join(",", batch);
        GetVideoInfosResponse response = vodSdkService.GetVideoInfos(ids);
        
        // 处理这一批数据
        processBatchResult(response);
    }
}
```

---

## 下一步

- 📖 查看 [API-Guide.md](./API-Guide.md) 了解完整接口说明
- 🚀 探索 [Advanced-Features.md](./Advanced-Features.md) 实现高级功能
- 💡 参考 [Integration-Guide.md](./Integration-Guide.md) 进行项目集成

---

**最后更新**: 2025-12-02