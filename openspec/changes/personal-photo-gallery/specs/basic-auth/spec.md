## ADDED Requirements

### Requirement: Basic personal login
系统应提供简单登录机制，使个人用户能够验证身份并访问其图库。

#### Scenario: Successful login
- **WHEN** 用户在登录页面输入有效凭据
- **THEN** 系统验证用户身份并重定向到个人图库

### Requirement: Access control for personal gallery
系统应仅允许经过身份验证的用户访问图库和管理页面。

#### Scenario: Access gallery without login
- **WHEN** 未经过身份验证的用户尝试访问图库页面
- **THEN** 系统将用户重定向到登录页面
