package net.enelson.sopachievements.service;

import net.enelson.sopachievements.SopAchievementsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatColor;

import java.lang.reflect.Method;
import java.util.Map;

public final class MessageService {

    private final SopAchievementsPlugin plugin;
    private final boolean papiAvailable;

    public MessageService(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
        this.papiAvailable = Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null;
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
        if (sender instanceof Player && papiAvailable) {
            result = applyPapi((Player) sender, result);
        }
        return ChatColor.translateAlternateColorCodes('&', result);
    }

    private String applyPapi(Player player, String text) {
        try {
            Class<?> clazz = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method method = clazz.getMethod("setPlaceholders", Player.class, String.class);
            Object result = method.invoke(null, player, text);
            return result == null ? text : String.valueOf(result);
        } catch (Throwable ignored) {
            return text;
        }
    }

    private String withPrefix(String message) {
        return plugin.getConfig().getString("messages.prefix", "") + message;
    }
}
