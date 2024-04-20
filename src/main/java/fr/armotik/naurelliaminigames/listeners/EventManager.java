package fr.armotik.naurelliaminigames.listeners;

import fr.armotik.louise.Louise;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;

public class EventManager implements Listener {

    List<Player> players = new ArrayList<>();

    @EventHandler
    public void onPlayerDamageByEntity(EntityDamageByEntityEvent event) {

        World world = event.getEntity().getWorld();

        if (world.getName().equals("world")) {
            return;
        }

        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            event.setCancelled(true);
        }
    }
}
