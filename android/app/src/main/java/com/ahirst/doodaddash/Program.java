package com.ahirst.doodaddash;

/**
 * Created by adamhirst on 06/05/2018.
 */

import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import java.net.URISyntaxException;

/**
 * Program class saves the game state
 */
public class Program {

    private static final String GAME_SERVER_URI = "";

    private static Socket mSocket;

    public enum GameState {
        MENU,
        LOBBY,
        PRE_GAME,
        GAME,
        POST_GAME
    };
    public static GameState currentState = GameState.MENU;

    public static void init() {
        // Try to initiate the WS connection
        try {
            mSocket = IO.socket(GAME_SERVER_URI);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
