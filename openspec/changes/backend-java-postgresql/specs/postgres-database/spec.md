## ADDED Requirements

### Requirement: PostgreSQL 数据库支持
系统应将数据存储切换到 PostgreSQL，支持关系型表结构、事务和更高并发能力。

#### Scenario: PostgreSQL 保存照片元数据
- **WHEN** 用户上传照片并提交元数据
- **THEN** 系统将照片信息保存到 PostgreSQL 中，并返回保存后的记录

### Requirement: PostgreSQL 连接配置
系统应提供 PostgreSQL 连接配置，支持本地开发和生产环境连接参数。

#### Scenario: 使用连接配置初始化数据库
- **WHEN** 后端启动时读取数据库配置
- **THEN** 系统成功连接到 PostgreSQL 实例并准备执行查询
