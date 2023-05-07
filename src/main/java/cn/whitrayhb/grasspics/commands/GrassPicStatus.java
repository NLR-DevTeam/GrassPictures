package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.GrasspicsMain;
import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.message.data.MessageChain;
import net.mamoe.mirai.message.data.MessageChainBuilder;
import net.mamoe.mirai.message.data.QuoteReply;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

public class GrassPicStatus extends JRawCommand {
    public GrassPicStatus() {
        super(GrasspicsMain.INSTANCE, "grass-pic-status", "草图信息");
        this.setDescription("草图信息");
        this.setPrefixOptional(true);
        this.setUsage("(/)草图信息  #草图信息");
    }

    @Override
    public void onCommand(@NotNull CommandContext context, @NotNull MessageChain args) {
        CommandSender sender = context.getSender();
        MessageChainBuilder builder = new MessageChainBuilder().append(new QuoteReply(context.getOriginalMessage()));

        try {
            String info = GrassPic.fetchJson("https://oss.grass.starxw.com/service/status");

            JSONObject jsonObject = new JSONObject(info);
            MessageChain message = new MessageChainBuilder()
                    .append("---==草图信息==---")
                    .append("\n是否正常提供服务: ").append(jsonObject.getBoolean("service") ? "是" : "否")
                    .append("\n图片总数: ").append(String.valueOf(jsonObject.get("totalImage")))
                    .append("\n今日调用次数: ").append(String.valueOf(jsonObject.get("apiCountToday")))
                    .append("\n今日图片流量: ").append(jsonObject.getString("apiFlowTodayHuman"))
                    .append("\n图片总大小: ").append(jsonObject.getString("totalImageSizeHuman"))
                    .append("\n审核队列: ").append(jsonObject.getString("waitImage"))
                    .append("\n插件版本: ").append(GrasspicsMain.INSTANCE.getDescription().getVersion().toString())
                    .build();

            sender.sendMessage(builder.append(message).build());
        } catch (Exception ex) {
            sender.sendMessage(builder.append("发生错误! 请到控制台获取详细信息: \n").append(String.valueOf(ex)).build());
            ex.printStackTrace();
        }
    }
}
