# 安全文档

本文档说明个人照片图库已实现的安全措施、已知风险及使用建议。

---

## Node.js 后端已实施的安全加固

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

**问题**：原始版本使用用户上传的原始文件名保存文件，存在同名覆盖和路径遍历风险。  
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

## Java 后端安全状态

### 已实施

- **登录校验**：所有管理接口（照片、相册）均校验 `HttpSession` 中的用户状态，未登录返回 401
- **全局异常处理**：`GlobalExceptionHandler` 捕获运行时异常，将"未登录"映射为 401，其余映射为 400/500，避免堆栈信息泄露
- **文件类型白名单**：`PhotoController` 上传接口仅接受 `image/jpeg`、`image/png`、`image/gif`
- **XSS 防护**：与 Node.js 后端共用同一套前端，`escapeHtml()` 已生效

### ⚠️ 已知风险

#### 1. 密码明文存储（待修复）

**风险级别：P0**

Java 后端当前使用明文密码进行比对（`user.getPassword().equals(password)`），未引入 bcrypt 或 Argon2 等哈希算法。  
**建议**：引入 `spring-security-crypto` 或 `jBCrypt`，在 `DataInitializer` 和 `AuthController` 中实现哈希存储与比对。

#### 2. 文件上传使用原始文件名

**风险级别：P1**

Java 后端当前保留用户上传的原始文件名（通过 `StringUtils.cleanPath`），存在同名覆盖风险，且未生成 UUID。  
**建议**：在 `LocalFileStorageService.storeFile()` 中生成唯一文件名（如 `UUID + 扩展名`）。

#### 3. Session Secret 未外部化

Tomcat 内嵌会话管理依赖默认配置，生产环境建议通过外部配置或 Redis 实现分布式会话共享。

---

## 生产环境必须配置

### 通用建议

1. **修改默认管理员密码**  
   首次部署后应立即登录并将默认密码 `password` 修改为强密码。

2. **使用 HTTPS**  
   在 HTTP 环境下，Session Cookie 可能被中间人截获。生产环境务必启用 HTTPS。
   - Node.js：考虑设置 `cookie: { secure: true }`
   - Java：通过 Nginx 反向代理强制 HTTPS

3. **限制 CORS 来源**  
   - Node.js：将 `origin: true` 改为具体域名
   - Java：在 `WebConfig` 中添加 `@CrossOrigin(origins = "https://your-domain.com")` 或全局 CORS 配置

4. **定期备份数据**  
   - Node.js：备份 `storage/` 目录（含 `gallery.db` 和图片）
   - Java：备份 PostgreSQL 数据库和 `APP_STORAGE_PATH` 目录下的图片

### Node.js 特有

5. **修改 Session Secret**  
   当前 `backend/src/app.js` 中的 `secret` 为硬编码字符串。生产环境应通过环境变量注入随机强密钥。

---

## 安全响应优先级

如发现新的安全漏洞，请按以下优先级修复：
- **P0**：直接影响数据安全或系统可用性（如注入、认证绕过、RCE、明文密码）
- **P1**：可能导致信息泄露或功能异常（如配置泄露、未授权访问、文件名覆盖）
- **P2**：建议改进项（如日志审计、速率限制、会话外部化）
