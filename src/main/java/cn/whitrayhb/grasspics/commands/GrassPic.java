package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.GrassPictures;
import cn.whitrayhb.grasspics.data.PluginConfig;
import cn.whitrayhb.grasspics.utils.Locker;
import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.internal.deps.okhttp3.Request;
import net.mamoe.mirai.internal.deps.okhttp3.Response;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.PlainText;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

public class GrassPic extends JRawCommand {
    private static final Vector<Object> tasks = new Vector<>();

    public GrassPic() {
        super(GrassPictures.INSTANCE, "grass-pic", "来张草图", "生草");
        this.setDescription("来张草图");
        this.setPrefixOptional(true);
        this.setUsage("(/)生草  #来张草图");
    }

    /**
     * 从网络获取 JSON
     *
     * @param inURL JSON URL
     * @return JSON 内容 (String)
     * @throws Exception 获取失败时抛出异常
     * @see #onCommand(CommandContext, MessageChain)
     */
    public static String fetchJson(String inURL) throws Exception {
        Request request = new Request.Builder().url(inURL).get().build();
        Response res = GrassPictures.globalHttpClient.newCall(request).execute();

        if (res.code() != 200) {
            throw new Exception("JSON Code 无效！状态码为：" + res.code());
        }

        if (res.body() == null) {
            throw new Exception("响应体为空！");
        }

        return res.body().string();
    }

    /**
     * 指定 ID 获取相应草图
     *
     * @param id 草图 ID
     * @return 缓存后的草图文件
     * @throws Exception 当获取失败时抛出
     * @see #onCommand(CommandContext, MessageChain)
     */
    public static File fetchPicture(String id) throws Exception {
        try {
            File file = new File(GrassPictures.INSTANCE.getDataFolder(), "cache");
            if (!file.exists() && !file.mkdirs()) {
                GrassPictures.INSTANCE.getLogger().error("缓存目录创建失败！");
                return null;
            }

            Path cachePicturePath = new File(file, id).toPath();

            Request imageReq = new Request.Builder().url(GrassPictures.IMAGE_URL + "?id=" + id).get().build();
            Response imageRes = GrassPictures.globalHttpClient.newCall(imageReq).execute();
            if (imageRes.body() == null) {
                throw new Exception("Unexpected null body.");
            }

            Files.deleteIfExists(cachePicturePath);
            Files.copy(imageRes.body().byteStream(), cachePicturePath);
            GrassPictures.INSTANCE.getLogger().info("图片下载成功！");

            return cachePicturePath.toFile();
        } catch (Exception e) {
            GrassPictures.INSTANCE.getLogger().error("图片下载失败！");
            throw e;
        }
    }

    @Override
    public void onCommand(@NotNull CommandContext context, @NotNull MessageChain args) {
        Object taskID = new Object();
        CommandSender sender = context.getSender();
        if (sender instanceof ConsoleCommandSender) {
            if (args.contains(new PlainText("reload"))) {
                GrassPictures.INSTANCE.reload();
                sender.sendMessage("配置重载成功！");
                return;
            }

            sender.sendMessage("请不要在控制台中运行该命令，如果需要重载配置，请使用 reload 子命令。");
            return;
        }

        /* Lambda Final - 我知道很粪 但改不了 */
        String varArg;
        if (args.size() > 0) {
            varArg = args.get(0).contentToString().toLowerCase();
        } else {
            varArg = null;
        }

        if (sender.getSubject() == null) {
            return;
        }

        long uid = Objects.requireNonNull(sender.getUser()).getId();

        if (Locker.isLocked(uid)) {
            sender.sendMessage(GrassPictures.wrap(context, "操作太快了，请稍后再试！"));
            return;
        }

        tasks.add(taskID);
        Locker.lock(uid, PluginConfig.INSTANCE.getPictureLockTime.get());

        // Open a thread and a watcher
        Future<?> task = GrassPictures.globalExecutorService.submit(() -> {
            try {
                JSONObject jsonObject = new JSONObject(fetchJson(GrassPictures.QUERY_URL + (varArg == null ? "" : "?id=" + varArg)));

                int code = jsonObject.getInt("code");
                if (code != 200) {
                    String message = switch (code) {
                        case 400 -> "操作太快，机器人被限流了，请过一会再试。";
                        case 403 -> "机器人 IP 地址已被加入黑名单！";
                        case 503 -> "草图服务接口目前正在维护，请耐心等待。";
                        case 999 -> "服务端返回内容：" + jsonObject.getString("msg");
                        case 1000 -> "您指定的图片不存在！";
                        default -> "服务端响应无效：" + code;
                    };

                    sender.sendMessage(GrassPictures.wrap(context, message));
                    return;
                }

                File file;
                try {
                    file = fetchPicture(jsonObject.getString("id"));
                } catch (IOException ex) {
                    // Connect failed, use cache image
                    Stream<Path> stream = Files.list(new File(GrassPictures.INSTANCE.getDataFolder(), "cache").toPath());

                    List<Path> list = stream.toList();
                    stream.close();

                    if (list.size() == 0) {
                        throw new Exception("获取图片失败，且缓存中没有图片。");
                    }

                    Random random = new Random();
                    Path randomInCache = list.get(random.nextInt(list.size() - 1));

                    file = randomInCache.toFile();
                }

                if (file != null && tasks.contains(taskID)) {
                    ExternalResource resource = ExternalResource.create(file);
                    Image image = sender.getSubject().uploadImage(resource);
                    sender.sendMessage(GrassPictures.wrap(context, image));

                    resource.close();
                }

                tasks.remove(taskID);
            } catch (InterruptedIOException | InterruptedException | TimeoutException ignored) {
                // Completely ignored
            } catch (Exception ex) {
                if (!tasks.contains(taskID)) {
                    return;
                }

                tasks.remove(taskID);
                sender.sendMessage(GrassPictures.wrap(context, "获取草图时发生错误，请稍后再试！\n" + ex));
                ex.printStackTrace();
            }
        });

        // 监视线程
        GrassPictures.globalExecutorService.submit(() -> {
            try {
                Thread.sleep(PluginConfig.INSTANCE.getPictureTimeout.get());
            } catch (InterruptedException ex) {
                return;
            }

            if (task.isDone() || task.isCancelled()) {
                return;
            }
            task.cancel(true);

            if (!tasks.contains(taskID)) {
                return;
            }

            tasks.remove(taskID);
            sender.sendMessage(GrassPictures.wrap(context, "草图获取超时，请稍后重试！"));
        });
    }
}
