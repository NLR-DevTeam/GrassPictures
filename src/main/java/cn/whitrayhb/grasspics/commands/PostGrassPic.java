package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.GrasspicsMain;
import cn.whitrayhb.grasspics.JavaConfig;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.internal.deps.okhttp3.*;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.ImageType;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.SingleMessage;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

public class PostGrassPic extends JRawCommand {
    public static final PostGrassPic INSTANCE = new PostGrassPic();
    private static final ArrayList<User> nextAreYou = new ArrayList<>();
    private static final HashMap<User, Thread> listeningThreads = new HashMap<>();

    @SuppressWarnings("deprecation")
    private PostGrassPic() {
        super(GrasspicsMain.INSTANCE, "post-grass-pic", "投草图", "草图投稿", "投张草图", "投稿草图");
        this.setDescription("草图投稿");
        this.setPrefixOptional(true);
        this.setUsage("(/)草图投稿  #草图投稿");

        Listener<GroupMessageEvent> listener = GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, m -> {
            if (!nextAreYou.contains(m.getSender())) return;
            SingleMessage message = m.getMessage().stream().filter(msg -> msg instanceof Image).findFirst().orElse(null);

            User user = m.getSender();

            if (message == null) {
                m.getGroup().sendMessage("您发送的不是图片哦, 已经取消投稿.");
                nextAreYou.remove(m.getSender());
                listeningThreads.remove(m.getSender());

                return;
            }

            String SIMS_USER = JavaConfig.INSTANCE.user.get();
            String SIMS_TOKEN = JavaConfig.INSTANCE.token.get();
            if (SIMS_USER.isEmpty() || SIMS_TOKEN.isEmpty()) {
                m.getGroup().sendMessage("对不起, 因为主人暂未填写 Simsoft user / token, 所以我无法提供投稿服务.");
                return;
            }

            Image image = (Image) message;
            if (image.getSize() > 2048000) {
                nextAreYou.remove(user);
                if (listeningThreads.containsKey(user)) {
                    listeningThreads.get(user).interrupt();
                    listeningThreads.remove(user);
                }

                m.getGroup().sendMessage("图片太大了, 请压缩一下再投稿!");
                return;
            }

            if (image.getImageType() == ImageType.GIF || image.getImageType() == ImageType.UNKNOWN) {
                nextAreYou.remove(user);
                if (listeningThreads.containsKey(user)) {
                    listeningThreads.get(user).interrupt();
                    listeningThreads.remove(user);
                }

                m.getGroup().sendMessage("您发送了不支持投稿的图片类型!");
                return;
            }

            nextAreYou.remove(user);
            if (listeningThreads.containsKey(user)) {
                listeningThreads.get(user).interrupt();
                listeningThreads.remove(user);
            }

            // Open a thread and a watcher
            Thread postThread = new Thread(() -> {
                try {
                    OkHttpClient client = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();

                    // Download Image
                    String queryURL = Image.queryUrl(image);
                    Request imageDownloadRequest = new Request.Builder().url(queryURL).get().build();
                    Response imageDownloadResponse = client.newCall(imageDownloadRequest).execute();
                    if (imageDownloadResponse.body() == null) throw new Exception("Empty response body.");
                    byte[] imageBytes = imageDownloadResponse.body().bytes();

                    // Post Image
                    String postURL = "https://i.simsoft.top/grass/nlr/upload";

                    MultipartBody.Builder builder = new MultipartBody.Builder();
                    builder.setType(MultipartBody.FORM);
                    builder.addFormDataPart("user", SIMS_USER);
                    builder.addFormDataPart("token", SIMS_TOKEN);
                    builder.addFormDataPart("qq", String.valueOf(m.getSender().getId()));
                    builder.addFormDataPart("file", "file", RequestBody.create(imageBytes));

                    Request imagePostRequest = new Request.Builder().url(postURL).post(builder.build()).build();
                    Response imagePostResponse = client.newCall(imagePostRequest).execute();

                    try {
                        if (imagePostResponse.body() == null) throw new Exception("Empty response body.");
                        String code = imagePostResponse.body().string();

                        switch (code) {
                            case "200":
                                m.getGroup().sendMessage("投稿成功, 正在等待审核。");
                                break;
                            case "401":
                                m.getGroup().sendMessage("鉴权信息无效, 请检查配置文件。");
                                break;
                            case "403":
                                m.getGroup().sendMessage("图片太大了, 投稿失败。");
                                break;
                            default:
                                m.getGroup().sendMessage("服务器响应无效: " + code);
                                break;
                        }
                    } catch (Exception ex) {
                        m.getGroup().sendMessage("投稿时发生错误, 请到控制台查看详细信息:\n" + ex);
                        ex.printStackTrace();
                    }

                    imageDownloadResponse.close();
                } catch (Exception ex) {
                    m.getGroup().sendMessage("发生错误! 请到控制台获取详细信息: \n" + ex);
                    ex.printStackTrace();
                }
            });
            postThread.start();

            new Thread(() -> {
                try {
                    Thread.sleep(10 * 1000);
                } catch (InterruptedException e) {
                    return;
                }

                if (!postThread.isAlive()) return;
                postThread.stop();

                m.getGroup().sendMessage("投稿超时, 请稍后重试!");

                nextAreYou.remove(user);
                if (listeningThreads.containsKey(user)) {
                    listeningThreads.get(user).interrupt();
                    listeningThreads.remove(user);
                }
            }).start();
        });

        listener.start();
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull MessageChain args) {
        if (sender.getSubject() == null) {
            sender.sendMessage("请不要在控制台中运行该命令");
            return;
        }

        String SIMS_USER = JavaConfig.INSTANCE.user.get();
        String SIMS_TOKEN = JavaConfig.INSTANCE.token.get();

        if (SIMS_USER.isEmpty() || SIMS_TOKEN.isEmpty()) {
            sender.sendMessage("对不起, 因为主人暂未填写 Simsoft user / token, 所以我无法提供投稿服务.");
            return;
        }

        User user = sender.getUser();

        if (listeningThreads.containsKey(user)) {
            listeningThreads.get(user).interrupt();
            listeningThreads.remove(user);
        }

        nextAreYou.remove(user);
        nextAreYou.add(user);
        sender.sendMessage("请把要投稿的图片发送给我吧~");

        Thread listener = new Thread(() -> {
            try {
                Thread.sleep(30 * 1000);

                if (Thread.currentThread().isInterrupted()) return;
                if (!nextAreYou.contains(user)) return;

                sender.sendMessage("还没想好要发什么嘛，想起来再来投稿吧!");

                nextAreYou.remove(user);
                listeningThreads.remove(user);
            } catch (Exception ignored) {
                nextAreYou.remove(user);
                listeningThreads.remove(user);
            }
        });

        listener.start();
        listeningThreads.put(user, listener);
    }
}
