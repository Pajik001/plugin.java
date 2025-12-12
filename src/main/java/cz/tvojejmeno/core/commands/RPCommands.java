package cz.tvojejmeno.core.commands;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.models.RPCharacter;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public class RPCommands implements CommandExecutor {

    private final Main plugin;

    public RPCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        RPCharacter ch = plugin.getCharacterManager().getCharacter(player);
        if (ch == null) return true;

        // --- /ME ---
        if (label.equalsIgnoreCase("me")) {
            if (args.length == 0) return true;
            String text = "* " + String.join(" ", args) + " *";
            // Bublina
            plugin.getChatBubbleManager().spawnBubble(player, "§d" + text, 5, Color.fromARGB(100, 50, 0, 50));
            player.sendMessage("§d" + ch.getFullName() + " " + String.join(" ", args));
            return true;
        }

        // --- /DO ---
        if (label.toLowerCase().startsWith("do")) {
            if (args.length == 0) return true;
            String text = "* " + String.join(" ", args) + " *";
            int duration = 5;
            if (label.equalsIgnoreCase("doc3")) duration = 3;
            if (label.equalsIgnoreCase("doc5")) duration = 5;

            plugin.getChatBubbleManager().spawnBubble(player, "§6" + text, duration, Color.fromARGB(100, 100, 50, 0));
            return true;
        }

        // --- /POOP (Opraveno) ---
        if (label.equalsIgnoreCase("poop")) {
            if (ch.getToilet() < 20) { // Snížen limit pro testování
                player.sendMessage("§cNecítíš potřebu.");
                return true;
            }
            
            player.sendMessage("§aUlevil jsi si...");
            // Reset potřeby přes Manager (aby se to uložilo do postavy)
            plugin.getNeedsManager().cleanToilet(player);
            
            // Drop bobku
            ItemStack poop = new ItemStack(Material.COCOA_BEANS);
            var item = player.getWorld().dropItemNaturally(player.getLocation(), poop);
            item.setPickupDelay(32767); 
            item.setCustomName("§6Exkrement");
            item.setCustomNameVisible(true);
            plugin.getServer().getScheduler().runTaskLater(plugin, item::remove, 600L); // 30s
            return true;
        }

        return true;
    }
}