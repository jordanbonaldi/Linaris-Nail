package net.neferett.linaris.nail.event.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;
import net.neferett.linaris.nail.scheduler.BeginCountdown;

public class PlayerJoin extends NailListener {
    public PlayerJoin(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerJoin(final PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        player.getInventory().clear();
        if (!Step.isStep(Step.LOBBY) && player.hasPermission("games.join")) {
            event.setJoinMessage(null);
            player.setGameMode(GameMode.ADVENTURE);
            for (final Player online : Bukkit.getOnlinePlayers()) {
                if (player != online && Team.getPlayerTeam(online) != null) {
                    online.hidePlayer(player);
                }
            }
            player.setGameMode(GameMode.CREATIVE);
        } else if (Step.isStep(Step.LOBBY)) {
            event.setJoinMessage(NailPlugin.prefix + ChatColor.YELLOW + player.getName() + ChatColor.GRAY + " a rejoint la partie " + ChatColor.GREEN + "(" + Bukkit.getOnlinePlayers().length + "/" + Bukkit.getMaxPlayers() + ")");
            if (plugin.cuboid != null) {
                for (final Team team : Team.allTeams) {
                    if (team.getSpawnLocation() != null) {
                        player.getInventory().addItem(team.getIcon());
                    }
                }
            }
            plugin.loadData(player);
            player.setGameMode(GameMode.ADVENTURE);
            player.teleport(plugin.lobbyLocation);
            if (Bukkit.getOnlinePlayers().length == 2 && !BeginCountdown.started) {
                for (final Team team : Team.allTeams) {
                    if (team.getSpawnLocation() == null) {
                        BeginCountdown.started = true;
                        return;
                    }
                }
                new BeginCountdown(plugin);
            }
        }
    }
}
