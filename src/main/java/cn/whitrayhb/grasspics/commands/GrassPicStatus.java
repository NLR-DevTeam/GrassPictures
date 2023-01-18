package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.GrasspicsMain;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class GrassPicStatus extends JRawCommand {
    public static final GrassPicStatus INSTANCE = new GrassPicStatus();

    private GrassPicStatus() {
        super(GrasspicsMain.INSTANCE, "grass-pic-status", "草图信息");
        this.setDescription("草图信息");
        this.setPrefixOptional(true);
        this.setUsage("(/)草图信息  #草图信息");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull MessageChain args) {
        try {
            String info = GrassPic.fetchJson("https://grass.nlrdev.top/backend/status");
            if (info == null) throw new Exception("Failed to fetch GrassPic info!");

            JSONObject jsonObject = new JSONObject(info);
            MessageChain message = new MessageChainBuilder()
                    .append("---==草图信息==---")
                    .append("\n是否正常提供服务: ").append(jsonObject.getBoolean("service") ? "是" : "否")
                    .append("\n图片总数: ").append(String.valueOf(jsonObject.get("totalImage")))
                    .append("\n调用次数: ").append(String.valueOf(jsonObject.get("apiCount")))
                    .append("\n今日调用次数: ").append(String.valueOf(jsonObject.get("apiCountToday")))
                    .append("\n今日图片流量: ").append(jsonObject.getString("apiFlowTodayHuman"))
                    .append("\n图片总大小: ").append(jsonObject.getString("totalImageSizeHuman"))
                    .append("\n审核队列: ").append(jsonObject.getString("waitImage"))
                    .build();

            sender.sendMessage(message);
        } catch (Exception ex) {
            sender.sendMessage("发生错误! 请到控制台获取详细信息: \n" + ex);
            ex.printStackTrace();
        }
    }
}
