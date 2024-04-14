package fr.armotik.naurelliaminigames.games.minigames;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.*;
import fr.armotik.naurelliaminigames.games.GameQueue;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.GameState;
import fr.armotik.naurelliaminigames.listeners.SpleefListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class Spleef extends MiniGame {

    public static final int START_X = 37;
    public static final int START_Y = 64;
    public static final int START_Z = -15;
    public static final int SIZE_X = 30;
    public static final int SIZE_Z = 30;
    public static final int LEVELS = 5;

    private SpleefListener spleefListener;

    public Spleef(Player starter) {
        super(Games.SPLEEF, starter);
    }

    @Override
    public void init() {

        state = GameState.WAITING;

        getStarter().sendMessage(Louise.PREFIX + "§aStarting Spleef game...");

        generateSpleefArena();

        setGameQueue(new GameQueue(this));

        Bukkit.broadcastMessage(Louise.PREFIX + "§cThe spleef game is starting ! \n§6>> Use §b/join spleef §6to join the game !");
    }

    @Override
    public void start() {

        if (gameQueue.getPlayers().size() < getGame().getMinPlayers()) {
            getStarter().sendMessage(Louise.PREFIX + "§cNot enough players to start the game !");
            return;
        }

        players = gameQueue.getPlayers();

        spleefListener = new SpleefListener();
        Bukkit.getPluginManager().registerEvents(spleefListener, NaurelliaMiniGames.getPlugin());

        final int[] taskId = {-1, -1};

        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(NaurelliaMiniGames.getPlugin(), new Runnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown > 0) {

                    for (Player p : players) {
                        p.sendMessage(Louise.PREFIX + "§aTeleportation to Spleef Game in §b" + countdown + " seconds !");
                    }

                    countdown--;
                } else {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                }
            }
        }, 0L, 20L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

            teleportPlayers(players);
            generateItems(players);

            // Start another chat timer that sends a message every second for 5 seconds
            taskId[1] = Bukkit.getScheduler().scheduleSyncRepeatingTask(NaurelliaMiniGames.getPlugin(), new Runnable() {
                int countdown = 5;

                @Override
                public void run() {
                    if (countdown > 0) {
                        Bukkit.broadcastMessage(Louise.PREFIX + "§cSpleef Game starting in §a" + countdown + " seconds !");
                        countdown--;
                    } else {
                        Bukkit.getScheduler().cancelTask(taskId[1]);
                    }
                }
            }, 0L, 20L);

            // Wait for another 5 seconds before starting the game
            Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                state = GameState.RUNNING;
                Bukkit.broadcastMessage(Louise.PREFIX + "§cSpleef Game started !");
            }, 20L * 5);
        }, 20L * 5);
    }

    @Override
    public void stop() {

        state = GameState.FINISHED;

        for (Player player : players) {
            player.getInventory().clear();

            assert WORLD != null;
            player.teleport(WORLD.getSpawnLocation());
        }

        Bukkit.broadcastMessage(Louise.PREFIX + "§cThe spleef game is finished !");

        if (winner != null) {
            Bukkit.broadcastMessage(Louise.PREFIX + "§cThe winner of the spleef game is §a" + winner.getName() + " !");
        }

        players.clear();
        HandlerList.unregisterAll(spleefListener);

        getMiniGames().remove(this);

        gameQueue.destroy();
        gameQueue = null;
    }

    /**
     * Win the game
     *
     * @param player the player who won
     */
    @Override
    public void win(Player player) {

        winner = player;

        stop();
    }

    /**
     * Generate the spleef arena
     */
    private static void generateSpleefArena() {
        for (int level = 0; level < Spleef.LEVELS; level++) {
            for (int x = Spleef.START_X; x < Spleef.START_X + Spleef.SIZE_X; x++) {
                for (int z = Spleef.START_Z; z < Spleef.START_Z + Spleef.SIZE_Z; z++) {
                    Location location = new Location(Spleef.WORLD, x, Spleef.START_Y + level * 5, z);
                    location.getBlock().setType(Material.SNOW_BLOCK);
                }
            }
        }
    }

    /**
     * Generate a shovel for each player
     *
     * @param players the players to give the shovel
     */
    private void generateItems(List<Player> players) {

        // create a shovel
        ItemStack shovel = new ItemStack(Material.DIAMOND_SHOVEL, 1);
        ItemMeta shovelMeta = shovel.getItemMeta();

        assert shovelMeta != null;

        shovelMeta.setDisplayName("§6Spleef Shovel");

        shovelMeta.addEnchant(Enchantment.DIG_SPEED, 10, true);
        shovelMeta.setUnbreakable(true);

        shovel.setItemMeta(shovelMeta);

        for (Player player : players) {
            player.getInventory().clear();

            player.getInventory().addItem(shovel);
        }
    }

    /**
     * Teleport players to the center of the arena
     *
     * @param players the players to teleport
     */
    private void teleportPlayers(List<Player> players) {
        for (Player player : players) {
            player.teleport(new Location(Spleef.WORLD, Spleef.START_X + (double) Spleef.SIZE_X / 2, Spleef.START_Y + LEVELS * 5 + 3, Spleef.START_Z + (double) Spleef.SIZE_Z / 2));
        }
    }
}
