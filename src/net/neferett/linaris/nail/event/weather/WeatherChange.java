package net.neferett.linaris.nail.event.weather;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.weather.WeatherChangeEvent;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;

public class WeatherChange extends NailListener {
    public WeatherChange(final NailPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onWeatherChange(final WeatherChangeEvent event) {
        final World world = event.getWorld();
        if (!world.isThundering() && !world.hasStorm()) {
            event.setCancelled(true);
        }
    }
}
