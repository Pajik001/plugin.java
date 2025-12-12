package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class NPCListener implements Listener {

    private final Main plugin;

    public NPCListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNPCClick(PlayerInteractEntityEvent event) {
        if (event.getRightClicked().getType() == EntityType.VILLAGER) {
            Villager npc = (Villager) event.getRightClicked();
            
            // Paper API: Získání jména
            if (npc.customName() == null) return;
            
            // Převedeme Component na String (spolehlivě)
            String name = PlainTextComponentSerializer.plainText().serialize(npc.customName());

            // Odstraníme barvy (§) pro jistotu
            name = name.replaceAll("§.", "");

            if (name.contains("Banka")) {
                event.setCancelled(true);
                plugin.getShopManager().openBank(event.getPlayer());
            } else if (name.contains("Úřad") || name.contains("Urad")) {
                event.setCancelled(true);
                plugin.getShopManager().openOffice(event.getPlayer());                
            } else if (name.contains("Stáj") || name.contains("Staj")) {
                event.setCancelled(true);
                plugin.getStableManager().openBuyMenu(event.getPlayer());
            }
        }
    }
}