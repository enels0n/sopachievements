package net.enelson.sopachievements.service;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.model.AchievementCriterion;
import net.enelson.sopachievements.model.AchievementDefinition;
import net.enelson.sopachievements.model.AchievementRegistryModel;
import net.enelson.sopachievements.util.ValueMatcher;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.block.Biome;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.World;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

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
    private final Map<TriggerType, List<CriterionBinding>> indexed = new EnumMap<TriggerType, List<CriterionBinding>>(TriggerType.class);
    private final Map<String, FallSession> fallSessions = new HashMap<String, FallSession>();
    private final Map<String, BiomeStaySession> biomeStaySessions = new HashMap<String, BiomeStaySession>();

    public AchievementTriggerService(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    public void reload(AchievementRegistryModel model) {
        indexed.clear();
        for (TriggerType type : TriggerType.values()) {
            indexed.put(type, new ArrayList<CriterionBinding>());
        }
        for (AchievementDefinition definition : model.getAchievements().values()) {
            for (AchievementCriterion criterion : definition.getEffectiveCriteria()) {
                TriggerType type = TriggerType.from(criterion.getTrigger().getType());
                if (type != null) {
                    indexed.get(type).add(new CriterionBinding(definition, criterion));
                }
            }
        }
        fallSessions.clear();
        biomeStaySessions.clear();
    }

    public void onJoin(Player player) {
        for (CriterionBinding binding : get(TriggerType.JOIN)) {
            incrementIfMatches(player, binding, 1, baseContext(player, "join"));
        }
    }

    public void onPlayerDeath(Player player, String cause) {
        for (CriterionBinding binding : get(TriggerType.DEATH)) {
            if (ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(cause)) {
                Map<String, String> context = with(baseContext(player, "death"), "death_cause", cause);
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onArmorlessDeath(Player player, String cause) {
        if (!isArmorless(player)) {
            return;
        }
        for (CriterionBinding binding : get(TriggerType.ARMORLESS_DEATH)) {
            if (ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(cause)) {
                Map<String, String> context = with(baseContext(player, "armorless_death"), "death_cause", cause);
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onLightningStrike(Player player) {
        for (CriterionBinding binding : get(TriggerType.LIGHTNING_STRIKE)) {
            incrementIfMatches(player, binding, 1, baseContext(player, "lightning_strike"));
        }
    }

    public void onSleep(Player player) {
        for (CriterionBinding binding : get(TriggerType.SLEEP)) {
            incrementIfMatches(player, binding, 1, baseContext(player, "sleep"));
        }
    }

    public void onTotemUse(Player player) {
        for (CriterionBinding binding : get(TriggerType.TOTEM_USE)) {
            incrementIfMatches(player, binding, 1, baseContext(player, "totem_use"));
        }
    }

    public void onRaidWin(Player player) {
        for (CriterionBinding binding : get(TriggerType.RAID_WIN)) {
            incrementIfMatches(player, binding, 1, baseContext(player, "raid_win"));
        }
    }

    public void onBucket(Player player, Material material, String action) {
        for (CriterionBinding binding : get(TriggerType.BUCKET)) {
            if (materialMatches(binding.criterion, material)) {
                Map<String, String> context = with(baseContext(player, "bucket"), "material", material.name(), "bucket_action", action);
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onVillagerTrade(Player player, Material result, int amount, String profession, String level) {
        for (CriterionBinding binding : get(TriggerType.VILLAGER_TRADE)) {
            if (materialMatches(binding.criterion, result)) {
                Map<String, String> context = with(baseContext(player, "villager_trade"), "material", result.name(), "amount", String.valueOf(amount));
                context.put("villager_profession", profession);
                context.put("villager_level", level);
                incrementIfMatches(player, binding, Math.max(1, amount), context);
            }
        }
    }

    public void onVillagerTradeProfession(Player player, Material result, int amount, String profession, String level) {
        for (CriterionBinding binding : get(TriggerType.VILLAGER_TRADE_PROFESSION)) {
            if (!materialMatches(binding.criterion, result)) {
                continue;
            }
            if (!ValueMatcher.parse(binding.criterion.getTrigger().getString("profession", "any")).matches(profession)) {
                continue;
            }
            if (!ValueMatcher.parse(binding.criterion.getTrigger().getString("level", "any")).matches(level)) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "villager_trade_profession"), "material", result.name(), "amount", String.valueOf(amount));
            context.put("villager_profession", profession);
            context.put("villager_level", level);
            incrementIfMatches(player, binding, Math.max(1, amount), context);
        }
    }

    public void onLootContainer(Player player, String inventoryType) {
        for (CriterionBinding binding : get(TriggerType.LOOT_CONTAINER)) {
            if (ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(inventoryType)) {
                Map<String, String> context = with(baseContext(player, "loot_container"), "inventory_type", inventoryType);
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onPotionEffect(Player player, PotionEffectType effectType, int amplifier) {
        if (effectType == null) {
            return;
        }
        for (CriterionBinding binding : get(TriggerType.POTION_EFFECT)) {
            if (!ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(effectType.getName())) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "potion_effect"),
                    "effect_type", effectType.getName(),
                    "effect_level", String.valueOf(amplifier + 1));
            incrementIfMatches(player, binding, 1, context);
        }
    }

    public void onEquipSync(Player player) {
        for (CriterionBinding binding : get(TriggerType.EQUIP)) {
            Material material = materialFrom(binding.criterion.getTrigger().getString("material", binding.criterion.getTrigger().getString("value", "")));
            if (material == null) {
                continue;
            }
            int count = countEquipped(player.getInventory(), material);
            if (count <= 0) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "equip"), "material", material.name(), "amount", String.valueOf(count));
            syncAbsoluteIfMatches(player, binding, count, context);
        }
    }

    public void onAdvancementDone(Player player, String advancementKey) {
        for (CriterionBinding binding : get(TriggerType.PLAYER_ADVANCEMENT)) {
            if (ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(advancementKey)) {
                Map<String, String> context = with(baseContext(player, "player_advancement"), "advancement", advancementKey);
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onBlockBreak(Player player, Material material) {
        for (CriterionBinding binding : get(TriggerType.BREAK_BLOCK)) {
            if (materialMatches(binding.criterion, material)) {
                Map<String, String> context = with(baseContext(player, "break_block"), "material", material.name());
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onBlockPlace(Player player, Material material) {
        for (CriterionBinding binding : get(TriggerType.BLOCK_PLACE)) {
            if (materialMatches(binding.criterion, material)) {
                Map<String, String> context = with(baseContext(player, "block_place"), "material", material.name());
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onEntityKill(Player player, EntityType entityType) {
        for (CriterionBinding binding : get(TriggerType.KILL_ENTITY)) {
            if (entityMatches(binding.criterion, entityType)) {
                Map<String, String> context = with(baseContext(player, "kill_entity"), "entity_type", entityType.name());
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onProjectileKill(Player player, EntityType entityType, EntityType projectileType) {
        for (CriterionBinding binding : get(TriggerType.PROJECTILE_KILL)) {
            if (!entityMatches(binding.criterion, entityType)) {
                continue;
            }
            String projectileRule = binding.criterion.getTrigger().getString("projectile", "any");
            if (!ValueMatcher.parse(projectileRule).matches(projectileType.name())) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "projectile_kill"),
                    "entity_type", entityType.name(),
                    "projectile_type", projectileType.name());
            incrementIfMatches(player, binding, 1, context);
        }
    }

    public void onCraft(Player player, Material material) {
        for (CriterionBinding binding : get(TriggerType.CRAFT_ITEM)) {
            if (materialMatches(binding.criterion, material)) {
                Map<String, String> context = with(baseContext(player, "craft_item"), "material", material.name());
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onSmeltItem(Player player, Material material, int amount) {
        for (CriterionBinding binding : get(TriggerType.SMELT_ITEM)) {
            if (materialMatches(binding.criterion, material)) {
                Map<String, String> context = with(baseContext(player, "smelt_item"), "material", material.name(), "amount", String.valueOf(amount));
                incrementIfMatches(player, binding, Math.max(1, amount), context);
            }
        }
    }

    public void onBrewItem(Player player, Material material, int amount) {
        for (CriterionBinding binding : get(TriggerType.BREW_ITEM)) {
            if (materialMatches(binding.criterion, material)) {
                Map<String, String> context = with(baseContext(player, "brew_item"), "material", material.name(), "amount", String.valueOf(amount));
                incrementIfMatches(player, binding, Math.max(1, amount), context);
            }
        }
    }

    public void onPickupItem(Player player, Material material, int amount) {
        for (CriterionBinding binding : get(TriggerType.PICKUP_ITEM)) {
            if (materialMatches(binding.criterion, material)) {
                Map<String, String> context = with(baseContext(player, "pickup_item"), "material", material.name(), "amount", String.valueOf(amount));
                incrementIfMatches(player, binding, Math.max(1, amount), context);
            }
        }
    }

    public void onConsumeItem(Player player, Material material) {
        for (CriterionBinding binding : get(TriggerType.CONSUME_ITEM)) {
            if (materialMatches(binding.criterion, material)) {
                Map<String, String> context = with(baseContext(player, "consume_item"), "material", material.name());
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onEnchantItem(Player player, int amount) {
        for (CriterionBinding binding : get(TriggerType.ENCHANT_ITEM)) {
            Map<String, String> context = with(baseContext(player, "enchant_item"), "amount", String.valueOf(amount));
            incrementIfMatches(player, binding, Math.max(1, amount), context);
        }
    }

    public void onTameEntity(Player player, EntityType entityType) {
        for (CriterionBinding binding : get(TriggerType.TAME_ENTITY)) {
            if (entityMatches(binding.criterion, entityType)) {
                Map<String, String> context = with(baseContext(player, "tame_entity"), "entity_type", entityType.name());
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onBreedEntity(Player player, EntityType entityType) {
        for (CriterionBinding binding : get(TriggerType.BREED_ENTITY)) {
            if (entityMatches(binding.criterion, entityType)) {
                Map<String, String> context = with(baseContext(player, "breed_entity"), "entity_type", entityType.name());
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onEnterWorld(Player player, World world) {
        for (CriterionBinding binding : get(TriggerType.ENTER_WORLD)) {
            String expectedWorld = binding.criterion.getTrigger().getString("world", binding.criterion.getTrigger().getString("value", ""));
            String expectedEnvironment = binding.criterion.getTrigger().getString("environment", binding.criterion.getTrigger().getString("value", ""));
            boolean matched = false;
            if (!expectedWorld.isEmpty() && ValueMatcher.parse(expectedWorld).matches(world.getName())) {
                matched = true;
            }
            if (!expectedEnvironment.isEmpty() && ValueMatcher.parse(expectedEnvironment).matches(world.getEnvironment().name())) {
                matched = true;
            }
            if (matched) {
                Map<String, String> context = with(baseContext(player, "enter_world"), "world", world.getName(), "environment", world.getEnvironment().name());
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onChat(Player player, String message) {
        for (CriterionBinding binding : get(TriggerType.CHAT)) {
            String value = binding.criterion.getTrigger().getString("value", "any");
            if ("any".equalsIgnoreCase(value) || message.toLowerCase(Locale.ROOT).contains(value.toLowerCase(Locale.ROOT))) {
                Map<String, String> context = with(baseContext(player, "chat"), "message", message);
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onExecuteCommand(Player player, String commandLabelWithSlash) {
        for (CriterionBinding binding : get(TriggerType.EXECUTE_COMMAND)) {
            if (ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(commandLabelWithSlash)) {
                Map<String, String> context = with(baseContext(player, "execute_command"), "command", commandLabelWithSlash);
                incrementIfMatches(player, binding, 1, context);
            }
        }
    }

    public void onDamageTaken(Player player, EntityDamageEvent.DamageCause cause, double damage) {
        int amount = (int) Math.ceil(Math.max(0.0D, damage));
        if (amount <= 0) {
            return;
        }
        for (CriterionBinding binding : get(TriggerType.DAMAGE_TAKEN)) {
            if (ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(cause.name())) {
                Map<String, String> context = with(baseContext(player, "damage_taken"), "damage_cause", cause.name(), "damage_amount", String.valueOf(amount));
                incrementIfMatches(player, binding, amount, context);
            }
        }
    }

    public void onDamageDealt(Player player, Entity target, double damage) {
        int amount = (int) Math.ceil(Math.max(0.0D, damage));
        if (amount <= 0 || target == null) {
            return;
        }
        for (CriterionBinding binding : get(TriggerType.DAMAGE_DEALT)) {
            if (ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(target.getType().name())) {
                Map<String, String> context = with(baseContext(player, "damage_dealt"), "entity_type", target.getType().name(), "damage_amount", String.valueOf(amount));
                incrementIfMatches(player, binding, amount, context);
            }
        }
    }

    public void onTravelDistance(Player player, double distance) {
        int amount = (int) Math.floor(Math.max(0.0D, distance));
        if (amount <= 0) {
            return;
        }
        for (CriterionBinding binding : get(TriggerType.TRAVEL_DISTANCE)) {
            Map<String, String> context = with(baseContext(player, "travel_distance"), "distance", String.valueOf(amount));
            incrementIfMatches(player, binding, amount, context);
        }
    }

    public void onFishItem(Player player, Material material, int amount) {
        if (material == null) {
            return;
        }
        for (CriterionBinding binding : get(TriggerType.FISH_ITEM)) {
            if (materialMatches(binding.criterion, material)) {
                Map<String, String> context = with(baseContext(player, "fish_item"), "material", material.name(), "amount", String.valueOf(amount));
                incrementIfMatches(player, binding, Math.max(1, amount), context);
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
            for (CriterionBinding binding : get(TriggerType.HARVEST)) {
                if (materialMatches(binding.criterion, material)) {
                    Map<String, String> context = with(baseContext(player, "harvest"), "material", material.name(), "amount", String.valueOf(amount));
                    incrementIfMatches(player, binding, amount, context);
                }
            }
        }
    }

    public void onStatisticSync(Player player) {
        for (CriterionBinding binding : get(TriggerType.STATISTIC)) {
            Statistic statistic = resolveStatistic(binding.criterion);
            if (statistic == null) {
                continue;
            }
            Integer progress = resolveStatisticValue(player, binding.criterion, statistic);
            if (progress == null || progress.intValue() <= 0) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "statistic"),
                    "statistic", statistic.name(),
                    "amount", String.valueOf(progress.intValue()));
            syncAbsoluteIfMatches(player, binding, progress.intValue(), context);
        }
    }

    public void onInventorySync(Player player) {
        PlayerInventory inventory = player.getInventory();
        for (CriterionBinding binding : get(TriggerType.INVENTORY_CONTAINS)) {
            Material material = materialFrom(binding.criterion.getTrigger().getString("material", binding.criterion.getTrigger().getString("value", "")));
            if (material == null) {
                continue;
            }
            int count = countItems(inventory, material);
            if (count <= 0) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "inventory_contains"), "material", material.name(), "amount", String.valueOf(count));
            syncAbsoluteIfMatches(player, binding, count, context);
        }
    }

    public void onLocationRangeSync(Player player) {
        for (CriterionBinding binding : get(TriggerType.LOCATION_RANGE)) {
            if (!isWithinRange(player, binding.criterion)) {
                continue;
            }
            Map<String, String> context = baseContext(player, "location_range");
            incrementIfMatches(player, binding, 1, context);
        }
    }

    public void onUnderwaterSync(Player player) {
        if (!player.getEyeLocation().getBlock().isLiquid()) {
            return;
        }
        int seconds = Math.max(1, plugin.getConfig().getInt("settings.sync-check-interval-ticks",
                plugin.getConfig().getInt("settings.statistic-check-interval-ticks", 100)) / 20);
        for (CriterionBinding binding : get(TriggerType.UNDERWATER_TIME)) {
            Map<String, String> context = with(baseContext(player, "underwater_time"), "time_seconds", String.valueOf(seconds));
            incrementIfMatches(player, binding, seconds, context);
        }
    }

    public void onWeatherSync(Player player) {
        String weather = resolveWeather(player.getWorld());
        for (CriterionBinding binding : get(TriggerType.WEATHER)) {
            if (!ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(weather)) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "weather"), "weather", weather);
            incrementIfMatches(player, binding, 1, context);
        }
    }

    public void onMoonPhaseSync(Player player) {
        if (player.getWorld().getEnvironment() != World.Environment.NORMAL) {
            return;
        }
        String moonPhase = resolveMoonPhase(player.getWorld());
        for (CriterionBinding binding : get(TriggerType.MOON_PHASE)) {
            if (!ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(moonPhase)) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "moon_phase"), "moon_phase", moonPhase);
            incrementIfMatches(player, binding, 1, context);
        }
    }

    public void onTimeWindowSync(Player player) {
        long time = player.getWorld().getTime();
        for (CriterionBinding binding : get(TriggerType.TIME_WINDOW)) {
            long min = binding.criterion.getTrigger().getInt("time-min", 0);
            long max = binding.criterion.getTrigger().getInt("time-max", 23999);
            if (!isWithinTimeWindow(time, min, max)) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "time_window"), "world_time", String.valueOf(time));
            incrementIfMatches(player, binding, 1, context);
        }
    }

    public void onEffectComboSync(Player player) {
        String activeEffects = activeEffects(player);
        for (CriterionBinding binding : get(TriggerType.EFFECT_COMBO)) {
            if (!hasAllEffects(player, binding.criterion.getTrigger().getString("value", ""))) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "effect_combo"), "active_effects", activeEffects);
            incrementIfMatches(player, binding, 1, context);
        }
    }

    public void onMove(Player player, double fromY, double toY, boolean onGroundNow) {
        tickBiomeStay(player);
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
            for (CriterionBinding binding : get(TriggerType.FALL_RANGE)) {
                int startMin = binding.criterion.getTrigger().getInt("start-y-min", 255);
                int endMax = binding.criterion.getTrigger().getInt("end-y-max", 5);
                boolean sameWorldOnly = binding.criterion.getTrigger().getBoolean("same-world-only", true);
                boolean denyFlight = binding.criterion.getTrigger().getBoolean("deny-flight", true);
                boolean denyVehicles = binding.criterion.getTrigger().getBoolean("deny-vehicles", true);
                boolean denyElytra = binding.criterion.getTrigger().getBoolean("deny-elytra", true);

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
                    awardIfMatches(player, binding, context);
                }
            }
            fallSessions.remove(key);
        }
    }

    public void resetTransientState(Player player) {
        fallSessions.remove(player.getUniqueId().toString());
        biomeStaySessions.remove(player.getUniqueId().toString());
    }

    private List<CriterionBinding> get(TriggerType type) {
        List<CriterionBinding> list = indexed.get(type);
        return list == null ? Collections.<CriterionBinding>emptyList() : list;
    }

    private boolean materialMatches(AchievementCriterion criterion, Material material) {
        String raw = criterion.getTrigger().getString("value", criterion.getTrigger().getString("material", "any"));
        return ValueMatcher.parse(raw).matches(material.name());
    }

    private boolean entityMatches(AchievementCriterion criterion, EntityType entityType) {
        String raw = criterion.getTrigger().getString("value", criterion.getTrigger().getString("entity-type", "any"));
        return ValueMatcher.parse(raw).matches(entityType.name());
    }

    private Statistic resolveStatistic(AchievementCriterion criterion) {
        String raw = criterion.getTrigger().getString("statistic", criterion.getTrigger().getString("value", ""));
        if (raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Statistic.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Integer resolveStatisticValue(Player player, AchievementCriterion criterion, Statistic statistic) {
        try {
            Type statisticType = Type.fromName(statistic.getType().name());
            if (statisticType == Type.BLOCK) {
                Material material = Material.matchMaterial(criterion.getTrigger().getString("material", ""));
                return material == null ? null : Integer.valueOf(player.getStatistic(statistic, material));
            }
            if (statisticType == Type.ITEM) {
                Material material = Material.matchMaterial(criterion.getTrigger().getString("material", ""));
                return material == null ? null : Integer.valueOf(player.getStatistic(statistic, material));
            }
            if (statisticType == Type.ENTITY) {
                EntityType entityType = entityTypeFrom(criterion.getTrigger().getString("entity-type", ""));
                return entityType == null ? null : Integer.valueOf(player.getStatistic(statistic, entityType));
            }
            return Integer.valueOf(player.getStatistic(statistic));
        } catch (Exception ignored) {
            return null;
        }
    }

    private EntityType entityTypeFrom(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return EntityType.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private Material materialFrom(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        return Material.matchMaterial(raw.trim());
    }

    private int countItems(PlayerInventory inventory, Material material) {
        int total = 0;
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null || itemStack.getType() != material) {
                continue;
            }
            total += Math.max(0, itemStack.getAmount());
        }
        return total;
    }

    private boolean isArmorless(Player player) {
        PlayerInventory inventory = player.getInventory();
        return isEmpty(inventory.getHelmet())
                && isEmpty(inventory.getChestplate())
                && isEmpty(inventory.getLeggings())
                && isEmpty(inventory.getBoots());
    }

    private boolean isEmpty(ItemStack itemStack) {
        return itemStack == null || itemStack.getType() == Material.AIR;
    }

    private int countEquipped(PlayerInventory inventory, Material material) {
        int total = 0;
        ItemStack helmet = inventory.getHelmet();
        ItemStack chestplate = inventory.getChestplate();
        ItemStack leggings = inventory.getLeggings();
        ItemStack boots = inventory.getBoots();
        ItemStack mainHand = inventory.getItemInMainHand();
        ItemStack offHand = inventory.getItemInOffHand();
        total += matchesItem(helmet, material);
        total += matchesItem(chestplate, material);
        total += matchesItem(leggings, material);
        total += matchesItem(boots, material);
        total += matchesItem(mainHand, material);
        total += matchesItem(offHand, material);
        return total;
    }

    private int matchesItem(ItemStack itemStack, Material material) {
        return itemStack != null && itemStack.getType() == material ? Math.max(1, itemStack.getAmount()) : 0;
    }

    private boolean isWithinRange(Player player, AchievementCriterion criterion) {
        String world = criterion.getTrigger().getString("world", "");
        if (!world.trim().isEmpty() && !player.getWorld().getName().equalsIgnoreCase(world)) {
            return false;
        }
        double x = player.getLocation().getX();
        double y = player.getLocation().getY();
        double z = player.getLocation().getZ();
        return inRange(x, criterion.getTrigger().getString("x-min", ""), criterion.getTrigger().getString("x-max", ""))
                && inRange(y, criterion.getTrigger().getString("y-min", ""), criterion.getTrigger().getString("y-max", ""))
                && inRange(z, criterion.getTrigger().getString("z-min", ""), criterion.getTrigger().getString("z-max", ""));
    }

    private boolean inRange(double value, String minRaw, String maxRaw) {
        Double min = toDouble(minRaw);
        Double max = toDouble(maxRaw);
        if (min != null && value < min.doubleValue()) {
            return false;
        }
        if (max != null && value > max.doubleValue()) {
            return false;
        }
        return true;
    }

    private Double toDouble(String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return null;
        }
        try {
            return Double.valueOf(raw.trim().replace(',', '.'));
        } catch (Exception ignored) {
            return null;
        }
    }

    private String activeEffects(Player player) {
        List<String> names = new ArrayList<String>();
        for (org.bukkit.potion.PotionEffect effect : player.getActivePotionEffects()) {
            if (effect != null && effect.getType() != null && effect.getType().getName() != null) {
                names.add(effect.getType().getName());
            }
        }
        return String.join(",", names);
    }

    private boolean hasAllEffects(Player player, String raw) {
        if (raw == null || raw.trim().isEmpty()) {
            return false;
        }
        String[] split = raw.split(",");
        for (String part : split) {
            String trimmed = part.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            PotionEffectType effectType = PotionEffectType.getByName(trimmed.toUpperCase(Locale.ROOT));
            if (effectType == null || !player.hasPotionEffect(effectType)) {
                return false;
            }
        }
        return true;
    }

    private String resolveMoonPhase(World world) {
        long fullTime = world.getFullTime();
        int phase = (int) ((fullTime / 24000L) % 8L);
        switch (phase) {
            case 0:
                return "FULL_MOON";
            case 1:
                return "WANING_GIBBOUS";
            case 2:
                return "LAST_QUARTER";
            case 3:
                return "WANING_CRESCENT";
            case 4:
                return "NEW_MOON";
            case 5:
                return "WAXING_CRESCENT";
            case 6:
                return "FIRST_QUARTER";
            default:
                return "WAXING_GIBBOUS";
        }
    }

    private boolean isWithinTimeWindow(long time, long min, long max) {
        if (min <= max) {
            return time >= min && time <= max;
        }
        return time >= min || time <= max;
    }

    private void tickBiomeStay(Player player) {
        List<CriterionBinding> bindings = get(TriggerType.BIOME_STAY);
        if (bindings.isEmpty()) {
            return;
        }
        String key = player.getUniqueId().toString();
        Biome biome = player.getLocation().getBlock().getBiome();
        BiomeStaySession session = biomeStaySessions.get(key);
        if (session == null || session.biome != biome || !session.worldName.equalsIgnoreCase(player.getWorld().getName())) {
            session = new BiomeStaySession(player.getWorld().getName(), biome);
            biomeStaySessions.put(key, session);
        } else {
            session.ticks++;
        }
        for (CriterionBinding binding : bindings) {
            if (!ValueMatcher.parse(binding.criterion.getTrigger().getString("value", "any")).matches(biome.name())) {
                continue;
            }
            int seconds = Math.max(1, session.ticks / 20);
            Map<String, String> context = with(baseContext(player, "biome_stay"),
                    "biome", biome.name(),
                    "time_seconds", String.valueOf(seconds));
            syncAbsoluteIfMatches(player, binding, seconds, context);
        }
    }

    public void onMoveDistance(Player player, double horizontalDistance) {
        if (horizontalDistance <= 0.0D) {
            return;
        }
        int amount = (int) Math.floor(horizontalDistance);
        if (amount <= 0) {
            return;
        }
        if (player.isGliding()) {
            tickGlideDistance(player, amount);
        }
        if (player.isInsideVehicle()) {
            tickVehicleDistance(player, amount);
        }
    }

    private void tickGlideDistance(Player player, double distance) {
        int amount = (int) Math.floor(Math.max(0.0D, distance));
        if (amount <= 0) {
            return;
        }
        for (CriterionBinding binding : get(TriggerType.GLIDE_DISTANCE)) {
            Map<String, String> context = with(baseContext(player, "glide_distance"), "distance", String.valueOf(amount));
            incrementIfMatches(player, binding, amount, context);
        }
    }

    private void tickVehicleDistance(Player player, double distance) {
        int amount = (int) Math.floor(Math.max(0.0D, distance));
        if (amount <= 0) {
            return;
        }
        String vehicleType = player.getVehicle() == null ? "UNKNOWN" : player.getVehicle().getType().name();
        for (CriterionBinding binding : get(TriggerType.VEHICLE_DISTANCE)) {
            String rule = binding.criterion.getTrigger().getString("vehicle", "any");
            if (!ValueMatcher.parse(rule).matches(vehicleType)) {
                continue;
            }
            Map<String, String> context = with(baseContext(player, "vehicle_distance"),
                    "distance", String.valueOf(amount),
                    "vehicle_type", vehicleType);
            incrementIfMatches(player, binding, amount, context);
        }
    }

    private String resolveWeather(World world) {
        if (world.isThundering()) {
            return "THUNDER";
        }
        if (world.hasStorm()) {
            return "RAIN";
        }
        return "CLEAR";
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

    private void incrementIfMatches(Player player, CriterionBinding binding, int amount, Map<String, String> context) {
        if (plugin.getConditionService().matches(player, binding.criterion.getConditions(), context)) {
            plugin.getProgressService().increment(player, binding.definition, binding.criterion.getId(), amount);
        }
    }

    private void awardIfMatches(Player player, CriterionBinding binding, Map<String, String> context) {
        if (plugin.getConditionService().matches(player, binding.criterion.getConditions(), context)) {
            plugin.getProgressService().increment(player, binding.definition, binding.criterion.getId(), binding.criterion.getTrigger().getInt("amount", 1));
        }
    }

    private void syncAbsoluteIfMatches(Player player, CriterionBinding binding, int value, Map<String, String> context) {
        if (!plugin.getConditionService().matches(player, binding.criterion.getConditions(), context)) {
            return;
        }
        int target = binding.criterion.getTrigger().getInt("amount", 1);
        int bounded = Math.max(0, value);
        plugin.getProgressService().setProgress(player, binding.definition, binding.criterion.getId(), bounded);
        if (bounded >= target) {
            plugin.getProgressService().increment(player, binding.definition, binding.criterion.getId(), 0);
        }
    }

    private enum TriggerType {
        JOIN,
        DEATH,
        LIGHTNING_STRIKE,
        ARMORLESS_DEATH,
        SLEEP,
        TOTEM_USE,
        RAID_WIN,
        BUCKET,
        POTION_EFFECT,
        VILLAGER_TRADE,
        VILLAGER_TRADE_PROFESSION,
        BREAK_BLOCK,
        BLOCK_PLACE,
        KILL_ENTITY,
        PROJECTILE_KILL,
        CRAFT_ITEM,
        BREW_ITEM,
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
        HARVEST,
        STATISTIC,
        SMELT_ITEM,
        PLAYER_ADVANCEMENT,
        INVENTORY_CONTAINS,
        EQUIP,
        LOOT_CONTAINER,
        BIOME_STAY,
        LOCATION_RANGE,
        GLIDE_DISTANCE,
        VEHICLE_DISTANCE,
        UNDERWATER_TIME,
        WEATHER,
        MOON_PHASE,
        TIME_WINDOW,
        EFFECT_COMBO;

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

    private static final class CriterionBinding {
        private final AchievementDefinition definition;
        private final AchievementCriterion criterion;

        private CriterionBinding(AchievementDefinition definition, AchievementCriterion criterion) {
            this.definition = definition;
            this.criterion = criterion;
        }
    }

    private enum Type {
        UNTYPED,
        ITEM,
        BLOCK,
        ENTITY;

        static Type fromName(String raw) {
            if (raw == null) {
                return UNTYPED;
            }
            if ("ITEM".equalsIgnoreCase(raw)) {
                return ITEM;
            }
            if ("BLOCK".equalsIgnoreCase(raw)) {
                return BLOCK;
            }
            if ("ENTITY".equalsIgnoreCase(raw) || "ENTITY_TYPE".equalsIgnoreCase(raw)) {
                return ENTITY;
            }
            return UNTYPED;
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

    private static final class BiomeStaySession {
        private final String worldName;
        private final Biome biome;
        private int ticks;

        private BiomeStaySession(String worldName, Biome biome) {
            this.worldName = worldName;
            this.biome = biome;
            this.ticks = 0;
        }
    }
}
