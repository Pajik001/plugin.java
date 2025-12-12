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

        if (args.length == 0) return false;
        String sub = args[0].toLowerCase();

        // --- ADMIN: CREATE FACTION ---
        if (sub.equals("create")) {
            if (!player.hasPermission("core.admin")) {
                player.sendMessage("§cPouze admin může zakládat frakce.");
                return true;
            }
            if (args.length < 2) {
                player.sendMessage("§cPoužití: /f create <Název>");
                return true;
            }
            String name = args[1];
            plugin.getFactionManager().createFaction(name, player);
            player.sendMessage("§aFrakce " + name + " založena.");
            return true;
        }

        // --- ADMIN: DEFINE ROLE ---
        if (sub.equals("addrole")) {
            if (!player.hasPermission("core.admin")) return true;
            // /f addrole <Frakce> <Role> <Limit> <Priorita>
            if (args.length < 5) {
                player.sendMessage("§cPoužití: /f addrole <Frakce> <Role> <Limit> <Priorita>");
                return true;
            }
            String facName = args[1];
            String roleName = args[2];
            int limit = Integer.parseInt(args[3]);
            int prio = Integer.parseInt(args[4]);

            plugin.getFactionManager().createCustomRole(facName, roleName, limit, prio);
            player.sendMessage("§aRole přidána.");
            return true;
        }

        // --- BOSS: SET RANK (Povýšení) ---
        if (sub.equals("setrank")) {
            // Zde by měla být kontrola, zda je hráč šéfem frakce (isBoss)
            if (args.length < 3) {
                player.sendMessage("§cPoužití: /f setrank <Hráč> <Role>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[1]);
            String role = args[2];
            // Frakci si systém zjistí podle toho, kde je 'sender'
            // Pro jednoduchost zde natvrdo předpokládáme název (v reálu bys musel poslat i název frakce nebo si ho zjistit)
            // plugin.getFactionManager().setPlayerRole(target, "Policie", role); 
            player.sendMessage("§eTato funkce vyžaduje přesné zacílení frakce (WIP).");
            return true;
        }

        return true;
    }
}