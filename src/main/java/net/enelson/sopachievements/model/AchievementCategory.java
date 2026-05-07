package net.enelson.sopachievements.model;

public final class AchievementCategory {

    private final String id;
    private final String title;
    private final String description;
    private final String iconMaterial;
    private final String background;

    public AchievementCategory(String id, String title, String description, String iconMaterial, String background) {
        this.id = id;
        this.title = title;
        this.description = description;
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

    public String getIconMaterial() {
        return iconMaterial;
    }

    public String getBackground() {
        return background;
    }
}
