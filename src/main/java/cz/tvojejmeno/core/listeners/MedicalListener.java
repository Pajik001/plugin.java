package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class MedicalListener implements Listener {

    private final Main plugin;

    public MedicalListener(Main plugin) {
        this.plugin = plugin;
    }

    // --- 1. SPÁNEK (Opravený) ---
    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        // Povolit spánek i ve dne (1.21 API: NOT_POSSIBLE_NOW = den/no storm)
        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_NOW) {
            event.setUseBed(org.bukkit.event.Event.Result.ALLOW);
        }

        // Zrychlení noci místo skipu (aby se nezměnil den skokově)
        new BukkitRunnable() {
            @Override
            public void run() {
                Player p = event.getPlayer();
                if (!p.isSleeping()) {
                    this.cancel();
                    return;
                }
                // Přidáme čas (zrychlíme noc 1.5x víc než normálně)
                p.getWorld().setTime(p.getWorld().getTime() + 100); 
                
                // Doplnění energie (pomalu)
                // TODO: Přidat volání do NeedsManageru pro doplnění energie
            }
        }.runTaskTimer(plugin, 10L, 10L); // Každou půl vteřinu
    }

    // --- 2. PITÍ ---
    @EventHandler
    public void onDrink(PlayerItemConsumeEvent event) {
        // Kontrola, jestli pije vodu (Water Bottle)
        if (event.getItem().getType() == Material.POTION) {
            // Nastavíme žízeň na 100 (plná hydratace)
            plugin.getNeedsManager().setThirst(event.getPlayer(), 100.0);
            event.getPlayer().sendMessage("§aNapil ses a zahnal žízeň.");
        }
    }

    // --- 3. DOWNED STAV & SMRT ---
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Pokud už je downed -> nesmí dostat damage od ničeho KROMĚ hráče
        if (plugin.getMedicalManager().isDowned(player)) {
            if (event instanceof EntityDamageByEntityEvent dmgEvent && dmgEvent.getDamager() instanceof Player) {
                return; // Povolit dorážku hráčem
            }
            event.setCancelled(true); // Zrušit oheň, pád, moby...
            return;
        }

        // Pokud by rána byla smrtelná -> aktivovat DOWNED místo smrti
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            // Pokud to byl pád do voidu, necháme ho umřít
            if (event.getCause() == EntityDamageEvent.DamageCause.VOID) return;

            event.setCancelled(true); // Zrušíme smrt
            plugin.getMedicalManager().setDowned(player, true); // Hodíme do bezvědomí
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        // Pokud opravdu zemře (dorážka), vyčistíme stav
        plugin.getMedicalManager().setDowned(event.getEntity(), false);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        // Pojistka
        plugin.getMedicalManager().setDowned(event.getPlayer(), false);
        event.getPlayer().setGliding(false);
    }
}