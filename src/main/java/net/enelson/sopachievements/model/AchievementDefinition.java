package net.enelson.sopachievements.model;

public final class AchievementDefinition {

    private final String id;
    private final String categoryId;
    private final String title;
    private final String description;
    private final String iconMaterial;
    private final String frame;
    private final int x;
    private final int y;
    private final String parentId;
    private final boolean announceToChat;
    private final boolean showToast;
    private final boolean hidden;
    private final AchievementTrigger trigger;
    private final AchievementConditions conditions;
    private final AchievementRewards rewards;

    public AchievementDefinition(String id,
                                 String categoryId,
                                 String title,
                                 String description,
                                 String iconMaterial,
                                 String frame,
                                 int x,
                                 int y,
                                 String parentId,
                                 boolean announceToChat,
                                 boolean showToast,
                                 boolean hidden,
                                 AchievementTrigger trigger,
                                 AchievementConditions conditions,
                                 AchievementRewards rewards) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.iconMaterial = iconMaterial;
        this.frame = frame;
        this.x = x;
        this.y = y;
        this.parentId = parentId;
        this.announceToChat = announceToChat;
        this.showToast = showToast;
        this.hidden = hidden;
        this.trigger = trigger;
        this.conditions = conditions;
        this.rewards = rewards;
    }

    public String getId() {
        return id;
    }

    public String getCategoryId() {
        return categoryId;
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

    public String getFrame() {
        return frame;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getParentId() {
        return parentId;
    }

    public boolean isAnnounceToChat() {
        return announceToChat;
    }

    public boolean isShowToast() {
        return showToast;
    }

    public boolean isHidden() {
        return hidden;
    }

    public AchievementTrigger getTrigger() {
        return trigger;
    }

    public AchievementConditions getConditions() {
        return conditions;
    }

    public AchievementRewards getRewards() {
        return rewards;
    }

    public boolean isRoot() {
        return parentId == null || parentId.trim().isEmpty();
    }
}
