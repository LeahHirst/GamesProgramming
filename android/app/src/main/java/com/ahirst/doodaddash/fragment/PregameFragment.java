package com.ahirst.doodaddash.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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

import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;
import com.ahirst.doodaddash.iface.CameraPollListener;
import com.ahirst.doodaddash.util.CameraUtil;

import io.socket.client.Ack;

public class PregameFragment extends Fragment {

    Activity mActivity;
    TextView mPredictionLabel;
    TextView mPredictionDescriptionLabel;
    TextView mStatusLabel;
    TextView mCountdownLabel;
    View mCountdownOverlay;

    boolean lockedIn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        return inflater.inflate(R.layout.fragment_pregame, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mPredictionLabel = getView().findViewById(R.id.prediction_label);
        mPredictionDescriptionLabel = getView().findViewById(R.id.prediction_label_label);
        mStatusLabel = getView().findViewById(R.id.status_label);
        mCountdownLabel = getView().findViewById(R.id.starting_in);
        mCountdownOverlay = getView().findViewById(R.id.countdown_overlay);

        mCountdownOverlay.setVisibility(View.INVISIBLE);

        final View lockInButton = getView().findViewById(R.id.btn_lockin);
        lockInButton.setClickable(true);
        lockInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lock In
                final String selectedObject = mPredictionLabel.getText().toString();

                // Tell the server
                if (Program.mSocket != null) {
                    Program.mSocket.emit("set object", selectedObject);
                }

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Update UI
                        mPredictionLabel.setText(selectedObject);
                        mPredictionDescriptionLabel.setText("Your object");
                        mStatusLabel.setText("Waiting for other players...");
                        lockInButton.setClickable(false);
                        lockInButton.setBackgroundColor(getResources().getColor(R.color.buttonDisabled));
                    }
                });
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    private void startCountdown() {
        mCountdownOverlay.setVisibility(View.INVISIBLE);

        final Thread countdownThread = new Thread() {

            boolean running = true;

            int timer = 5;

            @Override
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(1000);
                        timer--;
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mCountdownLabel.setText(Integer.toString(timer));
                                if (timer == 0) {
                                    running = false;
                                    transitionToInGame();
                                }
                            }
                        });
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        countdownThread.run();
    }

    private void transitionToInGame() {
//        FragmentManager fragmentManager = getFragmentManager();
//        FragmentTransaction ft = fragmentManager.beginTransaction();
//        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
//        ft.replace(R.id.menu_fragment, fragment);
//        ft.commit();
    }

    @Override
    public void onDestroyView() {


        super.onDestroyView();
    }

    @Override
    public void onResume() {
        super.onResume();
        Program.cameraPollListener = new CameraPollListener() {
            @Override
            public void onObjectUpdate(final String object) {

                if (mActivity != null) {
                    mActivity.runOnUiThread(new Runnable() {

                        @Override
                        public void run() {

                            mPredictionLabel.setText(object);

                        }
                    });
                }
            }
        };
    }

    @Override
    public void onPause() {
        Program.cameraPollListener = null;

        super.onPause();
    }
}
