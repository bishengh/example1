# 部署文档

本文档涵盖 Node.js 后端和 Java 后端两套部署方案，请根据实际场景选择。

---

## Node.js 后端部署

### 环境要求

- Node.js >= 16
- npm >= 8
- 至少 512MB 可用内存
- 至少 1GB 可用磁盘空间（用于存储上传的照片）

### 开发环境部署

```bash
cd /Users/zhang/source/kimi/example1
npm install
npm run dev
```

服务启动后，访问 http://localhost:3000

### 生产环境部署

#### 1. 环境变量配置

```bash
export PORT=80
export SESSION_SECRET="your-very-strong-random-secret-key"
```

#### 2. 使用 PM2 进程管理（推荐）

```bash
npm install -g pm2
pm2 start backend/src/app.js --name personal-photo-gallery
pm2 startup
pm2 save
```

#### 3. Nginx 反向代理

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

#### 4. 数据持久化

| 路径 | 说明 |
|------|------|
| `storage/gallery.db` | SQLite 数据库文件 |
| `storage/` 目录下的图片文件 | 用户实际上传的原始照片文件 |

---

## Java 后端部署

### 环境要求

- JDK 17+
- Maven 3.9+
- PostgreSQL 14+（生产环境）
- 至少 1GB 可用内存
- 至少 2GB 可用磁盘空间

### 开发环境部署

#### 1. 启动 PostgreSQL

```bash
docker compose -f backend-java/docker-compose.yml up -d
```

#### 2. 编译并运行

```bash
cd backend-java
mvn clean spring-boot:run
```

服务启动后，访问 http://localhost:8080

### 生产环境部署

#### 1. 配置环境变量

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://<db-host>:5432/gallery
export SPRING_DATASOURCE_USERNAME=gallery
export SPRING_DATASOURCE_PASSWORD=<strong-password>
export APP_STORAGE_PATH=/data/gallery-uploads
export PORT=8080
```

#### 2. 打包

```bash
cd backend-java
mvn clean package -DskipTests
```

#### 3. 运行 JAR

```bash
java -jar target/personal-photo-gallery-0.1.0.jar
```

#### 4. 使用 systemd 管理服务（推荐）

创建 `/etc/systemd/system/gallery.service`：

```ini
[Unit]
Description=Personal Photo Gallery (Java)
After=network.target

[Service]
Type=simple
User=app
WorkingDirectory=/opt/gallery
Environment="SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/gallery"
Environment="SPRING_DATASOURCE_USERNAME=gallery"
Environment="SPRING_DATASOURCE_PASSWORD=your-password"
Environment="APP_STORAGE_PATH=/data/gallery-uploads"
ExecStart=/usr/bin/java -jar /opt/gallery/personal-photo-gallery-0.1.0.jar
Restart=always

[Install]
WantedBy=multi-user.target
```

启用并启动：

```bash
sudo systemctl daemon-reload
sudo systemctl enable gallery
sudo systemctl start gallery
```

#### 5. Nginx 反向代理

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    client_max_body_size 50M;
}
```

#### 6. 数据持久化

| 路径 | 说明 |
|------|------|
| PostgreSQL 数据库 | 用户、相册、照片元数据 |
| `APP_STORAGE_PATH` 目录下的图片文件 | 用户实际上传的原始照片文件 |

---

## 通用安全建议

1. **修改默认密码**：首次部署后请立即登录并修改默认管理员密码
2. **使用 HTTPS**：生产环境务必配置 SSL/TLS 证书
3. **限制 CORS**：将 `origin: true` 改为具体的域名白名单
4. **定期备份**：
   - Node.js：备份 `storage/` 目录
   - Java：备份 PostgreSQL 数据库和上传文件目录
5. **文件上传大小限制**：根据 Nginx 和服务器配置调整 `client_max_body_size`
