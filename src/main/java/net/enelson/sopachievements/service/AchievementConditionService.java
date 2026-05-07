package net.enelson.sopachievements.service;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.model.AchievementConditionCheck;
import net.enelson.sopachievements.model.AchievementConditions;
import net.enelson.sopachievements.model.AchievementDefinition;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

public final class AchievementConditionService {

    private final SopAchievementsPlugin plugin;

    public AchievementConditionService(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean matches(Player player, AchievementDefinition definition, Map<String, String> context) {
        return matches(player, definition.getConditions(), context);
    }

    public boolean matches(Player player, AchievementConditions conditions, Map<String, String> context) {
        if (conditions == null || conditions.isEmpty()) {
            return true;
        }

        boolean anyMode = conditions.isAnyMode();
        boolean matched = !anyMode;
        for (AchievementConditionCheck check : conditions.getChecks()) {
            boolean result = test(player, check, context == null ? Collections.<String, String>emptyMap() : context);
            if (anyMode && result) {
                return true;
            }
            if (!anyMode && !result) {
                return false;
            }
            matched = result;
        }
        return matched;
    }

    public Map<String, String> baseContext(Player player, String eventType) {
        Map<String, String> context = new LinkedHashMap<String, String>();
        if (eventType != null) {
            context.put("event_type", eventType);
        }
        if (player != null) {
            context.put("player_name", player.getName());
            context.put("player_uuid", player.getUniqueId().toString());
            context.put("player_world", player.getWorld().getName());
            context.put("player_biome", player.getLocation().getBlock().getBiome().name());
            context.put("player_gamemode", player.getGameMode().name());
            context.put("player_level", String.valueOf(player.getLevel()));
            context.put("player_food", String.valueOf(player.getFoodLevel()));
            context.put("player_health", String.valueOf(player.getHealth()));
            Location location = player.getLocation();
            context.put("player_x", String.valueOf(location.getBlockX()));
            context.put("player_y", String.valueOf(location.getBlockY()));
            context.put("player_z", String.valueOf(location.getBlockZ()));
        }
        return context;
    }

    private boolean test(Player player, AchievementConditionCheck check, Map<String, String> context) {
        String input = resolve(player, check.getInput(), context);
        String output = resolve(player, check.getOutput(), context);
        String type = normalize(check.getType());

        if ("string equals".equals(type)) {
            return input.equalsIgnoreCase(output);
        }
        if ("string not equals".equals(type)) {
            return !input.equalsIgnoreCase(output);
        }
        if ("string contains".equals(type)) {
            return input.toLowerCase(Locale.ROOT).contains(output.toLowerCase(Locale.ROOT));
        }
        if ("string starts with".equals(type)) {
            return input.toLowerCase(Locale.ROOT).startsWith(output.toLowerCase(Locale.ROOT));
        }
        if ("string ends with".equals(type)) {
            return input.toLowerCase(Locale.ROOT).endsWith(output.toLowerCase(Locale.ROOT));
        }
        if ("boolean equals".equals(type)) {
            return Boolean.parseBoolean(input) == Boolean.parseBoolean(output);
        }

        Double inputNumber = toNumber(input);
        Double outputNumber = toNumber(output);
        if (inputNumber == null || outputNumber == null) {
            return false;
        }

        if ("number >=".equals(type) || ">=".equals(type)) {
            return inputNumber.doubleValue() >= outputNumber.doubleValue();
        }
        if ("number >".equals(type) || ">".equals(type)) {
            return inputNumber.doubleValue() > outputNumber.doubleValue();
        }
        if ("number <=".equals(type) || "<=".equals(type)) {
            return inputNumber.doubleValue() <= outputNumber.doubleValue();
        }
        if ("number <".equals(type) || "<".equals(type)) {
            return inputNumber.doubleValue() < outputNumber.doubleValue();
        }
        if ("number ==".equals(type) || "number equals".equals(type) || "==".equals(type) || "=".equals(type)) {
            return inputNumber.doubleValue() == outputNumber.doubleValue();
        }
        if ("number !=".equals(type) || "number not equals".equals(type) || "!=".equals(type)) {
            return inputNumber.doubleValue() != outputNumber.doubleValue();
        }
        return false;
    }

    private String resolve(Player player, String value, Map<String, String> context) {
        String resolved = value == null ? "" : value;
        for (Map.Entry<String, String> entry : context.entrySet()) {
            resolved = resolved.replace("%" + entry.getKey() + "%", entry.getValue() == null ? "" : entry.getValue());
        }
        return applyPlaceholderApi(player, resolved);
    }

    private String applyPlaceholderApi(Player player, String value) {
        if (player == null || value.indexOf('%') < 0) {
            return value;
        }
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            return value;
        }
        try {
            Class<?> placeholderApiClass = Class.forName("me.clip.placeholderapi.PlaceholderAPI");
            Method method = placeholderApiClass.getMethod("setPlaceholders", Player.class, String.class);
            Object result = method.invoke(null, player, value);
            return result == null ? value : String.valueOf(result);
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to resolve PlaceholderAPI condition value: " + exception.getMessage());
            return value;
        }
    }

    private String normalize(String type) {
        return type == null ? "" : type.trim().toLowerCase(Locale.ROOT);
    }

    private Double toNumber(String value) {
        try {
            return Double.valueOf(value.trim().replace(',', '.'));
        } catch (Exception exception) {
            return null;
        }
    }
}
