package cn.whitrayhb.grasspics;

import cn.whitrayhb.grasspics.commands.GrassPic;
import cn.whitrayhb.grasspics.commands.GrassPicStatus;
import cn.whitrayhb.grasspics.commands.PostGrassPic;
import cn.whitrayhb.grasspics.dataconfig.PluginConfig;
import cn.whitrayhb.grasspics.dataconfig.PluginData;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;


public final class GrasspicsMain extends JavaPlugin {
    public static final GrasspicsMain INSTANCE = new GrasspicsMain();
    public static final String TEXT_RULES = "1.请投稿能使多数人觉得有趣的图片\n" +
            "2.严禁任何违法、涉政等图片上传\n" +
            "3.禁止任何色情、擦边等图片上传\n" +
            "4.禁止任何含有个人广告的图片上传\n" +
            "5.禁止任何无关图片、灌水内容上传";
    private static boolean usePublicPosting = false;

    private GrasspicsMain() {
        super(new JvmPluginDescriptionBuilder("cn.whitrayhb.grasspics", "1.1.2")
                .name("草图插件")
                .info("草图适配插件")
                .author("NLR DevTeam")
                .build());

        reloadPluginConfig(PluginConfig.INSTANCE);
        reloadPluginData(PluginData.INSTANCE);
    }

    public static boolean shouldUsePublicPostingChannel() {
        return usePublicPosting;
    }

    @Override
    public void onEnable() {
        CommandManager.INSTANCE.registerCommand(GrassPic.INSTANCE, true);
        CommandManager.INSTANCE.registerCommand(GrassPicStatus.INSTANCE, true);
        CommandManager.INSTANCE.registerCommand(PostGrassPic.INSTANCE, true);

        String SIMS_USER = PluginConfig.INSTANCE.user.get();
        String SIMS_TOKEN = PluginConfig.INSTANCE.token.get();
        if (SIMS_USER.isEmpty() || SIMS_TOKEN.isEmpty()) {
            usePublicPosting = true;
            getLogger().warning("您正在使用公共投稿通道，如图片违规次数过多，则机器人 IP 就可能会被封禁。");
            getLogger().warning("如果您不希望启用公共投稿，请关闭聊群内投稿权限。");
        }
    }
}
