package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.models.RPCharacter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;

public class CustomItemListener implements Listener {

    private final Main plugin;
    private final Random random = new Random();

    public CustomItemListener(Main plugin) {
        this.plugin = plugin;
    }

    // --- INTERAKCE S ITEMY (Pravý klik) ---
    @EventHandler
    public void onUseItem(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null || !item.hasItemMeta()) return;

        String name = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());

        // 1. 🕊️ POŠTOVNÍ HOLUB
        if (name.contains("Poštovní Holub")) {
            event.setCancelled(true);
            
            // Hráč musí držet v druhé ruce Knihu (Written Book)
            ItemStack offhand = player.getInventory().getItemInOffHand();
            if (offhand.getType() != Material.WRITTEN_BOOK) {
                player.sendMessage("§cMusíš držet podepsanou knihu (dopis) v druhé ruce!");
                return;
            }

            // Otevřít jednoduché GUI se seznamem online hráčů (zjednodušeno pro chat)
            // V reálu bys zde otevřel Inventory s hlavami hráčů.
            player.sendMessage("§eNapiš do chatu jméno hráče, komu chceš holuba poslat (nebo 'cancel'):");
            // Zde bychom museli chytat chat, pro jednoduchost uděláme příkazovou verzi:
            player.sendMessage("§7Použij: /holub poslat <nick>"); 
            // (Logiku příkazu /holub přidáš do RPCommands, kde odebereš holuba a pošleš knihu cíli)
        }

        // 2. 💊 DROGY (Bílý prášek)
        if (name.contains("Bílý prášek") || item.getType() == Material.SUGAR) {
            event.setCancelled(true);
            // Konzumace
            item.setAmount(item.getAmount() - 1);
            player.playSound(player.getLocation(), Sound.ENTITY_SNIFFER_SNIFFING, 1, 1);
            player.sendMessage("§bCítíš nával energie...");
            
            // Boost
            player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60 * 20, 1)); // Speed II na 1 min
            player.addPotionEffect(new PotionEffect(PotionEffectType.HASTE, 60 * 20, 1));

            // Dojezd (Absťák) za minutu
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (player.isOnline()) {
                    player.sendMessage("§cZačíná ti být zle (dojezd)...");
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, 60 * 20, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, 60 * 20, 1));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.NAUSEA, 10 * 20, 1));
                }
            }, 1200L);
        }
        
        // 3. 🚬 DOUTNÍK
        if (name.contains("Doutník")) {
             event.setCancelled(true);
             item.setAmount(item.getAmount() - 1);
             player.playSound(player.getLocation(), Sound.BLOCK_FIRE_AMBIENT, 1, 1);
             
             // RP Hláška
             plugin.getChatBubbleManager().spawnBubble(player, "§7* Zapaluje si doutník *", 4, org.bukkit.Color.GRAY);
             
             // Efekt
             player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 20, 0)); // 20s Nightvision
             // Kouřové particles
             player.getWorld().spawnParticle(org.bukkit.Particle.CAMPFIRE_COSY_SMOKE, player.getLocation().add(0, 1.6, 0), 5, 0, 0.1, 0, 0.05);
        }
    }

    // --- 4. 📜 ZATYKAČ (Interakce s hráčem) ---
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (!(event.getRightClicked() instanceof Player target)) return;
        
        Player cop = event.getPlayer();
        ItemStack item = cop.getInventory().getItemInMainHand();
        
        if (item.hasItemMeta() && item.getItemMeta().displayName() != null) {
            String itemName = PlainTextComponentSerializer.plainText().serialize(item.getItemMeta().displayName());
            
            if (itemName.contains("Zatykač")) {
                // Získat jméno hledaného z Lore nebo NBT
                // Předpokládáme, že v Lore je napsáno: "Hledaný: Jan z Luhačovic"
                var lore = item.getItemMeta().lore();
                if (lore == null || lore.isEmpty()) return;
                
                String wantedNameLine = PlainTextComponentSerializer.plainText().serialize(lore.get(0));
                // Očistíme string (např. "Hledaný: " -> "")
                String wantedName = wantedNameLine.replace("Hledaný:", "").trim();

                RPCharacter targetChar = plugin.getCharacterManager().getCharacter(target);
                if (targetChar == null) return;

                cop.sendMessage("§ePorovnáváš obličej se zatykačem...");
                
                if (targetChar.getFullName().equalsIgnoreCase(wantedName)) {
                    cop.sendMessage("§a§lSHODA! §aToto je hledaná osoba: " + wantedName);
                    cop.playSound(cop.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                } else {
                    cop.sendMessage("§cOsoba neodpovídá popisu na zatykači.");
                }
            }
        }
    }
}