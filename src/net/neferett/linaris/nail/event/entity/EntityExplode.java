package net.neferett.linaris.nail.event.entity;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityExplodeEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;

public class EntityExplode extends NailListener {
    public EntityExplode(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityExplode(final EntityExplodeEvent event) {
        if (Step.isStep(Step.LOBBY)) {
            event.setCancelled(true);
            return;
        } else if (Step.isStep(Step.IN_GAME)) {
            Player player = null;
            Team team = null;
            if (event.getEntity() instanceof TNTPrimed) {
                final TNTPrimed tnt = (TNTPrimed) event.getEntity();
                if (tnt.getSource() instanceof Player) {
                    player = (Player) tnt.getSource();
                    team = Team.getPlayerTeam(player);
                }
            }
            boolean message = true;
            final Iterator<Block> blocks = event.blockList().iterator();
            while (blocks.hasNext()) {
                final Block block = blocks.next();
                if (block.getType() == Material.SPONGE) {
                    blocks.remove();
                    if (team == null || team == Team.DEFENDERS) {
                        if (message && player != null) {
                            message = false;
                            player.sendMessage(NailPlugin.prefix + ChatColor.RED + "Vous ne pouvez pas faire perdre votre équipe.");
                        }
                    } else {
                        block.setType(Material.AIR);
                        Bukkit.broadcastMessage(NailPlugin.prefix + ChatColor.GRAY + "L'équipe des " + Team.DEFENDERS.getColor() + Team.DEFENDERS.getDisplayName() + ChatColor.GRAY + " n'a pas réussi à défendre les deux éponges.");
                        plugin.stopGame(Team.ATTACKERS);
                        break;
                    }
                }
            }
        }
    }
}
