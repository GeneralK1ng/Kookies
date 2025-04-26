package org.kookies.mirai.plugin;

import lombok.extern.log4j.Log4j2;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import org.kookies.mirai.core.config.AuthorConfig;
import org.kookies.mirai.core.event.CoreEventDispatcher;
import org.kookies.mirai.core.event.EventBus;
import org.kookies.mirai.plugin.event.MiraiEventBus;

/**
 * @author General_K1ng
 */
@Log4j2
public final class Kookie extends JavaPlugin {
    public static final Kookie INSTANCE = new Kookie();
    private CoreEventDispatcher eventDispatcher;

    private Kookie() {
        super(new JvmPluginDescriptionBuilder(AuthorConfig.ID, AuthorConfig.VERSION)
                .info(AuthorConfig.INFO)
                .author(AuthorConfig.AUTHOR)
                .build()
        );
    }

    @Override
    public void onEnable() {
        EventBus bus = new MiraiEventBus(this);
        eventDispatcher = new CoreEventDispatcher(bus);

        registerHandlers();

        log.info("Kookie has been successfully loaded!");
    }

    private void registerHandlers() {
        eventDispatcher.registerHandler(GroupMessageEvent.class, event -> {
            try {
                log.info("Received a group message: {}", event.getMessage().contentToString());
            } catch (Exception e) {
                log.error("There is an exception in processing group messages:", e);
            }
        });

        eventDispatcher.registerHandler(FriendMessageEvent.class, event -> {
            try {
                log.info("Received a private message: {}", event.getMessage().contentToString());
            } catch (Exception e) {
                log.error("There is an exception in processing private messages:", e);
            }
        });
    }

}
