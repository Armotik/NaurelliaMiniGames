package fr.armotik.naurelliaminigames.listeners;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.games.GameState;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.AnvilFall;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AnvilFallListener implements Listener {

    AnvilFall anvilFall = (AnvilFall) MiniGame.getMiniGameByGameType(Games.ANVIL_FALL);

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (anvilFall == null) {
            return;
        }

        Player player = (Player) event.getEntity();

        if (!anvilFall.getPlayers().contains(player)) {
            return;
        }

        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }

        if (player.getHealth() - event.getFinalDamage() <= 0) {

            if (anvilFall.getPlayerCount() == 1) {

                anvilFall.win(player);
                anvilFall.stop();

            } else {

                anvilFall.getPlayers().remove(player);
                player.sendMessage(Louise.PREFIX + "You have been eliminated !");
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (anvilFall == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!anvilFall.getPlayers().contains(player)) {
            return;
        }

        anvilFall.getPlayers().remove(player);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {

        if (anvilFall == null) {
            return;
        }

        Player player = event.getPlayer();

        if (!anvilFall.getPlayers().contains(player)) {
            return;
        }

        if (anvilFall.getState() != GameState.RUNNING) {
            return;
        }

        if (anvilFall.getPlayerCount() > 1 && anvilFall.getPlayers().size() == 1) {
            anvilFall.win(anvilFall.getPlayers().get(0));
        }
    }
}
