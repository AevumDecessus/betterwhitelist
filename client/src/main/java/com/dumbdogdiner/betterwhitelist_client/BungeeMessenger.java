package com.dumbdogdiner.betterwhitelist_client;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.bukkit.BanList.Type;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

// I know * imports are bad practice, but intellij is being insistent.
import java.io.*;
import java.util.UUID;

public class BungeeMessenger implements PluginMessageListener {
    public boolean banSyncEnabled = BetterWhitelistClientPlugin.getPlugin().getConfig().getBoolean("enableBanSync");
    public UUID lastestBan;

    /**
     * Request for a player to be banned globally.
     * @param receiver
     * @param target
     * @throws IOException
     */
    public void addGlobalBan(Player receiver, UUID target) {
        Bukkit.getLogger().info("Requesting player UUID '" + target.toString() + "' be banned globally...");
        sendEvent(receiver, "Ban", target.toString());
    }

    /**
     * Requests the Bungee server to remove a global ban.
     * @param receiver
     * @param target
     */
    public void removeGlobalBan(Player receiver, UUID target) {
        Bukkit.getLogger().info("Requesting player UUID '" + target.toString() + "' be pardoned globally...");
        sendEvent(receiver, "Pardon", target.toString());
    }

    /**
     * Asks Bungee whether or not the target UUID has been banned.
     * @param receiver
     * @param target
     */
    public void checkGlobalBan(Player receiver, UUID target) {
        Bukkit.getLogger().info("Bunge => Is '" + target.toString() + "' banned?");
    }



    @Override
    public void onPluginMessageReceived(String channel, Player receiver, byte[] message) {
        if (!channel.equals("BungeeCord")) {
            return;
        }

        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        String subchannel = in.readUTF();

        if (!subchannel.equals("Forward")) {
            return;
        }

        // Read forwarded command - for some reason it's done like this.
        short len = in.readShort();
        byte[] msgbytes = new byte[len];
        in.readFully(msgbytes);
        DataInputStream msgin = new DataInputStream(new ByteArrayInputStream(msgbytes));

        String command;

        // This shouldn't be necessary but VS Code threw a fit at me.
        try {
            command = msgin.readUTF();
        } catch (IOException err) {
            return;
        }

        if (command.equals("Ban")) {
            String uuidToBan = in.readUTF();
            handleBanRequest(uuidToBan);
            return;
        }

        if (command.equals("IsBanned")) {
            String uuidToCheck = in.readUTF();
            handleCheckBanRequest(receiver, uuidToCheck);
            return;
        }
    }

    /**
     * Handle a request to ban a player from the server.
     * 
     * @param uuidToBan
     */
    public void handleBanRequest(String uuidToBan) {
        if (banSyncEnabled == false) {
            return;
        }

        Bukkit.getLogger().info("Request to ban user '" + uuidToBan + "' from Bungee.");

        Player onlinePlayer = Bukkit.getPlayer(UUID.fromString(uuidToBan));

        // If player was found
        if (onlinePlayer.isOnline() && !onlinePlayer.isBanned()) {
            onlinePlayer.kickPlayer("Banned from server.");
            Bukkit.getBanList(Type.NAME).addBan(onlinePlayer.getName(), "Banned from server.", null, null);
            lastestBan = UUID.fromString(uuidToBan);
        }
    }

    /**
     * Handle a request from another server to check if a player is banned.
     * 
     * @param receiver
     * @param uuidToCheck
     */
    public void handleCheckBanRequest(Player receiver, String uuidToCheck) {
        Bukkit.getLogger().info("Checking if UUID '" + uuidToCheck + "' should be banned globally...");

        Player player = Bukkit.getPlayer(UUID.fromString(uuidToCheck));

        if (player.isBanned()) {
            sendEvent(receiver, "Ban", uuidToCheck);
        }
    }

    /**
     * Send an event to other plugins connected to Bungeecord.
     * 
     * Defaults to messaging all ONLINE servers.
     */
    void sendEvent(Player receiver, String subChannel, String... args) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();

        out.writeUTF("Forward");
        out.writeUTF("ONLINE");
        out.writeUTF(subChannel);

        for (String arg : args) {
            out.writeUTF(arg);
        }

        receiver.sendPluginMessage(BetterWhitelistClientPlugin.getPlugin(), "BungeeCord", out.toByteArray());
    }
}