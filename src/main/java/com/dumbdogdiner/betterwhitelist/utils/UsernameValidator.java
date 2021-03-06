package com.dumbdogdiner.betterwhitelist.utils;

import com.google.gson.Gson;

import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Class for validating Minecraft usernames.
 */
public class UsernameValidator extends BaseUsernameValidator {

    /**
     * Fetch the UUID of a player from their Minecraft username.
     * 
     * @param username The name of the user to fetch from the API.
     * @return Mojang API user object
     */
    public static MojangUser getUser(String username, String ...origin) {
        // Make request to Mojang and decode JSON body.
        String json = fetchUserJson(formUrl(username), username, origin);

        if (json == null || json.equals("")) {
            return null;
        }

        MojangUser result = new Gson().fromJson(json, MojangUser.class);
        result.id = hyphenateUUID(result.id);

        return result;
    }
    
    /**
     * Create a MojangUser instance from ProxiedPlayer data.
     * 
     * @param ProxiedPlayer instance.
     * @return Mojang API user object.
     */
    public static MojangUser getUser(ProxiedPlayer player) {
    	MojangUser result = new MojangUser();
    	
    	result.id = hyphenateUUID(player.getUniqueId().toString());
    	result.name = player.getName();
    	
    	return result;
    }

    /**
     * Forms the base URL for the Mojang API request.
     * 
     * @param username
     * @return
     */
    private static String formUrl(String username) {
        // caching, load-balancing UUID server.
        String baseUrl = "https://mcuuid.jcx.ovh/v1/uuid/";
        return String.format("%s%s", baseUrl, username);
    }
}
