package cz.tvojejmeno.core.listeners;

import cz.tvojejmeno.core.Main;
import cz.tvojejmeno.core.managers.CharacterManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class PlayerConnectionListener implements Listener {

    private final Main plugin;
    private final CharacterManager characterManager;

    public PlayerConnectionListener(Main plugin) {
        this.plugin = plugin;
        this.characterManager = plugin.getCharacterManager();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // 1. Skrytí Nametagu (Nad hlavou)
        player.setCustomNameVisible(false); // Pro jistotu
        hideNameTag(player);

        // 2. Načtení postavy (z minula)
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
            characterManager.loadCharacter(player);
            // ... zbytek logiky z minula ...
        });
    }
    public void onQuit(PlayerQuitEvent event) {
        characterManager.unloadCharacter(event.getPlayer()); // Toto zavolá saveCharacter
    }

    private void hideNameTag(Player player) {
        Scoreboard sb = Bukkit.getScoreboardManager().getMainScoreboard();
        Team team = sb.getTeam("hide_names");
        if (team == null) {
            team = sb.registerNewTeam("hide_names");
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
        }
        team.addEntry(player.getName());
        player.setScoreboard(sb);
    }
}