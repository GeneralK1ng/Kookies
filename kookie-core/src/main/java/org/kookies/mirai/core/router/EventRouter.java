package org.kookies.mirai.core.router;

import lombok.extern.log4j.Log4j2;
import net.mamoe.mirai.event.events.FriendMessageEvent;
import net.mamoe.mirai.event.events.GroupMessageEvent;
import net.mamoe.mirai.event.events.MessageEvent;
import org.kookies.mirai.core.command.CommandContext;
import org.kookies.mirai.core.command.CommandParser;
import org.kookies.mirai.core.event.CoreEventDispatcher;

import java.util.Optional;

@Log4j2
public class EventRouter {
    private final CommandParser PARSER = new CommandParser();

    public EventRouter(CoreEventDispatcher dispatcher) {
        dispatcher.registerHandler(GroupMessageEvent.class, this::route);
        dispatcher.registerHandler(FriendMessageEvent.class, this::route);
    }

    private void route(MessageEvent event) {
        Optional<CommandContext> ctxOpt = PARSER.parse(event);
        if (ctxOpt.isEmpty()) return;
        CommandContext ctx = ctxOpt.get();
        try {
            ctx.getCommand().execute(ctx.getMatcher(), ctx.getEvent());
        } catch (Exception ex) {
            log.error("Command execution error", ex);
        }
    }
}
