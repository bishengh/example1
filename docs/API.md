# API 文档

本文档描述个人照片图库后端提供的所有 RESTful API 接口。

## 通用约定

- 基础地址：`http://localhost:3000`
- 所有受保护接口需要携带有效的 Session Cookie
- 请求/响应格式：`application/json`（照片上传除外，使用 `multipart/form-data`）
- 通用错误响应格式：`{ "error": "错误描述" }`

## 认证接口

### POST `/api/auth/login`

用户登录。

**请求体：**
```json
{
  "username": "admin",
  "password": "password"
}
```

**成功响应（200）：**
```json
{
  "message": "登录成功",
  "user": {
    "id": 1,
    "username": "admin"
  }
}
```

**失败响应：**
- `400`：用户名或密码为空
- `401`：用户名或密码错误

---

### POST `/api/auth/logout`

用户登出，销毁当前 Session。

**成功响应（200）：**
```json
{
  "message": "已退出登录"
}
```

---

### GET `/api/auth/me`

获取当前登录用户信息。

**成功响应（200）：**
```json
{
  "user": {
    "id": 1,
    "username": "admin"
  }
}
```

**失败响应：**
- `401`：未登录

## 照片接口

### GET `/api/photos`

获取当前用户的所有照片列表，按上传时间倒序排列。

**成功响应（200）：**
```json
[
  {
    "id": 1,
    "title": "测试照片",
    "description": "照片描述",
    "tags": ["标签1", "标签2"],
    "uploadedAt": "2026-04-17T05:00:00.000Z",
    "albumId": 1,
    "filename": "uuid.png",
    "url": "/uploads/uuid.png"
  }
]
```

**失败响应：**
- `401`：未登录

---

### GET `/api/photos/:id`

获取单张照片详情。

**成功响应（200）：** 同列表项格式

**失败响应：**
- `401`：未登录
- `404`：照片不存在

---

### POST `/api/photos`

上传新照片。请求类型为 `multipart/form-data`。

**请求字段：**

| 字段名 | 类型 | 必填 | 说明 |
|--------|------|------|------|
| `image` | File | 是 | 图像文件，仅支持 JPG、PNG、GIF |
| `title` | String | 否 | 照片标题 |
| `description` | String | 否 | 照片描述 |
| `tags` | String | 否 | 标签，逗号分隔 |
| `albumId` | Number | 否 | 所属相册 ID |

**成功响应（201）：**
```json
{
  "id": 1,
  "filename": "uuid.png",
  "title": "测试照片",
  "description": "照片描述",
  "tags": ["标签1", "标签2"],
  "album_id": 1,
  "uploaded_at": "2026-04-17T05:00:00.000Z",
  "url": "/uploads/uuid.png"
}
```

**失败响应：**
- `400`：未上传文件或文件格式不支持
- `401`：未登录

---

### PUT `/api/photos/:id`

更新照片元数据。

**请求体：**
```json
{
  "title": "新标题",
  "description": "新描述",
  "tags": "新标签1, 新标签2",
  "albumId": 2
}
```

**成功响应（200）：** 更新后的照片对象

**失败响应：**
- `401`：未登录
- `404`：照片不存在

## 相册接口

### GET `/api/albums`

获取所有相册列表，按名称升序排列。

**成功响应（200）：**
```json
[
  {
    "id": 1,
    "name": "旅行",
    "description": "旅行照片"
  }
]
```

**失败响应：**
- `401`：未登录

---

### POST `/api/albums`

创建新相册。

**请求体：**
```json
{
  "name": "新相册",
  "description": "相册描述"
}
```

**成功响应（201）：**
```json
{
  "id": 1,
  "name": "新相册",
  "description": "相册描述"
}
```

**失败响应：**
- `400`：相册名称为空
- `401`：未登录

---

### PUT `/api/albums/:id`

更新相册信息。

**请求体：**
```json
{
  "name": "更新后的名称",
  "description": "更新后的描述"
}
```

**成功响应（200）：** 更新后的相册对象

**失败响应：**
- `401`：未登录
- `404`：相册不存在

---

### DELETE `/api/albums/:id`

删除相册。删除后，原属于该相册的照片的 `album_id` 会被自动置为 `NULL`。

**成功响应（200）：**
```json
{
  "message": "相册已删除"
}
```

**失败响应：**
- `401`：未登录
- `404`：相册不存在
