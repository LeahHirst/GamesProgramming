package com.ahirst.doodaddash.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ahirst.doodaddash.R;

import static java.lang.Boolean.getBoolean;

public class MenuFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_menu, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();

        Bundle bundle = getArguments();
        boolean postgame = false;

        if (bundle != null) {
            postgame = bundle.getBoolean("postgame");
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (!postgame) {
            ft.replace(R.id.menu_fragment, new SignInFragment());
        } else {
            ft.replace(R.id.menu_fragment, new PostgameFragment());
        }

        ft.commit();

    }
}
