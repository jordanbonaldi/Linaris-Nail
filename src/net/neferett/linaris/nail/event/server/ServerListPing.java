package net.neferett.linaris.nail.event.server;

import org.bukkit.event.EventHandler;
import org.bukkit.event.server.ServerListPingEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;

public class ServerListPing extends NailListener {
    public ServerListPing(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onServerListPing(final ServerListPingEvent event) {
        event.setMotd(Step.getMOTD());
    }
}
