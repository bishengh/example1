# 前端文档

## 技术栈

- **HTML5**：语义化标签构建页面结构
- **CSS3**：原生 CSS，使用 Flexbox 和 Grid 布局
- **JavaScript (ES6+)**：原生 JS，无框架依赖，使用 `async/await` 处理异步请求

## 页面结构

应用为单页应用（SPA），所有视图通过 JavaScript 动态切换，无需页面刷新。

### 视图切换机制

页面包含 4 个主要 `section` 元素，分别对应不同视图：

| ID | 名称 | 说明 |
|----|------|------|
| `#login-screen` | 登录页 | 输入用户名密码进行身份验证 |
| `#gallery-screen` | 图库页 | 浏览、搜索、筛选照片，查看详情 |
| `#upload-screen` | 上传页 | 上传新照片，填写元数据 |
| `#albums-screen` | 相册管理页 | 创建、删除相册 |

通过 `showScreen(name)` 函数控制视图显示：先移除所有 `section` 的 `active` 类，再给目标视图添加 `active` 类。

## 核心函数说明

### `request(url, options)`

封装 `fetch` 的通用请求函数，自动携带 `credentials: 'include'`，默认解析 JSON 响应。

### `fetchCurrentUser()`

页面加载时调用，检查用户登录状态：
- 已登录 → 显示图库页并加载数据
- 未登录 → 显示登录页

### `loadAppData()`

并行加载相册列表和照片列表：`Promise.all([loadAlbums(), loadPhotos()])`

### `loadAlbums()` / `loadPhotos()`

分别从 `/api/albums` 和 `/api/photos` 获取数据，并渲染到对应容器。

### `renderPhotos()`

基于当前搜索关键词和相册筛选条件，对 `currentPhotos` 进行客户端过滤后渲染。

过滤逻辑：
- 文本匹配：标题、描述、标签（空格拼接后）任一包含搜索词
- 相册匹配：照片 `albumId` 等于筛选值，或筛选值为空（显示全部）

### `renderPhotoDetail(photo)`

在图库页下方展示选中照片的详细信息，包括大图、标签、上传时间等。

## 事件绑定

| 元素 | 事件 | 行为 |
|------|------|------|
| `#login-form` | submit | 提交登录请求 |
| `#upload-form` | submit | 使用 `FormData` 上传照片 |
| `#album-form` | submit | 创建新相册 |
| `#photo-list` | click（委托） | 点击"查看详情"按钮显示照片详情 |
| `#album-list` | click（委托） | 点击"删除"按钮删除相册 |
| `#filter-text` | input | 实时过滤照片 |
| `#album-filter` | change | 按相册筛选照片 |
| 导航按钮 | click | 切换视图或登出 |

## 安全说明

前端已实现 XSS 防护：
- 所有从后端获取并插入 DOM 的用户输入字段（如照片标题、描述、相册名称、标签等）均经过 `escapeHtml()` 函数处理
- `escapeHtml()` 将 `& < > " '` 转换为对应的 HTML 实体，防止恶意脚本注入

## 样式说明

### 响应式布局

- 页面最大宽度限制为 `1180px`，居中显示
- 照片网格使用 CSS Grid：`grid-template-columns: repeat(auto-fill, minmax(240px, 1fr))`
- 工具栏使用 Flexbox 并开启换行，适配小屏幕

### 主要组件样式

- `.screen`：默认隐藏（`display: none`），`.active` 时显示
- `.photo-card`：白色背景、圆角、轻微阴影的照片卡片
- `.album-item`：相册列表项，左右布局（信息 + 删除按钮）
- `.hidden`：用于隐藏照片详情面板
