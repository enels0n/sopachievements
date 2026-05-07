package net.enelson.sopachievements.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class AchievementRewards {

    private final List<String> commands;
    private final String message;

    public AchievementRewards(List<String> commands, String message) {
        this.commands = Collections.unmodifiableList(new ArrayList<String>(commands));
        this.message = message == null ? "" : message;
    }

    public List<String> getCommands() {
        return commands;
    }

    public String getMessage() {
        return message;
    }

    public boolean hasCommands() {
        return !commands.isEmpty();
    }

    public boolean hasMessage() {
        return !message.trim().isEmpty();
    }
}
