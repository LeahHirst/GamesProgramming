package com.ahirst.doodaddash.util;

import com.ahirst.doodaddash.model.Player;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PlayerUtil {

    public static List<Player> parsePlayersFromJSON(JSONArray players) {
        try {
            List<Player> playersList = new ArrayList<Player>();

            for (int i = 0; i < players.length(); i++) {
                JSONObject user = players.getJSONObject(i);
                Player player = new Player();
                player.name = user.getString("name");
                player.imgUrl = user.getString( "imgUrl");
                playersList.add(player);
            }

            return playersList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }

}
