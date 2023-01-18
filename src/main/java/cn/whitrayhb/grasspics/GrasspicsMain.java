package cn.whitrayhb.grasspics;

import cn.whitrayhb.grasspics.commands.GrassPic;
import cn.whitrayhb.grasspics.commands.GrassPicStatus;
import cn.whitrayhb.grasspics.commands.PostGrassPic;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;


public final class GrasspicsMain extends JavaPlugin {
    public static final GrasspicsMain INSTANCE = new GrasspicsMain();

    private GrasspicsMain() {
        super(new JvmPluginDescriptionBuilder("cn.whitrayhb.grasspics", "1.1.1")
                .name("草图插件")
                .info("草图适配插件")
                .author("NLR DevTeam")
                .build());

        reloadPluginConfig(JavaConfig.INSTANCE);
    }

    @Override
    public void onEnable() {
        CommandManager.INSTANCE.registerCommand(GrassPic.INSTANCE, true);
        CommandManager.INSTANCE.registerCommand(GrassPicStatus.INSTANCE, true);
        CommandManager.INSTANCE.registerCommand(PostGrassPic.INSTANCE, true);
    }
}
