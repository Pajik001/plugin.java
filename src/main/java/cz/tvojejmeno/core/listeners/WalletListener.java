package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.managers.CurrencyManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BundleMeta;

public class WalletListener implements Listener {

    private final CurrencyManager currencyManager;

    public WalletListener(Main plugin) {
        this.currencyManager = plugin.getCurrencyManager();
    }

    // Tento event se snaží zabránit vložení ne-měny do Měšce v inventáři
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null) return;

        ItemStack cursor = event.getCursor();
        ItemStack current = event.getCurrentItem();
        
        // Scénář 1: Hráč bere item kurzorem a kliká na Měšec v inventáři (vkládání)
        if (cursor != null && !cursor.getType().isAir() && current != null && currencyManager.isWallet(current)) {
            
            // Pokud to, co drží na myši, NENÍ měna
            if (!currencyManager.isCurrency(cursor)) {
                // Pokud kliká pravým (vkládání do bundlu ve vanille)
                if (event.isRightClick()) {
                    event.setCancelled(true);
                    event.getWhoClicked().sendMessage(Component.text("§cDo Měšce patří pouze mince a šeky!"));
                }
            }
        }
    }
}