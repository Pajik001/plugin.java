package cz.tvojejmeno.core.commands;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.managers.CurrencyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class EconomyCommand implements CommandExecutor {

    private final CurrencyManager currencyManager;

    public EconomyCommand(Main plugin) {
        this.currencyManager = plugin.getCurrencyManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("core.admin")) {
            sender.sendMessage("§cNa toto nemáš právo.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage("§ePoužití: /eco <give/wallet> [hráč] [částka]");
            return true;
        }

        // /eco wallet
        if (args[0].equalsIgnoreCase("wallet")) {
            if (sender instanceof Player) {
                ((Player) sender).getInventory().addItem(currencyManager.getWallet());
                sender.sendMessage("§aZískal jsi Měšec.");
            }
            return true;
        }

        // /eco give <hráč> <částka>
        if (args[0].equalsIgnoreCase("give")) {
            if (args.length < 3) {
                sender.sendMessage("§cChybí parametry.");
                return true;
            }

            Player target = Bukkit.getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage("§cHráč nenalezen.");
                return true;
            }

            try {
                int amount = Integer.parseInt(args[2]);
                currencyManager.giveMoney(target, amount);
                sender.sendMessage("§aHráči " + target.getName() + " bylo dáno " + amount + " měďáků (převedeno).");
                target.sendMessage("§aObdržel jsi finance od státu.");
            } catch (NumberFormatException e) {
                sender.sendMessage("§cČástka musí být číslo.");
            }
            return true;
        }

        return true;
    }
}