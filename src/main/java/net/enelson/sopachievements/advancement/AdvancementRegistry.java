package net.enelson.sopachievements.advancement;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.model.AchievementRegistryModel;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

public final class AdvancementRegistry {

    private final SopAchievementsPlugin plugin;

    public AdvancementRegistry(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload(AchievementRegistryModel model) {
        // Datapack-only mode: advancements are loaded by Minecraft itself.
        // We intentionally do not register or remove anything through Bukkit unsafe APIs here.
    }

    public NamespacedKey key(String id) {
        return new NamespacedKey(plugin.getConfig().getString("settings.namespace", "sopachievements"), id);
    }

    public Advancement getAdvancement(String id) {
        return Bukkit.getAdvancement(key(id));
    }
}
