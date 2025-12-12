package cz.tvojejmeno.core.commands;

import cz.tvojejmeno.core.Main;
import net.kyori.adventure.text.Component;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Villager;
import org.jetbrains.annotations.NotNull;

public class NPCCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        if (!player.hasPermission("core.admin")) return true;

        if (args.length < 1) {
            player.sendMessage("§cPoužití: /core npc <banka/urad>");
            return true;
        }

        Location loc = player.getLocation();
        Villager npc = (Villager) player.getWorld().spawnEntity(loc, EntityType.VILLAGER);
        
        // Zákaz pohybu a interakce
        npc.setAI(false);
        npc.setInvulnerable(true);
        npc.setRemoveWhenFarAway(false);

        String type = args[0].toLowerCase();
        if (type.equals("banka")) {
            npc.setCustomName("§6§lBanka");
            npc.setProfession(Villager.Profession.LIBRARIAN);
        } else if (type.equals("urad")) {
            npc.setCustomName("§9§lÚřad");
            npc.setProfession(Villager.Profession.CARTOGRAPHER);
        } else {
            player.sendMessage("§cNeznámý typ NPC.");
            npc.remove();
            return true;
        }
        
        npc.setCustomNameVisible(true);
        player.sendMessage("§aNPC " + type + " vytvořeno.");
        return true;
    }
}