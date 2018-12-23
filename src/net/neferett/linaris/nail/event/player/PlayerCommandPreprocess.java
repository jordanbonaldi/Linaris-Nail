package net.neferett.linaris.nail.event.player;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;

public class PlayerCommandPreprocess extends NailListener {
    public PlayerCommandPreprocess(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        final Player player = event.getPlayer();
        if (player.isOp() && event.getMessage().split(" ")[0].equalsIgnoreCase("/reload")) {
            event.setCancelled(true);
            player.sendMessage(ChatColor.RED + "Cette fonctionnalit� est d�sactiv�e par le plugin Nail � cause de contraintes techniques (reset de map).");
        }
    }
}
