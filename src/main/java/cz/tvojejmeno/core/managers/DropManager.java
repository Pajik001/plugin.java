package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class DropManager {

    private final Main plugin;
    private final Random random;
    // Mapa pro ukládání custom dropů (Mob -> Seznam dropů ve formátu "pravděpodobnost%materiál")
    private final Map<EntityType, List<String>> customDrops = new HashMap<>();

    public DropManager(Main plugin) {
        this.plugin = plugin;
        this.random = new Random();
    }

    // Metoda volaná příkazem /mobdrop set
    public void setDrops(EntityType type, List<String> drops) {
        customDrops.put(type, drops);
    }

    // Metoda volaná Listenerem při smrti moba
    public ItemStack getCustomDrop(EntityType mobType) {
        // 1. Zkontrolujeme, zda máme nastavené dropy v mapě (z příkazu)
        if (customDrops.containsKey(mobType)) {
            List<String> dropList = customDrops.get(mobType);
            for (String dropEntry : dropList) {
                // Formát: "12%leather"
                String[] parts = dropEntry.split("%");
                if (parts.length == 2) {
                    try {
                        int chance = Integer.parseInt(parts[0]);
                        String matName = parts[1];
                        
                        if (random.nextInt(100) < chance) {
                            Material mat = Material.matchMaterial(matName);
                            if (mat != null) return new ItemStack(mat);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        // 2. Defaultní logika (pokud není nastaveno jinak)
        if (mobType == EntityType.ZOMBIE) {
            if (random.nextInt(100) < 50) {
                ItemStack loot = new ItemStack(Material.IRON_NUGGET, 1);
                ItemMeta meta = loot.getItemMeta();
                if (meta != null) {
                    meta.displayName(net.kyori.adventure.text.Component.text("§eStará Mince"));
                    loot.setItemMeta(meta);
                }
                return loot;
            }
        }
        
        return null;
    }
    
    // Pro zpětnou kompatibilitu se stringem
    public ItemStack getCustomDrop(String mobName) {
        try {
            return getCustomDrop(EntityType.valueOf(mobName.toUpperCase()));
        } catch (Exception e) {
            return null;
        }
    }
}