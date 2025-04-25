package org.kookies.mirai.plugin;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;

import org.kookies.mirai.core.config.AuthorConfig;


public final class Kookies extends JavaPlugin {
    public static final Kookies INSTANCE = new Kookies();

    private Kookies() {
        super(new JvmPluginDescriptionBuilder(AuthorConfig.ID, AuthorConfig.VERSION)
                .info(AuthorConfig.INFO)
                .author(AuthorConfig.AUTHOR)
                .build());
    }

    @Override
    public void onEnable() {
        getLogger().info("Kookie 开始加载！");


        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(this);
        eventChannel.subscribeAlways(GroupMessageEvent.class, g -> {
            //监听群消息
            getLogger().info(g.getMessage().contentToString());

        });
        eventChannel.subscribeAlways(FriendMessageEvent.class, f -> {
            //监听好友消息
            getLogger().info(f.getMessage().contentToString());
        });

    }

}
