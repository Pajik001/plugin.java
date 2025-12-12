package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class LockManager {

    private final Main plugin;
    private final Set<Location> lockedBlocks = new HashSet<>();

    public LockManager(Main plugin) {
        this.plugin = plugin;
        loadLocks();
    }

    private void loadLocks() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM rp_locks");
                ResultSet rs = ps.executeQuery();
                while (rs.next()) {
                    String world = rs.getString("location_world");
                    int x = rs.getInt("location_x");
                    int y = rs.getInt("location_y");
                    int z = rs.getInt("location_z");
                    if (plugin.getServer().getWorld(world) != null) {
                        lockedBlocks.add(new Location(plugin.getServer().getWorld(world), x, y, z));
                    }
                }
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public boolean isLocked(Block block) {
        return lockedBlocks.contains(block.getLocation());
    }

    public boolean canAccess(Player player, Block block) {
        if (!isLocked(block)) return true;
        if (player.hasPermission("core.admin.bypass")) return true;

        // 1. Zjistit Faction ID zámku
        int lockFactionId = getLockFactionId(block);
        
        // 2. Pokud patří frakci a hráč je v ní -> Povolit
        if (lockFactionId > 0) {
            if (plugin.getFactionManager().isInFaction(player, lockFactionId)) {
                return true;
            }
        }

        // 3. Kontrola Ownera a Keys (SQL)
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT owner_uuid, allowed_players FROM rp_locks WHERE location_world=? AND location_x=? AND location_y=? AND location_z=?");
            Location loc = block.getLocation();
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String owner = rs.getString("owner_uuid");
                String allowed = rs.getString("allowed_players"); 

                if (owner.equals(player.getUniqueId().toString())) return true;
                if (allowed != null && allowed.contains(player.getUniqueId().toString())) return true;
            }
        } catch (SQLException e) { e.printStackTrace(); }
        
        return false;
    }

    // Pomocná metoda pro získání ID frakce zámku
    private int getLockFactionId(Block block) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT faction_id FROM rp_locks WHERE location_world=? AND location_x=? AND location_y=? AND location_z=?");
            Location loc = block.getLocation();
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("faction_id");
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }

    public void createLock(Player owner, Block block) {
        Location loc = block.getLocation();
        lockedBlocks.add(loc);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO rp_locks (location_world, location_x, location_y, location_z, owner_uuid) VALUES (?, ?, ?, ?, ?)");
                ps.setString(1, loc.getWorld().getName());
                ps.setInt(2, loc.getBlockX());
                ps.setInt(3, loc.getBlockY());
                ps.setInt(4, loc.getBlockZ());
                ps.setString(5, owner.getUniqueId().toString());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public void removeLock(Block block) {
        Location loc = block.getLocation();
        lockedBlocks.remove(loc);

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("DELETE FROM rp_locks WHERE location_world=? AND location_x=? AND location_y=? AND location_z=?");
                ps.setString(1, loc.getWorld().getName());
                ps.setInt(2, loc.getBlockX());
                ps.setInt(3, loc.getBlockY());
                ps.setInt(4, loc.getBlockZ());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }
    
    public void breakLock(Block block) {
        removeLock(block); 
        block.getWorld().playSound(block.getLocation(), org.bukkit.Sound.ENTITY_ZOMBIE_BREAK_WOODEN_DOOR, 1, 1);
        if (block.getBlockData() instanceof Openable op) {
            op.setOpen(true);
            block.setBlockData(op);
        }
    }

    // --- Metody pro /key ---

    public Set<String> getAllowedPlayers(Block block) {
        Set<String> set = new HashSet<>();
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT allowed_players FROM rp_locks WHERE location_world=? AND location_x=? AND location_y=? AND location_z=?");
            Location loc = block.getLocation();
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String raw = rs.getString("allowed_players");
                if (raw != null && !raw.isEmpty()) {
                    String[] parts = raw.split(",");
                    for (String s : parts) set.add(s);
                }
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return set;
    }

    public void addAccess(Block block, UUID playerUUID) {
        Set<String> allowed = getAllowedPlayers(block);
        allowed.add(playerUUID.toString());
        updateAllowedPlayers(block, allowed);
    }

    public void removeAccess(Block block, UUID playerUUID) {
        Set<String> allowed = getAllowedPlayers(block);
        allowed.remove(playerUUID.toString());
        updateAllowedPlayers(block, allowed);
    }

    private void updateAllowedPlayers(Block block, Set<String> allowed) {
        String data = String.join(",", allowed);
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("UPDATE rp_locks SET allowed_players = ? WHERE location_world=? AND location_x=? AND location_y=? AND location_z=?");
                Location loc = block.getLocation();
                ps.setString(1, data);
                ps.setString(2, loc.getWorld().getName());
                ps.setInt(3, loc.getBlockX());
                ps.setInt(4, loc.getBlockY());
                ps.setInt(5, loc.getBlockZ());
                ps.executeUpdate();
            } catch (SQLException e) { e.printStackTrace(); }
        });
    }

    public boolean isOwner(Block block, Player player) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT owner_uuid FROM rp_locks WHERE location_world=? AND location_x=? AND location_y=? AND location_z=?");
            Location loc = block.getLocation();
            ps.setString(1, loc.getWorld().getName());
            ps.setInt(2, loc.getBlockX());
            ps.setInt(3, loc.getBlockY());
            ps.setInt(4, loc.getBlockZ());
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("owner_uuid").equals(player.getUniqueId().toString());
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return false;
    }
}