package net.neferett.linaris.nail.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;
import net.neferett.linaris.nail.scheduler.AttackersTeleportation;

public class PlayerDamage extends NailListener {
    public PlayerDamage(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerDamage(final EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            final Team team = Team.getPlayerTeam(player);
            if (!Step.isStep(Step.IN_GAME) || team == null || team == Team.ATTACKERS && AttackersTeleportation.timeUntilTeleporation > 0) {
                event.setCancelled(true);
            }
        }
    }
}
