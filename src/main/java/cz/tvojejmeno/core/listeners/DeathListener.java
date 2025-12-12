package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class DeathListener implements Listener {

    private final Main plugin;

    public DeathListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // 1. Skrýt smrt pro hráče (Adminům pošleme info)
        event.deathMessage(null); 
        String adminMsg = "§c[AdminInfo] Hráč " + player.getName() + " zemřel na: " + player.getLastDamageCause().getCause();
        Bukkit.broadcast(net.kyori.adventure.text.Component.text(adminMsg), "core.admin");

        // 2. Kontrola CK
        plugin.getCkManager().checkCKDeath(player);

        // 3. Kontrola Bounty (z minula)
        if (player.getKiller() != null) {
            plugin.getBountyManager().checkBounty(player.getKiller(), player);
        }
    }
}