# 变更日志

所有 notable 变更都会记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)。

## [Unreleased]

### 安全
- 使用 `bcrypt` 对密码进行哈希存储和比对，替代明文存储（P0 修复）
- 前端新增 `escapeHtml()` 函数，对所有动态用户输入内容编码，修复 XSS 注入漏洞（P0 修复）
- 上传文件使用 `crypto.randomUUID()` 生成唯一文件名，修复路径遍历和同名覆盖风险（P0 修复）
- 引入 `express-async-errors` 与统一错误中间件，修复 async 路由未捕获异常导致请求挂起的问题（P0 修复）

### 测试
- 扩充认证接口测试至 8 个用例
- 扩充照片接口测试至 9 个用例
- 新增相册接口测试 8 个用例
- 修复测试与异步数据库初始化的竞态问题
- 当前总计 24 个测试用例全部通过

### 文档
- 新增 README.md
- 新增 API 文档（docs/API.md）
- 新增架构设计文档（docs/ARCHITECTURE.md）
- 新增数据库设计文档（docs/DATABASE.md）
- 新增部署文档（docs/DEPLOYMENT.md）
- 新增前端文档（docs/FRONTEND.md）
- 新增安全文档（docs/SECURITY.md）
- 新增测试文档（docs/TESTING.md）
- 新增环境变量示例文件（.env.example）

## [0.1.0] - 2026-04-17

### 新增
- 个人照片图库基础功能：照片上传、浏览、搜索、相册管理
- 基于 Express + SQLite 的后端服务
- 基于原生 HTML/CSS/JS 的单页前端应用
- 基础 Session 认证机制
- 初始 Jest + Supertest 测试覆盖
