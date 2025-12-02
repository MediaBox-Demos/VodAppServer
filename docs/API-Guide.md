# API 指引

本文档提供 VodAppServer 当前可用 API 接口的详细说明。部分接口可能已被移除或更新。

## 目录

- [接口规范](#接口规范)
- [播单管理](#播单管理)
- [媒体处理](#媒体处理)
- [系统接口](#系统接口)
- [错误码说明](#错误码说明)

---

## 接口规范

### 基础信息

- **Base URL**: `http://localhost:9000`
- **Content-Type**: `application/json`
- **字符编码**: `UTF-8`
- **请求方法**: `POST`（支持 GET/POST）

⚠️ **重要提醒**：调用 VOD 的实体类和播单类接口需要提交工单进行加白后才可以使用。

### 统一响应格式（仅限业务增强接口 & 系统接口）

对于**业务增强接口**（如 `getPlaylistInfo` / `getPlaylistVideos`）以及**系统接口**（如健康检查），统一使用如下业务响应结构：

```json
{
  "code": 0,
  "httpCode": "200",
  "success": true,
  "message": "success",
  "data": {}
}
```

**字段说明**：
- `code`: 业务状态码（0 表示成功，详见「错误码说明」）
- `httpCode`: HTTP 状态码
- `success`: 是否成功（true/false）
- `message`: 响应消息
- `data`: 业务数据

> 说明：**直接透传 VOD SDK 的接口**（如创建播单、查询播单列表等），返回值结构与阿里云 VOD OpenAPI 中对应接口保持一致，请参考官方文档。

## 播单管理

### 3.1 创建播单

创建一个新的播单。

**接口地址**：`POST /appServer/createPlaylist`

> 返回值结构：与 VOD OpenAPI `CreatePlaylist` 接口保持一致，返回 `CreatePlaylistResponse`。

**请求参数**：
```json
{
  "playlistName": "精选短剧合集",
  "playlistDescribe": "2024年最受欢迎的短剧",
  "playlistTags": "爱情,都市,热播",
  "playlistCoverUrl": "IMAGE_ID",
  "playlistVideos": "[{\"VideoId\":\"video1\",\"Title\":\"第1集\"}]"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| playlistName | String | 是 | 播单名称 |
| playlistDescribe | String | 否 | 播单描述 |
| playlistTags | String | 否 | 标签（逗号分隔） |
| playlistCoverUrl | String | 否 | 封面图ID |
| playlistVideos | String | 否 | 初始视频列表（JSON数组） |

**响应示例**：
```json
{
  "playlistId": "pl_xxx",
  "requestId": "xxx"
}
```

---

### 3.2 删除播单

删除一个或多个播单。

**接口地址**：`POST /appServer/deletePlaylists`

> 返回值结构：与 VOD OpenAPI `DeletePlaylists` 接口保持一致，返回 `DeletePlaylistsResponse`。

**请求参数**：
```json
{
  "playlistIds": "pl_001,pl_002",
  "forceDelete": false
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| playlistIds | String | 是 | 播单ID列表（逗号分隔） |
| forceDelete | Boolean | 否 | 是否强制删除（默认false） |

⚠️ **注意**：
- `forceDelete=false`: 只能删除空播单
- `forceDelete=true`: 可删除含视频的播单
- 删除播单不会删除视频媒资

---

### 3.3 获取播单详情

获取单个播单的完整信息。

**接口地址**：`POST /appServer/getPlaylist`

> 返回值结构：与 VOD OpenAPI `GetPlaylist` 接口保持一致，返回 `GetPlaylistResponse`。

**请求参数**：
```json
{
  "playlistId": "pl_xxx"
}
```

**响应示例**：
```json
{
  "playlistId": "pl_xxx",
  "playlistName": "精选短剧合集",
  "playlistDescribe": "2024年最受欢迎的短剧",
  "playlistStatus": "Normal",
  "playlistTags": "爱情,都市,热播",
  "playlistCoverUrl": "https://xxx.oss.com/cover.jpg",
  "createTime": "2025-01-01T00:00:00Z",
  "playlistVideos": [
    {
      "videoId": "video1",
      "title": "第1集",
      "coverUrl": "https://xxx.oss.com/video1.jpg",
      "sortKey": 1
    }
  ]
}
```

---

### 3.4 获取播单列表

分页查询播单列表。

**接口地址**：`POST /appServer/getPlaylists`

> 返回值结构：与 VOD OpenAPI `GetPlaylists` 接口保持一致，返回 `GetPlaylistsResponse`。

**请求参数**：
```json
{
  "pageNo": "1",
  "pageSize": "10",
  "sortBy": "CreationTime:Desc"
}
```

---

### 3.5 更新播单基本信息

修改播单的名称、描述等基本属性。

**接口地址**：`POST /appServer/updatePlaylistBasicInfo`

**请求参数**：
```json
{
  "playlistId": "pl_xxx",
  "playlistName": "超级热播短剧",
  "playlistDescribe": "最新热播短剧精选",
  "playlistStatus": "Normal",
  "playlistTags": "热门,推荐",
  "playlistCoverUrl": "new_image_id"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| playlistId | String | 是 | 播单ID |
| playlistName | String | 否 | 播单名称 |
| playlistDescribe | String | 否 | 播单描述 |
| playlistStatus | String | 否 | 状态（Normal/Disabled） |
| playlistTags | String | 否 | 标签 |
| playlistCoverUrl | String | 否 | 封面图ID |

---

### 3.6 添加播单视频

向播单中添加一个或多个视频。

**接口地址**：`POST /appServer/addPlaylistVideos`

**请求参数**：
```json
{
  "playlistId": "pl_xxx",
  "preVideoId": "video1",
  "playlistVideos": "[{\"VideoId\":\"video2\",\"Title\":\"第2集\"}]"
}
```

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| playlistId | String | 是 | 播单ID |
| preVideoId | String | 否 | 前置视频ID（插入位置，为空则添加到末尾） |
| playlistVideos | String | 是 | 视频列表（JSON数组） |

**playlistVideos 格式**：
```json
[
  {
    "VideoId": "video2",
    "Title": "第2集",
    "Description": "剧情介绍",
    "CoverUrl": "image_id"
  }
]
```

---

### 3.7 删除播单视频

从播单中删除指定视频。

**接口地址**：`POST /appServer/deletePlaylistVideos`

**请求参数**：
```json
{
  "playlistId": "pl_xxx",
  "videoIds": "video1,video2"
}
```

⚠️ **注意**：只删除播单与视频的关联，不删除视频本身。

---

### 3.8 更新播单视频列表

完全替换播单的视频列表。

**接口地址**：`POST /appServer/updatePlaylistVideos`

**请求参数**：
```json
{
  "playlistId": "pl_xxx",
  "videoIds": "video1,video2,video3"
}
```

⚠️ **注意**：
- 会删除不在新列表中的视频
- 会添加新列表中的新视频
- 会更新所有视频的排序

---

### 3.9 获取播单详情（业务增强）

获取播单详情，包含播放凭证和封面图URL。

**接口地址**：`POST /appServer/getPlaylistInfo?playListId=pl_xxx`

**特点**：
- 自动生成视频播放凭证（playAuth）
- 封面图转换为实际URL
- 如不传 playListId，返回第一个播单

**响应示例**：
```json
{
  "code": 0,
  "httpCode": "200",
  "success": true,
  "data": {
    "playlistId": "pl_xxx",
    "playlistName": "精选短剧合集",
    "playlistCoverUrl": "https://xxx.oss.com/cover.jpg",
    "playlistVideos": [
      {
        "videoId": "video1",
        "title": "第1集",
        "playAuth": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
        "coverUrl": "https://xxx.oss.com/video1.jpg"
      }
    ]
  }
}
```

---

### 3.10 获取播单列表（业务增强）

获取播单列表，每个播单包含预览视频和播放凭证。

**接口地址**：`POST /appServer/getPlaylistVideos`

**请求参数**：
```json
{
  "pageNo": "1",
  "pageSize": "10"
}
```

**特点**：
- 每个播单包含第一个视频作为预览
- 预览视频包含播放凭证
- 封面图转换为实际URL

---

## 媒体处理

### 4.1 提交转码任务

为视频提交转码任务。

**接口地址**：`POST /submitTransCodeJob`

> 返回值结构：与 VOD OpenAPI `SubmitTranscodeJobs` 接口保持一致，返回 `SubmitTranscodeJobsResponse`。

**请求参数**：

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| videoId | String | 是 | 视频ID |
| templateGroupId | String | 是 | 转码模板组ID |

**示例**：
```bash
POST /submitTransCodeJob?videoId=xxx&templateGroupId=xxx
```

**转码模板组**：
- 在 VOD 控制台配置
- 路径：`配置管理 → 媒体处理配置 → 转码模板组`
- 支持多清晰度输出

---

## 错误码说明

### 业务错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 0 | 成功 | - |
| 10001 | 参数无效 | 检查请求参数格式 |
| 10002 | 参数为空 | 补充必填参数 |
| 10003 | 参数类型错误 | 检查参数类型 |
| 10004 | 参数缺失 | 补充缺失参数 |
| 40001 | 系统内部错误 | 查看日志排查 |

### HTTP 状态码

| 状态码 | 说明 |
|--------|------|
| 200 | 请求成功 |
| 400 | 请求参数错误 |
| 401 | 未授权 |
| 403 | 禁止访问 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

### 阿里云 VOD 错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| InvalidAccessKeyId.NotFound | AccessKey 不存在 | 检查 AccessKey 配置 |
| SignatureDoesNotMatch | 签名错误 | 检查 SecretKey 配置 |
| InvalidParameter | 参数无效 | 检查 API 参数格式 |
| EntityNotExist | 实体不存在 | 确认实体ID正确 |
| MediaNotExist | 媒资不存在 | 确认媒资ID正确 |

完整错误码请参考：[阿里云 VOD 错误码](https://help.aliyun.com/document_detail/61075.html)

---

## 接口调用示例

### Postman 导入

可导入以下 Collection 快速测试：

```json
{
  "info": {
    "name": "VodAppServer API",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "获取短剧列表",
      "request": {
        "method": "POST",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": {
          "mode": "raw",
          "raw": "{\"pageNo\":1,\"pageSize\":10,\"sortBy\":\"desc\"}"
        },
        "url": "{{baseUrl}}/dramaList"
      }
    }
  ],
  "variable": [
    {
      "key": "baseUrl",
      "value": "http://localhost:9000"
    }
  ]
}
```

---

## 系统接口

### Health Check 健康检查

用于检查服务是否正常运行。

**接口地址**：`GET/POST /appServer/health`

**请求示例**：
```bash
curl -X GET http://localhost:9000/appServer/health
```

**响应示例**：
```json
{
  "code": 0,
  "httpCode": "200",
  "success": true,
  "message": "服务响应成功"
}
```

**字段说明**：
- `code`: 业务状态码（0表示成功）
- `httpCode`: HTTP 状态码
- `success`: 是否成功（true/false）
- `message`: 响应消息

---

## 下一步

- 📖 查看 [Quick-Start.md](./Quick-Start.md) 了解基本使用
- 🚀 探索 [Advanced-Features.md](./Advanced-Features.md) 实现复杂业务
- 📤 了解 [Upload-and-MPS.md](./Upload-and-MPS.md) 的上传与媒体处理能力
- 💡 参考 [Integration-Guide.md](./Integration-Guide.md) 集成到生产环境

---

**最后更新**: 2025-12-02
