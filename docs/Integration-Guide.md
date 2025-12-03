# 集成指引

本文档将指导您如何将 VodAppServer 集成到您的项目中，包含详细的配置说明、依赖配置、代码集成和问题排查。

> 💡 **快速上手**：如果您只是想快速体验，可以先查看 [快速开始](./Quick-Start.md) 了解基本使用流程。

⚠️ **重要提醒**：调用 VOD 的实体类和播单类接口需要提交工单进行加白后才可以使用。

## 目录

- [环境准备](#环境准备)
- [依赖配置](#依赖配置)
- [阿里云配置](#阿里云配置)
- [项目集成](#项目集成)
- [配置验证](#配置验证)

---

## 环境准备

### 基础环境要求

| 组件 | 版本要求 | 说明 |
|------|---------|------|
| **JDK** | 1.8+ | 必须 |
| **Maven** | 3.x+ | 构建工具 |
| **Spring Boot** | 2.6.6 | 框架版本 |

### 开发工具推荐

- **IDE**: IntelliJ IDEA / Eclipse
- **API 测试**: Postman / Apifox
- **版本控制**: Git

---

## 依赖配置

### Maven 依赖

在您的项目 `pom.xml` 中添加以下依赖：
⚠️ **重要提醒**：<!-- 阿里云 VOD SDK -->依赖项需要提升到2.16.39及以上
<!-- 使用fc部署是需要添加依赖项并注释调本地jar包引用 -->

```xml
<dependencies>
    <!-- Spring Boot Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- 阿里云核心 SDK -->
    <dependency>
        <groupId>com.aliyun</groupId>
        <artifactId>aliyun-java-sdk-core</artifactId>
        <version>4.6.3</version>
    </dependency>

    <!-- 阿里云 VOD SDK本地jar包引用 -->
   <dependency>
      <groupId>com.aliyun-inner</groupId>
      <artifactId>aliyun-java-sdk-vod</artifactId>
      <scope>system</scope>
      <version>2.16.34</version>
      <systemPath>${project.basedir}/src/main/resources/lib/aliyun-java-sdk-vod-2.16.34.jar</systemPath>
   </dependency>

   <!-- 阿里云 VOD SDK -->
   <!-- 使用fc部署是需要添加依赖项并注释调本地jar包引用 -->
    <dependency>
        <groupId>com.aliyun.inner</groupId>
        <artifactId>aliyun-java-sdk-vod</artifactId>
        <version>2.16.39</version>
    </dependency>

    <!-- 阿里云 VOD 上传 SDK -->
    <dependency>
        <groupId>com.aliyun.vod</groupId>
        <artifactId>aliyun-java-vod-upload</artifactId>
        <version>1.4.15</version>
    </dependency>

    <!-- 阿里云 OSS SDK -->
    <dependency>
        <groupId>com.aliyun.oss</groupId>
        <artifactId>aliyun-sdk-oss</artifactId>
        <version>3.17.3</version>
    </dependency>

    <!-- 阿里云 STS SDK -->
    <dependency>
        <groupId>com.aliyun</groupId>
        <artifactId>aliyun-java-sdk-sts</artifactId>
        <version>3.0.0</version>
    </dependency>

    <!-- JWT 认证 -->
    <dependency>
        <groupId>com.auth0</groupId>
        <artifactId>java-jwt</artifactId>
        <version>4.4.0</version>
    </dependency>

    <!-- Fastjson -->
    <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>fastjson</artifactId>
        <version>1.2.83</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## 阿里云配置

### 1. 获取 AccessKey

1. 登录 [阿里云控制台](https://ram.console.aliyun.com/manage/ak)
2. 创建或获取 AccessKey ID 和 AccessKey Secret
3. **注意**：请妥善保管密钥，不要泄露

### 2. 开通 VOD 服务

1. 访问 [视频点播控制台](https://vod.console.aliyun.com/)
2. 开通视频点播服务
3. **选择存储区域**（重要）：
   - 根据业务需求选择合适的地域
   - 推荐：华东2（上海）`cn-shanghai`
   - 注意：选择的区域必须与 `application.yml` 中的 `region` 配置一致

### 3. 配置应用密钥

在 VOD 控制台配置播放密钥：
```
控制台 → 配置管理 → 分发加速配置 → 域名管理 → 访问控制
```

---

## 项目集成

### 1. 配置文件设置

在 `application.yml` 中添加配置：

```yaml
  server:
    port: 9000

aliyun:
  vod:
    # 阿里云 AccessKeyId（必填）
    # 获取方式：https://ram.console.aliyun.com/manage/ak
    ak: YOUR_ACCESS_KEY_ID
    
    # 阿里云 AccessKeySecret（必填）
    sk: YOUR_ACCESS_KEY_SECRET
    
    # 地域标识（Region ID，必填）
    # 必须与 VOD 控制台中开通服务的区域一致
    # 当前支持的地域：
    #   - 上海：cn-shanghai（默认，推荐）
    #   - 北京：cn-beijing
    #   - 深圳：cn-shenzhen
    #   - 新加坡：ap-southeast-1
    # 后续考虑支持：美西（us-west-1）
    region: cn-shanghai
```

**配置项说明**：

| 配置项 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `ak` | String | ✅ | 阿里云 AccessKey ID，用于身份认证 |
| `sk` | String | ✅ | 阿里云 AccessKey Secret，用于签名 |
| `region` | String | ✅ | VOD 服务地域标识，必须与开通区域一致 |

**地域选择指南**：
- 查看 VOD 控制台左上角显示的地域
- 华东2（上海）→ `cn-shanghai`
- 华北2（北京）→ `cn-beijing`
- 华南1（深圳）→ `cn-shenzhen`
- 亚太东南1（新加坡）→ `ap-southeast-1`

**⚠️ 安全提示**：
- **开发环境**：可直接在配置文件中填写
- **生产环境**：强烈建议使用环境变量或密钥管理服务
- **代码仓库**：不要将包含真实密钥的配置文件提交到 Git
- **密钥轮换**：定期更换 AccessKey，提高安全性

**使用环境变量（推荐）**：

```yaml
aliyun:
  vod:
    ak: ${ALIYUN_VOD_AK:}           # 从环境变量读取，为空则使用默认值
    sk: ${ALIYUN_VOD_SK:}
    region: ${ALIYUN_VOD_REGION:cn-shanghai}  # 支持环境变量，默认 cn-shanghai
```

启动时设置环境变量：
```bash
export ALIYUN_VOD_AK=your_access_key
export ALIYUN_VOD_SK=your_secret_key
export ALIYUN_VOD_REGION=cn-shanghai
mvn spring-boot:run
```

### 2. 配置类引入

复制以下配置类到您的项目：

#### VodConfig.java

```java
@Component
@ConfigurationProperties(prefix = "aliyun.vod")
public class VodConfig {
    /**
     * 阿里云 AccessKeyId
     */
    private String ak;
    
    /**
     * 阿里云 AccessKeySecret
     */
    private String sk;
    
    /**
     * 地域标识（Region ID）
     * 当前支持：cn-shanghai, cn-beijing, cn-shenzhen, ap-southeast-1
     * 默认值：cn-shanghai
     */
    private String region = "cn-shanghai";
    
    // getter 和 setter（使用 Lombok @Getter/@Setter 或手动实现）
}
```

### 3. 引入核心服务

将以下包复制到您的项目：

```
com.aliyun.appserver/
├── config/          # 配置类
├── controller/      # 控制器
├── service/         # 服务接口
├── service/impl/    # 服务实现
├── dto/             # 数据传输对象
├── entity/          # 实体类
├── result/          # 统一返回结果
└── jwt/             # JWT 工具
```

### 4. 启动类配置

```java
@SpringBootApplication
@ComponentScan(basePackages = "com.aliyun.appserver")
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

---

## 配置验证

### 1. 启动项目

```bash
# Maven 构建
mvn clean package

# 运行项目
java -jar target/VodAppServer-1.0-SNAPSHOT.jar

# 或使用 Maven 直接运行
mvn spring-boot:run
```

**验证配置是否正确**：
- 查看启动日志，确认显示：`VOD 客户端初始化成功，Region: xxx`
- Region 应该与您在 `application.yml` 中配置的 `region` 一致

### 2. 健康检查

项目启动后，测试接口验证配置：

```bash
# 测试播单列表接口
curl -X POST http://localhost:9000/appServer/getPlaylists \
  -H "Content-Type: application/json" \
  -d '{"pageNo": "1", "pageSize": "10"}'
```

**成功响应说明**：
- `getPlaylists` 接口直接返回 VOD SDK 的 `GetPlaylistsResponse`，结构以阿里云 VOD OpenAPI 文档为准
- 如需查看统一响应格式（`CallResult`），请使用业务增强接口 `getPlaylistVideos`，参考 [API 指引 - 获取播单列表（业务增强）](./API-Guide.md#310-获取播单列表业务增强)

> 💡 **快速体验**：更多接口使用示例和典型场景，请参考 [快速开始 - 典型场景](./Quick-Start.md#典型场景)

### 3. 常见问题排查

#### ❌ 启动失败

**问题**：`Failed to initialize VOD client`

**解决**：
1. **检查 AccessKey**：确认 `ak` 和 `sk` 配置正确
2. **验证 Region**：确认 `region` 与 VOD 控制台开通区域一致
   - 查看启动日志中的 `Region: xxx` 是否与配置一致
   - 如果配置错误，会提示 `InvalidRegionId.NotFound`
3. **网络连接**：确认服务器能访问阿里云服务
4. **权限检查**：确认 AccessKey 有 VOD 服务访问权限

#### ❌ 接口调用失败

**问题**：`InvalidAccessKeyId.NotFound`

**解决**：
1. 确认 AccessKey 已激活
2. 检查 RAM 权限配置
3. 确认 VOD 服务已开通

#### ❌ 跨域问题

**解决**：控制器已配置 `@CrossOrigin`，如需自定义：

```java
@CrossOrigin(
    origins = "http://your-domain.com",
    methods = {RequestMethod.GET, RequestMethod.POST}
)
```

---

## 下一步

配置完成后，您可以：

1. 📖 阅读 [Quick-Start.md](./Quick-Start.md) 了解基本使用
2. 📚 查看 [API-Guide.md](./API-Guide.md) 了解接口详情
3. 🚀 探索 [Advanced-Features.md](./Advanced-Features.md) 进行深度定制

---

**最后更新**: 2025-12-02
