package com.ahirst.doodaddash;

/**
 * Created by adamhirst on 06/05/2018.
 */

import android.content.res.AssetManager;

import com.ahirst.doodaddash.iface.CameraPollListener;
import com.ahirst.doodaddash.model.Player;
import com.ahirst.doodaddash.util.ImageClassifier;

import java.net.URISyntaxException;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;

/**
 * Program class saves the game state
 */
public class Program {

    private static final String GAME_SERVER_URI = "http://192.168.1.112:3000";
    public static final int CAMERA_POLL_DURATION = 500;

    private static boolean initiated = false;

    public static Socket mSocket;
    public static ImageClassifier mClassifier;

    public static CameraPollListener cameraPollListener;

    public enum SignInMethod {
        GOOGLE,
        FACEBOOK
    }
    public static SignInMethod signInMethod;

    public enum GameState {
        MENU,
        LOBBY,
        PRE_GAME,
        GAME,
        POST_GAME
    };
    public static GameState currentState = GameState.MENU;


    private static Player mProfile;
    private static List<Player> mPlayers;

    public static void updatePlayerList(List<Player> players) {
        mPlayers = players;
    }

    public static List<Player> getPlayerList() {
        return mPlayers;
    }

    public static void updateProfile(String givenName, String photoUrl) {
        mProfile = new Player();
        mProfile.name = givenName;
        mProfile.imgUrl = photoUrl;

        // Inform the server of the update
        if (mSocket != null) {
            mSocket.emit("update profile", mProfile.name, mProfile.imgUrl);
        } else {
            establishSocketConnection();
        }
    }

    public static String getUserPhoto() {
        return mProfile.imgUrl;
    }

    public static String getUserName() {
        return mProfile.name;
    }

    public static void establishSocketConnection() {
        try {
            mSocket = IO.socket(GAME_SERVER_URI);

            if (mProfile != null) {
                mSocket.emit("update profile", mProfile.name, mProfile.imgUrl);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }

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

    public static void safeDiscconnect() {
        if (mSocket != null) {
            mSocket.disconnect();
            mSocket = null;
        }
    }

}
