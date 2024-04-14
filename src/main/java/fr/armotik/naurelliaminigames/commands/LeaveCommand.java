package fr.armotik.naurelliaminigames.commands;

import fr.armotik.louise.Louise;
import fr.armotik.louise.louise.LouiseGlobal;
import fr.armotik.naurelliaminigames.games.GameQueue;
import fr.armotik.naurelliaminigames.games.Games;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class LeaveCommand implements CommandExecutor {
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

                GameQueue spleefQueue = GameQueue.getGameQueueByGame(Games.SPLEEF);

                if (spleefQueue == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                if (!spleefQueue.removePlayer((Player) commandSender)) {
                    commandSender.sendMessage(Louise.PREFIX + "You are not in this game !");
                    return false;
                }

                commandSender.sendMessage(Louise.PREFIX + "You joined the game !");

                break;
            case "ttn":

                GameQueue ttfQueue = GameQueue.getGameQueueByGame(Games.THREAD_THE_NEEDLE);

                if (ttfQueue == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                if (!ttfQueue.removePlayer((Player) commandSender)) {
                    commandSender.sendMessage(Louise.PREFIX + "You are not in this game !");
                    return false;
                }

                commandSender.sendMessage(Louise.PREFIX + "You joined the game !");

                break;
            case "anvil":

                GameQueue anvilQueue = GameQueue.getGameQueueByGame(Games.ANVIL_FALL);

                if (anvilQueue == null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is not running !");
                    return false;
                }

                if (!anvilQueue.removePlayer((Player) commandSender)) {
                    commandSender.sendMessage(Louise.PREFIX + "You are not in this game !");
                    return false;
                }

                commandSender.sendMessage(Louise.PREFIX + "You joined the game !");

                break;
            default:
                commandSender.sendMessage(Louise.PREFIX + "Unknown game !");
                break;
        }

        return true;
    }
}
