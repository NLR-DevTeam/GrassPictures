package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.GrasspicsMain;
import cn.whitrayhb.grasspics.dataConfig.PluginConfig;
import cn.whitrayhb.grasspics.utils.Cooler;
import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.internal.deps.okhttp3.Request;
import net.mamoe.mirai.internal.deps.okhttp3.Response;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.InterruptedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.concurrent.Future;

public class GrassPic extends JRawCommand {
    public GrassPic() {
        super(GrasspicsMain.INSTANCE, "grass-pic", "来张草图", "生草");
        this.setDescription("来张草图");
        this.setPrefixOptional(true);
        this.setUsage("(/)生草  #来张草图");
    }

    public static String fetchJson(String inURL) {
        try {
            Request request = new Request.Builder().url(inURL).get().build();
            Response res = GrasspicsMain.globalHttpClient.newCall(request).execute();

            if (res.code() != 200) throw new RuntimeException("JSON 下载失败! 状态码为: " + res.code());

            if (res.body() == null) throw new RuntimeException("响应体为空!");

            return res.body().string();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 下载图片
     *
     * @return 字符串，为带文件名的图片位置
     */
    public static File fetchPicture(String id) throws Exception {
        try {
            File file = new File(GrasspicsMain.INSTANCE.getDataFolder(), "cache/");
            if (!file.exists() && !file.mkdirs()) {
                GrasspicsMain.INSTANCE.getLogger().error("缓存目录创建失败!");
                return null;
            }

            Request imageReq = new Request.Builder().url("https://grass.nlrdev.top/backend/image?id=" + id).get().build();
            Response imageRes = GrasspicsMain.globalHttpClient.newCall(imageReq).execute();
            if (imageRes.body() == null) throw new Exception("Unexpected null body.");

            Path cachePicturePath = new File(file, id).toPath();
            Files.deleteIfExists(cachePicturePath);
            Files.copy(imageRes.body().byteStream(), cachePicturePath);

            GrasspicsMain.INSTANCE.getLogger().info("图片下载成功!");
            return cachePicturePath.toFile();
        } catch (Exception e) {
            GrasspicsMain.INSTANCE.getLogger().error("图片下载失败!");
            throw e;
        }
    }

    @Override
    public void onCommand(@NotNull CommandContext context, @NotNull MessageChain args) {
        CommandSender sender = context.getSender();
        MessageChainBuilder builder = new MessageChainBuilder().append(new QuoteReply(context.getOriginalMessage()));
        String varArg;

        if (args.size() > 0) {
            varArg = args.get(0).contentToString().toLowerCase();
        } else {
            varArg = null;
        }

        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("请不要在控制台中运行该命令");
            return;
        }

        if (sender.getSubject() == null) return;

        long uid = Objects.requireNonNull(sender.getUser()).getId();

        if (Cooler.isLocked(uid)) {
            sender.sendMessage(builder.append("操作太快了，请稍后再试").build());
            return;
        }

        Cooler.lock(uid, PluginConfig.INSTANCE.getPictureLockTime.get());

        // Open a thread and a watcher
        Future<?> task = GrasspicsMain.globalExecutorService.submit(() -> {
            try {
                JSONObject jsonObject = new JSONObject(fetchJson("https://grass.nlrdev.top/backend/info" + (varArg == null ? "" : "?id=" + varArg)));
                if (jsonObject.getInt("code") != 200) {
                    sender.sendMessage(builder.append("您指定的图片不存在!").build());
                    return;
                }

                File file = fetchPicture(jsonObject.getString("id"));

                if (file != null) {
                    ExternalResource resource = ExternalResource.create(file);
                    Image image = sender.getSubject().uploadImage(resource);
                    sender.sendMessage(builder.append(image).build());

                    resource.close();
                }
            } catch (Exception ex) {
                if (ex instanceof InterruptedIOException) return;

                sender.sendMessage(builder.append("获取草图时发生错误，请稍后再试!\n").append(String.valueOf(ex)).build());
                ex.printStackTrace();
            }
        });

        GrasspicsMain.globalExecutorService.submit(() -> {
            try {
                Thread.sleep(PluginConfig.INSTANCE.getPictureTimeout.get());
            } catch (InterruptedException ex) {
                return;
            }

            if (task.isDone() || task.isCancelled()) return;
            task.cancel(true);

            sender.sendMessage(builder.append("草图获取超时，请稍后重试!").build());
        });
    }
}
