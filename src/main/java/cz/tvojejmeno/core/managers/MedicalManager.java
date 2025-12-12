package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MedicalManager {

    private final Main plugin;
    // UUID hráče -> Čas kdy zemře (timestamp)
    private final Map<UUID, Long> downedPlayers = new HashMap<>();
    private BukkitTask medicalTask;

    public MedicalManager(Main plugin) {
        this.plugin = plugin;
        startLoop();
    }

    private void startLoop() {
        // Každou vteřinu kontroluje stav zraněných
        medicalTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            
            for (UUID uuid : new HashMap<>(downedPlayers).keySet()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null) continue;

                // Vynucení ležení a efektů
                if (p.getPose() != Pose.SLEEPING) {
                    p.setPose(Pose.SLEEPING); // 1.21 API pro plazení/ležení
                }
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 1, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 5, false, false));

                // Kontrola času (5 minut = 300 000 ms)
                long deathTime = downedPlayers.get(uuid);
                long timeLeft = (deathTime - now) / 1000;

                p.sendActionBar(Component.text("§c§lJSI V BEZVĚDOMÍ! §7Zbývá čas: " + timeLeft + "s"));

                if (now >= deathTime) {
                    // Čas vypršel -> respawn (nemocnice)
                    handleFinalDeath(p);
                }
            }
        }, 20L, 20L);
    }

    public void setDowned(Player player) {
        // 5 minut do smrti
        downedPlayers.put(player.getUniqueId(), System.currentTimeMillis() + (5 * 60 * 1000));
        
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(1.0); // Necháme ho naživu
        player.sendMessage("§c§lUpadl jsi do bezvědomí!");
        player.sendMessage("§7Musí tě ošetřit doktor (/revive), jinak za 5 minut zemřeš.");
    }

    public void revive(Player doctor, Player target) {
        if (!downedPlayers.containsKey(target.getUniqueId())) {
            doctor.sendMessage("§cTento hráč není v bezvědomí.");
            return;
        }

        downedPlayers.remove(target.getUniqueId());
        
        // Vyléčení
        target.setPose(Pose.STANDING);
        target.setHealth(6.0); // Trochu života
        target.removePotionEffect(PotionEffectType.BLINDNESS);
        target.removePotionEffect(PotionEffectType.SLOWNESS);
        
        // Slowness po zvednutí (jak jsi chtěl - 15s)
        target.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 15 * 20, 1));
        
        target.sendMessage("§aByl jsi ošetřen doktorem " + doctor.getName());
        doctor.sendMessage("§aÚspěšně jsi ošetřil hráče.");
    }

    private void handleFinalDeath(Player player) {
        downedPlayers.remove(player.getUniqueId());
        
        // Tady by byl teleport do nemocnice
        player.setHealth(20);
        player.setPose(Pose.STANDING);
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOWNESS);
        
        player.sendMessage("§cZemřel jsi na následky zranění. (Respawn v nemocnici)");
        // player.teleport(nemocniceLoc);
    }

    public boolean isDowned(Player p) {
        return downedPlayers.containsKey(p.getUniqueId());
    }
}