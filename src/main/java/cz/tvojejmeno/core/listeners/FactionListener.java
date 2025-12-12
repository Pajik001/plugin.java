package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class FactionListener implements Listener {

    private final Main plugin;

    public FactionListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        // OPRAVA: Metoda se nyní jmenuje loadPlayerCache
        plugin.getFactionManager().loadPlayerCache(event.getPlayer());
    }
}