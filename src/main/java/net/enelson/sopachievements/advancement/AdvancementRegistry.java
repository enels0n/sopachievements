package net.enelson.sopachievements.advancement;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.model.AchievementCategory;
import net.enelson.sopachievements.model.AchievementDefinition;
import net.enelson.sopachievements.model.AchievementRegistryModel;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.advancement.Advancement;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public final class AdvancementRegistry {

    private final SopAchievementsPlugin plugin;
    private final Set<NamespacedKey> loadedKeys = new HashSet<NamespacedKey>();

    public AdvancementRegistry(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload(AchievementRegistryModel model) {
        if (plugin.getConfig().getBoolean("settings.remove-missing-on-reload", true)) {
            for (NamespacedKey key : new HashSet<NamespacedKey>(loadedKeys)) {
                removeAdvancement(key);
            }
            loadedKeys.clear();
        }

        String namespace = plugin.getConfig().getString("settings.namespace", "sopachievements");
        for (Map.Entry<String, AchievementDefinition> entry : model.getAchievements().entrySet()) {
            AchievementDefinition definition = entry.getValue();
            AchievementCategory category = model.getCategories().get(definition.getCategoryId());
            if (category == null) {
                continue;
            }
            NamespacedKey key = new NamespacedKey(namespace, definition.getId());
            String json = AdvancementJsonBuilder.build(definition, category, namespace);
            if (loadAdvancement(key, json) != null) {
                loadedKeys.add(key);
            } else {
                plugin.getMessageService().send(Bukkit.getConsoleSender(), "advancement-load-failed",
                        java.util.Collections.singletonMap("id", definition.getId()));
            }
        }
    }

    public NamespacedKey key(String id) {
        return new NamespacedKey(plugin.getConfig().getString("settings.namespace", "sopachievements"), id);
    }

    public Advancement getAdvancement(String id) {
        return Bukkit.getAdvancement(key(id));
    }

    private Advancement loadAdvancement(NamespacedKey key, String json) {
        try {
            Method method = Bukkit.getUnsafe().getClass().getMethod("loadAdvancement", NamespacedKey.class, String.class);
            return (Advancement) method.invoke(Bukkit.getUnsafe(), key, json);
        } catch (Exception exception) {
            try {
                return Bukkit.getAdvancement(key);
            } catch (Exception ignored) {
                return null;
            }
        }
    }

    private void removeAdvancement(NamespacedKey key) {
        try {
            Method method = Bukkit.getUnsafe().getClass().getMethod("removeAdvancement", NamespacedKey.class);
            method.invoke(Bukkit.getUnsafe(), key);
        } catch (Exception ignored) {
        }
    }
}
