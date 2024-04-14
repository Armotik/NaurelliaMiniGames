package fr.armotik.naurelliaminigames.listeners;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

public class EventManager implements Listener {

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
