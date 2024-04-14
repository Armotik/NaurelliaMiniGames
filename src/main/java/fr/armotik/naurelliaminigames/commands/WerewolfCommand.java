package fr.armotik.naurelliaminigames.commands;

import fr.armotik.louise.Louise;
import fr.armotik.louise.louise.LouiseGlobal;
import fr.armotik.naurelliaminigames.games.Games;
import fr.armotik.naurelliaminigames.games.minigames.MiniGame;
import fr.armotik.naurelliaminigames.games.minigames.werewolf.Werewolf;
import fr.armotik.naurelliaminigames.games.minigames.werewolf.WerewolfRoles;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class WerewolfCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {

        if (strings.length == 0) {

            commandSender.sendMessage(LouiseGlobal.wrongCommand());
            return false;
        }

        Player player = (Player) commandSender;

        if (player == null) {
            return false;
        }

        Werewolf werewolf = (Werewolf) MiniGame.getMiniGameByGameType(Games.WEREWOLF);

        if (werewolf == null) {
            return false;
        }

        if (werewolf.getPlayersInBed().contains(player) && !player.hasPermission("naurellia.staff.mod")) {
            player.sendMessage(Louise.PREFIX + "§cYou're sleeping !");
            return false;
        }

        // Staff command
        if (strings[0].equalsIgnoreCase("leave") && player.hasPermission("naurellia.staff.mod")) {

            player.removePotionEffect(PotionEffectType.BLINDNESS);
            player.removePotionEffect(PotionEffectType.SLOW);

            player.sendMessage(Louise.PREFIX + "§aYou're awake !");

            werewolf.getPlayersInBed().remove(player);

            return true;
        }

        //
        // Pass command
        // Night only
        //
        if (strings[0].equalsIgnoreCase("pass") && werewolf.getPlayersRoles().get(player).getPriority() != null) {

            if (werewolf.isDayTime()) return false;

            // Beast Hunter trap
            if (werewolf.getPlayersRoles().get(player) == WerewolfRoles.BEAST_HUNTER && !werewolf.getTrappedPlayers().isEmpty()) {

                werewolf.beastHunterActivateTrap();

                player.sendMessage(Louise.PREFIX + "§aThe trap has been activated !");
            }

            werewolf.sleepPlayer(player);

            return true;
        }

        //
        // village command
        // Day only
        //
        if (strings[0].equalsIgnoreCase("village")) {

            if (!werewolf.isDayTime()) return false;

            Player player1 = Bukkit.getPlayer(strings[1]);

            if (player1 == null) {

                player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[1] + " §cdoesn't exist.");
                return false;
            }

            if (strings[2].equalsIgnoreCase("vote")) {

                // TODO: Implement village vote command

            }

            else if (strings[2].equalsIgnoreCase("mayor")) {

                // TODO: Implement village mayor command
            }

            return true;
        }

        //
        // Cupid commands
        // Night only and only the first night
        //
        if (strings[0].equalsIgnoreCase("cupid") && werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.CUPID)) {

            if (strings[1].equalsIgnoreCase("love")) {

                if (werewolf.isDayTime() && werewolf.getNight() != 1) return false;

                Player player1 = Bukkit.getPlayer(strings[2]);
                Player player2 = Bukkit.getPlayer(strings[3]);

                if (player1 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                if (player2 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[3] + " §cdoesn't exist.");
                    return false;
                }

                werewolf.cupidLove(player, player1, player2);

                werewolf.sleepPlayer(player);

                return true;
            }
        }

        //
        // Lover commands
        // Anytime
        //
        if (strings[0].equalsIgnoreCase("lover") && (werewolf.getPlayersRoles().get(player).getRole2().equals(WerewolfRoles.LOVER) || werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.CUPID))) {

            Player lover1 = null;
            Player lover2 = null;

            for (Player lover : werewolf.getPlayersRoles().keySet()) {
                if (werewolf.getPlayersRoles().get(player).getRole2().equals(WerewolfRoles.LOVER)) {

                    if (lover1 == null) {
                        lover1 = lover;
                        continue;
                    }

                    lover2 = lover;
                }
            }

            assert lover1 != null;
            assert lover2 != null;

            player.sendMessage(Louise.PREFIX + "§aThe lovers are §7" + lover1.getName() + " §aand §7" + lover2.getName() + "§a.");
        }

        //
        // Seer commands (Seer and Wolf Seer)
        // Night only
        //
        if (strings[0].equalsIgnoreCase("seer") && (werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.SEER) || werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.WOLF_SEER))) {

            if (werewolf.isDayTime()) return false;

            if (strings[1].equalsIgnoreCase("see")) {

                Player player1 = Bukkit.getPlayer(strings[2]);

                if (player1 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                WerewolfRoles role = werewolf.seerCheck(player, player1);

                player.sendMessage(Louise.PREFIX + "§a" + player1.getName() + " §7is a §a" + role.getName() + "§7.");

                werewolf.sleepPlayer(player);

                return true;
            }
        }

        //
        // Salvatore commands
        // Night only
        //
        if (strings[0].equalsIgnoreCase("salvatore") && werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.SAVIOR)) {

            if (werewolf.isDayTime()) return false;

            if (strings[1].equalsIgnoreCase("protect")) {

                Player player1 = Bukkit.getPlayer(strings[2]);

                if (player1 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                werewolf.salvatoreProtect(player, player1);

                player.sendMessage(Louise.PREFIX + "§aYou protected §7" + player1.getName() + "§a.");

                werewolf.sleepPlayer(player);

                return true;
            }
        }

        //
        // Beast Hunter commands
        // Night only
        //
        if (strings[0].equalsIgnoreCase("beasthunter") && werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.BEAST_HUNTER)) {

            if (werewolf.isDayTime()) return false;

            if (strings[1].equalsIgnoreCase("settrap")) {

                Player player1 = Bukkit.getPlayer(strings[2]);

                if (player1 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                werewolf.beastHunterSetTrap(player, player1);

                player.sendMessage(Louise.PREFIX + "§aYou placed a trap on §7" + player1.getName() + "§a.");

                werewolf.sleepPlayer(player);

                return true;
            }
        }

        //
        // Werewolf commands (Werewolf and Wolf Seer)
        // Night only
        //
        if (strings[0].equalsIgnoreCase("werewolf") && (werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.WEREWOLF) || werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.WOLF_SEER))) {

            if (werewolf.isDayTime()) return false;

            if (strings[1].equalsIgnoreCase("kill")) {

                Player player1 = Bukkit.getPlayer(strings[2]);

                if (player1 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                // TODO: Implement werewolf kill command

                werewolf.sleepPlayer(player);

                return true;
            }
        }

        //
        // Witch commands
        // Night only
        //
        if (strings[0].equalsIgnoreCase("witch") && werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.WITCH)) {

            if (werewolf.isDayTime()) return false;

            if (strings[1].equalsIgnoreCase("save")) {

                Player player1 = Bukkit.getPlayer(strings[2]);

                if (player1 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                werewolf.witchSave(player, player1);

                player.sendMessage(Louise.PREFIX + "§aYou saved §7" + player1.getName() + "§a.");

                werewolf.sleepPlayer(player);

                return true;
            }

            if (strings[1].equalsIgnoreCase("kill")) {

                Player player1 = Bukkit.getPlayer(strings[2]);

                if (player1 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                werewolf.witchKill(player, player1);

                player.sendMessage(Louise.PREFIX + "§aYou killed §7" + player1.getName() + "§a.");

                werewolf.sleepPlayer(player);

                return true;
            }
        }

        return false;
    }
}
