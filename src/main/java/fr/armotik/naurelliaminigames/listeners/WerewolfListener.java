package fr.armotik.naurelliaminigames.listeners;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import fr.armotik.naurelliaminigames.games.minigames.werewolf.Team;
import fr.armotik.naurelliaminigames.games.minigames.werewolf.Werewolf;
import fr.armotik.naurelliaminigames.games.minigames.werewolf.WerewolfRoles;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class WerewolfListener implements Listener {

    Werewolf werewolf = (Werewolf) MiniGame.getMiniGameByGameType(Games.WEREWOLF);

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (werewolf == null) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!werewolf.getPlayers().contains(player)) {
            return;
        }

        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (werewolf == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!werewolf.getPlayers().contains(player)) {
            return;
        }

        werewolf.getPlayers().remove(player);
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (werewolf == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!werewolf.getPlayers().contains(player)) {
            return;
        }

        if (!werewolf.isSleepingTime()) {

            event.setCancelled(true);
            player.sendMessage(Louise.PREFIX + "§cYou can't sleep now.");
        }

        event.setCancelled(true);

        werewolf.getPlayersInBed().add(player);

        Block bedBlock = event.getBed();
        player.teleport(new Location(bedBlock.getWorld(), bedBlock.getX() + 0.5, bedBlock.getY(), bedBlock.getZ() + 0.5, 90.0F, 90.0F));

        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 100, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100, false, false));

        player.setRespawnLocation(bedBlock.getLocation());

        player.sendMessage(Louise.PREFIX + "§aYou're falling asleep ...");

        if (werewolf.getPlayersInBed().size() == werewolf.getPlayers().size()) {
            werewolf.endDay();
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (werewolf == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!werewolf.getPlayers().contains(player)) {
            return;
        }

        if (werewolf.getPlayersInBed().contains(player)) {
            event.setCancelled(true);
        }
    }


    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {

        if (werewolf == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!werewolf.getPlayers().contains(player)) {
            return;
        }

        werewolf.getPlayersInBed().remove(player);
    }

    @EventHandler
    public void onWerewolfChat(AsyncPlayerChatEvent event) {

        if (werewolf == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!werewolf.getPlayers().contains(player)) {
            return;
        }

        if (!werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.WEREWOLF) && !werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.WOLF_SEER)) {
            return;
        }

        if (werewolf.isDayTime()) {
            return;
        }

        event.setCancelled(true);

        List<Player> wereWolves = new ArrayList<>();
        List<Player> spectators = new ArrayList<>();

        werewolf.getPlayersRoles().forEach((p, role) -> {
            if (role.equals(WerewolfRoles.WEREWOLF) || role.equals(WerewolfRoles.WOLF_SEER)) {
                wereWolves.add(p);
            }
        });

        werewolf.getPlayersRoles().forEach((p, role) -> {
            if (role.equals(WerewolfRoles.SPECTATOR)) {
                spectators.add(p);
            }
        });

        wereWolves.forEach(p -> p.sendMessage("§8[§4Werewolf§8] §c" + player.getName() + "§7: " + event.getMessage()));
        spectators.forEach(p -> p.sendMessage("§8[§4Werewolf§8] §c" + player.getName() + "§7: " + event.getMessage()));
    }

    @EventHandler
    public void onSpecChat(AsyncPlayerChatEvent event) {
        if (werewolf == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!werewolf.getPlayers().contains(player)) {
            return;
        }

        if (!werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.SPECTATOR)) {
            return;
        }

        event.setCancelled(true);

        List<Player> spectators = new ArrayList<>();

        werewolf.getPlayersRoles().forEach((p, role) -> {
            if (role.equals(WerewolfRoles.SPECTATOR)) {
                spectators.add(p);
            }
        });

        spectators.forEach(p -> p.sendMessage("§8[§7Spectator§8] §c" + player.getName() + "§7: " + event.getMessage()));
    }

    @EventHandler
    public void onPlayerNightChat(AsyncPlayerChatEvent event) {

        Player player = event.getPlayer();

        if (werewolf == null) {
            return;
        }

        if (!werewolf.getPlayers().contains(player)) {
            return;
        }

        if (werewolf.getPlayersRoles().get(player).getTeam().equals(Team.VILLAGE) && !werewolf.isDayTime()) {
            event.setCancelled(true);
            player.sendMessage(Louise.PREFIX + "§cYou can't talk at night.");
        }
    }
}
