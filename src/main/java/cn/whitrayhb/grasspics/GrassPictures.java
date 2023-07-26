package cn.whitrayhb.grasspics;

import cn.whitrayhb.grasspics.commands.GrassPic;
import cn.whitrayhb.grasspics.commands.GrassPicStatus;
import cn.whitrayhb.grasspics.commands.PostGrassPic;
import cn.whitrayhb.grasspics.data.PluginConfig;
import cn.whitrayhb.grasspics.data.PluginData;
import cn.whitrayhb.grasspics.data.SimSoftSecureConfig;
import net.mamoe.mirai.console.command.CommandContext;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.GroupMessagePostSendEvent;
import net.mamoe.mirai.internal.deps.okhttp3.*;
import net.mamoe.mirai.message.data.*;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public final class GrassPictures extends JavaPlugin {
    public static final OkHttpClient globalHttpClient = new OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build();
    public static final ExecutorService globalExecutorService = Executors.newFixedThreadPool(16);
    public static final Timer globalTimer = new Timer("GrassPicture Timer", true);
    public static final String VERSION = "1.2.1";
    public static final String TEXT_RULES = """
            1.请投稿能使多数人觉得有趣的图片
            2.严禁上传任何包含违法、涉政等信息的图片
            3.禁止上传任何包含色情、擦边等令人反感的图片
            4.禁止上传任何含有个人广告的图片
            5.禁止上传任何无关图片、灌水内容""";
    public static final String QUERY_URL = "https://oss.grass.starxw.com/service/info";
    public static final String IMAGE_URL = "https://oss.grass.starxw.com/service/image";
    public static final String PUBLIC_POST_URL = "https://oss.grass.starxw.com/service/upload";
    public static final String STATUS_URL = "https://oss.grass.starxw.com/service/status";
    public static GrassPictures INSTANCE = null;
    public static String latestVersion = null;
    private static boolean usePublicPosting = false;

    private GrassPictures() {
        super(new JvmPluginDescriptionBuilder("cn.whitrayhb.grasspics", VERSION)
                .name("草图插件")
                .info("草图适配插件")
                .author("NLR DevTeam")
                .build());

        reload();
    }

    /**
     * 获取是否应该使用公共投稿通道
     * 让值 "readonly"，需要通过 reload 改变
     *
     * @return 是否应该使用公共投稿通道
     * @see #usePublicPosting
     */
    public static boolean shouldUsePublicPostingChannel() {
        return usePublicPosting;
    }

    /**
     * 检查更新
     *
     * @see #onEnable()
     */
    private static void checkUpdate() {
        INSTANCE.getLogger().info("正在检查更新...");
        String currentVersion = INSTANCE.getDescription().getVersion().toString();
        Request request = new Request.Builder().url("https://api.github.com/repos/NLR-DevTeam/GrassPictures/releases/latest").get().build();

        globalHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                INSTANCE.getLogger().error("检查更新失败：" + e);
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                if (response.body() == null) {
                    throw new RuntimeException("Unexpected null body.");
                }

                JSONObject jsonObject = new JSONObject(response.body().string());
                String ver = jsonObject.getString("tag_name");

                if (currentVersion.equals(ver)) {
                    INSTANCE.getLogger().info("您正在运行最新版本！");
                    return;
                }

                latestVersion = ver;
                INSTANCE.getLogger().info("发现插件更新：v" + ver);
                INSTANCE.getLogger().info("访问 https://github.com/NLR-DevTeam/GrassPictures/releases/latest 以下载最新版本。");
            }
        });
    }

    public static MessageChain wrap(@NotNull CommandContext context, @NotNull Object messageToWrap) {
        Message message;
        if (messageToWrap instanceof String string) {
            message = new PlainText(string);
        } else if (messageToWrap instanceof Message msg) {
            message = msg;
        } else {
            throw new IllegalArgumentException("Message or String expected.");
        }

        return new MessageChainBuilder()
                .append(new QuoteReply(context.getOriginalMessage()))
                .append(message)
                .build();
    }

    /**
     * 插件加载事件
     * 会注册草图相关命令以及监听器。
     */
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

        globalTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                checkUpdate();
            }
        }, 0, TimeUnit.HOURS.toMillis(12));
    }

    /**
     * 重载插件配置与数据
     *
     * @see #GrassPictures()
     * @see GrassPic#onCommand(CommandContext, MessageChain)
     */
    public void reload() {
        reloadPluginConfig(SimSoftSecureConfig.INSTANCE);
        reloadPluginConfig(PluginConfig.INSTANCE);
        reloadPluginData(PluginData.INSTANCE);
    }
}
