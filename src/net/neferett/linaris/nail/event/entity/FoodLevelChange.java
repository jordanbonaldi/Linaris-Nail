package net.neferett.linaris.nail.event.entity;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.FoodLevelChangeEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;
import net.neferett.linaris.nail.scheduler.AttackersTeleportation;

public class FoodLevelChange extends NailListener {
    public FoodLevelChange(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onFoodLevelChange(final FoodLevelChangeEvent event) {
        if (event.getEntity() instanceof Player) {
            final Player player = (Player) event.getEntity();
            final Team team = Team.getPlayerTeam(player);
            if (Step.isStep(Step.LOBBY) || team == Team.ATTACKERS && AttackersTeleportation.timeUntilTeleporation > 0) {
                event.setCancelled(true);
            }
        }
    }
}
