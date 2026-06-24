package net.enelson.sopachievements;

import net.enelson.sopachievements.advancement.AdvancementRegistry;
import net.enelson.sopachievements.advancement.AdvancementDataPackWriter;
import net.enelson.sopachievements.advancement.AdvancementJsonBuilder;
import net.enelson.sopachievements.command.SopAchievementsCommand;
import net.enelson.sopachievements.config.AchievementConfigLoader;
import net.enelson.sopachievements.listener.AchievementEventListener;
import net.enelson.sopachievements.model.AchievementCategory;
import net.enelson.sopachievements.model.AchievementDefinition;
import net.enelson.sopachievements.model.AchievementRegistryModel;
import net.enelson.sopachievements.service.AchievementProgressService;
import net.enelson.sopachievements.service.AchievementConditionService;
import net.enelson.sopachievements.service.AchievementRewardService;
import net.enelson.sopachievements.service.AchievementTriggerService;
import net.enelson.sopachievements.service.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public final class SopAchievementsPlugin extends JavaPlugin {

    private MessageService messageService;
    private AchievementRegistryModel registryModel;
    private AdvancementRegistry advancementRegistry;
    private AdvancementDataPackWriter advancementDataPackWriter;
    private AchievementProgressService progressService;
    private AchievementConditionService conditionService;
    private AchievementRewardService rewardService;
    private AchievementTriggerService triggerService;
    private int syncTaskId = -1;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("achievements.yml");

        this.messageService = new MessageService(this);
        this.advancementRegistry = new AdvancementRegistry(this);
        this.advancementDataPackWriter = new AdvancementDataPackWriter(this);
        this.progressService = new AchievementProgressService(this);
        this.conditionService = new AchievementConditionService(this);
        this.rewardService = new AchievementRewardService(this);
        this.triggerService = new AchievementTriggerService(this);

        if (!reloadPlugin()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new AchievementEventListener(this), this);
        SopAchievementsCommand commandExecutor = new SopAchievementsCommand(this);
        getCommand("sopachievements").setExecutor(commandExecutor);
        getCommand("sopachievements").setTabCompleter(commandExecutor);
    }

    public boolean reloadPlugin() {
        reloadConfig();
        try {
            stopSyncTask();
            AchievementRegistryModel loaded = new AchievementConfigLoader(this).load();
            this.registryModel = loaded;
            this.triggerService.reload(loaded);
            this.advancementDataPackWriter.write(loaded);
            this.advancementRegistry.reload(loaded);
            for (AchievementDefinition definition : loaded.getAchievements().values()) {
                progressService.ensureAwardConsistency(definition, Collections.unmodifiableMap(loaded.getAchievements()));
            }
            for (Player player : Bukkit.getOnlinePlayers()) {
                schedulePlayerInitialization(player, 60L);
            }
            startSyncTask();
            return true;
        } catch (Exception exception) {
            getLogger().severe("Failed to reload SopAchievements: " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }
    }

    @Override
    public void onDisable() {
        stopSyncTask();
    }

    public MessageService getMessageService() {
        return messageService;
    }

    public AchievementRegistryModel getRegistryModel() {
        return registryModel;
    }

    public AdvancementRegistry getAdvancementRegistry() {
        return advancementRegistry;
    }

    public AdvancementDataPackWriter getAdvancementDataPackWriter() {
        return advancementDataPackWriter;
    }

    public AchievementProgressService getProgressService() {
        return progressService;
    }

    public AchievementConditionService getConditionService() {
        return conditionService;
    }

    public AchievementRewardService getRewardService() {
        return rewardService;
    }

    public AchievementTriggerService getTriggerService() {
        return triggerService;
    }

    public void schedulePlayerInitialization(final Player player, long delayTicks) {
        getServer().getScheduler().runTaskLater(this, new Runnable() {
            @Override
            public void run() {
                if (player == null || !player.isOnline()) {
                    return;
                }
                runInitializationPass(player);
                getServer().getScheduler().runTaskLater(SopAchievementsPlugin.this, new Runnable() {
                    @Override
                    public void run() {
                        if (player == null || !player.isOnline()) {
                            return;
                        }
                        runInitializationPass(player);
                    }
                }, 40L);
            }
        }, Math.max(40L, delayTicks));
    }

    private void runInitializationPass(Player player) {
        ensureCategoryTabsGranted(player);
        progressService.refreshAwardedAdvancements(player, registryModel.getAchievements().values());
        triggerService.onJoin(player);
        triggerService.onEnterWorld(player, player.getWorld());
        triggerService.onStatisticSync(player);
        triggerService.onInventorySync(player);
        triggerService.onEquipSync(player);
        triggerService.onLocationRangeSync(player);
        triggerService.onUnderwaterSync(player);
        triggerService.onWeatherSync(player);
        triggerService.onMoonPhaseSync(player);
        triggerService.onTimeWindowSync(player);
        triggerService.onEffectComboSync(player);
        progressService.refreshAwardedAdvancements(player, registryModel.getAchievements().values());
    }

    private void ensureCategoryTabsGranted(Player player) {
        if (registryModel == null) {
            return;
        }
        for (AchievementCategory category : registryModel.getCategories().values()) {
            org.bukkit.advancement.Advancement advancement = Bukkit.getAdvancement(
                    advancementRegistry.key(AdvancementJsonBuilder.categoryRootId(category.getId()))
            );
            progressService.awardAllCriteria(player, advancement);
        }
    }

    private void saveResourceIfMissing(String path) {
        java.io.File file = new java.io.File(getDataFolder(), path);
        if (!file.exists()) {
            saveResource(path, false);
        }
    }

    private void startSyncTask() {
        int interval = Math.max(20, getConfig().getInt("settings.sync-check-interval-ticks",
                getConfig().getInt("settings.statistic-check-interval-ticks", 100)));
        this.syncTaskId = getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    triggerService.onStatisticSync(player);
                    triggerService.onInventorySync(player);
                    triggerService.onEquipSync(player);
                    triggerService.onLocationRangeSync(player);
                    triggerService.onUnderwaterSync(player);
                    triggerService.onWeatherSync(player);
                    triggerService.onMoonPhaseSync(player);
                    triggerService.onTimeWindowSync(player);
                    triggerService.onEffectComboSync(player);
                    triggerService.onBiomeStaySync(player);
                }
            }
        }, interval, interval);
    }

    private void stopSyncTask() {
        if (syncTaskId != -1) {
            getServer().getScheduler().cancelTask(syncTaskId);
            syncTaskId = -1;
        }
    }
}
