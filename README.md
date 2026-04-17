# 个人照片图库

一个轻量级的个人照片管理应用，支持照片上传、相册分类、标签搜索和基础身份认证。项目提供 **Node.js** 和 **Java (Spring Boot)** 两套后端实现，共用同一套前端界面。

## 功能特性

- **用户认证**：基于 Session 的登录/登出，密码使用 bcrypt 哈希存储
- **照片管理**：支持上传 JPG/PNG/GIF 格式照片，可添加标题、描述和标签
- **相册分类**：创建相册并将照片归类，删除相册时自动解除关联
- **搜索过滤**：按标题、描述、标签关键词搜索，或按相册筛选照片
- **响应式前端**：原生 HTML/CSS/JS，简洁的卡片式布局

## 技术栈

| 层级 | Node.js 后端 | Java 后端 |
|------|-------------|-----------|
| 后端框架 | Express | Spring Boot 3.2 |
| 数据库 | SQLite3 | PostgreSQL |
| ORM / 数据访问 | 原生 SQL | Spring Data JPA |
| 前端 | 原生 HTML5, CSS3, JavaScript (ES6+) | 共用 |
| 测试 | Jest, Supertest | JUnit 5, Mockito, Spring MockMvc |
| 安全 | bcrypt, express-session | Spring Session (内嵌 Tomcat) |

## 快速开始

### 方式一：Node.js 后端（推荐快速体验）

环境要求：Node.js >= 16

```bash
npm install
npm run dev
```

服务启动后，访问 http://localhost:3000

### 方式二：Java 后端（推荐生产环境）

环境要求：JDK 17+，Maven 3.9+，PostgreSQL

```bash
cd backend-java
mvn clean spring-boot:run
```

服务启动后，访问 http://localhost:8080

> Java 后端默认需要 PostgreSQL，可使用项目提供的 `docker-compose.yml` 快速启动数据库：
> ```bash
> docker compose -f backend-java/docker-compose.yml up -d
> ```

### 默认管理员账号

- 用户名：`admin`
- 密码：`password`

## 项目结构

```
.
├── backend/                    # Node.js + Express + SQLite 后端
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
├── backend-java/               # Java + Spring Boot + PostgreSQL 后端
│   ├── src/main/java/...       # Spring Boot 业务代码
│   ├── src/test/java/...       # JUnit 单元测试
│   ├── pom.xml                 # Maven 构建配置
│   └── docker-compose.yml      # PostgreSQL 开发环境
├── frontend/                   # 共用前端（原生 HTML/CSS/JS）
│   ├── index.html
│   ├── style.css
│   └── app.js
├── docs/                       # 项目文档
│   ├── API.md
│   ├── ARCHITECTURE.md
│   ├── DATABASE.md
│   ├── DEPLOYMENT.md
│   ├── FRONTEND.md
│   ├── SECURITY.md
│   └── TESTING.md
├── storage/                    # SQLite 数据库与上传文件（Node 后端）
└── package.json
```

## API 概览

两套后端实现提供**完全一致**的 RESTful API：

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

> 详细的请求/响应格式请参考 [docs/API.md](./docs/API.md)

## 测试

### Node.js 后端测试

```bash
npm test
```

### Java 后端测试

```bash
cd backend-java
mvn clean test
```

## 文档索引

| 文档 | 说明 |
|------|------|
| [docs/API.md](./docs/API.md) | 详细 API 文档 |
| [docs/ARCHITECTURE.md](./docs/ARCHITECTURE.md) | 系统架构设计 |
| [docs/DATABASE.md](./docs/DATABASE.md) | 数据库设计与模型说明 |
| [docs/DEPLOYMENT.md](./docs/DEPLOYMENT.md) | 部署与运维指南 |
| [docs/FRONTEND.md](./docs/FRONTEND.md) | 前端实现说明 |
| [docs/SECURITY.md](./docs/SECURITY.md) | 安全加固与建议 |
| [docs/TESTING.md](./docs/TESTING.md) | 测试策略与用例说明 |

## 开源许可

MIT
