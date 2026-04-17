# 架构设计文档

## 系统概述

个人照片图库是一个面向单用户的轻量级 Web 应用，采用经典的三层架构：前端展示层、后端 API 层、数据存储层。系统设计遵循"简单可用"原则，避免过早引入微服务或复杂基础设施。

项目目前包含两套后端实现，共用同一套前端：
- **Node.js 后端**：Express + SQLite，适合快速体验和单机部署
- **Java 后端**：Spring Boot + PostgreSQL，适合生产环境和后续扩展

## 架构图

### Node.js 后端架构

```
┌─────────────────┐
│    浏览器        │
│  (HTML/CSS/JS)  │
└────────┬────────┘
         │ HTTP
┌────────▼────────┐
│   Express 服务   │
│  - 静态文件服务   │
│  - RESTful API  │
│  - Session 认证  │
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌────────┐ ┌──────────┐
│ SQLite │ │ 文件系统  │
│(元数据) │ │ (上传文件)│
└────────┘ └──────────┘
```

### Java 后端架构

```
┌─────────────────┐
│    浏览器        │
│  (HTML/CSS/JS)  │
└────────┬────────┘
         │ HTTP
┌────────▼────────┐
│  Spring Boot    │
│  - 静态文件服务  │
│  - RESTful API  │
│  - Session 认证  │
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌──────────┐ ┌──────────┐
│PostgreSQL│ │ 文件系统  │
│ (元数据)  │ │ (上传文件)│
└──────────┘ └──────────┘
```

## 核心模块

### 1. 认证模块

#### Node.js（`backend/src/routes/auth.js`）
基于 `express-session` 实现简单的 Session 认证。登录成功后，用户信息存入 Session Cookie；后续受保护接口通过 `requireLogin` 中间件校验 Session 有效性。密码使用 `bcrypt` 进行哈希存储和比对。

#### Java（`backend-java/.../controller/AuthController.java`）
基于 Servlet `HttpSession` 实现认证逻辑，通过 `UserRepository` 查询用户并比对密码。当前版本密码为明文存储（建议后续升级为 bcrypt）。

### 2. 照片管理模块

#### Node.js（`backend/src/routes/photos.js`）
- **上传**：使用 `multer` 接收 `multipart/form-data`，校验 MIME 类型后，通过 `crypto.randomUUID()` 生成唯一文件名保存到本地
- **查询**：从 SQLite 读取照片元数据，拼接静态文件访问 URL 返回
- **更新**：支持修改标题、描述、标签和所属相册

#### Java（`backend-java/.../controller/PhotoController.java`）
- **上传**：使用 Spring `MultipartFile` 接收文件，校验 MIME 类型后调用 `FileStorageService` 保存
- **查询**：通过 `PhotoRepository` 从 PostgreSQL 读取数据，使用 JPA `@ManyToOne` 关联相册
- **更新**：支持修改照片元数据，标签以逗号分隔字符串存储，API 层自动进行数组转换

### 3. 相册管理模块

#### Node.js（`backend/src/routes/albums.js`）
提供相册的 CRUD 操作。删除相册时，通过 SQL `UPDATE` 将关联照片的 `album_id` 置为 `NULL`，保持数据一致性。

#### Java（`backend-java/.../controller/AlbumController.java`）
提供相册的 CRUD 操作。删除相册时，通过 JPA 查询关联照片并设置 `album = null`，再删除相册实体，实现与 Node.js 后端一致的业务行为。

### 4. 前端模块（`frontend/`）

纯原生前端实现，无框架依赖：
- 使用原生 DOM 操作切换页面视图（登录页、图库页、上传页、相册管理页）
- 使用 `fetch` 与后端 API 通信
- 使用 CSS Grid 实现响应式照片卡片布局
- 已实现 XSS 防护，所有动态内容均经过 HTML 实体编码

## 技术决策

### 为什么选择 SQLite（Node 后端）？

项目面向个人单用户场景，数据量可控，SQLite 无需额外部署数据库服务，零配置即可运行，符合"简单可用"的目标。

### 为什么选择 PostgreSQL（Java 后端）？

Spring Boot + JPA 的企业级生态与 PostgreSQL 配合更为成熟，支持更复杂的事务、并发和扩展需求，为后续可能的多用户、分布式部署打下基础。

### 为什么提供两套后端？

- **Node.js 后端**：降低入门门槛，用户无需安装数据库即可体验完整功能
- **Java 后端**：满足生产环境对稳定性、可维护性和扩展性的要求
- 两套后端 API 完全一致，前端无需任何修改即可切换

### 为什么前端不使用框架？

功能页面数量少（4 个主要视图）、交互复杂度低，使用原生技术栈可减少构建步骤和依赖体积，保持项目轻量。

## 扩展性说明

| 扩展方向 | 当前状态 | 建议方案 |
|----------|----------|----------|
| 多用户支持 | 仅支持单用户 | 扩展 `users` 表并在所有查询中加入 `user_id` 过滤 |
| 对象存储 | 本地文件系统 | 替换 `storage.js` / `LocalFileStorageService` 为 S3/MinIO 客户端 |
| 缩略图生成 | 未实现 | 上传后触发异步任务（Node 用 `sharp`，Java 用 `Thumbnailator`） |
| 分布式部署 | 单机运行 | Node 后端可升级至 PostgreSQL + Redis；Java 后端可引入 Spring Cloud Gateway |
