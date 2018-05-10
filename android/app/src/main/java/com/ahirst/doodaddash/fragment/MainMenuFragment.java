package com.ahirst.doodaddash.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;
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

        btnHost.setClickable(true);
        btnHost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openMenu(new HostGameFragment());
            }
        });

        super.onActivityCreated(savedInstanceState);
    }

    public void openMenu(Fragment fragment) {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_right, R.anim.exit_from_left);
        ft.replace(R.id.menu_fragment, fragment);
        ft.commit();
    }
}
