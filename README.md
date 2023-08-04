<h1 align=center><img src="https://asset.simsoft.top/products/grass/icon.webp" width="120px" height="120px"><br>草图大全</h1>
<p align=center><b>🤖 Mirai Console Plugin 🤖</b></p>

<p align=center>一个 Mirai Console 插件，可在 QQ 群聊中接入 NLR 草图大全的服务</p>
<p align=center>
  <a href="https://github.com/NLR-DevTeam/GrassPictures/tree/master/src/main"><img src="https://img.shields.io/codefactor/grade/github/NLR-DevTeam/GrassPictures?label=Code%20Quality"></a>
  <a href="https://github.com/NLR-DevTeam/GrassPictures/tree/master/src/main"><img src="https://img.shields.io/github/languages/code-size/NLR-DevTeam/GrassPictures?label=Code%20Size"></a>
  <a href="https://github.com/NLR-DevTeam/GrassPictures/releases/"><img src="https://img.shields.io/github/v/release/NLR-DevTeam/GrassPictures?label=Latest%20Release"></a>
  <a href="https://github.com/NLR-DevTeam/GrassPictures/stargazers"><img src="https://img.shields.io/github/stars/NLR-DevTeam/GrassPictures?label=Stars"></a>
</p>

## 服务条款

使用本插件即代表您已阅读并同意[草图服务条款](https://grass.nlrdev.top/tos)。

## 相关链接

> [草图官方网站](https://grass.nlrdev.top)

> [草图开发文档](https://docs.simsoft.top/?doc=grass-dev-doc)

> [Mirai Forum 介绍帖](https://mirai.mamoe.net/topic/1965/grasspictures-随机获取生草插件)

## 命令列表

|名称|指令|指令功能|权限节点|
|--|--|--|--|
|**生草**|<li>来张草图</li><li>生草</li><li>grass-pic</li>|来一张草图|`cn.whitrayhb.grasspics:command.grass-pic`|
|**草图信息**|<li>草图信息</li><li>grass-pic-status</li>|获取草图当前状态|`cn.whitrayhb.grasspics:command.grass-pic-status`|
|**投稿草图**|<li>草图投稿</li><li>投稿草图</li><li>post-grass-pic</li>|向草图库投稿|`cn.whitrayhb.grasspics:command.post-grass-pic`|

<details>
  <summary>效果预览</summary>
  <p align=center>
    <img src="https://imgcdn.simsoft.top/1674283139-BE788259-842F-4583-A744-E5D786D62653.jpeg" width="300px">
    <img src="https://imgcdn.simsoft.top/1673953098-53A45BD7-A8F1-4581-BAEE-EBB5A7619A86.jpeg" width="300px">
    <img src="https://imgcdn.simsoft.top/1673953355-2A5D48FE-0C24-46C5-B6B7-139169EFECF5.jpeg" width="300px">
  </p>
</details>

## 配置文件

本插件提供一些选项供您按需灵活配置，配置文件位于 `config/cn.whitrayhb.grasspics/Config.yml` 内。

**对配置内容的说明如下：**

|名称|说明|默认值|
|--|--|--|
|`getPictureLockTime`|每位群友执行 `生草` 指令的冷却时间，可用于防止刷屏，设置 `<= 0` 时不进行冷却|30000|
|`fetchPictureTimeout`|从草图服务接口下载图片时的超时时间，不推荐设置过低的数字|10000|
|`postPictureLockTime`|每位群友执行 `投稿草图` 指令的冷却时间，推荐设置在 `10000` 以上|10000|
|`postPictureTimeout`|从 QQ 服务器中下载用户投稿图片与上传图片的超时时间，不推荐设置过低的数字|30000|
|`isQuotePostEnabled`|是否启用回复消息投稿，默认启用|true|
|`NLRPassToken`|在 NLR Pass 管理页获取的接口密钥，可在 [NLR Pass 管理页](https://pass.nlrdev.top/) 进行申请|空|

- 配置中出现的时间单位均为 `ms` (毫秒) 而不是 `s` (秒)
- 修改配置文件后，您可以在控制台使用 `/grass-pic reload` 命令重载插件
## 常见问题

- **如何开始使用本插件?**
  
  1）下载并配置 [Mirai Console Loader](//github.com/iTXTech/mirai-console-loader) ，请**使用 Java 17** 运行本插件；
  
  2）安装前置 [Chat Command](//github.com/project-mirai/chat-command) 插件；
  
  3）在 [Releases](//github.com/NLR-DevTeam/GrassPictures/releases) 页面下载最新的 `.jar` 文件，放入 MCL
根目录下的 `plugins` 目录中；
  
  4）参阅[权限节点说明文档](https://docs.mirai.mamoe.net/console/Permissions.html)，授予成员相应的权限节点；
  
  5）开始生草吧！
- **草图投稿是什么? 如何使用?**
  
  目前 Mirai Console 插件中投稿功能已开放公众投稿通道，其投稿要求请参阅服务条款，请不要投稿违规图片（您的 Bot
的用户进行的投稿行为也将被视为您进行的投稿）。**投稿违规图片的用户可能会被加入黑名单**。
  
  如需向我们投稿图片，你可以前往 [草图官方网站](//grass.nlrdev.top/) 进行投稿，或是在群聊中使用 `草图投稿` 命令进行投稿。
- **为什么我输入指令没反应？**
  
  您可能没有给予相对应的群权限；如果出现报错，请向我们反馈。
- **`SimsoftSecure.yml` 配置文件有何作用？**
  
  配置文件 `SimsoftSecure.yml` 用于内部投稿接口鉴权，您只需忽略此处的配置并使用我们开放的公共投稿通道。

## 注意事项

请使用 **Java 17** 运行 MCL 并加载此插件，使用低版本的 Java 将导致插件无法正常加载。

请**仅在您信任的群聊中**开启草图投稿权限。

若您使用插件时，其他用户恶意利用 Bot 投稿违规图片导致您被加入黑名单，请加入页面底部的社群并提供相关的聊天记录进行申诉，我们将视情况进行解封。

## 联系我们

您可加入我们的 [QQ社群](https://join.nlrdev.top) 进行提问或了解更多。
