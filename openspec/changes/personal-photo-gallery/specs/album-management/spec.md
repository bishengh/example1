## ADDED Requirements

### Requirement: Create and manage albums
系统应允许用户创建、编辑和删除个人照片相册。

#### Scenario: Create a new album
- **WHEN** 用户输入相册名称并保存
- **THEN** 系统创建该相册并使其可用于照片分配

### Requirement: Assign photos to albums and tags
系统应允许用户将照片添加到或移出相册，并管理简单的文本标签。

#### Scenario: Add photo to album
- **WHEN** 用户选择一张照片并将其添加到相册
- **THEN** 系统将该照片与该相册关联，并在图库中反映该更改
