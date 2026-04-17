# 变更日志

所有 notable 变更都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)。

## [Unreleased]

### 新增
- 新增 **Java + Spring Boot + PostgreSQL** 后端实现 (`backend-java/`)，与 Node.js 后端共用同一套前端和 API 规范
- Java 后端完整覆盖认证、照片、相册三大模块，共 27 个 JUnit 测试用例全部通过

### 安全
- **Node.js 后端**：
  - 使用 `bcrypt` 对密码进行哈希存储和比对，替代明文存储（P0 修复）
  - 前端新增 `escapeHtml()` 函数，对所有动态用户输入内容编码，修复 XSS 注入漏洞（P0 修复）
  - 上传文件使用 `crypto.randomUUID()` 生成唯一文件名，修复路径遍历和同名覆盖风险（P0 修复）
  - 引入 `express-async-errors` 与统一错误中间件，修复 async 路由未捕获异常导致请求挂起的问题（P0 修复）
- **Java 后端**：
  - 修复 `AlbumController.list()` 缺失登录校验的问题
  - 修复 `AuthController` 空字符串校验不完善的问题
  - 修复 `User` 实体表名与 H2 保留关键字冲突的问题
  - 提取 `FileStorageService` 接口，避免实现类与框架紧耦合

### 测试
- **Node.js 后端**：
  - 扩充认证接口测试至 8 个用例
  - 扩充照片接口测试至 9 个用例
  - 新增相册接口测试 8 个用例
  - 修复测试与异步数据库初始化的竞态问题（添加 `--runInBand`）
  - 当前总计 24 个测试用例全部通过
- **Java 后端**：
  - 新增 `AuthControllerTest`（7 用例）
  - 新增 `PhotoControllerTest`（9 用例）
  - 新增 `AlbumControllerTest`（8 用例）
  - 新增 `FileStorageServiceTest`（2 用例）
  - 修复 POM 中 Spring Boot 版本号错误、编译器参数缺失、Java 25 与 Mockito 不兼容等问题
  - 当前总计 27 个测试用例全部通过

### 文档
- 重写根目录 [README.md](./README.md)，补充双后端架构说明
- 重写 [backend-java/README.md](./backend-java/README.md)，补充构建、测试、部署说明
- 更新 [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md)，加入 Java 后端架构图和模块说明
- 更新 [docs/DATABASE.md](./docs/DATABASE.md)，补充 PostgreSQL + JPA 实现说明
- 更新 [docs/DEPLOYMENT.md](./docs/DEPLOYMENT.md)，补充 Java 后端生产部署方案（systemd + Nginx）
- 更新 [docs/TESTING.md](./docs/TESTING.md)，补充 Java 后端测试说明和双后端对比
- 保留 [docs/API.md](./docs/API.md)、[docs/FRONTEND.md](./docs/FRONTEND.md)、[docs/SECURITY.md](./docs/SECURITY.md)

## [0.1.0] - 2026-04-17

### 新增
- 个人照片图库基础功能：照片上传、浏览、搜索、相册管理
- 基于 Express + SQLite 的 Node.js 后端服务
- 基于原生 HTML/CSS/JS 的单页前端应用
- 基础 Session 认证机制
- 初始 Jest + Supertest 测试覆盖
