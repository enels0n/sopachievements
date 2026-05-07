package net.enelson.sopachievements.service;

import me.clip.placeholderapi.PlaceholderAPI;
import net.md_5.bungee.api.ChatColor;
import net.enelson.sopachievements.SopAchievementsPlugin;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Map;

public final class MessageService {

    private final SopAchievementsPlugin plugin;

    public MessageService(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    public void send(CommandSender sender, String path) {
        send(sender, path, null);
    }

    public void send(CommandSender sender, String path, Map<String, String> replacements) {
        sender.sendMessage(resolve(sender, withPrefix(get(path)), replacements));
    }

    public String get(String path) {
        return plugin.getConfig().getString("messages." + path, "&cMissing message: " + path);
    }

    public String resolve(CommandSender sender, String input, Map<String, String> replacements) {
        String result = input == null ? "" : input;
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                result = result.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        if (sender instanceof Player) {
            result = PlaceholderAPI.setPlaceholders((Player) sender, result);
        }
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    private String withPrefix(String message) {
        return plugin.getConfig().getString("messages.prefix", "") + message;
    }
}
