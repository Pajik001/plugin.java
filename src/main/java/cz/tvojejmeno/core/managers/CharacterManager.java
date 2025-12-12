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
                
                // Načtení potřeb (pokud existují sloupce)
                character.setThirst(rs.getDouble("thirst"));
                character.setSleep(rs.getDouble("sleep"));
                character.setToilet(rs.getInt("toilet"));
                
                characterCache.put(uuid, character);
            } else {
                createEmptyRecord(uuid);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // TUTO METODU JSME NEMĚLI - PROTO SE TO NEUKLÁDALO
    public void saveCharacter(Player player) {
        RPCharacter ch = characterCache.get(player.getUniqueId());
        if (ch == null) return;

        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                String sql = "UPDATE rp_characters SET thirst=?, sleep=?, toilet=?, playtime_minutes=? WHERE uuid=?";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setDouble(1, ch.getThirst());
                ps.setDouble(2, ch.getSleep());
                ps.setInt(3, ch.getToilet());
                ps.setInt(4, ch.getPlaytimeMinutes());
                ps.setString(5, player.getUniqueId().toString());
                ps.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void createEmptyRecord(UUID uuid) {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            PreparedStatement ps = conn.prepareStatement("INSERT INTO rp_characters (uuid, has_character, thirst, sleep, toilet) VALUES (?, 0, 100, 100, 0)");
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
            characterCache.put(uuid, new RPCharacter(uuid, "Neznámý", "Neznámo", 0, false));
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public boolean createCharacter(Player player, String name, String origin, int age) {
        // ... (stejné jako předtím) ...
        // Pro stručnost vynechávám, zkopíruj si tu metodu createCharacter z minula, ta byla OK
        // Jen na konci přidej saveCharacter(player);
        return true; 
    }
    
    public RPCharacter getCharacter(Player player) {
        if (!characterCache.containsKey(player.getUniqueId())) loadCharacter(player);
        return characterCache.get(player.getUniqueId());
    }

    public void unloadCharacter(Player player) {
        saveCharacter(player); // Uložit při odpojení!
        characterCache.remove(player.getUniqueId());
    }
    
    public void performCK(Player player) {
        // ... (stejné jako minule) ...
    }
}