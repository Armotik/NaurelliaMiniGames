package fr.armotik.naurelliaminigames.commands;

import fr.armotik.louise.Louise;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.Objects;

public class StopCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        String game = strings[0];

        switch (game) {
            case "spleef":

                if (MiniGame.getMiniGameByGameType(Games.SPLEEF) == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                Objects.requireNonNull(MiniGame.getMiniGameByGameType(Games.SPLEEF)).stop();

                break;
            case "ttn":

                if (MiniGame.getMiniGameByGameType(Games.THREAD_THE_NEEDLE) == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                Objects.requireNonNull(MiniGame.getMiniGameByGameType(Games.THREAD_THE_NEEDLE)).stop();

                break;
            case "anvil":

                if (MiniGame.getMiniGameByGameType(Games.ANVIL_FALL) == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                Objects.requireNonNull(MiniGame.getMiniGameByGameType(Games.ANVIL_FALL)).stop();

                break;

            case "werewolf":

                if (MiniGame.getMiniGameByGameType(Games.WEREWOLF) == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                Objects.requireNonNull(MiniGame.getMiniGameByGameType(Games.WEREWOLF)).stop();

                break;
            default:
                commandSender.sendMessage(Louise.PREFIX + "Unknown game !");
                break;
        }

        return true;
    }
}
