package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.GrasspicsMain;
import cn.whitrayhb.grasspics.dataConfig.PluginConfig;
import cn.whitrayhb.grasspics.utils.Cooler;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.ConsoleCommandSender;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.internal.deps.okhttp3.Request;
import net.mamoe.mirai.internal.deps.okhttp3.Response;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

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
    public static File fetchPicture() {
        try {
            File file = new File(GrasspicsMain.INSTANCE.getDataFolder(), "cache/");
            if (!file.exists() && !file.mkdirs()) {
                GrasspicsMain.INSTANCE.getLogger().error("缓存目录创建失败!");
                return null;
            }

            Request request = new Request.Builder().url("https://grass.nlrdev.top/backend/info").get().build();
            Response response = GrasspicsMain.globalHttpClient.newCall(request).execute();
            if (response.body() == null) throw new Exception("Unexpected null body.");
            JSONObject jsonObject = new JSONObject(response.body().string());

            Request imageReq = new Request.Builder().url("https://grass.nlrdev.top/backend/image?id=" + jsonObject.getString("id")).get().build();
            Response imageRes = GrasspicsMain.globalHttpClient.newCall(imageReq).execute();
            if (imageRes.body() == null) throw new Exception("Unexpected null body.");

            Path cachePicturePath = new File(file, jsonObject.getString("id")).toPath();
            Files.deleteIfExists(cachePicturePath);
            Files.copy(imageRes.body().byteStream(), cachePicturePath);

            GrasspicsMain.INSTANCE.getLogger().info("图片下载成功!");
            return cachePicturePath.toFile();
        } catch (Exception e) {
            GrasspicsMain.INSTANCE.getLogger().error("图片下载失败!");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull MessageChain args) {
        if (sender instanceof ConsoleCommandSender) {
            sender.sendMessage("请不要在控制台中运行该命令");
            return;
        }

        if (sender.getSubject() == null) return;

        long uid = Objects.requireNonNull(sender.getUser()).getId();

        if (Cooler.isLocked(uid)) {
            sender.sendMessage("操作太快了，请稍后再试");
            return;
        }

        Cooler.lock(uid, PluginConfig.INSTANCE.getPictureLockTime.get());

        // Open a thread and a watcher
        Thread getThread = new Thread(() -> {
            File file = fetchPicture();

            if (file != null) {
                ExternalResource resource = ExternalResource.create(file);
                Image image = sender.getSubject().uploadImage(resource);
                sender.sendMessage(image);

                try {
                    resource.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        getThread.start();

        new Thread(() -> {
            try {
                Thread.sleep(10 * 1000);
            } catch (InterruptedException e) {
                return;
            }

            if (!getThread.isInterrupted()) return;
            getThread.interrupt();

            sender.sendMessage("草图获取超时, 请稍后重试!");
        }).start();
    }
}
