package fr.armotik.naurelliaminigames.games.minigames.werewolf;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.NaurelliaMiniGames;
import fr.armotik.naurelliaminigames.games.GameQueue;
import fr.armotik.naurelliaminigames.games.GameState;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import fr.armotik.naurelliaminigames.listeners.WerewolfListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class Werewolf extends MiniGame {

    public static final List<String> WW_RULES = Arrays.asList(
            "Welcome to the Werewolf Game!",
            "Each night, the werewolves will choose a victim to eliminate.",
            "During the day, all players will discuss and vote on whom they suspect is a werewolf.",
            "The goal for the villagers is to eliminate all werewolves.",
            "The goal for the werewolves is to outnumber the villagers.",
            "Use your roles wisely and trust no one!",
            "The game will last until all werewolves are eliminated or until the werewolves outnumber the villagers.",
            "Reveal your role is not fun, like spoiling a movie. Keep it secret!",
            "Speak with dead players is not allowed. Dead players are dead. Do it in the afterlife.",
            "If you are a spectator, you can watch the game without participating. The goal is to have fun.",
            "Good luck and have fun!"
    );

    private Map<Player, WerewolfRoles> playersRoles;
    private Map<Player, Integer> playersVotes;
    private Map<Player, Integer> werewolvesVotes;
    private Map<Player, Boolean> trappedPlayers;

    private List<Player> killedLastNight;
    private List<Player> protectedLastNight;
    private List<Player> trappedLastNight;
    private List<Player> playersInBed;

    private int day;
    private int night;

    private boolean dayTime;
    private boolean votingTime;
    private boolean sleepingTime;

    private Player gameMaster;

    WerewolfListener werewolfListener;

    public Werewolf(Player starter) {
        super(Games.WEREWOLF, starter);

        gameMaster = starter;
    }

    /**
     * Initialize the game
     */
    @Override
    public void init() {

        state = GameState.WAITING;

        getStarter().sendMessage(Louise.PREFIX + "§aStarting Werewolf game...");

        setGameQueue(new GameQueue(this));

        playersRoles = new HashMap<>();
        playersVotes = new HashMap<>();
        werewolvesVotes = new HashMap<>();
        trappedPlayers = new HashMap<>();

        killedLastNight = new ArrayList<>();
        protectedLastNight = new ArrayList<>();
        trappedLastNight = new ArrayList<>();
        playersInBed = new ArrayList<>();

        day = 0;
        night = 0;
        dayTime = true; // true = day, false = night
        votingTime = false; // true = voting, false = not voting
        sleepingTime = false; // true = sleeping, false = not sleeping

        Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Werewolf game is starting ! \n§cUse §a/join werewolf §cto join the game !");
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

        werewolfListener = new WerewolfListener();
        Bukkit.getPluginManager().registerEvents(werewolfListener, NaurelliaMiniGames.getPlugin());

        final int[] taskId = {-1, -1};

        taskId[0] = Bukkit.getScheduler().scheduleSyncRepeatingTask(NaurelliaMiniGames.getPlugin(), new Runnable() {
            int countdown = 5;

            @Override
            public void run() {
                if (countdown > 0) {

                    for (Player p : players) {
                        p.sendMessage(Louise.PREFIX + "§aTeleportation to Werewolf Game in §b" + countdown + " seconds !");
                    }

                    countdown--;
                } else {
                    Bukkit.getScheduler().cancelTask(taskId[0]);

                    teleportPlayers();

                    assignRoles();

                    // Wait 1min before starting the game
                    taskId[1] = Bukkit.getScheduler().scheduleSyncRepeatingTask(NaurelliaMiniGames.getPlugin(), new Runnable() {
                        int countdown = 60;

                        @Override
                        public void run() {

                            for (Player p : players) {
                                p.sendMessage(Louise.PREFIX + "§aStarting Werewolf Game in §b" + countdown + " seconds !");
                            }

                            if (countdown > 0) {

                                countdown--;

                                if (countdown == 30) {
                                    Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Werewolf Game is starting in 30 seconds !");
                                }

                                if (countdown == 10) {
                                    Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Werewolf Game is starting in 10 seconds !");
                                }

                                if (countdown == 5) {
                                    Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Werewolf Game is starting in 5 seconds !");
                                }

                            } else {
                                Bukkit.getScheduler().cancelTask(taskId[1]);

                                Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Werewolf Game is starting !");

                                dayLogic();
                            }
                        }
                    }, 0L, 20L);
                }
            }
        }, 0L, 20L);
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

        Bukkit.broadcastMessage(Louise.PREFIX + "§cThe Werewolf Game is finished !");

        if (winner != null) {
            Bukkit.broadcastMessage(Louise.PREFIX + "§cThe winner team of the Werewolf Game is §a" + winner + " !");
        }

        players.clear();
        playersRoles.clear();
        playersVotes.clear();
        werewolvesVotes.clear();
        trappedPlayers.clear();
        killedLastNight.clear();
        protectedLastNight.clear();
        trappedLastNight.clear();
        playersInBed.clear();

        day = 0;
        night = 0;
        dayTime = true;
        votingTime = false;
        sleepingTime = false;

        gameMaster = null;

        HandlerList.unregisterAll(werewolfListener);
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

    }

    /**
     * Get the roles of the players
     *
     * @return the roles of the players
     */
    public Map<Player, WerewolfRoles> getPlayersRoles() {
        return playersRoles;
    }

    /**
     * Get the day number
     *
     * @return the day number
     */
    public int getDay() {
        return day;
    }

    /**
     * Get the night number
     *
     * @return the night number
     */
    public int getNight() {
        return night;
    }

    /**
     * Teleport players to the game arena
     */
    private void teleportPlayers() {
        // Teleport players to the game arena
    }

    /**
     * Get the game master
     *
     * @return the game master
     */
    public Player getGameMaster() {
        return gameMaster;
    }

    /**
     * Set the game master
     *
     * @param gameMaster the game master
     */
    public void setGameMaster(Player gameMaster) {
        this.gameMaster = gameMaster;
    }

    /**
     * Get the voting time
     *
     * @return true if it is voting time, false otherwise
     */
    public boolean isVotingTime() {
        return votingTime;
    }

    /**
     * Get the sleeping time
     *
     * @return true if it is sleeping time, false otherwise
     */
    public boolean isSleepingTime() {
        return sleepingTime;
    }

    /**
     * Get the players in bed
     *
     * @return the players in bed
     */
    public List<Player> getPlayersInBed() {
        return playersInBed;
    }

    public Map<Player, Boolean> getTrappedPlayers() {
        return trappedPlayers;
    }

    /**
     * Get the day time
     *
     * @return true if it is day time, false otherwise
     */
    public boolean isDayTime() {
        return dayTime;
    }

    //
    // Game logic
    //

    /**
     * Assign a random role to each player, depending on the number of players in the game
     */
    private void assignRoles() {

        Collections.shuffle(gameQueue.getPlayers());

        int numWerewolves = Math.max(1, players.size() / 5);
        int numSpecialRoles = Math.min(5, players.size() / 2);

        for (int i = 0; i < numWerewolves; i++) {
            playersRoles.put(players.get(i), WerewolfRoles.WEREWOLF);
        }

        List<WerewolfRoles> specialRoles = new ArrayList<>(List.of(
                WerewolfRoles.SEER, WerewolfRoles.WITCH, WerewolfRoles.HUNTER, WerewolfRoles.CUPID, WerewolfRoles.SAVIOR,
                WerewolfRoles.ANCIENT, WerewolfRoles.VILLAGE_IDIOT, WerewolfRoles.BEAST_HUNTER
        ));
        Collections.shuffle(specialRoles);
        int index = numWerewolves;

        for (WerewolfRoles role : specialRoles) {
            if (index >= players.size()) break;
            if (index - numWerewolves < numSpecialRoles) {

                playersRoles.put(players.get(index), role);
                index++;
            }
        }

        while (index < players.size()) {
            playersRoles.put(players.get(index), WerewolfRoles.VILLAGER);
            index++;
        }
    }

    /**
     * Vote logic
     *
     * @param voter  the voter
     * @param target the target
     * @return true if the vote was successful, false otherwise
     */
    public boolean vote(Player voter, Player target) {

        if (dayTime) {

            if (playersVotes.containsKey(target)) {
                playersVotes.put(target, playersVotes.get(target) + 1);
            } else {
                playersVotes.put(target, 1);
            }
        } else {

            if (playersRoles.get(voter) == WerewolfRoles.WEREWOLF) {

                if (playersRoles.get(target) == WerewolfRoles.WEREWOLF) {
                    return false;
                }

                if (werewolvesVotes.containsKey(target)) {

                    werewolvesVotes.put(target, werewolvesVotes.get(target) + 1);
                } else {
                    werewolvesVotes.put(target, 1);
                }
            }
        }

        return true;
    }

    public void dayLogic() {

        day++;

        // Midday
        Objects.requireNonNull(Bukkit.getWorld("roleplay")).setTime(6000L);

        for (Player player : players) {
            player.sendMessage(Louise.PREFIX + "§eThe sun is shining, and the village is waking up... It's a new day !");
            player.sendMessage(Louise.PREFIX + "§eYou have 5 minutes to discuss and vote on who you think is a werewolf !");
            player.sendMessage(Louise.PREFIX + "§6/ww village vote <player>");
            player.sendMessage(Louise.PREFIX + "§7Day " + day);
        }

        // First day
        if (day == 1) {

            // Display the rules
            for (String rule : WW_RULES) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {
                    for (Player player : players) {
                        player.sendMessage(Louise.PREFIX + "§e" + rule);
                    }
                }, 60L);
            }

            // Assign the roles and display the description of the role to each player
            Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                    entry.getKey().sendMessage(Louise.PREFIX + "§aYou are a " + entry.getValue().getName() + " !");
                    entry.getKey().sendMessage(Louise.PREFIX + "§e" + entry.getValue().getDescription());
                }
            }, 60L);

            // Ask the players to go to sleep
            Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {
                for (Player player : players) {
                    player.sendMessage(Louise.PREFIX + "§aThe night is falling, and you feel tired... You go to sleep !");
                }
            }, 60L);
        }
    }

    public void nightLogic() {

        night++;

        Objects.requireNonNull(Bukkit.getWorld("roleplay")).setTime(18000L);

        // TODO : Change -> use roles instead of players
        playersRoles.forEach((player, role) -> {

            switch (role.getPriority()) {

                case 0:
                    // Cupid's logic
                    // If the player is a cupid, he can choose two players to be in love
                    // Only works on the first night

                    if (night != 1) {
                        break;
                    }

                    wakeUpPlayer(player);

                    // Ask the player to choose two players
                    player.sendMessage(Louise.PREFIX + "§aYou are the Cupid ! Choose two players to be in love !");
                    player.sendMessage(Louise.PREFIX + "§6/ww cupid love <player1> <player2>");

                    // 1m to choose two players
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                        int lovers = 0;

                        for (WerewolfRoles r : playersRoles.values()) {
                            if (r.getRole2() == WerewolfRoles.LOVER) {

                                lovers++;
                            }
                        }

                        if (lovers < 2) {
                            player.sendMessage(Louise.PREFIX + "§cYou didn't choose two players to be in love !");

                            // Choose two random players and make them lovers (using cupidLove method)
                            List<Player> playersList = new ArrayList<>(players);

                            Collections.shuffle(playersList);

                            Player player1 = null;
                            Player player2 = null;

                            for (Player p : playersList) {
                                if (player1 == null) {
                                    player1 = p;
                                } else if (player2 == null) {
                                    player2 = p;
                                }
                            }

                            cupidLove(player, player1, player2);
                        }

                        // Go back to sleep
                        if (!playersInBed.contains(player)) {
                            sleepPlayer(player);
                        }

                    }, TimeUnit.MINUTES.toMillis(1));

                    break;

                case 1:
                    // Seer's logic
                    // If the player is a seer, he can choose a player to discover his role

                    wakeUpPlayer(player);

                    // Ask the player to choose a player
                    player.sendMessage(Louise.PREFIX + "§aYou are the Seer ! Choose a player to discover his role !");
                    player.sendMessage(Louise.PREFIX + "§6/ww seer check <player>");

                    // 30s to choose a player
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                        // Go back to sleep
                        if (!playersInBed.contains(player)) {
                            sleepPlayer(player);
                        }

                    }, TimeUnit.SECONDS.toMillis(30));

                    break;

                case 2:
                    // Salvatore's logic
                    // If the player is a salvatore, he can protect a player from elimination
                    // The player cannot protect the same player two nights in a row
                    // The player cannot protect himself

                    wakeUpPlayer(player);

                    // Ask the player to choose a player
                    player.sendMessage(Louise.PREFIX + "§aYou are the Salvatore ! Choose a player to protect ! (You cannot protect yourself)");
                    player.sendMessage(Louise.PREFIX + "§6/ww salvatore protect <player>");

                    // 30s to choose a player
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                        // Go back to sleep
                        if (!playersInBed.contains(player)) {
                            sleepPlayer(player);
                        }

                    }, TimeUnit.SECONDS.toMillis(30));

                    break;

                case 3:
                    // Beast hunter's logic
                    // If the player is a beast hunter, he can place a trap on a player
                    // The player cannot place a trap on the same player two nights in a row
                    // The trap will become active the following night (active => will kill the weakest werewolf)

                    wakeUpPlayer(player);

                    // Ask the player to choose a player
                    if (trappedPlayers.isEmpty()) {

                        player.sendMessage(Louise.PREFIX + "§aYou are the Beast Hunter ! Choose a player to place a trap on !");
                        player.sendMessage(Louise.PREFIX + "§6/ww beasthunter settrap <player>");
                    } else {

                        player.sendMessage(Louise.PREFIX + "§aYou are the Beast Hunter ! Do nothing to activate the trap !");
                        player.sendMessage(Louise.PREFIX + "§6/ww pass night");

                        player.sendMessage(Louise.PREFIX + "§aYou can also move the trap to another player !");
                        player.sendMessage(Louise.PREFIX + "§6/ww beasthunter settrap <player>");
                    }

                    // 30s to choose a player
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                        // Go back to sleep
                        if (!playersInBed.contains(player)) {
                            sleepPlayer(player);
                        }

                    }, TimeUnit.SECONDS.toMillis(30));

                    break;

                case 4:
                    // Wolf seer's logic
                    // If the player is a wolf seer, he can choose a player to discover his role
                    // If he is the last werewolf, he loses his ability and becomes a regular werewolf

                    int werewolves = 0;

                    for (WerewolfRoles r : playersRoles.values()) {
                        if (r == WerewolfRoles.WOLF_SEER) {
                            werewolves++;
                        }
                    }

                    if (werewolves > 0 && role == WerewolfRoles.WOLF_SEER) {
                        // Lose the ability
                        playersRoles.put(player, WerewolfRoles.WEREWOLF);
                    } else if (role == WerewolfRoles.WOLF_SEER) {
                        // Same as seer

                        wakeUpPlayer(player);

                        // Ask the player to choose a player
                        player.sendMessage(Louise.PREFIX + "§aYou are the Wolf Seer ! Choose a player to discover his role !");
                        player.sendMessage(Louise.PREFIX + "§6/ww seer check <player>");

                        // 30s to choose a player
                        Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                            // Go back to sleep
                            if (!playersInBed.contains(player)) {
                                sleepPlayer(player);
                            }

                        }, TimeUnit.SECONDS.toMillis(30));
                    }

                    break;

                case 5:
                    // Werewolf's logic
                    // If the player is a werewolf, he can choose a player to eliminate (by voting)
                    // The werewolves can only eliminate one player per night

                    // TODO (wake up the werewolves and ask them to vote)

                    break;

                case 6:
                    // Witch's logic
                    // If the player is a witch, he can choose to save a player or kill a player
                    // The witch has two potions: a potion of life and a potion of death
                    // The potion of life can save a player from elimination
                    // The potion of death can eliminate a player
                    // The witch can only use one potion per night and have just one of each

                    // TODO (wake up the witch and ask her to save or kill a player)

                    break;

                default:
                    break;
            }
        });
    }

    /**
     * Wake up the player
     *
     * @param player the player to wake up
     */
    public void wakeUpPlayer(Player player) {
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        player.removePotionEffect(PotionEffectType.SLOW);

        playersInBed.remove(player);

        player.sendMessage(Louise.PREFIX + "§aYou're awake !");
    }

    /**
     * Sleep the player
     *
     * @param player the player to sleep
     */
    public void sleepPlayer(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 100, false, false));
        player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, Integer.MAX_VALUE, 100, false, false));

        player.teleport(Objects.requireNonNull(player.getRespawnLocation()));

        playersInBed.add(player);

        player.sendMessage(Louise.PREFIX + "§aYou're falling asleep ...");
    }

    /**
     * End the day
     */
    public void endDay() {

        for (Player player : players) {
            player.sendMessage(Louise.PREFIX + "§eThe night falls, and the village falls silent... Who will be the next victim of the werewolves tonight ?");
        }

        dayTime = false;
        sleepingTime = false;

        Objects.requireNonNull(Bukkit.getWorld("roleplay")).setTime(18000L);
    }

    /**
     * If player is a seer, check the role of the target
     *
     * @param seer   the seer
     * @param target the target
     * @return the role of the target
     */
    public WerewolfRoles seerCheck(Player seer, Player target) {

        if (playersRoles.get(seer) != WerewolfRoles.SEER || playersRoles.get(seer) != WerewolfRoles.WOLF_SEER) {
            return null;
        }

        if (!dayTime) {
            return null;
        }

        return playersRoles.get(target);
    }

    /**
     * Witch save logic
     *
     * @param witch  the witch
     * @param target the target
     * @return true if the save was successful, false otherwise
     */
    public boolean witchSave(Player witch, Player target) {

        if (playersRoles.get(witch) != WerewolfRoles.WITCH) {
            return false;
        }

        if (!dayTime) {
            return false;
        }

        return protectedLastNight.add(target);
    }

    /**
     * Witch kill logic
     *
     * @param witch  the witch
     * @param target the target
     * @return true if the kill was successful, false otherwise
     */
    public boolean witchKill(Player witch, Player target) {

        if (playersRoles.get(witch) != WerewolfRoles.WITCH) {
            return false;
        }

        if (!dayTime) {
            return false;
        }

        return killedLastNight.add(target);
    }

    /**
     * Cupid love logic
     *
     * @param cupid   the cupid
     * @param target1 the first target
     * @param target2 the second target
     */
    public void cupidLove(Player cupid, Player target1, Player target2) {

        if (playersRoles.get(cupid) != WerewolfRoles.CUPID) {
            return;
        }

        if (night != 1) {
            return;
        }

        playersRoles.get(target1).setRole2(WerewolfRoles.LOVER);
        playersRoles.get(target2).setRole2(WerewolfRoles.LOVER);

        target1.sendMessage(Louise.PREFIX + "§aYou are now in love with " + target2.getName() + " !");
        target1.sendMessage(Louise.PREFIX + "§aYour lover is a " + playersRoles.get(target2).getName() + " !");

        target2.sendMessage(Louise.PREFIX + "§aYou are now in love with " + target1.getName() + " !");
        target2.sendMessage(Louise.PREFIX + "§aYour lover is a " + playersRoles.get(target1).getName() + " !");

        cupid.sendMessage(Louise.PREFIX + "§aYou have successfully made " + target1.getName() + " and " + target2.getName() + " fall in love !");
    }

    /**
     * Salvatore protect logic
     *
     * @param salvatore the salvatore
     * @param target    the target to protect
     */
    public void salvatoreProtect(Player salvatore, Player target) {

        if (playersRoles.get(salvatore) != WerewolfRoles.SAVIOR) {
            return;
        }

        if (!dayTime) {
            return;
        }

        protectedLastNight.add(target);
    }

    /**
     * Beast hunter set trap logic
     *
     * @param beastHunter the beast hunter
     * @param target      the target to trap
     */
    public void beastHunterSetTrap(Player beastHunter, Player target) {
        if (playersRoles.get(beastHunter) != WerewolfRoles.BEAST_HUNTER) {
            return;
        }

        if (!dayTime) {
            return;
        }

        trappedPlayers.clear();
        trappedPlayers.put(target, false);
    }

    /**
     * Beast hunter activate trap logic
     */
    public void beastHunterActivateTrap() {

        if (!dayTime) {
            return;
        }

        trappedPlayers.forEach((player, active) -> {
            if (!active) {
                trappedPlayers.put(player, true);
            }
        });
    }

    /**
     * Beast hunter trigger trap logic
     *
     * @param beastHunter the beast hunter
     * @param target      the target to trigger the trap on
     */
    public void bestHunterTriggerTrap(Player beastHunter, Player target) {
        if (playersRoles.get(beastHunter) != WerewolfRoles.BEAST_HUNTER) {
            return;
        }

        if (!dayTime) {
            return;
        }

        for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {

            WerewolfRoles role = entry.getValue();

            if (role == WerewolfRoles.WEREWOLF) {
                if (trappedPlayers.containsKey(target) && trappedPlayers.get(target)) {
                    // Kill a random werewolf
                    killedLastNight.add(target);
                }
            }
        }
    }
}