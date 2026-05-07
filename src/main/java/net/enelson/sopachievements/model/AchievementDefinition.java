package net.enelson.sopachievements.model;

import java.util.Collections;
import java.util.List;

public final class AchievementDefinition {

    public static final String DEFAULT_CRITERION_ID = "default";

    private final String id;
    private final String categoryId;
    private final String title;
    private final String description;
    private final String nameKey;
    private final String nameKeyFallback;
    private final String loreKey;
    private final String loreKeyFallback;
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
    private final AchievementRequirements requirements;

    public AchievementDefinition(String id,
                                 String categoryId,
                                 String title,
                                 String description,
                                 String nameKey,
                                 String nameKeyFallback,
                                 String loreKey,
                                 String loreKeyFallback,
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
                                 AchievementRewards rewards,
                                 AchievementRequirements requirements) {
        this.id = id;
        this.categoryId = categoryId;
        this.title = title;
        this.description = description;
        this.nameKey = nameKey;
        this.nameKeyFallback = nameKeyFallback;
        this.loreKey = loreKey;
        this.loreKeyFallback = loreKeyFallback;
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
        this.requirements = requirements;
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

    public AchievementRequirements getRequirements() {
        return requirements;
    }

    public List<AchievementCriterion> getEffectiveCriteria() {
        if (requirements != null && !requirements.isEmpty()) {
            return requirements.getCriteria();
        }
        return Collections.singletonList(new AchievementCriterion(DEFAULT_CRITERION_ID, trigger, conditions));
    }

    public boolean isAnyCriteriaMode() {
        return requirements != null && requirements.isAnyMode();
    }

    public boolean isRoot() {
        return parentId == null || parentId.trim().isEmpty();
    }
}
