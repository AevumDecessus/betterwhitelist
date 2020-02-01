package com.dumbdogdiner.betterwhitelist_client.listeners;

import com.dumbdogdiner.betterwhitelist_client.BetterWhitelistClientPlugin;
import com.dumbdogdiner.betterwhitelist_client.BungeeMessenger;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;


/**
 * Listener that checks newly-joined players with the global ban list to see if they should be banned.
 */
public class PlayerJoinListener implements Listener {
    private boolean banSyncEnabled = BetterWhitelistClientPlugin.getPlugin().getConfig().getBoolean("enableBanSync");
    private BungeeMessenger bungee = BetterWhitelistClientPlugin.bungee;

    @EventHandler
    void onPlayerJoin(PlayerJoinEvent e) {
        if (!banSyncEnabled) {
            return;
        }

        Player target = e.getPlayer();

        // Attempt to tell Bungee - need to have an online player to send messages.
        // This is probs redundant.
        Player receiver;
        if (target.isOnline()) {
            receiver = target;
        } else {
            receiver = Bukkit.getOnlinePlayers().iterator().next();
        }

        if (!receiver.isOnline()) {
            Bukkit.getLogger().info("No players are online - cannot inform Bungee of ban. Caching until somebody joins...");
            // TODO: Add cache.
            return;
        }

        bungee.checkGlobalBan(receiver, target.getUniqueId());
    }

}
