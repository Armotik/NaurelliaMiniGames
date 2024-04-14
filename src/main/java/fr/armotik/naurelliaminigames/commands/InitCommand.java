package fr.armotik.naurelliaminigames.commands;

import fr.armotik.louise.Louise;
import fr.armotik.louise.louise.LouiseGlobal;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.AnvilFall;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import fr.armotik.naurelliaminigames.games.minigames.Spleef;
import fr.armotik.naurelliaminigames.games.minigames.ThreadTheNeedle;
import fr.armotik.naurelliaminigames.games.minigames.werewolf.Werewolf;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InitCommand implements CommandExecutor {
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

                if (MiniGame.getMiniGameByGameType(Games.SPLEEF) != null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is already running !");
                    return false;
                }

                new Spleef((Player) commandSender).init();

                break;
            case "ttn":

                if (MiniGame.getMiniGameByGameType(Games.THREAD_THE_NEEDLE) != null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is already running !");
                    return false;
                }

                new ThreadTheNeedle((Player) commandSender).init();

                break;
            case "anvil":

                if (MiniGame.getMiniGameByGameType(Games.ANVIL_FALL) != null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is already running !");
                    return false;
                }

                new AnvilFall((Player) commandSender).init();

                break;

            case "werewolf":

                if (MiniGame.getMiniGameByGameType(Games.WEREWOLF) != null) {
                    commandSender.sendMessage(Louise.PREFIX + "This game is already running !");
                    return false;
                }

                new Werewolf((Player) commandSender).init();

                break;
            default:
                commandSender.sendMessage(Louise.PREFIX + "Unknown game !");
                break;
        }

        return true;
    }
}
