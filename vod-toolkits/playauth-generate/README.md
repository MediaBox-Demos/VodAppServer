# 阿里云视频点播播放授权 Token 生成器

## 1. 项目简介

VidAuthGenerator 是一套跨语言的阿里云视频点播（VOD）播放授权 Token 生成与验证工具集，支持 Java、Python、Go、PHP 四种主流开发语言。基于 JWT（JSON Web Token）技术实现视频播放前的身份验证和权限控制。

> **重要提示：** 使用 `vid + JWTPlayAuth`（字段名：`playAuth`）进行播放时，客户端播放器 SDK 版本需要满足 `>= 7.10.0`，否则无法正常完成播放鉴权。

## 2. 功能特性

- 生成播放授权 Token
- 验证 Token 有效性
- 解析 Token 的 Header 和 Payload
- 支持时间戳校验，防止重放攻击
- 多语言统一接口

## 3. 项目结构

```
VidAuthGenerator/
├── Java/                          # Java 实现
│   ├── src/main/java/com/aliyun/vod/
│   │   ├── Main.java              # 示例程序入口
│   │   └── playauth/              # 播放授权工具包
│   ├── pom.xml
│   └── README.md
├── Python/                        # Python 实现
│   ├── main.py                    # 示例程序入口
│   ├── vod/playauth/              # 播放授权工具包
│   ├── requirements.txt
│   └── README.md
├── Go/                            # Go 实现
│   ├── main.go                    # 示例程序入口
│   ├── playauth/                  # 播放授权工具包
│   ├── go.mod
│   └── README.md
├── PHP/                           # PHP 实现
│   ├── main.php                   # 示例程序入口
│   ├── src/Aliyun/Vod/playauth/   # 播放授权工具包
│   ├── composer.json
│   └── README.md
└── README.md                      # 项目总览文档
```

## 4. 快速开始

请根据您的开发语言，选择对应的子项目：

| 语言 | 目录 | 运行命令 | 详细文档 |
|------|------|----------|----------|
| Java | `Java/` | `mvn exec:java -Dexec.mainClass="com.aliyun.vod.Main"` | [README.md](./Java/README.md) |
| Python | `Python/` | `python3 main.py` | [README.md](./Python/README.md) |
| Go | `Go/` | `go run main.go` | [README.md](./Go/README.md) |
| PHP | `PHP/` | `php main.php` | [README.md](./PHP/README.md) |

## 5. 集成使用

### 5.1. 配置必要参数

在各语言的常量配置文件中设置以下关键信息：

| 参数 | 说明 | 获取方式 |
|------|------|----------|
| `appId` | 应用 ID | 阿里云视频点播控制台 - 应用管理 |
| `playKey` | 播放密钥 | GetAppPlayKey 接口获取 |
| `regionId` | 地域标识 | 如 `cn-shanghai` |
| `expiredTime` | 过期时间 | 默认 24 小时 |

### 5.2. API 接口

各语言均提供统一的 API 接口：

```java
// Java
String token = VodPlayAuthToken.generateToken("videoId");
boolean isValid = VodPlayAuthToken.verifyToken(token);
```

```python
# Python
token = generate_token("video_id")
is_valid = verify_token(token)
```

```go
// Go
token, _ := playauth.GenerateToken("videoId")
isValid := playauth.VerifyToken(token)
```

```php
// PHP
$token = VodPlayAuthToken::generateToken("videoId");
$isValid = VodPlayAuthToken::verifyToken($token);
```

## 6. Token 结构

Token Payload 包含以下字段：

| 字段 | 说明 |
|------|------|
| `appId` | 应用标识 |
| `videoId` | 视频 ID |
| `currentTimeStamp` | 签发时间戳（毫秒） |
| `expireTimeStamp` | 过期时间戳（毫秒） |
| `regionId` | 地域标识 |
| `playContentInfo` | 播放配置信息 |

## 7. 注意事项

1. **密钥安全**：请妥善保管播放密钥，避免泄露
2. **时间同步**：确保服务器时间准确，避免时间戳校验失败
3. **Token 过期**：合理设置 Token 过期时间，平衡安全性和用户体验
4. **密钥长度**：
   - PHP：`firebase/php-jwt` 要求 PlayKey 长度 >= 32 位
   - Python：PyJWT 遵循 RFC 7518 规范，建议 HMAC 密钥长度 >= 32 字节

## 8. 相关文档

- [阿里云视频点播控制台](https://vod.console.aliyun.com/)
- [GetAppPlayKey - 获取播放密钥](https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-getappplaykey)
- [SetAppPlayKey - 设置播放密钥](https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-setappplaykey)
