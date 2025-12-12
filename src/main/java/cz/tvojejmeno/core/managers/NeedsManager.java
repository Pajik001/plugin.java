package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.models.RPCharacter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

public class NeedsManager {

    private final Main plugin;
    private BukkitTask task;

    public NeedsManager(Main plugin) {
        this.plugin = plugin;
        startLoop();
    }

    private void startLoop() {
        // Běží každou vteřinu (20 ticků)
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                RPCharacter ch = plugin.getCharacterManager().getCharacter(player);
                if (ch == null || !ch.hasCharacter()) continue;

                // 1. Ubývání potřeb (hodnoty z configu / 60, protože loop je 1s a config je per minute)
                double thirstLoss = plugin.getConfig().getDouble("needs.thirst-loss-per-minute", 0.8) / 60.0;
                double toiletGain = plugin.getConfig().getDouble("needs.toilet-gain-per-minute", 0.6) / 60.0;
                
                ch.setThirst(ch.getThirst() - thirstLoss);
                ch.setToilet(ch.getToilet() + (int)Math.ceil(toiletGain));
                
                // Spánek řešíme jen když nespí (zde zjednodušeno)
                ch.setSleep(ch.getSleep() - (plugin.getConfig().getDouble("needs.sleep-loss-per-minute", 0.5) / 60.0));

                // 2. Postihy
                if (ch.getThirst() <= 0) player.damage(1.0); // Dehydratace
                
                // 3. Váha
                double weight = plugin.getBackpackManager().vypocitejVahu(player);
                double maxWeight = 50.0;
                String weightColor = (weight > maxWeight) ? "§c" : "§a";

                // 4. Sestavení Action Baru
                // Formát: 💧 80% | 💤 90% | 🚽 10% | ⚖ 15/50kg
                String hud = String.format("§b💧 %d%% §8| §9💤 %d%% §8| §6🚽 %d%% §8| %s⚖ %.1f/%.0fkg", 
                        (int)ch.getThirst(), 
                        (int)ch.getSleep(), 
                        ch.getToilet(),
                        weightColor, weight, maxWeight);

                player.sendActionBar(Component.text(hud));
            }
        }, 20L, 20L);
    }
}