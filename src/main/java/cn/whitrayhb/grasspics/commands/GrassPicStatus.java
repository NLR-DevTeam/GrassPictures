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
        String info = GrassPic.fetchJson("https://grass.nlrdev.top/backend/status");
        JSONObject jsonObject = new JSONObject(info);
        MessageChain message = new MessageChainBuilder()
                .append("---==草图信息==---")
                .append("\n是否正常提供服务："+jsonObject.get("service"))
                .append("\n图片总数："+jsonObject.get("totalImage"))
                .append("\n待审核图片数："+jsonObject.get("waitImage"))
                .append("\n调用次数："+jsonObject.get("apiCount"))
                .append("\n今日调用次数："+jsonObject.get("apiCountToday"))
                .append("\n图片总大小："+jsonObject.get("totalImageSizeHuman"))
                .append("\n今日图片流量:"+jsonObject.get("apiFlowTodayHuman")).build();
        sender.sendMessage(message);
    }
}
