package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class CurrencyManager {

    private final Main plugin;
    // Hodnoty v měďácích (Base Unit)
    public static final int VAL_COPPER = 1;
    public static final int VAL_SILVER = 64;
    public static final int VAL_GOLD = 1024;
    public static final int VAL_CHECK = 32768;

    private final NamespacedKey currencyKey;

    public CurrencyManager(Main plugin) {
        this.plugin = plugin;
        this.currencyKey = new NamespacedKey(plugin, "currency_type");
    }

    // --- Vytváření Itemů ---

    public ItemStack getCopper(int amount) {
        // OPRAVA: COPPER_NUGGET neexistuje -> RAW_COPPER
        return createCurrency(Material.RAW_COPPER, amount, "Měďák", TextColor.color(0xB87333), "copper", VAL_COPPER);
    }

    public ItemStack getSilver(int amount) {
        return createCurrency(Material.IRON_NUGGET, amount, "Stříbrňák", NamedTextColor.GRAY, "silver", VAL_SILVER);
    }

    public ItemStack getGold(int amount) {
        return createCurrency(Material.GOLD_NUGGET, amount, "Zlatý", NamedTextColor.GOLD, "gold", VAL_GOLD);
    }

    public ItemStack getCheck(int amount) {
        return createCurrency(Material.PAPER, amount, "Šek na 32 Zlatých", NamedTextColor.AQUA, "check", VAL_CHECK);
    }

    private ItemStack createCurrency(Material mat, int amount, String name, TextColor color, String typeKey, int value) {
        ItemStack item = new ItemStack(mat, amount);
        ItemMeta meta = item.getItemMeta();
        
        meta.displayName(Component.text(name).color(color).decorate(TextDecoration.BOLD));
        
        int cmd = plugin.getConfig().getInt("currency.model-data." + typeKey, 0);
        if (cmd > 0) meta.setCustomModelData(cmd);

        meta.getPersistentDataContainer().set(currencyKey, PersistentDataType.STRING, typeKey);
        
        item.setItemMeta(meta);
        return item;
    }

    // --- Práce s inventářem ---

    public int getPlayerFunds(Player player) {
        int totalValue = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            totalValue += getItemValue(item);
        }
        return totalValue;
    }

    private int getItemValue(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return 0;
        
        if (item.getType() == Material.BUNDLE) {
            int bundleValue = 0;
            if (item.getItemMeta() instanceof BundleMeta bundleMeta) {
                for (ItemStack innerItem : bundleMeta.getItems()) {
                    bundleValue += getItemValue(innerItem);
                }
            }
            return bundleValue;
        }

        if (!item.hasItemMeta()) return 0;
        String type = item.getItemMeta().getPersistentDataContainer().get(currencyKey, PersistentDataType.STRING);
        
        if (type == null) return 0;

        int amount = item.getAmount();
        switch (type) {
            case "copper": return amount * VAL_COPPER;
            case "silver": return amount * VAL_SILVER;
            case "gold": return amount * VAL_GOLD;
            case "check": return amount * VAL_CHECK;
            default: return 0;
        }
    }

    public boolean takeMoney(Player player, int amountInCopper) {
        int totalFunds = getPlayerFunds(player);

        if (totalFunds < amountInCopper) {
            return false;
        }

        removeCurrencyFromInventory(player);

        int remainingFunds = totalFunds - amountInCopper;
        giveMoney(player, remainingFunds);
        
        return true;
    }

    private void removeCurrencyFromInventory(Player player) {
        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItem(i);
            if (item == null) continue;

            if (isCurrency(item)) {
                player.getInventory().setItem(i, null);
            } 
            else if (item.getType() == Material.BUNDLE && isWallet(item)) {
                 BundleMeta meta = (BundleMeta) item.getItemMeta();
                 meta.setItems(new ArrayList<>());
                 item.setItemMeta(meta);
            }
        }
    }

    public void giveMoney(Player player, int amountInCopper) {
        int remaining = amountInCopper;

        int checks = remaining / VAL_CHECK;
        if (checks > 0) {
            player.getInventory().addItem(getCheck(checks));
            remaining %= VAL_CHECK;
        }

        int golds = remaining / VAL_GOLD;
        if (golds > 0) {
            player.getInventory().addItem(getGold(golds));
            remaining %= VAL_GOLD;
        }

        int silvers = remaining / VAL_SILVER;
        if (silvers > 0) {
            player.getInventory().addItem(getSilver(silvers));
            remaining %= VAL_SILVER;
        }

        if (remaining > 0) {
            player.getInventory().addItem(getCopper(remaining));
        }
    }

    public boolean isCurrency(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(currencyKey, PersistentDataType.STRING);
    }
    
    public ItemStack getWallet() {
        ItemStack wallet = new ItemStack(Material.BUNDLE);
        BundleMeta meta = (BundleMeta) wallet.getItemMeta();
        meta.displayName(Component.text("Měšec").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD));
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_wallet"), PersistentDataType.BYTE, (byte) 1);
        wallet.setItemMeta(meta);
        return wallet;
    }
    
    public boolean isWallet(ItemStack item) {
         if (item == null || !item.hasItemMeta()) return false;
         return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "is_wallet"), PersistentDataType.BYTE);
    }
}