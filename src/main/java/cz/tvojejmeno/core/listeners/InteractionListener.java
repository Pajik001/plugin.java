package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.managers.LockManager;
import cz.tvojejmeno.core.managers.MedicalManager;
import cz.tvojejmeno.core.models.RPCharacter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.block.data.Openable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class InteractionListener implements Listener {

    private final Main plugin;
    private final LockManager lockManager;
    private final MedicalManager medicalManager;
    private final Random random = new Random();
    private final Map<UUID, UUID> cuffedPlayers = new HashMap<>();

    public InteractionListener(Main plugin) {
        this.plugin = plugin;
        this.lockManager = plugin.getLockManager();
        this.medicalManager = plugin.getMedicalManager();
    }

    // --- 1. LOOTING (Okrádání) ---
    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Player target)) return;
        Player thief = event.getPlayer();

        if (!thief.isSneaking()) return;

        // Zde jen kontrolujeme, zda je cíl indisponován
        boolean isDowned = medicalManager.isDowned(target);
        boolean isCuffed = cuffedPlayers.containsKey(target.getUniqueId());
        
        if (isDowned || isCuffed) {
            thief.openInventory(target.getInventory());
            RPCharacter ch = plugin.getCharacterManager().getCharacter(target);
            String rpInfo = (ch != null) ? ch.getFullName() + " (" + ch.getAge() + " let)" : target.getName();
            thief.sendMessage("§e§lProhledáváš: §f" + rpInfo);
            target.sendMessage("§c§lNěkdo tě prohledává...");
        }
    }

    // --- 2. POUTA ---
    @EventHandler
    public void onPlayerHitPlayer(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim) || !(event.getDamager() instanceof Player attacker)) return;
        ItemStack item = attacker.getInventory().getItemInMainHand();

        if (item.getType() == Material.BLAZE_ROD) { // Pouta
            event.setCancelled(true); 
            if (cuffedPlayers.containsKey(victim.getUniqueId())) {
                attacker.sendMessage("§cTento hráč už je spoután.");
                return;
            }
            cuffedPlayers.put(victim.getUniqueId(), attacker.getUniqueId());
            victim.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, Integer.MAX_VALUE, 255));
            victim.addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, Integer.MAX_VALUE, 2));
            
            // Shazování brnění (aby nemohl bojovat)
            for (ItemStack armor : victim.getInventory().getArmorContents()) {
                if (armor != null && armor.getType() != Material.AIR) {
                    if (victim.getInventory().firstEmpty() != -1) victim.getInventory().addItem(armor);
                    else victim.getWorld().dropItemNaturally(victim.getLocation(), armor);
                }
            }
            victim.getInventory().setArmorContents(null);
            attacker.sendMessage("§aSpoutal jsi hráče " + victim.getName());
            victim.sendMessage("§cBYL JSI SPOUTÁN!");
        }
        else if (item.getType() == Material.TRIAL_KEY) { // Klíč k poutům
            event.setCancelled(true);
            if (cuffedPlayers.containsKey(victim.getUniqueId())) {
                uncuff(victim);
                attacker.sendMessage("§aOdpoutal jsi hráče.");
            }
        }
    }

    private void uncuff(Player victim) {
        cuffedPlayers.remove(victim.getUniqueId());
        victim.removePotionEffect(PotionEffectType.WEAKNESS);
        victim.removePotionEffect(PotionEffectType.SLOWNESS);
        victim.sendMessage("§aByl jsi odpoután.");
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player victim = event.getPlayer();
        if (cuffedPlayers.containsKey(victim.getUniqueId())) {
            UUID copUUID = cuffedPlayers.get(victim.getUniqueId());
            Player cop = Bukkit.getPlayer(copUUID);
            // Pokud policista odejde/zmizí, pouta se povolí
            if (cop == null || !cop.isOnline() || cop.isDead()) {
                uncuff(victim); 
                return;
            }
            // Tahání hráče za sebou (vodítko)
            if (victim.getLocation().distance(cop.getLocation()) > 5) {
                victim.teleport(cop.getLocation());
            }
        }
    }
    
    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        cuffedPlayers.remove(event.getPlayer().getUniqueId());
    }

    // --- 3. DOWNED STATE --- 
    // ODSTRANĚNO: Tato logika byla přesunuta do MedicalListener.java, 
    // aby nedocházelo k duplicitám a chybám.

    // --- 4. ZÁMKY (Lockpicking) ---
    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (block.getBlockData() instanceof Openable || block.getType() == Material.CHEST || block.getType() == Material.BARREL) {
            if (lockManager.isLocked(block)) {
                if (!lockManager.canAccess(player, block)) {
                    // Lockpicking logika
                    if (item != null) {
                        if (item.getType() == Material.TRIPWIRE_HOOK) { // Šperhák
                            event.setCancelled(true);
                            startLockpicking(player, block, 10, 80); // 10s, 80% fail chance
                            return;
                        }
                        if (item.getType() == Material.HEAVY_CORE) { // Beranidlo
                            event.setCancelled(true);
                            startLockpicking(player, block, 5, 0); // 5s, 0% fail chance
                            return;
                        }
                    }
                    event.setCancelled(true);
                    player.sendActionBar(Component.text("§cZamčeno!"));
                }
            }
        }
    }

    private void startLockpicking(Player player, Block block, int seconds, int failChance) {
        player.sendMessage("§eZačínáš páčit zámek... (" + seconds + "s)");
        player.playSound(block.getLocation(), Sound.BLOCK_CHAIN_HIT, 1, 0.5f);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            if (player.getLocation().distance(block.getLocation()) > 5) {
                player.sendMessage("§cPřerušeno - odešel jsi.");
                return;
            }

            if (failChance > 0 && random.nextInt(100) < failChance) {
                player.sendMessage("§cŠperhák se zlomil! (Neúspěch)");
                player.playSound(block.getLocation(), Sound.ENTITY_ITEM_BREAK, 1, 1);
                player.getInventory().getItemInMainHand().setAmount(player.getInventory().getItemInMainHand().getAmount() - 1);
            } else {
                player.sendMessage("§aZámek byl zničen! Dveře jsou trvale odemčené.");
                lockManager.breakLock(block);
            }
        }, seconds * 20L);
    }
}