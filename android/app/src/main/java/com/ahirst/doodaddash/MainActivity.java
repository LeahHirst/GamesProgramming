package com.ahirst.doodaddash;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.otaliastudios.cameraview.CameraView;

import static java.security.AccessController.getContext;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private CameraView mCameraView;
    private SignInButton mGoogleSignInButton;
    private GoogleSignInClient mGoogleSignInClient;

    private static final int RC_GOOGLE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mCameraView = (CameraView) findViewById(R.id.camera);

        // Set up google sign in
        mGoogleSignInButton = (SignInButton) findViewById(R.id.google_signin_button);
        setGooglePlusButtonText(mGoogleSignInButton, "Continue with Google");
        mGoogleSignInButton.setOnClickListener(this);
    }

    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE);
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            String givenName = account.getGivenName();
            android.net.Uri photo = account.getPhotoUrl();

            Program.updateProfile(givenName, photo.toString());
            Intent menuIntent = new Intent(this, CameraActivity.class);
            startActivity(menuIntent);
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            // Google Sign in
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.google_signin_button:
            signInWithGoogle();
            break;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        Program.init(getAssets());

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if (account != null) {
            // Google account detected!
            String givenName = account.getGivenName();
            android.net.Uri photo = account.getPhotoUrl();

            Program.updateProfile(givenName, photo.toString());

            Intent menuIntent = new Intent(this, CameraActivity.class);
            startActivity(menuIntent);
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraView.destroy();
    }

    protected void setGooglePlusButtonText(SignInButton signInButton, String buttonText) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (int i = 0; i < signInButton.getChildCount(); i++) {
            View v = signInButton.getChildAt(i);

            if (v instanceof TextView) {
                TextView tv = (TextView) v;
                tv.setText(buttonText);
                return;
            }
        }
    }
}
