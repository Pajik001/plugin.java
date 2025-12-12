package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class BountyManager {

    private final Main plugin;

    public BountyManager(Main plugin) {
        this.plugin = plugin;
        createTable();
    }

    private void createTable() {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS rp_bounties (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "target_uuid VARCHAR(36), " +
                    "issuer_uuid VARCHAR(36), " +
                    "reward INT, " +
                    "is_ck BOOLEAN DEFAULT FALSE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");";
            conn.createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addBounty(Player issuer, Player target, int reward, boolean isCk) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO rp_bounties (target_uuid, issuer_uuid, reward, is_ck) VALUES (?, ?, ?, ?)");
                ps.setString(1, target.getUniqueId().toString());
                ps.setString(2, issuer.getUniqueId().toString());
                ps.setInt(3, reward);
                ps.setBoolean(4, isCk);
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    // Kontrola při smrti
    public void checkBounty(Player killer, Player victim) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM rp_bounties WHERE target_uuid = ?");
                ps.setString(1, victim.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    int id = rs.getInt("id");
                    int reward = rs.getInt("reward");
                    boolean isCk = rs.getBoolean("is_ck");

                    // Vyplacení odměny (převod na účet nebo do invu - zde do invu přes CurrencyManager)
                    // Musíme na hlavní vlákno
                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        plugin.getCurrencyManager().giveMoney(killer, reward); // reward je v měďácích? (Zde předpokládáme že reward je v měďácích)
                        killer.sendMessage("§a§lZÍSKAL JSI ODMĚNU!");
                        killer.sendMessage("§eZabil jsi hledaného hráče. Odměna: " + reward + " měďáků.");
                        
                        if (isCk) {
                            killer.sendMessage("§c§lTOTO B YL CK KONTRAKT! Hráč bude smazán.");
                            plugin.getCharacterManager().performCK(victim);
                        }
                    });

                    // Smazat bounty
                    PreparedStatement del = conn.prepareStatement("DELETE FROM rp_bounties WHERE id = ?");
                    del.setInt(1, id);
                    del.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }
}