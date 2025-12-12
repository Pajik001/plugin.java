package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class FolderManager {

    private final Main plugin;
    private final NamespacedKey folderKey;

    public FolderManager(Main plugin) {
        this.plugin = plugin;
        this.folderKey = new NamespacedKey(plugin, "folder_content");
    }

    public ItemStack getFolderItem() {
        ItemStack item = new ItemStack(Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(Component.text("§6§lDesky"));
        meta.lore(java.util.List.of(Component.text("§7Kapacita: 27 papírů")));
        // Označení itemu
        meta.getPersistentDataContainer().set(new NamespacedKey(plugin, "is_folder"), PersistentDataType.BYTE, (byte) 1);
        item.setItemMeta(meta);
        return item;
    }

    public boolean isFolder(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(new NamespacedKey(plugin, "is_folder"), PersistentDataType.BYTE);
    }

    public void openFolder(Player player, ItemStack folderItem) {
        Inventory inv = Bukkit.createInventory(null, 27, Component.text("§8Desky"));
        
        // Načtení obsahu
        ItemMeta meta = folderItem.getItemMeta();
        if (meta.getPersistentDataContainer().has(folderKey, PersistentDataType.STRING)) {
            String data = meta.getPersistentDataContainer().get(folderKey, PersistentDataType.STRING);
            try {
                ItemStack[] items = itemsFromBase64(data);
                inv.setContents(items);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        player.openInventory(inv);
    }

    public void saveFolder(ItemStack folderItem, Inventory inv) {
        try {
            String data = itemsToBase64(inv.getContents());
            ItemMeta meta = folderItem.getItemMeta();
            meta.getPersistentDataContainer().set(folderKey, PersistentDataType.STRING, data);
            folderItem.setItemMeta(meta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Serializace (Bukkit API)
    private String itemsToBase64(ItemStack[] items) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
        dataOutput.writeInt(items.length);
        for (ItemStack item : items) {
            dataOutput.writeObject(item);
        }
        dataOutput.close();
        return Base64Coder.encodeLines(outputStream.toByteArray());
    }

    private ItemStack[] itemsFromBase64(String data) throws Exception {
        ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
        BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
        ItemStack[] items = new ItemStack[dataInput.readInt()];
        for (int i = 0; i < items.length; i++) {
            items[i] = (ItemStack) dataInput.readObject();
        }
        dataInput.close();
        return items;
    }
}