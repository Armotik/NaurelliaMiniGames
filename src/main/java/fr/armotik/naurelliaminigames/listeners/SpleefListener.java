package fr.armotik.naurelliaminigames.listeners;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.games.GameState;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import fr.armotik.naurelliaminigames.games.minigames.Spleef;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Objects;


public class SpleefListener implements Listener {

    Spleef spleef = (Spleef) MiniGame.getMiniGameByGameType(Games.SPLEEF);


    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        if (spleef == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!spleef.getPlayers().contains(player)) {
            event.setCancelled(true);
            return;
        }

        if (spleef.getState() != GameState.RUNNING) {
            player.sendMessage(Louise.PREFIX + "§cThe game is not started yet !");
            event.setCancelled(true);
            return;
        }

        if (event.getBlock().getType() != Material.SNOW_BLOCK) {
            event.setCancelled(true);
        }

        event.setDropItems(false);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (spleef == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!spleef.getPlayers().contains(player)) {
            return;
        }

        event.getPlayer().setFallDistance(0);
        event.getPlayer().setFoodLevel(20);
        event.getPlayer().setHealth(20D);

        if (spleef.getState() != GameState.RUNNING) {
            return;
        }

        if (spleef.getPlayers().size() == 1) {
            spleef.win(spleef.getPlayers().get(0));
            return;
        }

        if (event.getTo() == null) {
            return;
        }

        int toY = (int) event.getTo().getY();

        if (toY < Spleef.START_Y - 1) {

            spleef.getPlayers().remove(player);
            player.getInventory().clear();
            player.sendMessage(Louise.PREFIX + "§cYou are out of the game !");
            player.teleport(Objects.requireNonNull(Spleef.WORLD).getSpawnLocation());
        }
    }


    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (spleef == null) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!spleef.getPlayers().contains(player)) {
            return;
        }

        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (spleef == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!spleef.getPlayers().contains(player)) {
            return;
        }

        spleef.getPlayers().remove(player);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (spleef == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!spleef.getPlayers().contains(player)) {
            return;
        }

        event.setCancelled(true);
    }
}
