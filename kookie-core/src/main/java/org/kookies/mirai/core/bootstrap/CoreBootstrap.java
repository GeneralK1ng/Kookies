package org.kookies.mirai.core.bootstrap;

import org.kookies.mirai.core.command.Command;
import org.kookies.mirai.core.command.CommandRegistry;
import org.kookies.mirai.core.event.CoreEventDispatcher;
import org.kookies.mirai.core.router.EventRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class CoreBootstrap {
    private final CoreEventDispatcher dispatcher;
    private final List<Command> commands = new ArrayList<>();

    private CoreBootstrap(CoreEventDispatcher dispatcher) {
        this.dispatcher = Objects.requireNonNull(dispatcher, "dispatcher");
    }
    public static CoreBootstrap with(CoreEventDispatcher dispatcher) {
        return new CoreBootstrap(dispatcher);
    }

    public CoreBootstrap bind(Command cmd) {
        commands.add(Objects.requireNonNull(cmd, "cmd"));
        return this;
    }

    public CoreBootstrap bindAll(Iterable<Command> cmds) {
        cmds.forEach(cmd -> bind(Objects.requireNonNull(cmd, "cmd")));
        return this;
    }

    public void start() {
        commands.forEach(CommandRegistry::register);
        new EventRouter(dispatcher);
    }
}
