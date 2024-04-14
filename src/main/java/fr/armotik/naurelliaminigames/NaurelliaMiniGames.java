package fr.armotik.naurelliaminigames;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.commands.*;
import fr.armotik.naurelliaminigames.completers.CommandCompleter;
import fr.armotik.naurelliaminigames.completers.WerewolfCommandCompleter;
import fr.armotik.naurelliaminigames.listeners.EventManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class NaurelliaMiniGames extends JavaPlugin {

    private static NaurelliaMiniGames plugin;
    private static final Logger logger = Logger.getLogger(NaurelliaMiniGames.class.getName());
    private static Louise louiseAPI;

    public static NaurelliaMiniGames getPlugin() {

        if (plugin == null) {
            plugin = new NaurelliaMiniGames();
        }


        return plugin;
    }

    public static Louise getLouiseAPI() {
        return louiseAPI;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        logger.info("[NaurelliaMiniGames] -> NaurelliaMiniGames is loading ...");

        plugin = this;

        setupLouiseAPI();

        this.getConfig().options().copyDefaults(true);
        this.saveConfig();

        getServer().getPluginManager().registerEvents(new EventManager(), this);

        Objects.requireNonNull(getCommand("init")).setExecutor(new InitCommand());
        Objects.requireNonNull(getCommand("join")).setExecutor(new JoinCommand());
        Objects.requireNonNull(getCommand("leave")).setExecutor(new LeaveCommand());
        Objects.requireNonNull(getCommand("start")).setExecutor(new StartCommand());
        Objects.requireNonNull(getCommand("stop")).setExecutor(new StopCommand());
        Objects.requireNonNull(getCommand("werewolf")).setExecutor(new WerewolfCommand());

        Objects.requireNonNull(getCommand("init")).setTabCompleter(new CommandCompleter());
        Objects.requireNonNull(getCommand("join")).setTabCompleter(new CommandCompleter());
        Objects.requireNonNull(getCommand("leave")).setTabCompleter(new CommandCompleter());
        Objects.requireNonNull(getCommand("start")).setTabCompleter(new CommandCompleter());
        Objects.requireNonNull(getCommand("stop")).setTabCompleter(new CommandCompleter());
        Objects.requireNonNull(getCommand("werewolf")).setTabCompleter(new WerewolfCommandCompleter());

        logger.info("[NaurelliaMiniGames] -> NaurelliaMiniGames is loaded !");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void setupLouiseAPI() {
        Louise plugin = Louise.getInstance();

        if (plugin == null || !plugin.isEnabled()) {
            logger.log(Level.SEVERE, "[NaurelliaMiniGames] -> Louise not found !");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        louiseAPI = plugin;
        logger.info("[NaurelliaMiniGames] -> LouiseAPI is loaded !");
    }
}
