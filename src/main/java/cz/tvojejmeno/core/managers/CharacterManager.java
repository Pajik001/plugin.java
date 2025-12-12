package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.models.RPCharacter;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CharacterManager {

    private final Main plugin;
    private final Map<UUID, RPCharacter> characterCache = new HashMap<>();

    public CharacterManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadCharacter(Player player) {
        UUID uuid = player.getUniqueId();
        plugin.getLogger().info("Nacitam postavu pro: " + player.getName());

        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("SELECT * FROM rp_characters WHERE uuid = ?");
            ps.setString(1, uuid.toString());
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                boolean hasChar = rs.getInt("has_character") == 1;
                String name = rs.getString("rp_name");
                String origin = rs.getString("origin");
                int age = rs.getInt("age");
                
                RPCharacter character = new RPCharacter(uuid, name, origin, age, hasChar);
                characterCache.put(uuid, character);
                plugin.getLogger().info("Postava nactena z DB: " + (hasChar ? name : "Zadna (Nema Char)"));
            } else {
                createEmptyRecord(uuid);
                plugin.getLogger().info("Vytvoren prazdny zaznam pro noveho hrace.");
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Chyba SQL loadCharacter: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createEmptyRecord(UUID uuid) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO rp_characters (uuid, has_character) VALUES (?, 0)");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
            characterCache.put(uuid, new RPCharacter(uuid, "Neznámý", "Neznámo", 0, false));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean createCharacter(Player player, String name, String origin, int age) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE rp_characters SET rp_name = ?, origin = ?, age = ?, has_character = 1 WHERE uuid = ?");
            ps.setString(1, name);
            ps.setString(2, origin);
            ps.setInt(3, age);
            ps.setString(4, player.getUniqueId().toString());
            ps.executeUpdate();

            RPCharacter character = new RPCharacter(player.getUniqueId(), name, origin, age, true);
            characterCache.put(player.getUniqueId(), character);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    public RPCharacter getCharacter(Player player) {
        if (!characterCache.containsKey(player.getUniqueId())) {
            // Pokud není v cache, zkusíme nouzově načíst synchronně (aby to nevrátilo null)
            loadCharacter(player);
        }
        return characterCache.get(player.getUniqueId());
    }

    public void unloadCharacter(Player player) {
        characterCache.remove(player.getUniqueId());
    }
    
    public void performCK(Player player) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("UPDATE rp_characters SET has_character = 0, rp_name = NULL, origin = NULL, wallet_balance = 0 WHERE uuid = ?");
            ps.setString(1, player.getUniqueId().toString());
            ps.executeUpdate();
            characterCache.remove(player.getUniqueId());
            loadCharacter(player);
            player.getInventory().clear();
            player.setHealth(0);
        } catch (SQLException e) { e.printStackTrace(); }
    }
}