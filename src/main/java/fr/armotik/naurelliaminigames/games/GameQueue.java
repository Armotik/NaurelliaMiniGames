package fr.armotik.naurelliaminigames.games;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class GameQueue {

    private final MiniGame game;
    private final Integer minPlayers;
    private final Integer maxPlayers;
    private final List<Player> players;
    private static final List<GameQueue> gameQueues = new ArrayList<>();

    public GameQueue(MiniGame game) {
        this.game = game;
        this.maxPlayers = game.getGame().getMaxPlayers();
        this.minPlayers = game.getGame().getMinPlayers();

        gameQueues.add(this);
        players = new ArrayList<>();
    }

    /**
     * Add a player to the queue
     * @param player the player to add
     * @return true if the player has been added, false otherwise
     */
    public boolean addPlayer(Player player) {

        if (!game.canJoin(player)) {
            return false;
        }

        if (maxPlayers != null && players.size() < maxPlayers) {
            return players.add(player);
        } else if (maxPlayers == null) {
            return players.add(player);
        }

        return false;
    }

    /**
     * Remove a player from the queue
     * @param player the player to remove
     * @return true if the player has been removed, false otherwise
     */
    public boolean removePlayer(Player player) {
        return players.remove(player);
    }

    /**
     * Get the players in the queue
     * @return the players in the queue
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Get the game
     * @return the game
     */
    public MiniGame getGame() {
        return game;
    }

    /**
     * Get the maximum number of players
     * @return the maximum number of players
     */
    public Integer getMaxPlayers() {
        return maxPlayers;
    }

    /**
     * Get the minimum number of players
     * @return the minimum number of players
     */
    public Integer getMinPlayers() {
        return minPlayers;
    }

    /**
     * Get the queue by the game
     * @param game the game
     * @return the queue
     */
    public static GameQueue getGameQueueByGame(Games game) {

        if (game == null) {
            return null;
        }

        for (GameQueue gameQueue : gameQueues) {
            if (gameQueue.getGame().getGame().equals(game)) {
                return gameQueue;
            }
        }

        return null;
    }

    /**
     * Get the list of game queues
     * @return the list of game queues
     */
    public static List<GameQueue> getGameQueues() {
        return gameQueues;
    }

    /**
     * Destroy the queue
     */
    public void destroy() {
        gameQueues.remove(this);
        players.clear();
    }
}
