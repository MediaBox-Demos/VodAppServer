# **播单管理**

本文档详细介绍 VodAppServer 中播单（Playlist）的概念、技术实现和管理方式。

⚠️ **重要提醒**：调用 VOD 的实体类和播单类接口需要提交工单进行加白后才可以使用。

## **目录**

- [从单一媒资到媒资列表](#从单一媒资到媒资列表)
- [播单：基于动态实体的 VOD 官方能力](#播单基于动态实体的-vod-官方能力)
- [播单结构](#播单结构)
- [播单视频结构](#播单视频结构)
- [VideoId 来源](#videoid-来源)
- [创建播单示例](#创建播单示例)
- [扩展能力说明](#扩展能力说明)
- [播单管理接口](#播单管理接口)

---

## **从单一媒资到媒资列表**

### **媒资（VideoId）：管理单一媒资**

在 VOD 系统中，**媒资（VideoId）** 是最基础的单元，用于管理单个音视频文件：

- **单一媒资管理**：每个上传到 VOD 的音视频文件都会获得一个唯一的 `VideoId`
- **媒资属性**：包含视频的基本信息（标题、描述、封面、时长、清晰度等）
- **业务场景**：适合管理独立的视频内容

### **播单（Playlist）：管理媒资列表**

在实际业务场景中（如短剧、连续剧），往往需要将多个视频组织成一个有序的集合：

- **媒资列表管理**：播单（Playlist）用于管理一组有序的视频媒资
- **业务抽象**：例如，10 个 `VideoId` 可以组成 1 部剧集
- **有序组织**：支持按顺序播放、预览、分集展示等业务需求

**典型场景**：

```
10 个 VideoId（第1集、第2集...第10集）
    ↓
1 个 Playlist（《某短剧全集》）
```

---

## **播单：基于动态实体的 VOD 官方能力**

### **动态实体（Entity）的背景**

在存储媒资（VideoId）后，客户经常遇到以下业务问题：

- **自定义业务信息存储**：封面图、介绍（describe）、标签、分类等业务属性
- **业务元数据管理**：需要存储与业务相关的 key-value 信息（Map 结构）

**动态实体（Entity）** 正是为了解决客户存储自定义业务信息的需求而引入的 VOD 官方能力：

- **灵活存储**：支持 key-value 形式的自定义业务信息集合
- **业务扩展**：客户可以根据业务需求定义自己的实体属性和关联关系
- **官方能力**：基于 VOD 动态实体机制，提供稳定可靠的数据存储

### **播单的实现方式**

**播单（Playlist）是基于动态实体（Entity）这一基础能力搭建的业务形态**：

- **技术基础**：播单和播单视频（PlaylistItem）基于阿里云 VOD 动态实体创建
- **容器实体**：播单作为容器实体，包含名称、描述、状态、标签、封面地址等属性
- **关联实体**：播单视频作为关联实体，通过 `PlaylistId` 与播单关联，通过 `VideoId` 与视频媒资绑定

**抽象理解**：
```
动态实体（Entity）
    ↓
播单（Playlist）← 基于动态实体搭建的业务形态
    ↓
播单视频（PlaylistItem）← 关联实体，绑定 VideoId
```

---

## **播单结构**

播单依赖于 VOD 官方的 **动态实体** 创建，类似于预置的实体类。

| 属性 | 类型 | 非空 | 说明 |
|------|------|------|------|
| PlaylistName（名称） | string | 是 | 播单名称 |
| PlaylistDescribe（描述） | string | 否 | 播单描述 |
| PlaylistStatus（状态） | enum | 否 | 播单状态（Normal/Disabled） |
| PlaylistTags（标签） | String | 否 | 多个按','隔开 |
| PlaylistCoverUrl（封面地址） | String | 否 | 播单封面地址（ImageId） |
| PlaylistOrderBy（排序规则） | String | 是 | ● asc（默认）<br>● desc<br>按照SortKey升序/降序展示，默认升序 |
| Total | Integer | 是 | 集数 |
| PlaylistExtension（拓展信息） | String | 否 | 拓展信息（JSON格式，可用于配置预览视频等） |
| CreateTime(创建时间) | String | 是 | 创建时间 |
| ModifyTime（修改时间） | String | 是 | 修改时间 |
| PlaylistId（实体内容ID） | String | 是 | 播单ID，即实体的内容ID，唯一 |

---

## **播单视频结构**

播单视频（PlaylistItem）是播单与视频媒资的关联实体：

| 属性 | 类型 | 非空 | 说明 |
|------|------|------|------|
| PlaylistId(播单ID) | EntityMediaId | 是 | 所属播单 |
| VideoId（播放媒资） | NormalMedia，单值 | 是 | 播放的媒资ID |
| SortKey（排序键） | Double | 是 | 用于按序展示 |
| Title（标题） | String | 否 | 默认取自媒资，可在播单中自定义 |
| Description（描述） | String | 否 | 默认取自媒资，可在播单中自定义 |
| CoverUrl（封面） | String | 否 | 默认取自媒资，可在播单中自定义 |

---

## **VideoId 来源**

播单视频结构中的 `VideoId` 来源于阿里云 VOD 的媒资管理能力：

### **1. 上传获取 VideoId**

通过 VOD 上传 SDK 将音视频文件上传至 VOD 平台，上传成功后返回唯一的 `VideoId`：

- **本地文件上传**：支持分片上传和断点续传，适合大文件上传
- **网络流上传**：可指定文件URL进行上传，支持断点续传
- **文件流上传**：可指定本地文件进行上传
- **流式上传**：可指定输入流进行上传

> 💡 **详细说明**：上传功能的最佳实践和示例代码，请参考 [上传与媒体处理](./Upload-and-MPS.md) 文档。

### **2. 媒资绑定**

将获取的 `VideoId` 与播单视频实体进行绑定，建立播单与视频媒资的关联关系。

### **3. 关联管理**

通过 `VideoId` 可以查询、管理对应的音视频内容，实现播单与媒资的统一管理。

---

## **创建播单示例**

以下是一个创建播单的示例请求：

**接口地址**：`POST /appServer/createPlaylist`

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

**参数说明**：
- `playlistName`：播单名称，必填
- `playlistDescribe`：播单描述，选填
- `playlistTags`：播单标签，多个标签用逗号分隔，选填
- `playlistCoverUrl`：播单封面图ID（ImageId），选填
- `playlistVideos`：初始视频列表，JSON数组格式，选填

> 📚 **完整接口文档**：详细的播单管理接口请参考 [API 指引](./API-Guide.md#播单管理) 文档。

---

## **扩展能力说明**

### **1. 播单封面图处理**

播单的 `playlistCoverUrl` 字段支持通过 ImageId 获取封面图：

- 通过 VOD 媒资管理能力上传封面图类型的媒资，获取 `ImageId`
- AppServer 会自动将 `ImageId` 转换为可访问的封面图 URL
- 在播单详情接口（`getPlaylistInfo`）中，系统会自动返回完整的封面图 URL

### **2. 视频播放凭证生成**

播单中的每个视频都会自动生成 JWT 播放凭证（playAuth）：

- **本地签名方案**：基于 VOD 的 PlayKey 本地签名生成，无需每次调用 VOD OpenAPI
- **性能优势**：相比传统方式（每次播放调用 `GetVideoPlayAuth`），本地签名减少了一次远程调用
- **业务增强接口**：在 `getPlaylistInfo` 和 `getPlaylistVideos` 接口中，系统会自动为每个视频生成 `playAuth`
- **客户端使用**：客户端可直接使用 `videoId + playAuth` 进行视频播放

> 📘 **详细说明**：
> - 播放鉴权方案对比，请参考 [播放鉴权方案](./vidauth-design.md)
> - JWT 播放鉴权实践，请参考 [高级功能 - JWT 播放鉴权](./Advanced-Features.md#jwt-播放鉴权)
> - **注意**：使用本地签名生成的 `vid + playAuth` 进行播放时，客户端播放器 SDK 版本需要 **≥ 7.10.0**

### 3. 预览效果实现

在播单 `playlistExtension` 扩展参数上配置包含 `previewVideoId` 的 JSON 数据，可以实现预览效果：

**配置示例**：

```json
{
  "previewVideoId": "f0d8a2b3ce8d71f0bf8e4531959c0402"
}
```

**配置说明**：
- `previewVideoId`：需要预览的视频 VideoId

**实现效果**：
- 在调用获取播单列表接口（`getPlaylistVideos`）时，系统会返回需要预览的剧集信息
- 同时返回预览视频的播放 `playAuth`，便于客户端直接播放

---

## **播单管理接口**

VodAppServer 提供了完整的播单管理接口，包括：

### 基础接口（透传 VOD SDK）

- `POST /appServer/createPlaylist` - 创建播单
- `POST /appServer/deletePlaylists` - 删除播单
- `POST /appServer/getPlaylist` - 获取播单详情
- `POST /appServer/getPlaylists` - 获取播单列表
- `POST /appServer/updatePlaylistBasicInfo` - 更新播单基本信息
- `POST /appServer/updatePlaylistVideoBasicInfo` - 更新播单视频基本信息
- `POST /appServer/updatePlaylistVideos` - 更新播单视频列表
- `POST /appServer/addPlaylistVideos` - 添加播单视频
- `POST /appServer/deletePlaylistVideos` - 删除播单视频

### **业务增强接口（统一返回 CallResult）**

- `GET/POST /appServer/getPlaylistInfo` - 获取播单详情（含播放凭证和封面图URL）
- `POST /appServer/getPlaylistVideos` - 获取播单列表（含预览视频和播放凭证）

> 📚 **完整接口文档**：详细的接口说明、请求参数、响应格式，请参考 [API 指引](./API-Guide.md#播单管理) 文档。

---

## **下一步**

- 📖 查看 [API 指引](./API-Guide.md) 了解完整接口说明
- 🚀 探索 [高级功能](./Advanced-Features.md) 了解 JWT 播放鉴权实践
- 💡 参考 [上传与媒体处理](./Upload-and-MPS.md) 了解视频上传最佳实践

---

**最后更新**: 2025-12-02
