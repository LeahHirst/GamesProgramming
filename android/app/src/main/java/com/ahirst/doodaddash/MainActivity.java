package com.ahirst.doodaddash;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.google.android.cameraview.CameraView;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity {

    private CameraView mCameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mCameraView = (CameraView) findViewById(R.id.camera);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraView.start();
    }

    @Override
    protected void onPause() {
        mCameraView.stop();
        super.onPause();
    }
}
