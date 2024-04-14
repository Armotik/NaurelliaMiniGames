package fr.armotik.naurelliaminigames.games.minigames;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.NaurelliaMiniGames;
import fr.armotik.naurelliaminigames.games.GameQueue;
import fr.armotik.naurelliaminigames.games.GameState;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.listeners.AnvilFallListener;
import fr.armotik.naurelliaminigames.listeners.ThreadTheNeedleListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;

import java.util.logging.Level;
import java.util.logging.Logger;

public class AnvilFall extends MiniGame{

    public static final int START_X = -57;
    public static final int START_Y = 64;
    public static final int START_Z = -10;
    public static final int SIZE_X = 21;
    public static final int SIZE_Z = 21;

    private int playerCount = 0;
    private AnvilFallListener anvilFallListener;

    public AnvilFall(Player starter) {
        super(Games.ANVIL_FALL, starter);
    }

    /**
     * Initialize the game
     */
    @Override
    public void init() {

        state = GameState.WAITING;

        clearArena();

        getStarter().sendMessage(Louise.PREFIX + "§aStarting AnvilFall game...");

        setGameQueue(new GameQueue(this));

        Bukkit.broadcastMessage(Louise.PREFIX + "§cThe AnvilFall game is starting ! \n§cUse §a/join anvil §cto join the game !");
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

        Bukkit.getScheduler().runTaskAsynchronously(NaurelliaMiniGames.getPlugin(), () -> {
            for (int i = 5; i > 0; i--) {
                final int secondsLeft = i;
                Bukkit.getScheduler().runTask(NaurelliaMiniGames.getPlugin(), () -> {
                    for (Player p : players) {
                        p.sendMessage(Louise.PREFIX + "§aTeleportation to Anvil Fall Game in §b" + secondsLeft + " seconds !");
                    }
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logger.getLogger(AnvilFall.class.getName()).log(Level.SEVERE, null, e);
                }
            }

            teleportPlayers();

            anvilFallListener = new AnvilFallListener();
            Bukkit.getPluginManager().registerEvents(anvilFallListener, NaurelliaMiniGames.getPlugin());

            for (int i = 5; i > 0; i--) {
                final int secondsLeft = i;
                Bukkit.getScheduler().runTask(NaurelliaMiniGames.getPlugin(), () -> {
                    Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Anvil Fall Game starting in §a" + secondsLeft + " seconds !");
                });
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Logger.getLogger(AnvilFall.class.getName()).log(Level.SEVERE, null, e);
                }
            }

            state = GameState.RUNNING;
            Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Anvil Fall Game started !");

            int[] percentages = {10, 20, 30, 40, 50, 60, 70, 80, 90, 95, 98, 99};
            boolean winnerFound = false;
            int index = 0;

            while (!winnerFound) {

                if (index >= percentages.length) {
                    index = 11;
                }

                int percentage = percentages[index];

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    Logger.getLogger(AnvilFall.class.getName()).log(Level.SEVERE, null, e);
                }

                Bukkit.getScheduler().runTask(NaurelliaMiniGames.getPlugin(), () -> {
                    Bukkit.broadcastMessage(Louise.PREFIX + "§cAnvils dropping in " + percentage + "% of the arena !");
                });

                Bukkit.getScheduler().runTask(NaurelliaMiniGames.getPlugin(), () -> {
                    dropAnvils(percentage);
                    clearArena();
                });

                if (percentage == 99) {
                    if (winner != null) {
                        winnerFound = true;
                    }
                }

                index++;
            }
        });
    }


    private void dropAnvils(int percentage) {
        int anvilCount = (int) ((percentage / 100.0) * SIZE_X * SIZE_Z);
        for (int i = 0; i < anvilCount; i++) {
            int x = START_X + (int) (Math.random() * SIZE_X);
            int z = START_Z + (int) (Math.random() * SIZE_Z);
            Location location = new Location(WORLD, x, START_Y + 25, z);
            location.getBlock().setType(Material.ANVIL);
        }
    }

    private void clearArena() {
        for (int x = START_X; x < START_X + SIZE_X; x++) {
            for (int z = START_Z; z < START_Z + SIZE_Z; z++) {
                Location location = new Location(WORLD, x, START_Y, z);
                location.getBlock().setType(Material.AIR);
            }
        }
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

        Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Anvil Fall Game is finished !");

        if (winner != null) {
            Bukkit.broadcastMessage(Louise.PREFIX + "§cThe winner of the Anvil Falle Game is §a" + winner.getName() + " !");
        }

        players.clear();
        HandlerList.unregisterAll(anvilFallListener);
        Bukkit.getScheduler().cancelTasks(NaurelliaMiniGames.getPlugin());

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
     * Teleport players to the center of the arena
     */
    private void teleportPlayers() {
        Bukkit.getScheduler().runTask(NaurelliaMiniGames.getPlugin(), () -> {
            for (Player player : players) {
                player.teleport(new Location(AnvilFall.WORLD, AnvilFall.START_X + (double) AnvilFall.SIZE_X / 2, AnvilFall.START_Y, AnvilFall.START_Z + (double) AnvilFall.SIZE_Z / 2));
            }
        });
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
