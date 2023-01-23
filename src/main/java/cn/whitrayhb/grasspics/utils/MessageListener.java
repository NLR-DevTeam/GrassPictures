package cn.whitrayhb.grasspics.utils;

import net.mamoe.mirai.console.command.CommandSender;
import net.mamoe.mirai.contact.Friend;
import net.mamoe.mirai.contact.Group;
import net.mamoe.mirai.contact.Stranger;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.Listener;
import net.mamoe.mirai.event.ListeningStatus;
import net.mamoe.mirai.event.events.*;
import net.mamoe.mirai.message.data.MessageChain;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

public class MessageListener {
    private static final Timer timer = new Timer(true);
    private final long createTime = System.currentTimeMillis();
    private MessageChain message = null;

    public void getNextMessage(CommandSender sender, Consumer<MessageChain> onSuccess, Consumer<Exception> onTimeout, long timeout) {
        Class<? extends MessageEvent> nextEventClass;
        if (sender.getSubject() instanceof Group) {
            nextEventClass = GroupMessageEvent.class;
        } else if (sender.getUser() instanceof Friend) {
            nextEventClass = FriendMessageEvent.class;
        } else if (sender.getUser() instanceof Stranger) {
            nextEventClass = StrangerMessageEvent.class;
        } else {
            nextEventClass = GroupTempMessageEvent.class;
        }

        Listener<MessageEvent> listener = GlobalEventChannel.INSTANCE.subscribe(nextEventClass, m -> {
            if (Objects.requireNonNull(sender.getBot()).getId() != m.getBot().getId() ||
                    Objects.requireNonNull(sender.getUser()).getId() != m.getSender().getId())
                return ListeningStatus.LISTENING;

            message = m.getMessage();
            return ListeningStatus.STOPPED;
        });
        listener.start();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                if ((System.currentTimeMillis() - createTime) > timeout) {
                    onTimeout.accept(new TimeoutException());

                    this.cancel();
                    return;
                }

                if (message == null) return;

                onSuccess.accept(message);
                this.cancel();
            }
        }, 50, 50);
    }
}
