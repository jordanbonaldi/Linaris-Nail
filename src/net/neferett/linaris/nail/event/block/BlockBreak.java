package net.neferett.linaris.nail.event.block;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.BlockBreakEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;
import net.neferett.linaris.nail.scheduler.AttackersTeleportation;

public class BlockBreak extends NailListener {
    public BlockBreak(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBlockBreak(final BlockBreakEvent event) {
        final Player player = event.getPlayer();
        final Block block = event.getBlock();
        final Team team = Team.getPlayerTeam(player);
        if (Step.isStep(Step.LOBBY) || team == null || team == Team.ATTACKERS && AttackersTeleportation.timeUntilTeleporation > 0) {
            if (!player.isOp()) {
                event.setCancelled(true);
            }
        } else {
            if (block.getType() == Material.SPONGE) {
                event.setCancelled(true);
                player.sendMessage(NailPlugin.prefix + (team == Team.ATTACKERS ? ChatColor.RED + "Vous devez obligatoirement faire exploser les éponges à la TNT." : ChatColor.RED + "Vous ne pouvez faire perdre votre équipe."));
            } else if (team == Team.DEFENDERS && plugin.cuboid.contains(block.getLocation())) {
                event.setCancelled(true);
                player.sendMessage(NailPlugin.prefix + ChatColor.RED + "Vous ne pouvez pas casser la base de vos ennemis.");
            }
        }
    }
}
