package com.ahirst.doodaddash.fragment;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.widget.CardView;
import android.transition.ArcMotion;

import com.ahirst.doodaddash.iface.SocketAction;
import com.transitionseverywhere.ChangeBounds;
import android.transition.Transition;
import com.transitionseverywhere.TransitionManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahirst.doodaddash.PlayerLabelView;
import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;
import com.ahirst.doodaddash.iface.CameraPollListener;
import com.ahirst.doodaddash.model.Player;
import com.ahirst.doodaddash.util.PlayerUtil;
import com.squareup.picasso.Picasso;
import com.transitionseverywhere.extra.Scale;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class InGameFragment extends Fragment {

    private FragmentActivity mActivity;

    private boolean isFirst = true;
    private boolean skip = false;

    private LinearLayout mScoreboard;
    private TextView mTimeLabel;
    private TextView mCurrentObject;
    private TextView mPredictedObject;
    private TextView mCurrentObjectAnim;

    private boolean running = true;

    private RelativeLayout mCurrentContainer;
    private RelativeLayout mCurrentContainerActual;
    private RelativeLayout mPredictedContainer;
    private RelativeLayout mFooter;
    private CardView mCurrentContainerAnimCard;

    private int gameTimeLeft = Program.GAME_TIME;

    private String currentObject;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        return inflater.inflate(R.layout.fragment_ingame, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mScoreboard = getView().findViewById(R.id.scoreboard);
        mTimeLabel = getView().findViewById(R.id.time_left);
        mCurrentObject = getView().findViewById(R.id.current_label);
        mCurrentObjectAnim = getView().findViewById(R.id.current_label_anim);
        mPredictedObject = getView().findViewById(R.id.prediction_label);
        mCurrentContainer = getView().findViewById(R.id.current_container_anim);
        mPredictedContainer = getView().findViewById(R.id.prediction_container);
        mFooter = getView().findViewById(R.id.footer);
        mCurrentContainerAnimCard = getView().findViewById(R.id.current_container_anim_card);
        mCurrentContainer.setVisibility(View.INVISIBLE);
        mCurrentContainerActual = getView().findViewById(R.id.current_container);

        mCurrentContainerActual.setVisibility(View.INVISIBLE);

        getView().setClickable(true);
        getView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Skip the current question
                Program.getSocket(new SocketAction() {
                    @Override
                    public void run(Socket socket) {
                        socket.emit("skip");
                    }
                });

                skip = true;
            }
        });

        Program.getSocket(new SocketAction() {
            @Override
            public void run(final Socket socket) {
                socket.on("object request", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        final String newObject = (String) args[0];
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (isFirst) {
                                    currentObject = newObject;
                                    mCurrentObject.setText(newObject);
                                    isFirst = false;
                                    TransitionManager.beginDelayedTransition(mCurrentContainerActual,
                                            new Scale(0.7f).setDuration(500));
                                    mCurrentContainerActual.setVisibility(View.VISIBLE);
                                } else if (skip) {
                                    skip = false;
                                    skipAnimation(newObject);
                                } else {
                                    successAnimation(newObject);
                                    Player p = Program.getPlayer();
                                    p.score++;
                                    Program.setPlayer(p);
                                }

                            }
                        });
                    }
                });
                socket.on("scoreboard update", new Emitter.Listener() {
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
                socket.once("end game", new Emitter.Listener() {
                    @Override
                    public void call(Object... args) {
                        JSONObject obj = (JSONObject) args[0];

                        socket.off("object request");
                        socket.off("scoreboard update");

                        try {
                            JSONArray users = obj.getJSONArray("users");

                            Program.cameraPollListener = null;

                            Program.updatePlayerList(PlayerUtil.parsePlayersFromJSON(users));

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    transitionToPostGame();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        Program.cameraPollListener = new CameraPollListener() {
            @Override
            public void onObjectUpdate(final String object) {

                mActivity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mPredictedObject.setText(object);
                    }
                });

                if (currentObject.equalsIgnoreCase(object) && object != "") {
                    // Player has detected the object!
                    Program.getSocket(new SocketAction() {
                        @Override
                        public void run(Socket socket) {
                            socket.emit("item detected", object);
                        }
                    });
                }
            }
        };

        startTimer();

        super.onActivityCreated(savedInstanceState);
    }

    private void transitionToPostGame() {
        FragmentManager fragmentManager = mActivity.getSupportFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        MenuFragment menuFragment = new MenuFragment();
        Bundle bundle = new Bundle();
        bundle.putBoolean("postgame", true);
        menuFragment.setArguments(bundle);
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        ft.replace(R.id.overlay_fragment, menuFragment);
        ft.commit();
    }

    private void successAnimation(String newObject) {
        if (mCurrentContainer != null && getContext() != null) {
            int white = Color.WHITE;
            int black = Color.BLACK;
            int green = ContextCompat.getColor(getContext(), R.color.successColor);
            mCurrentContainer.setVisibility(View.VISIBLE);
            mCurrentContainerActual.setVisibility(View.INVISIBLE);
            mCurrentObjectAnim.setText(currentObject);
            currentObject = newObject;
            mCurrentContainer.bringToFront();
            mCurrentObject.setText(newObject);
            ValueAnimator cardColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), white, green);
            cardColorAnim.setDuration(250);
            cardColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final ValueAnimator valueAnimator) {
                    mCurrentContainerAnimCard.setCardBackgroundColor((int) valueAnimator.getAnimatedValue());
                }
            });
            cardColorAnim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animator) {
                    // Start offscreen anim
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                Thread.sleep(1000);
                            } catch (Exception e) {}

                            mActivity.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    final int dX = -(int)Math.floor((mCurrentContainer.getWidth()*1.5));
                                    final int dY = -mCurrentContainer.getHeight();
                                    final int dR = -90;
                                    mCurrentContainer.animate()
                                            .setInterpolator(new AccelerateInterpolator())
                                            .setDuration(500)
                                            .rotationBy(dR)
                                            .xBy(dX)
                                            .yBy(dY)
                                            .setListener(new AnimatorListenerAdapter() {
                                                @Override
                                                public void onAnimationEnd(Animator animation) {
                                                    mActivity.runOnUiThread(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            mCurrentContainer.setVisibility(View.INVISIBLE);
                                                            mCurrentContainer.clearAnimation();
                                                            mCurrentContainer.setX(mCurrentContainer.getX()-dX);
                                                            mCurrentContainer.setY(mCurrentContainer.getY()-dY);
                                                            mCurrentContainer.setRotation(mCurrentContainer.getRotation()-dR);
                                                            mCurrentContainerAnimCard.setCardBackgroundColor(Color.WHITE);
                                                            mCurrentObjectAnim.setTextColor(Color.BLACK);
                                                        }
                                                    });
                                                }
                                            })
                                            .start();

                                    TransitionManager.beginDelayedTransition(mCurrentContainerActual, new Scale(0.7f).setDuration(500));
                                    mCurrentContainerActual.setVisibility(View.VISIBLE);
                                }
                            });
                        }
                    }.start();
                }
            });
            ValueAnimator textColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), black, white);
            textColorAnim.setDuration(250);
            textColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mCurrentObjectAnim.setTextColor((int) valueAnimator.getAnimatedValue());
                }
            });

            cardColorAnim.start();
            textColorAnim.start();
        }
    }

    private void skipAnimation(String newObject) {
        if (mCurrentContainer != null) {
            int white = Color.WHITE;
            int black = Color.BLACK;
            int orange = ContextCompat.getColor(getContext(), R.color.orange);

            mCurrentContainer.setVisibility(View.VISIBLE);
            mCurrentContainerActual.setVisibility(View.INVISIBLE);
            mCurrentObjectAnim.setText(currentObject);
            currentObject = newObject;
            mCurrentObject.setText(newObject);
            ValueAnimator cardColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), white, orange);
            cardColorAnim.setDuration(250);
            cardColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(final ValueAnimator valueAnimator) {
                    mCurrentContainerAnimCard.setCardBackgroundColor((int) valueAnimator.getAnimatedValue());
                }
            });
            ValueAnimator textColorAnim = ValueAnimator.ofObject(new ArgbEvaluator(), black, white);
            textColorAnim.setDuration(250);
            textColorAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    mCurrentObjectAnim.setTextColor((int) valueAnimator.getAnimatedValue());
                }
            });
            cardColorAnim.start();
            textColorAnim.start();

            final int dY = (int) (-mCurrentContainer.getHeight() * 1.2);
            mCurrentContainer.bringToFront();
            mCurrentContainer.animate()
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .setDuration(500)
                    .yBy(dY)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            mCurrentContainerActual.bringToFront();
                            mCurrentContainer.animate()
                                    .setInterpolator(new AccelerateDecelerateInterpolator())
                                    .setDuration(500)
                                    .yBy(-dY)
                                    .setListener(new AnimatorListenerAdapter() {
                                        @Override
                                        public void onAnimationEnd(Animator animation) {
                                            mCurrentContainer.setVisibility(View.INVISIBLE);
                                            mCurrentContainer.clearAnimation();
                                            mCurrentContainerAnimCard.setCardBackgroundColor(Color.WHITE);
                                            mCurrentObjectAnim.setTextColor(Color.BLACK);
                                        }
                                    }).start();
                            TransitionManager.beginDelayedTransition(mCurrentContainer, new Scale(0.5f).setDuration(500));
                            mCurrentContainer.setVisibility(View.INVISIBLE);
                        }
                    }).start();

            TransitionManager.beginDelayedTransition(mCurrentContainerActual, new Scale(0.7f).setDuration(500));
            mCurrentContainerActual.setVisibility(View.VISIBLE);
        }
    }

    private void startTimer() {
        new Thread() {
            @Override
            public void run() {
                while (running) {
                    try {
                        Thread.sleep(1000);
                        gameTimeLeft--;
                        mActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int minutes = (int) Math.floor(gameTimeLeft / 60);
                                int seconds = gameTimeLeft % 60;

                                String timeString = String.format("%01d:%02d", minutes, seconds);
                                mTimeLabel.setText(timeString);
                            }
                        });
                        if (gameTimeLeft == 0) {
                            running = false;
                        }
                    } catch (Exception e) {}
                }
            }
        }.start();
    }

    private void updateScoreboard() {
        Context context = getContext();
        if (context != null && mScoreboard != null) {
            List<Player> players = Program.getPlayerList();
            if (players != null) {
                TransitionManager.beginDelayedTransition(mScoreboard, new ChangeBounds());
                mScoreboard.removeAllViews();
                boolean playerFeaturedInScore = false;
                for (int i = 0; i < players.size(); i++) {
                    Player player = players.get(i);
                    if (player.imgUrl == Program.getUserPhoto()) playerFeaturedInScore = true;
                    if (i == 2 && !playerFeaturedInScore) {
                        player = Program.getPlayer();
                    }
                    PlayerLabelView playerLabel = new PlayerLabelView(getContext());
                    Picasso.get().load(player.imgUrl).into(playerLabel.image);
                    playerLabel.text.setText(Integer.toString(player.score));
                    TransitionManager.setTransitionName(playerLabel, player.imgUrl);
                    mScoreboard.addView(playerLabel);
                }
            }
        }
    }
}
