package net.neferett.linaris.nail.event.player;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;

public class PlayerQuit extends NailListener {
    public PlayerQuit(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerQuit(final PlayerQuitEvent event) {
        event.setQuitMessage(null);
        final Player player = event.getPlayer();
        player.getInventory().clear();
        plugin.playerLoose(player);
    }
}
