package net.neferett.linaris.nail.event.block;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockPlaceEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;
import net.neferett.linaris.nail.scheduler.AttackersTeleportation;

public class BlockPlace extends NailListener {
    public BlockPlace(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBlockPlace(final BlockPlaceEvent event) {
        final Player player = event.getPlayer();
        final Team team = Team.getPlayerTeam(player);
        if (Step.isStep(Step.LOBBY) || team == Team.ATTACKERS && AttackersTeleportation.timeUntilTeleporation > 0) {
            if (!player.isOp()) {
                event.setCancelled(true);
            }
        } else if (team == Team.DEFENDERS && plugin.cuboid.contains(event.getBlock().getLocation())) {
            event.setCancelled(true);
            player.sendMessage(NailPlugin.prefix + ChatColor.RED + "Vous ne pouvez pas poser de blocs dans la base de vos ennemis.");
        }
    }
}
