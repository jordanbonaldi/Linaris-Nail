package net.neferett.linaris.nail.event.player;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.scoreboard.Score;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;

public class PlayerDeath extends NailListener {
    public PlayerDeath(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerDeath(final PlayerDeathEvent event) {
        if (!Step.isStep(Step.IN_GAME)) {
            event.setDeathMessage(null);
            event.getDrops().clear();
            event.setDroppedExp(0);
        } else {
            final Player player = event.getEntity();
            final Team team = Team.getPlayerTeam(player);
            Player killer = event.getEntity().getKiller();
            Team killerTeam = null;
            if (killer != null) {
                killerTeam = Team.getPlayerTeam(killer);
            }
            Bukkit.broadcastMessage(team.getColor() + player.getName() + ChatColor.WHITE + " " + (killer == null ? "est mort." : "a été tué par " + (killerTeam == null ? ChatColor.GRAY : killerTeam.getColor()) + killer.getName()));
            if (killer != null) {
                final Score kills = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("kills").getScore(killer);
                kills.setScore(kills.getScore() + 1);
                if (killerTeam != null) {
                    final Score score = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("nail").getScore(Bukkit.getOfflinePlayer(killerTeam.getColor() + StringUtils.capitalize(killerTeam.getDisplayName())));
                    score.setScore(score.getScore() + 1);
                }
            }
        }
    }
}
