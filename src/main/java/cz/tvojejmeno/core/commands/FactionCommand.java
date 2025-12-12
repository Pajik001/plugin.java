package cz.tvojejmeno.core.commands;

import cz.tvojejmeno.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class FactionCommand implements CommandExecutor {

    private final Main plugin;

    public FactionCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) return true;

        if (args.length == 0) {
            player.sendMessage("§ePoužití: /f <create/addrole/setrank>");
            return true;
        }
        String sub = args[0].toLowerCase();

        // --- CREATE ---
        if (sub.equals("create")) {
            if (!player.hasPermission("core.admin")) return true;
            if (args.length < 2) {
                player.sendMessage("§cPoužití: /f create <Název>");
                return true;
            }
            plugin.getFactionManager().createFaction(args[1], player);
            player.sendMessage("§aFrakce založena.");
            return true;
        }

        // --- ADD ROLE ---
        if (sub.equals("addrole")) {
            if (!player.hasPermission("core.admin")) return true;
            if (args.length < 5) {
                player.sendMessage("§cPoužití: /f addrole <Frakce> <Role> <Limit> <Priorita>");
                return true;
            }
            try {
                plugin.getFactionManager().createCustomRole(args[1], args[2], Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                player.sendMessage("§aRole přidána.");
            } catch (NumberFormatException e) {
                player.sendMessage("§cČísla jsou špatně.");
            }
            return true;
        }

        // --- SET RANK (Opraveno) ---
        if (sub.equals("setrank")) {
            // /f setrank <Frakce> <Hráč> <Role>
            if (args.length < 4) {
                player.sendMessage("§cPoužití: /f setrank <Frakce> <Hráč> <Role>");
                return true;
            }
            String factionName = args[1];
            Player target = Bukkit.getPlayer(args[2]);
            String roleName = args[3];

            if (target == null) {
                player.sendMessage("§cHráč nenalezen.");
                return true;
            }

            // Voláme manager (ten by měl kontrolovat i duplicity rolí jako 2x Boss)
            plugin.getFactionManager().setPlayerRole(target, factionName, roleName);
            player.sendMessage("§aPožadavek odeslán (zkontroluj konzoli pro chyby DB).");
            return true;
        }

        return true;
    }
}