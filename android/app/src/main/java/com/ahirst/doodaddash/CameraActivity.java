package com.ahirst.doodaddash;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.Window;
import android.view.WindowManager;

import com.otaliastudios.cameraview.CameraView;

public class CameraActivity extends FragmentActivity {

    CameraView mCameraView;
    Fragment mFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Disable title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // Set layout
        setContentView(R.layout.activity_camera);

        mCameraView = (CameraView) findViewById(R.id.camera);
        mFragment = getSupportFragmentManager().findFragmentById(R.id.overlay_fragment);

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        // ft.setCustomAnimations(R.id.[ ... ], R.id.[ ... ]);


    }
}
