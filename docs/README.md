## **VodAppServer 文档目录**

本文档介绍了 VodAppServer 项目中各个文档的内容和用途，并按照推荐的学习和使用流程进行组织。

## **文档列表**

### **1. 基础集成文档**

| 文档名称 | 文档链接 | 详细描述 | 用途说明 |
|---------|---------|---------|---------|
| 集成指引 | [Integration-Guide.md](./Integration-Guide.md) | 详细介绍 VodAppServer 的集成步骤、环境准备、依赖配置和阿里云配置 | 帮助开发者完成项目的集成和环境配置 |
| 快速开始 | [Quick-Start.md](./Quick-Start.md) | 介绍如何快速上手使用 VodAppServer，包括5分钟快速体验、部署到函数计算、核心概念和典型场景 | 包括基本的集成步骤、部署方法和示例代码 |

### **2. 接口使用文档**

| 文档名称 | 文档链接 | 详细描述 | 用途说明 |
|---------|---------|---------|---------|
| API 指引 | [API-Guide.md](./API-Guide.md) | 提供 VodAppServer 当前可用 API 接口的详细说明，包括播单管理和媒体处理等接口 | 帮助开发者了解接口使用、参数说明和响应格式 |
| 上传与媒体处理 | [Upload-and-MPS.md](./Upload-and-MPS.md) | 介绍 VOD 上传 SDK、转码（MPS）等相关能力和典型用法 | 帮助开发者完成媒资上传、转码等媒体处理流程 |

### **3. 高级功能与专题文档**

| 文档名称 | 文档链接 | 详细描述 | 用途说明 |
|---------|---------|---------|---------|
| 高级功能 | [Advanced-Features.md](./Advanced-Features.md) | 详细介绍 JWT 播放鉴权、安全与性能等高级特性 | 帮助开发者实现复杂业务场景、性能优化和系统扩展 |
| 播放鉴权方案 | [vidauth-design.md](./vidauth-design.md) | 对比旧版 GetVideoPlayAuth 与新版本地 JWT 签名方案，说明时序流程、优势及 SDK 版本要求 | 帮助客户和开发者理解 VidAuth 播放鉴权整体方案 |

## **推荐阅读顺序**

1. [集成指引](./Integration-Guide.md) - 环境准备与项目集成
2. [快速开始](./Quick-Start.md) - 快速上手和基础操作
3. [API 指引](./API-Guide.md) - 接口规范和使用说明
4. [上传与媒体处理](./Upload-and-MPS.md) - 介绍上传和媒体处理功能
5. [高级功能](./Advanced-Features.md) - 深度定制和性能优化
6. [播放鉴权方案](./vidauth-design.md) - 了解 VidAuth 新旧方案对比与本地签名优势

---

**最后更新**: 2025-12-02
