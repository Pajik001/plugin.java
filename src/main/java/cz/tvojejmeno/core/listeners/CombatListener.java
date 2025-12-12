package cz.tvojejmeno.core.listeners;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class CombatListener implements Listener {

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker) || !(event.getEntity() instanceof Player victim)) return;

        ItemStack weapon = attacker.getInventory().getItemInMainHand();

        // --- MACE (Palcát) MECHANIKA ---
        if (weapon.getType() == Material.MACE) {
            // 1. Nerf damage (zrušení násobení pádem)
            // Pokud je damage podezřele vysoký (Mace dává base 6, s pádem klidně 50+), zastropujeme ho.
            if (event.getDamage() > 10.0) {
                event.setDamage(8.0); // Fixní damage (jako silný meč), ignoruje výšku pádu
            }

            // 2. Aplikace efektů (podle zadání)
            // Blindness na 3s (60 ticků), Slowness na 5s (100 ticků)
            victim.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 100, 1));
            
            attacker.sendMessage("§7Zasáhl jsi palcátem a omráčil nepřítele!");
        }
    }
}