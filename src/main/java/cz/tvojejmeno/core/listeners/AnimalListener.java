package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class AnimalListener implements Listener {

    private final Main plugin;

    public AnimalListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        // Kontrola, zda je to naše RP zvíře
        if (plugin.getAnimalManager().getActiveAnimals().containsKey(event.getEntity().getUniqueId())) {
            
            plugin.getAnimalManager().handleAnimalDeath(event.getEntity().getUniqueId());
            
            if (event.getEntity() instanceof Tameable t && t.getOwner() instanceof Player owner) {
                owner.sendMessage("§c§lTvé zvíře " + (event.getEntity().getCustomName() != null ? event.getEntity().getCustomName() : "zvíře") + " zemřelo!");
                owner.sendMessage("§7Budeš si muset koupit nové.");
            }
        }
    }
}