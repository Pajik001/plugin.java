package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.models.RPCharacter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class NeedsManager {

    private final Main plugin;

    public NeedsManager(Main plugin) {
        this.plugin = plugin;
        startLoop();
    }

    private void startLoop() {
        // Loop běží každou vteřinu (20 ticks)
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                RPCharacter ch = plugin.getCharacterManager().getCharacter(player);
                if (ch == null || !ch.hasCharacter()) continue;

                // --- MATEMATIKA ---
                // Config hodnoty jsou "za minutu". Dělíme 60, abychom dostali "za vteřinu".
                double thirstLoss = plugin.getConfig().getDouble("needs.thirst-loss-per-minute", 0.8) / 60.0;
                double toiletGain = plugin.getConfig().getDouble("needs.toilet-gain-per-minute", 0.6) / 60.0;
                
                // Používáme přesná čísla (double), ne int!
                ch.setThirst(ch.getThirst() - thirstLoss);
                ch.setToilet((int) (ch.getToilet() + toiletGain)); // Tady pozor, ch.toilet je int v modelu?
                // V modelu RPCharacter změň toilet na double, nebo to dělej takto:
                // Lepší je mít v RPCharacter všechno jako double a int vracet jen pro zobrazení.
                
                // Pokud nemůžeš změnit model, uděláme "náhodu" (probabilistický přístup)
                // Pokud je zisk 0.01/s, tak je 1% šance každou vteřinu, že se přičte 1.
                if (Math.random() < toiletGain) {
                    ch.setToilet(ch.getToilet() + 1);
                }

                // Spánek (jen pokud nespí)
                if (!player.isSleeping()) {
                     double sleepLoss = plugin.getConfig().getDouble("needs.sleep-loss-per-minute", 0.5) / 60.0;
                     ch.setSleep(ch.getSleep() - sleepLoss);
                }

                // --- ZOBRAZENÍ ---
                String hud = String.format("§b💧 %d%% §8| §9💤 %d%% §8| §6🚽 %d%%", 
                        (int)ch.getThirst(), (int)ch.getSleep(), ch.getToilet());
                player.sendActionBar(Component.text(hud));
            }
        }, 20L, 20L);
    }
}