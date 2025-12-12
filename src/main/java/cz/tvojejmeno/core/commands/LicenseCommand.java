package cz.tvojejmeno.core.commands;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.models.RPCharacter;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class LicenseCommand implements CommandExecutor {

    private final Main plugin;

    public LicenseCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;
        RPCharacter character = plugin.getCharacterManager().getCharacter(player);

        if (character == null || !character.hasCharacter()) {
            player.sendMessage("§cMusíš mít postavu!");
            return true;
        }

        // Asynchronně načteme licence
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            List<String> licenses = plugin.getLicenseManager().getLicenses(player.getUniqueId());
            
            // Zpět na hlavní vlákno pro odeslání zpráv
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                String licensesList = licenses.isEmpty() ? "Žádné" : String.join(", ", licenses);
                String message = "§e§lLicence občana " + character.getFullName() + ": §f" + licensesList;

                if (args.length > 0) {
                    // Cíleně na jednoho hráče (radius 8)
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target != null && target.getLocation().distance(player.getLocation()) <= 8) {
                        target.sendMessage(message);
                        player.sendMessage("§aUkázal jsi licence hráči " + target.getName());
                    } else {
                        player.sendMessage("§cHráč je moc daleko nebo není online.");
                    }
                } else {
                    // Všem v okruhu 5 bloků
                    int count = 0;
                    for (Player nearby : player.getWorld().getPlayers()) {
                        if (nearby.getLocation().distance(player.getLocation()) <= 5) {
                            nearby.sendMessage(message);
                            count++;
                        }
                    }
                    if (count <= 1) player.sendMessage("§7Nikdo v okolí nevidí tvé licence.");
                }
            });
        });

        return true;
    }
}