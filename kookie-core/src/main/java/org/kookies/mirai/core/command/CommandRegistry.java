package org.kookies.mirai.core.command;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CommandRegistry {
    private static final List<Command> COMMANDS = new CopyOnWriteArrayList<>();

    public static void register(Command cmd) {
        COMMANDS.add(cmd);
    }

    public static List<Command> all() {
        return COMMANDS;
    }
}
