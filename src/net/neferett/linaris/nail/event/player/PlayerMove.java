package net.neferett.linaris.nail.event.player;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;
import net.neferett.linaris.nail.scheduler.AttackersTeleportation;

public class PlayerMove extends NailListener {
    public PlayerMove(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerMove(final PlayerMoveEvent event) {
        final Player player = event.getPlayer();
        final Location from = event.getFrom();
        final Location to = event.getTo();
        final int y = to.getBlockY();
        if (from.getBlockX() != to.getBlockX() || from.getBlockY() != y || from.getBlockZ() != to.getBlockZ()) {
            final Team team = Team.getPlayerTeam(player);
            if (!Step.isStep(Step.IN_GAME) || team == null || team == Team.ATTACKERS && AttackersTeleportation.timeUntilTeleporation > 0) {
                if (y <= 0) {
                    player.teleport(plugin.lobbyLocation);
                }
            } else if (team == Team.DEFENDERS && plugin.cuboid.contains(to)) {
                event.setCancelled(true);
                player.sendMessage(NailPlugin.prefix + ChatColor.RED + "Vous ne pouvez pas entrer dans la base de vos ennemis.");
            }
        }
    }
}
