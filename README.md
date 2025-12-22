# VodAppServer

基于 Spring Boot 的阿里云视频点播（VOD）短剧管理平台后端服务

## 📋 项目概述

本项目是一个专为短剧、短视频内容管理设计的后端服务系统，基于阿里云 VOD（视频点播）服务构建，提供完整的实体管理、媒资管理、榜单服务和视频播放鉴权功能。

### 核心功能

- 🎬 **VOD 实体管理**：支持实体、实体属性、实体媒资的完整 CRUD 操作
- 📺 **短剧业务服务**：榜单管理、短剧详情、短剧列表查询
- 🔐 **JWT 播放鉴权**：安全的视频播放凭证生成
- 🎞️ **媒体处理**：视频转码任务提交
- 📤 **视频上传**：集成阿里云 VOD 上传 SDK

## 🏗️ 技术架构

### 技术栈

| 技术 | 版本 | 说明 |
|------|------|------|
| **Java** | 1.8 | 编程语言 |
| **Spring Boot** | 2.6.6 | 应用框架 |
| **Maven** | - | 构建工具 |
| **阿里云 VOD SDK** | 2.16.39 | 视频点播服务 |
| **阿里云 OSS SDK** | 3.17.3 | 对象存储服务 |
| **阿里云 STS SDK** | 3.0.0 | 安全令牌服务 |
| **JWT** | 4.4.0 | 身份认证 |
| **Fastjson** | 1.2.83 | JSON 处理 |
| **Gson** | 2.11.0 | JSON 处理 |
| **Lombok** | - | 代码简化 |
| **Commons Lang3** | 3.12.0 | 工具库 |

### 项目结构

```
VodAppServer/
├── src/main/java/com/aliyun/appserver/
│   ├── Application.java          # Spring Boot 启动类
│   ├── config/
│   │   └── VodConfig.java        # VOD 接入配置（AK/SK 等）
│   ├── controller/               # 控制器层（HTTP 接口）
│   │   ├── PlayListController.java   # 播单管理 & 业务增强接口
│   │   └── MpsController.java        # 媒体处理（提交转码任务）
│   ├── service/
│   │   ├── VodSdkService.java        # VOD OpenAPI 适配接口
│   │   ├── PlayListService.java      # 播单业务服务接口
│   │   └── impl/                     # 服务实现
│   ├── entity/
│   │   ├── PlayList.java             # 播单业务视图（含视频列表）
│   │   └── PlaylistItemDto.java      # 播单视频条目（含 playAuth）
│   ├── jwt/                          # 本地签名播放凭证（JWTPlayAuth）实现
│   ├── result/                       # 统一返回结果模型
│   └── sample/                       # 独立上传 SDK 示例（不参与主业务）
├── src/main/resources/
│   └── application.yml               # 应用配置
└── pom.xml                           # Maven 配置
```

## 📚 文档导航

> 📖 **完整文档**：查看 [文档目录](./docs/README.md) 获取详细的集成指南、API 说明、快速开始等完整文档。

## 🚀 快速开始

### 环境要求

- JDK 1.8+
- Maven 3.x
- 阿里云 VOD 账号（AccessKey 和 SecretKey）

### 配置说明

修改 `src/main/resources/application.yml`：

```yaml
project:
  server:
    port: 9000

aliyun:
  vod:
    # 阿里云 AccessKeyId（必填）
    ak: YOUR_ACCESS_KEY
    # 阿里云 AccessKeySecret（必填）
    sk: YOUR_SECRET_KEY
    # 地域标识（Region ID，必填）
    # 当前支持的地域：
    #   - 上海：cn-shanghai（默认）
    #   - 北京：cn-beijing
    #   - 深圳：cn-shenzhen
    #   - 新加坡：ap-southeast-1
    # 后续考虑支持：美西（us-west-1）
    region: cn-shanghai
```

**配置说明**：
- `ak` / `sk`：阿里云账号的 AccessKey，用于访问 VOD 服务
- `region`：VOD 服务的地域标识，必须与 VOD 控制台中开通服务的区域一致
- ⚠️ **安全提示**：生产环境建议使用环境变量或密钥管理服务，不要将密钥提交到代码仓库

### 构建运行

```bash
# 构建项目
mvn clean package

# 运行项目
java -jar target/VodAppServer-1.0-SNAPSHOT.jar

# 或使用 Maven 直接运行
mvn spring-boot:run
```

服务将在 `http://localhost:9000` 启动

## 📡 API 接口文档

### 1. 播单管理（PlayListController）

**基础路径**：`/appServer`

- `POST /appServer/createPlaylist`  
  - 说明：创建播单，对应 VOD OpenAPI `CreatePlaylist`，请求体为阿里云 SDK 的 `CreatePlaylistRequest`。  
- `POST /appServer/deletePlaylists`  
  - 说明：删除一个或多个播单，请求体为 `DeletePlaylistsRequest`。  
- `POST /appServer/getPlaylist`  
  - 说明：查询单个播单详情，返回 SDK `GetPlaylistResponse`。  
- `POST /appServer/getPlaylists`  
  - 说明：分页查询播单列表，返回 SDK `GetPlaylistsResponse`。  
- `POST /appServer/updatePlaylistBasicInfo` / `updatePlaylistVideoBasicInfo` / `updatePlaylistVideos` / `addPlaylistVideos` / `deletePlaylistVideos`  
  - 说明：对播单及播单内视频做增删改操作，入参与出参均为 VOD SDK 对应的 Request/Response 类型。

#### 1.1 业务增强接口（统一返回 `CallResult`）

- `GET/POST /appServer/getPlaylistInfo`  
  - 入参：`playListId`（可选，为空时返回第一个播单）；  
  - 返回：`CallResult<PlayList>`，在 SDK 播单信息基础上，补充封面图 URL、视频播放凭证等业务字段。

- `POST /appServer/getPlaylistVideos`  
  - 入参：`GetPlaylistsRequest`（分页参数）；  
  - 返回：`CallResult<List<PlayList>>`，每个播单附带一个预览视频及其 `playAuth`。

### 2. 媒体转码（MpsController）

#### 2.1 提交转码任务

```http
GET/POST /submitTransCodeJob?videoId=xxx&templateGroupId=xxx
```

**参数说明**：
- `videoId`: 视频ID
- `templateGroupId`: 转码模板组ID（可在阿里云控制台查看）

### 3. 上传与媒体处理

VodAppServer 提供完整的上传和媒体处理功能，详细说明请参考独立文档：
- [Upload-and-MPS.md](docs/Upload-and-MPS.md)

该文档包含：
- 音视频上传（本地文件、网络流、文件流、流式上传）
- 图片上传（本地文件、流式上传）
- M3U8文件上传
- 辅助媒资上传
- 上传进度回调
- STS方式上传
- 内网上传优化
- 媒体处理（转码、截图、水印等）

## 🎨 业务模型（当前代码）

### 播单与视频关系

```
PlayList（播单）
  └── PlaylistItemDto（播单视频条目，含 playAuth）
```

### 核心实体说明

#### PlayList（播单）
- 继承自 VOD SDK 的 `GetPlaylistsResponse.PlaylistDO`，并增加字段：  
  - `playlistVideos`: 当前播单下的视频列表（`List<PlaylistItemDto>`）。

#### PlaylistItemDto（播单视频条目）
- 继承自 VOD SDK 的 `GetPlaylistResponse.PlaylistItemDO`，并增加字段：  
  - `playAuth`: 播放凭证字段，承载 JWTPlayAuth（AppServer 基于 PlayKey 本地签名生成，点播校验）。

## 🔐 安全与播放鉴权说明

### 本地签名播放凭证（JWTPlayAuth）

系统使用 **基于 PlayKey 的本地 JWT 签名** 方式生成本地签名播放凭证 JWTPlayAuth（字段名为 `playAuth`）：

```java
String playAuth = JwtUtil.getPlayAuthToken(videoId, playKey);
```

- 服务端通过 `GetAppPlayKey` 获取应用的 PlayKey，并在本地根据 `videoId + playKey` 生成 JWTPlayAuth；
- 相比点播颁发播放凭证（PlayAuth）的方式，本地签名减少了一次远程调用，在高并发和网络波动场景下更稳定；
- 详细对比与时序图说明，见 `docs/vidauth-design.md`。

> 注意：使用 `vid + JWTPlayAuth`（字段名 `playAuth`）进行播放时，客户端播放器 SDK 版本需要 **>= 7.10.0**，否则无法完成播放鉴权。

### AccessKey 安全

⚠️ **重要提示**：
- 不要将 AccessKey/SecretKey 提交到代码仓库
- 生产环境建议使用环境变量或密钥管理服务
- 定期轮换密钥

## 📦 依赖说明

主要依赖项：

```xml
<!-- Spring Boot Web -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-web</artifactId>
</dependency>

<!-- 阿里云 VOD SDK -->
<dependency>
    <groupId>com.aliyun.inner</groupId>
    <artifactId>aliyun-java-sdk-vod</artifactId>
    <version>2.16.38</version>
</dependency>

<!-- JWT 认证 -->
<dependency>
    <groupId>com.auth0</groupId>
    <artifactId>java-jwt</artifactId>
    <version>4.4.0</version>
</dependency>
```

## 🐛 常见问题

### 1. 播放凭证无效

- 检查服务端是否正确获取 PlayKey（可通过 `VodSdkService.GetAppPlayKey` 调试）；  
- 确认客户端播放器 SDK 版本是否 ≥ 7.10.0；  
- 确认 `videoId` 有效且对应的视频已在 VOD 上线。

### 2. 实体创建失败

确保实体属性已正确创建，属性ID格式为逗号分隔的字符串。

### 3. 视频信息查询失败

确认 VideoId 是否正确，以及视频是否已上传到 VOD。

---

## 技术支持

如遇到问题或需要技术支持，可通过以下方式获取帮助：

- 📘 **官方文档**：[播放器帮助中心](https://help.aliyun.com/zh/vod/)
- 💬 **GitHub Issues**：[欢迎提交反馈与建议](https://github.com/MediaBox-Demos/VodAppServer/issues)
- 🔍 **控制台**：[视频点播控制台](https://vod.console.aliyun.com/)

---

**最后更新时间**: 2025-12-01

