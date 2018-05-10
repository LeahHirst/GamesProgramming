package com.ahirst.doodaddash.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;

import io.socket.client.Ack;

public class HostGameFragment extends Fragment {

    Activity mActivity;
    TextView mGamePinLabel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        return inflater.inflate(R.layout.fragment_host_game, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mGamePinLabel = getView().findViewById(R.id.game_pin);

        if (Program.mSocket != null) {
            Program.mSocket.emit("host game", new Ack() {
                @Override
                public void call(Object... args) {
                    String gamePin = (String) args[0];
                    mGamePinLabel.setText(gamePin);
                }
            });
        }

        super.onActivityCreated(savedInstanceState);
    }

}
