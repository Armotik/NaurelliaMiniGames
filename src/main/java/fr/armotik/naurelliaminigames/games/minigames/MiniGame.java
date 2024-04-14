package fr.armotik.naurelliaminigames.games.minigames;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.games.GameQueue;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.GameState;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class MiniGame {

    public static final World WORLD = Bukkit.getWorld("minigames");
    private static int id = 0;
    private static List<MiniGame> miniGames;
    private final Player starter;
    private final int gameID;
    protected Games game;
    protected GameState state;
    protected long startTime;
    protected Map<Player, Integer> scores;
    protected GameQueue gameQueue;
    protected List<Player> players;
    protected Player winner;

    public MiniGame(Games game, Player starter) {

        this.gameID = id++;
        this.game = game;
        this.state = GameState.WAITING;
        this.startTime = System.currentTimeMillis();
        this.scores = new HashMap<>();
        this.starter = starter;
        this.players = new ArrayList<>();
        this.winner = null;

        if (miniGames == null) {
            miniGames = new ArrayList<>();
        }

        if (getMiniGameByGameType(game) != null) {

            starter.sendMessage(Louise.PREFIX + "§cThis game is already running !");
            return;
        }

        miniGames.add(this);
    }

    /**
     * Get the game ID
     * @return the game ID
     */
    public int getGameID() {
        return gameID;
    }

    /**
     * Get the game type
     * @return the game type
     */
    public Games getGame() {
        return game;
    }

    /**
     * Get the game state
     * @return the game state
     */
    public GameState getState() {
        return state;
    }

    /**
     * Get the list of started games
     * @return the list of started games
     */
    public static List<MiniGame> getMiniGames() {
        return miniGames;
    }

    /**
     * Get a game by its game type
     * @param game the game type
     * @return the game
     */
    public static MiniGame getMiniGameByGameType(Games game) {

        if (miniGames == null) {
            return null;
        }

        for (MiniGame miniGame : miniGames) {
            if (miniGame.getGame() == game) {
                return miniGame;
            }
        }
        return null;
    }

    /**
     * Get the starter of the game
     * @return the starter of the game
     */
    public Player getStarter() {
        return starter;
    }

    /**
     * Check if a player can join the game
     * @param player the player
     * @return true if the player can join the game, false otherwise
     */
    public boolean canJoin(Player player) {

        return player.hasPermission("naurellia.staff") || state == GameState.WAITING;
    }

    /**
     * Get the queue of the game
     * @return the queue of the game
     */
    public GameQueue getGameQueue() {
        return gameQueue;
    }

    /**
     * Set the queue of the game
     * @param gameQueue the queue of the game
     */
    public void setGameQueue(GameQueue gameQueue) {
        this.gameQueue = gameQueue;
    }

    /**
     * Get the players in the game
     * @return the players in the game
     */
    public List<Player> getPlayers() {
        return players;
    }

    /**
     * Get the winner of the game
     * @return the winner of the game
     */
    public Player getWinner() {
        return winner;
    }

    /**
     * Set the winner of the game
     * @param winner the winner of the game
     */
    public void setWinner(Player winner) {
        this.winner = winner;
    }

    /**
     * Initialize the game
     */
    public abstract void init();

    /**
     * Start the game
     */
    public abstract void start();

    /**
     * Stop the game
     * Clean up the game
     */
    public abstract void stop();

    /**
     * Win the game
     * @param player the player who won
     */
    public abstract void win(Player player);
}
