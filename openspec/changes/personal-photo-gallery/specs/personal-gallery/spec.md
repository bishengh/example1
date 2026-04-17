## ADDED Requirements

### Requirement: Personal gallery browsing
系统应提供个人照片图库界面，以简单的列表或网格布局显示已上传的照片。

#### Scenario: View personal photo gallery
- **WHEN** 经过身份验证的用户访问图库页面
- **THEN** 系统显示该用户已上传的照片，包含缩略图、标题和上传日期

### Requirement: Photo detail view
系统应为每张照片提供详情视图，包含完整图像、元数据和管理操作。

#### Scenario: Open photo details
- **WHEN** 用户在图库中点击照片
- **THEN** 系统显示照片详情页，带有图像预览、标题、描述、上传日期以及相册/标签信息
