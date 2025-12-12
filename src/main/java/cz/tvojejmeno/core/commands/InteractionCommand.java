package cz.tvojejmeno.core.commands;

import cz.tvojejmeno.core.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Set;
import java.util.UUID;

public class InteractionCommand implements CommandExecutor {

    private final Main plugin;

    public InteractionCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (label.equalsIgnoreCase("revive")) {
            if (args.length < 1) {
                player.sendMessage("§cPoužití: /revive <hráč>");
                return true;
            }
            Player target = Bukkit.getPlayer(args[0]);
            if (target != null && player.getLocation().distance(target.getLocation()) < 5) {
                plugin.getMedicalManager().revive(player, target);
            } else {
                player.sendMessage("§cHráč je daleko nebo offline.");
            }
            return true;
        }

        Block block = player.getTargetBlockExact(5);
        if (block == null || block.getType().isAir()) {
            player.sendMessage("§cMusíš se dívat na dveře nebo truhlu.");
            return true;
        }

        if (label.equalsIgnoreCase("lock")) {
            if (plugin.getLockManager().isLocked(block)) {
                player.sendMessage("§cToto už je zamčeno.");
            } else {
                plugin.getLockManager().createLock(player, block);
                player.sendMessage("§aZámek vytvořen!");
            }
            return true;
        }

        if (label.equalsIgnoreCase("unlock")) {
            if (!plugin.getLockManager().isLocked(block)) {
                player.sendMessage("§cToto není zamčeno.");
                return true;
            }
            if (plugin.getLockManager().isOwner(block, player) || player.hasPermission("core.admin")) {
                plugin.getLockManager().removeLock(block);
                player.sendMessage("§aZámek odstraněn.");
            } else {
                player.sendMessage("§cNemáš klíč.");
            }
            return true;
        }

        if (label.equalsIgnoreCase("key")) {
            if (!plugin.getLockManager().isLocked(block)) return true;
            if (!plugin.getLockManager().isOwner(block, player)) return true;

            if (args.length == 0) return true;
            String sub = args[0].toLowerCase();
            
            if (sub.equals("list")) {
                Set<String> uuids = plugin.getLockManager().getAllowedPlayers(block);
                player.sendMessage("§eKlíče: " + uuids.size());
                return true;
            }

            if (args.length < 2) return true;
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);

            if (sub.equals("add")) {
                plugin.getLockManager().addAccess(block, target.getUniqueId());
                player.sendMessage("§aKlíč přidán.");
            } else if (sub.equals("remove")) {
                plugin.getLockManager().removeAccess(block, target.getUniqueId());
                player.sendMessage("§cKlíč odebrán.");
            }
        }
        return true;
    }
}