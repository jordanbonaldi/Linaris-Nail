package net.neferett.linaris.nail.scheduler;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;

public class BeginCountdown extends BukkitRunnable {
    public static boolean started = false;
    public static int timeUntilStart = 120;

    private final NailPlugin plugin;

    public BeginCountdown(final NailPlugin plugin) {
        BeginCountdown.started = true;
        this.plugin = plugin;
        this.runTaskTimer(plugin, 0l, 20l);
    }

    @Override
    public void run() {
        if (BeginCountdown.timeUntilStart == 0) {
            this.cancel();
            if (Bukkit.getOnlinePlayers().length < 2) {
                Bukkit.broadcastMessage(NailPlugin.prefix + ChatColor.RED + "Il n'y a pas assez de joueurs !");
                BeginCountdown.timeUntilStart = 120;
                BeginCountdown.started = false;
            } else {
                Bukkit.broadcastMessage(NailPlugin.prefix + ChatColor.AQUA + "La partie commence, bon jeu !");
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    player.setFallDistance(0);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.getInventory().clear();
                    player.closeInventory();
                    Team team = Team.getPlayerTeam(player);
                    if (team == null) {
                        team = Team.getRandomTeam();
                        team.getScoreboardTeam().addPlayer(player);
                    }
                    player.sendMessage(NailPlugin.prefix + ChatColor.GRAY + "Vous faites parti des " + team.getColor() + team.getDisplayName());
                    if (team != Team.ATTACKERS) {
                        player.teleport(team.getSpawnLocation());
                    }
                }
                new AttackersTeleportation(plugin);
                Bukkit.broadcastMessage(ChatColor.GRAY + "Les " + Team.ATTACKERS.getColor() + Team.ATTACKERS.getDisplayName() + ChatColor.GRAY + " seront téléportés dans 1 minute, le temps que les " + Team.DEFENDERS.getColor() + Team.DEFENDERS.getDisplayName() + ChatColor.GRAY + " se préparent.");
                final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
                scoreboard.getObjective("teams").unregister();
                scoreboard.registerNewObjective("kills", "dummy").setDisplaySlot(DisplaySlot.PLAYER_LIST);
                final Objective objective = scoreboard.registerNewObjective("nail", "dummy");
                objective.setDisplayName(ChatColor.GRAY + "Nail " + ChatColor.GOLD + "20:00");
                objective.setDisplaySlot(DisplaySlot.SIDEBAR);
                for (final Team team : Team.allTeams) {
                    final String name = team.getColor() + StringUtils.capitalize(team.getDisplayName());
                    objective.getScore(name).setScore(1);
                    objective.getScore(name).setScore(0);
                }
                Step.setCurrentStep(Step.IN_GAME);
                new GameTask(plugin);
            }
            return;
        }
        final int remainingMins = BeginCountdown.timeUntilStart / 60 % 60;
        final int remainingSecs = BeginCountdown.timeUntilStart % 60;
        if (BeginCountdown.timeUntilStart % 30 == 0 || remainingMins == 0 && (remainingSecs % 10 == 0 || remainingSecs <= 5)) {
            Bukkit.broadcastMessage(NailPlugin.prefix + ChatColor.GOLD + "Démarrage du jeu dans " + ChatColor.YELLOW + (remainingMins > 0 ? remainingMins + " minute" + (remainingMins > 1 ? "s" : "") : "") + (remainingSecs > 0 ? (remainingMins > 0 ? " " : "") + remainingSecs + " seconde" + (remainingSecs > 1 ? "s" : "") : "") + ".");
            if (remainingMins == 0 && remainingSecs <= 10) {
                for (final Player player : Bukkit.getOnlinePlayers()) {
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
            }
        }
        BeginCountdown.timeUntilStart--;
    }
}
