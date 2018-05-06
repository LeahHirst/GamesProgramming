package com.ahirst.doodaddash.authentication;

import android.preference.PreferenceManager;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;

public class GoogleAuthenticationHandler {

    private GoogleSignInClient mGoogleSignInClient;

    public GoogleAuthenticationHandler() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build();


    }

    public void signIn() {

    }

}
