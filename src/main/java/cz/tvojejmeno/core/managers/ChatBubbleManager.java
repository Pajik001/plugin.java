package cz.tvojejmeno.core.managers;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.entity.Display;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

public class ChatBubbleManager {

    private final Main plugin;

    public ChatBubbleManager(Main plugin) {
        this.plugin = plugin;
    }

    public void spawnBubble(Player player, String text, int seconds, org.bukkit.Color bgColor) {
        // Spawneme TextDisplay entitu
        TextDisplay display = (TextDisplay) player.getWorld().spawnEntity(player.getLocation().add(0, 2.2, 0), EntityType.TEXT_DISPLAY);
        
        display.text(Component.text(text));
        display.setBillboard(Display.Billboard.CENTER); // Otáčí se na hráče
        display.setBackgroundColor(bgColor); // Průhledné pozadí
        display.setSeeThrough(false);
        display.setShadowed(true);

        // Zvětšení textu (Scale)
        Transformation transformation = display.getTransformation();
        transformation.getScale().set(1.5f, 1.5f, 1.5f); // Trochu větší text
        display.setTransformation(transformation);

        // Připojení k hráči (aby se hýbala s ním)
        player.addPassenger(display);

        // Smazání po X sekundách
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            display.remove();
        }, seconds * 20L);
    }
}