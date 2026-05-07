package net.enelson.sopachievements.model;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AchievementTrigger {

    private final String type;
    private final Map<String, Object> settings;

    public AchievementTrigger(String type, Map<String, Object> settings) {
        this.type = type;
        this.settings = Collections.unmodifiableMap(new LinkedHashMap<String, Object>(settings));
    }

    public String getType() {
        return type;
    }

    public Map<String, Object> getSettings() {
        return settings;
    }

    public String getString(String key, String defaultValue) {
        Object value = settings.get(key);
        return value == null ? defaultValue : String.valueOf(value);
    }

    public int getInt(String key, int defaultValue) {
        Object value = settings.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        if (value != null) {
            try {
                return Integer.parseInt(String.valueOf(value));
            } catch (NumberFormatException ignored) {
            }
        }
        return defaultValue;
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        Object value = settings.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value != null) {
            return Boolean.parseBoolean(String.valueOf(value));
        }
        return defaultValue;
    }
}
