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

import java.util.Arrays;

public class RPCommands implements CommandExecutor {

    private final Main plugin;

    public RPCommands(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        RPCharacter ch = plugin.getCharacterManager().getCharacter(player);
        if (ch == null || !ch.hasCharacter()) {
            player.sendMessage("§cMusíš mít postavu!");
            return true;
        }

        // --- /ME ---
        if (label.equalsIgnoreCase("me")) {
            if (args.length == 0) return true;
            String action = String.join(" ", args);
            String text = "* " + action + " *";
            
            // Bublina (Fialová pro /me)
            plugin.getChatBubbleManager().spawnBubble(player, "§d" + text, 5, Color.fromARGB(100, 0, 0, 0));
            player.sendMessage("§d" + ch.getFullName() + " " + action); // Zpráva do chatu jen pro hráče (nebo local radius, pokud chceš)
            return true;
        }

        // --- /DO, /DOC3, /DOC5 ---
        if (label.toLowerCase().startsWith("do")) {
            if (args.length == 0) return true;
            String action = String.join(" ", args);
            String text = "* " + action + " *";
            
            int duration = 5;
            if (label.equalsIgnoreCase("doc3")) duration = 3;
            if (label.equalsIgnoreCase("doc5")) duration = 5;

            // Bublina (Žlutá/Oranžová pro /do)
            plugin.getChatBubbleManager().spawnBubble(player, "§6" + text, duration, Color.fromARGB(100, 0, 0, 0));
            
            // Animace teček v bublině by byla složitější (vyžaduje task), 
            // pro začátek stačí statická bublina, která tam visí daný čas.
            return true;
        }

        // --- /POOP ---
        if (label.equalsIgnoreCase("poop")) {
            if (ch.getToilet() < 30) {
                player.sendMessage("§cNecítíš potřebu.");
                return true;
            }
            
            player.sendMessage("§aUlevil jsi si...");
            ch.setToilet(0);
            
            // Drop kakaového bobu
            ItemStack poop = new ItemStack(Material.COCOA_BEANS);
            // Nastavíme, aby nešel sebrat (pomocí PickupDelay nebo NBT)
            var item = player.getWorld().dropItemNaturally(player.getLocation(), poop);
            item.setPickupDelay(32767); // Nikdy nejde sebrat
            item.setCustomName("§6Exkrement");
            item.setCustomNameVisible(true);
            
            // Smazání bobu po minutě
            plugin.getServer().getScheduler().runTaskLater(plugin, item::remove, 1200L); 
            return true;
        }

        return true;
    }
}