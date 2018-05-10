package com.ahirst.doodaddash;

/**
 * Created by adamhirst on 06/05/2018.
 */

import android.content.res.AssetManager;

import com.ahirst.doodaddash.iface.CameraPollListener;
import com.ahirst.doodaddash.util.ImageClassifier;

import java.net.URISyntaxException;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Program class saves the game state
 */
public class Program {

    private static final String GAME_SERVER_URI = "http://10.201.246.14:3000";
    public static final int CAMERA_POLL_DURATION = 3000;

    private static boolean initiated = false;

    public static Socket mSocket;
    public static ImageClassifier mClassifier;

    public static CameraPollListener cameraPollListener;

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

        // Inform the server of the update
        mSocket.emit("update profile", givenName, photoUrl);
    }

    public static String getUserPhoto() {
        return photoUrl;
    }

    public static String getUserName() {
        return givenName;
    }

    public static void init(AssetManager assetManager) {
        if (!initiated) {
            // Try to initiate the WS connection
            try {
                mSocket = IO.socket(GAME_SERVER_URI);
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
            mSocket.connect();

            // Initiate classifier
            mClassifier = new ImageClassifier(assetManager);

            initiated = true;
        }
    }

}
