package net.enelson.sopachievements.model;

public final class AchievementCategory {

    private final String id;
    private final String title;
    private final String description;
    private final String nameKey;
    private final String nameKeyFallback;
    private final String loreKey;
    private final String loreKeyFallback;
    private final String iconMaterial;
    private final String background;

    public AchievementCategory(String id,
                               String title,
                               String description,
                               String nameKey,
                               String nameKeyFallback,
                               String loreKey,
                               String loreKeyFallback,
                               String iconMaterial,
                               String background) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.nameKey = nameKey;
        this.nameKeyFallback = nameKeyFallback;
        this.loreKey = loreKey;
        this.loreKeyFallback = loreKeyFallback;
        this.iconMaterial = iconMaterial;
        this.background = background;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getNameKey() {
        return nameKey;
    }

    public String getNameKeyFallback() {
        return nameKeyFallback;
    }

    public String getLoreKey() {
        return loreKey;
    }

    public String getLoreKeyFallback() {
        return loreKeyFallback;
    }

    public String getIconMaterial() {
        return iconMaterial;
    }

    public String getBackground() {
        return background;
    }
}
