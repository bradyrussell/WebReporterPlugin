package com.bradyrussell.webreporter;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;


import java.sql.*;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class WebReporterDatabase {
    public static String REPORTS_LINK; // only here for convenience

    private static final String JDBC_DRIVER = "org.mariadb.jdbc.Driver";
    //  Database credentials
    private static String DB_USER;
    private static String DB_PASS;
    private static String DB_URL;

    public static void RegisterCredentials(String DB_HOST, String DB_NAME, String theDB_USER, String theDB_PASS, String theREPORTS_LINK) {
        REPORTS_LINK = theREPORTS_LINK;
        DB_USER = theDB_USER;
        DB_PASS = theDB_PASS;
        DB_URL = "jdbc:mariadb://" + DB_HOST + "/" + DB_NAME;
        try {
            Class.forName(JDBC_DRIVER);
        } catch (ClassNotFoundException e) {
            Bukkit.getLogger().warning("Cannot find mariaDB driver. Aborting.");
        }
    }

    //todo webpage links to the coords on the dynmap
    public static boolean createReport(Player reporting_player, Player reported_player) {

        // prepare details

        Connection conn = null;
        PreparedStatement stmt;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            stmt = conn.prepareStatement("INSERT INTO reports (status, reported_username, reported_uuid, reporter_username, reporter_uuid, reported_location, reporter_location, form_key, reported_inv) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, "pending");

            stmt.setString(2, reported_player.getDisplayName());
            stmt.setString(3, reported_player.getUniqueId().toString());

            stmt.setString(4, reporting_player.getDisplayName());
            stmt.setString(5, reporting_player.getUniqueId().toString());

            stmt.setString(6, LocationToString(reported_player.getLocation()));
            stmt.setString(7, LocationToString(reporting_player.getLocation()));

            String key = CreateRandomKey(16);
            stmt.setString(8, key);

            stmt.setString(9, InventoryToString(reported_player.getInventory()));

            int numChanges = stmt.executeUpdate();

            if (numChanges > 0) {
                NotifyManagers(reporting_player.getDisplayName(), reported_player.getDisplayName());
                MessagePlayerReportLink(reporting_player, key);
            } else
                reporting_player.sendRawMessage(ChatColor.RED + "There was an error submitting your report. Please try again or contact an administrator!");

            return numChanges > 0;
        } catch (Exception e) {
            e.printStackTrace();
            reporting_player.sendRawMessage(ChatColor.RED + "There was an error submitting your report. Please try again or contact an administrator!");
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
                reporting_player.sendRawMessage(ChatColor.RED + "There was an error submitting your report. Please try again or contact an administrator!");
            }
        }

        return false;
    }

    public static boolean createOfflineReport(Player reporting_player, String reported_player) {

        // prepare details

        Connection conn = null;
        PreparedStatement stmt;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            stmt = conn.prepareStatement("INSERT INTO reports (status, reported_username, reported_uuid, reporter_username, reporter_uuid, reported_location, reporter_location, form_key, reported_inv) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
            stmt.setString(1, "pending");

            stmt.setString(2, reported_player);
            stmt.setString(3, "offline");

            stmt.setString(4, reporting_player.getDisplayName());
            stmt.setString(5, reporting_player.getUniqueId().toString());

            stmt.setString(6, "offline");
            stmt.setString(7, LocationToString(reporting_player.getLocation()));

            String key = CreateRandomKey(16);
            stmt.setString(8, key);

            stmt.setString(9, "offline");

            int numChanges = stmt.executeUpdate();

            if (numChanges > 0) {
                MessagePlayerReportLink(reporting_player, key);
                NotifyManagers(reporting_player.getDisplayName(), reported_player);
            } else
                reporting_player.sendRawMessage(ChatColor.RED + "There was an error submitting your report. Please try again or contact an administrator!");

            return numChanges > 0;
        } catch (Exception e) {
            e.printStackTrace();
            reporting_player.sendRawMessage(ChatColor.RED + "There was an error submitting your report. Please try again or contact an administrator!");
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
                reporting_player.sendRawMessage(ChatColor.RED + "There was an error submitting your report. Please try again or contact an administrator!");
            }
        }

        return false;
    }


    public static boolean createManagerSession(Player manager) {
        if (!manager.hasPermission("webreporter.manager")) return false;
        // prepare details

        Connection conn = null;
        PreparedStatement stmt;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            stmt = conn.prepareStatement("INSERT INTO manager_sessions (username, session_key, ip) VALUES (?, ?, ?)");

            String key = CreateRandomKey(64);

            stmt.setString(1, manager.getDisplayName());
            stmt.setString(2, key);
            stmt.setString(3, manager.getAddress().getHostString());

            int numChanges = stmt.executeUpdate();

            if (numChanges > 0) {
                manager.sendRawMessage(ChatColor.GOLD + "Your report manager session can be accessed here: " + ChatColor.AQUA + REPORTS_LINK.replace("report.php", "mod/login.php") + key + ChatColor.GOLD + ". This link will expire in one hour.");
            } else
                manager.sendRawMessage(ChatColor.RED + "There was an error logging you in to the report manager system. Please try again or contact an administrator!");

            return numChanges > 0;
        } catch (Exception e) {
            e.printStackTrace();
            manager.sendRawMessage(ChatColor.RED + "There was an error logging you in to the report manager system. Please try again or contact an administrator!");
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
                manager.sendRawMessage(ChatColor.RED + "There was an error logging you in to the report manager system. Please try again or contact an administrator!");
            }
        }

        return false;
    }

    public static void sendOpenReports(Player player) {
        Connection conn = null;
        PreparedStatement stmt;
        try {
            conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
            stmt = conn.prepareStatement("SELECT COUNT(*) FROM reports WHERE status = 'open'");
            int count = 0;
            ResultSet resultSet = stmt.executeQuery();
            while (resultSet.next()) {
                count = resultSet.getInt(1);
            }
            player.sendRawMessage(ChatColor.GOLD + "There are currently " + ChatColor.GREEN + count + ChatColor.GOLD + " open reports.");
            return;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException se) {
                se.printStackTrace();
            }
        }
        return;
    }


    private static void MessagePlayerReportLink(Player player, String key) {
        player.sendRawMessage(ChatColor.LIGHT_PURPLE + "Saved report snapshot. You must complete it here: " + ChatColor.AQUA + REPORTS_LINK + key);
    }

    private static String CreateRandomKey(int len) {
        StringBuilder s = new StringBuilder();
        for (int i = 0; i < len; i++) {
            s.append((char) (ThreadLocalRandom.current().nextBoolean() ? ThreadLocalRandom.current().nextInt(65, 90) : ThreadLocalRandom.current().nextInt(97, 122)));
        }
        return s.toString();
    }

    private static String InventoryToString(Inventory inventory) {
        StringBuilder s = new StringBuilder();
        for (ItemStack i : inventory.getContents()) {
            if (i != null) s.append("\n").append(i.toString());
        }
        return s.toString();
    }

    @NotNull
    private static String LocationToString(Location location) {
        return location.getWorld().getName() + ":" +
                location.getX() + ":" +
                location.getY() + ":" +
                location.getZ();
    }

    private static void NotifyManagers(String sender, String reported) {
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p != null && p.hasPermission("webreporter.manager")) {
                p.sendRawMessage(ChatColor.GOLD + "A new report has been submitted by " + ChatColor.GREEN + sender + ChatColor.GOLD + " regarding " + ChatColor.RED + reported + ChatColor.GOLD + "! You can check it with " + ChatColor.LIGHT_PURPLE + "/reports" + ChatColor.GOLD + ".");
            }
        }
    }
}

