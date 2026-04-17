## ADDED Requirements

### Requirement: Java backend implementation
系统应将后端 API 服务实现迁移到 Java，保持现有 API 合约、认证逻辑和照片管理行为不变。

#### Scenario: Java 后端返回照片列表
- **WHEN** 经过身份验证的用户访问照片列表接口
- **THEN** Java 后端返回与原系统相同格式的照片数组，包括 URL、标题、描述、标签和上传时间

### Requirement: Java 后端支持会话认证
系统应在 Java 后端实现会话或令牌机制，使用户登录后能够访问受保护接口。

#### Scenario: Java 后端验证登录状态
- **WHEN** 用户发送带有有效会话的请求
- **THEN** Java 后端允许访问受保护资源
