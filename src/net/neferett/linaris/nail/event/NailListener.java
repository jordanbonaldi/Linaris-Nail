package net.neferett.linaris.nail.event;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import net.neferett.linaris.nail.NailPlugin;

import org.bukkit.event.Listener;

@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class NailListener implements Listener {
    protected NailPlugin plugin;
}
