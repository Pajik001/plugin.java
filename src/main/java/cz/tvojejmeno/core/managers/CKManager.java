package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CKManager {

    private final Main plugin;

    public CKManager(Main plugin) {
        this.plugin = plugin;
    }

    // Vytvoření požadavku na CK (Admin příkaz)
    public void createCKRequest(Player target, String issuerName, long durationMinutes) {
        long expiresAt = System.currentTimeMillis() + (durationMinutes * 60 * 1000);
        
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("INSERT OR REPLACE INTO rp_ck_requests (target_uuid, issuer_uuid, reason, expires_at) VALUES (?, ?, ?, ?)");
                ps.setString(1, target.getUniqueId().toString());
                ps.setString(2, issuerName); // Ukládáme jméno zadavatele (nebo UUID)
                ps.setString(3, "Admin Decision");
                ps.setLong(4, expiresAt);
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    // Kontrola při smrti hráče (voláno z DeathListener)
    public void checkCKDeath(Player victim) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM rp_ck_requests WHERE target_uuid = ?");
                ps.setString(1, victim.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    long expires = rs.getLong("expires_at");
                    // Pokud je CK stále platné (nevypršel čas)
                    if (System.currentTimeMillis() < expires) {
                        
                        // SMAZAT POSTAVU
                        plugin.getServer().getScheduler().runTask(plugin, () -> {
                            plugin.getCharacterManager().performCK(victim);
                            victim.sendMessage("§4§l§k||| §4§lTVÁ POSTAVA BYLA TRVALE USMRCENA (CK) §4§l§k|||");
                            Bukkit.broadcast(net.kyori.adventure.text.Component.text("§cHráč " + victim.getName() + " zemřel trvalou smrtí (CK)."));
                        });

                        // Smazat request z DB
                        PreparedStatement del = conn.prepareStatement("DELETE FROM rp_ck_requests WHERE target_uuid = ?");
                        del.setString(1, victim.getUniqueId().toString());
                        del.executeUpdate();
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }
}