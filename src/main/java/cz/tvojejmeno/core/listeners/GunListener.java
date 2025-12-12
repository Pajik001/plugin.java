package cz.tvojejmeno.core.listeners;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GunListener implements Listener {

    // --- ČÁST A: STŘELBA ---
    @EventHandler
    public void onShoot(PlayerInteractEvent event) {
        // Reagujeme na pravé kliknutí (do vzduchu nebo na blok)
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Player player = event.getPlayer();
            ItemStack item = event.getItem();

            if (isCursedWeapon(item)) {
                // Zrušíme normální interakci (aby nepokládal bloky, pokud je to blok)
                event.setCancelled(true);

                // Vystřelíme "kulku" (Snowball)
                Snowball bullet = player.launchProjectile(Snowball.class);
                bullet.setVelocity(player.getLocation().getDirection().multiply(3.0)); // Rychlost
                bullet.setShooter(player);

                // Zvukový efekt
                player.playSound(player.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_BLAST, 1.0f, 0.5f);
            }
        }
    }

    // --- ČÁST B: ZAMYKÁNÍ SLOTU (Prokletí) ---
    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int previousSlot = event.getPreviousSlot();
        
        // Získáme item, který hráč držel PŘED pokusem o změnu
        ItemStack heldItem = player.getInventory().getItem(previousSlot);

        if (isCursedWeapon(heldItem)) {
            // Zakážeme změnu slotu -> hra ho vrátí zpět na zbraň
            event.setCancelled(true);
            
            // Volitelné pípnutí
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 0.5f);
        }
    }

    // --- Pomocná metoda pro identifikaci zbraně ---
    private boolean isCursedWeapon(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        
        ItemMeta meta = item.getItemMeta();
        
        // Zbraň poznáme tak, že se jmenuje "AK-47" (nebo cokoliv tam napíšeš)
        if (meta.hasDisplayName() && meta.getDisplayName().contains("AK-47")) {
            return true;
        }
        return false;
    }
}