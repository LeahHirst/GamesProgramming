package com.ahirst.doodaddash.fragment;

import android.animation.Animator;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;
import com.ahirst.doodaddash.iface.CameraPollListener;
import com.ahirst.doodaddash.util.PlayerUtil;

import org.json.JSONArray;
import org.json.JSONObject;

import io.socket.emitter.Emitter;

public class InGameFragment extends Fragment {

    private Activity mActivity;

    private LinearLayout mScoreboard;
    private TextView mTimeLabel;
    private TextView mCurrentObject;
    private TextView mPredictedObject;

    private View mCurrentContainer;
    private View mPredictedContainer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        return inflater.inflate(R.layout.fragment_ingame, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mScoreboard = getView().findViewById(R.id.scoreboard);
        mTimeLabel = getView().findViewById(R.id.time_left);
        mCurrentObject = getView().findViewById(R.id.current_label);
        mPredictedObject = getView().findViewById(R.id.prediction_label);
        mCurrentContainer = getView().findViewById(R.id.current_container);
        mPredictedContainer = getView().findViewById(R.id.prediction_container);

        if (Program.mSocket != null) {
            Program.mSocket.on("next object", new Emitter.Listener() {
                @Override
                public void call(Object... args) {

                }
            });
            Program.mSocket.on("scoreboard update", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    JSONObject obj = (JSONObject) args[0];

                    try {
                        JSONArray users = obj.getJSONArray("users");

                        Program.updatePlayerList(PlayerUtil.parsePlayersFromJSON(users));

                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateScoreboard();
                            }
                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            Program.mSocket.on("end game", new Emitter.Listener() {
                @Override
                public void call(Object... args) {
                    // TODO: Handle endgame
                }
            });
        }

        Program.cameraPollListener = new CameraPollListener() {
            @Override
            public void onObjectUpdate(String object) {
                if (mCurrentObject.getText() == object) {
                    // Player has detected the object!
                    if (Program.mSocket != null) {
                        Program.mSocket.emit("item detected", object);
                    }
                }
            }
        };

        super.onActivityCreated(savedInstanceState);
    }

    private void successAnimation() {
        if (mCurrentContainer != null && mPredictedContainer != null) {
            float cX = mCurrentContainer.getX();
            float cY = mCurrentContainer.getY();
            float pX = mPredictedContainer.getX();
            float pY = mPredictedContainer.getY();

            mCurrentContainer.animate()
                    .translationX((getView().getWidth() - mCurrentContainer.getWidth()) / 2)
                    .translationY((getView().getHeight() - mPredictedContainer.getHeight()) / 2)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(500)
                    .setListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animator) {}

                        @Override
                        public void onAnimationEnd(Animator animator) {
                            // Animation ended
                            
                        }

                        @Override
                        public void onAnimationCancel(Animator animator) {}

                        @Override
                        public void onAnimationRepeat(Animator animator) {}
                    });
        }
    }

    private void updateScoreboard() {

    }
}
