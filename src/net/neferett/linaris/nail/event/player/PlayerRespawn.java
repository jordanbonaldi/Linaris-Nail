package net.neferett.linaris.nail.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerRespawnEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;

public class PlayerRespawn extends NailListener {

    public PlayerRespawn(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerRespawn(final PlayerRespawnEvent event) {
        if (Step.isStep(Step.IN_GAME)) {
            final Player player = event.getPlayer();
            final Team team = Team.getPlayerTeam(player);
            event.setRespawnLocation(team == null ? player.getWorld().getSpawnLocation() : team.getSpawnLocation());
        }
    }
}
