package com.ahirst.doodaddash.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahirst.doodaddash.PlayerLabelView;
import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;
import com.ahirst.doodaddash.model.Player;
import com.ahirst.doodaddash.util.PlayerUtil;
import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import io.socket.client.Ack;
import io.socket.emitter.Emitter;

public class HostGameFragment extends Fragment {

    Activity mActivity;
    TextView mGamePinLabel;
    FlexboxLayout mPlayerList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        return inflater.inflate(R.layout.fragment_host_game, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mGamePinLabel = getView().findViewById(R.id.game_pin);
        mPlayerList = getView().findViewById(R.id.player_list);

        if (Program.mSocket != null && mActivity != null) {
            // Ask the server for a game pin
            Program.mSocket.emit("host game", new Ack() {
                @Override
                public void call(final Object... args) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            String gamePin = (String) args[0];
                            mGamePinLabel.setText(gamePin);
                        }
                    });
                }
            });

            // Register the player update event
            Program.mSocket.on("userlist update", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];

                    try {
                        JSONArray users = obj.getJSONArray("users");

                        Program.updatePlayerList(PlayerUtil.parsePlayersFromJSON(users));

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updatePlayersUI();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        View btnLeave = getView().findViewById(R.id.btn_leave);
        View btnStart = getView().findViewById(R.id.btn_start);

        btnLeave.setClickable(true);
        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                leaveGame();
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    private void updatePlayersUI() {
        List<Player> players = Program.getPlayerList();
        if (mPlayerList != null && players != null && getContext() != null) {
            // Clear all current views
            mPlayerList.removeAllViews();

            // Add each view
            for (int i = 0; i < players.size(); i++) {
                PlayerLabelView label = new PlayerLabelView(getContext());
                label.text.setText(players.get(i).name);
                Picasso.get().load(players.get(i).imgUrl).into(label.image);
                mPlayerList.addView(label);
            }
        }
    }

    private void startGame() {
        if (Program.mSocket != null) {
            // Start the game
        }
    }

    private void leaveGame() {
        if (Program.mSocket != null) {
            Program.mSocket.emit("leave game");
        }
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        ft.replace(R.id.menu_fragment, new MainMenuFragment());
        ft.commit();
    }

}
