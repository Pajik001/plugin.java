package cz.tvojejmeno.core.commands;

import cz.tvojejmeno.core.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.jetbrains.annotations.NotNull;

public class AnimalCommand implements CommandExecutor {

    private final Main plugin;

    public AnimalCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage("§ePoužití: /zvire <nakup/seznam/privolat/odvolat>");
            return true;
        }

        if (args[0].equalsIgnoreCase("nakup")) {
            plugin.getStableManager().openBuyMenu(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("seznam")) {
            plugin.getStableManager().openMyAnimals(player);
            return true;
        }

        if (args[0].equalsIgnoreCase("privolat")) {
            if (args.length < 2) {
                player.sendMessage("§cMusíš zadat ID zvířete (zjistíš v /zvire seznam)");
                return true;
            }
            try {
                int id = Integer.parseInt(args[1]);
                plugin.getAnimalManager().spawnAnimal(player, id);
            } catch (NumberFormatException e) {
                player.sendMessage("§cID musí být číslo.");
            }
            return true;
        }

        if (args[0].equalsIgnoreCase("odvolat")) {
            // Najít zvířata v okolí, která patří hráči
            int count = 0;
            for (Entity en : player.getNearbyEntities(10, 10, 10)) {
                if (en instanceof Tameable tameable && tameable.getOwner() != null && tameable.getOwner().equals(player)) {
                    // Pokud je v cache manageru (je to RP zvíře)
                    if (plugin.getAnimalManager().getActiveAnimals().containsKey(en.getUniqueId())) {
                        plugin.getAnimalManager().getActiveAnimals().remove(en.getUniqueId());
                        en.remove(); // Despawn
                        count++;
                    }
                }
            }
            if (count > 0) player.sendMessage("§aOdvoláno " + count + " zvířat.");
            else player.sendMessage("§cŽádné tvé zvíře v okolí.");
            return true;
        }

        return true;
    }
}