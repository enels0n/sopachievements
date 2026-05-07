package net.enelson.sopachievements.config;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.model.AchievementCategory;
import net.enelson.sopachievements.model.AchievementDefinition;
import net.enelson.sopachievements.model.AchievementRegistryModel;
import net.enelson.sopachievements.model.AchievementTrigger;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

public final class AchievementConfigLoader {

    private final SopAchievementsPlugin plugin;

    public AchievementConfigLoader(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    public AchievementRegistryModel load() {
        File file = new File(plugin.getDataFolder(), "achievements.yml");
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        Map<String, AchievementCategory> categories = new LinkedHashMap<String, AchievementCategory>();
        ConfigurationSection categoriesSection = config.getConfigurationSection("categories");
        if (categoriesSection != null) {
            for (String id : categoriesSection.getKeys(false)) {
                ConfigurationSection section = categoriesSection.getConfigurationSection(id);
                if (section == null) {
                    continue;
                }
                categories.put(id, new AchievementCategory(
                        id,
                        section.getString("title", id),
                        section.getString("description", ""),
                        section.getString("icon", "STONE"),
                        section.getString("background", "minecraft:textures/gui/advancements/backgrounds/stone.png")
                ));
            }
        }

        Map<String, AchievementDefinition> achievements = new LinkedHashMap<String, AchievementDefinition>();
        ConfigurationSection achievementsSection = config.getConfigurationSection("achievements");
        if (achievementsSection != null) {
            for (String id : achievementsSection.getKeys(false)) {
                ConfigurationSection section = achievementsSection.getConfigurationSection(id);
                if (section == null) {
                    continue;
                }
                ConfigurationSection triggerSection = section.getConfigurationSection("trigger");
                Map<String, Object> settings = new LinkedHashMap<String, Object>();
                if (triggerSection != null) {
                    for (String key : triggerSection.getKeys(false)) {
                        settings.put(key, triggerSection.get(key));
                    }
                }
                AchievementTrigger trigger = new AchievementTrigger(
                        triggerSection == null ? "join" : triggerSection.getString("type", "join"),
                        settings
                );
                achievements.put(id, new AchievementDefinition(
                        id,
                        section.getString("category", "main"),
                        section.getString("title", id),
                        section.getString("description", ""),
                        section.getString("icon", "STONE"),
                        section.getString("frame", "task"),
                        section.getInt("x", 0),
                        section.getInt("y", 0),
                        section.getString("parent", ""),
                        section.getBoolean("announce-to-chat", true),
                        section.getBoolean("toast", true),
                        section.getBoolean("hidden", false),
                        trigger
                ));
            }
        }
        return new AchievementRegistryModel(categories, achievements);
    }
}
