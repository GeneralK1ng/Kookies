package org.kookies.mirai.core.command;

import net.mamoe.mirai.event.events.MessageEvent;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CommandParser {
    public Optional<CommandContext> parse(MessageEvent event) {
        String text = event.getMessage().contentToString();
        for (Command cmd : CommandRegistry.all()) {
            for (Pattern p : cmd.patterns()) {
                Matcher m = p.matcher(text);
                if (m.matches()) {
                    return Optional.of(new CommandContext(cmd, m, event));
                }
            }
        }
        return Optional.empty();
    }

}
