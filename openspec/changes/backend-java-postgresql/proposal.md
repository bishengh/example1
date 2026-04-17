## Why

当前项目后端基于 Node.js 和 SQLite，已实现个人照片图库功能。为了提高系统可维护性、企业级兼容性和数据库扩展能力，计划将后端开发语言改为 Java，同时将数据库改为 PostgreSQL。

## What Changes

- 将后端服务从 Node.js 迁移到 Java，实现相同的 API 和业务逻辑。
- 将存储层从 SQLite 切换到 PostgreSQL，支持更高并发和更稳健的数据管理。
- 保留现有个人图库功能、认证流程和照片管理体验，不引入新的用户功能。
- 调整项目结构、构建工具以及部署配置以支持 Java 后端。

## Capabilities

### New Capabilities
- `backend-java`: 后端迁移到 Java，包括 API 服务、业务模型和控制层。
- `postgres-database`: 数据库迁移到 PostgreSQL，包含架构定义、连接配置和数据库操作。
- `java-build-config`: Java 构建与运行配置，包括 Maven/Gradle 脚本和本地运行支持。

### Modified Capabilities
- 无

## Impact

- 影响后端代码：从 `backend/src` 转换为 Java 源码和构建文件。
- 影响数据库依赖：移除 SQLite，新增 PostgreSQL 客户端和连接配置。
- 影响部署配置：需要支持 Java 运行环境和 PostgreSQL 数据库实例。
- 影响开发流程：本地运行、测试和构建命令将调整为 Java 工具链。
