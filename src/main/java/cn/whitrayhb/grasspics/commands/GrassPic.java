package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.Cooler;
import cn.whitrayhb.grasspics.GrasspicsMain;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.message.data.Image;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.utils.ExternalResource;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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
            URL url = new URL(inURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(10000);
            conn.setRequestProperty("Accept", "application/json");
            conn.connect();

            if (conn.getResponseCode() == 200) {
                InputStream is = conn.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));
                conn.disconnect();

                return br.readLine();
            } else {
                GrasspicsMain.INSTANCE.getLogger().error("JSON下载失败！状态码为" + conn.getResponseCode());
                conn.disconnect();

                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 下载图片
     *
     * @param inUrl 将要下载的图片的链接
     * @param path  图片将要保存的位置
     * @return 字符串，为带文件名的图片位置
     */
    public static String fetchPicture(String inUrl, String path) {
        String[] arrUrl = inUrl.split("/");
        String name = arrUrl[arrUrl.length - 1];

        try {
            File file = new File(path);
            if (!file.exists() && !file.mkdirs()) {
                GrasspicsMain.INSTANCE.getLogger().error("缓存目录创建失败!");
                return null;
            }

            URL url = new URL(inUrl);
            HttpURLConnection httpUrl = (HttpURLConnection) url.openConnection();
            httpUrl.setConnectTimeout(3000);
            httpUrl.setReadTimeout(10000);
            httpUrl.connect();

            Files.copy(httpUrl.getInputStream(), Paths.get(path + "/" + name));

            httpUrl.disconnect();
        } catch (Exception e) {
            GrasspicsMain.INSTANCE.getLogger().error("图片下载失败!");
            GrasspicsMain.INSTANCE.getLogger().error(e);
            return null;
        }

        GrasspicsMain.INSTANCE.getLogger().info("图片下载成功！");
        return path + "/" + name;
    }

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
        String savePath = "./data/cn.whitrayhb.hbsplugin/cache/grasspic/";
        String picPath = fetchPicture("https://i.simsoft.top/grass/backend/image", savePath);

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
    }
}
