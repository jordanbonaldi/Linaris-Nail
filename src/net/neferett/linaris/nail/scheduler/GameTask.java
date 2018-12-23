package net.neferett.linaris.nail.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.Objective;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;

public class GameTask extends BukkitRunnable {
    public static int remainingDurationInSecs = 1200;
    private final NailPlugin plugin;

    public GameTask(final NailPlugin plugin) {
        this.plugin = plugin;
        this.runTaskTimer(plugin, 0l, 20l);
    }

    @Override
    public void run() {
        if (GameTask.remainingDurationInSecs == 0 || !Step.isStep(Step.IN_GAME)) {
            this.cancel();
            if (GameTask.remainingDurationInSecs == 0) {
                Bukkit.broadcastMessage(NailPlugin.prefix + ChatColor.GRAY + "L'équipe des " + Team.ATTACKERS.getColor() + Team.ATTACKERS.getDisplayName() + ChatColor.GRAY + " n'a pas réussi à exploser les deux éponges.");
                plugin.stopGame(Team.DEFENDERS);
            }
            return;
        }
        final int remainingMins = GameTask.remainingDurationInSecs / 60 % 60;
        final int remainingSecs = GameTask.remainingDurationInSecs % 60;
        final Objective objective = Bukkit.getScoreboardManager().getMainScoreboard().getObjective("nail");
        objective.setDisplayName(ChatColor.DARK_GRAY + "-" + ChatColor.YELLOW + "Nail " + ChatColor.GREEN + (remainingMins < 10 ? "0" : "") + remainingMins + ":" + (remainingSecs < 10 ? "0" : "") + remainingSecs + ChatColor.DARK_GRAY + "-");
        if (GameTask.remainingDurationInSecs % 60 == 0 || remainingMins == 0 && (remainingSecs % 10 == 0 || remainingSecs < 10)) {
            Bukkit.broadcastMessage(NailPlugin.prefix + ChatColor.GRAY + "Victoire des " + Team.DEFENDERS.getColor() + Team.DEFENDERS.getDisplayName() + ChatColor.GRAY + " dans " + ChatColor.AQUA + (remainingMins > 0 ? remainingMins + " minute" + (remainingMins > 1 ? "s" : "") : "") + (remainingSecs > 0 ? (remainingMins > 0 ? " " : "") + remainingSecs + " seconde" + (remainingSecs > 1 ? "s" : "") : "") + ".");
            if (remainingMins == 0 && (remainingSecs % 10 == 0 || remainingSecs <= 5)) {
                for (final Player player : Team.ATTACKERS.getPlayers()) {
                    player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                }
            }
        }
        GameTask.remainingDurationInSecs--;
    }
}
