## ADDED Requirements

### Requirement: Upload personal photos
系统应允许用户将一个或多个图像文件上传到个人图库。

#### Scenario: Upload a single photo
- **WHEN** 用户选择一个有效图像文件并提交上传表单
- **THEN** 系统将文件存储到对象存储中并保存照片元数据

### Requirement: Validate upload file type
系统应仅接受常见图像文件格式，例如 JPG、PNG 和 GIF。

#### Scenario: Upload invalid file type
- **WHEN** 用户尝试上传非图像文件
- **THEN** 系统拒绝上传并显示明确的验证消息
