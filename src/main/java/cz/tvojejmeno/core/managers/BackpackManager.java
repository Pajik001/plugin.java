package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class BackpackManager {

    private final Main plugin;
    // Limit váhy (příklad: 50 jednotek)
    private final double MAX_VAHA = 50.0;

    public BackpackManager(Main plugin) {
        this.plugin = plugin;
    }

    /**
     * Vrátí true, pokud hráč nese více než MAX_VAHA.
     */
    public boolean jePretizen(Player player) {
        return vypocitejVahu(player) > MAX_VAHA;
    }

    /**
     * Sečte váhu všech věcí v inventáři.
     */
    public double vypocitejVahu(Player player) {
        double totalWeight = 0;
        
        // Projdeme itemy v inventáři (včetně brnění a off-handu v novějších verzích)
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() != Material.AIR) {
                totalWeight += getVahaItemu(item);
            }
        }
        return totalWeight;
    }

    /**
     * Určí váhu konkrétního stacku itemů.
     */
    private double getVahaItemu(ItemStack item) {
        double vahaZaKus = 0.1; // Základní váha (pírko, kytka...)
        Material type = item.getType();

        // Tady definuj těžké věci
        if (type == Material.STONE || type == Material.COBBLESTONE || type == Material.DEEPSLATE) {
            vahaZaKus = 1.0;
        } else if (type == Material.IRON_BLOCK || type == Material.ANVIL || type == Material.OBSIDIAN) {
            vahaZaKus = 5.0;
        } else if (type == Material.NETHERITE_CHESTPLATE || type == Material.NETHERITE_INGOT) {
            vahaZaKus = 10.0;
        } else if (type == Material.GOLD_INGOT || type == Material.RAW_GOLD) {
            vahaZaKus = 2.0;
        }

        return vahaZaKus * item.getAmount();
    }
}