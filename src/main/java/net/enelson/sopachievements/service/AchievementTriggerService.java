package net.enelson.sopachievements.service;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.model.AchievementDefinition;
import net.enelson.sopachievements.model.AchievementRegistryModel;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public final class AchievementTriggerService {

    private final SopAchievementsPlugin plugin;
    private final Map<TriggerType, List<AchievementDefinition>> indexed = new EnumMap<TriggerType, List<AchievementDefinition>>(TriggerType.class);
    private final Map<String, FallSession> fallSessions = new HashMap<String, FallSession>();

    public AchievementTriggerService(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload(AchievementRegistryModel model) {
        indexed.clear();
        for (TriggerType type : TriggerType.values()) {
            indexed.put(type, new ArrayList<AchievementDefinition>());
        }
        for (AchievementDefinition definition : model.getAchievements().values()) {
            TriggerType type = TriggerType.from(definition.getTrigger().getType());
            if (type != null) {
                indexed.get(type).add(definition);
            }
        }
        fallSessions.clear();
    }

    public void onJoin(Player player) {
        for (AchievementDefinition definition : get(TriggerType.JOIN)) {
            plugin.getProgressService().increment(player, definition, 1);
        }
    }

    public void onBlockBreak(Player player, Material material) {
        for (AchievementDefinition definition : get(TriggerType.BREAK_BLOCK)) {
            Material expected = Material.matchMaterial(definition.getTrigger().getString("material", ""));
            if (expected != null && expected == material) {
                plugin.getProgressService().increment(player, definition, 1);
            }
        }
    }

    public void onEntityKill(Player player, EntityType entityType) {
        for (AchievementDefinition definition : get(TriggerType.KILL_ENTITY)) {
            String expected = definition.getTrigger().getString("entity-type", "");
            if (entityType.name().equalsIgnoreCase(expected)) {
                plugin.getProgressService().increment(player, definition, 1);
            }
        }
    }

    public void onCraft(Player player, Material material) {
        for (AchievementDefinition definition : get(TriggerType.CRAFT_ITEM)) {
            Material expected = Material.matchMaterial(definition.getTrigger().getString("material", ""));
            if (expected != null && expected == material) {
                plugin.getProgressService().increment(player, definition, 1);
            }
        }
    }

    public void onMove(Player player, double fromY, double toY, boolean onGroundNow) {
        if (get(TriggerType.FALL_RANGE).isEmpty()) {
            return;
        }
        String key = player.getUniqueId().toString();
        FallSession session = fallSessions.get(key);
        boolean descending = toY < fromY;

        if (descending && session == null && !player.isFlying() && !player.isInsideVehicle()) {
            session = new FallSession(player.getWorld().getName(), fromY);
            fallSessions.put(key, session);
        }

        if (session == null) {
            return;
        }

        if (!player.getWorld().getName().equalsIgnoreCase(session.worldName)) {
            fallSessions.remove(key);
            return;
        }

        if (toY > session.startY) {
            session.startY = toY;
        }
        if (toY < session.lowestY) {
            session.lowestY = toY;
        }

        if (player.isFlying() || player.isInsideVehicle()) {
            fallSessions.remove(key);
            return;
        }

        if (onGroundNow) {
            for (AchievementDefinition definition : get(TriggerType.FALL_RANGE)) {
                int startMin = definition.getTrigger().getInt("start-y-min", 255);
                int endMax = definition.getTrigger().getInt("end-y-max", 5);
                boolean sameWorldOnly = definition.getTrigger().getBoolean("same-world-only", true);
                boolean denyFlight = definition.getTrigger().getBoolean("deny-flight", true);
                boolean denyVehicles = definition.getTrigger().getBoolean("deny-vehicles", true);
                boolean denyElytra = definition.getTrigger().getBoolean("deny-elytra", true);

                if (sameWorldOnly && !player.getWorld().getName().equalsIgnoreCase(session.worldName)) {
                    continue;
                }
                if (denyFlight && player.isFlying()) {
                    continue;
                }
                if (denyVehicles && player.isInsideVehicle()) {
                    continue;
                }
                if (denyElytra && player.isGliding()) {
                    continue;
                }
                if (session.startY >= startMin && session.lowestY <= endMax) {
                    plugin.getProgressService().award(player, definition);
                }
            }
            fallSessions.remove(key);
        }
    }

    public void resetTransientState(Player player) {
        fallSessions.remove(player.getUniqueId().toString());
    }

    private List<AchievementDefinition> get(TriggerType type) {
        List<AchievementDefinition> list = indexed.get(type);
        return list == null ? Collections.<AchievementDefinition>emptyList() : list;
    }

    private enum TriggerType {
        JOIN,
        BREAK_BLOCK,
        KILL_ENTITY,
        CRAFT_ITEM,
        FALL_RANGE;

        static TriggerType from(String raw) {
            if (raw == null) {
                return null;
            }
            String normalized = raw.trim().toUpperCase(Locale.ROOT);
            try {
                return TriggerType.valueOf(normalized);
            } catch (IllegalArgumentException ignored) {
                return null;
            }
        }
    }

    private static final class FallSession {
        private final String worldName;
        private double startY;
        private double lowestY;

        private FallSession(String worldName, double startY) {
            this.worldName = worldName;
            this.startY = startY;
            this.lowestY = startY;
        }
    }
}
