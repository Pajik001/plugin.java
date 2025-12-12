package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent.RegainReason; 
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class MedicalListener implements Listener {

    private final Main plugin;

    public MedicalListener(Main plugin) {
        this.plugin = plugin;
    }

    // --- 1. Zákaz přirozené regenerace ---
    @EventHandler
    public void onRegen(EntityRegainHealthEvent event) {
        if (!(event.getEntity() instanceof Player)) return;

        // OPRAVA: Používáme SATIATED podle tvé verze API
        if (event.getRegainReason() == RegainReason.SATIATED || 
            event.getRegainReason() == RegainReason.REGEN) {
            
            event.setCancelled(true);
        }
    }

    // --- 2. Použití lékárničky/obvazu ---
    @EventHandler
    public void onUseItem(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        if (item == null || !item.hasItemMeta()) return;

        String name = "";
        if (item.getItemMeta().hasDisplayName()) {
            name = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
        }

        if (name.contains("Obvaz")) {
            event.setCancelled(true);
            applyHealing(player, item, 4.0, "Obvazuje si rány...", 3); 
        } 
        else if (name.contains("Léky") || name.contains("Antibiotika")) {
            event.setCancelled(true);
            applyHealing(player, item, 2.0, "Bere léky...", 2); 
            player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 100, 0)); 
        } 
        else if (name.contains("Lékařský kufřík")) {
            event.setCancelled(true);
            applyHealing(player, item, 10.0, "Ošetřuje se kufříkem...", 5); 
        }
    }

    private void applyHealing(Player player, ItemStack item, double healAmount, String rpAction, int seconds) {
        plugin.getChatBubbleManager().spawnBubble(player, "§6* " + rpAction + " *", seconds, org.bukkit.Color.fromARGB(100, 255, 170, 0));
        
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1f, 1f);
        item.setAmount(item.getAmount() - 1);

        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !player.isDead()) {
                double maxHealth = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_MAX_HEALTH).getValue();
                double newHealth = Math.min(player.getHealth() + healAmount, maxHealth);
                
                player.setHealth(newHealth);
                player.sendMessage("§aCítíš se lépe.");
            }
        }, seconds * 20L);
    }
}