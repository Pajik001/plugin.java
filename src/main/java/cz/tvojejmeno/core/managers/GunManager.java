package cz.tvojejmeno.core.managers;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;

public class GunManager {
    
    // Jednoduchá metoda pro výstřel
    public void shoot(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        // Kontrola, zda má zbraň (např. motyka jako puška)
        if (item.getType() == Material.IRON_HOE) {
            // Má náboje? (musíš implementovat systém itemů)
            if (!hasAmmo(player)) {
                player.sendMessage("§cCVAK! Nemáš náboje.");
                player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 2);
                return;
            }

            // Výstřel - použijeme Snowball jako projektil (neviditelný, rychlý)
            Snowball bullet = player.launchProjectile(Snowball.class);
            bullet.setVelocity(player.getLocation().getDirection().multiply(3)); // Rychlost
            bullet.setShooter(player);
            bullet.setCustomName("Bullet"); // Označení pro DamageListener

            // Zvuk a efekt
            player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 0.5f);
            
            // Odebrat náboj
            removeAmmo(player);
        }
    }

    private boolean hasAmmo(Player player) {
        // Zde checkni inventory na specifický item (např. Gold Nugget jako náboj)
        return player.getInventory().contains(Material.GOLD_NUGGET);
    }

    private void removeAmmo(Player player) {
        // Odebrání 1 kusu munice
        ItemStack ammo = new ItemStack(Material.GOLD_NUGGET, 1);
        player.getInventory().removeItem(ammo);
    }
}