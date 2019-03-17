package com.bradyrussell.webreporter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;

public class CommandReport implements CommandExecutor {
    private WebReporterPlugin plugin;

    public CommandReport(WebReporterPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if(commandSender instanceof Player){
            Player reporter = (Player) commandSender;

            if(strings.length == 0) {
                reporter.sendRawMessage(ChatColor.RED+"You must enter a player's name e.g.:"+ChatColor.GOLD+" /report Notch");
                return true;
            } else {
                Player reported = Bukkit.getPlayer(strings[0]);

                if(reported == null){
                    // player not online
                    if(reporter.hasMetadata("report_offline")){
                        // they want to report offline player
                        Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->WebReporterDatabase.createOfflineReport(reporter, strings[0]));
                    } else {
                        reporter.sendRawMessage(ChatColor.GOLD+"The player "+ChatColor.AQUA+strings[0]+ChatColor.GOLD+" is not online. Resend the same command to create an offline player report.");
                        reporter.setMetadata("report_offline",new FixedMetadataValue(plugin,strings[0]));
                        Bukkit.getScheduler().runTaskLater(plugin, () -> reporter.removeMetadata("report_offline", plugin), 300);
                    }
                } else {
                    //reported player online
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, ()->WebReporterDatabase.createReport(reporter, reported));
                }
            }
        }
        return true;
    }
}
