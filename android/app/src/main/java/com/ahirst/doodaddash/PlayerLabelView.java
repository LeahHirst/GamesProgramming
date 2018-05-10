package com.ahirst.doodaddash;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class PlayerLabelView extends LinearLayout {

    public ImageView image;
    public TextView text;

    public PlayerLabelView(Context context) {
        super(context);
        sharedInit();
    }

    public PlayerLabelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        sharedInit();
    }

    public PlayerLabelView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        sharedInit();
    }

    private void sharedInit() {
        inflate(getContext(), R.layout.view_player_card, this);
        image = (ImageView)findViewById(R.id.user_image);
        text = (TextView)findViewById(R.id.text_view);
    }

}
