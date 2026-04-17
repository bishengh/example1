# 数据库设计文档

本项目使用 **SQLite** 作为关系型数据库，数据库文件位于 `storage/gallery.db`。

## 表结构

### 1. `users` — 用户表

存储系统用户的基本信息。当前版本仅支持单一管理员用户。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 用户唯一标识 |
| `username` | TEXT | UNIQUE NOT NULL | 用户名 |
| `password` | TEXT | NOT NULL | bcrypt 哈希后的密码 |

**初始数据：**
- 用户名：`admin`
- 密码：经 bcrypt 哈希处理后的 `password`

---

### 2. `albums` — 相册表

存储用户创建的相册信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 相册唯一标识 |
| `name` | TEXT | NOT NULL | 相册名称 |
| `description` | TEXT | DEFAULT '' | 相册描述 |

---

### 3. `photos` — 照片表

存储上传照片的元数据信息。

| 字段名 | 类型 | 约束 | 说明 |
|--------|------|------|------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | 照片唯一标识 |
| `filename` | TEXT | NOT NULL | 服务器端唯一文件名（UUID + 扩展名） |
| `title` | TEXT | DEFAULT '' | 照片标题 |
| `description` | TEXT | DEFAULT '' | 照片描述 |
| `tags` | TEXT | DEFAULT '' | 标签，以逗号分隔的字符串 |
| `album_id` | INTEGER | FOREIGN KEY | 所属相册 ID，引用 `albums(id)` |
| `uploaded_at` | TEXT | NOT NULL | 上传时间，ISO 8601 格式 |

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

## 建表语句

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

## 数据初始化逻辑

系统启动时会自动执行 `backend/src/init-db.js`：
1. 检查并创建上述三张表
2. 检查是否存在 `admin` 用户，若不存在则创建默认管理员账号
3. 若已存在 `admin` 用户但密码仍为明文（不以 `$2` 开头），自动将其升级为 bcrypt 哈希格式

## 标签存储说明

由于 SQLite 不支持原生数组类型，`tags` 字段采用逗号分隔的字符串形式存储：
- 存储格式：`"标签1,标签2,标签3"`
- 后端在序列化/反序列化时自动进行 `.split(',')` 和 `.join(',')` 转换
- 空标签会被 `.filter(Boolean)` 过滤掉
