package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.Cooler;
import cn.whitrayhb.grasspics.GrasspicsMain;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.internal.deps.okhttp3.OkHttpClient;
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
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class GrassPic extends JRawCommand {
    public static final GrassPic INSTANCE = new GrassPic();

    private GrassPic() {
        super(GrasspicsMain.INSTANCE, "grass-pic", "来张草图", "生草");
        this.setDescription("来张草图");
        this.setPrefixOptional(true);
        this.setUsage("(/)生草  #来张草图");
    }

    public static String fetchJson(String inURL) {
        try {
            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).callTimeout(10, TimeUnit.SECONDS).build();
            Request request = new Request.Builder().url(inURL).get().build();
            Response res = client.newCall(request).execute();

            if (res.code() != 200) {
                GrasspicsMain.INSTANCE.getLogger().error("JSON下载失败！状态码为" + res.code());
                return null;
            }

            if (res.body() == null) {
                GrasspicsMain.INSTANCE.getLogger().error("Unexpected null body.");
                return null;
            }

            return res.body().string();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 下载图片
     *
     * @return 字符串，为带文件名的图片位置
     */
    public static String fetchPicture() {
        try {
            File file = new File("./data/cn.whitrayhb.hbsplugin/cache/grasspic/");
            if (!file.exists() && !file.mkdirs()) {
                GrasspicsMain.INSTANCE.getLogger().error("缓存目录创建失败!");
                return null;
            }

            OkHttpClient client = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).callTimeout(10, TimeUnit.SECONDS).build();
            Request request = new Request.Builder().url("https://grass.nlrdev.top/backend/info").get().build();
            Response response = client.newCall(request).execute();
            if (response.body() == null) throw new Exception("Unexpected null body.");
            JSONObject jsonObject = new JSONObject(response.body().string());

            Request imageReq = new Request.Builder().url("https://grass.nlrdev.top/backend/image?id=" + jsonObject.getString("id")).get().build();
            Response imageRes = client.newCall(imageReq).execute();
            if (imageRes.body() == null) throw new Exception("Unexpected null body.");

            Path cachePicturePath = Paths.get("./data/cn.whitrayhb.hbsplugin/cache/grasspic/" + jsonObject.getString("id"));
            Files.deleteIfExists(cachePicturePath);
            Files.copy(imageRes.body().byteStream(), cachePicturePath);

            GrasspicsMain.INSTANCE.getLogger().info("图片下载成功！");
            return "./data/cn.whitrayhb.hbsplugin/cache/grasspic/" + jsonObject.getString("id");
        } catch (Exception e) {
            GrasspicsMain.INSTANCE.getLogger().error("图片下载失败!");
            GrasspicsMain.INSTANCE.getLogger().error(e);
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull MessageChain args) {
        if (sender.getSubject() == null) {
            sender.sendMessage("请不要在控制台中运行该命令");
            return;
        }

        if (Cooler.isLocked(sender)) {
            sender.sendMessage("操作太快了，请稍后再试");
            return;
        }

        Cooler.lock(sender, 30);

        // Open a thread and a watcher
        Thread getThread = new Thread(() -> {
            String picPath = fetchPicture();

            if (picPath != null) {
                File file = new File(picPath);

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

            if (!getThread.isAlive()) return;
            getThread.stop();

            sender.sendMessage("草图获取超时, 请稍后重试!");
        }).start();
    }
}
