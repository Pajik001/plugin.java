package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.models.RPCharacter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerEditBookEvent; // <--- OPRAVENÝ IMPORT
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.ArrayList;
import java.util.List;

public class ContractListener implements Listener {

    private final Main plugin;

    public ContractListener(Main plugin) {
        this.plugin = plugin;
    }

    // --- 1. DESKY (FOLDERS) ---

    @EventHandler
    public void onFolderOpen(PlayerInteractEvent event) {
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = event.getItem();
            if (plugin.getFolderManager().isFolder(item)) {
                plugin.getFolderManager().openFolder(event.getPlayer(), item);
                event.getPlayer().playSound(event.getPlayer().getLocation(), Sound.ITEM_BOOK_PAGE_TURN, 1, 1);
            }
        }
    }

    @EventHandler
    public void onFolderClose(InventoryCloseEvent event) {
        if (event.getView().title().equals(Component.text("§8Desky"))) {
            Player player = (Player) event.getPlayer();
            ItemStack folder = player.getInventory().getItemInMainHand();
            
            // Validace (Folder musí být v ruce)
            if (!plugin.getFolderManager().isFolder(folder)) {
                player.sendMessage("§cChyba: Musíš držet desky v ruce při zavírání!");
                return;
            }

            // Kontrola obsahu (Jen papíry, knihy, mapy)
            Inventory inv = event.getInventory();
            for (ItemStack content : inv.getContents()) {
                if (content != null && !isAllowedInFolder(content.getType())) {
                    player.sendMessage("§cDo desek patří jen papíry, knihy a mapy!");
                    player.getWorld().dropItemNaturally(player.getLocation(), content);
                    inv.remove(content);
                }
            }

            // Uložení
            plugin.getFolderManager().saveFolder(folder, inv);
            player.playSound(player.getLocation(), Sound.ITEM_BOOK_PUT, 1, 1);
        }
    }

    private boolean isAllowedInFolder(Material mat) {
        return mat == Material.PAPER || mat == Material.MAP || mat == Material.FILLED_MAP 
            || mat == Material.WRITABLE_BOOK || mat == Material.WRITTEN_BOOK || mat == Material.BOOK;
    }

    // --- 2. SMLOUVY (PODPIS) ---

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        BookMeta meta = event.getNewBookMeta();
        boolean modified = false;
        List<Component> newPages = new ArrayList<>();

        RPCharacter character = plugin.getCharacterManager().getCharacter(event.getPlayer());
        String signature = (character != null && character.hasCharacter()) ? character.getFullName() : event.getPlayer().getName();

        // Projdeme stránky a hledáme "*Podpis*"
        // Poznámka: meta.pages() je Paper API metoda vracející List<Component>
        for (Component page : meta.pages()) {
            String text = PlainTextComponentSerializer.plainText().serialize(page);
            
            if (text.contains("*Podpis*") || text.contains("*podpis*")) {
                // Nahradíme placeholder podpisem
                String newText = text.replaceAll("(?i)\\*podpis\\*", "\n§3§o(Podepsán: " + signature + ")§r\n");
                newPages.add(Component.text(newText));
                modified = true;
            } else {
                newPages.add(page);
            }
        }

        if (modified) {
            // Vytvoříme novou meta s upravenými stránkami
            // Používáme Builder pattern z Paper API pro bezpečné nastavení stránek
            BookMeta newMeta = meta.toBuilder().pages(newPages).build();
            event.setNewBookMeta(newMeta);
            event.getPlayer().sendMessage("§aDokument byl podepsán jako: " + signature);
            
            // Volitelně: Pokud chceš, aby se po podpisu kniha zamkla (změnila na Written Book),
            // musel bys zde změnit logiku a event.setSigning(true).
        }
    }
}