package net.enelson.sopachievements.service;

import net.enelson.sopachievements.SopAchievementsPlugin;
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
        if (isAwarded(player, definition)) {
            return;
        }
        int current = getProgress(player, definition);
        int target = definition.getTrigger().getInt("amount", 1);
        int next = Math.max(0, current + amount);
        setProgress(player, definition, next);
        if (next >= target) {
            award(player, definition);
        }
    }

    public void award(Player player, AchievementDefinition definition) {
        Advancement advancement = plugin.getAdvancementRegistry().getAdvancement(definition.getId());
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancementProgress(advancement);
        for (String criteria : progress.getRemainingCriteria()) {
            progress.awardCriteria(criteria);
        }
        setProgress(player, definition, definition.getTrigger().getInt("amount", 1));
    }

    public boolean isAwarded(Player player, AchievementDefinition definition) {
        Advancement advancement = plugin.getAdvancementRegistry().getAdvancement(definition.getId());
        if (advancement == null) {
            return false;
        }
        return player.getAdvancementProgress(advancement).isDone();
    }

    public int getProgress(Player player, AchievementDefinition definition) {
        Integer value = player.getPersistentDataContainer().get(progressKey(definition), PersistentDataType.INTEGER);
        return value == null ? 0 : value;
    }

    public void setProgress(Player player, AchievementDefinition definition, int value) {
        player.getPersistentDataContainer().set(progressKey(definition), PersistentDataType.INTEGER, value);
    }

    public void ensureAwardConsistency(AchievementDefinition definition, Map<String, AchievementDefinition> allDefinitions) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (isAwarded(player, definition)) {
                setProgress(player, definition, definition.getTrigger().getInt("amount", 1));
            }
        }
    }

    private NamespacedKey progressKey(AchievementDefinition definition) {
        return new NamespacedKey(plugin, "progress_" + definition.getId());
    }
}
