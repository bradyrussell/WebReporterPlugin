package com.bradyrussell.webreporter;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class WebReporterListener implements Listener {

    private JavaPlugin plugin;

    public WebReporterListener(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if(event.getPlayer().hasPermission("webreporter.manager")){
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> WebReporterDatabase.sendOpenReports(event.getPlayer()),100);
        }
    }
}
