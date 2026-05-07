package net.enelson.sopachievements.model;

public final class AchievementConditionCheck {

    private final String type;
    private final String input;
    private final String output;

    public AchievementConditionCheck(String type, String input, String output) {
        this.type = type;
        this.input = input;
        this.output = output;
    }

    public String getType() {
        return type;
    }

    public String getInput() {
        return input;
    }

    public String getOutput() {
        return output;
    }
}
