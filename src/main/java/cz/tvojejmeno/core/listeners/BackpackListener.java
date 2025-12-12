package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.managers.BackpackManager;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class BackpackListener implements Listener {

    private final BackpackManager backpackManager;

    public BackpackListener(BackpackManager backpackManager) {
        this.backpackManager = backpackManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        // 1. Optimalizace: Pokud se hráč nepohnul (jen otáčel hlavou), nic nepočítáme
        if (to == null || (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ())) {
            return;
        }

        // 2. VÝJIMKA: Pokud má Creative nebo Létá, váhu neřešíme
        if (player.getGameMode() == GameMode.CREATIVE || player.isFlying()) {
            return;
        }

        // 3. Detekce pohybu nahoru (skok nebo chůze do schodů)
        if (to.getY() > from.getY()) {
            // Zeptáme se manageru, jestli je hráč tlustý
            if (backpackManager.jePretizen(player)) {
                
                // Zrušíme pohyb (hráč se "zasekne" ve vzduchu/na zemi)
                event.setCancelled(true);
                
                // Pošleme zprávu (používám moderní Paper API komponenty)
                player.sendActionBar(Component.text("§cJsi příliš těžký na skákání!"));
            }
        }
    }
}