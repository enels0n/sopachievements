package net.enelson.sopachievements.service;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.model.AchievementCriterion;
import net.enelson.sopachievements.model.AchievementDefinition;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Map;

public final class AchievementProgressService {

    private final SopAchievementsPlugin plugin;

    public AchievementProgressService(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    public void increment(Player player, AchievementDefinition definition, int amount) {
        increment(player, definition, AchievementDefinition.DEFAULT_CRITERION_ID, amount);
    }

    public void increment(Player player, AchievementDefinition definition, String criterionId, int amount) {
        if (isAwarded(player, definition)) {
            return;
        }
        AchievementCriterion criterion = findCriterion(definition, criterionId);
        if (criterion == null) {
            return;
        }
        int current = getProgress(player, definition, criterionId);
        int target = criterion.getTrigger().getInt("amount", 1);
        int next = Math.max(0, current + amount);
        setProgress(player, definition, criterionId, next);
        if (next >= target && meetsRequirements(player, definition)) {
            award(player, definition);
        }
    }

    public void award(Player player, AchievementDefinition definition) {
        Advancement advancement = plugin.getAdvancementRegistry().getAdvancement(definition.getId());
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        if (progress.isDone()) {
            setProgress(player, definition, definition.getTrigger().getInt("amount", 1));
            return;
        }
        for (String criteria : progress.getRemainingCriteria()) {
            progress.awardCriteria(criteria);
        }
        for (AchievementCriterion criterion : definition.getEffectiveCriteria()) {
            setProgress(player, definition, criterion.getId(), criterion.getTrigger().getInt("amount", 1));
        }
        plugin.getRewardService().give(player, definition);
    }

    public boolean isAwarded(Player player, AchievementDefinition definition) {
        Advancement advancement = plugin.getAdvancementRegistry().getAdvancement(definition.getId());
        if (advancement == null) {
            return false;
        }
        return player.getAdvancementProgress(advancement).isDone();
    }

    public int getProgress(Player player, AchievementDefinition definition) {
        return getProgress(player, definition, AchievementDefinition.DEFAULT_CRITERION_ID);
    }

    public int getProgress(Player player, AchievementDefinition definition, String criterionId) {
        Integer value = player.getPersistentDataContainer().get(progressKey(definition, criterionId), PersistentDataType.INTEGER);
        return value == null ? 0 : value;
    }

    public void setProgress(Player player, AchievementDefinition definition, int value) {
        setProgress(player, definition, AchievementDefinition.DEFAULT_CRITERION_ID, value);
    }

    public void setProgress(Player player, AchievementDefinition definition, String criterionId, int value) {
        player.getPersistentDataContainer().set(progressKey(definition, criterionId), PersistentDataType.INTEGER, value);
    }

    public void ensureAwardConsistency(AchievementDefinition definition, Map<String, AchievementDefinition> allDefinitions) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isAwarded(player, definition)) {
                for (AchievementCriterion criterion : definition.getEffectiveCriteria()) {
                    setProgress(player, definition, criterion.getId(), criterion.getTrigger().getInt("amount", 1));
                }
            }
        }
    }

    private boolean meetsRequirements(Player player, AchievementDefinition definition) {
        if (definition.isAnyCriteriaMode()) {
            for (AchievementCriterion criterion : definition.getEffectiveCriteria()) {
                if (getProgress(player, definition, criterion.getId()) >= criterion.getTrigger().getInt("amount", 1)) {
                    return true;
                }
            }
            return false;
        }
        for (AchievementCriterion criterion : definition.getEffectiveCriteria()) {
            if (getProgress(player, definition, criterion.getId()) < criterion.getTrigger().getInt("amount", 1)) {
                return false;
            }
        }
        return true;
    }

    private AchievementCriterion findCriterion(AchievementDefinition definition, String criterionId) {
        for (AchievementCriterion criterion : definition.getEffectiveCriteria()) {
            if (criterion.getId().equalsIgnoreCase(criterionId)) {
                return criterion;
            }
        }
        return null;
    }

    private NamespacedKey progressKey(AchievementDefinition definition, String criterionId) {
        return new NamespacedKey(plugin, "progress_" + definition.getId() + "_" + criterionId);
    }
}
