package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.GrassPictures;
import cn.whitrayhb.grasspics.data.PluginConfig;
import cn.whitrayhb.grasspics.data.PluginData;
import cn.whitrayhb.grasspics.data.SimSoftSecureConfig;
import cn.whitrayhb.grasspics.utils.ImageUtil;
import cn.whitrayhb.grasspics.utils.Locker;
import cn.whitrayhb.grasspics.utils.MessageListener;
import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent;
import net.mamoe.mirai.internal.deps.okhttp3.*;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class PostGrassPic extends JRawCommand {
    private static final Vector<Long> nextAreYou = new Vector<>();
    private static final ConcurrentHashMap<String, Image> imageCachePool = new ConcurrentHashMap<>();

    public PostGrassPic() {
        super(GrassPictures.INSTANCE, "post-grass-pic", "投草图", "草图投稿", "投张草图", "投稿草图");
        this.setDescription("草图投稿");
        this.setPrefixOptional(true);
        this.setUsage("(/)草图投稿  #草图投稿");
    }

    /**
     * 向内部投稿通道投稿 (需要鉴权)
     *
     * @param sender     命令发送者
     * @param imageBytes 图片数据
     * @see #onCommand(CommandContext, MessageChain)
     */
    public static void postToPrivateChannel(CommandSender sender, byte[] imageBytes) {
        String SIMS_USER = SimSoftSecureConfig.INSTANCE.user.get();
        String SIMS_TOKEN = SimSoftSecureConfig.INSTANCE.token.get();

        GrassPictures.globalExecutorService.submit(() -> {
            if (sender.getSubject() == null) {
                return;
            }

            try {
                // Post Image
                String postURL = "https://oss.grass.starxw.com/nlr/upload";

                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);
                builder.addFormDataPart("user", SIMS_USER);
                builder.addFormDataPart("token", SIMS_TOKEN);
                builder.addFormDataPart("qq", String.valueOf(Objects.requireNonNull(sender.getUser()).getId()));
                builder.addFormDataPart("file", "file", RequestBody.create(imageBytes));

                Request imagePostRequest = new Request.Builder().url(postURL).post(builder.build()).build();
                String code;
                try (Response imagePostResponse = GrassPictures.globalHttpClient.newCall(imagePostRequest).execute()) {
                    if (imagePostResponse.body() == null) {
                        throw new Exception("Empty response body.");
                    }

                    code = imagePostResponse.body().string();
                }

                switch (code) {
                    case "200" -> sender.sendMessage("投稿成功, 正在等待审核。");
                    case "400" -> sender.sendMessage("操作太快了，机器人已被限流，请稍后再试！");
                    case "401" -> sender.sendMessage("鉴权信息无效, 请检查配置文件。");
                    case "403" -> sender.sendMessage("图片太大了, 投稿失败。");
                    case "503" -> sender.sendMessage("你已被草图服务封禁，投稿失败。");
                    default -> sender.sendMessage("服务器响应无效：" + code);
                }
            } catch (Exception ex) {
                sender.sendMessage("发生错误！请到控制台获取详细信息：\n" + ex);
                ex.printStackTrace();
            }
        });
    }

    /**
     * 向公共投稿通道投稿
     *
     * @param sender     命令发送者
     * @param imageBytes 图片数据
     * @see #onCommand(CommandContext, MessageChain)
     */
    public static void postToPublicChannel(CommandSender sender, byte[] imageBytes) {
        GrassPictures.globalExecutorService.submit(() -> {
            if (sender.getSubject() == null) {
                return;
            }

            try {
                // Post Image
                String token = PluginConfig.INSTANCE.NLRPassToken.get();
                if (token.isEmpty()) {
                    sender.sendMessage("由于主人未填写 NLR Pass Token，因此我无法为您提供投稿服务，请见谅。");
                    return;
                }

                String postURL = GrassPictures.PUBLIC_POST_URL + "?token=" + token;

                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);
                builder.addFormDataPart("file", "file", RequestBody.create(imageBytes, MediaType.parse("image/png")));

                Request imagePostRequest = new Request.Builder().url(postURL).post(builder.build()).build();
                int code;
                try (Response imagePostResponse = GrassPictures.globalHttpClient.newCall(imagePostRequest).execute()) {
                    if (imagePostResponse.body() == null) {
                        throw new Exception("Empty response body.");
                    }

                    code = new JSONObject(imagePostResponse.body().string()).getInt("code");
                }

                switch (code) {
                    case 200 -> sender.sendMessage("投稿成功, 正在等待审核。");
                    case 400 -> sender.sendMessage("操作太快了，机器人已被限流，请稍后再试！");
                    case 401 -> sender.sendMessage("NLR Pass Token 无效，请通知机器人所有者检查。");
                    case 403 -> sender.sendMessage("机器人已被草图服务封禁，投稿失败。");
                    case 1000 -> sender.sendMessage("图片格式无效！");
                    case 1001 -> sender.sendMessage("图片太大，投稿失败。");
                    default -> sender.sendMessage("服务器响应无效：" + code);
                }
            } catch (Exception ex) {
                sender.sendMessage("发生错误！请到控制台获取详细信息：\n" + ex);
                ex.printStackTrace();
            } finally {
                nextAreYou.remove(Objects.requireNonNull(sender.getUser()).getId());
            }
        });
    }

    /**
     * 缓存聊群中的图片
     *
     * @param e 事件
     * @see GrassPictures#onEnable()
     */
    public static void cacheImage(GroupMessageEvent e) {
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof Image image) {
                String id = e.getGroup().getId() + "-" + Objects.requireNonNull(e.getMessage().get(MessageSource.Key)).getIds()[0];
                imageCachePool.put(id, image);
                return;
            }
        }
    }

    /**
     * 缓存聊群中的图片
     *
     * @param e 事件
     * @see GrassPictures#onEnable()
     */
    public static void cacheImage(GroupMessagePostSendEvent e) {
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof Image image) {
                try {
                    String id = e.getTarget().getId() + "-" + Objects.requireNonNull(e.getMessage().get(MessageSource.Key)).getIds()[0];
                    imageCachePool.put(id, image);
                } catch (Exception ignored) {
                    // Do nothing here
                }

                return;
            }
        }
    }

    /**
     * 下载并自动选择投稿通道上传图片
     *
     * @param image   等待下载的 Mirai 图片
     * @param context 上下文
     */
    public static void postImage(Image image, CommandContext context) {
        CommandSender sender = context.getSender();
        byte[] imageBytes;
        try {
            // Download Image
            String queryURL = Image.queryUrl(image);
            Request imageDownloadRequest = new Request.Builder().url(queryURL).get().build();
            try (Response imageDownloadResponse = GrassPictures.globalHttpClient.newCall(imageDownloadRequest).execute()) {
                if (imageDownloadResponse.body() == null) {
                    throw new IOException("Empty response body.");
                }

                imageBytes = imageDownloadResponse.body().bytes();
            }

            String type = ImageUtil.getImageExt(imageBytes);
            if (type.equals("unknown") || type.equals("gif")) {
                sender.sendMessage(GrassPictures.wrap(context, "您发送了不支持投稿的图片类型！"));
                return;
            }
        } catch (IOException e) {
            sender.sendMessage(GrassPictures.wrap(context, "机器人下载图片失败！"));
            return;
        }

        if (GrassPictures.shouldUsePublicPostingChannel()) {
            postToPublicChannel(sender, imageBytes);
            return;
        }

        postToPrivateChannel(sender, imageBytes);
    }

    /**
     * 触发命令
     *
     * @param context 命令上下文，用于获取源消息
     * @param args    预期: [] / [Image]
     */
    @Override
    public void onCommand(@NotNull CommandContext context, @NotNull MessageChain args) {
        CommandSender sender = context.getSender();
        if (sender instanceof ConsoleCommandSender || sender.getSubject() == null) {
            sender.sendMessage("请不要在控制台中运行该命令！");
            return;
        }

        User user = sender.getUser();
        long uid = Objects.requireNonNull(user).getId();

        if (Locker.isLocked(uid)) {
            sender.sendMessage(GrassPictures.wrap(context, "操作太快了，请稍后再试！"));
            return;
        }

        Locker.lock(uid, PluginConfig.INSTANCE.postPictureLockTime.get());

        if (GrassPictures.shouldUsePublicPostingChannel()) {
            if (!PluginData.INSTANCE.savedQQ.get().contains(uid)) {
                PluginData.INSTANCE.savedQQ.get().add(uid);
                sender.sendMessage(GrassPictures.wrap(context, "这是您第一次向公共投稿通道投稿，请认真阅读以下内容：\n\n" +
                        GrassPictures.TEXT_RULES +
                        "\n\n如果您投稿违规内容，则机器人的 IP 可能会被封禁。\n" +
                        "在您熟读以上内容后，请再次发送投稿命令进行投稿。"));
                return;
            }
        } else {
            String SIMS_USER = SimSoftSecureConfig.INSTANCE.user.get();
            String SIMS_TOKEN = SimSoftSecureConfig.INSTANCE.token.get();

            if (SIMS_USER.isEmpty() || SIMS_TOKEN.isEmpty()) {
                sender.sendMessage(GrassPictures.wrap(context, "对不起, 因为主人暂未填写 Simsv user / token, 所以我无法提供投稿服务。"));
                return;
            }
        }

        // 回复图片
        MessageChain originalMessage = context.getOriginalMessage();
        QuoteReply quote = (QuoteReply) originalMessage.stream().filter(m -> m instanceof QuoteReply).findFirst().orElse(null);
        if (quote != null && PluginConfig.INSTANCE.isQuotePostEnabled.get()) {
            // Disallow replying others' messages
            if (quote.getSource().getFromId() != sender.getUser().getId()) {
                sender.sendMessage(GrassPictures.wrap(context, "为减少打扰，现在已无法通过回复别人的消息来投稿草图。"));
                return;
            }

            String id = sender.getSubject().getId() + "-" + quote.getSource().getIds()[0];
            Image image = imageCachePool.getOrDefault(id, null);
            if (image == null) {
                sender.sendMessage(GrassPictures.wrap(context, "该图片未在缓存中，请重新在聊群中发送！"));
                return;
            }

            if (image.getSize() > 2048000) {
                sender.sendMessage(GrassPictures.wrap(context, "图片太大了，请压缩一下再投稿！"));
                return;
            }

            if (image.getImageType() == ImageType.GIF) {
                sender.sendMessage(GrassPictures.wrap(context, "您发送了不支持投稿的图片类型！"));
                return;
            }

            postImage(image, context);
            return;
        }

        // 在命令的同一行发图
        SingleMessage sm = args.stream().filter(msg -> msg instanceof Image).findFirst().orElse(null);
        if (sm != null) {
            Image image = (Image) sm;
            if (image.getSize() > 2048000) {
                sender.sendMessage(GrassPictures.wrap(context, "图片太大了, 请压缩一下再投稿！"));
                return;
            }

            if (image.getImageType() == ImageType.GIF) {
                sender.sendMessage(GrassPictures.wrap(context, "您发送了不支持投稿的图片类型！"));
                return;
            }

            postImage(image, context);
            return;
        }

        // 命令后发图
        sender.sendMessage(GrassPictures.wrap(context, "请把要投稿的图片发送给我吧~"));
        nextAreYou.add(uid);

        new MessageListener().getNextMessage(sender, message -> {
            if (!nextAreYou.contains(uid)) {
                return;
            }

            String content = message.contentToString();
            if (content.equals("取消") || content.equals("cancel")) {
                sender.sendMessage("您已取消投稿。");
                return;
            }

            SingleMessage imageMessage = message.stream().filter(msg -> msg instanceof Image).findFirst().orElse(null);
            if (imageMessage == null) {
                sender.sendMessage("您发送的不是图片哦, 已经取消投稿。");
                nextAreYou.remove(uid);
                return;
            }

            String SIMS_USER = SimSoftSecureConfig.INSTANCE.user.get();
            String SIMS_TOKEN = SimSoftSecureConfig.INSTANCE.token.get();
            if (!GrassPictures.shouldUsePublicPostingChannel() && (SIMS_USER.isEmpty() || SIMS_TOKEN.isEmpty())) {
                sender.sendMessage("对不起, 因为主人暂未填写 Simsv user / token, 所以我无法提供投稿服务。");
                return;
            }

            Image image = (Image) imageMessage;
            if (image.getSize() > 2048000) {
                nextAreYou.remove(uid);

                sender.sendMessage("图片太大了, 请压缩一下再投稿！");
                return;
            }

            if (image.getImageType() == ImageType.GIF) {
                nextAreYou.remove(uid);

                sender.sendMessage("您发送了不支持投稿的图片类型！");
                return;
            }

            nextAreYou.remove(uid);

            postImage(image, context);
        }, exception -> {
            if (!nextAreYou.contains(uid)) {
                return;
            }

            sender.sendMessage("还没想好要发什么嘛，想起来再来投稿吧！");
            nextAreYou.remove(uid);
        }, PluginConfig.INSTANCE.postPictureTimeout.get());
    }
}
