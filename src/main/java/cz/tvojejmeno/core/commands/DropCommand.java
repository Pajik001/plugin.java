package cz.tvojejmeno.core.commands;

import cz.tvojejmeno.core.managers.DropManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DropCommand implements CommandExecutor {

    private final DropManager dropManager;

    public DropCommand(DropManager dropManager) {
        this.dropManager = dropManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // Syntax: /mobdrop set rabbit 12%leather,10%carrot
        if (args.length < 3 || !args[0].equalsIgnoreCase("set")) {
            sender.sendMessage("§cPoužití: /mobdrop set <mob> <dropy>");
            return true;
        }

        // 1. Získání typu moba
        EntityType type;
        try {
            type = EntityType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            sender.sendMessage("§cNeznámý mob: " + args[1]);
            return true;
        }

        // 2. Parsování stringu
        String rawData = args[2];
        List<String> dropList = new ArrayList<>(Arrays.asList(rawData.split(",")));

        // Validace
        for (String drop : dropList) {
            if (!drop.contains("%")) {
                sender.sendMessage("§cChyba formátu: " + drop + " (chybí %)");
                return true;
            }
            String matName = drop.split("%")[1];
            if (Material.matchMaterial(matName) == null) {
                sender.sendMessage("§cNeznámý materiál: " + matName);
                return true;
            }
        }

        // 3. Uložení - TEĎ UŽ BUDE FUNGOVAT
        dropManager.setDrops(type, dropList);
        sender.sendMessage("§aDropy pro " + type.name() + " byly nastaveny!");

        return true;
    }
}