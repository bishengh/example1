# Java 后端服务

基于 **Spring Boot 3.2 + Spring Data JPA + PostgreSQL** 实现的照片图库后端服务，与 Node.js 后端共用同一套前端，提供完全一致的 RESTful API。

## 技术栈

- **框架**：Spring Boot 3.2.12
- **数据访问**：Spring Data JPA (Hibernate)
- **数据库**：PostgreSQL 15
- **Web**：Spring MVC + Embedded Tomcat
- **Session**：Servlet HttpSession (Tomcat 内嵌会话管理)
- **测试**：JUnit 5 + Mockito + Spring MockMvc
- **构建工具**：Maven

## 项目结构

```
backend-java/
├── src/main/java/com/example/gallery/
│   ├── GalleryApplication.java                 # 启动类
│   ├── config/
│   │   ├── SessionConfig.java                  # 会话配置占位
│   │   └── WebConfig.java                      # 静态资源映射
│   ├── controller/
│   │   ├── AuthController.java                 # 认证接口
│   │   ├── PhotoController.java                # 照片接口
│   │   ├── AlbumController.java                # 相册接口
│   │   └── GlobalExceptionHandler.java         # 全局异常处理
│   ├── model/
│   │   ├── User.java                           # 用户实体
│   │   ├── Album.java                          # 相册实体
│   │   └── Photo.java                          # 照片实体
│   ├── repository/
│   │   ├── UserRepository.java                 # 用户数据访问
│   │   ├── AlbumRepository.java                # 相册数据访问
│   │   └── PhotoRepository.java                # 照片数据访问
│   ├── service/
│   │   ├── FileStorageService.java             # 文件存储接口
│   │   └── LocalFileStorageService.java        # 本地文件存储实现
│   └── initializer/
│       └── DataInitializer.java                # 默认数据初始化
├── src/test/java/com/example/gallery/
│   ├── GalleryApplicationTests.java            # 应用上下文加载测试
│   ├── controller/
│   │   ├── AuthControllerTest.java             # 认证接口测试 (7)
│   │   ├── PhotoControllerTest.java            # 照片接口测试 (9)
│   │   └── AlbumControllerTest.java            # 相册接口测试 (8)
│   └── service/
│       └── FileStorageServiceTest.java         # 文件存储测试 (2)
├── src/test/resources/
│   └── application-test.yml                    # 测试环境配置 (H2)
├── src/main/resources/
│   └── application.yml                         # 主配置文件
├── pom.xml                                      # Maven 配置
├── docker-compose.yml                           # PostgreSQL 开发环境
└── run.sh                                       # 快速启动脚本
```

## 快速开始

### 1. 环境要求

- JDK 17 或更高版本
- Maven 3.9+
- PostgreSQL 15（可选，也可用 docker-compose 启动）

### 2. 启动 PostgreSQL

```bash
# 使用项目自带的 docker-compose 启动 PostgreSQL
docker compose -f backend-java/docker-compose.yml up -d
```

### 3. 编译与运行

```bash
cd backend-java
mvn clean spring-boot:run
```

服务启动后，访问 http://localhost:8080

### 4. 运行测试

```bash
cd backend-java
mvn clean test
```

测试使用 H2 内存数据库，无需启动 PostgreSQL。

## 配置说明

配置位于 `src/main/resources/application.yml`，支持通过环境变量覆盖：

| 配置项 | 环境变量 | 默认值 |
|--------|---------|--------|
| 服务端口号 | `PORT` | `8080` |
| 数据库连接 URL | `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/gallery` |
| 数据库用户名 | `SPRING_DATASOURCE_USERNAME` | `gallery` |
| 数据库密码 | `SPRING_DATASOURCE_PASSWORD` | `password` |
| 照片存储路径 | `APP_STORAGE_PATH` | `./uploads` |

### 示例：自定义环境变量启动

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/mydb
export SPRING_DATASOURCE_USERNAME=admin
export SPRING_DATASOURCE_PASSWORD=secret
export APP_STORAGE_PATH=/data/gallery-uploads

cd backend-java
mvn spring-boot:run
```

## 数据模型

### 用户 (`User`)

```java
@Entity
@Table(name = "users")
public class User {
    private Long id;
    private String username;
    private String password;
}
```

### 相册 (`Album`)

```java
@Entity
public class Album {
    private Long id;
    private String name;
    private String description;
}
```

### 照片 (`Photo`)

```java
@Entity
public class Photo {
    private Long id;
    private String filename;
    private String title;
    private String description;
    private String tags;
    private Album album;
    private OffsetDateTime uploadedAt;
}
```

- `tags` 以逗号分隔字符串存储，API 层自动转换为数組
- `album` 为 `@ManyToOne` 关联，删除相册时会自动解除关联

## 默认账号

服务首次启动时，`DataInitializer` 会自动创建默认管理员账号：

- 用户名：`admin`
- 密码：`password`

> **生产环境警告**：请务必在首次登录后修改默认密码。

## 打包与部署

### Maven 打包

```bash
cd backend-java
mvn clean package
```

生成的可执行 JAR 位于 `target/personal-photo-gallery-0.1.0.jar`。

### 直接运行 JAR

```bash
java -jar target/personal-photo-gallery-0.1.0.jar
```

### 生产环境建议

1. **使用 PostgreSQL 生产实例**，不要使用 H2
2. **配置反向代理**（Nginx），并启用 HTTPS
3. **修改默认密码**，并考虑引入 bcrypt 密码哈希（当前版本为明文比对，待增强）
4. **持久化上传目录** (`APP_STORAGE_PATH`)，并定期备份
5. **限制 CORS**：修改 `WebConfig` 或添加 `@CrossOrigin` 域名白名单

## 与 Node.js 后端的差异

| 特性 | Node.js 后端 | Java 后端 |
|------|-------------|-----------|
| 数据库 | SQLite（零配置） | PostgreSQL（需部署） |
| 密码存储 | bcrypt 哈希 | 当前为明文（待升级） |
| 上传文件名 | `crypto.randomUUID()` | 原始文件名 |
| 文件存储 | `storage/` | `uploads/`（可配置） |
| 事务/并发 | 基础级别 | JPA 事务，更强并发支持 |
| 适用场景 | 个人单机、快速体验 | 生产环境、多用户扩展 |

## 测试覆盖

当前 Java 后端包含 **27 个单元测试用例**：

- **AuthControllerTest** (7)：登录成功/失败、空凭据、未登录访问、登出
- **PhotoControllerTest** (9)：列表、详情、上传、格式校验、更新、404
- **AlbumControllerTest** (8)：列表、创建、更新、删除、解关联、权限校验
- **FileStorageServiceTest** (2)：文件存储、目录创建
- **GalleryApplicationTests** (1)：Spring 上下文加载
