package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class ChatListener implements Listener {

    private final Main plugin;
    private final double LOCAL_RADIUS;

    public ChatListener(Main plugin) {
        this.plugin = plugin;
        this.LOCAL_RADIUS = plugin.getConfig().getDouble("roleplay.local-ooc-range", 8.0);
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true); // Zrušíme vanilla chat
        Player player = event.getPlayer();
        String message = PlainTextComponentSerializer.plainText().serialize(event.message());

        // 1. GLOBAL OOC (Začíná "!")
        if (message.startsWith("!")) {
            String content = message.substring(1).trim();
            Component format = Component.text("[Global OOC] ", NamedTextColor.RED)
                    .append(Component.text(player.getName(), NamedTextColor.GRAY))
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(Component.text(content, NamedTextColor.WHITE));
            
            Bukkit.broadcast(format);
            return;
        }

        // 2. LOCAL OOC (Bez "!")
        Component format = Component.text("[Local OOC] ", NamedTextColor.BLUE)
                .append(Component.text(player.getName(), NamedTextColor.GRAY))
                .append(Component.text(": ", NamedTextColor.GRAY))
                .append(Component.text(message, NamedTextColor.GRAY)); // Šedá pro local

        int recipients = 0;
        for (Player target : player.getWorld().getPlayers()) {
            if (target.getLocation().distance(player.getLocation()) <= LOCAL_RADIUS) {
                target.sendMessage(format);
                recipients++;
            }
        }

        if (recipients <= 1) {
            player.sendActionBar(Component.text("§7Nikdo v okolí tě neslyší (Local OOC)."));
        }
    }
}