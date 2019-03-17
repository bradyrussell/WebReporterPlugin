package com.bradyrussell.webreporter;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;

public class WebReporterPlugin extends JavaPlugin {

    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {

        YamlConfiguration dbsettings = YamlConfiguration.loadConfiguration(createConfig());
        WebReporterDatabase.RegisterCredentials(dbsettings.getString("database_host"),dbsettings.getString("database_name"),dbsettings.getString("database_username"),dbsettings.getString("database_password"),dbsettings.getString("reports_link"));

        getServer().getPluginManager().registerEvents(new WebReporterListener(this), this);
        this.getCommand("report").setExecutor(new CommandReport(this));
        this.getCommand("reports").setExecutor(new CommandReports(this));
        this.getCommand("tpreport").setExecutor(new CommandTPReport(this));

    }

    //load file stole this code from spigot wiki
    private File createConfig() {
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdirs();
            }
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getLogger().info("Config.yml not found, creating!");
                getLogger().warning("You must fill out WebReporter/config.yml to connect to the database.");
                saveDefaultConfig();
            } else {
                getLogger().info("Config.yml found, loading!");
            }
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }
}
