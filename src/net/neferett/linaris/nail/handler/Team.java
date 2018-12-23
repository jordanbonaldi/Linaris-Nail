package net.neferett.linaris.nail.handler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.Data;
import net.neferett.linaris.nail.util.ItemBuilder;

import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scoreboard.Scoreboard;

@Data
public class Team {
    public static List<Team> allTeams = new ArrayList<>();
    public static Team DEFENDERS = new Team("defenders", "Défenseurs", new ItemBuilder(Material.INK_SACK, DyeColor.BLUE.getDyeData()).setTitle(ChatColor.BLUE + "Rejoindre les Défenseurs").build(), ChatColor.BLUE);
    public static Team ATTACKERS = new Team("attackers", "Attaquants", new ItemBuilder(Material.INK_SACK, DyeColor.RED.getDyeData()).setTitle(ChatColor.RED + "Rejoindre les Attaquants").build(), ChatColor.RED);

    public static List<Team> getTeams() {
        return Team.allTeams;
    }

    public static Team getPlayerTeam(final Player player) {
        for (final Team team : Team.allTeams) {
            if (team.getScoreboardTeam().getPlayers().contains(player)) { return team; }
        }
        return null;
    }

    public static Team getRandomTeam() {
        Team lowest = null;
        for (final Team team : Team.allTeams) {
            if (lowest == null || team.getScoreboardTeam().getPlayers().size() < lowest.getScoreboardTeam().getPlayers().size()) {
                lowest = team;
            }
        }
        return lowest;
    }

    public static Team getTeam(final String name) {
        for (final Team team : Team.allTeams) {
            if (team.getScoreboardTeam() != null && team.getScoreboardTeam().getName().equalsIgnoreCase(name)) { return team; }
        }
        return null;
    }

    public static Team getTeam(final ChatColor color) {
        for (final Team team : Team.allTeams) {
            if (team.getColor() == color) { return team; }
        }
        return null;
    }

    private final String name;
    private final String displayName;
    private final ItemStack icon;
    private final ChatColor color;
    private org.bukkit.scoreboard.Team scoreboardTeam;
    private Location spawnLocation;
    private int points;

    private Team(final String name, final String displayName, final ItemStack icon, final ChatColor color) {
        this.name = name;
        this.displayName = displayName;
        this.icon = icon;
        this.color = color;
        Team.allTeams.add(this);
    }

    public void broadcastMessage(final String msg) {
        for (final Player player : this.getPlayers()) {
            player.sendMessage(msg);
        }
    }

    public void createTeam(final Scoreboard scoreboard) {
        scoreboardTeam = scoreboard.getTeam(name);
        if (scoreboardTeam == null) {
            scoreboardTeam = scoreboard.registerNewTeam(name);
        }
        scoreboardTeam.setPrefix(color.toString());
        scoreboardTeam.setDisplayName(name);
        scoreboardTeam.setAllowFriendlyFire(false);
    }

    public Set<Player> getPlayers() {
        final Set<Player> players = new HashSet<>();
        for (final OfflinePlayer offline : scoreboardTeam.getPlayers()) {
            if (offline instanceof Player) {
                players.add((Player) offline);
            }
        }
        return players;
    }
}
