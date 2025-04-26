package org.kookies.mirai.core.command;

import net.mamoe.mirai.event.events.MessageEvent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Command {
    /**
     * The regular expression that can trigger this command.
     *
     * @return The regular expression that can trigger this command.
     */
    List<Pattern> patterns();

    /**
     * Execute the command.
     *
     * @param matcher The matcher that matches the command.
     * @param event   The event that triggered the command.
     */
    void execute(Matcher matcher, MessageEvent event);
}
