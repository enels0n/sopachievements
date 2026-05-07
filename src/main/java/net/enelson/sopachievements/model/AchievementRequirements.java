package net.enelson.sopachievements.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AchievementRequirements {

    private final String type;
    private final List<AchievementCriterion> criteria;

    public AchievementRequirements(String type, List<AchievementCriterion> criteria) {
        this.type = normalize(type);
        this.criteria = Collections.unmodifiableList(new ArrayList<AchievementCriterion>(criteria));
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
