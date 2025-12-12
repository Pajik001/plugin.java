package cz.tvojejmeno.core.commands;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.managers.CharacterManager;
import cz.tvojejmeno.core.models.RPCharacter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class CharacterCommand implements CommandExecutor {

    private final CharacterManager characterManager;

    public CharacterCommand(Main plugin) {
        // CharacterManager je nyní dostupný přes Main
        this.characterManager = plugin.getCharacterManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (args.length >= 4 && args[0].equalsIgnoreCase("create")) {
            RPCharacter ch = characterManager.getCharacter(player);
            
            // --- OPRAVA CHYBY NullPointerException ---
            // Pokud chybí data, zkusíme je synchronně načíst (nebo se to už stalo v Listeneru)
            if (ch == null) {
                // Možnost 1: Vyzveme hráče k čekání (bezpečnější pro DB)
                player.sendMessage("§cData postavy se ještě nenačetla. Zkus to za chvíli znovu.");
                return true; 
                
                // Možnost 2: Nouzové načtení (nedoporučuje se, může seknout)
                // plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> characterManager.loadCharacter(player));
                // return true;
            }

            // Původní kontrola:
            if (ch.hasCharacter()) {
                player.sendMessage("§cPostavu už máš! (" + ch.getFullName() + ")");
                return true;
            }
            // ------------------------------------------

            String jmeno = args[1];
            String puvod = args[2];
            int vek;

            try {
                vek = Integer.parseInt(args[3]);
            } catch (NumberFormatException e) {
                player.sendMessage("§cVěk musí být číslo!");
                return true;
            }

            // Vytvoření
            // POZOR: Tato operace volá DB, ačkoliv je volaná synchronně, CharacterManager to řeší asynchronně.
            boolean success = characterManager.createCharacter(player, jmeno, puvod, vek);
            if (success) {
                player.sendMessage("§aPostava úspěšně vytvořena: " + jmeno + " z " + puvod + " (" + vek + " let).");
                player.sendMessage("§ePříběh začíná...");
            } else {
                player.sendMessage("§cChyba při ukládání do databáze.");
            }
            return true;
        }

        player.sendMessage("§ePoužití: /char create <Jméno> <Původ> <Věk>");
        return true;
    }
}