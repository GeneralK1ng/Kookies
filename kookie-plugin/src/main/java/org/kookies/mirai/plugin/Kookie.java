package org.kookies.mirai.plugin;

import lombok.extern.log4j.Log4j2;
import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import org.kookies.mirai.core.bootstrap.CoreBootstrap;
import org.kookies.mirai.core.config.AuthorConfig;
import org.kookies.mirai.core.event.CoreEventDispatcher;
import org.kookies.mirai.core.event.EventBus;
import org.kookies.mirai.plugin.commands.HelloWorldCommand;
import org.kookies.mirai.plugin.event.MiraiEventBus;

/**
 * @author General_K1ng
 */
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
        EventBus bus = new MiraiEventBus(this);
        CoreEventDispatcher eventDispatcher = new CoreEventDispatcher(bus);

        CoreBootstrap.with(eventDispatcher)
                .bind(new HelloWorldCommand())
                .start();

        log.info("Kookie has been successfully loaded!");
    }

}
