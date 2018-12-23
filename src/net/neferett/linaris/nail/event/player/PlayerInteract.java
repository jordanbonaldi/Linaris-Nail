package net.neferett.linaris.nail.event.player;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

import net.neferett.linaris.nail.NailPlugin;
import net.neferett.linaris.nail.event.NailListener;
import net.neferett.linaris.nail.handler.Step;
import net.neferett.linaris.nail.handler.Team;
import net.neferett.linaris.nail.scheduler.AttackersTeleportation;

public class PlayerInteract extends NailListener {
    public PlayerInteract(final NailPlugin plugin) {
        super(plugin);
    }

    @SuppressWarnings("deprecation")
    @EventHandler
    public void onPlayerInteract(final PlayerInteractEvent event) {
        final Player player = event.getPlayer();
        if (!Step.isStep(Step.LOBBY)) {
            final Team team = Team.getPlayerTeam(player);
            if (team == Team.ATTACKERS && AttackersTeleportation.timeUntilTeleporation > 0 || team == Team.DEFENDERS && plugin.cuboid.contains(player.getLocation())) {
                event.setCancelled(true);
            }
        } else {
            if (event.hasItem()) {
                final ItemStack item = event.getItem();
                if (item.getType() == Material.WOOD_AXE && player.hasPermission("nail.admin")) {
                    event.setCancelled(true);
                    if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                        plugin.firstPoint = event.getClickedBlock().getLocation();
                        player.sendMessage(ChatColor.GREEN + "Vous avez défini le point 1 de la séléction.");
                    } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                        plugin.secondPoint = event.getClickedBlock().getLocation();
                        player.sendMessage(ChatColor.GREEN + "Vous avez défini le point 2 de la séléction.");
                        player.sendMessage(ChatColor.GRAY + "Tapez " + ChatColor.GOLD + "/nail setcuboid" + ChatColor.GRAY + " pour valider votre séléction.");
                    }
                } else if (plugin.cuboid != null && item.getType() == Material.INK_SACK && item.hasItemMeta()) {
                    final ChatColor color = ChatColor.getByChar(item.getItemMeta().getDisplayName().substring(1, 2));
                    if (color == ChatColor.BLUE || color == ChatColor.RED) {
                        for (final Team team : Team.getTeams()) {
                            if (item.isSimilar(team.getIcon())) {
                                final String displayName = team.getDisplayName();
                                final Team playerTeam = Team.getPlayerTeam(player);
                                if (playerTeam != team) {
                                    if (Bukkit.getOnlinePlayers().length > 1 && team.getScoreboardTeam().getSize() >= Bukkit.getOnlinePlayers().length / 2) {
                                        player.sendMessage(NailPlugin.prefix + ChatColor.GRAY + "Impossible de rejoindre cette équipe, trop de joueurs !");
                                    } else {
                                        if (playerTeam != null) {
                                            playerTeam.getScoreboardTeam().removePlayer(player);
                                        }
                                        team.getScoreboardTeam().addPlayer(player);
                                        player.sendMessage(NailPlugin.prefix + ChatColor.GRAY + "Vous rejoignez l'équipe des " + team.getColor() + displayName);
                                    }
                                }
                                break;
                            }
                        }
                        player.updateInventory();
                        return;
                    }
                    player.updateInventory();
                    event.setCancelled(true);
                    return;
                }
            }
            if (!player.isOp()) {
                event.setCancelled(true);
                return;
            }
        }
    }
}
