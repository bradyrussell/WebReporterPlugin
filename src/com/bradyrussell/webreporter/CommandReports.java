package com.bradyrussell.webreporter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class CommandReports implements CommandExecutor {
    private WebReporterPlugin plugin;

    public CommandReports(WebReporterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player && commandSender.hasPermission("webreporter.manager")) {
            Player manager = (Player) commandSender;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> WebReporterDatabase.createManagerSession(manager));
            return true;
        }
        return true;
    }
}
