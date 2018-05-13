package com.ahirst.doodaddash;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.ahirst.doodaddash.util.AnimSlide;
import com.ahirst.doodaddash.util.DashSlide;
import com.github.paolorotolo.appintro.AppIntro;
import com.github.paolorotolo.appintro.AppIntro2;
import com.github.paolorotolo.appintro.AppIntro2Fragment;
import com.github.paolorotolo.appintro.AppIntroFragment;

/**
 * Created by adamhirst on 06/05/2018.
 */

public class PermissionActivity extends AppIntro2 {

    private static final int PERMISSION_REQUEST_CAMERA = 0;

    Activity activityContext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityContext = this;

        addSlide(DashSlide.newInstance(R.layout.slide_first));
        addSlide(DashSlide.newInstance(R.layout.slide_second));
        addSlide(DashSlide.newInstance(R.layout.slide_three));
        addSlide(AnimSlide.newInstance(R.layout.slide_four));
        addSlide(DashSlide.newInstance(R.layout.slide_five));


        boolean skipPermissionCheck = getIntent().getExtras().getBoolean("skippermcheck");

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED && !skipPermissionCheck) {
            openMainActivity();
        }
    }

    @Override
    public void onSlideChanged(@Nullable Fragment oldFragment, @Nullable Fragment newFragment) {
//        if (oldFragment instanceof AnimSlide) {
//            ((AnimSlide)oldFragment).animRunning = false;
//        } else if (newFragment instanceof AnimSlide) {
//            ((AnimSlide)newFragment).animRunning = true;
//        }

        super.onSlideChanged(oldFragment, newFragment);
    }

    private void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // request the permission
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    PERMISSION_REQUEST_CAMERA);
        } else {
            openMainActivity();
        }
    }

    private void openMainActivity() {
        Intent intent = new Intent(this, CameraActivity.class);
        startActivity(intent);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch(requestCode) {
            case PERMISSION_REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openMainActivity();
                }
                return;
            }
        }
    }

    @Override
    public void onDonePressed(Fragment currentFragment) {
        super.onDonePressed(currentFragment);
        requestPermissions();
    }

    @Override
    public void onSkipPressed(Fragment currentFragment) {
        super.onSkipPressed(currentFragment);
        requestPermissions();
    }
}
