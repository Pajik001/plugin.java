package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class FactionManager {

    private final Main plugin;
    // Cache: UUID -> Faction ID
    private final Map<UUID, Integer> playerFactionMap = new HashMap<>();
    // Cache: UUID -> Role Name
    private final Map<UUID, String> playerRoleMap = new HashMap<>();

    public FactionManager(Main plugin) {
        this.plugin = plugin;
    }

    // --- Admin: Vytváření a správa ---

    public void createFaction(String name, Player owner) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                // 1. Vytvoření frakce
                PreparedStatement ps = conn.prepareStatement("INSERT INTO rp_factions (name, owner_uuid) VALUES (?, ?)");
                ps.setString(1, name);
                ps.setString(2, owner.getUniqueId().toString());
                ps.executeUpdate();
                
                // Získání ID
                int factionId = getLastId(conn);

                // 2. Vytvoření základních rolí (Template)
                createRoleInternal(conn, factionId, "Šéf", 1, 10); // Limit 1, Priorita 10
                createRoleInternal(conn, factionId, "Zástupce", 2, 8); // Limit 2, Priorita 8
                createRoleInternal(conn, factionId, "Člen", 0, 1); // Limit 0 (nekonečno), Priorita 1

                // 3. Přiřazení majitele
                setMemberRoleInternal(conn, owner.getUniqueId(), factionId, "Šéf");
                
                plugin.getLogger().info("Frakce " + name + " vytvořena.");
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public void createCustomRole(String factionName, String roleName, int limit, int priority) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                int factionId = getFactionId(conn, factionName);
                if (factionId == -1) return;
                createRoleInternal(conn, factionId, roleName, limit, priority);
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    // --- Logika členství ---

    public void setPlayerRole(Player target, String factionName, String roleName) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                int factionId = getFactionId(conn, factionName);
                if (factionId == -1) return;

                // Kontrola limitu role
                if (!checkRoleLimit(conn, factionId, roleName)) {
                    // Poslat zprávu někomu (tady nemáme sender, vyřešíme v commandu)
                    return; 
                }

                setMemberRoleInternal(conn, target.getUniqueId(), factionId, roleName);
                
                // Update Cache
                playerFactionMap.put(target.getUniqueId(), factionId);
                playerRoleMap.put(target.getUniqueId(), roleName);
                
                // Update Tablist
                plugin.getServer().getScheduler().runTask(plugin, () -> updateTablist(target));
                
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    // --- Pomocné metody (Internal) ---

    private void createRoleInternal(Connection conn, int factionId, String name, int limit, int priority) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("INSERT INTO rp_faction_roles (faction_id, role_name, member_limit, priority) VALUES (?, ?, ?, ?)");
        ps.setInt(1, factionId);
        ps.setString(2, name);
        ps.setInt(3, limit);
        ps.setInt(4, priority);
        ps.executeUpdate();
    }

    private void setMemberRoleInternal(Connection conn, UUID uuid, int factionId, String roleName) throws SQLException {
        // Získat ID role
        PreparedStatement psRole = conn.prepareStatement("SELECT id FROM rp_faction_roles WHERE faction_id = ? AND role_name = ?");
        psRole.setInt(1, factionId);
        psRole.setString(2, roleName);
        ResultSet rs = psRole.executeQuery();
        if (!rs.next()) return; // Role neexistuje
        int roleId = rs.getInt("id");

        PreparedStatement ps = conn.prepareStatement("REPLACE INTO rp_members (uuid, faction_id, role_id) VALUES (?, ?, ?)");
        ps.setString(1, uuid.toString());
        ps.setInt(2, factionId);
        ps.setInt(3, roleId);
        ps.executeUpdate();
    }

    private boolean checkRoleLimit(Connection conn, int factionId, String roleName) throws SQLException {
        // 1. Zjistit limit
        PreparedStatement psLimit = conn.prepareStatement("SELECT id, member_limit FROM rp_faction_roles WHERE faction_id = ? AND role_name = ?");
        psLimit.setInt(1, factionId);
        psLimit.setString(2, roleName);
        ResultSet rsLimit = psLimit.executeQuery();
        if (!rsLimit.next()) return false;
        
        int limit = rsLimit.getInt("member_limit");
        int roleId = rsLimit.getInt("id");
        if (limit == 0) return true; // Neomezeno

        // 2. Zjistit současný počet
        PreparedStatement psCount = conn.prepareStatement("SELECT COUNT(*) as count FROM rp_members WHERE faction_id = ? AND role_id = ?");
        psCount.setInt(1, factionId);
        psCount.setInt(2, roleId);
        ResultSet rsCount = psCount.executeQuery();
        int current = rsCount.next() ? rsCount.getInt("count") : 0;

        return current < limit;
    }

    private int getFactionId(Connection conn, String name) throws SQLException {
        PreparedStatement ps = conn.prepareStatement("SELECT id FROM rp_factions WHERE name = ?");
        ps.setString(1, name);
        ResultSet rs = ps.executeQuery();
        return rs.next() ? rs.getInt("id") : -1;
    }
    
    private int getLastId(Connection conn) throws SQLException {
        return conn.prepareStatement("SELECT last_insert_rowid()").executeQuery().getInt(1);
    }

    public void updateTablist(Player player) {
        String role = playerRoleMap.get(player.getUniqueId());
        if (role != null) {
            // Zde bys mohl vytáhnout i název frakce z ID, ale pro zjednodušení:
            player.playerListName(Component.text("§e[" + role + "] §f" + player.getName()));
        }
    }
    
    // Pro LockManager: Má hráč přístup k frakčním dveřím?
    public boolean isInFaction(Player player, int factionId) {
        return playerFactionMap.containsKey(player.getUniqueId()) && playerFactionMap.get(player.getUniqueId()) == factionId;
    }
    
    public void loadPlayerCache(Player player) {
        // Voláno při onJoin (PlayerConnectionListener)
        // ... (SQL SELECT z rp_members JOIN rp_faction_roles ...)
        // Uložit do Map<>
    }
}