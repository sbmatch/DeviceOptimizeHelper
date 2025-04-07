package com.sbmatch.deviceopt.activity;

import android.annotation.SuppressLint;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import com.hchen.himiuix.DialogInterface;
import com.hchen.himiuix.MiuiAlertDialog;
import com.hchen.himiuix.MiuiSwitchPreference;
import com.hchen.himiuix.widget.MiuiTextView;
import com.kongzue.baseframework.util.AppManager;
import com.sbmatch.deviceopt.AppGlobals;
import com.sbmatch.deviceopt.MainActivity;
import com.sbmatch.deviceopt.databinding.ActivitySplashBinding;
import com.sbmatch.deviceopt.R;
import com.sbmatch.deviceopt.utils.ResourceUtils;

import io.noties.markwon.Markwon;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class SplashActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler(Looper.myLooper());
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private Runnable mHideRunnable;
    private ActivitySplashBinding binding;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySplashBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!shouldShowPrivacyPolicy()){
            showPrivacyPolicyDialog();
        }else {

            mHideHandler.postDelayed(() -> {
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
                finish();
            }, 1000 * 3);
        }

    }


    private boolean shouldShowPrivacyPolicy(){
        return AppGlobals.getMMKV().decodeBool("privacy_policy_shown", false);
    }

    private void showPrivacyPolicyDialog(){
        MiuiTextView textView = new MiuiTextView(this);
        Markwon markwon = Markwon.create(this);
        markwon.setMarkdown(textView, getString(R.string.privacy_policy_desc, getString(R.string.app_name)));

        new MiuiAlertDialog(this)
                .setTitle(AppGlobals.getResources().getString(R.string.welcome_useme, getString(R.string.app_name)))
                .setCanceledOnTouchOutside(false)
                .setEnableCustomView(true)
                .setCustomView(textView, new DialogInterface.OnBindView() {
                    @Override
                    public void onBindView(ViewGroup root, View view) {

                    }
                })
                .setPositiveButton("同意并继续", (dialog, v) -> {

                    AppGlobals.getMMKV().encode("privacy_policy_shown", true);
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                })
                .setNegativeButton("退出", (dialog, v) -> finish())
                .show();
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}