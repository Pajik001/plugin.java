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
    // UUID -> Timestamp smrti
    private final Map<UUID, Long> downedPlayers = new HashMap<>();
    
    public MedicalManager(Main plugin) {
        this.plugin = plugin;
        startLoop();
    }

    private void startLoop() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            long now = System.currentTimeMillis();
            
            // Iterujeme přes kopii, abychom mohli mazat
            for (UUID uuid : new HashMap<>(downedPlayers).keySet()) {
                Player p = Bukkit.getPlayer(uuid);
                if (p == null || !p.isOnline()) continue;

                // 1. PLAZENÍ (Gliding + Swimming pose funguje v 1.21 nejlépe)
                if (!p.isGliding()) p.setGliding(true);
                // p.setPose(Pose.SWIMMING); // Volitelné, gliding to dělá taky

                // 2. Efekty
                p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 40, 0, false, false));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 40, 5, false, false));

                // 3. Časovač
                long deathTime = downedPlayers.get(uuid);
                long timeLeft = (deathTime - now) / 1000;
                p.sendActionBar(Component.text("§c§lJSI V BEZVĚDOMÍ! §7Zbývá: " + Math.max(0, timeLeft) + "s"));

                if (now >= deathTime) {
                    handleFinalDeath(p);
                }
            }
        }, 20L, 20L);
    }

    // Listener volá toto:
    public void setDowned(Player player, boolean state) {
        if (state) {
            // Aktivace
            downedPlayers.put(player.getUniqueId(), System.currentTimeMillis() + (5 * 60 * 1000));
            player.setGameMode(GameMode.ADVENTURE);
            player.setHealth(1.0);
            player.setGliding(true); // Důležité pro plazení
            player.sendMessage("§c§lUpadl jsi do bezvědomí!");
        } else {
            // Deaktivace (Respawn/Revive)
            downedPlayers.remove(player.getUniqueId());
            player.setGliding(false);
            player.setPose(Pose.STANDING);
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOWNESS);
            if (player.getGameMode() == GameMode.ADVENTURE) {
                player.setGameMode(GameMode.SURVIVAL);
            }
        }
    }

    public void revive(Player doctor, Player target) {
        if (!isDowned(target)) {
            doctor.sendMessage("§cHráč není v bezvědomí.");
            return;
        }
        setDowned(target, false); // Probrat
        target.setHealth(4.0);
        target.sendMessage("§aByl jsi ošetřen.");
        doctor.sendMessage("§aÚspěšně jsi ošetřil hráče.");
    }

    private void handleFinalDeath(Player player) {
        setDowned(player, false); // Vyčistit stav, aby se nerespawnul jako downed
        player.setHealth(0.0); // Zabít doopravdy
    }

    public boolean isDowned(Player p) {
        return downedPlayers.containsKey(p.getUniqueId());
    }
}