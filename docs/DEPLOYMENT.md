# 部署文档

## 环境要求

- Node.js >= 16
- npm >= 8
- 至少 512MB 可用内存
- 至少 1GB 可用磁盘空间（用于存储上传的照片）

## 开发环境部署

### 1. 克隆并安装依赖

```bash
cd /Users/zhang/source/kimi/example1
npm install
```

### 2. 配置环境变量（可选）

创建 `.env` 文件（参考项目根目录的 `.env.example`），可配置端口号和 Session Secret。

### 3. 启动开发服务器

```bash
npm run dev
```

服务默认监听 `http://localhost:3000`。

### 4. 访问应用

在浏览器中打开 `http://localhost:3000`，使用默认管理员账号登录：
- 用户名：`admin`
- 密码：`password`

## 生产环境部署

### 1. 环境变量配置

请务必修改以下配置，避免使用默认值：

```bash
export PORT=80
export SESSION_SECRET="your-very-strong-random-secret-key"
```

### 2. 使用 Node.js 直接运行

```bash
npm start
```

### 3. 使用 PM2 进程管理（推荐）

全局安装 PM2：
```bash
npm install -g pm2
```

启动应用：
```bash
pm2 start backend/src/app.js --name personal-photo-gallery
```

设置开机自启：
```bash
pm2 startup
pm2 save
```

### 4. 使用 Nginx 反向代理

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    client_max_body_size 20M;
}
```

> 注意：`client_max_body_size` 需要根据实际照片大小进行调整，默认建议 20MB。

## 数据持久化

生产环境中，请确保以下目录已被正确持久化或备份：

| 路径 | 说明 |
|------|------|
| `storage/gallery.db` | SQLite 数据库文件，包含用户、相册、照片元数据 |
| `storage/` 目录下的图片文件 | 用户实际上传的原始照片文件 |

## 安全建议

1. **修改默认密码**：首次部署后请立即登录并修改默认管理员密码
2. **使用 HTTPS**：生产环境务必配置 SSL/TLS 证书
3. **限制 CORS**：修改 `backend/src/app.js` 中的 `cors({ origin: true, ... })` 为具体的域名白名单
4. **定期备份**：建议每日备份 `storage/` 目录到远程存储

## 升级说明

若从旧版本升级到新版本：
1. 先备份 `storage/gallery.db` 和上传的照片文件
2. 执行 `git pull` 拉取新代码
3. 执行 `npm install` 安装新增依赖
4. 重启服务，数据库初始化脚本会自动完成表结构更新和密码哈希升级
