package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.stream.Collectors;

public class ShopManager implements Listener {

    private final Main plugin;

    public ShopManager(Main plugin) {
        this.plugin = plugin;
    }

    // --- GUI KREACE ---

    public void openBank(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§6Banka - Směnárna"));
        inv.setItem(11, createGuiItem(Material.RAW_COPPER, "§6Směnit Měďáky za Stříbrňák", "§7Cena: 64 Měďáků", "§7Dostaneš: 1 Stříbrňák"));
        inv.setItem(13, createGuiItem(Material.IRON_INGOT, "§fSměnit Stříbrňáky za Zlatý", "§7Cena: 16 Stříbrňáků", "§7Dostaneš: 1 Zlatý"));
        inv.setItem(15, createGuiItem(Material.PAPER, "§bVydat Šek", "§7Cena: 32 Zlatých", "§7Dostaneš: Šek na 32 Zlatých"));
        player.openInventory(inv);
    }

    public void openOffice(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§9Úřad - Licence"));
        inv.setItem(10, createGuiItem(Material.PAPER, "§aPobytská Licence", "§7Cena: 50 Měďáků", "§7Nutné pro bydlení"));
        inv.setItem(11, createGuiItem(Material.BOOK, "§aObčanská Licence", "§7Cena: 200 Měďáků", "§7Nutné: Pobytská Licence"));
        inv.setItem(13, createGuiItem(Material.WRITABLE_BOOK, "§6Zaplatit Daně", "§7Strhne dluhy z inventáře"));
        inv.setItem(15, createGuiItem(Material.IRON_SWORD, "§cMalý Zbrojní Průkaz", "§7Cena: 5 Zlatých", "§7Nutné: Občanská Licence"));
        player.openInventory(inv);
    }

    private ItemStack createGuiItem(Material mat, String name, String... lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text(name));
        // OPRAVA: Použití lore() místo setLore() pro Paper API komponenty
        meta.lore(Arrays.stream(lore).map(Component::text).collect(Collectors.toList()));
        item.setItemMeta(meta);
        return item;
    }

    // --- LOGIKA KLIKNUTÍ ---

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().title().equals(Component.text("§6Banka - Směnárna"))) {
            event.setCancelled(true);
            handleBankClick((Player) event.getWhoClicked(), event.getSlot());
        } else if (event.getView().title().equals(Component.text("§9Úřad - Licence"))) {
            event.setCancelled(true);
            handleOfficeClick((Player) event.getWhoClicked(), event.getSlot());
        }
    }

    private void handleBankClick(Player player, int slot) {
        CurrencyManager cm = plugin.getCurrencyManager();

        if (slot == 11) { // 64 Copper -> 1 Silver
            int cost = 64 * CurrencyManager.VAL_COPPER;
            if (cm.takeMoney(player, cost)) {
                player.getInventory().addItem(cm.getSilver(1));
                player.sendMessage("§aSměnil jsi 64 Měďáků za 1 Stříbrňák.");
            } else {
                player.sendMessage("§cNemáš dostatek Měďáků (Potřebuješ 64).");
            }
        } 
        else if (slot == 13) { // 16 Silver -> 1 Gold
            int cost = 16 * CurrencyManager.VAL_SILVER;
            if (cm.takeMoney(player, cost)) {
                player.getInventory().addItem(cm.getGold(1));
                player.sendMessage("§aSměnil jsi 16 Stříbrňáků za 1 Zlatý.");
            } else {
                player.sendMessage("§cNemáš dostatek Stříbrňáků (Potřebuješ 16).");
            }
        }
        else if (slot == 15) { // 32 Gold -> Check
            int cost = 32 * CurrencyManager.VAL_GOLD;
            if (cm.takeMoney(player, cost)) {
                player.getInventory().addItem(cm.getCheck(1));
                player.sendMessage("§aVydal jsi Šek na 32 Zlatých.");
            } else {
                player.sendMessage("§cNemáš dostatek Zlatých (Potřebuješ 32).");
            }
        }
    }

    private void handleOfficeClick(Player player, int slot) {
        CurrencyManager cm = plugin.getCurrencyManager();
        LicenseManager lm = plugin.getLicenseManager();

        if (slot == 10) buyLicense(player, "pobytska", 50, null);
        else if (slot == 11) buyLicense(player, "obcanska", 200, "pobytska");
        else if (slot == 15) buyLicense(player, "zbrojni_maly", 5 * 1024, "obcanska");
        else if (slot == 13) player.sendMessage("§eSystém daní se připravuje...");
    }

    private void buyLicense(Player player, String license, int cost, String requiredLicense) {
        LicenseManager lm = plugin.getLicenseManager();
        CurrencyManager cm = plugin.getCurrencyManager();

        if (lm.hasLicense(player, license)) {
            player.sendMessage("§cTuto licenci už vlastníš!");
            return;
        }

        if (requiredLicense != null && !lm.hasLicense(player, requiredLicense)) {
            player.sendMessage("§cMusíš vlastnit předchozí licenci: " + requiredLicense);
            return;
        }

        if (cm.takeMoney(player, cost)) {
            lm.addLicense(player, license);
            player.sendMessage("§aZakoupil jsi licenci: " + license);
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        } else {
            player.sendMessage("§cNemáš dostatek peněz!");
        }
    }
}