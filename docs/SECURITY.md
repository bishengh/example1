# 安全文档

本文档说明个人照片图库已实现的安全措施及使用建议。

## 已实施的安全加固

### 1. 密码安全（bcrypt 哈希）

**问题**：早期版本使用明文存储和比对密码。  
**修复**：
- 引入 `bcrypt` 库，密码使用 10 轮 salt 进行哈希处理
- 数据库初始化时，若检测到已有明文密码（不以 `$2` 开头），自动平滑升级为哈希格式
- 登录时通过 `bcrypt.compare()` 进行安全比对

**相关文件**：
- `backend/src/init-db.js`
- `backend/src/routes/auth.js`

---

### 2. XSS 防护（HTML 实体编码）

**问题**：前端直接将用户输入内容插入 `innerHTML`，存在反射型/存储型 XSS 风险。  
**修复**：
- 在前端 `frontend/app.js` 中新增 `escapeHtml()` 工具函数
- 对所有从 API 获取的动态内容（照片标题、描述、标签、相册名称、URL 等）在插入 DOM 前进行编码

**编码规则**：
```javascript
&  → &amp;
<  → &lt;
>  → &gt;
"  → &quot;
'  → &#039;
```

**相关文件**：
- `frontend/app.js`

---

### 3. 文件上传安全（UUID 文件名 + 类型白名单）

**问题**：原始版本使用用户上传的原始文件名保存文件，存在同名覆盖和路径遍历风险（如 `../../../etc/passwd`）。  
**修复**：
- 后端仅允许 `image/jpeg`、`image/png`、`image/gif` 三种 MIME 类型
- 保存文件时丢弃原始文件名，使用 `crypto.randomUUID()` 生成唯一文件名
- 仅保留安全的文件扩展名（小写），彻底杜绝路径遍历和恶意覆盖

**相关文件**：
- `backend/src/storage.js`
- `backend/src/routes/photos.js`

---

### 4. 全局异常捕获

**问题**：Express 的 async 路由处理函数未包裹 `try-catch`，数据库异常会导致请求挂起或进程崩溃。  
**修复**：
- 引入 `express-async-errors`，使 Express 能够自动捕获 async 路由中的未处理 Promise 异常
- 在应用末尾添加统一错误处理中间件，返回 `500 { error: "服务器内部错误" }`
- 错误详情仅记录在服务端日志中，不暴露给客户端

**相关文件**：
- `backend/src/app.js`

---

### 5. Session 与会话管理

- 使用 `express-session` 管理用户登录状态
- Session Cookie 设有 24 小时的有效期（`maxAge: 24 * 60 * 60 * 1000`）
- 设置了 `resave: false` 和 `saveUninitialized: false`，减少不必要的会话写入

## 仍需注意的安全建议

### 生产环境必须配置

1. **修改 Session Secret**  
   当前 `backend/src/app.js` 中的 `secret` 为硬编码字符串。生产环境应通过环境变量注入随机强密钥：
   ```javascript
   secret: process.env.SESSION_SECRET || 'fallback-secret'
   ```

2. **使用 HTTPS**  
   在 HTTP 环境下，Session Cookie 可能被中间人截获。生产环境务必启用 HTTPS，并考虑设置 `cookie: { secure: true }`。

3. **限制 CORS 来源**  
   当前 CORS 配置为 `origin: true`，生产环境应限制为具体的前端域名：
   ```javascript
   app.use(cors({ origin: 'https://your-domain.com', credentials: true }));
   ```

4. **修改默认管理员密码**  
   首次部署后应立即登录并将默认密码 `password` 修改为强密码。

5. **定期备份数据**  
   `storage/gallery.db` 和上传的照片文件是核心资产，建议配置定时备份策略。

## 安全响应

如发现新的安全漏洞，请按以下优先级修复：
- **P0**：直接影响数据安全或系统可用性（如注入、认证绕过、RCE）
- **P1**：可能导致信息泄露或功能异常（如配置泄露、未授权访问）
- **P2**：建议改进项（如日志审计、速率限制）
