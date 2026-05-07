package net.enelson.sopachievements.service;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.model.AchievementDefinition;
import net.enelson.sopachievements.model.AchievementRegistryModel;
import net.enelson.sopachievements.util.ValueMatcher;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
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
            incrementIfMatches(player, definition, 1, baseContext(player, "join"));
        }
    }

    public void onBlockBreak(Player player, Material material) {
        for (AchievementDefinition definition : get(TriggerType.BREAK_BLOCK)) {
            if (materialMatches(definition, material)) {
                Map<String, String> context = with(baseContext(player, "break_block"), "material", material.name());
                incrementIfMatches(player, definition, 1, context);
            }
        }
    }

    public void onBlockPlace(Player player, Material material) {
        for (AchievementDefinition definition : get(TriggerType.BLOCK_PLACE)) {
            if (materialMatches(definition, material)) {
                Map<String, String> context = with(baseContext(player, "block_place"), "material", material.name());
                incrementIfMatches(player, definition, 1, context);
            }
        }
    }

    public void onEntityKill(Player player, EntityType entityType) {
        for (AchievementDefinition definition : get(TriggerType.KILL_ENTITY)) {
            if (entityMatches(definition, entityType)) {
                Map<String, String> context = with(baseContext(player, "kill_entity"), "entity_type", entityType.name());
                incrementIfMatches(player, definition, 1, context);
            }
        }
    }

    public void onCraft(Player player, Material material) {
        for (AchievementDefinition definition : get(TriggerType.CRAFT_ITEM)) {
            if (materialMatches(definition, material)) {
                Map<String, String> context = with(baseContext(player, "craft_item"), "material", material.name());
                incrementIfMatches(player, definition, 1, context);
            }
        }
    }

    public void onPickupItem(Player player, Material material, int amount) {
        for (AchievementDefinition definition : get(TriggerType.PICKUP_ITEM)) {
            if (materialMatches(definition, material)) {
                Map<String, String> context = with(baseContext(player, "pickup_item"), "material", material.name(), "amount", String.valueOf(amount));
                incrementIfMatches(player, definition, Math.max(1, amount), context);
            }
        }
    }

    public void onConsumeItem(Player player, Material material) {
        for (AchievementDefinition definition : get(TriggerType.CONSUME_ITEM)) {
            if (materialMatches(definition, material)) {
                Map<String, String> context = with(baseContext(player, "consume_item"), "material", material.name());
                incrementIfMatches(player, definition, 1, context);
            }
        }
    }

    public void onEnchantItem(Player player, int amount) {
        for (AchievementDefinition definition : get(TriggerType.ENCHANT_ITEM)) {
            Map<String, String> context = with(baseContext(player, "enchant_item"), "amount", String.valueOf(amount));
            incrementIfMatches(player, definition, Math.max(1, amount), context);
        }
    }

    public void onTameEntity(Player player, EntityType entityType) {
        for (AchievementDefinition definition : get(TriggerType.TAME_ENTITY)) {
            if (entityMatches(definition, entityType)) {
                Map<String, String> context = with(baseContext(player, "tame_entity"), "entity_type", entityType.name());
                incrementIfMatches(player, definition, 1, context);
            }
        }
    }

    public void onBreedEntity(Player player, EntityType entityType) {
        for (AchievementDefinition definition : get(TriggerType.BREED_ENTITY)) {
            if (entityMatches(definition, entityType)) {
                Map<String, String> context = with(baseContext(player, "breed_entity"), "entity_type", entityType.name());
                incrementIfMatches(player, definition, 1, context);
            }
        }
    }

    public void onEnterWorld(Player player, World world) {
        for (AchievementDefinition definition : get(TriggerType.ENTER_WORLD)) {
            String expectedWorld = definition.getTrigger().getString("world", definition.getTrigger().getString("value", ""));
            String expectedEnvironment = definition.getTrigger().getString("environment", definition.getTrigger().getString("value", ""));
            boolean matched = false;
            if (!expectedWorld.isEmpty() && ValueMatcher.parse(expectedWorld).matches(world.getName())) {
                matched = true;
            }
            if (!expectedEnvironment.isEmpty() && ValueMatcher.parse(expectedEnvironment).matches(world.getEnvironment().name())) {
                matched = true;
            }
            if (matched) {
                Map<String, String> context = with(baseContext(player, "enter_world"), "world", world.getName(), "environment", world.getEnvironment().name());
                incrementIfMatches(player, definition, 1, context);
            }
        }
    }

    public void onChat(Player player, String message) {
        for (AchievementDefinition definition : get(TriggerType.CHAT)) {
            String value = definition.getTrigger().getString("value", "any");
            if ("any".equalsIgnoreCase(value) || message.toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT))) {
                Map<String, String> context = with(baseContext(player, "chat"), "message", message);
                incrementIfMatches(player, definition, 1, context);
            }
        }
    }

    public void onExecuteCommand(Player player, String commandLabelWithSlash) {
        for (AchievementDefinition definition : get(TriggerType.EXECUTE_COMMAND)) {
            if (ValueMatcher.parse(definition.getTrigger().getString("value", "any")).matches(commandLabelWithSlash)) {
                Map<String, String> context = with(baseContext(player, "execute_command"), "command", commandLabelWithSlash);
                incrementIfMatches(player, definition, 1, context);
            }
        }
    }

    public void onDamageTaken(Player player, EntityDamageEvent.DamageCause cause, double damage) {
        int amount = (int) Math.ceil(Math.max(0.0D, damage));
        if (amount <= 0) {
            return;
        }
        for (AchievementDefinition definition : get(TriggerType.DAMAGE_TAKEN)) {
            if (ValueMatcher.parse(definition.getTrigger().getString("value", "any")).matches(cause.name())) {
                Map<String, String> context = with(baseContext(player, "damage_taken"), "damage_cause", cause.name(), "damage_amount", String.valueOf(amount));
                incrementIfMatches(player, definition, amount, context);
            }
        }
    }

    public void onDamageDealt(Player player, Entity target, double damage) {
        int amount = (int) Math.ceil(Math.max(0.0D, damage));
        if (amount <= 0 || target == null) {
            return;
        }
        for (AchievementDefinition definition : get(TriggerType.DAMAGE_DEALT)) {
            if (ValueMatcher.parse(definition.getTrigger().getString("value", "any")).matches(target.getType().name())) {
                Map<String, String> context = with(baseContext(player, "damage_dealt"), "entity_type", target.getType().name(), "damage_amount", String.valueOf(amount));
                incrementIfMatches(player, definition, amount, context);
            }
        }
    }

    public void onTravelDistance(Player player, double distance) {
        int amount = (int) Math.floor(Math.max(0.0D, distance));
        if (amount <= 0) {
            return;
        }
        for (AchievementDefinition definition : get(TriggerType.TRAVEL_DISTANCE)) {
            Map<String, String> context = with(baseContext(player, "travel_distance"), "distance", String.valueOf(amount));
            incrementIfMatches(player, definition, amount, context);
        }
    }

    public void onFishItem(Player player, Material material, int amount) {
        if (material == null) {
            return;
        }
        for (AchievementDefinition definition : get(TriggerType.FISH_ITEM)) {
            if (materialMatches(definition, material)) {
                Map<String, String> context = with(baseContext(player, "fish_item"), "material", material.name(), "amount", String.valueOf(amount));
                incrementIfMatches(player, definition, Math.max(1, amount), context);
            }
        }
    }

    public void onHarvest(Player player, java.util.List<ItemStack> harvestedItems) {
        if (harvestedItems == null || harvestedItems.isEmpty()) {
            return;
        }
        for (ItemStack itemStack : harvestedItems) {
            if (itemStack == null) {
                continue;
            }
            Material material = itemStack.getType();
            int amount = Math.max(1, itemStack.getAmount());
            for (AchievementDefinition definition : get(TriggerType.HARVEST)) {
                if (materialMatches(definition, material)) {
                    Map<String, String> context = with(baseContext(player, "harvest"), "material", material.name(), "amount", String.valueOf(amount));
                    incrementIfMatches(player, definition, amount, context);
                }
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
                    Map<String, String> context = with(baseContext(player, "fall_range"),
                            "fall_start_y", String.valueOf(session.startY),
                            "fall_lowest_y", String.valueOf(session.lowestY));
                    awardIfMatches(player, definition, context);
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

    private boolean materialMatches(AchievementDefinition definition, Material material) {
        String raw = definition.getTrigger().getString("value", definition.getTrigger().getString("material", "any"));
        return ValueMatcher.parse(raw).matches(material.name());
    }

    private boolean entityMatches(AchievementDefinition definition, EntityType entityType) {
        String raw = definition.getTrigger().getString("value", definition.getTrigger().getString("entity-type", "any"));
        return ValueMatcher.parse(raw).matches(entityType.name());
    }

    private Map<String, String> baseContext(Player player, String eventType) {
        return plugin.getConditionService().baseContext(player, eventType);
    }

    private Map<String, String> with(Map<String, String> original, String key, String value) {
        Map<String, String> context = new LinkedHashMap<String, String>(original);
        context.put(key, value);
        return context;
    }

    private Map<String, String> with(Map<String, String> original, String key1, String value1, String key2, String value2) {
        Map<String, String> context = with(original, key1, value1);
        context.put(key2, value2);
        return context;
    }

    private void incrementIfMatches(Player player, AchievementDefinition definition, int amount, Map<String, String> context) {
        if (plugin.getConditionService().matches(player, definition, context)) {
            plugin.getProgressService().increment(player, definition, amount);
        }
    }

    private void awardIfMatches(Player player, AchievementDefinition definition, Map<String, String> context) {
        if (plugin.getConditionService().matches(player, definition, context)) {
            plugin.getProgressService().award(player, definition);
        }
    }

    private enum TriggerType {
        JOIN,
        BREAK_BLOCK,
        BLOCK_PLACE,
        KILL_ENTITY,
        CRAFT_ITEM,
        FALL_RANGE,
        PICKUP_ITEM,
        CONSUME_ITEM,
        ENCHANT_ITEM,
        TAME_ENTITY,
        BREED_ENTITY,
        ENTER_WORLD,
        CHAT,
        EXECUTE_COMMAND,
        DAMAGE_TAKEN,
        DAMAGE_DEALT,
        TRAVEL_DISTANCE,
        FISH_ITEM,
        HARVEST;

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
