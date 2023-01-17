# 草图大全 Mirai 插件

一个 [Mirai Console](https://github.com/mamoe/mirai-console) 插件，可在 QQ 群聊中 接入 NLR 草图大全的服务

## Links：

- [草图官方网站](https://grass.nlrdev.top)
- [草图开发文档](https://docs.simsoft.top/?doc=grass-dev-doc)

## Commands：

- **命令名称**：草图 / 来张草图 / 生草 / grass-pic
  
  **命令功能**：来一张草图 
  
  **权限节点**：`cn.whitrayhb.grasspics:command.grass-pic`
- **命令名称**：草图信息 
  
  **命令功能**：获取当前草图服务器信息 
  
  **权限节点**：`cn.whitrayhb.grasspics:command.grass-pic-status`
  - 服务器图片总数
  - 待审核图片数
  - 调用次数
  - 图片总大小
- **命令名称**：草图投稿
  
  **权限节点**：`cn.whitrayhb.grasspics:command.post-grass-pic`

## FAQs:

- 如何在群内使用?
  - 赋予群员权限节点
  - 安装 Chat Command 插件
- 草图投稿是什么? 如何使用?
  - 目前 Bot 中草图投稿使用 NLR DevTeam 内部接口，需要进行账户鉴权，暂不对外开放。
