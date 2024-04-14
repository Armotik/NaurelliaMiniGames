package fr.armotik.naurelliaminigames.commands;

import fr.armotik.louise.Louise;
import fr.armotik.louise.louise.LouiseGlobal;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import fr.armotik.naurelliaminigames.games.GameState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Objects;

public class StartCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (strings.length == 0) {
            commandSender.sendMessage(LouiseGlobal.wrongCommand());
            return false;
        }

        if (!(commandSender instanceof Player)) {
            commandSender.sendMessage(Louise.PREFIX + "You must be a player to use this command !");
            return false;
        }

        String game = strings[0];

        switch (game) {
            case "spleef":

                MiniGame spleef = MiniGame.getMiniGameByGameType(Games.SPLEEF);

                if (spleef == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                if (!spleef.getState().equals(GameState.WAITING)) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is already started !");
                    return false;
                }

                Objects.requireNonNull(MiniGame.getMiniGameByGameType(Games.SPLEEF)).start();

                break;
            case "ttn":

                MiniGame ttf = MiniGame.getMiniGameByGameType(Games.THREAD_THE_NEEDLE);

                if (ttf == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                if (!ttf.getState().equals(GameState.WAITING)) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is already started !");
                    return false;
                }

                Objects.requireNonNull(MiniGame.getMiniGameByGameType(Games.THREAD_THE_NEEDLE)).start();

                break;
            case "anvil":

                MiniGame anvil = MiniGame.getMiniGameByGameType(Games.ANVIL_FALL);

                if (anvil == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                if (!anvil.getState().equals(GameState.WAITING)) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is already started !");
                    return false;
                }

                Objects.requireNonNull(MiniGame.getMiniGameByGameType(Games.ANVIL_FALL)).start();

                break;

            case "werewolf":

                MiniGame werewolf = MiniGame.getMiniGameByGameType(Games.WEREWOLF);

                if (werewolf == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                if (!werewolf.getState().equals(GameState.WAITING)) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is already started !");
                    return false;
                }

                Objects.requireNonNull(MiniGame.getMiniGameByGameType(Games.WEREWOLF)).start();

                break;

            default:
                commandSender.sendMessage(Louise.PREFIX + "Unknown game !");
                break;
        }

        return true;
    }
}
