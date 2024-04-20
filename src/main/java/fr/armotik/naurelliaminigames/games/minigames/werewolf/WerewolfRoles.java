package fr.armotik.naurelliaminigames.games.minigames.werewolf;

public enum WerewolfRoles {
    SPECTATOR("Spectator", "The spectator can watch the game without participating. The goal of the spectator is to have fun.", Team.SPECTATOR, null),
    LOVER("Lover", "The lover can only win if his lover wins. The goal of the lover is to eliminate all the werewolves.", Team.LOVER, null),

    WEREWOLF("Werewolf", "Each night, the werewolves choose a player to eliminate. During the day, all players discuss and vote to eliminate a player. The goal of the werewolves is to eliminate all the villagers.", Team.WEREWOLF, 4),
    VILLAGER("Villager", "You are a regular villager without any special abilities", Team.VILLAGE, null),
    SEER("Seer", "Each night, the seer can choose a player to discover his role.", Team.VILLAGE, 1),
    WITCH("Witch", "The witch has two potions: a potion of life and a potion of death. The potion of life can save a player from elimination, and the potion of death can eliminate a player. The goal of the witch is to eliminate all the werewolves.", Team.VILLAGE, 5),
    HUNTER("Hunter", "If the hunter is eliminated, he can eliminate another player. The goal of the hunter is to eliminate all the werewolves.", Team.VILLAGE, null),
    CUPID("Cupid", "The cupid can choose two players to be in love. If one of the lovers is eliminated, the other dies of a broken heart.", Team.VILLAGE, 0),
    SAVIOR("Salvatore", "Every night, the salvatore protects one person. This person is protected and cannot die during the night. The salvatore cannot protect the same person two nights in a row.", Team.VILLAGE, 2),
    ANCIENT("Ancient", "The ancient one has two lives against the night. When he should be killed by the werewolves, he loses one without being warned. In the morning, he wakes up with the others, but reveals his card (the second time he is attacked by werewolves, he dies normally). If the ancient is expelled from the village by the villagers' vote, he dies directly and all the villagers' roles lose their powers.", Team.VILLAGE, null),
    VILLAGE_IDIOT("Village idiot", "If designated by the village vote, he does not die, and only once in the game, but only loses his ability to vote (he can take part in debates).", Team.VILLAGE, null),
    BEAST_HUNTER("Beast Hunter", "At night you can place a trap on a player which will become active the following night. This player cannot be killed at night. If the player is attacked by a werewolf, the weakest werewolf will die.", Team.VILLAGE, 3),
    HEAD_HUNTER("Head Hunter", "At the start of the game you are assigned a target. Your goal is to get your target lynched before you die in order to win. If your target dies another way, you will win with the village but remain a headhunter", Team.SOLO, null),
    WOLF_SEER("Wolf Seer", "Each night you can select a player to discover his role. If you are the last werewolf, you lost you ability and become a regular werewolf", Team.WEREWOLF, 4),
    ;

    private final String name;
    private final String description;
    private Team team;
    private final Integer priority;
    private WerewolfRoles role2;

    WerewolfRoles(String name, String description, Team team, Integer priority) {
        this.name = name;
        this.description = description;
        this.team = team;
        this.priority = priority;
        this.role2 = null;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Team getTeam() {
        return team;
    }

    public void setTeam(Team team) {
        this.team = team;
    }

    public Integer getPriority() {
        return priority;
    }

    public WerewolfRoles getRole2() {
        return role2;
    }

    public void setRole2(WerewolfRoles role2) {
        this.role2 = role2;
    }
}
