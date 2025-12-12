package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.inventory.ItemStack;

public class GunManager {
    private final Main plugin;

    public GunManager(Main plugin) {
        this.plugin = plugin;
    }

    public void shoot(Player player) {
        // Kontrola nábojů (třeba Iron Nugget)
        if (!player.getInventory().containsAtLeast(new ItemStack(Material.IRON_NUGGET), 1)) {
            player.sendMessage("§cNemáš munici!");
            player.playSound(player.getLocation(), Sound.BLOCK_DISPENSER_FAIL, 1, 2);
            return;
        }

        // Výstřel
        player.getInventory().removeItem(new ItemStack(Material.IRON_NUGGET, 1));
        Snowball bullet = player.launchProjectile(Snowball.class);
        bullet.setVelocity(player.getLocation().getDirection().multiply(3.0));
        bullet.setShooter(player);
        bullet.setCustomName("Bullet"); // Abychom poznali v CombatListeneru, že to je kulka

        player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1, 0.5f);
    }
}