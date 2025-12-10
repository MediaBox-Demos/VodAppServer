# 高级功能

本文档介绍 VodAppServer 的高级特性和深度使用场景。

## 目录

- [JWT 播放鉴权](#jwt-播放鉴权)
- [安全与性能](#安全与性能)

---

## JWT 播放鉴权

> **整体方案说明**：如果需要从架构视角了解 VidAuth 新旧方案（旧版 `GetVideoPlayAuth` vs 新版本地 JWT 签名）、时序对比以及安全边界，建议先阅读专题文档：[播放鉴权方案](./vidauth-design.md)。

### 工作原理

VodAppServer 使用 JWT（JSON Web Token）实现安全的视频播放鉴权，本质是由应用服务端基于 PlayKey 本地签名，VOD 在播放链路中校验：

```
1. 客户端请求视频
   ↓
2. 服务端生成 JWT Token（playAuth）
   ↓
3. 客户端使用 playAuth 请求播放
   ↓
4. 阿里云 VOD 验证 Token
   ↓
5. 返回视频流
```

### 生成播放凭证（服务端本地签名）

```java
// 核心代码
String playAuth = JwtUtil.getPlayAuthToken(videoId, playKey);
```

**参数说明**：
- `videoId`: 视频ID
- `playKey`: 应用播放密钥（从 VOD 控制台获取）

### 自定义 Token 有效期

```java
// 默认有效期：3600秒（1小时）
String playAuth = JwtUtil.getPlayAuthToken(videoId, playKey);

// 自定义有效期：7200秒（2小时）
String playAuth = JwtUtil.getPlayAuthToken(videoId, playKey, 7200);
```

### 配置播放密钥（PlayKey）

#### 获取 PlayKey

详细接口文档路径：[GetAppPlayKey - 获取应用播放密钥](https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-getappplaykey)

#### 设置 PlayKey

详细接口文档路径：[SetAppPlayKey - 设置应用播放密钥](https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-setappplaykey)

```java
// 调用接口设置
SetAppPlayKeyResponse response = vodSdkService.SetAppPlayKey(
    JwtConstants.DEFAULT_APP_ID,
    "your_new_play_key"
);
```

### 播放器 SDK 版本限制说明

- 使用 JWT 本地签名 方式生成的 vid + playAuth 进行播放时，客户端播放器 SDK 版本必须满足以下要求：
  - Android / iOS 播放器 SDK 版本需 ≥ 7.10.0

- 如果播放器 SDK 版本不符合要求：

  - 即使服务端正确生成了基于 JWT 的 playAuth，播放器也无法正常完成鉴权与播放；

  - 建议先升级播放器 SDK 至 7.10.0 或以上版本后，再接入本地签名方案。

### 安全建议

✅ **推荐做法**：
- 每次播放生成新的 playAuth
- 设置合理的 Token 有效期
- 服务端存储 playKey，不要暴露给客户端
- 启用 HTTPS 传输

❌ **避免**：
- 在客户端生成 playAuth
- 使用永久有效的 Token
- 明文传输 playKey

---

## 安全与性能

### AccessKey 安全

#### ❌ 不安全的做法

```yaml
# 直接写在配置文件
aliyun:
  vod:
    ak: YOUR_ACCESS_KEY
    sk: YOUR_ACCESS_KEY_SECRET
```

#### ✅ 推荐做法

**1. 使用环境变量**

```bash
# 设置环境变量
export ALIYUN_AK=your_access_key
export ALIYUN_SK=your_secret_key
```

```yaml
# 配置文件引用
aliyun:
  vod:
    ak: ${ALIYUN_AK}
    sk: ${ALIYUN_SK}
    region: ${ALIYUN_VOD_REGION:cn-shanghai}  # 地域标识，支持环境变量
```

**2. 使用配置中心**

```java
@Configuration
public class VodConfigLoader {
    
    @Value("${config.center.url}")
    private String configCenterUrl;
    
    @Bean
    public VodConfig vodConfig() {
        // 从配置中心加载
        return configClient.load("vod-config");
    }
}
```

**3. 使用阿里云 RAM 角色**

```java
// 使用 ECS 实例角色
// 注意：region 需要从配置中读取，不能硬编码
String regionId = vodConfig.getRegion(); // 从配置读取
DefaultProfile profile = DefaultProfile.getProfile(
    regionId
    // 不需要 AK/SK，自动使用实例角色
);
```

### 接口限流

```java
@Component
public class RateLimiter {
    
    private final Semaphore semaphore = new Semaphore(100);  // 并发限制
    
    public <T> T execute(Supplier<T> action) throws InterruptedException {
        semaphore.acquire();
        try {
            return action.get();
        } finally {
            semaphore.release();
        }
    }
}
```

### 缓存策略

```java
@Service
public class CachedVodService {
    
    @Cacheable(value = "playlistCache", key = "#playlistId", unless = "#result == null")
    public PlayList getPlaylist(String playlistId) {
        return vodSdkService.getPlaylist(playlistId);
    }
    
    @CacheEvict(value = "playlistCache", key = "#playlistId")
    public void updatePlaylist(String playlistId, PlayList playlist) {
        vodSdkService.updatePlaylist(playlistId, playlist);
    }
}
```

### 异步处理

```java
@Service
public class AsyncVideoProcessor {
    
    @Async("vodExecutor")
    public CompletableFuture<String> generatePlayAuth(String videoId) {
        String playAuth = JwtUtil.getPlayAuthToken(videoId, playKey);
        return CompletableFuture.completedFuture(playAuth);
    }
    
    public List<String> batchGeneratePlayAuth(List<String> videoIds) {
        List<CompletableFuture<String>> futures = videoIds.stream()
            .map(this::generatePlayAuth)
            .collect(Collectors.toList());
        
        return futures.stream()
            .map(CompletableFuture::join)
            .collect(Collectors.toList());
    }
}
```

---

## 扩展开发

### 自定义拦截器

```java
@Component
public class VodAuthInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, 
                           HttpServletResponse response, 
                           Object handler) throws Exception {
        // 验证请求签名
        String signature = request.getHeader("X-Signature");
        if (!validateSignature(signature)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return false;
        }
        return true;
    }
    
    private boolean validateSignature(String signature) {
        // 实现签名验证逻辑
        return true;
    }
}
```

### 自定义异常处理

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ClientException.class)
    public ResponseEntity<CallResult> handleClientException(ClientException e) {
        CallResult result = new CallResult();
        // 这里示例直接复用 SYSTEM_INNER_ERROR，可根据需要在 ResultCode 中扩展专门的 API 错误码
        result.setCode(ResultCode.SYSTEM_INNER_ERROR.code);
        result.setHttpCode("500");
        result.setSuccess(false);
        result.setMessage("阿里云 API 调用失败: " + e.getMessage());
        
        return ResponseEntity.status(500).body(result);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<CallResult> handleException(Exception e) {
        CallResult result = new CallResult();
        result.setCode(ResultCode.SYSTEM_INNER_ERROR.code);
        result.setHttpCode("500");
        result.setSuccess(false);
        result.setMessage("系统错误: " + e.getMessage());
        
        return ResponseEntity.status(500).body(result);
    }
}
```

---

## 性能监控

### 接口耗时统计

```java
@Aspect
@Component
public class PerformanceMonitor {
    
    @Around("execution(* com.aliyun.appserver.controller..*(..))")
    public Object monitorPerformance(ProceedingJoinPoint pjp) throws Throwable {
        long startTime = System.currentTimeMillis();
        
        Object result = pjp.proceed();
        
        long duration = System.currentTimeMillis() - startTime;
        String methodName = pjp.getSignature().getName();
        
        if (duration > 1000) {
            log.warn("接口 {} 耗时过长: {}ms", methodName, duration);
        }
        
        return result;
    }
}
```

### 日志记录

```java
@Slf4j
@Component
public class ApiLogger {
    
    public void logRequest(HttpServletRequest request) {
        log.info("API 请求 - 方法: {}, URI: {}, 参数: {}", 
            request.getMethod(),
            request.getRequestURI(),
            request.getParameterMap()
        );
    }
    
    public void logResponse(CallResult result, long duration) {
        log.info("API 响应 - 状态: {}, 耗时: {}ms, 消息: {}", 
            result.getSuccess(),
            duration,
            result.getMessage()
        );
    }
}
```

---

## 最佳实践

### 1. 实体设计

- 合理规划实体层级关系（最多3层）
- 使用有意义的属性名称
- 预留扩展字段

### 2. 性能优化

- 批量查询替代单个查询
- 使用缓存减少 API 调用
- 异步处理耗时操作

### 3. 安全防护

- AccessKey 使用环境变量
- 接口添加签名验证
- 启用 HTTPS 传输

### 4. 错误处理

- 统一异常处理
- 详细的错误日志
- 友好的错误提示

---

## 下一步

- 📖 回顾 [Quick-Start.md](./Quick-Start.md) 巩固基础
- 📚 查阅 [API-Guide.md](./API-Guide.md) 了解完整接口
- 💡 参考 [Integration-Guide.md](./Integration-Guide.md) 进行生产部署

---

**最后更新**: 2025-11-24
