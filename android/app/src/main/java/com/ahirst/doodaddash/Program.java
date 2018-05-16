package com.ahirst.doodaddash;

/**
 * Created by adamhirst on 06/05/2018.
 */

import android.content.res.AssetManager;

import com.ahirst.doodaddash.iface.CameraPollListener;
import com.ahirst.doodaddash.iface.SocketAction;
import com.ahirst.doodaddash.model.GameState;
import com.ahirst.doodaddash.model.Player;
import com.ahirst.doodaddash.util.ImageClassifier;

import java.net.URISyntaxException;
import java.util.List;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

/**
 * Program class saves the game state
 */
public class Program {

    // Game options
    private static final String GAME_SERVER_URI = "https://ahirst.com:3000";
    public static final int CAMERA_POLL_DURATION = 500;
    public static final int GAME_TIME = 180; // Seconds

    private static boolean initiated = false;

    private static Socket mSocket;
    public static ImageClassifier mClassifier;
    private static GameState mGameState;

    public static CameraPollListener cameraPollListener;

    public enum SignInMethod {
        GOOGLE,
        FACEBOOK
    }
    public static SignInMethod signInMethod;

    private static Player mProfile;
    private static List<Player> mPlayers;

    public static void updatePlayerList(List<Player> players) {
        mPlayers = players;
    }

    public static Player getPlayer() {
        return mProfile;
    }

    public static void setPlayer(Player player) {
        mProfile = player;
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
            establishSocketConnection(null);
        }
    }

    public static String getUserPhoto() {
        return mProfile.imgUrl;
    }

    public static String getUserName() {
        return mProfile.name;
    }

    public static void establishSocketConnection(final SocketAction action) {
        try {
            mSocket = IO.socket(GAME_SERVER_URI);

            if (action != null) {
                mSocket.once(Socket.EVENT_CONNECT, new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        action.run(mSocket);
                    }
                });
            }

            mSocket.connect();

            if (mProfile != null) {
                mSocket.emit("update profile", mProfile.name, mProfile.imgUrl);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static void getSocket(final SocketAction action) {
        if (mSocket == null || !mSocket.connected()) {
            establishSocketConnection(action);
        } else {
            action.run(mSocket);
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
            mClassifier = new ImageClassifier.Builder(assetManager)
                    .setModelFile("file:///android_asset/tensorflow_inception_graph.pb")
                    .setLabelFile("file:///android_asset/imagenet_comp_graph_label_strings.txt")
                    .setInputSize(224)
                    .setInputName("input")
                    .setOutputName("output")
                    .create();

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
