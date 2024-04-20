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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        if (werewolf.isSpectator(player)) {
            player.sendMessage(Louise.PREFIX + "§cYou're a spectator ! You are dead and can't participate in the game.");
            return true;
        }

        //
        // Pass command
        // Anytime
        //
        if (strings[0].equalsIgnoreCase("pass") && werewolf.getPlayersRoles().get(player).getPriority() != null) {

            if (strings[1].equalsIgnoreCase("day")) {

                // TODO: Implement pass day command

                return true;
            }

            if (strings[1].equalsIgnoreCase("night")) {

                if (werewolf.isDayTime()) return false;

                // Beast Hunter trap
                if (werewolf.getPlayersRoles().get(player) == WerewolfRoles.BEAST_HUNTER && !werewolf.getTrappedPlayers().isEmpty()) {

                    werewolf.beastHunterActivateTrap();

                    player.sendMessage(Louise.PREFIX + "§aThe trap has been activated !");
                }

                werewolf.sleepPlayer(player);

                return true;
            }

            else {

                player.sendMessage(LouiseGlobal.wrongCommand());
                return false;
            }
        }

        //
        // village command
        // Day only
        //
        if (strings[0].equalsIgnoreCase("village")) {

            if (!werewolf.isDayTime()) return false;

            if (strings[2].equalsIgnoreCase("vote")) {

                if (!werewolf.isVotingTime()) {

                    player.sendMessage(Louise.PREFIX + "§cYou can't vote now !");
                    return true;
                }

                if (werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.VILLAGE_IDIOT) && werewolf.isVillageIdiotVoted()) {
                    player.sendMessage(Louise.PREFIX + "§cAn idiot can't vote !");
                    return true;
                }

                Player player1 = Bukkit.getPlayer(strings[1]);

                if (player1 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[1] + " §cdoesn't exist.");
                    return false;
                }

                if (player1 == player) {

                    player.sendMessage(Louise.PREFIX + "§cYou can't vote for yourself !");
                    return false;
                }

                if (werewolf.isSpectator(player1)) {

                    player.sendMessage(Louise.PREFIX + "§cYou really want to vote for a dead player ?");
                    return true;
                }

                werewolf.vote(player, player1);

                player.sendMessage(Louise.PREFIX + "§aYou voted for §7" + player1.getName() + "§a.");
                return true;
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

                if (werewolf.isSpectator(player1)) {

                    player.sendMessage(Louise.PREFIX + "§cThis player is dead.");
                    return true;
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

                if (werewolf.isSpectator(player1)) {

                    player.sendMessage(Louise.PREFIX + "§cThis player is dead.");
                    return true;
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

                if (werewolf.isSpectator(player1)) {

                    player.sendMessage(Louise.PREFIX + "§cThis player is dead.");
                    return true;
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

                Player target = Bukkit.getPlayer(strings[2]);

                if (target == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                if (werewolf.isSpectator(target)) {

                    player.sendMessage(Louise.PREFIX + "§cThis player is already dead !");
                    return true;
                }

                List<Player> werewolves = new ArrayList<>();

                for (Player werewolf1 : werewolf.getPlayersRoles().keySet()) {
                    if (werewolf.getPlayersRoles().get(werewolf1).equals(WerewolfRoles.WEREWOLF) || werewolf.getPlayersRoles().get(werewolf1).equals(WerewolfRoles.WOLF_SEER)) {
                        werewolves.add(werewolf1);
                    }
                }

                werewolf.werewolfKill(player, target);

                werewolves.forEach(werewolf1 -> werewolf1.sendMessage(Louise.PREFIX + "§b" + player.getName() + " §7voted for §b" + target.getName() + "§7."));

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

                if (werewolf.getWitchPotions().containsKey(false)) {

                    player.sendMessage(Louise.PREFIX + "§cYou already used your save potion.");
                    return false;
                }

                Player player1 = Bukkit.getPlayer(strings[2]);

                if (player1 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                if (werewolf.isSpectator(player1)) {

                    player.sendMessage(Louise.PREFIX + "§cThis player is dead.");
                    return true;
                }

                werewolf.witchSave(player, player1);

                player.sendMessage(Louise.PREFIX + "§aYou protected §7" + player1.getName() + "§a.");

                werewolf.sleepPlayer(player);

                return true;
            }

            if (strings[1].equalsIgnoreCase("kill")) {

                if (werewolf.getWitchPotions().containsValue(false)) {

                    player.sendMessage(Louise.PREFIX + "§cYou already used your kill potion.");
                    return false;
                }

                Player player1 = Bukkit.getPlayer(strings[2]);

                if (player1 == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                if (werewolf.isSpectator(player1)) {

                    player.sendMessage(Louise.PREFIX + "§cThis player is dead.");
                    return true;
                }

                if (player1 == player) {

                    player.sendMessage(Louise.PREFIX + "§cYou can't kill yourself.");
                    return false;
                }

                werewolf.witchKill(player, player1);

                player.sendMessage(Louise.PREFIX + "§aYou killed §7" + player1.getName() + "§a.");

                werewolf.sleepPlayer(player);

                return true;
            }
        }

        //
        // Hunter command
        //
        if (strings[0].equalsIgnoreCase("hunter") && (werewolf.getPlayersRoles().get(player).equals(WerewolfRoles.HUNTER))) {

            if (!werewolf.isDayTime()) return false;

            if (strings[1].equalsIgnoreCase("kill")) {

                if (!werewolf.isHunterVoted()) {
                    player.sendMessage(Louise.PREFIX + "§cYou can't use this command now !");
                    return true;
                }

                Player target = Bukkit.getPlayer(strings[2]);

                if (target == null) {

                    player.sendMessage(Louise.PREFIX + "§cThe player §4" + strings[2] + " §cdoesn't exist.");
                    return false;
                }

                if (werewolf.isSpectator(target)) {

                    player.sendMessage(Louise.PREFIX + "§cThis player is dead.");
                    return true;
                }

                werewolf.eliminatePlayer(target);

                if (werewolf.isLoverDead(target)) {

                    for (Map.Entry<Player, WerewolfRoles> entry : werewolf.getPlayersRoles().entrySet()) {
                        if (entry.getValue().getRole2() == WerewolfRoles.LOVER && !entry.getKey().equals(target)) {

                            werewolf.getPlayers().forEach(player1 -> {

                                player1.sendMessage(Louise.PREFIX + "§c" + target.getName() + " was shot by the hunter !");
                                player1.sendMessage(Louise.PREFIX + "§c" + werewolf.getPlayersRoles().get(target).getName() + " was a " + werewolf.getPlayersRoles().get(target).getName() + " !");

                                player1.sendMessage(Louise.PREFIX + "§cUnfortunately, " + entry.getKey().getName() + " was in love with " + target.getName() + " !");

                                player1.sendMessage(Louise.PREFIX + "§cSo " + entry.getKey().getName() + " died of a broken heart !");
                                player1.sendMessage(Louise.PREFIX + "§c" + entry.getKey().getName() + " was a " + entry.getValue().getName() + " !");
                            });

                            werewolf.eliminatePlayer(entry.getKey());
                            return true;
                        }
                    }
                }

                werewolf.getPlayers().forEach(player1 -> {

                    player1.sendMessage(Louise.PREFIX + "§cThe hunter §4" + player.getName() + " §7shot on §c" + target.getName() + "§7.");
                    player1.sendMessage(Louise.PREFIX + "§c" + target.getName() + " §7was eliminated.");
                    player1.sendMessage(Louise.PREFIX + "§c" + target.getName() + " §7was a §c" + werewolf.getPlayersRoles().get(target).getName() + "§7.");
                });

                return true;
            }
        }

        return false;
    }
}