package com.ahirst.doodaddash.fragment;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;
import com.ahirst.doodaddash.iface.CameraPollListener;
import com.ahirst.doodaddash.iface.SocketAction;
import com.ahirst.doodaddash.util.CameraUtil;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Recolor;
import com.transitionseverywhere.TransitionSet;

import io.socket.client.Ack;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class PregameFragment extends Fragment {

    Activity mActivity;
    TextView mPredictionLabel;
    TextView mPredictionDescriptionLabel;
    TextView mStatusLabel;
    TextView mCountdownLabel;
    View mCountdownOverlay;
    CardView mLockInButton;

    boolean lockedIn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        return inflater.inflate(R.layout.fragment_pregame, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        // Get references to views
        mPredictionLabel = getView().findViewById(R.id.prediction_label);
        mPredictionDescriptionLabel = getView().findViewById(R.id.prediction_label_label);
        mStatusLabel = getView().findViewById(R.id.status_label);
        mCountdownLabel = getView().findViewById(R.id.starting_in);
        mCountdownOverlay = getView().findViewById(R.id.countdown_overlay);

        // Register socket events
        Program.getSocket(new SocketAction() {
            @Override
            public void run(Socket socket) {

                socket.on("start countdown", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                startCountdown();
                            }
                        });
                    }
                });
            }
        });

        // Handle the lock in button
        mLockInButton = getView().findViewById(R.id.btn_lockin);
        mLockInButton.setClickable(true);
        mLockInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Lock In
                lockIn();
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    private void setLockInDisabled(boolean state) {
        mLockInButton.setClickable(!state);
        if (state) {
            // Disabled


            int white = Color.WHITE;
            int black = Color.BLACK;

        } else {
            // Enabled
            int white = Color.WHITE;
            int black = Color.BLACK;
        }
    }

    private void lockIn() {
        if (!lockedIn) {
            final String selectedObject = mPredictionLabel.getText().toString();

            if (selectedObject == "") return;

            TransitionSet ts = new TransitionSet()
                    .addTransition(new Recolor())
                    .addTransition(new Fade());

            Program.cameraPollListener = null;

            // Tell the server
            Program.getSocket(new SocketAction() {
                @Override
                public void run(Socket socket) {
                    socket.emit("set object", selectedObject);
                }
            });

            mPredictionLabel.setText(selectedObject);
            mPredictionDescriptionLabel.setText("Your object");
            mStatusLabel.setText("Waiting for other players...");
            mLockInButton.setClickable(false);
            mLockInButton.setCardBackgroundColor(getResources().getColor(R.color.buttonDisabled));

            int white = Color.WHITE;
            int black = Color.BLACK;
            int orange = ContextCompat.getColor(getContext(), R.color.orange);
        }
    }

    private void startCountdown() {
        mCountdownOverlay.setVisibility(View.VISIBLE);

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
        countdownThread.start();
    }

    private void transitionToInGame() {
        if (getActivity() == null) return;
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        ft.replace(R.id.overlay_fragment, new InGameFragment());
        ft.commit();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
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

                            if (!lockedIn) {
                                mPredictionLabel.setText(object);
                            }

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
