package org.kookies.mirai.plugin;

import lombok.extern.log4j.Log4j2;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.Event;
import net.mamoe.mirai.event.EventChannel;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.kookies.mirai.core.config.AuthorConfig;

@Log4j2
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
        log.info("Kookie has been successfully loaded!");


        EventChannel<Event> eventChannel = GlobalEventChannel.INSTANCE.parentScope(this);
        eventChannel.subscribeAlways(GroupMessageEvent.class, g -> {
            // Listen to group messages.
            log.info(g.getMessage().contentToString());

        });
        eventChannel.subscribeAlways(FriendMessageEvent.class, f -> {
            // Listen to my friend's messages.
            log.info(f.getMessage().contentToString());
        });

    }

}
