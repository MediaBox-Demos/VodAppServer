# 阿里云视频点播播放授权 Token 生成器

## 1. 项目简介

基于 Python 的阿里云视频点播（VOD）播放授权 Token 生成器，使用 JWT（JSON Web Token）技术实现视频播放前的身份验证和权限控制。

> **重要提示：** 使用 `vid + JWTPlayAuth`（字段名：`playAuth`）进行播放时，客户端播放器 SDK 版本需要满足 `>= 7.10.0`，否则无法正常完成播放鉴权。

## 2. 功能特性

- 生成播放授权 Token
- 验证 Token 有效性
- 解析 Token 的 Header 和 Payload
- 支持时间戳校验，防止重放攻击

## 3. 技术栈

- Python 3.6+
- PyJWT >= 2.0.0

## 4. 项目结构

```
Python/
├── main.py                           # 示例程序入口
├── requirements.txt                  # 依赖声明
└── vod/
    ├── __init__.py
    └── playauth/                     # 播放授权工具包（可直接提取集成）
        ├── __init__.py
        ├── vod_jwt_constants.py      # JWT 常量配置
        └── vod_play_auth_token.py    # Token 生成与验证工具
```

## 5. 快速开始

### 5.1. 运行示例

**方式一：命令行运行**

```bash
# 安装依赖（首次运行需要）
pip3 install PyJWT

# 进入项目目录
cd Python

# 运行示例程序
python3 main.py
```

**方式二：IDEA 运行**

1. 使用 IDEA 打开 `Python` 目录
2. 打开 `main.py` 文件
3. 右键点击 `main` 函数，选择 `Run`

### 5.2. 示例结果

```
========== 阿里云视频点播播放授权 Token 示例 ==========

【步骤1】生成 Token
Token: eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJhcHBJZCI6ImFwcC0xMDAwMDAwIiwidmlkZW9JZCI6IjEwYjcxMTA3YTk5ODcxZjA5N2I5NjczMmI3OGUwMTAyIiwiY3VycmVudFRpbWVTdGFtcCI6MTc0MjI4MDAwMDAwMCwiZXhwaXJlVGltZVN0YW1wIjoxNzQyMzY2NDAwMDAwLCJyZWdpb25JZCI6ImNuLXNoYW5naGFpIiwicGxheUNvbnRlbnRJbmZvIjp7ImZvcm1hdHMiOiJtcDQiLCJhdXRoVGltZW91dCI6MTgwMCwic3RyZWFtVHlwZSI6InZpZGVvIn19.abc123def456

【步骤2】解析 Token
Header: {"alg":"HS256","typ":"JWT"}
Payload: {"appId":"app-1000000","videoId":"10b71107a99871f097b96732b78e0102","currentTimeStamp":1742280000000,"expireTimeStamp":1742366400000,"regionId":"cn-shanghai","playContentInfo":{"formats":"mp4","authTimeout":1800,"streamType":"video"}}

【步骤3】验证 Token
验证结果: 通过
```

## 6. 集成使用

### 步骤1：拷贝工具包

将 `vod` 包直接拷贝到您的项目中：

```
vod/
├── __init__.py
└── playauth/
    ├── __init__.py
    ├── vod_jwt_constants.py
    └── vod_play_auth_token.py
```

### 步骤2：修改配置

> **重要提示：** 请打开 `vod_jwt_constants.py` 文件，将 `DEFAULT_PLAY_KEY` 变量上方注释掉的空值赋值行放开，并将空字符串替换为您从阿里云获取的播放密钥。

修改 `vod_jwt_constants.py` 中的配置：

```python
# 应用 ID（从阿里云视频点播控制台获取）
DEFAULT_APP_ID = "your-app-id"

# 播放密钥（通过 GetAppPlayKey 接口获取）
DEFAULT_PLAY_KEY = "your-play-key"

# 地域标识
DEFAULT_REGION_ID = "cn-shanghai"

# Token 过期时间（毫秒）
EXPIRED_TIME_MILLS = 24 * 60 * 60 * 1000
```

### 步骤3：安装依赖

```bash
pip3 install PyJWT
```

## 7. API 使用

### 7.1. 生成 Token

```python
from vod.playauth import generate_token, generate_token_with_config

# 使用默认配置生成 Token
token = generate_token("your-video-id")

# 自定义配置生成 Token
token = generate_token_with_config(
    "your-video-id",      # 视频 ID
    "your-app-id",        # 应用 ID
    "your-play-key",      # 播放密钥
    "cn-shanghai",        # 地域标识
    3600 * 1000           # 过期时间（毫秒）
)
```

### 7.2. 验证 Token

```python
from vod.playauth import verify_token, verify_token_with_key

# 使用默认密钥验证
is_valid = verify_token(token)

# 自定义密钥验证
is_valid = verify_token_with_key(token, "your-play-key")
```

### 7.3. 解析 Token

```python
from vod.playauth import parse_header, parse_payload

# 解析 Header
header = parse_header(token)

# 解析 Payload
payload = parse_payload(token)
```

## 8. Token 结构

Token Payload 包含以下字段：

| 字段 | 说明 |
|------|------|
| `appId` | 应用标识 |
| `videoId` | 视频 ID |
| `currentTimeStamp` | 签发时间戳（毫秒） |
| `expireTimeStamp` | 过期时间戳（毫秒） |
| `regionId` | 地域标识 |
| `playContentInfo` | 播放配置信息 |

## 9. 注意事项

1. **密钥安全**：请妥善保管播放密钥，避免泄露
2. **时间同步**：确保服务器时间准确，避免时间戳校验失败
3. **Token 过期**：合理设置 Token 过期时间，平衡安全性和用户体验
4. **密钥长度**：由于 PyJWT 遵循 RFC 7518 安全规范，要求 HMAC 密钥长度至少为 32 字节（SHA256 哈希输出长度），而当前默认 PlayKey 仅为 13 字节，虽能运行但存在安全风险；因此建议您调用 `SetAppPlayKey` 接口，设置一个长度不少于 32 字节的强随机密钥

## 10. 相关文档

- [阿里云视频点播控制台](https://vod.console.aliyun.com/)
- [GetAppPlayKey - 获取播放密钥](https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-getappplaykey)
- [SetAppPlayKey - 设置播放密钥](https://help.aliyun.com/zh/vod/developer-reference/api-vod-2017-03-21-setappplaykey)
