# VOD Toolkits

阿里云点播服务端工具包集合，供客户服务端直接集成使用。

---

## 1. 概述

本目录包含一系列独立的工具包，每个工具包解决特定的服务端集成场景。工具包以子目录形式组织，客户可根据需要选择性地引入。

----

## 2. 工具列表

| 工具 | 说明 | 支持语言 |
|------|------|----------|
| [playauth-generate](./playauth-generate) | 播放鉴权生成工具，用于在服务端本地生成 JWTPlayAuth | Java, Python, Go, PHP |

> 💡 更多工具持续添加中...

---

## 3. 工具说明

### 3.1. playauth-generate

播放鉴权生成工具，用于在服务端本地生成 JWTPlayAuth（基于 PlayKey 的 JWT 签名播放凭证）。

**适用场景**：
- 高并发播放场景，减少对点播服务的远程调用
- 网络不稳定环境，提升播放凭证获取成功率
- 需要自主控制播放凭证生成逻辑

**支持的编程语言**：

- **Java** - 适用于 Spring Boot 等 Java 服务端
- **Python** - 适用于 Django/Flask 等 Python 服务端
- **Go** - 适用于 Go 服务端
- **PHP** - 适用于 PHP 服务端

详细使用方式请参考各语言版本的 README。

---

## 4. 目录结构

```
vod-toolkits/
├── README.md                    # 说明文档
├── playauth-generate/           # 播放鉴权生成工具（submodule）
│   ├── java/                    # Java 版本
│   ├── python/                  # Python 版本
│   ├── go/                      # Go 版本
│   └── php/                     # PHP 版本
└── ...                          # 更多工具
```

---

