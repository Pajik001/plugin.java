package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.models.RPCharacter;
import io.papermc.paper.event.player.PlayerBedFailEnterEvent;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionType;

public class MedicalListener implements Listener {

    private final Main plugin;

    public MedicalListener(Main plugin) {
        this.plugin = plugin;
    }

    // --- PITÍ VODY ---
    @EventHandler
    public void onDrink(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item.getType() == Material.POTION) {
            PotionMeta meta = (PotionMeta) item.getItemMeta();
            if (meta.getBasePotionType() == PotionType.WATER) {
                RPCharacter ch = plugin.getCharacterManager().getCharacter(event.getPlayer());
                if (ch != null) {
                    ch.setThirst(ch.getThirst() + 30.0); // Doplní 30%
                    event.getPlayer().sendMessage("§bOsvěžil ses.");
                }
            }
        }
    }

    // --- SPÁNEK (Den i Noc) ---
    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        // Povolit spánek vždy (i ve dne)
        if (event.getBedEnterResult() == PlayerBedEnterEvent.BedEnterResult.NOT_POSSIBLE_DAY) {
            event.setUseBed(org.bukkit.event.Event.Result.ALLOW);
        }
    }
    
    // Zrychlení noci a regenerace spánku se musí řešit v Scheduleru (v NeedsManager nebo Main),
    // protože Event proběhne jen jednou při ulehnutí.
    // Přidej do NeedsManager loopu:
    /*
    if (player.isSleeping()) {
        ch.setSleep(ch.getSleep() + 1.0); // +1% za vteřinu
        // Zrychlení času
        player.getWorld().setTime(player.getWorld().getTime() + 50); 
    }
    */

    // --- DOWNED STATE FIX ---
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onFatalDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Pokud už je downed, ignorovat damage, pokud to NENÍ od hráče
        if (plugin.getMedicalManager().isDowned(player)) {
            if (event.getCause() != EntityDamageEvent.DamageCause.ENTITY_ATTACK) {
                event.setCancelled(true); // PvE damage (hoření, pád) nezabije downed hráče
            }
            // Pokud je to hráč, necháme damage projít (aby ho mohl dorazit/okrást)
            return; 
        }

        // Pokud by měl umřít a není downed -> Downed
        if (player.getHealth() - event.getFinalDamage() <= 0) {
            event.setCancelled(true);
            plugin.getMedicalManager().setDowned(player);
            // Plazení
            player.setGliding(true); // Někdy pomůže setGliding pro animaci
            player.setPose(Pose.SWIMMING); // Nebo SWIMMING pro plazení na suchu (1.21 mechanika)
        }
    }
    
    // Oprava respawnu: Pokud se hráč připojí a je "mrtvý" v DB, nebo bugne,
    // MedicalManager by měl při loadu zkontrolovat stav.
}