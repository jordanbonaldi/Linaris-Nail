package net.neferett.linaris.nail.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerKickEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;

public class PlayerKick extends NailListener {
    public PlayerKick(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerKick(final PlayerKickEvent event) {
        event.setLeaveMessage(null);
        final Player player = event.getPlayer();
        player.getInventory().clear();
        plugin.playerLoose(player);
    }
}
