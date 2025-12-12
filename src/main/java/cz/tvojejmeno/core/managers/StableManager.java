package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.stream.Collectors;

public class StableManager implements Listener {

    private final Main plugin;

    public StableManager(Main plugin) {
        this.plugin = plugin;
    }

    public void openBuyMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§2Stáj - Nákup"));
        inv.setItem(11, createItem(Material.LEATHER_HORSE_ARMOR, "§6Koupit Koně (Hnědý)", "§7Cena: 200 Zlatých", "TYPE:HORSE", "VAR:CHESTNUT", "COST:204800"));
        inv.setItem(13, createItem(Material.BONE, "§fKoupit Psa (Pale)", "§7Cena: 50 Zlatých", "TYPE:WOLF", "VAR:PALE", "COST:51200"));
        inv.setItem(15, createItem(Material.COD, "§eKoupit Kočku (Černá)", "§7Cena: 30 Zlatých", "TYPE:CAT", "VAR:ALL_BLACK", "COST:30720"));
        player.openInventory(inv);
    }

    public void openMyAnimals(Player player) {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            try (Connection conn = plugin.getDatabaseManager().getConnection()) {
                PreparedStatement ps = conn.prepareStatement("SELECT * FROM rp_animals WHERE owner_uuid = ? AND is_dead = 0");
                ps.setString(1, player.getUniqueId().toString());
                ResultSet rs = ps.executeQuery();

                Inventory inv = Bukkit.createInventory(null, 54, Component.text("§2Moje Zvířata"));
                
                int slot = 0;
                while (rs.next()) {
                    int id = rs.getInt("id");
                    String name = rs.getString("name");
                    String type = rs.getString("type");
                    
                    ItemStack icon = new ItemStack(Material.SADDLE);
                    if (type.equals("WOLF")) icon.setType(Material.BONE);
                    if (type.equals("CAT")) icon.setType(Material.COD);
                    
                    ItemMeta meta = icon.getItemMeta();
                    meta.displayName(Component.text("§a" + name));
                    // Použití lore() pro komponenty
                    meta.lore(Arrays.asList(Component.text("§7Typ: " + type), Component.text("§eKlikni pro přivolání (ID: " + id + ")")));
                    icon.setItemMeta(meta);
                    
                    inv.setItem(slot++, icon);
                }

                plugin.getServer().getScheduler().runTask(plugin, () -> player.openInventory(inv));
                
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private ItemStack createItem(Material mat, String name, String loreCost, String typeTag, String varTag, String costTag) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        // OPRAVA: Použití lore()
        meta.lore(Arrays.asList(Component.text(loreCost), Component.text("§8" + typeTag), Component.text("§8" + varTag), Component.text("§8" + costTag)));
        item.setItemMeta(meta);
        return item;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().equals(Component.text("§2Stáj - Nákup"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            
            if (item == null || !item.hasItemMeta() || item.getItemMeta().lore() == null) return;

            int slot = event.getSlot();
            if (slot == 11) buy(player, EntityType.HORSE, "CHESTNUT", 200 * 1024, "Můj Kůň");
            if (slot == 13) buy(player, EntityType.WOLF, "PALE", 50 * 1024, "Můj Pes");
            if (slot == 15) buy(player, EntityType.CAT, "ALL_BLACK", 30 * 1024, "Moje Kočka");
            
            player.closeInventory();
        } 
        else if (event.getView().title().equals(Component.text("§2Moje Zvířata"))) {
            event.setCancelled(true);
            Player player = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            
            if (item != null && item.getType() != Material.AIR) {
                player.sendMessage("§ePoužij příkaz: /zvire privolat <ID>");
            }
        }
    }

    private void buy(Player player, EntityType type, String variant, int costCopper, String defaultName) {
        CurrencyManager cm = plugin.getCurrencyManager();
        if (cm.takeMoney(player, costCopper)) {
            plugin.getAnimalManager().buyAnimal(player, type, variant, defaultName);
        } else {
            player.sendMessage("§cNemáš dostatek peněz!");
        }
    }
}