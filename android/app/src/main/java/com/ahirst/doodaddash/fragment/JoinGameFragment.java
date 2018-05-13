package com.ahirst.doodaddash.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;
import com.ahirst.doodaddash.iface.SocketAction;

import io.socket.client.Ack;
import io.socket.client.Socket;

public class JoinGameFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_join_game, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        View btnJoin = getView().findViewById(R.id.btn_join);
        View btnCancel = getView().findViewById(R.id.btn_cancel);
        final EditText pin = getView().findViewById(R.id.game_pin);

        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String gamePin = pin.getText().toString();
                Program.getSocket(new SocketAction() {
                    @Override
                    public void run(Socket socket) {
                        socket.emit("join game", gamePin, new Ack() {
                            @Override
                            public void call(Object... args) {
                                FragmentManager fragmentManager = getFragmentManager();
                                FragmentTransaction ft = fragmentManager.beginTransaction();
                                ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
                                JoinGameLobbyFragment newFrag = new JoinGameLobbyFragment();
                                Bundle bundle = new Bundle();
                                bundle.putString("pin", gamePin);
                                newFrag.setArguments(bundle);
                                ft.replace(R.id.menu_fragment, newFrag);
                                ft.commit();
                            }
                        });
                    }
                });

            }
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager fragmentManager = getFragmentManager();
                FragmentTransaction ft = fragmentManager.beginTransaction();
                ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
                ft.replace(R.id.menu_fragment, new MainMenuFragment());
                ft.commit();
            }
        });

        super.onActivityCreated(savedInstanceState);
    }
}
