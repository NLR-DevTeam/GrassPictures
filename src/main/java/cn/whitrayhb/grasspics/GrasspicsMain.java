package cn.whitrayhb.grasspics;

import cn.whitrayhb.grasspics.commands.GrassPic;
import cn.whitrayhb.grasspics.commands.GrassPicStatus;
import cn.whitrayhb.grasspics.commands.PostGrassPic;
import cn.whitrayhb.grasspics.dataConfig.PluginConfig;
import cn.whitrayhb.grasspics.dataConfig.PluginData;
import cn.whitrayhb.grasspics.dataConfig.SimSoftSecureConfig;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent;
import net.mamoe.mirai.internal.deps.okhttp3.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class GrasspicsMain extends JavaPlugin {
    public static final OkHttpClient globalHttpClient = new OkHttpClient.Builder().connectTimeout(3, TimeUnit.SECONDS).readTimeout(10, TimeUnit.SECONDS).build();
    public static final GrasspicsMain INSTANCE = new GrasspicsMain();
    public static final ExecutorService globalExecutorService = Executors.newFixedThreadPool(16);
    public static final String TEXT_RULES = """
            1.请投稿能使多数人觉得有趣的图片
            2.严禁任何违法、涉政等图片上传
            3.禁止任何色情、擦边等图片上传
            4.禁止任何含有个人广告的图片上传
            5.禁止任何无关图片、灌水内容上传""";
    private static boolean usePublicPosting = false;

    private GrasspicsMain() {
        super(new JvmPluginDescriptionBuilder("cn.whitrayhb.grasspics", "1.1.7")
                .name("草图插件")
                .info("草图适配插件")
                .author("NLR DevTeam")
                .build());

        reloadPluginConfig(SimSoftSecureConfig.INSTANCE);
        reloadPluginConfig(PluginConfig.INSTANCE);
        reloadPluginData(PluginData.INSTANCE);
    }

    public static boolean shouldUsePublicPostingChannel() {
        return usePublicPosting;
    }

    private static void checkUpdate() {
        INSTANCE.getLogger().info("正在检查更新...");
        String currentVersion = INSTANCE.getDescription().getVersion().toString();
        Request request = new Request.Builder().url("https://api.github.com/repos/NLR-DevTeam/GrassPictures/releases/latest").get().build();

        globalHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                INSTANCE.getLogger().error("检查更新失败!");
                INSTANCE.getLogger().error(e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.body() == null) throw new RuntimeException("Unexpected null body.");
                JSONObject jsonObject = new JSONObject(response.body().string());
                String latestVersion = jsonObject.getString("tag_name");

                if (currentVersion.equals(latestVersion)) {
                    INSTANCE.getLogger().info("您正在运行最新版本!");
                    return;
                }

                INSTANCE.getLogger().info("发现插件更新: v" + latestVersion);
                INSTANCE.getLogger().info("访问 https://github.com/NLR-DevTeam/GrassPictures/releases/latest 下载最新版本.");
            }
        });
    }

    @Override
    public void onEnable() {
        CommandManager.INSTANCE.registerCommand(new GrassPic(), true);
        CommandManager.INSTANCE.registerCommand(new GrassPicStatus(), true);
        CommandManager.INSTANCE.registerCommand(new PostGrassPic(), true);

        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessageEvent.class, PostGrassPic::cacheImage);
        GlobalEventChannel.INSTANCE.subscribeAlways(GroupMessagePostSendEvent.class, PostGrassPic::cacheImage);

        String SIMS_USER = SimSoftSecureConfig.INSTANCE.user.get();
        String SIMS_TOKEN = SimSoftSecureConfig.INSTANCE.token.get();
        if (SIMS_USER.isEmpty() || SIMS_TOKEN.isEmpty()) {
            usePublicPosting = true;
            getLogger().warning("您正在使用公共投稿通道，如图片违规次数过多，则机器人 IP 就可能会被封禁。");
            getLogger().warning("如果您不希望启用公共投稿，请关闭聊群内投稿权限。");
        }

        checkUpdate();
    }
}
