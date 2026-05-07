package net.enelson.sopachievements.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AchievementRequirements {

    private final String type;
    private final List<AchievementCriterion> criteria;
    private final boolean ordered;
    private final boolean resetOnDeath;
    private final boolean resetOnWorldChange;
    private final boolean resetOnTeleport;

    public AchievementRequirements(String type,
                                   List<AchievementCriterion> criteria,
                                   boolean ordered,
                                   boolean resetOnDeath,
                                   boolean resetOnWorldChange,
                                   boolean resetOnTeleport) {
        this.type = normalize(type);
        this.criteria = Collections.unmodifiableList(new ArrayList<AchievementCriterion>(criteria));
        this.ordered = ordered;
        this.resetOnDeath = resetOnDeath;
        this.resetOnWorldChange = resetOnWorldChange;
        this.resetOnTeleport = resetOnTeleport;
    }

    public String getType() {
        return type;
    }

    public boolean isAnyMode() {
        return "any".equals(type);
    }

    public List<AchievementCriterion> getCriteria() {
        return criteria;
    }

    public boolean isOrdered() {
        return ordered;
    }

    public boolean isResetOnDeath() {
        return resetOnDeath;
    }

    public boolean isResetOnWorldChange() {
        return resetOnWorldChange;
    }

    public boolean isResetOnTeleport() {
        return resetOnTeleport;
    }

    public boolean isEmpty() {
        return criteria.isEmpty();
    }

    private String normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "all";
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return "any".equals(normalized) ? "any" : "all";
    }
}
