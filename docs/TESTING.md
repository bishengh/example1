# 测试文档

本项目包含两套后端实现，分别使用不同的测试技术栈，但测试策略保持一致：覆盖认证、照片、相册三大核心模块，验证正常流程、异常输入和权限控制。

---

## Node.js 后端测试

### 测试框架

- **Jest**：JavaScript 测试运行器
- **Supertest**：HTTP 断言库，用于测试 Express API

### 运行测试

```bash
# 运行全部测试
npm test

# 带覆盖率报告
npm test -- --coverage

# 调试特定测试文件
npx jest backend/tests/photos.test.js
```

### 测试文件结构

```
backend/tests/
├── auth.test.js    # 认证接口测试 (8)
├── photos.test.js  # 照片接口测试 (9)
└── albums.test.js  # 相册接口测试 (8)
```

### 测试设计原则

#### 数据隔离与清理

每个测试文件在 `afterAll` 中负责清理自身产生的测试数据：
- 删除测试创建的照片和相册数据库记录
- 删除测试上传的物理文件（`storage/` 目录下的图片）
- 关闭 SQLite 数据库连接，防止 Jest 进程挂起

#### 登录态复用

每个测试文件在 `beforeAll` 中完成一次登录，获取 `set-cookie`，后续用例复用该 Cookie，减少重复登录开销。

#### 等待数据库就绪

由于 `initializeDatabase()` 是异步操作，所有测试文件都通过 `await app.ready` 确保数据库已初始化完成后再执行测试，避免竞态条件。

### 测试用例覆盖

| 模块 | 用例数 | 覆盖重点 |
|------|--------|---------|
| 认证 | 8 | 正常登录、空凭据、密码错误、未登录访问、登出 |
| 照片 | 9 | 上传、格式校验、列表、详情、更新、404 |
| 相册 | 8 | 创建、更新、删除、解关联、权限控制 |

**总计：24 个测试用例**

---

## Java 后端测试

### 测试框架

- **JUnit 5**：Java 单元测试框架
- **Mockito**：模拟对象框架
- **Spring MockMvc**：Spring MVC 层的集成测试

### 运行测试

```bash
cd backend-java
mvn clean test
```

测试环境使用 **H2 内存数据库**（由 `application-test.yml` 配置），无需启动 PostgreSQL。

### 测试文件结构

```
backend-java/src/test/java/com/example/gallery/
├── GalleryApplicationTests.java              # 上下文加载 (1)
├── controller/
│   ├── AuthControllerTest.java               # 认证接口测试 (7)
│   ├── PhotoControllerTest.java              # 照片接口测试 (9)
│   └── AlbumControllerTest.java              # 相册接口测试 (8)
└── service/
    └── FileStorageServiceTest.java           # 文件存储测试 (2)
```

### 测试设计原则

#### WebMvcTest 切片测试

Controller 测试使用 `@WebMvcTest` 注解，仅加载 Spring MVC 相关组件，不启动完整容器，测试速度快。数据库访问层通过 `@MockBean` 替换为 Mockito Mock。

#### Session 模拟

通过 `MockHttpSession` 模拟已登录用户状态，验证受保护接口的权限控制逻辑。

#### 独立单元测试

`FileStorageServiceTest` 为纯单元测试，不依赖 Spring 容器，直接实例化 `LocalFileStorageService` 并验证文件存取行为。

### 测试用例覆盖

| 模块 | 用例数 | 覆盖重点 |
|------|--------|---------|
| 认证 | 7 | 正常登录、空凭据、密码错误、未登录访问、登出 |
| 照片 | 9 | 上传、格式校验、列表、详情、更新、404 |
| 相册 | 8 | 创建、更新、删除、解关联、权限控制 |
| 文件存储 | 2 | 存储、目录创建 |
| 上下文 | 1 | Spring Boot 应用正常启动 |

**总计：27 个测试用例**

---

## 后端测试对比

| 维度 | Node.js | Java |
|------|---------|------|
| 测试框架 | Jest + Supertest | JUnit 5 + Mockito + MockMvc |
| 数据库 | SQLite（共用文件） | H2（内存，测试隔离） |
| 测试类型 | 集成测试（直接发 HTTP 请求） | 切片测试（Mock 数据层） |
| 运行命令 | `npm test` | `mvn clean test` |
| 用例总数 | 24 | 27 |

---

## 新增测试指南

### Node.js 后端

```javascript
const request = require('supertest');
const app = require('../src/app');
const { db } = require('../src/db');

beforeAll(async () => {
  await app.ready;
});

afterAll(() => {
  db.close();
});

describe('新接口测试', () => {
  it('应能正常访问', async () => {
    const response = await request(app).get('/api/your-endpoint');
    expect(response.status).toBe(200);
  });
});
```

### Java 后端

```java
@WebMvcTest(YourController.class)
class YourControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private YourRepository yourRepository;

    @Test
    void shouldReturn200() throws Exception {
        mockMvc.perform(get("/api/your-endpoint"))
               .andExpect(status().isOk());
    }
}
```

---

## 已知限制

- **Node.js 后端**：当前测试直接操作 SQLite 生产数据库文件，测试数据与真实数据共存。建议在 CI/CD 环境中配置独立的测试数据库文件。
- **Java 后端**：Controller 测试主要验证 HTTP 层和参数校验，复杂的业务逻辑和数据库事务行为建议补充 `@SpringBootTest` 级别的端到端测试。
