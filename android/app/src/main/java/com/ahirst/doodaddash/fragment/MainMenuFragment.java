package com.ahirst.doodaddash.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ahirst.doodaddash.PermissionActivity;
import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.squareup.picasso.Picasso;

public class MainMenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu_main, container, false);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        ImageView userImage = (ImageView) getView().findViewById(R.id.user_image);
        Picasso.get().load(Program.getUserPhoto()).into(userImage);

        View btnJoin = getView().findViewById(R.id.btn_join);
        View btnHost = getView().findViewById(R.id.btn_host);
        View btnSignout = getView().findViewById(R.id.btn_logout);
        View btnHelp = getView().findViewById(R.id.btn_help);

        btnHost.setClickable(true);
        btnHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMenu(new HostGameFragment());
            }
        });

        btnSignout.setClickable(true);
        btnSignout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signOut();
            }
        });

        btnJoin.setClickable(true);
        btnJoin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMenu(new JoinGameFragment());
            }
        });

        btnHelp.setClickable(true);
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), PermissionActivity.class);
                intent.putExtra("skippermcheck", true);
                startActivity(intent);
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    public void openMenu(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_to_left);
        ft.replace(R.id.menu_fragment, fragment);
        ft.commit();
    }

    private void signOut() {
        switch (Program.signInMethod) {
            case GOOGLE:
                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestProfile()
                        .build();
                GoogleSignIn.getClient(getActivity(), gso).signOut();
                break;
            case FACEBOOK:
                LoginManager.getInstance().logOut();
                break;
        }

        openMenu(new SignInFragment());
    }
}
