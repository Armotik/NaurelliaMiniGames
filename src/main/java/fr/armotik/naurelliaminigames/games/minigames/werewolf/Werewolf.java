package fr.armotik.naurelliaminigames.games.minigames.werewolf;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.NaurelliaMiniGames;
import fr.armotik.naurelliaminigames.games.GameQueue;
import fr.armotik.naurelliaminigames.games.GameState;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import fr.armotik.naurelliaminigames.listeners.WerewolfListener;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
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
    private Map<Player, Player> playersVotesTarget;
    private Map<Player, Integer> werewolvesVotes;
    private Map<Player, Boolean> trappedPlayers;

    private Map<WerewolfRoles, Integer> ancientLives;
    private Map<Boolean, Boolean> witchPotions; // key = life, value = death (true = available, false = not available)

    private List<Player> killedLastNight;
    private List<Player> protectedLastNight;
    private List<Player> trappedLastNight;
    private List<Player> playersInBed;

    private int day;
    private int night;

    private boolean dayTime; // true = day, false = night
    private boolean votingTime; // true = voting, false = not voting
    private boolean sleepingTime; // true = sleeping, false = not sleeping
    private boolean isVillageIdiotVoted;  // true = voted, false = not voted
    private boolean isHunterVoted; // true = voted, false = not voted

    private Player gameMaster;
    private Player headHunterTarget;

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
        playersVotesTarget = new HashMap<>();

        ancientLives = new HashMap<>();
        ancientLives.put(WerewolfRoles.ANCIENT, 2);

        witchPotions = new HashMap<>();
        witchPotions.put(true, true);

        killedLastNight = new ArrayList<>();
        protectedLastNight = new ArrayList<>();
        trappedLastNight = new ArrayList<>();
        playersInBed = new ArrayList<>();

        day = 0;
        night = 0;
        dayTime = true; // true = day, false = night
        votingTime = false; // true = voting, false = not voting
        sleepingTime = false; // true = sleeping, false = not sleeping
        isVillageIdiotVoted = false; // true = voted, false = not voted
        isHunterVoted = false; // true = voted, false = not voted

        werewolfListener = new WerewolfListener();

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
        playersVotesTarget.clear();

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

    /**
     * Get the witch potions (life and death)
     *
     * @return the witch potions
     */
    public Map<Boolean, Boolean> getWitchPotions() {
        return witchPotions;
    }

    /**
     * Get if the village idiot is voted
     *
     * @return true if the village idiot is voted, false otherwise
     */
    public boolean isVillageIdiotVoted() {
        return isVillageIdiotVoted;
    }

    /**
     * Set if the village idiot is voted
     *
     * @param villageIdiotVoted true if the village idiot is voted, false otherwise
     */
    public void setVillageIdiotVoted(boolean villageIdiotVoted) {
        isVillageIdiotVoted = villageIdiotVoted;
    }

    /**
     * Get the headhunter target
     *
     * @return the headhunter target
     */
    public Player getHeadHunterTarget() {
        return headHunterTarget;
    }

    /**
     * Set the headhunter target
     *
     * @param headHunterTarget the headhunter target
     */
    public void setHeadHunterTarget(Player headHunterTarget) {
        this.headHunterTarget = headHunterTarget;
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
     * Day logic
     * Each player can discuss and vote on whom they think is a werewolf
     * At the end of the day, the player with the most votes is eliminated
     * If there is a tie, no one is eliminated
     */
    public void dayLogic() {

        day++;

        playersVotes.clear();
        playersVotesTarget.clear();

        // Midday
        Objects.requireNonNull(Bukkit.getWorld("roleplay")).setTime(6000L);

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

            // 2m to discuss and choose a bed
            Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {
                for (Player player : players) {
                    player.sendMessage(Louise.PREFIX + "§eYou have 2 minutes to discuss and choose a bed !");
                }
            }, TimeUnit.MINUTES.toMillis(2));

            // Ask the players to go to sleep
            Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {
                for (Player player : players) {
                    player.sendMessage(Louise.PREFIX + "§aThe night is falling, and you feel tired... You go to sleep !");
                }

                if (playersInBed.size() < players.size()) {

                    // TODO: handle players who didn't go to bed
                }

            }, TimeUnit.MINUTES.toMillis(1));

        } else {

            for (Player player : players) {
                player.sendMessage(Louise.PREFIX + "§eThe sun is shining, and the village is waking up... It's a new day !");
                player.sendMessage(Louise.PREFIX + "§eYou have 5 minutes to discuss and vote on who you think is a werewolf !");
                player.sendMessage(Louise.PREFIX + "§6/ww village vote <player>");
                player.sendMessage(Louise.PREFIX + "§7Day " + day);
            }

            // Display who was eliminated last night
            if (!killedLastNight.isEmpty()) {
                for (Player eliminated : killedLastNight) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {
                        for (Player player : players) {

                            player.sendMessage(Louise.PREFIX + "§c" + eliminated.getName() + " was eliminated last night !");
                            player.sendMessage(Louise.PREFIX + "§c" + playersRoles.get(eliminated).getName() + " was a " + playersRoles.get(eliminated).getName() + " !");

                            eliminatePlayer(eliminated);

                            if (isLoverDead(player)) {

                                for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                                    if (entry.getValue().getRole2() == WerewolfRoles.LOVER && !entry.getKey().equals(eliminated)) {
                                        player.sendMessage(Louise.PREFIX + "§c" + entry.getKey().getName() + " died of a broken heart !");
                                        player.sendMessage(Louise.PREFIX + "§c" + entry.getKey().getName() + " was a " + entry.getValue().getName() + " !");

                                    }
                                }
                            }
                        }
                    }, 60L);
                }
            } else {
                for (Player player : players) {
                    player.sendMessage(Louise.PREFIX + "§aNo one was eliminated last night !");
                }
            }

            players.forEach(player -> {

                player.sendMessage(Louise.PREFIX + "§eYou have 5 minutes to discuss about who you think is a werewolf !");
                player.sendMessage(Louise.PREFIX + "At the end of the 5 minutes, you will have 1 minute to vote !");
                player.sendMessage(Louise.PREFIX + "You can also choose to pass the discussion and to vote directly !");
                player.sendMessage(Louise.PREFIX + "§6/ww pass discussion");
            });

            // 5m to discuss
            Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {
                for (Player player : players) {
                    player.sendMessage(Louise.PREFIX + "§eYou have 1 minute to vote !");
                    player.sendMessage(Louise.PREFIX + "§6/ww village vote <player>");
                    player.sendMessage(Louise.PREFIX + "§6/ww pass vote");
                }

                votingTime = true;
            }, TimeUnit.MINUTES.toMillis(5));

            // 1m to vote
            Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                handleVote();

                players.forEach(player -> {

                    player.sendMessage(Louise.PREFIX + "§aThe sun is setting, and you feel tired... You go to sleep !");
                    player.sendMessage(Louise.PREFIX + "§aYou have 1 minute to go to bed !");
                });

                sleepingTime = true;

            }, TimeUnit.MINUTES.toMillis(1));

            // 1m to go to bed
            Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                if (playersInBed.size() < players.size()) {

                    for (Player player : players) {
                        if (!playersInBed.contains(player)) {

                            sleepPlayer(player);
                        }
                    }
                }

            }, TimeUnit.MINUTES.toMillis(1));
        }

        endDay();
    }

    /**
     * Night logic
     * Each role has a priority, and each role has a specific action to do during the night
     */
    public void nightLogic() {

        night++;

        Objects.requireNonNull(Bukkit.getWorld("roleplay")).setTime(18000L);

        for (WerewolfRoles role : playersRoles.values()) {

            switch (role.getPriority()) {

                case 0:
                    // Cupid's logic
                    // If the player is a cupid, he can choose two players to be in love
                    // Only works on the first night

                    if (night != 1) {
                        break;
                    }

                    Player cupid = null;

                    for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                        if (entry.getValue() == WerewolfRoles.CUPID) {
                            cupid = entry.getKey();
                        }
                    }

                    if (cupid == null) {
                        break;
                    }

                    wakeUpPlayer(cupid);

                    // Ask the player to choose two players
                    cupid.sendMessage(Louise.PREFIX + "§aYou are the Cupid ! Choose two players to be in love !");
                    cupid.sendMessage(Louise.PREFIX + "§aYou have 1 minute to choose two players !");
                    cupid.sendMessage(Louise.PREFIX + "§6/ww cupid love <player1> <player2>");

                    // 1m to choose two players
                    Player finalCupid = cupid;
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                        int lovers = 0;

                        for (WerewolfRoles r : playersRoles.values()) {
                            if (r.getRole2() == WerewolfRoles.LOVER) {

                                lovers++;
                            }
                        }

                        if (lovers < 2) {
                            finalCupid.sendMessage(Louise.PREFIX + "§cYou didn't choose two players to be in love !");

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

                            cupidLove(finalCupid, player1, player2);
                        }

                        // Go back to sleep
                        if (!playersInBed.contains(finalCupid)) {
                            sleepPlayer(finalCupid);
                        }

                    }, TimeUnit.MINUTES.toMillis(1));

                    break;

                case 1:
                    // Seer's logic
                    // If the player is a seer, he can choose a player to discover his role

                    Player seer = null;

                    for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                        if (entry.getValue() == WerewolfRoles.SEER) {
                            seer = entry.getKey();
                        }
                    }

                    if (seer == null) {
                        break;
                    }

                    final Player finalSeer = seer;

                    wakeUpPlayer(seer);

                    // Ask the player to choose a player
                    seer.sendMessage(Louise.PREFIX + "§aYou are the Seer ! Choose a player to discover his role !");
                    seer.sendMessage(Louise.PREFIX + "§aYou have 30 seconds to choose a player !");
                    seer.sendMessage(Louise.PREFIX + "§6/ww seer check <player>");

                    // 30s to choose a player
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                        // Go back to sleep
                        if (!playersInBed.contains(finalSeer)) {
                            sleepPlayer(finalSeer);
                        }

                    }, TimeUnit.SECONDS.toMillis(30));

                    break;

                case 2:
                    // Salvatore's logic
                    // If the player is a salvatore, he can protect a player from elimination
                    // The player cannot protect the same player two nights in a row
                    // The player cannot protect himself

                    Player salvatore = null;

                    for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                        if (entry.getValue() == WerewolfRoles.SAVIOR) {
                            salvatore = entry.getKey();
                        }
                    }

                    if (salvatore == null) {
                        break;
                    }

                    final Player finalSalvatore = salvatore;

                    wakeUpPlayer(salvatore);

                    // Ask the player to choose a player
                    salvatore.sendMessage(Louise.PREFIX + "§aYou are the Salvatore ! Choose a player to protect ! (You cannot protect yourself)");
                    salvatore.sendMessage(Louise.PREFIX + "§aYou have 30 seconds to choose a player !");
                    salvatore.sendMessage(Louise.PREFIX + "§6/ww salvatore protect <player>");

                    // 30s to choose a player
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                        // Go back to sleep
                        if (!playersInBed.contains(finalSalvatore)) {
                            sleepPlayer(finalSalvatore);
                        }

                    }, TimeUnit.SECONDS.toMillis(30));

                    break;

                case 3:
                    // Beast hunter's logic
                    // If the player is a beast hunter, he can place a trap on a player
                    // The player cannot place a trap on the same player two nights in a row
                    // The trap will become active the following night (active => will kill the weakest werewolf)

                    Player beastHunter = null;

                    for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                        if (entry.getValue() == WerewolfRoles.BEAST_HUNTER) {
                            beastHunter = entry.getKey();
                        }
                    }

                    if (beastHunter == null) {
                        break;
                    }

                    final Player finalBeastHunter = beastHunter;

                    wakeUpPlayer(beastHunter);

                    // Ask the player to choose a player
                    if (trappedPlayers.isEmpty()) {

                        beastHunter.sendMessage(Louise.PREFIX + "§aYou are the Beast Hunter ! Choose a player to place a trap on !");
                        beastHunter.sendMessage(Louise.PREFIX + "§aYou have 30 seconds to choose a player !");
                        beastHunter.sendMessage(Louise.PREFIX + "§6/ww beasthunter settrap <player>");
                    } else {

                        beastHunter.sendMessage(Louise.PREFIX + "§aYou are the Beast Hunter ! Do nothing to activate the trap !");
                        beastHunter.sendMessage(Louise.PREFIX + "§6/ww pass night");

                        beastHunter.sendMessage(Louise.PREFIX + "§aYou can also move the trap to another player !");
                        beastHunter.sendMessage(Louise.PREFIX + "§aYou have 30 seconds to choose a player !");
                        beastHunter.sendMessage(Louise.PREFIX + "§6/ww beasthunter settrap <player>");
                    }

                    // 30s to choose a player
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                        // Go back to sleep
                        if (!playersInBed.contains(finalBeastHunter)) {
                            sleepPlayer(finalBeastHunter);
                        }

                    }, TimeUnit.SECONDS.toMillis(30));

                    break;

                case 4:
                    // Wolf seer's logic
                    // If the player is a wolf seer, he can choose a player to discover his role
                    // If he is the last werewolf, he loses his ability and becomes a regular werewolf

                    int werewolvesNb = 0;

                    for (WerewolfRoles r : playersRoles.values()) {
                        if (r == WerewolfRoles.WOLF_SEER) {
                            werewolvesNb++;
                        }
                    }

                    Player wolfSeer = null;

                    for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                        if (entry.getValue() == WerewolfRoles.WOLF_SEER) {
                            wolfSeer = entry.getKey();
                        }
                    }

                    if (wolfSeer == null) {
                        break;
                    }

                    final Player finalWolfSeer = wolfSeer;

                    if (werewolvesNb > 0 && role == WerewolfRoles.WOLF_SEER) {
                        // Lose the ability
                        playersRoles.put(wolfSeer, WerewolfRoles.WEREWOLF);
                    } else if (role == WerewolfRoles.WOLF_SEER) {
                        // Same as seer

                        wakeUpPlayer(wolfSeer);

                        // Ask the player to choose a player
                        wolfSeer.sendMessage(Louise.PREFIX + "§aYou are the Wolf Seer ! Choose a player to discover his role !");
                        wolfSeer.sendMessage(Louise.PREFIX + "§aYou have 30 seconds to choose a player !");
                        wolfSeer.sendMessage(Louise.PREFIX + "§6/ww seer check <player>");

                        // 30s to choose a player
                        Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                            // Go back to sleep
                            if (!playersInBed.contains(finalWolfSeer)) {
                                sleepPlayer(finalWolfSeer);
                            }

                        }, TimeUnit.SECONDS.toMillis(30));
                    }

                    break;

                case 5:
                    // Werewolf's logic
                    // If the player is a werewolf, he can choose a player to eliminate (by voting)
                    // The werewolves can only eliminate one player per night

                    List<Player> werewolves = new ArrayList<>();
                    werewolvesVotes.clear();

                    for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                        if (entry.getValue() == WerewolfRoles.WEREWOLF || entry.getValue() == WerewolfRoles.WOLF_SEER) {
                            werewolves.add(entry.getKey());
                        }
                    }

                    for (Player werewolf : werewolves) {

                        wakeUpPlayer(werewolf);

                        // Ask the player to choose a player
                        werewolf.sendMessage(Louise.PREFIX + "§aYou are a Werewolf ! Choose a player to eliminate !");
                        werewolf.sendMessage(Louise.PREFIX + "§aYou have 1 minute and 30 seconds to choose a player !");
                        werewolf.sendMessage(Louise.PREFIX + "§aWerewolves can speak with each other during the night ! No one else can hear you ! (Everyone is asleep)");
                        werewolf.sendMessage(Louise.PREFIX + "§aIf you don't want to eliminate anyone, you can pass the night !");
                        werewolf.sendMessage(Louise.PREFIX + "§6/ww werewolf kill <player>");
                        werewolf.sendMessage(Louise.PREFIX + "§6/ww pass night");

                        // 1m30 to choose a player
                        Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                            // Go back to sleep
                            if (!playersInBed.contains(werewolf)) {
                                sleepPlayer(werewolf);
                            }

                            handleWerewolfVote();

                        }, TimeUnit.MINUTES.toMillis(1) + TimeUnit.SECONDS.toMillis(30));
                    }

                    break;

                case 6:
                    // Witch's logic
                    // If the player is a witch, he can choose to save a player or kill a player
                    // The witch has two potions: a potion of life and a potion of death
                    // The potion of life can save a player from elimination
                    // The potion of death can eliminate a player
                    // The witch can only use one potion per night and have just one of each

                    Player witch = null;

                    for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                        if (entry.getValue() == WerewolfRoles.WITCH) {
                            witch = entry.getKey();
                        }
                    }

                    if (witch == null) {
                        break;
                    }

                    final Player finalWitch = witch;

                    wakeUpPlayer(witch);

                    // Ask the player to choose a player

                    // Life potion used
                    if (witchPotions.containsKey(false)) {

                        witch.sendMessage(Louise.PREFIX + "§aYou are the Witch ! You have already used your potion of life ! You can only use the potion of death !");
                        witch.sendMessage(Louise.PREFIX + "§aYou have 30s to choose a player !");
                        witch.sendMessage(Louise.PREFIX + "§6/ww witch kill <player>");

                        witch.sendMessage(Louise.PREFIX + "§aIf you don't want to use the potion of death, you can pass the night !");
                        witch.sendMessage(Louise.PREFIX + "§6/ww pass night");
                    }

                    // Death potion used
                    if (witchPotions.containsValue(false)) {

                        witch.sendMessage(Louise.PREFIX + "§aYou are the Witch ! You have already used your potion of death !");
                        witch.sendMessage(Louise.PREFIX + "§aYou have 30s to choose a player !");
                        witch.sendMessage(Louise.PREFIX + "§6/ww witch save <player>");

                        witch.sendMessage(Louise.PREFIX + "§aIf you don't want to use the potion of life, you can pass the night !");
                        witch.sendMessage(Louise.PREFIX + "§6/ww pass night");
                    }

                    // No potion used

                    if (witchPotions.containsValue(true) && witchPotions.containsKey(true)) {

                        witch.sendMessage(Louise.PREFIX + "§aYou are the Witch ! Choose a player to save or kill ! You can only use 1 potion per night");
                        witch.sendMessage(Louise.PREFIX + "§aYou have 30s to choose a player !");
                        witch.sendMessage(Louise.PREFIX + "§6/ww witch save <player>");
                        witch.sendMessage(Louise.PREFIX + "§6/ww witch kill <player>");

                        witch.sendMessage(Louise.PREFIX + "§aIf you don't want to use any potions, you can pass the night !");
                        witch.sendMessage(Louise.PREFIX + "§6/ww pass night");
                    }

                    // 30s to choose a player
                    Bukkit.getScheduler().scheduleSyncDelayedTask(NaurelliaMiniGames.getPlugin(), () -> {

                        // Go back to sleep
                        if (!playersInBed.contains(finalWitch)) {
                            sleepPlayer(finalWitch);
                        }

                    }, TimeUnit.SECONDS.toMillis(30));

                    break;

                default:
                    break;
            }
        }

        endNight();
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
     * End the night
     */
    public void endNight() {

        for (Player player : players) {

            wakeUpPlayer(player);

            player.sendMessage(Louise.PREFIX + "§eThe sun rises, and the village wakes up... Who were the victims of the werewolves last night ?");
        }

        dayTime = true;

        Objects.requireNonNull(Bukkit.getWorld("roleplay")).setTime(6000L);
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
     */
    public void witchSave(Player witch, Player target) {

        if (playersRoles.get(witch) != WerewolfRoles.WITCH) {
            return;
        }

        if (!dayTime) {
            return;
        }

        if (protectedLastNight.contains(target)) {
            return;
        }

        killedLastNight.remove(target);

        // Set the key to false (but don't change the value)
        witchPotions.put(false, witchPotions.get(false));
    }

    /**
     * Witch kill logic
     *
     * @param witch  the witch
     * @param target the target
     */
    public void witchKill(Player witch, Player target) {

        if (playersRoles.get(witch) != WerewolfRoles.WITCH) {
            return;
        }

        if (!dayTime) {
            return;
        }

        if (protectedLastNight.contains(target)) {
            return;
        }

        killedLastNight.add(target);

        // Set the value to false (but don't change the key)
        witchPotions.put(witchPotions.containsKey(true), false);
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

        if (salvatore == target) {
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
     * @param target the target who triggered the trap
     */
    public Player bestHunterTriggerTrap(Player target) {
        if (playersRoles.get(target).getTeam() != Team.WEREWOLF) {
            return null;
        }

        if (!dayTime) {
            return null;
        }

        // kill a random werewolf
        List<Player> werewolves = new ArrayList<>();

        for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
            if (entry.getValue().getTeam() == Team.WEREWOLF) {
                werewolves.add(entry.getKey());
            }
        }

        if (werewolves.isEmpty()) {
            return null;
        }

        Player randomWerewolf = werewolves.get(new Random().nextInt(werewolves.size()));

        trappedPlayers.clear();

        return randomWerewolf;
    }

    /**
     * Werewolf kill logic
     *
     * @param werewolf the werewolf
     * @param target   the target to kill
     */
    public void werewolfKill(Player werewolf, Player target) {

        if (playersRoles.get(werewolf) != WerewolfRoles.WEREWOLF) {
            return;
        }

        if (dayTime) {
            return;
        }

        if (werewolvesVotes.containsKey(target)) {
            werewolvesVotes.put(target, werewolvesVotes.get(target) + 1);
        } else {
            werewolvesVotes.put(target, 1);
        }
    }

    /**
     * Handle the werewolf vote
     * Check if the target is trapped
     * Check if the target is protected
     * Check if the target is a werewolf
     * Check if the target is the ancient
     * Check if the target is one of the lovers
     * Add the target to the killedLastNight list
     */
    public void handleWerewolfVote() {
        if (werewolvesVotes.isEmpty()) {
            return;
        }

        Player target = null;
        int maxVotes = 0;

        for (Map.Entry<Player, Integer> entry : werewolvesVotes.entrySet()) {
            if (entry.getValue() > maxVotes) {
                target = entry.getKey();
                maxVotes = entry.getValue();
            }
        }

        if (target == null) {
            return;
        }

        // Check if the target is trapped
        if (trappedLastNight.contains(target)) {
            target = bestHunterTriggerTrap(target);
        }

        // Check if the target is protected
        if (protectedLastNight.contains(target)) {
            return;
        }

        // Check if the target is a werewolf
        if (playersRoles.get(target).getTeam() == Team.WEREWOLF) {
            return;
        }

        // Check if the target is the ancient
        if (playersRoles.get(target) == WerewolfRoles.ANCIENT) {

            if (ancientLives.get(WerewolfRoles.ANCIENT) > 0) {
                ancientLives.put(WerewolfRoles.ANCIENT, ancientLives.get(WerewolfRoles.ANCIENT) - 1);
                return;
            }
        }

        killedLastNight.add(target);
    }

    /**
     * Vote for a player
     * Check if the player has already voted
     * (if yes decrement the old target's votes and increment the new target's votes)
     * Uses the playersVotes and playersVotesTarget maps
     *
     * @param player the player who votes
     * @param target the target of the vote
     */
    public void vote(Player player, Player target) {

        if (playersVotes.containsKey(player)) {
            playersVotes.put(player, playersVotes.get(player) - 1);
        }

        playersVotes.put(player, playersVotes.getOrDefault(player, 0) + 1);
        playersVotesTarget.put(player, target);
    }

    /**
     * Handle the vote
     * Check if the target is the headhunter target
     * Check if the target is one of the lovers
     * Check if the target is the village idiot
     * Check if the target is the ancient
     * Check if the target is the hunter
     * Eliminate the target
     */
    public void handleVote() {

        if (playersVotes.isEmpty() || playersVotesTarget.isEmpty()) {

            players.forEach(player -> {
                player.sendMessage(Louise.PREFIX + "§aNo one was eliminated !");
            });
            return;
        }

        Player target = null;
        int maxVotes = 0;

        // Get the player with the most votes
        // If there is a tie, the first player with the most votes is eliminated
        for (Map.Entry<Player, Integer> entry : playersVotes.entrySet()) {
            if (entry.getValue() > maxVotes) {
                target = playersVotesTarget.get(entry.getKey());
                maxVotes = entry.getValue();
            }

            if (entry.getValue() == maxVotes) {
                target = playersVotesTarget.get(entry.getKey());
            }
        }

        if (target == null) {

            players.forEach(player -> {
                player.sendMessage(Louise.PREFIX + "§aNo one was eliminated !");
            });
            return;
        }

        // Check if the target is the headhunter target
        // If the target is the headhunter target, the headhunter wins the game
        if (headHunterTarget != null && headHunterTarget == target) {

            Player finalTarget = target;
            players.forEach(player -> {
                player.sendMessage(Louise.PREFIX + "§c" + finalTarget.getName() + " was eliminated by the village !");
                player.sendMessage(Louise.PREFIX + "§c" + playersRoles.get(finalTarget).getName() + " was a " + playersRoles.get(finalTarget).getName() + " !");

                player.sendMessage(Louise.PREFIX + "§cUnfortunately, " + headHunterTarget.getName() + " was the target of the Head Hunter !");
                player.sendMessage(Louise.PREFIX + "§cThe Head Hunter wins the game !");
            });

            eliminatePlayer(target);

            Player headhunter = null;

            for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                if (entry.getValue() == WerewolfRoles.HEAD_HUNTER) {
                    headhunter = entry.getKey();
                }
            }

            winner = headhunter;
            stop(); // End the game

            return;
        }

        // Check if the target is one of the lovers
        // If the target is a lover, the other lover dies of a broken heart
        if (isLoverDead(target)) {

            for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {
                if (entry.getValue().getRole2() == WerewolfRoles.LOVER && !entry.getKey().equals(target)) {

                    Player finalTarget = target;
                    players.forEach(player -> {

                        player.sendMessage(Louise.PREFIX + "§c" + finalTarget.getName() + " was eliminated by the village !");
                        player.sendMessage(Louise.PREFIX + "§c" + playersRoles.get(finalTarget).getName() + " was a " + playersRoles.get(finalTarget).getName() + " !");

                        player.sendMessage(Louise.PREFIX + "§cUnfortunately, " + entry.getKey().getName() + " was in love with " + finalTarget.getName() + " !");

                        player.sendMessage(Louise.PREFIX + "§cSo " + entry.getKey().getName() + " died of a broken heart !");
                        player.sendMessage(Louise.PREFIX + "§c" + entry.getKey().getName() + " was a " + entry.getValue().getName() + " !");
                    });

                    eliminatePlayer(entry.getKey());
                    eliminatePlayer(target);
                    return;
                }
            }
        }

        // Check if the target is the village idiot
        // If the target is the village idiot, no one is eliminated
        // The village idiot loses his ability to vote
        if (playersRoles.get(target) == WerewolfRoles.VILLAGE_IDIOT) {

            isVillageIdiotVoted = true;

            Player finalTarget1 = target;
            players.forEach(player -> {
                player.sendMessage(Louise.PREFIX + finalTarget1.getName() + " is the Village Idiot !");
                player.sendMessage(Louise.PREFIX + "§aNo one was eliminated !");
            });

            return;
        }

        // Check if the target is the ancient
        // If the target is the ancient, the village loses all its roles and becomes villagers
        if (playersRoles.get(target) == WerewolfRoles.ANCIENT) {

            Player finalTarget = target;
            players.forEach(player -> {
                player.sendMessage(Louise.PREFIX + "§c" + finalTarget.getName() + " was eliminated by the village !");
                player.sendMessage(Louise.PREFIX + "§c" + playersRoles.get(finalTarget).getName() + " was a " + playersRoles.get(finalTarget).getName() + " !");

                player.sendMessage(Louise.PREFIX + "§cThe village has decided to eliminate the Ancient !");
                player.sendMessage(Louise.PREFIX + "§cFeeling betrayed, the Ancient curses the village ! The village loses all its roles and becomes villagers !");
            });

            handleAncientDeathByVillage();
            eliminatePlayer(target);

            return;
        }

        // Check if the target is the hunter
        // If the target is the hunter, he can eliminate another player
        if (playersRoles.get(target) == WerewolfRoles.HUNTER) {

            isHunterVoted = true;

            Player finalTarget = target;
            players.forEach(player -> {
                player.sendMessage(Louise.PREFIX + finalTarget.getName() + " was eliminated by the village !");
                player.sendMessage(Louise.PREFIX + playersRoles.get(finalTarget).getName() + " was a " + playersRoles.get(finalTarget).getName() + " !");
            });

            target.sendMessage(Louise.PREFIX + "§aYou are the Hunter ! Choose a player to eliminate !");
            target.sendMessage(Louise.PREFIX + "§6/ww hunter kill <player>");

            eliminatePlayer(target);
            return;
        }

        Player finalTarget2 = target;
        players.forEach(player -> {
            player.sendMessage(Louise.PREFIX + "§c" + finalTarget2.getName() + " was eliminated by the village !");
            player.sendMessage(Louise.PREFIX + "§c" + playersRoles.get(finalTarget2).getName() + " was a " + playersRoles.get(finalTarget2).getName() + " !");
        });

        eliminatePlayer(target);
    }

    /**
     * Check if the lover is dead
     *
     * @param target the target to check
     * @return true if the lover is dead, false otherwise
     */
    public boolean isLoverDead(Player target) {

        return playersRoles.get(target).getRole2() == WerewolfRoles.LOVER;
    }

    /**
     * Eliminate the player
     * Set the player to spectator
     * Set the player's role to spectator
     *
     * @param target the target to eliminate
     */
    public void eliminatePlayer(Player target) {

        if (isSpectator(target)) {
            return;
        }

        target.sendMessage(Louise.PREFIX + "§cYou have been eliminated !");

        target.setGameMode(GameMode.SPECTATOR);

        playersRoles.get(target).setRole2(WerewolfRoles.SPECTATOR);
    }

    /**
     * Handle the ancient death by the village
     * If the ancient is voted, all the village loses their role and become villagers
     * Only works if the ancient is voted
     * The werewolves don't lose their role
     */
    public void handleAncientDeathByVillage() {

        for (Map.Entry<Player, WerewolfRoles> entry : playersRoles.entrySet()) {

            if (entry.getValue().getTeam().equals(Team.WEREWOLF)) {
                continue;
            }

            entry.setValue(WerewolfRoles.VILLAGER);
        }
    }

    /**
     * Check if the hunter is voted
     * @return true if the hunter is voted, false otherwise
     */
    public boolean isHunterVoted() {
        return isHunterVoted;
    }

    /**
     * Check if the player is a spectator
     * @param player the player to check
     * @return true if the player is a spectator, false otherwise
     */
    public boolean isSpectator(Player player) {
        return getPlayersRoles().get(player).getRole2().equals(WerewolfRoles.SPECTATOR);
    }
}