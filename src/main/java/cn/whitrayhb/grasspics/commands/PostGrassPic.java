package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.GrasspicsMain;
import cn.whitrayhb.grasspics.dataConfig.PluginConfig;
import cn.whitrayhb.grasspics.dataConfig.PluginData;
import cn.whitrayhb.grasspics.dataConfig.SimSoftSecureConfig;
import cn.whitrayhb.grasspics.utils.Cooler;
import cn.whitrayhb.grasspics.utils.ImageUtil;
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

import java.util.Objects;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

public class PostGrassPic extends JRawCommand {
    private static final Vector<Long> nextAreYou = new Vector<>();
    private static final ConcurrentHashMap<String, Image> imageCachePool = new ConcurrentHashMap<>();

    public PostGrassPic() {
        super(GrasspicsMain.INSTANCE, "post-grass-pic", "投草图", "草图投稿", "投张草图", "投稿草图");
        this.setDescription("草图投稿");
        this.setPrefixOptional(true);
        this.setUsage("(/)草图投稿  #草图投稿");
    }

    public static void postToPrivateChannel(CommandSender sender, Image image) {
        String SIMS_USER = SimSoftSecureConfig.INSTANCE.user.get();
        String SIMS_TOKEN = SimSoftSecureConfig.INSTANCE.token.get();

        GrasspicsMain.globalExecutorService.submit(() -> {
            if (sender.getSubject() == null) return;

            try {
                // Download Image
                String queryURL = Image.queryUrl(image);
                Request imageDownloadRequest = new Request.Builder().url(queryURL).get().build();
                Response imageDownloadResponse = GrasspicsMain.globalHttpClient.newCall(imageDownloadRequest).execute();
                if (imageDownloadResponse.body() == null) throw new Exception("Empty response body.");
                byte[] imageBytes = imageDownloadResponse.body().bytes();

                String type = ImageUtil.getImageExt(imageBytes);
                if (type.equals("unknown") || type.equals("gif")) {
                    sender.sendMessage("此图片类型不受支持!");
                    return;
                }

                // Post Image
                String postURL = "https://oss.grass.starxw.com/nlr/upload";

                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);
                builder.addFormDataPart("user", SIMS_USER);
                builder.addFormDataPart("token", SIMS_TOKEN);
                builder.addFormDataPart("qq", String.valueOf(Objects.requireNonNull(sender.getUser()).getId()));
                builder.addFormDataPart("file", "file", RequestBody.create(imageBytes));

                Request imagePostRequest = new Request.Builder().url(postURL).post(builder.build()).build();
                Response imagePostResponse = GrasspicsMain.globalHttpClient.newCall(imagePostRequest).execute();

                if (imagePostResponse.body() == null) throw new Exception("Empty response body.");
                String code = imagePostResponse.body().string();

                switch (code) {
                    case "200" -> sender.sendMessage("投稿成功, 正在等待审核。");
                    case "401" -> sender.sendMessage("鉴权信息无效, 请检查配置文件。");
                    case "403" -> sender.sendMessage("图片太大了, 投稿失败。");
                    case "503" -> sender.sendMessage("你已被草图服务封禁，投稿失败。");
                    default -> sender.sendMessage("服务器响应无效: " + code);
                }

                imageDownloadResponse.close();
            } catch (Exception ex) {
                sender.sendMessage("发生错误! 请到控制台获取详细信息: \n" + ex);
                ex.printStackTrace();
            }
        });
    }

    public static void postToPublicChannel(CommandSender sender, Image image) {
        GrasspicsMain.globalExecutorService.submit(() -> {
            if (sender.getSubject() == null) return;

            try {
                // Download Image
                String queryURL = Image.queryUrl(image);
                Request imageDownloadRequest = new Request.Builder().url(queryURL).get().build();
                Response imageDownloadResponse = GrasspicsMain.globalHttpClient.newCall(imageDownloadRequest).execute();
                if (imageDownloadResponse.body() == null) throw new Exception("Empty response body.");
                byte[] imageBytes = imageDownloadResponse.body().bytes();

                String type = ImageUtil.getImageExt(imageBytes);
                if (type.equals("unknown") || type.equals("gif")) {
                    sender.sendMessage("此图片类型不受支持!");
                    return;
                }

                // Post Image
                String postURL = "https://oss.grass.starxw.com/service/upload";

                MultipartBody.Builder builder = new MultipartBody.Builder();
                builder.setType(MultipartBody.FORM);
                builder.addFormDataPart("file", "file", RequestBody.create(imageBytes, MediaType.parse("image/png")));

                Request imagePostRequest = new Request.Builder().url(postURL).post(builder.build()).build();
                Response imagePostResponse = GrasspicsMain.globalHttpClient.newCall(imagePostRequest).execute();

                if (imagePostResponse.body() == null) throw new Exception("Empty response body.");
                int code = new JSONObject(imagePostResponse.body().string()).getInt("code");

                switch (code) {
                    case 200 -> sender.sendMessage("投稿成功, 正在等待审核。");
                    case 400 -> sender.sendMessage("图片格式无效!");
                    case 403 -> sender.sendMessage("图片太大，投稿失败。");
                    case 503 -> sender.sendMessage("机器人已被草图服务封禁，投稿失败。");
                    default -> sender.sendMessage("服务器响应无效: " + code);
                }
            } catch (Exception ex) {
                sender.sendMessage("发生错误! 请到控制台获取详细信息: \n" + ex);
                ex.printStackTrace();
            } finally {
                nextAreYou.remove(Objects.requireNonNull(sender.getUser()).getId());
            }
        });
    }

    public static void cacheImage(GroupMessageEvent e) {
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof Image image) {
                String id = e.getGroup().getId() + "-" + Objects.requireNonNull(e.getMessage().get(MessageSource.Key)).getIds()[0];
                imageCachePool.put(id, image);
                return;
            }
        }
    }

    public static void cacheImage(GroupMessagePostSendEvent e) {
        for (SingleMessage singleMessage : e.getMessage()) {
            if (singleMessage instanceof Image image) {
                try {
                    String id = e.getTarget().getId() + "-" + Objects.requireNonNull(e.getMessage().get(MessageSource.Key)).getIds()[0];
                    imageCachePool.put(id, image);
                } catch (Exception ignored) {
                }

                return;
            }
        }
    }

    @Override
    public void onCommand(@NotNull CommandContext context, @NotNull MessageChain args) {
        CommandSender sender = context.getSender();
        MessageChainBuilder builder = new MessageChainBuilder().append(new QuoteReply(context.getOriginalMessage()));

        if (sender instanceof ConsoleCommandSender || sender.getSubject() == null) {
            sender.sendMessage("请不要在控制台中运行该命令。");
            return;
        }

        User user = sender.getUser();
        long uid = Objects.requireNonNull(user).getId();

        if (Cooler.isLocked(uid)) {
            sender.sendMessage(builder.append("操作太快了，请稍后再试").build());
            return;
        }

        Cooler.lock(uid, PluginConfig.INSTANCE.postPictureLockTime.get());

        if (GrasspicsMain.shouldUsePublicPostingChannel()) {
            if (!PluginData.INSTANCE.savedQQ.get().contains(uid)) {
                PluginData.INSTANCE.savedQQ.get().add(uid);
                sender.sendMessage(builder.append("您第一次向公共投稿通道投稿，请认真阅读以下内容:\n\n" + GrasspicsMain.TEXT_RULES + "\n\n如果您投稿违规内容，机器人的 IP 可能会被封禁.").build());
            }
        } else {
            String SIMS_USER = SimSoftSecureConfig.INSTANCE.user.get();
            String SIMS_TOKEN = SimSoftSecureConfig.INSTANCE.token.get();

            if (SIMS_USER.isEmpty() || SIMS_TOKEN.isEmpty()) {
                sender.sendMessage(builder.append("对不起, 因为主人暂未填写 Simsoft user / token, 所以我无法提供投稿服务。").build());
                return;
            }
        }

        //命令中发图
        MessageChain originalMessage = context.getOriginalMessage();
        QuoteReply quote = (QuoteReply) originalMessage.stream().filter(m -> m instanceof QuoteReply).findFirst().orElse(null);
        if (quote != null) {
            String id = sender.getSubject().getId() + "-" + quote.getSource().getIds()[0];

            if (!imageCachePool.containsKey(id)) {
                sender.sendMessage(builder.append("该图片未在缓存中，请重新在聊群中发送!").build());
                return;
            }

            Image image = imageCachePool.get(id);
            if (image.getSize() > 2048000) {
                sender.sendMessage(builder.build());
                return;
            }

            if (image.getImageType() == ImageType.GIF) {
                sender.sendMessage(builder.append("您发送了不支持投稿的图片类型!").build());
                return;
            }

            if (GrasspicsMain.shouldUsePublicPostingChannel()) {
                postToPublicChannel(sender, image);
                return;
            }

            postToPrivateChannel(sender, image);
            return;
        }

        SingleMessage sm = args.stream().filter(msg -> msg instanceof Image).findFirst().orElse(null);
        if (sm != null) {
            Image image = (Image) sm;
            if (image.getSize() > 2048000) {
                sender.sendMessage(builder.append("图片太大了, 请压缩一下再投稿!").build());
                return;
            }

            if (image.getImageType() == ImageType.GIF) {
                sender.sendMessage(builder.append("您发送了不支持投稿的图片类型!").build());
                return;
            }

            if (GrasspicsMain.shouldUsePublicPostingChannel()) {
                postToPublicChannel(sender, image);
                return;
            }

            postToPrivateChannel(sender, image);
            return;
        }

        // 命令后发图
        sender.sendMessage(builder.append("请把要投稿的图片发送给我吧~").build());
        nextAreYou.add(uid);

        new MessageListener().getNextMessage(sender, message -> {
            if (!nextAreYou.contains(uid)) return;

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
            if (!GrasspicsMain.shouldUsePublicPostingChannel() && (SIMS_USER.isEmpty() || SIMS_TOKEN.isEmpty())) {
                sender.sendMessage("对不起, 因为主人暂未填写 Simsoft user / token, 所以我无法提供投稿服务。");
                return;
            }

            Image image = (Image) imageMessage;
            if (image.getSize() > 2048000) {
                nextAreYou.remove(uid);

                sender.sendMessage("图片太大了, 请压缩一下再投稿!");
                return;
            }

            if (image.getImageType() == ImageType.GIF) {
                nextAreYou.remove(uid);

                sender.sendMessage("您发送了不支持投稿的图片类型!");
                return;
            }

            nextAreYou.remove(uid);

            if (GrasspicsMain.shouldUsePublicPostingChannel()) {
                postToPublicChannel(sender, image);
                return;
            }

            postToPrivateChannel(sender, image);
        }, exception -> {
            if (!nextAreYou.contains(uid)) return;

            sender.sendMessage("还没想好要发什么嘛，想起来再来投稿吧!");
            nextAreYou.remove(uid);
        }, PluginConfig.INSTANCE.postPictureTimeout.get());
    }
}
