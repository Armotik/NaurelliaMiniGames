package fr.armotik.naurelliaminigames.listeners;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.games.GameState;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import fr.armotik.naurelliaminigames.games.minigames.ThreadTheNeedle;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public class ThreadTheNeedleListener implements Listener {

    ThreadTheNeedle threadTheNeedle = (ThreadTheNeedle) MiniGame.getMiniGameByGameType(Games.THREAD_THE_NEEDLE);

    List<Material> woolColors = Arrays.asList(
            Material.RED_WOOL,
            Material.ORANGE_WOOL,
            Material.YELLOW_WOOL,
            Material.LIME_WOOL,
            Material.GREEN_WOOL,
            Material.LIGHT_BLUE_WOOL,
            Material.BLUE_WOOL,
            Material.PURPLE_WOOL,
            Material.MAGENTA_WOOL,
            Material.PINK_WOOL,
            Material.BROWN_WOOL,
            Material.BLACK_WOOL,
            Material.GRAY_WOOL,
            Material.LIGHT_GRAY_WOOL,
            Material.CYAN_WOOL,
            Material.WHITE_WOOL
    );

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {

        if (threadTheNeedle == null) {
            return;
        }

        Player player = event.getPlayer();

        if (threadTheNeedle.getState() != GameState.RUNNING) {
            return;
        }

        if (threadTheNeedle.getPlayers().contains(player)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (threadTheNeedle == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!threadTheNeedle.getPlayers().contains(player)) {
            return;
        }

        if (threadTheNeedle.getState() != GameState.RUNNING) {
            return;
        }

        if (threadTheNeedle.getPlayerCount() > 1 && threadTheNeedle.getPlayers().size() == 1) {
            threadTheNeedle.win(threadTheNeedle.getPlayers().get(0));
            return;
        }

        if (event.getTo() == null) {
            return;
        }

        Block blockY = Objects.requireNonNull(event.getTo().getWorld()).getBlockAt(event.getTo().getBlockX(), event.getTo().getBlockY(), event.getTo().getBlockZ());
        Block blockYMinusOne = Objects.requireNonNull(event.getTo().getWorld()).getBlockAt(event.getTo().getBlockX(), event.getTo().getBlockY() - 1, event.getTo().getBlockZ());

        if (blockYMinusOne.getType() == Material.AIR) {
            return;
        }

        if (blockY.getType() == Material.WATER) {

            player.setFallDistance(0);

            Material randomWoolColor = woolColors.get(new Random().nextInt(woolColors.size()));

            threadTheNeedle.teleportPlayer(player);

            blockY.setType(randomWoolColor);
            return;
        }

        if (woolColors.contains(blockYMinusOne.getType())) {

            player.setHealth(0D);

            if (threadTheNeedle.getPlayerCount() == 1) {
                threadTheNeedle.win(threadTheNeedle.getPlayers().get(0));
                return;
            }

            threadTheNeedle.getPlayers().remove(player);
            player.sendMessage(Louise.PREFIX + "§cYou lost !");

            player.teleport(Objects.requireNonNull(ThreadTheNeedle.WORLD).getSpawnLocation());
        }
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (threadTheNeedle == null) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!threadTheNeedle.getPlayers().contains(player)) {
            return;
        }

        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (threadTheNeedle == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!threadTheNeedle.getPlayers().contains(player)) {
            return;
        }

        threadTheNeedle.getPlayers().remove(player);
    }
}
