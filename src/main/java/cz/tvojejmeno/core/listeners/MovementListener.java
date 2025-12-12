package cz.tvojejmeno.core.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MovementListener implements Listener {

    private final Main plugin;
    private final Map<UUID, Long> lastLandTime = new HashMap<>(); // Kdy naposledy dopadl na zem
    private final long BASE_JUMP_DELAY = 800; // 0.8s v milisekundách

    public MovementListener(Main plugin) {
        this.plugin = plugin;
    }

    // Detekce dopadu na zem
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player p = event.getPlayer();
        // Pokud je na zemi a předtím nebyl (nebo prostě aktualizujeme čas, dokud je na zemi)
        // isOnGround() v novějších verzích funguje spolehlivě
        if (p.isOnGround()) {
            lastLandTime.put(p.getUniqueId(), System.currentTimeMillis());
        }
    }

    // Detekce pokusu o skok (Paper API)
    @EventHandler
    public void onJump(PlayerJumpEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        double currentWeight = plugin.getBackpackManager().vypocitejVahu(player);
        boolean isOverweight = plugin.getBackpackManager().jePretizen(player);

        // 1. Kontrola Váhy (Zákaz skákání při přetížení)
        if (isOverweight) {
            event.setCancelled(true);
            event.setFrom(event.getFrom()); // Cancel někdy cukne, toto pomáhá
            // Odeslání Action Baru s varováním (jak jsi chtěl)
            player.sendActionBar(Component.text("§c⚖ Jsi příliš těžký na skákání! (" + currentWeight + "kg)"));
            return;
        }

        // 2. Anti-Bunnyhop (Jump Delay)
        long now = System.currentTimeMillis();
        long lastLand = lastLandTime.getOrDefault(player.getUniqueId(), 0L);
        
        // Pokud je přetížený (ale ne nad limit, třeba 40kg), delay se zvyšuje
        long actualDelay = BASE_JUMP_DELAY;
        if (currentWeight > 30.0) {
            actualDelay += 500; // +0.5s pokud neseš nad 30kg
        }

        if (now - lastLand < actualDelay) {
            event.setCancelled(true);
            event.setFrom(event.getFrom());
            // Volitelné: Zpráva nebo zvuk "zadýchání"
        }
    }
}