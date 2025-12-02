## **VidAuth 播放鉴权方案**

本章节面向**架构师 / 后端开发 / 客户技术同学**，用一页内容讲清楚三件事：

1. 旧版 VidAuth 的工作原理（基于 `GetVideoPlayAuth` 的远程签名）；  
2. 新版 VidAuth 的工作原理（基于 PlayKey 的本地 JWT 签名）；  
3. 新旧方案在性能、安全性、扩展性上的核心差异，以及播放器 SDK 的版本要求。

---

### **1. 场景概览：VidAuth 在播放链路中的位置**

- VidAuth 的核心职责：**给某个 `videoId` 颁发一张“临时通行证”（PlayAuth），由播放器携带 `vid + PlayAuth` 去 VOD 获取真实播放地址。**
- 区别只在于：
  - **旧版**：这张“通行证”由 VOD Server 生成，App Server 每次向 VOD 要一张；  
  - **新版**：这张“通行证”由 App Server 本地生成，VOD Server 只负责验票。

---

### **2. 播放时序对比：旧版 vs 新版**

![Vid 模式总体交互图](./assets/vidauth-overview.png)

> 上图统一展示了 Vid 播放的通用交互链路：App 请求播放 → App Server 返回 `vid + PlayAuth` → Player SDK 携带 `vid + PlayAuth` 向 VOD 换取真实播放地址。  
> 新旧方案的唯一差异在于：「App Server 如何获得 / 生成 PlayAuth」，后文将在新版 / 旧版章节分别展开。

---

### **3. 新版 VidAuth（本地签名 + JWT）**

#### **3.1 播放流程（新版：本地签名）**

![VidAuth 播放流程（新版）](./assets/vidauth-local-sign.png)

> 特点：**App Server 持有 PlayKey，在本地直接生成 JWT 形式的 PlayAuth，VOD Server 只做验签。**

#### **3.2 JWT 原理示意图**

![JWT 原理示意图](./assets/jwt-overview.png)

#### **3.3 一句话说明新方案**

- App Server 使用 **PlayKey + videoId** 在本地生成 JWT 形式的 PlayAuth；  
- VOD Server 使用同一个 **PlayKey** 验证 JWT 的签名与过期时间；  
- AK/SK 只用于后台获取 PlayKey，本身不出现在播放链路中；  
- 过期时间可配置（默认 1 天），业务可按需收紧。

----

### **4. 旧版 VidAuth（GetVideoPlayAuth）**

#### **4.1 播放流程（旧版：远程签名）**

![VidAuth 播放流程（旧版）](./assets/vidauth-legacy.png)

> 特点：**每次播放前，App Server 都要向 VOD Server 调用 `GetVideoPlayAuth` 获取 PlayAuth。**

#### **4.2 PlayAuth 的生成方式**

- App Server 调用 `GetVideoPlayAuth` 接口，将 `videoId` 和相关播放参数传给 VOD；  
- VOD Server 在服务端根据配置与权限规则，生成一份临时的 PlayAuth；  
- 这份 PlayAuth 回传给 App Server，再由 App 下发给客户端使用。

#### **4.3 一句话说明旧方案**

- App Server 每次播放前通过 `GetVideoPlayAuth` 让 VOD Server 生成一次性 PlayAuth（本质是 Base64 编码的 JSON，默认有效期约 3000 秒），业务方可控空间相对有限。

---

### **5. 新版本地签名相比旧版的核心优势**

#### **5.1 更快、更稳定**

- 旧版：每次播放都要远程调用 `GetVideoPlayAuth`，多一次网络 RTT，接口耗时和失败率都受网络/服务端抖动影响。  
- 新版：签名在 App Server 本地完成，播放链路只保留一次 `GetVideoPlayInfoV2` 调用，整体平均耗时更低、抖动更小。

#### **5.2 更易扩展**

- 旧版：高并发场景下，`GetVideoPlayAuth` QPS 直接压在 VOD 接口上。  
- 新版：签名计算变成本地 CPU 开销，通过横向扩容 App Server 即可线性扩展播放能力。

#### **5.3 更灵活的业务控制**

- 旧版：PlayAuth 结构与生成逻辑封装在 VOD 内部，可调参数有限。  
- 新版：PlayAuth 是标准 JWT，可按需增加用户 ID、设备 ID、IP/地域限制、风控标签等 Claim，并自定义过期时间与策略。

#### **5.4 更清晰的安全边界**

- 新版仅在后台用 AK/SK 获取 PlayKey，播放链路只暴露 Vid + JWT，不直接暴露 AK/SK，也不依赖 STS，安全面更收敛。  

#### **5.5 与新版 Player SDK 的能力对齐**

- 本地签名方案依赖播放器 SDK 对 VidAuth + JWT 的内建支持能力；  
- **客户端播放器 SDK 版本要求**：

> 使用 JWT 本地签名生成的 `vid + playAuth` 进行播放时，  
> 客户端播放器 SDK 版本需要 **≥ 7.10.0**，否则无法完成播放鉴权。

---

### **6. 与源码的对应关系（方便二次开发快速定位）**

- `jwt/JwtConstants.java`  
  - 定义了 `DEFAULT_APP_ID`、`DEFAULT_REGION_ID`、`EXPIRED_TIME_MILLS` 等 JWT 播放鉴权相关常量；  
  - 可以在这里调整默认过期时间等基础配置。

- `jwt/JwtUtil.java`  
  - 提供 `getPlayAuthToken(String videoId, String playKey)` 方法，用于基于 PlayKey 生成 JWT 播放凭证；  
  - 顶部注释中已注明“仅服务端调用 + Player SDK 版本要求”等注意事项。

- `service/impl/PlayListServiceImpl.java`  
  - 在 `getPlaylistInfo`、`getPlaylistVideos` 等方法中调用 `JwtUtil.getPlayAuthToken(...)`，为短剧视频生成 PlayAuth；  
  - 是本 Demo 中“如何将本地签名集成到实际业务返回体”的直接示例。

-----

