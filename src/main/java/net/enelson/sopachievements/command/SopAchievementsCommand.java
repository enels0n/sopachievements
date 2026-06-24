package net.enelson.sopachievements.command;

import net.enelson.sopachievements.SopAchievementsPlugin;
import net.enelson.sopachievements.advancement.AdvancementJsonBuilder;
import net.enelson.sopachievements.model.AchievementDefinition;
import org.bukkit.Bukkit;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SopAchievementsCommand implements CommandExecutor, TabCompleter {

    private final SopAchievementsPlugin plugin;

    public SopAchievementsCommand(SopAchievementsPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.isOp() && !sender.hasPermission("sopachievements.admin")) {
            plugin.getMessageService().send(sender, "no-permission");
            return true;
        }

        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            if (plugin.reloadPlugin()) {
                plugin.getMessageService().send(sender, "reload-success");
            } else {
                plugin.getMessageService().send(sender, "reload-failed");
            }
            return true;
        }

        if ((args.length == 1 && "datapack".equalsIgnoreCase(args[0]))
                || (args.length == 2 && "datapack".equalsIgnoreCase(args[0]) && "rebuild".equalsIgnoreCase(args[1]))) {
            if (plugin.getRegistryModel() == null) {
                plugin.getMessageService().send(sender, "reload-failed");
                return true;
            }
            try {
                plugin.getAdvancementDataPackWriter().write(plugin.getRegistryModel());
                File packFolder = plugin.getAdvancementDataPackWriter().getPackFolder();
                sender.sendMessage(" ");
                plugin.getMessageService().send(sender, "datapack-rebuild-success");
                if (packFolder != null) {
                    sender.sendMessage("Pack folder: " + packFolder.getAbsolutePath());
                }
                plugin.getMessageService().send(sender, "datapack-rebuild-restart");
                sender.sendMessage("/datapack disable \"" + plugin.getAdvancementDataPackWriter().getPackReference() + "\"");
                sender.sendMessage("/datapack enable \"" + plugin.getAdvancementDataPackWriter().getPackReference() + "\"");
            } catch (Exception exception) {
                plugin.getMessageService().send(sender, "datapack-rebuild-failed");
                exception.printStackTrace();
            }
            return true;
        }

        if (args.length == 1 && "debug".equalsIgnoreCase(args[0]) && sender instanceof Player) {
            Player player = (Player) sender;
            AchievementDefinition root = plugin.getRegistryModel().getAchievements().get("first_join");
            if (root == null) {
                sender.sendMessage("first_join definition is missing");
                return true;
            }
            Advancement advancement = plugin.getAdvancementRegistry().getAdvancement(root.getId());
            Advancement categoryRoot = plugin.getAdvancementRegistry().getAdvancement(AdvancementJsonBuilder.categoryRootId(root.getCategoryId()));
            AdvancementProgress advancementProgress = advancement == null ? null : player.getAdvancementProgress(advancement);
            AdvancementProgress categoryProgress = categoryRoot == null ? null : player.getAdvancementProgress(categoryRoot);
            sender.sendMessage("advancement exists: " + (advancement != null));
            sender.sendMessage("bukkit done: " + (advancementProgress != null && advancementProgress.isDone()));
            sender.sendMessage("category root exists: " + (categoryRoot != null));
            sender.sendMessage("category root done: " + (categoryProgress != null && categoryProgress.isDone()));
            sender.sendMessage("service awarded: " + plugin.getProgressService().isAwarded(player, root));
            sender.sendMessage("stored progress: " + plugin.getProgressService().getProgress(player, root, AchievementDefinition.DEFAULT_CRITERION_ID));
            return true;
        }

        if (args.length == 1 && "reinit".equalsIgnoreCase(args[0])) {
            if (sender instanceof Player) {
                plugin.schedulePlayerInitialization((Player) sender, 1L);
                plugin.getMessageService().send(sender, "reinit-success");
            } else {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    plugin.schedulePlayerInitialization(player, 1L);
                }
                plugin.getMessageService().send(sender, "reinit-success");
            }
            return true;
        }
        sender.sendMessage("/sopachievements <reload|datapack|reinit|debug>");
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.isOp() && !sender.hasPermission("sopachievements.admin")) {
            return Collections.emptyList();
        }
        if (args.length == 1) {
            List<String> options = new ArrayList<>();
            for (String s : new String[]{"reload", "datapack", "reinit", "debug"}) {
                if (s.startsWith(args[0].toLowerCase())) {
                    options.add(s);
                }
            }
            return options;
        }
        if (args.length == 2 && "datapack".equalsIgnoreCase(args[0])) {
            List<String> options = new ArrayList<>();
            if ("rebuild".startsWith(args[1].toLowerCase())) {
                options.add("rebuild");
            }
            return options;
        }
        return Collections.emptyList();
    }
}
