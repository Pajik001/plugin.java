package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.managers.DropManager;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

public class DropListener implements Listener {

    private final DropManager dropManager;

    public DropListener(DropManager dropManager) {
        this.dropManager = dropManager;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();

        // Drop měníme, jen pokud moba zabil hráč
        if (entity.getKiller() instanceof Player) {
            
            // Pokud je to Zombie
            if (entity instanceof Zombie) {
                // 1. Smažeme původní dropy (Rotten Flesh)
                event.getDrops().clear();

                // 2. Získáme custom drop z Manageru
                ItemStack customLoot = dropManager.getCustomDrop("ZOMBIE");
                
                // 3. Pokud padl (není null), přidáme ho na zem
                if (customLoot != null) {
                    event.getDrops().add(customLoot);
                }
            }
        }
    }
}