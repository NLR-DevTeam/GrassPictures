package cn.whitrayhb.grasspics.commands;

import cn.whitrayhb.grasspics.GrasspicsMain;
import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.console.command.java.JRawCommand;
import net.mamoe.mirai.message.data.MessageChain;
import org.jetbrains.annotations.NotNull;

public class PostGrassPic extends JRawCommand {
    public static final PostGrassPic INSTANCE = new PostGrassPic();

    private PostGrassPic() {
        super(GrasspicsMain.INSTANCE, "post-grass-pic", "投草图","草图投稿","投张草图");
        this.setDescription("草图投稿");
        this.setPrefixOptional(true);
        this.setUsage("(/)草图投稿  #草图投稿");
    }

    @Override
    public void onCommand(@NotNull CommandSender sender, @NotNull MessageChain args) {

    }
}
