package net.neferett.linaris.nail;

import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import lombok.SneakyThrows;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.event.block.BlockBreak;
import net.neferett.linaris.nail.event.block.BlockPlace;
import net.neferett.linaris.nail.event.entity.EntityDamage;
import net.neferett.linaris.nail.event.entity.EntityDamageByPlayer;
import net.neferett.linaris.nail.event.entity.EntityExplode;
import net.neferett.linaris.nail.event.entity.FoodLevelChange;
import net.neferett.linaris.nail.event.player.AsyncPlayerChat;
import net.neferett.linaris.nail.event.player.PlayerCommandPreprocess;
import net.neferett.linaris.nail.event.player.PlayerDamage;
import net.neferett.linaris.nail.event.player.PlayerDeath;
import net.neferett.linaris.nail.event.player.PlayerDropItem;
import net.neferett.linaris.nail.event.player.PlayerInteract;
import net.neferett.linaris.nail.event.player.PlayerJoin;
import net.neferett.linaris.nail.event.player.PlayerKick;
import net.neferett.linaris.nail.event.player.PlayerLogin;
import net.neferett.linaris.nail.event.player.PlayerMove;
import net.neferett.linaris.nail.event.player.PlayerPickupItem;
import net.neferett.linaris.nail.event.player.PlayerQuit;
import net.neferett.linaris.nail.event.player.PlayerRespawn;
import net.neferett.linaris.nail.event.server.ServerCommand;
import net.neferett.linaris.nail.event.server.ServerListPing;
import net.neferett.linaris.nail.event.weather.ThunderChange;
import net.neferett.linaris.nail.event.weather.WeatherChange;
import net.neferett.linaris.nail.handler.MySQL;
import net.neferett.linaris.nail.handler.PlayerData;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;
import net.neferett.linaris.nail.util.Cuboid;
import net.neferett.linaris.nail.util.FileUtils;
import net.neferett.linaris.nail.util.ReflectionHandler;
import net.neferett.linaris.nail.util.ReflectionHandler.PackageType;

import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;

public class NailPlugin extends JavaPlugin {
    public static String prefix = ChatColor.DARK_GRAY + "[" + ChatColor.BLUE + "NAIL" + ChatColor.DARK_GRAY + "]" + ChatColor.WHITE + " ";

    private World world;
    public MySQL database;
    public Location lobbyLocation;
    public Location firstPoint;
    public Location secondPoint;
    public Cuboid cuboid;
    private final Map<UUID, PlayerData> data = new HashMap<>();

    private void load() {
        this.saveDefaultConfig();
        final Scoreboard scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
        Team.DEFENDERS.createTeam(scoreboard);
        Team.ATTACKERS.createTeam(scoreboard);
        final ConfigurationSection teams = this.getConfig().getConfigurationSection("teams");
        if (teams != null) {
            final Objective objective = scoreboard.registerNewObjective("teams", "dummy");
            objective.setDisplayName(ChatColor.GRAY + "Kills");
            objective.setDisplaySlot(DisplaySlot.SIDEBAR);
            if (teams.isString("defenders")) {
                Team.DEFENDERS.setSpawnLocation(this.toLocation(teams.getString("defenders")));
                final String name = Team.DEFENDERS.getColor() + StringUtils.capitalize(Team.DEFENDERS.getDisplayName());
                objective.getScore(Bukkit.getOfflinePlayer(name)).setScore(1);
                objective.getScore(Bukkit.getOfflinePlayer(name)).setScore(0);
            }
            if (teams.isString("attackers")) {
                Team.ATTACKERS.setSpawnLocation(this.toLocation(teams.getString("attackers")));
                final String name = Team.ATTACKERS.getColor() + StringUtils.capitalize(Team.ATTACKERS.getDisplayName());
                objective.getScore(Bukkit.getOfflinePlayer(name)).setScore(1);
                objective.getScore(Bukkit.getOfflinePlayer(name)).setScore(0);
            }
        }
        final ConfigurationSection cuboid = this.getConfig().getConfigurationSection("cuboid");
        if (cuboid != null) {
            firstPoint = this.toLocation(cuboid.getString("first"));
            secondPoint = this.toLocation(cuboid.getString("second"));
            this.cuboid = new Cuboid(firstPoint, secondPoint);
        }
        lobbyLocation = this.toLocation(this.getConfig().getString("lobby", this.toString(world.getSpawnLocation())));
    }

    public PlayerData getData(final Player player) {
        PlayerData data = this.data.get(player.getUniqueId());
        if (data == null) {
            data = new PlayerData(player.getUniqueId(), player.getName(), 0, 0, 0, 0, 0, 0);
            this.loadData(player);
        }
        return data;
    }

    public void loadData(final Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    final ResultSet res = database.querySQL("SELECT * FROM players WHERE uuid=UNHEX('" + player.getUniqueId().toString().replaceAll("-", "") + "')");
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            PlayerData data = null;
                            try {
                                if (res.first()) {
                                    data = new PlayerData(player.getUniqueId(), res.getString("name"), res.getInt("sw_more_health"), res.getInt("sw_better_bow"), res.getInt("sw_better_sword"), res.getInt("sw_more_sheep"), res.getInt("sw_mobility"), 0);
                                } else {
                                    data = new PlayerData(player.getUniqueId(), player.getName(), 0, 0, 0, 0, 0, 0);
                                }
                                NailPlugin.this.data.put(player.getUniqueId(), data);
                            } catch (final SQLException e) {
                                player.kickPlayer(ChatColor.RED + "Impossible de charger vos statistiques... :(");
                            }
                        }
                    }.runTask(NailPlugin.this);
                } catch (ClassNotFoundException | SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    public void stopGame(final Team winnerTeam) {
        Step.setCurrentStep(Step.POST_GAME);
        Bukkit.broadcastMessage(NailPlugin.prefix + ChatColor.GOLD + ChatColor.BOLD + "Victoire de l'équipe " + winnerTeam.getColor() + ChatColor.BOLD + winnerTeam.getDisplayName() + " " + ChatColor.YELLOW + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|" + ChatColor.YELLOW + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.BOLD + " Félicitations " + ChatColor.YELLOW + ChatColor.MAGIC + " |" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|" + ChatColor.YELLOW + ChatColor.MAGIC + "|" + ChatColor.AQUA + ChatColor.MAGIC + "|" + ChatColor.GREEN + ChatColor.MAGIC + "|" + ChatColor.RED + ChatColor.MAGIC + "|" + ChatColor.LIGHT_PURPLE + ChatColor.MAGIC + "|");
        for (final Entry<UUID, PlayerData> entry : NailPlugin.this.data.entrySet()) {
            final String uuid = entry.getKey().toString().replaceAll("-", "");
            final PlayerData data = entry.getValue();
            final Player online = Bukkit.getPlayer(entry.getKey());
            if (online != null && online.isOnline()) {
                data.addCoins(Team.getPlayerTeam(online) == winnerTeam ? 9 : 1.5);
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    try {
                        final ResultSet res = database.querySQL("SELECT name FROM players WHERE uuid=UNHEX('" + uuid + "')");
                        if (res.first()) {
                            database.updateSQL("UPDATE players SET name='" + data.getName() + "', coins=coins+" + data.getCoins() + ", updated_at=NOW() WHERE uuid=UNHEX('" + uuid + "')");
                        } else {
                            database.updateSQL("INSERT INTO players(name, uuid, coins, created_at, updated_at) VALUES('" + data.getName() + "', UNHEX('" + uuid + "'), " + data.getCoins() + ", NOW(), NOW())");
                        }
                    } catch (ClassNotFoundException | SQLException e) {
                        e.printStackTrace();
                    }
                }
            }.runTaskAsynchronously(NailPlugin.this);
        }
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Player online : Bukkit.getOnlinePlayers()) {
                    NailPlugin.this.teleportToLobby(online);
                }
            }
        }.runTaskLater(this, 300);
        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.shutdown();
            }
        }.runTaskLater(this, 400);
    }

    public void teleportToLobby(final Player player) {
        final ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF("lobby");
        player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {
        if (command.getName().equalsIgnoreCase("nail")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "Vous devez être un joueur.");
                return true;
            }
            final Player player = (Player) sender;
            if (args.length != 0) {
                final String sub = args[0];
                if (sub.equalsIgnoreCase("help")) {
                    player.sendMessage(ChatColor.GOLD + "Aide du plugin Nail :");
                    player.sendMessage("/nail setlobby" + ChatColor.YELLOW + " - définit le lobby du jeu");
                    player.sendMessage("/nail setcuboid" + ChatColor.YELLOW + " - définit le cuboid des " + Team.ATTACKERS.getDisplayName());
                    player.sendMessage("/nail setspawn <couleur>" + ChatColor.YELLOW + " - définit le spawn de l'équipe <couleur>");
                } else if (sub.equalsIgnoreCase("setlobby")) {
                    lobbyLocation = player.getLocation();
                    player.sendMessage(ChatColor.GREEN + "Vous avez défini le lobby avec succès.");
                    this.getConfig().set("lobby", this.toString(player.getLocation()));
                    this.saveConfig();
                } else if (sub.equalsIgnoreCase("setspawn")) {
                    if (!args[1].equalsIgnoreCase("defenders") && !args[1].equalsIgnoreCase("attackers")) {
                        player.sendMessage(ChatColor.RED + "La couleur " + ChatColor.DARK_RED + args[1] + ChatColor.RED + " n'existe pas.");
                    } else {
                        final Location location = player.getLocation();
                        final Team team = Team.getTeam(args[1]);
                        player.sendMessage(ChatColor.GREEN + "Vous avez défini avec succès le spawn de l'équipe des " + team.getColor() + team.getName());
                        team.setSpawnLocation(location);
                        this.getConfig().set("teams." + args[1], this.toString(location));
                        this.saveConfig();
                    }
                } else if (sub.equalsIgnoreCase("setcuboid")) {
                    if (firstPoint == null || secondPoint == null) {
                        player.sendMessage(ChatColor.RED + "Vous devez d'abord séléctionner deux positions avec un " + ChatColor.DARK_RED + "blaze rod");
                    } else {
                        cuboid = new Cuboid(firstPoint, secondPoint);
                        this.getConfig().set("cuboid.first", this.toString(firstPoint));
                        this.getConfig().set("cuboid.second", this.toString(secondPoint));
                        this.saveConfig();
                        player.sendMessage(ChatColor.GREEN + "Vous avez défini avec succès le cuboid de l'équipe des " + Team.ATTACKERS.getColor() + Team.ATTACKERS.getDisplayName());
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "Mauvais arguments ou commande inexistante. Tapez " + ChatColor.DARK_RED + "/nail help" + ChatColor.RED + " pour de l'aide.");
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public void onDisable() {
        this.save();
    }

    @Override
    public void onEnable() {
        Step.setCurrentStep(Step.LOBBY);
        world = Bukkit.getWorlds().get(0);
        this.load();
        database = new MySQL(this, this.getConfig().getString("mysql.host"), this.getConfig().getString("mysql.port"), this.getConfig().getString("mysql.database"), this.getConfig().getString("mysql.user"), this.getConfig().getString("mysql.pass"));
        try {
            database.openConnection();
            database.updateSQL("CREATE TABLE IF NOT EXISTS `players` ( `id` int(11) NOT NULL AUTO_INCREMENT, `name` varchar(30) NOT NULL, `uuid` varbinary(16) NOT NULL, `coins` double NOT NULL, `sw_more_health` int(11) DEFAULT '0' NOT NULL, `sw_better_bow` int(11) DEFAULT '0' NOT NULL, `sw_better_sword` int(11) DEFAULT '0' NOT NULL, `sw_mobility` int(11) DEFAULT '0' NOT NULL, `sw_more_sheep` int(11) DEFAULT '0' NOT NULL, `created_at` datetime NOT NULL, `updated_at` datetime NOT NULL, PRIMARY KEY (`id`) ) ENGINE=InnoDB DEFAULT CHARSET=latin1 AUTO_INCREMENT=1 ;");
        } catch (ClassNotFoundException | SQLException e) {
            this.getLogger().severe("Impossible de se connecter à la base de données :");
            e.printStackTrace();
            this.getLogger().severe("Arrêt du serveur...");
            Bukkit.shutdown();
            return;
        }
        this.register(BlockBreak.class, BlockPlace.class, EntityDamage.class, EntityDamageByPlayer.class, EntityExplode.class, FoodLevelChange.class, AsyncPlayerChat.class, PlayerCommandPreprocess.class, PlayerDamage.class, PlayerDeath.class, PlayerDropItem.class, PlayerInteract.class, PlayerJoin.class, PlayerKick.class, PlayerLogin.class, PlayerMove.class, PlayerPickupItem.class, PlayerQuit.class, PlayerRespawn.class, ServerCommand.class, ServerListPing.class, ThunderChange.class, WeatherChange.class);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
    }

    @SneakyThrows
    @Override
    public void onLoad() {
        Bukkit.unloadWorld("world", false);
        final File worldContainer = this.getServer().getWorldContainer();
        final File worldFolder = new File(worldContainer, "world");
        final File copyFolder = new File(worldContainer, "nail");
        if (copyFolder.exists()) {
            ReflectionHandler.getClass("RegionFileCache", PackageType.MINECRAFT_SERVER).getMethod("a").invoke(null);
            FileUtils.delete(worldFolder);
            FileUtils.copyFolder(copyFolder, worldFolder);
        }
    }

    public void playerLoose(final Player player) {
        if (Step.isStep(Step.LOBBY)) {
            data.remove(player.getUniqueId());
        }
        final Team team = Team.getPlayerTeam(player);
        if (team != null) {
            team.getScoreboardTeam().removePlayer(player);
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (Step.isStep(Step.IN_GAME) && team.getScoreboardTeam().getPlayers().size() == 0) {
                        Team.allTeams.remove(team);
                        if (Team.allTeams.size() == 1) {
                            NailPlugin.this.stopGame(Team.allTeams.get(0));
                        }
                    }
                }
            }.runTaskLater(NailPlugin.this, 1);
        }
    }

    @SneakyThrows
    private void register(final Class<? extends NailListener>... classes) {
        for (final Class<? extends NailListener> clazz : classes) {
            final Constructor<? extends NailListener> constructor = clazz.getConstructor(NailPlugin.class);
            Bukkit.getPluginManager().registerEvents(constructor.newInstance(this), this);
        }
    }

    private void save() {
        this.getConfig().set("lobby", this.toString(lobbyLocation));
        for (final Team team : Team.allTeams) {
            final String name = team.getScoreboardTeam().getName();
            if (team.getSpawnLocation() != null) {
                this.getConfig().set("teams." + name, this.toString(team.getSpawnLocation()));
            }
        }
        this.saveConfig();
    }

    private Location toLocation(final String string) {
        final String[] splitted = string.split("_");
        World world = Bukkit.getWorld(splitted[0]);
        if (world == null || splitted.length < 6) {
            world = this.world;
        }
        return new Location(world, Double.parseDouble(splitted[1]), Double.parseDouble(splitted[2]), Double.parseDouble(splitted[3]), Float.parseFloat(splitted[4]), Float.parseFloat(splitted[5]));
    }

    private String toString(final Location location) {
        final World world = location.getWorld();
        return world.getName() + "_" + location.getX() + "_" + location.getY() + "_" + location.getZ() + "_" + location.getYaw() + "_" + location.getPitch();
    }
}
