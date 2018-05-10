package com.ahirst.doodaddash.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ahirst.doodaddash.CameraActivity;
import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class SignInFragment extends Fragment {

    private int RC_GOOGLE = 0;

    private SignInButton mGoogleSignInButton;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sign_in, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        mGoogleSignInButton = getView().findViewById(R.id.google_signin_button);
        setGooglePlusButtonText(mGoogleSignInButton, "Continue with Google");
        mGoogleSignInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signInWithGoogle();
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    private void signInWithGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestProfile()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(getActivity(), gso);

        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE);
    }

    private void showMainMenu() {
        // Show the main menu
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        MainMenuFragment mainMenuFragment = new MainMenuFragment();
        ft.replace(R.id.menu_fragment, mainMenuFragment);
        ft.commit();
    }

    private void handleGoogleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            // Signed in successfully, show authenticated UI.
            String givenName = account.getGivenName();
            android.net.Uri photo = account.getPhotoUrl();
            // Update the profile
            Program.updateProfile(givenName, photo.toString());
            Program.signInMethod = Program.SignInMethod.GOOGLE;

            showMainMenu();
        } catch (ApiException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_GOOGLE) {
            // Google Sign in
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleGoogleSignInResult(task);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        Program.init(getActivity().getAssets());

        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(getActivity());
        if (account != null) {
            // Google account detected!
            String givenName = account.getGivenName();
            android.net.Uri photo = account.getPhotoUrl();

            Program.updateProfile(givenName, photo.toString());
            Program.signInMethod = Program.SignInMethod.GOOGLE;

            showMainMenu();
        }
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
