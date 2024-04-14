package fr.armotik.naurelliaminigames.games;

public enum Games {
    SPLEEF("Spleef", 2, 20),
    THREAD_THE_NEEDLE("ThreadTheNeedle", 1, 15),
    ANVIL_FALL("AnvilFall", 1, 30),
    WEREWOLF("Werewolf", 5, 15),
    BUILD_BATTLE("BuildBattle", 2, 20),
    JUMP_LEAGUE("JumpLeague", 2, 20),
    CTF("CaptureTheFlag", 2, 20),
    TOWER_DEFENSE("TowerDefense", 2, 20),
    SPLATOON("Splatoon", 2, 20),
    PVPBOX("PvpBox", 2, 20),
    ZOMBIE_INVASION("ZombieInvasion", 1, 20),
    PARKOUR("Parkour", 1, null),
    JUMP("Jump", 1, null),
    ;

    private final String name;
    private final Integer minPlayers;
    private final Integer maxPlayers;

    Games(String name, Integer minPlayers, Integer maxPlayers) {
        this.name = name;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public String getName() {
        return name;
    }

    public Integer getMinPlayers() {
        return minPlayers;
    }

    public Integer getMaxPlayers() {
        return maxPlayers;
    }
}
