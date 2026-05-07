package net.enelson.sopachievements;

import net.enelson.sopachievements.advancement.AdvancementRegistry;
import net.enelson.sopachievements.command.SopAchievementsCommand;
import net.enelson.sopachievements.config.AchievementConfigLoader;
import net.enelson.sopachievements.listener.AchievementEventListener;
import net.enelson.sopachievements.model.AchievementDefinition;
import net.enelson.sopachievements.model.AchievementRegistryModel;
import net.enelson.sopachievements.service.AchievementProgressService;
import net.enelson.sopachievements.service.AchievementConditionService;
import net.enelson.sopachievements.service.AchievementRewardService;
import net.enelson.sopachievements.service.AchievementTriggerService;
import net.enelson.sopachievements.service.MessageService;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Collections;

public final class SopAchievementsPlugin extends JavaPlugin {

    private MessageService messageService;
    private AchievementRegistryModel registryModel;
    private AdvancementRegistry advancementRegistry;
    private AchievementProgressService progressService;
    private AchievementConditionService conditionService;
    private AchievementRewardService rewardService;
    private AchievementTriggerService triggerService;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        saveResourceIfMissing("achievements.yml");

        this.messageService = new MessageService(this);
        this.advancementRegistry = new AdvancementRegistry(this);
        this.progressService = new AchievementProgressService(this);
        this.conditionService = new AchievementConditionService(this);
        this.rewardService = new AchievementRewardService(this);
        this.triggerService = new AchievementTriggerService(this);

        if (!reloadPlugin()) {
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        getServer().getPluginManager().registerEvents(new AchievementEventListener(this), this);
        getCommand("sopachievements").setExecutor(new SopAchievementsCommand(this));
    }

    public boolean reloadPlugin() {
        reloadConfig();
        try {
            AchievementRegistryModel loaded = new AchievementConfigLoader(this).load();
            this.registryModel = loaded;
            this.triggerService.reload(loaded);
            this.advancementRegistry.reload(loaded);
            for (AchievementDefinition definition : loaded.getAchievements().values()) {
                if (definition.isRoot()) {
                    progressService.ensureAwardConsistency(definition, Collections.unmodifiableMap(loaded.getAchievements()));
                }
            }
            return true;
        } catch (Exception exception) {
            getLogger().severe("Failed to reload SopAchievements: " + exception.getMessage());
            exception.printStackTrace();
            return false;
        }
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

    private void saveResourceIfMissing(String path) {
        java.io.File file = new java.io.File(getDataFolder(), path);
        if (!file.exists()) {
            saveResource(path, false);
        }
    }
}
