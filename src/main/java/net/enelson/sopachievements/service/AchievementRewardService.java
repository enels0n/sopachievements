package net.enelson.sopachievements.service;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.model.AchievementDefinition;
import net.enelson.sopachievements.model.AchievementRewards;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AchievementRewardService {

    private final SopAchievementsPlugin plugin;

    public AchievementRewardService(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    public void give(Player player, AchievementDefinition definition) {
        AchievementRewards rewards = definition.getRewards();
        if (rewards == null) {
            return;
        }

        Map<String, String> replacements = replacements(player, definition);
        if (rewards.hasMessage()) {
            player.sendMessage(plugin.getMessageService().resolve(player, rewards.getMessage(), replacements));
        }
        if (rewards.hasCommands()) {
            for (String rawCommand : rewards.getCommands()) {
                execute(player, rawCommand, replacements);
            }
        }
    }

    private void execute(Player player, String rawCommand, Map<String, String> replacements) {
        String command = plugin.getMessageService().resolve(player, rawCommand, replacements);
        if (command == null) {
            return;
        }
        command = command.trim();
        if (command.isEmpty()) {
            return;
        }
        if (command.startsWith("[console]")) {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command.substring("[console]".length()).trim());
            return;
        }
        if (command.startsWith("[player]")) {
            player.performCommand(command.substring("[player]".length()).trim());
            return;
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
    }

    private Map<String, String> replacements(Player player, AchievementDefinition definition) {
        Map<String, String> replacements = new LinkedHashMap<String, String>();
        replacements.put("player_name", player.getName());
        replacements.put("player_uuid", player.getUniqueId().toString());
        replacements.put("achievement_id", definition.getId());
        replacements.put("achievement_title", definition.getTitle());
        replacements.put("achievement_description", definition.getDescription());
        replacements.put("achievement_category", definition.getCategoryId());
        return replacements;
    }
}
