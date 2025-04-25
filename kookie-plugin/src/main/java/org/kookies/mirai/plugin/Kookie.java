package org.kookies.mirai.plugin;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.kookies.mirai.core.config.AuthorConfig;

public final class Kookie extends JavaPlugin {
    public static final Kookie INSTANCE = new Kookie();

    private Kookie() {
        super(new JvmPluginDescriptionBuilder(AuthorConfig.ID, AuthorConfig.VERSION)
                .info(AuthorConfig.INFO)
                .author(AuthorConfig.AUTHOR)
                .build()
        );
    }

    @Override
    public void onEnable() {
        getLogger().info("Kookie has been successfully loaded!");


        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(this);
        eventChannel.subscribeAlways(GroupMessageEvent.class, g -> {
            // Listen to group messages.
            getLogger().info(g.getMessage().contentToString());

        });
        eventChannel.subscribeAlways(FriendMessageEvent.class, f -> {
            // Listen to my friend's messages.
            getLogger().info(f.getMessage().contentToString());
        });

    }

}
