<h1 align=center><img src="https://asset.simsoft.top/products/grass/icon.webp" width="120px" height="120px"><br>èå¾å¤§å¨</h1>
<p align=center><b>ð¤ Mirai Console Plugin ð¤</b></p>

<p align=center>ä¸ä¸ª Mirai Console æä»¶ï¼å¯å¨ QQ ç¾¤èä¸­ æ¥å¥ NLR èå¾å¤§å¨çæå¡</p>

## Terms:

ä½¿ç¨æä»¶åï¼è¯·éè¯»[ãèå¾æå¡æ¡æ¬¾ã](https://grass.nlrdev.top/tos)ã

## Linksï¼

>[èå¾å®æ¹ç½ç«](https://grass.nlrdev.top)

>[èå¾å¼åææ¡£](https://docs.simsoft.top/?doc=grass-dev-doc)

>[Mirai Forum ä»ç»å¸](https://mirai.mamoe.net/topic/1965/grasspictures-éæºè·åçèæä»¶)


## Commandsï¼

|åç§°|æä»¤|æä»¤åè½|æéèç¹|
|--|--|--|--|
|**çè**|<li>æ¥å¼ èå¾</li><li>çè</li><li>grass-pic</li>|æ¥ä¸å¼ èå¾|`cn.whitrayhb.grasspics:command.grass-pic`|
|**èå¾ä¿¡æ¯**|<li>èå¾ä¿¡æ¯</li><li>grass-pic-status</li>|è·åèå¾å½åç¶æ|`cn.whitrayhb.grasspics:command.grass-pic-status`|
|**æç¨¿èå¾**|<li>èå¾æç¨¿</li><li>æç¨¿èå¾</li><li>post-grass-pic</li>|åèå¾åºæç¨¿|`cn.whitrayhb.grasspics:command.post-grass-pic`|

<details>
  <summary>ææé¢è§</summary>
  <img src="https://imgcdn.simsoft.top/1674283139-BE788259-842F-4583-A744-E5D786D62653.jpeg" width="300px">
  <img src="https://imgcdn.simsoft.top/1673953098-53A45BD7-A8F1-4581-BAEE-EBB5A7619A86.jpeg" width="300px">
  <img src="https://imgcdn.simsoft.top/1673953355-2A5D48FE-0C24-46C5-B6B7-139169EFECF5.jpeg" width="300px">
</details>

## Configsï¼
æ¬æä»¶æä¾ä¸äºéé¡¹ä¾æ¨æéçµæ´»éç½®ï¼éç½®æä»¶ä½äº `config/cn.whitrayhb.grasspics/Config.yml` åã

**å¯¹éç½®åå®¹çè¯´æå¦ä¸ï¼**

åç§°|è¯´æ|é»è®¤
|--|--|--|
 `getPictureLockTime`|æ¯ä½ç¾¤åæ§è¡ `çè` æä»¤çå·å´æ¶é´ï¼å¯ç¨äºé²æ­¢å·å±ï¼è®¾ç½® <0 æ¶ä¸è¿è¡å·å´|30000
 `fetchPictureTimeout`|ä»èå¾æå¡æ¥å£ä¸è½½å¾çæ¶çè¶æ¶æ¶é´ï¼ä¸æ¨èè®¾ç½®è¿ä½çæ°å­|10000
 `postPictureLockTime`|æ¯ä½ç¾¤åæ§è¡ `æç¨¿èå¾` æä»¤çå·å´æ¶é´ï¼æ¨èè®¾ç½®å¨ `10000` ä»¥ä¸|10000
 `postPictureTimeout`|ä» QQ æå¡å¨ä¸­ä¸è½½ç¨æ·æç¨¿å¾çä¸ä¸ä¼ å¾ççè¶æ¶æ¶é´ï¼ä¸æ¨èè®¾ç½®è¿ä½çæ°å­|30000

Tip: éç½®ä¸­åºç°çæ¶é´åä½åä¸º `ms` (æ¯«ç§) èä¸æ¯ `s` (ç§)


## FAQs

- **å¦ä½å¼å§ä½¿ç¨æ¬æä»¶?**
  
  1ï¼ä¸è½½å¹¶éç½® [Mirai Console Loader](//github.com/iTXTech/mirai-console-loader) ï¼è¯·**ä½¿ç¨ Java 17** è¿è¡æ¬æä»¶
  
  2ï¼å®è£åç½® [Chat Command](//github.com/project-mirai/chat-command) æä»¶
  
  3ï¼å¨ [Releases](//github.com/NLR-DevTeam/GrassPictures/releases) é¡µé¢ä¸è½½ææ°ç `.jar` æä»¶ï¼æ¾å¥ MCL ä¸ `plugins` ç®å½ä¸
  
  4ï¼æäºæåç¸åºçæéèç¹ [æéèç¹è¯´æææ¡£](https://docs.mirai.mamoe.net/console/Permissions.html)
  
  5ï¼å¼å§çèå§ï¼

- **èå¾æç¨¿æ¯ä»ä¹? å¦ä½ä½¿ç¨?**
  
  ç®å Mirai Console æä»¶ä¸­æç¨¿åè½å·²å¼æ¾å¬ä¼æç¨¿ééï¼å¶æç¨¿è¦æ±ä¸ä¸»ç«ç¸åï¼è¯·ä¸è¦æç¨¿è¿è§æçæ°´å¾çãå¤æ¬¡æç¨¿è¿è§å¾ççç¨æ· IP å¯è½ä¼è¢«**å°ç¦**ã
  
  ä½ å¯ä»¥åå¾ [èå¾å®æ¹ç½ç«](//grass.nlrdev.top/) è¿è¡æç¨¿ï¼ææ¯ä½¿ç¨ `èå¾æç¨¿` å½ä»¤è¿è¡æç¨¿

- **ä¸ºä»ä¹æè¾å¥æä»¤æ²¡ååºï¼**
  
  æ¨å¯è½æ²¡æç»äºç¸å¯¹åºçç¾¤æéï¼å¦æåºç°æ¥éï¼è¯·åæä»¬åé¦ã

- **éç½®æä»¶ä¸­ç `user` å `token` æ¯å¹²åçå¢ï¼**

  éç½®æä»¶ `SimsoftSecure.yml` ä¸­ç `user` å `token` ç¨äºåé¨æç¨¿ééä½¿ç¨ï¼æ¨å¯å¿½ç¥æ­¤å¤çéç½®å¹¶ä½¿ç¨æä»¬å¼æ¾çæç¨¿ééã
___
æ¨å¯å å¥æä»¬ç [QQç¾¤](https://qm.qq.com/cgi-bin/qm/qr?k=3fydvbI64F7r0Tz2Y5BTWfJi-irnBnSz&authKey=ib%2FY0l5RwzWu2X5cDRK%2FB2swvZotR7f68BpJWLy5TuT1vRQGjya%2FT36dgV1xn4fs&noverify=0&group_code=182850795) äºè§£æ´å¤

___
