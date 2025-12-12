package cz.tvojejmeno.core.commands;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class AdminItemsCommand implements CommandExecutor {

    private final Main plugin;

    public AdminItemsCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        // Kontrola práv
        if (!player.hasPermission("core.admin") && !player.isOp()) {
            player.sendMessage("§cNa toto nemáš právo.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§e/adminitems <desky/pouta/sperhak/beranidlo>");
            return true;
        }

        ItemStack item = null;
        String type = args[0].toLowerCase();

        if (type.equals("desky")) {
            item = plugin.getFolderManager().getFolderItem();
        } else if (type.equals("pouta")) {
            item = new ItemStack(Material.BLAZE_ROD);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§c§lPolicejní Pouta"));
            item.setItemMeta(meta);
        } else if (type.equals("sperhak")) {
            item = new ItemStack(Material.TRIPWIRE_HOOK);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§7Šperhák"));
            item.setItemMeta(meta);
        } else if (type.equals("beranidlo")) {
            item = new ItemStack(Material.HEAVY_CORE);
            ItemMeta meta = item.getItemMeta();
            meta.displayName(Component.text("§4§lBeranidlo"));
            item.setItemMeta(meta);
        }

        if (item != null) {
            player.getInventory().addItem(item);
            player.sendMessage("§aZískal jsi: " + type);
        } else {
            player.sendMessage("§cNeznámý item: " + type);
        }

        return true;
    }
}