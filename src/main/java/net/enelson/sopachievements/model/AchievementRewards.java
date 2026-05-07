package net.enelson.sopachievements.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AchievementRewards {

    private final List<String> commands;
    private final String message;
    private final String messageKey;
    private final String messageFallback;

    public AchievementRewards(List<String> commands, String message, String messageKey, String messageFallback) {
        this.commands = Collections.unmodifiableList(new ArrayList<String>(commands));
        this.message = message == null ? "" : message;
        this.messageKey = messageKey == null ? "" : messageKey;
        this.messageFallback = messageFallback == null ? "" : messageFallback;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public String getMessageFallback() {
        return messageFallback;
    }

    public boolean hasCommands() {
        return !commands.isEmpty();
    }

    public boolean hasMessage() {
        return !message.trim().isEmpty() || !messageKey.trim().isEmpty() || !messageFallback.trim().isEmpty();
    }
}
