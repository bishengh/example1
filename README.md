# 个人照片图库

一个基于 Node.js + Express + SQLite 的轻量级个人照片管理应用，支持照片上传、相册分类、标签搜索和基础身份认证。

## 功能特性

- **用户认证**：基于 Session 的登录/登出，密码使用 bcrypt 哈希存储
- **照片管理**：支持上传 JPG/PNG/GIF 格式照片，可添加标题、描述和标签
- **相册分类**：创建相册并将照片归类，删除相册时自动解除关联
- **搜索过滤**：按标题、描述、标签关键词搜索，或按相册筛选照片
- **响应式前端**：原生 HTML/CSS/JS，简洁的卡片式布局

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Node.js, Express, SQLite3 |
| 前端 | 原生 HTML5, CSS3, JavaScript (ES6+) |
| 测试 | Jest, Supertest |
| 安全 | bcrypt, express-session, express-async-errors |

## 快速开始

### 环境要求

- Node.js >= 16

### 安装依赖

```bash
npm install
```

### 运行开发服务器

```bash
npm run dev
```

服务启动后，访问 http://localhost:3000

默认管理员账号：
- 用户名：`admin`
- 密码：`password`

### 运行测试

```bash
npm test
```

## 项目结构

```
.
├── backend/
│   ├── src/
│   │   ├── app.js              # Express 应用入口
│   │   ├── db.js               # SQLite 连接与 Promise 封装
│   │   ├── init-db.js          # 数据库初始化与默认用户创建
│   │   ├── storage.js          # 上传文件存储逻辑
│   │   ├── middleware/
│   │   │   └── auth.js         # 登录校验中间件
│   │   └── routes/
│   │       ├── auth.js         # 认证接口
│   │       ├── photos.js       # 照片 CRUD 接口
│   │       └── albums.js       # 相册 CRUD 接口
│   └── tests/                  # Jest 单元测试
├── frontend/
│   ├── index.html
│   ├── style.css
│   └── app.js
├── storage/                    # SQLite 数据库与上传文件
└── package.json
```

## API 概览

### 认证

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/login` | 登录（username, password） |
| POST | `/api/auth/logout` | 登出 |
| GET | `/api/auth/me` | 获取当前登录用户 |

### 照片

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/photos` | 获取照片列表 |
| GET | `/api/photos/:id` | 获取单张照片详情 |
| POST | `/api/photos` | 上传照片（multipart/form-data） |
| PUT | `/api/photos/:id` | 更新照片信息 |

### 相册

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | `/api/albums` | 获取相册列表 |
| POST | `/api/albums` | 创建相册 |
| PUT | `/api/albums/:id` | 更新相册信息 |
| DELETE | `/api/albums/:id` | 删除相册 |

## 安全说明

本项目已实现以下安全加固：

- **密码安全**：使用 bcrypt 对密码进行哈希处理，拒绝明文存储
- **XSS 防护**：前端对所有用户输入内容（照片标题、描述、标签、相册名称等）进行 HTML 实体编码
- **文件上传安全**：上传文件使用 `crypto.randomUUID()` 生成唯一文件名，杜绝路径遍历和同名覆盖风险
- **全局异常捕获**：通过 `express-async-errors` 与统一错误中间件，防止 async 路由未捕获异常导致请求挂起
- **会话安全**：Session Cookie 设有合理的过期时间

## 开源许可

MIT
