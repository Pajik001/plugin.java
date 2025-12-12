package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.entity.*;
import org.bukkit.entity.Horse.Color;
import org.bukkit.entity.Horse.Style;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AnimalManager {

    private final Main plugin;
    private final Map<UUID, Integer> activeAnimals = new HashMap<>();

    public AnimalManager(Main plugin) {
        this.plugin = plugin;
        createTable();
    }

    private void createTable() {
        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS rp_animals (" +
                    "id INT AUTO_INCREMENT PRIMARY KEY, " +
                    "owner_uuid VARCHAR(36), " +
                    "type VARCHAR(32), " + 
                    "variant VARCHAR(64), " + 
                    "name VARCHAR(64), " +
                    "is_dead BOOLEAN DEFAULT FALSE" +
                    ");";
            conn.createStatement().executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void buyAnimal(Player player, EntityType type, String variant, String name) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("INSERT INTO rp_animals (owner_uuid, type, variant, name) VALUES (?, ?, ?, ?)");
                ps.setString(1, player.getUniqueId().toString());
                ps.setString(2, type.toString());
                ps.setString(3, variant);
                ps.setString(4, name);
                ps.executeUpdate();
                
                player.sendMessage("§aZakoupil jsi zvíře: " + name);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void spawnAnimal(Player player, int animalId) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM rp_animals WHERE id = ? AND owner_uuid = ? AND is_dead = 0");
                ps.setInt(1, animalId);
                ps.setString(2, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    String typeStr = rs.getString("type");
                    String variantStr = rs.getString("variant");
                    String name = rs.getString("name");

                    plugin.getServer().getScheduler().runTask(plugin, () -> {
                        Location loc = player.getLocation();
                        EntityType type = EntityType.valueOf(typeStr);
                        Tameable entity = (Tameable) player.getWorld().spawnEntity(loc, type);
                        
                        entity.setTamed(true);
                        entity.setOwner(player);
                        entity.setCustomName(name);
                        entity.setCustomNameVisible(true);

                        applyVariant(entity, variantStr);

                        activeAnimals.put(entity.getUniqueId(), animalId);
                        player.sendMessage("§aZvíře " + name + " bylo přivoláno.");
                    });
                } else {
                    player.sendMessage("§cToto zvíře neexistuje nebo je mrtvé.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    private void applyVariant(Entity entity, String variant) {
        // OPRAVA: Použití Registry pro 1.21
        if (entity instanceof Horse horse) {
            try {
                horse.setColor(Color.valueOf(variant));
                horse.setStyle(Style.NONE);
                horse.getInventory().setSaddle(new org.bukkit.inventory.ItemStack(org.bukkit.Material.SADDLE));
            } catch (Exception ignored) {}
        } 
        else if (entity instanceof Wolf wolf) {
            try {
                // Převod String na NamespacedKey (např. "PALE" -> "minecraft:pale")
                NamespacedKey key = NamespacedKey.minecraft(variant.toLowerCase());
                Wolf.Variant v = Registry.WOLF_VARIANT.get(key);
                if (v != null) wolf.setVariant(v);
            } catch (Exception ignored) {}
        } 
        else if (entity instanceof Cat cat) {
            try {
                NamespacedKey key = NamespacedKey.minecraft(variant.toLowerCase());
                Cat.Type t = Registry.CAT_VARIANT.get(key);
                if (t != null) cat.setCatType(t);
            } catch (Exception ignored) {}
        }
    }

    public void handleAnimalDeath(UUID entityUuid) {
        if (activeAnimals.containsKey(entityUuid)) {
            int dbId = activeAnimals.remove(entityUuid);
            
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                    PreparedStatement ps = conn.prepareStatement("UPDATE rp_animals SET is_dead = 1 WHERE id = ?");
                    ps.setInt(1, dbId);
                    ps.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
        }
    }
    
    public Map<UUID, Integer> getActiveAnimals() {
        return activeAnimals;
    }
}