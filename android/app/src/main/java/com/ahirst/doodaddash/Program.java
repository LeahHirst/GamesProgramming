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

    private static String givenName;
    private static String photoUrl;

    public static void updateProfile(String givenName, String photoUrl) {
        Program.givenName = givenName;
        Program.photoUrl = photoUrl;

        if (mSocket == null) Program.establishConnection();

        // Inform the server of the update
        mSocket.emit("update profile", givenName, photoUrl);
    }

    private static void establishConnection() {
        try {
            mSocket = IO.socket(GAME_SERVER_URI);
        } catch(URISyntaxException e) {}

        mSocket.connect();
    }

    public static void init() {
        // Try to initiate the WS connection
        try {
            mSocket = IO.socket(GAME_SERVER_URI);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

}
