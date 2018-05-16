package com.ahirst.doodaddash.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ahirst.doodaddash.Program;
import com.ahirst.doodaddash.R;
import com.ahirst.doodaddash.model.Player;
import com.squareup.picasso.Picasso;

import java.util.List;

public class PostgameFragment extends Fragment {

    Activity mActivity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_postgame, container, false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = getActivity();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<Player> players = Program.getPlayerList();

        View firstContainer = mActivity.findViewById(R.id.first_place_container);
        View secondContainer = mActivity.findViewById(R.id.second_place_container);
        View thirdContainer = mActivity.findViewById(R.id.third_place_container);
        ImageView firstImage = mActivity.findViewById(R.id.first_image);
        ImageView secondImage = mActivity.findViewById(R.id.second_image);
        ImageView thirdImage = mActivity.findViewById(R.id.third_image);
        TextView firstName = mActivity.findViewById(R.id.first_place_name);
        TextView secondName = mActivity.findViewById(R.id.second_place_name);
        TextView thirdName = mActivity.findViewById(R.id.third_place_name);
        TextView firstPoints = mActivity.findViewById(R.id.first_place_points);
        TextView secondPoints = mActivity.findViewById(R.id.second_place_points);
        TextView thirdPoints = mActivity.findViewById(R.id.third_player_points);

        View btnMenu = mActivity.findViewById(R.id.btn_menu);
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        goToMainMenu();
                    }
                });
            }
        });

        int size = players.size();

        if (size > 0) {
            Player player = players.get(0);
            firstName.setText(player.name);
            firstPoints.setText(player.score + " points");
            Picasso.get().load(player.imgUrl).into(firstImage);
        } else {
            firstContainer.setVisibility(View.INVISIBLE);
        }

        if (size > 1) {
            Player player = players.get(1);
            secondName.setText(player.name);
            secondPoints.setText(player.score + " points");
            Picasso.get().load(player.imgUrl).into(secondImage);
        } else {
            secondContainer.setVisibility(View.INVISIBLE);
        }

        if (size > 2) {
            Player player = players.get(2);
            thirdName.setText(player.name);
            thirdPoints.setText(player.score + " points");
            Picasso.get().load(player.imgUrl).into(thirdImage);
        } else {
            thirdContainer.setVisibility(View.INVISIBLE);
        }
    }

    private void goToMainMenu() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction ft = fragmentManager.beginTransaction();
        ft.setCustomAnimations(R.anim.enter_from_left, R.anim.exit_to_right);
        ft.replace(R.id.menu_fragment, new MainMenuFragment());
        ft.commit();
    }
}
