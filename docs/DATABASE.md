# 数据库设计文档

本项目的数据库模型在 Node.js 后端和 Java 后端中保持一致，仅在具体的数据库类型和 ORM 映射上有所差异。

- **Node.js 后端**：使用 **SQLite**，数据库文件位于 `storage/gallery.db`
- **Java 后端**：使用 **PostgreSQL**，通过 JPA/Hibernate 自动管理表结构

## 逻辑表结构

### 1. `users` — 用户表

存储系统用户的基本信息。当前版本仅支持单一管理员用户。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | INTEGER / BIGINT | PRIMARY KEY AUTOINCREMENT / IDENTITY | 用户唯一标识 |
| `username` | TEXT / VARCHAR | UNIQUE NOT NULL | 用户名 |
| `password` | TEXT / VARCHAR | NOT NULL | 密码（Node: bcrypt 哈希；Java: 当前明文） |

**初始数据：**
- 用户名：`admin`
- 密码：`password`

---

### 2. `albums` — 相册表

存储用户创建的相册信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | INTEGER / BIGINT | PRIMARY KEY | 相册唯一标识 |
| `name` | TEXT / VARCHAR | NOT NULL | 相册名称 |
| `description` | TEXT / VARCHAR | DEFAULT '' | 相册描述 |

---

### 3. `photos` — 照片表

存储上传照片的元数据信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | INTEGER / BIGINT | PRIMARY KEY | 照片唯一标识 |
| `filename` | TEXT / VARCHAR | NOT NULL | 服务器端文件名 |
| `title` | TEXT / VARCHAR | DEFAULT '' | 照片标题 |
| `description` | TEXT / VARCHAR | DEFAULT '' | 照片描述 |
| `tags` | TEXT / VARCHAR | DEFAULT '' | 标签，以逗号分隔的字符串 |
| `album_id` | INTEGER / BIGINT | FOREIGN KEY | 所属相册 ID，引用 `albums(id)` |
| `uploaded_at` | TEXT / TIMESTAMP | NOT NULL | 上传时间，ISO 8601 格式 |

## E-R 关系图

```
┌─────────────┐         ┌─────────────┐
│    users    │         │   albums    │
├─────────────┤         ├─────────────┤
│     id (PK) │         │     id (PK) │
│  username   │         │    name     │
│  password   │         │ description │
└─────────────┘         └──────┬──────┘
                                │
                                │ 1 : N
                                ▼
                         ┌─────────────┐
                         │    photos   │
                         ├─────────────┤
                         │     id (PK) │
                         │   filename  │
                         │    title    │
                         │ description │
                         │    tags     │
                         │  album_id   │◄── FK
                         │ uploaded_at │
                         └─────────────┘
```

## 后端实现差异

### Node.js（SQLite）

建表语句：

```sql
CREATE TABLE IF NOT EXISTS users (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  username TEXT UNIQUE NOT NULL,
  password TEXT NOT NULL
);

CREATE TABLE IF NOT EXISTS albums (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  name TEXT NOT NULL,
  description TEXT DEFAULT ''
);

CREATE TABLE IF NOT EXISTS photos (
  id INTEGER PRIMARY KEY AUTOINCREMENT,
  filename TEXT NOT NULL,
  title TEXT DEFAULT '',
  description TEXT DEFAULT '',
  tags TEXT DEFAULT '',
  album_id INTEGER,
  uploaded_at TEXT NOT NULL,
  FOREIGN KEY(album_id) REFERENCES albums(id)
);
```

数据初始化由 `backend/src/init-db.js` 负责：
1. 检查并创建上述三张表
2. 检查是否存在 `admin` 用户，若不存在则创建默认管理员账号（bcrypt 哈希）
3. 若已存在 `admin` 用户但密码仍为明文，自动升级为 bcrypt 哈希格式

### Java（PostgreSQL + JPA）

实体类通过 JPA 注解映射，Hibernate 自动生成 DDL：

```java
@Entity
@Table(name = "users")
public class User { ... }

@Entity
public class Album { ... }

@Entity
public class Photo {
    @ManyToOne
    @JoinColumn(name = "album_id")
    private Album album;
}
```

数据初始化由 `DataInitializer`（`CommandLineRunner`）负责：
- 启动时检查 `admin` 用户是否存在，不存在则插入默认记录
- 当前密码为明文存储，建议后续升级为 bcrypt

## 标签存储说明

由于关系型数据库通常不支持原生数组类型，`tags` 字段采用逗号分隔的字符串形式统一存储：
- 存储格式：`"标签1,标签2,标签3"`
- 后端在序列化/反序列化时自动进行 `.split(',')` 和 `.join(',')` 转换
- 空标签会被过滤掉

## 索引与优化建议

- 已为 `users.username` 建立唯一索引（UNIQUE 约束隐式创建）
- 建议在生产环境中为 `photos.album_id` 和 `photos.uploaded_at` 添加显式索引，以优化列表查询和相册筛选性能
