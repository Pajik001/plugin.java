package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class DatabaseManager {

    private final Main plugin;
    private Connection connection;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        connect();
        createTables();
    }

    private void connect() {
        try {
            if (!plugin.getDataFolder().exists()) plugin.getDataFolder().mkdirs();
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            
            // Načtení driveru pro 1.21 Paper
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            plugin.getLogger().info("§aDatabáze připojena.");
        } catch (Exception e) {
            plugin.getLogger().severe("§cCRITICAL: Nelze připojit DB!");
            e.printStackTrace();
        }
    }

    private void createTables() {
        String[] queries = {
            // Postavy (přidány potřeby: thirst, sleep, toilet)
            "CREATE TABLE IF NOT EXISTS rp_characters (" +
            "uuid TEXT PRIMARY KEY, " +
            "rp_name TEXT, " +
            "origin TEXT, " +
            "age INTEGER, " +
            "has_character INTEGER DEFAULT 0, " +
            "thirst REAL DEFAULT 100, " +
            "sleep REAL DEFAULT 100, " +
            "toilet INTEGER DEFAULT 0, " +
            "playtime_minutes INTEGER DEFAULT 0, " + // Pro daně
            "tax_debt INTEGER DEFAULT 0" +
            ")",

            // Licence
            "CREATE TABLE IF NOT EXISTS rp_licenses (uuid TEXT, license_type TEXT)",

            // Zámky (location + owner + allowed players)
            // allowed_players bude string s UUID oddělenými čárkou: "uuid1,uuid2,uuid3"
            // faction_id povoluje vstup celé frakci (volitelné)
            "CREATE TABLE IF NOT EXISTS rp_locks (" +
            "location_world TEXT, location_x INTEGER, location_y INTEGER, location_z INTEGER, " +
            "owner_uuid TEXT, " +
            "allowed_players TEXT, " +
            "faction_id INTEGER DEFAULT 0, " +
            "PRIMARY KEY (location_world, location_x, location_y, location_z)" +
            ")",

            // Zvířata
            "CREATE TABLE IF NOT EXISTS rp_animals (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "owner_uuid TEXT, " +
            "type TEXT, " +
            "variant TEXT, " +
            "name TEXT, " +
            "is_dead INTEGER DEFAULT 0" +
            ")",

            // Frakce (Definice)
            "CREATE TABLE IF NOT EXISTS rp_factions (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "name TEXT UNIQUE, " +
            "owner_uuid TEXT" +
            ")",

            // Definice rolí ve frakci (Název, Limit, Priorita/Váha)
            "CREATE TABLE IF NOT EXISTS rp_faction_roles (" +
            "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
            "faction_id INTEGER, " +
            "role_name TEXT, " +
            "member_limit INTEGER DEFAULT 0, " + // 0 = neomezeně
            "priority INTEGER DEFAULT 1, " + // 10 = Šéf, 1 = Nováček
            "FOREIGN KEY(faction_id) REFERENCES rp_factions(id) ON DELETE CASCADE" +
            ")",

            // Členové frakcí
            "CREATE TABLE IF NOT EXISTS rp_members (" +
            "uuid TEXT PRIMARY KEY, " +
            "faction_id INTEGER, " +
            "role_id INTEGER, " +
            "FOREIGN KEY(faction_id) REFERENCES rp_factions(id) ON DELETE CASCADE" +
            ")",

            // Bounty & CK (Active CKs)
            "CREATE TABLE IF NOT EXISTS rp_ck_requests (" +
            "target_uuid TEXT PRIMARY KEY, " +
            "issuer_uuid TEXT, " +
            "reason TEXT, " +
            "expires_at INTEGER" + // Timestamp
            ")"
        };

        try {
            for (String q : queries) {
                try (PreparedStatement ps = connection.prepareStatement(q)) {
                    ps.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) connect();
        } catch (SQLException e) { e.printStackTrace(); }
        return connection;
    }
    
    public void close() {
        try { if (connection != null) connection.close(); } catch (Exception e) {}
    }
}