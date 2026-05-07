package net.enelson.sopachievements.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public final class AchievementConditions {

    private final String type;
    private final List<AchievementConditionCheck> checks;

    public AchievementConditions(String type, List<AchievementConditionCheck> checks) {
        this.type = normalize(type);
        this.checks = Collections.unmodifiableList(new ArrayList<AchievementConditionCheck>(checks));
    }

    public String getType() {
        return type;
    }

    public List<AchievementConditionCheck> getChecks() {
        return checks;
    }

    public boolean isEmpty() {
        return checks.isEmpty();
    }

    public boolean isAnyMode() {
        return "any".equals(type);
    }

    private String normalize(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return "all";
        }
        String normalized = raw.trim().toLowerCase(Locale.ROOT);
        return "any".equals(normalized) ? "any" : "all";
    }
}
