package com.sbmatch.deviceopt.utils.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.preference.PreferenceViewHolder;

import com.sbmatch.deviceopt.R;

public class Preference extends androidx.preference.Preference implements View.OnClickListener{
    private static final int arrow_right = 1000010;
    private TextView mTitle;
    public Preference(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs, androidx.preference.R.attr.preferenceStyle);
    }

    public Preference(@NonNull Context context) {
        this(context, null);
    }

    @Override
    public void onBindViewHolder(@NonNull PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        initLayouts(holder);
    }

    private void initLayouts(PreferenceViewHolder viewHolder){
        ViewGroup root = (ViewGroup) viewHolder.itemView;
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) root.getLayoutParams();

        if (params != null){
            params.leftMargin = 20;
            params.rightMargin = 45;
            root.setLayoutParams(params);
        }


        mTitle = root.findViewById(android.R.id.title);
        mTitle.setTextSize(19);

        AppCompatImageView arrow_rightView = new AppCompatImageView(getContext());
        arrow_rightView.setId(arrow_right);
        arrow_rightView.setImageResource(R.drawable.miuix_appcompat_intent_arrow_right);
        root.addView(arrow_rightView);
    }

    @Override
    public void onClick(View v) {

    }
}
