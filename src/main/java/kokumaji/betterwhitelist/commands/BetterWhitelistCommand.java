package kokumaji.betterwhitelist.commands;

import kokumaji.betterwhitelist.BetterWhitelist;
import lombok.SneakyThrows;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class BetterWhitelistCommand implements CommandExecutor {

    FileConfiguration plConf = BetterWhitelist.getPlugin().getConfig();

    @SneakyThrows
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if(sender.hasPermission("betterwhitelist.command")) {
                if (args.length == 0) {
                    sender.sendMessage(ChatColor.GOLD + "BetterWhitelist " + BetterWhitelist.getPlugin().getDescription().getVersion() + ChatColor.GREEN + " developed by jaquewolfee");
                    sender.sendMessage(ChatColor.GOLD + "/betterwhitelist reload " + ChatColor.GREEN + "- Reloads the config file." + ChatColor.GOLD + "\n/betterwhitelist whois <Minecraft User> " + ChatColor.GREEN + "- Displays information about the connected Discord account.");
                } else if (args[0].equals("help")) {
                    sender.sendMessage(ChatColor.GOLD + "/betterwhitelist reload " + ChatColor.GREEN + "- Reloads the config file." + ChatColor.GOLD + "\n/betterwhitelist whois <Minecraft User> " + ChatColor.GREEN + "- Displays information about the connected Discord account.");
                } else if (args[0].equals("whois")) {
                    if(sender.hasPermission("betterwhitelist.command.whois")) {
                        Reader reader = Files.newBufferedReader(Paths.get(BetterWhitelist.getPlugin().getDataFolder() + "/userdata.csv"));
                        List<String[]> userData = BetterWhitelist.getUserData(reader);
                        boolean userFound = false;

                        for (int i = 0; i < userData.size(); i++) {
                            String[] current = userData.get(i);
                            String pUUID = Bukkit.getServer().getOfflinePlayer(args[1]).getUniqueId().toString();
                            if (current[0].equals(pUUID)) {
                                Guild guild = BetterWhitelist.getJda().getGuildById(BetterWhitelist.getPlugin().getConfig().getString("discord.guildid"));
                                Member member = guild.getMemberById(current[1]);
                                userFound = true;
                                sender.sendMessage(ChatColor.DARK_GREEN + "========================================\n" + ChatColor.GREEN + "Results of " + ChatColor.GOLD + Bukkit.getServer().getOfflinePlayer(pUUID).getName() + ChatColor.GREEN + "\nDiscord Username " + ChatColor.GOLD + member.getUser().getAsTag() + ChatColor.GREEN + "\nAccount created on " + ChatColor.GOLD + member.getUser().getTimeCreated() + ChatColor.GREEN + "\nUser ID " + ChatColor.GOLD + member.getUser().getId() + ChatColor.GREEN + "\nHighest Role " + ChatColor.GOLD + member.getRoles().get(0) + ChatColor.DARK_GREEN + "========================================");
                                break;
                            }
                        }

                        if(!userFound) {
                            sender.sendMessage(ChatColor.RED + "Could not find user.");
                        }
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have the required permissions to do this!");
                    }
                } else if (args[0].equals("reload")) {
                    if(sender.hasPermission("betterwhitelist.command.reload")) {
                        BetterWhitelist.getPlugin().reloadConfig();
                        sender.sendMessage(ChatColor.GREEN + "Config reloaded!");
                    } else {
                        sender.sendMessage(ChatColor.RED + "You don't have the required permissions to do this!");
                    }

                }
            } else {
                sender.sendMessage(ChatColor.RED + "You don't have the required permissions to do this!");
            }

        }

        return true;
    }
}
