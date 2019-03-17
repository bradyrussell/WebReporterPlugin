package com.bradyrussell.webreporter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandTPReport implements CommandExecutor {
    private WebReporterPlugin plugin;

    public CommandTPReport(WebReporterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (commandSender instanceof Player && commandSender.hasPermission("webreporter.manager")) {
            Player manager = (Player) commandSender;

            if(strings.length == 0){
                manager.sendRawMessage(ChatColor.RED+"/tpreport <report #> <reporter/reported/back>");
            }

            //Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> WebReporterDatabase.createManagerSession(manager));
            return true;
        }
        return true;
    }
}
