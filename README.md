# 草图大全 Mirai 插件

一个 [Mirai Console][mcl]插件，可在 QQ 群聊中 接入 NLR 草图大全的服务

## Links：

- [草图官方网站][gw]
- [草图开发文档](https://docs.simsoft.top/?doc=grass-dev-doc)

## Commands：

- **命令名称**：草图 / 来张草图 / 生草 / grass-pic
  
  **命令功能**：来一张草图 
  ![图片](https://imgcdn.simsoft.top/1673953098-53A45BD7-A8F1-4581-BAEE-EBB5A7619A86.jpeg)
  
  
  **权限节点**：`cn.whitrayhb.grasspics:command.grass-pic`
- **命令名称**：草图信息 
  
  **命令功能**：获取当前草图服务器信息 
  
  **权限节点**：`cn.whitrayhb.grasspics:command.grass-pic-status`
  - 是否正常提供服务
  - 服务器图片总数
  - 待审核图片数
  - 调用次数
  - 今日调用次数
  - 图片总大小
  - 今日图片流量

- **命令名称**：草图投稿
  
  **权限节点**：`cn.whitrayhb.grasspics:command.post-grass-pic`
  ![图片](https://imgcdn.simsoft.top/1673953355-2A5D48FE-0C24-46C5-B6B7-139169EFECF5.jpeg)
## FAQs:

- 如何在群内使用?
 1.下载[MCL][mcl]
 2.安装前置 [Chat Command](https://github.com/project-mirai/chat-command) 插件
 3.登陆Bot账号
 4.赋予群员权限
- 草图投稿是什么? 如何使用?
  - 目前 Bot 中草图投稿使用 NLR DevTeam 内部接口，需要进行账户鉴权，暂不对外开放。

[gw]:https://grass.nlrdev.top"草图的官方网站"

[mcl]:https://github.com/mamoe/mirai-console

|名称|指令|描述|权限节点|
|:---:|:---:|:---:|
|生草|草图，来张草图，生草，grass-pic |来一张草图|cn.whitrayhb.grasspics:command.grass-pic-status|
|投稿草图|投稿草图|向草图库投稿，需要经过审核|cn.whitrayhb.grasspics:command.post-grass-pic|
|草图信息|草图信息|获取草图信息|cn.whitrayhb.grasspics:command.grass-pic-status|
