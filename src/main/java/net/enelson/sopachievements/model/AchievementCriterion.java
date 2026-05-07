package net.enelson.sopachievements.model;

public final class AchievementCriterion {

    private final String id;
    private final AchievementTrigger trigger;
    private final AchievementConditions conditions;

    public AchievementCriterion(String id, AchievementTrigger trigger, AchievementConditions conditions) {
        this.id = id;
        this.trigger = trigger;
        this.conditions = conditions;
    }

    public String getId() {
        return id;
    }

    public AchievementTrigger getTrigger() {
        return trigger;
    }

    public AchievementConditions getConditions() {
        return conditions;
    }
}
