package net.neferett.linaris.nail.scheduler;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.handler.Team;

public class AttackersTeleportation extends BukkitRunnable {
    public static int timeUntilTeleporation = 60;

    public AttackersTeleportation(final NailPlugin plugin) {
        this.runTaskTimer(plugin, 0, 20);
    }

    @Override
    public void run() {
        if (AttackersTeleportation.timeUntilTeleporation == 0) {
            this.cancel();
            Bukkit.broadcastMessage(NailPlugin.prefix + ChatColor.GRAY + "L'équipe des " + Team.ATTACKERS.getColor() + Team.ATTACKERS.getDisplayName() + ChatColor.GRAY + " a été téléporté dans leur base !");
            for (final Player player : Team.ATTACKERS.getPlayers()) {
                player.setFallDistance(0);
                player.teleport(Team.ATTACKERS.getSpawnLocation());
            }
            return;
        } else {
            final int remainingMins = AttackersTeleportation.timeUntilTeleporation / 60 % 60;
            final int remainingSecs = AttackersTeleportation.timeUntilTeleporation % 60;
            if (AttackersTeleportation.timeUntilTeleporation % 30 == 0 || remainingMins == 0 && (remainingSecs % 10 == 0 || remainingSecs <= 5)) {
                Team.ATTACKERS.broadcastMessage(NailPlugin.prefix + String.format(ChatColor.GRAY + "Vous serez téléporté dans " + ChatColor.AQUA + "%s.", (remainingMins > 0 ? remainingMins + " minute" + (remainingMins > 1 ? "s" : "") : "") + (remainingSecs > 0 ? (remainingMins > 0 ? " " : "") + remainingSecs + " seconde" + (remainingSecs > 1 ? "s" : "") : "")));
                if (remainingMins == 0 && (remainingSecs % 10 == 0 || remainingSecs <= 5)) {
                    for (final Player player : Team.ATTACKERS.getPlayers()) {
                        player.playSound(player.getLocation(), Sound.CLICK, 1, 1);
                    }
                }
            }
        }
        AttackersTeleportation.timeUntilTeleporation--;
    }
}
