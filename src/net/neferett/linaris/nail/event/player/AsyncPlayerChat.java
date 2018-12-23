package net.neferett.linaris.nail.event.player;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Team;

public class AsyncPlayerChat extends NailListener {
    public AsyncPlayerChat(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerChat(final AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        final Team playerTeam = Team.getPlayerTeam(player);
        event.setFormat((playerTeam != null ? playerTeam.getColor() : ChatColor.GRAY) + player.getName() + ChatColor.WHITE + ": " + event.getMessage());
    }
}
