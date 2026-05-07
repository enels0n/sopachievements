package net.enelson.sopachievements.listener;

import net.enelson.sopachievements.SopAchievementsPlugin;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.raid.RaidFinishEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityResurrectEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.entity.EntityPotionEffectEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.BrewerInventory;
import org.bukkit.inventory.MerchantInventory;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;
import org.bukkit.event.player.PlayerHarvestBlockEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.potion.PotionEffect;

import java.util.List;

public final class AchievementEventListener implements Listener {

    private final SopAchievementsPlugin plugin;

    public AchievementEventListener(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getTriggerService().onJoin(event.getPlayer());
        plugin.getTriggerService().onEnterWorld(event.getPlayer(), event.getPlayer().getWorld());
        plugin.getTriggerService().onStatisticSync(event.getPlayer());
        plugin.getTriggerService().onInventorySync(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getTriggerService().resetTransientState(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        String cause = event.getEntity().getLastDamageCause() == null ? "UNKNOWN" : event.getEntity().getLastDamageCause().getCause().name();
        plugin.getTriggerService().onPlayerDeath(event.getEntity(), cause);
    }

    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.OK) {
            plugin.getTriggerService().onSleep(event.getPlayer());
        }
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        plugin.getTriggerService().onBlockBreak(event.getPlayer(), event.getBlock().getType());
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        plugin.getTriggerService().onBlockPlace(event.getPlayer(), event.getBlockPlaced().getType());
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        plugin.getTriggerService().onBucket(event.getPlayer(), event.getBucket(), "FILL");
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        plugin.getTriggerService().onBucket(event.getPlayer(), event.getBucket(), "EMPTY");
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        Projectile projectile = projectileKiller(entity);
        if (killer == null) {
            if (projectile != null && projectile.getShooter() instanceof Player) {
                plugin.getTriggerService().onProjectileKill((Player) projectile.getShooter(), entity.getType(), projectile.getType());
            }
            return;
        }
        plugin.getTriggerService().onEntityKill(killer, entity.getType());
        if (projectile != null && projectile.getShooter() instanceof Player) {
            plugin.getTriggerService().onProjectileKill((Player) projectile.getShooter(), entity.getType(), projectile.getType());
        }
    }

    @EventHandler
    public void onCraft(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        ItemStack item = event.getRecipe() == null ? null : event.getRecipe().getResult();
        if (item == null) {
            return;
        }
        plugin.getTriggerService().onCraft((Player) event.getWhoClicked(), item.getType());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        InventoryType type = event.getInventory().getType();
        if (type == null) {
            return;
        }
        plugin.getTriggerService().onLootContainer((Player) event.getPlayer(), type.name());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onMerchantTrade(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        if (!(event.getInventory() instanceof MerchantInventory)) {
            return;
        }
        if (event.getSlotType() != org.bukkit.event.inventory.InventoryType.SlotType.RESULT) {
            return;
        }
        ItemStack current = event.getCurrentItem();
        if (current == null) {
            return;
        }
        plugin.getTriggerService().onVillagerTrade((Player) event.getWhoClicked(), current.getType(), Math.max(1, current.getAmount()));
    }

    @EventHandler
    public void onSmelt(FurnaceExtractEvent event) {
        plugin.getTriggerService().onSmeltItem(event.getPlayer(), event.getItemType(), event.getItemAmount());
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        ItemStack item = event.getItem().getItemStack();
        plugin.getTriggerService().onPickupItem((Player) event.getEntity(), item.getType(), item.getAmount());
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        plugin.getTriggerService().onConsumeItem(event.getPlayer(), event.getItem().getType());
    }

    @EventHandler
    public void onResurrect(EntityResurrectEvent event) {
        if (event.getEntity() instanceof Player) {
            plugin.getTriggerService().onTotemUse((Player) event.getEntity());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onPotionEffect(EntityPotionEffectEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        PotionEffect effect = event.getNewEffect();
        if (effect == null) {
            return;
        }
        plugin.getTriggerService().onPotionEffect((Player) event.getEntity(), effect.getType(), effect.getAmplifier());
    }

    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        plugin.getTriggerService().onEnchantItem(event.getEnchanter(), 1);
    }

    @EventHandler
    public void onTame(EntityTameEvent event) {
        AnimalTamer owner = event.getOwner();
        if (!(owner instanceof Player)) {
            return;
        }
        plugin.getTriggerService().onTameEntity((Player) owner, event.getEntity().getType());
    }

    @EventHandler
    public void onBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player)) {
            return;
        }
        plugin.getTriggerService().onBreedEntity((Player) event.getBreeder(), event.getEntity().getType());
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        plugin.getTriggerService().onEnterWorld(event.getPlayer(), event.getPlayer().getWorld());
        plugin.getTriggerService().resetTransientState(event.getPlayer());
    }

    @EventHandler
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        plugin.getTriggerService().onAdvancementDone(event.getPlayer(), event.getAdvancement().getKey().toString());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onRaidFinish(RaidFinishEvent event) {
        List<Player> winners = event.getWinners();
        if (winners == null) {
            return;
        }
        for (Player player : winners) {
            plugin.getTriggerService().onRaidWin(player);
        }
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        plugin.getTriggerService().onChat(event.getPlayer(), event.getMessage());
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String command = event.getMessage();
        int spaceIndex = command.indexOf(' ');
        if (spaceIndex >= 0) {
            command = command.substring(0, spaceIndex);
        }
        plugin.getTriggerService().onExecuteCommand(event.getPlayer(), command);
    }

    @EventHandler
    public void onDamageTaken(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        plugin.getTriggerService().onDamageTaken((Player) event.getEntity(), event.getCause(), event.getFinalDamage());
    }

    @EventHandler
    public void onDamageDealt(EntityDamageByEntityEvent event) {
        Player player = resolveDamager(event.getDamager());
        if (player == null) {
            return;
        }
        plugin.getTriggerService().onDamageDealt(player, event.getEntity(), event.getFinalDamage());
    }

    @EventHandler
    public void onFish(PlayerFishEvent event) {
        if (!(event.getCaught() instanceof org.bukkit.entity.Item)) {
            return;
        }
        ItemStack item = ((org.bukkit.entity.Item) event.getCaught()).getItemStack();
        plugin.getTriggerService().onFishItem(event.getPlayer(), item.getType(), item.getAmount());
    }

    @EventHandler
    public void onHarvest(PlayerHarvestBlockEvent event) {
        plugin.getTriggerService().onHarvest(event.getPlayer(), event.getItemsHarvested());
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
    public void onBrew(BrewEvent event) {
        BrewerInventory inventory = event.getContents();
        if (inventory == null) {
            return;
        }
        List<HumanEntity> viewers = inventory.getViewers();
        if (viewers == null || viewers.isEmpty()) {
            return;
        }
        for (ItemStack itemStack : inventory.getContents()) {
            if (itemStack == null) {
                continue;
            }
            for (HumanEntity viewer : viewers) {
                if (viewer instanceof Player) {
                    plugin.getTriggerService().onBrewItem((Player) viewer, itemStack.getType(), Math.max(1, itemStack.getAmount()));
                }
            }
        }
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        if (!event.getFrom().getWorld().equals(event.getTo().getWorld())) {
            plugin.getTriggerService().resetTransientState(event.getPlayer());
            return;
        }
        double deltaX = event.getTo().getX() - event.getFrom().getX();
        double deltaZ = event.getTo().getZ() - event.getFrom().getZ();
        double horizontalDistance = Math.sqrt((deltaX * deltaX) + (deltaZ * deltaZ));
        if (horizontalDistance >= 1.0D) {
            plugin.getTriggerService().onTravelDistance(event.getPlayer(), horizontalDistance);
        }
        if (event.getFrom().getY() == event.getTo().getY()) {
            return;
        }
        plugin.getTriggerService().onMove(
                event.getPlayer(),
                event.getFrom().getY(),
                event.getTo().getY(),
                event.getPlayer().isOnGround()
        );
    }

    private Player resolveDamager(Entity damager) {
        if (damager instanceof Player) {
            return (Player) damager;
        }
        if (damager instanceof Projectile) {
            Projectile projectile = (Projectile) damager;
            if (projectile.getShooter() instanceof Player) {
                return (Player) projectile.getShooter();
            }
        }
        return null;
    }

    private Projectile projectileKiller(LivingEntity entity) {
        if (entity.getLastDamageCause() instanceof EntityDamageByEntityEvent) {
            Entity damager = ((EntityDamageByEntityEvent) entity.getLastDamageCause()).getDamager();
            if (damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;
                ProjectileSource shooter = projectile.getShooter();
                if (shooter instanceof Player) {
                    return projectile;
                }
            }
        }
        return null;
    }
}
