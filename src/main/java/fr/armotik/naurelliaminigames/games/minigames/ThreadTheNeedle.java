package fr.armotik.naurelliaminigames.games.minigames;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.NaurelliaMiniGames;
import fr.armotik.naurelliaminigames.games.GameQueue;
import fr.armotik.naurelliaminigames.games.GameState;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.listeners.ThreadTheNeedleListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

public class ThreadTheNeedle extends MiniGame {

    public static final int START_X = -10;
    public static final int START_Y = 64;
    public static final int START_Z = -56;
    public static final int SIZE_X = 21;
    public static final int SIZE_Z = 21;

    private static final int SPAWN_X = 0;
    private static final int SPAWN_Y = 106;
    private static final int SPAWN_Z = -29;

    private ThreadTheNeedleListener threadTheNeedleListener;
    private int playerCount = 0;

    public ThreadTheNeedle(Player starter) {
        super(Games.THREAD_THE_NEEDLE, starter);
    }

    /**
     * Initialize the game
     */
    @Override
    public void init() {

        state = GameState.WAITING;

        getStarter().sendMessage(Louise.PREFIX + "§aStarting ThreadTheNeedle game...");

        generateThreadTheNeedleArena();

        setGameQueue(new GameQueue(this));

        Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Thread The Needle game is starting ! \n§c>> Use §6/join ttn §cto join the game !");
    }

    /**
     * Start the game
     */
    @Override
    public void start() {

        if (gameQueue.getPlayers().size() < getGame().getMinPlayers()) {
            getStarter().sendMessage(Louise.PREFIX + "§cNot enough players to start the game !");
            return;
        }

        players = gameQueue.getPlayers();
        playerCount = players.size();

        ThreadTheNeedleListener threadTheNeedleListener = new ThreadTheNeedleListener();
        Bukkit.getPluginManager().registerEvents(threadTheNeedleListener, NaurelliaMiniGames.getPlugin());

        final int[] taskId = {-1, -1};

        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(NaurelliaMiniGames.getPlugin(), new Runnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown > 0) {

                    for (Player p : players) {
                        p.sendMessage(Louise.PREFIX + "§aTeleportation to Thread The Needle Game in §b" + countdown + " seconds !");
                    }

                    countdown--;
                } else {
                    Bukkit.getScheduler().cancelTask(taskId[0]);
                }
            }
        }, 0L, 20L);

        Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

            teleportPlayers();

            // Start another chat timer that sends a message every second for 5 seconds
            taskId[1] = Bukkit.getScheduler().scheduleSyncRepeatingTask(NaurelliaMiniGames.getPlugin(), new Runnable() {
                int countdown = 5;

                @Override
                public void run() {
                    if (countdown > 0) {
                        Bukkit.broadcastMessage(Louise.PREFIX + "§cThread The Needle Game starting in §a" + countdown + " seconds !");
                        countdown--;
                    } else {
                        Bukkit.getScheduler().cancelTask(taskId[1]);
                    }
                }
            }, 0L, 20L);

            // Wait for another 5 seconds before starting the game
            Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                state = GameState.RUNNING;
                Bukkit.broadcastMessage(Louise.PREFIX + "§cThread The Needle Game started !");
            }, 20L * 5);
        }, 20L * 5);
    }

    /**
     * Stop the game
     * Clean up the game
     */
    @Override
    public void stop() {

        state = GameState.FINISHED;

        for (Player player : players) {
            player.getInventory().clear();

            assert WORLD != null;
            player.teleport(WORLD.getSpawnLocation());
        }

        Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Thread The Needle game is finished !");

        if (winner != null) {
            Bukkit.broadcastMessage(Louise.PREFIX + "§cThe winner of the Thread The Needle game is §a" + winner.getName() + " !");
        }

        players.clear();
        HandlerList.unregisterAll(threadTheNeedleListener);

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
     * Generate the ThreadTheNeedle arena with a water pool
     */
    public void generateThreadTheNeedleArena() {
        for (int x = START_X; x < START_X + SIZE_X; x++) {
            for (int z = START_Z; z < START_Z + SIZE_Z; z++) {

                Location location = new Location(ThreadTheNeedle.WORLD, x, ThreadTheNeedle.START_Y, z);
                location.getBlock().setType(Material.WATER);
            }
        }
    }

    /**
     * Teleport the players to the ThreadTheNeedle arena
     */
    private void teleportPlayers() {
        for (Player player : getGameQueue().getPlayers()) {
            // Teleport the player to the ThreadTheNeedle arena with north orientation
            player.teleport(new Location(ThreadTheNeedle.WORLD, SPAWN_X, SPAWN_Y, SPAWN_Z, 180, 0));
        }
    }

    /**
     * Teleport a player to the ThreadTheNeedle arena
     *
     * @param player the player
     */
    public void teleportPlayer(Player player) {
        player.teleport(new Location(ThreadTheNeedle.WORLD, SPAWN_X, SPAWN_Y, SPAWN_Z, 180, 0));
    }

    /**
     * Get the player count
     *
     * @return the player count
     */
    public int getPlayerCount() {
        return playerCount;
    }
}
