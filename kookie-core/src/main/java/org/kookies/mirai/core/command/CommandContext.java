package org.kookies.mirai.core.command;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import net.mamoe.mirai.event.events.MessageEvent;

import java.util.regex.Matcher;

@Data
@RequiredArgsConstructor
public class CommandContext {
    private final Command command;
    private final Matcher matcher;
    private final MessageEvent event;
}
