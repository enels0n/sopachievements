package net.enelson.sopachievements.listener;

import net.enelson.sopachievements.SopAchievementsPlugin;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;

public final class AchievementEventListener implements Listener {

    private final SopAchievementsPlugin plugin;

    public AchievementEventListener(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getTriggerService().onJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getTriggerService().resetTransientState(event.getPlayer());
    }

    @EventHandler
    public void onBreak(BlockBreakEvent event) {
        plugin.getTriggerService().onBlockBreak(event.getPlayer(), event.getBlock().getType());
    }

    @EventHandler
    public void onDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();
        if (killer == null) {
            return;
        }
        plugin.getTriggerService().onEntityKill(killer, entity.getType());
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

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
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
}
