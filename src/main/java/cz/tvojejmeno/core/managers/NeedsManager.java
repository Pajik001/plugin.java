package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.models.RPCharacter;
import org.bukkit.entity.Player;

public class NeedsManager {

    private final Main plugin;

    // Hodnoty za vteřinu (upraveno na pomalejší)
    // 0.015 za vteřinu = cca 0.9 za minutu = 100% za ~110 minut
    private static final double TOILET_INCREMENT = 0.015; 
    private static final double THIRST_INCREMENT = 0.02;
    private static final double SLEEP_DECREMENT = 0.01;

    public NeedsManager(Main plugin) {
        this.plugin = plugin;
    }

    public void tick(Player player) {
        RPCharacter character = plugin.getCharacterManager().getCharacter(player);
        if (character == null || !character.hasCharacter()) return;

        // Aktualizace hodnot přímo v postavě
        double newToilet = character.getToilet() + TOILET_INCREMENT;
        double newThirst = character.getThirst() - THIRST_INCREMENT; // Žízeň ubývá (100 -> 0)
        double newSleep = character.getSleep() - SLEEP_DECREMENT;    // Energie ubývá

        character.setToilet((int) newToilet);
        character.setThirst(newThirst);
        character.setSleep(newSleep);

        // Varování
        if (character.getToilet() >= 95) {
            player.sendMessage("§c§lPotřebuješ nutně na toaletu!");
        }
        if (character.getThirst() <= 10) {
            player.sendMessage("§c§lUmíráš žízní!");
            player.damage(1.0);
        }
    }

    // Settery pro interakce (pití, záchod)
    public void setThirst(Player player, double value) {
        RPCharacter ch = plugin.getCharacterManager().getCharacter(player);
        if (ch != null) ch.setThirst(value);
    }
    
    public void cleanToilet(Player player) {
        RPCharacter ch = plugin.getCharacterManager().getCharacter(player);
        if (ch != null) ch.setToilet(0);
    }
}