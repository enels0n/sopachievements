package net.enelson.sopachievements.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AchievementRegistryModel {

    private final Map<String, AchievementCategory> categories;
    private final Map<String, AchievementDefinition> achievements;

    public AchievementRegistryModel(Map<String, AchievementCategory> categories, Map<String, AchievementDefinition> achievements) {
        this.categories = Collections.unmodifiableMap(new LinkedHashMap<String, AchievementCategory>(categories));
        this.achievements = Collections.unmodifiableMap(new LinkedHashMap<String, AchievementDefinition>(achievements));
    }

    public Map<String, AchievementCategory> getCategories() {
        return categories;
    }

    public Map<String, AchievementDefinition> getAchievements() {
        return achievements;
    }
}
