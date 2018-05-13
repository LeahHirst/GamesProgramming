package com.ahirst.doodaddash.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ahirst.doodaddash.R;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.extra.Scale;

public class AnimSlide extends Fragment {

    private static final String ARG_LAYOUT_RES_ID = "layoutResId";
    private int layoutResId;

    Activity mActivity;

    Thread animRunner;

    private String currentObject = "";

    private TextView mCurrentObject;
    private TextView mPredictedObject;
    private TextView mCurrentObjectAnim;
    private RelativeLayout mCurrentContainer;
    private RelativeLayout mCurrentContainerActual;
    private RelativeLayout mPredictedContainer;
    private RelativeLayout mFooter;
    private CardView mCurrentContainerAnimCard;

    public boolean animRunning = false;

    public static AnimSlide newInstance(int layoutResId) {
        AnimSlide sampleSlide = new AnimSlide();

        Bundle args = new Bundle();
        args.putInt(ARG_LAYOUT_RES_ID, layoutResId);
        sampleSlide.setArguments(args);

        return sampleSlide;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null && getArguments().containsKey(ARG_LAYOUT_RES_ID)) {
            layoutResId = getArguments().getInt(ARG_LAYOUT_RES_ID);
        }
    }

    void sleepN(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception e) {}
    }

    void setPredicted(final String predicted) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mPredictedObject.setText(predicted);
            }
        });
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mCurrentObject = getView().findViewById(R.id.current_label);
        mCurrentObjectAnim = getView().findViewById(R.id.current_label_anim);
        mPredictedObject = getView().findViewById(R.id.prediction_label);
        mCurrentContainer = getView().findViewById(R.id.current_container_anim);
        mPredictedContainer = getView().findViewById(R.id.prediction_container);
        mFooter = getView().findViewById(R.id.footer);
        mCurrentContainerAnimCard = getView().findViewById(R.id.current_container_anim_card);
        mCurrentContainer.setVisibility(View.INVISIBLE);
        mCurrentContainerActual = getView().findViewById(R.id.current_container);

        super.onActivityCreated(savedInstanceState);
    }

    private void successAnimation(final String newObject) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mCurrentContainer != null) {
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
        });
    }

    private void skipAnimation(final String newObject) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
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
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mActivity = getActivity();

        animRunner = new Thread() {
            @Override
            public void run() {
                while (animRunning) {

                    if (currentObject == "")
                        currentObject = "laptop";

                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mCurrentObject.setText("laptop");
                        }
                    });
                    sleepN(500);

                    setPredicted("monitor");
                    sleepN(1000);
                    setPredicted("mouse");
                    sleepN(1000);
                    setPredicted("keyboard");
                    sleepN(1000);
                    setPredicted("laptop");
                    successAnimation("cat");
                    sleepN(2000);
                    setPredicted("dog");
                    sleepN(1000);
                    setPredicted("hamster");
                    sleepN(1000);
                    skipAnimation("laptop");

                }
            }
        };
        animRunning = true;
        animRunner.start();
    }

    @Override
    public void onDetach() {
        animRunning = false;

        super.onDetach();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mActivity = getActivity();

        return inflater.inflate(layoutResId, container, false);
    }

}
