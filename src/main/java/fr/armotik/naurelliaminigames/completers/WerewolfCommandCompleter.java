package fr.armotik.naurelliaminigames.completers;

import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import fr.armotik.naurelliaminigames.games.minigames.werewolf.Werewolf;
import fr.armotik.naurelliaminigames.games.minigames.werewolf.WerewolfRoles;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.List;

public class WerewolfCommandCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] strings) {

        Werewolf werewolf = (Werewolf) MiniGame.getMiniGameByGameType(Games.WEREWOLF);

        Player player = (Player) commandSender;

        if (werewolf == null) {
            return List.of();
        }

        // Staff command
        if (strings.length == 1 && commandSender.hasPermission("naurellia.staff.mod")) {
            return List.of("leave");
        }

        //
        // Cupid commands
        //
        if (strings.length == 1 && werewolf.getPlayersRoles().get(commandSender) != null && werewolf.getPlayersRoles().get(commandSender).equals(WerewolfRoles.CUPID)) {
            return List.of("cupid", "lovers", "pass");
        }

        if (strings.length == 2 && strings[0].equalsIgnoreCase("cupid") && werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.CUPID)) {
            return List.of("love");
        }

        //
        // Lover commands
        // Always available
        //
        if (strings.length == 1 && werewolf.getPlayersRoles().get(commandSender) != null && werewolf.getPlayersRoles().get(commandSender).getRole2().equals(WerewolfRoles.LOVER)) {
            return List.of("lover");
        }

        //
        // Seer commands (Seer and Wolf Seer)
        // Night only
        //
        if (!werewolf.isDayTime() && strings.length == 1 && werewolf.getPlayersRoles().get(commandSender) != null && (werewolf.getPlayersRoles().get(commandSender).equals(WerewolfRoles.SEER) || werewolf.getPlayersRoles().get(commandSender).equals(WerewolfRoles.WOLF_SEER))) {
            return List.of("seer", "pass");
        }

        if (!werewolf.isDayTime() && strings.length == 2 && strings[0].equalsIgnoreCase("seer") && (werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.SEER) || werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.WOLF_SEER))) {
            return List.of("see");
        }

        //
        // Salvatore commands
        // Night only
        //
        if (!werewolf.isDayTime() && strings.length == 1 && werewolf.getPlayersRoles().get(commandSender) != null && werewolf.getPlayersRoles().get(commandSender).equals(WerewolfRoles.SAVIOR)) {
            return List.of("salvatore", "pass");
        }

        if (!werewolf.isDayTime() && strings.length == 2 && strings[0].equalsIgnoreCase("salvatore") && werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.SAVIOR)) {
            return List.of("protect");
        }

        //
        // Beast Hunter commands
        // Night only
        //
        if (!werewolf.isDayTime() && strings.length == 1 && werewolf.getPlayersRoles().get(commandSender) != null && werewolf.getPlayersRoles().get(commandSender).equals(WerewolfRoles.BEAST_HUNTER)) {
            return List.of("beasthunter", "pass");
        }

        if (!werewolf.isDayTime() && strings.length == 2 && strings[0].equalsIgnoreCase("beasthunter") && werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.BEAST_HUNTER)) {
            return List.of("settrap");
        }

        //
        // Werewolf commands
        // Night only
        //
        if (!werewolf.isDayTime() && strings.length == 1 && werewolf.getPlayersRoles().get(commandSender) != null && (werewolf.getPlayersRoles().get(commandSender).equals(WerewolfRoles.WEREWOLF) || werewolf.getPlayersRoles().get(commandSender).equals(WerewolfRoles.WOLF_SEER))) {
            return List.of("werewolf", "pass");
        }

        if (!werewolf.isDayTime() && strings.length == 2 && strings[0].equalsIgnoreCase("werewolf") && (werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.WEREWOLF) || werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.WOLF_SEER))) {
            return List.of("kill");
        }

        //
        // Witch commands
        // Night only
        //
        if (!werewolf.isDayTime() && strings.length == 1 && werewolf.getPlayersRoles().get(commandSender) != null && werewolf.getPlayersRoles().get(commandSender).equals(WerewolfRoles.WITCH)) {
            return List.of("witch", "pass");
        }

        if (!werewolf.isDayTime() && strings.length == 2 && strings[0].equalsIgnoreCase("witch") && werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.WITCH)) {
            return List.of("kill", "save");
        }

        return List.of();
    }
}
