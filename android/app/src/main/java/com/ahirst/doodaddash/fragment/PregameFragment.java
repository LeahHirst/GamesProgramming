package com.ahirst.doodaddash.fragment;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mActivity = getActivity();
        return inflater.inflate(R.layout.fragment_pregame, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        mPredictionLabel = getView().findViewById(R.id.prediction_label);

        super.onActivityCreated(savedInstanceState);
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
