package com.ahirst.doodaddash.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import com.ahirst.doodaddash.iface.SocketAction;
import com.ahirst.doodaddash.model.Player;
import com.ahirst.doodaddash.util.PlayerUtil;
import com.google.android.flexbox.FlexboxLayout;
import com.squareup.picasso.Picasso;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.TransitionManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class JoinGameLobbyFragment extends Fragment {

    Activity mActivity;
    TextView mGamePinLabel;
    FlexboxLayout mPlayerList;

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        return inflater.inflate(R.layout.fragment_join_lobby, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mGamePinLabel = getView().findViewById(R.id.game_pin);
        mPlayerList = getView().findViewById(R.id.player_list);

        Bundle bundle = getArguments();
        if (bundle != null) {
            String pin = bundle.getString("pin");
            if (pin != null) {
                mGamePinLabel.setText(pin);
            }
        }

        Program.getSocket(new SocketAction() {
            @Override
            public void run(Socket socket) {
                socket.on("userlist update", new Emitter.Listener() {
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

                socket.on("start pregame", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startGame();
                            }
                        });
                    }
                });
            }
        });

        View btnLeave = getView().findViewById(R.id.btn_leave);
        btnLeave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        leaveGame();
                    }
                });
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    private void startGame() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        ft.replace(R.id.overlay_fragment, new PregameFragment());
        ft.commit();
    }

    private void updatePlayersUI() {
        List<Player> players = Program.getPlayerList();
        if (mPlayerList != null && players != null && getContext() != null) {
            // Clear all current views
            TransitionManager.beginDelayedTransition(mPlayerList, new ChangeBounds());

            mPlayerList.removeAllViews();

            // Add each view
            for (int i = 0; i < players.size(); i++) {
                Player player = players.get(i);
                PlayerLabelView label = new PlayerLabelView(getContext());
                label.text.setText(player.name);
                Picasso.get().load(player.imgUrl).into(label.image);
                TransitionManager.setTransitionName(label, player.imgUrl);
                mPlayerList.addView(label);
            }
        }
    }

    private void leaveGame() {
        Program.getSocket(new SocketAction() {
            @Override
            public void run(Socket socket) {
                socket.emit("leave game");
            }
        });

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        ft.replace(R.id.menu_fragment, new MainMenuFragment());
        ft.commit();
    }
}
