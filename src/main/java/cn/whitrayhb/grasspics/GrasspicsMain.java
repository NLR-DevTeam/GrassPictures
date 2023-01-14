package cn.whitrayhb.grasspics;

import cn.whitrayhb.grasspics.commands.GrassPic;
import cn.whitrayhb.grasspics.commands.GrassPicStatus;
import kotlin.Lazy;
import kotlin.LazyKt;
import net.mamoe.mirai.console.command.CommandManager;
import net.mamoe.mirai.console.permission.*;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescription;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Member;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;


public final class GrasspicsMain extends JavaPlugin {
    public static final GrasspicsMain INSTANCE = new GrasspicsMain();

    private GrasspicsMain() {
        super(new JvmPluginDescriptionBuilder("cn.whitrayhb.grasspics", "1.0.1")
                .info("草图插件")
                .build());
    }

    @Override
    public void onEnable() {
        CommandManager.INSTANCE.registerCommand(GrassPic.INSTANCE,true);
        CommandManager.INSTANCE.registerCommand(GrassPicStatus.INSTANCE,true);
    }
}
