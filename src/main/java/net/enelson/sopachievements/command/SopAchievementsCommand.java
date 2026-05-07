package net.enelson.sopachievements.command;

import net.enelson.sopachievements.SopAchievementsPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public final class SopAchievementsCommand implements CommandExecutor {

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
        }
        return true;
    }
}
