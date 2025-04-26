package org.kookies.mirai.plugin.commands;

import net.mamoe.mirai.event.events.MessageEvent;
import org.kookies.mirai.core.command.Command;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author General_K1ng
 */
public class HelloWorldCommand implements Command {

    private static final List<Pattern> PATTERNS = List.of(
            Pattern.compile("^hello$")
    );

    @Override
    public List<Pattern> patterns() {
        return PATTERNS;
    }

    @Override
    public void execute(Matcher matcher, MessageEvent event) {
        event.getSubject().sendMessage("Hello, World!");
    }
}
