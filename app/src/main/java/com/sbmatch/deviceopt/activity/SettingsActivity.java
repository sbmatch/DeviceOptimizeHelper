package com.sbmatch.deviceopt.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.color.DynamicColors;
import com.kongzue.baseframework.BaseActivity;
import com.kongzue.baseframework.interfaces.FragmentLayout;
import com.kongzue.baseframework.interfaces.Layout;
import com.kongzue.baseframework.util.AppManager;
import com.kongzue.baseframework.util.JumpParameter;
import com.sbmatch.deviceopt.AppGlobals;
import com.sbmatch.deviceopt.R;
import com.sbmatch.deviceopt.utils.ResourceUtils;
import com.sbmatch.deviceopt.fragment.SettingsFragment;


@SuppressLint("NonConstantResourceId")
@Layout(R.layout.settings_activity)
@FragmentLayout(R.id.settings)
public class SettingsActivity extends BaseActivity implements FragmentManager.OnBackStackChangedListener {

    public final Bundle bundle = new Bundle();
    private ActionBar actionBar;


    @Override
    public void initViews() {
        if (actionBar == null) actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(null);

        DynamicColors.applyToActivityIfAvailable(this);

        if (isLightMode()) setDarkStatusBarTheme(true);
    }


    @Override
    public void initDatas(JumpParameter parameter) {

    }

    @Override
    public void setEvents() {
        getSupportFragmentManager().addOnBackStackChangedListener(this);
        if (getSupportFragmentManager().findFragmentByTag("settings_page") == null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment.class, bundle, "settings_page")
                    .commit();
        }
    }

    public boolean isLightMode() {
        int nightModeFlags = AppGlobals.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags != Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                    getSupportFragmentManager().popBackStack();
                } else {
                    // Navigate up using NavUtils, should return to parent Activity
                    NavUtils.navigateUpFromSameTask(this);
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onBack() {

        return super.onBack();
    }


    @Override
    public void onBackStackChanged() {
        if (getSupportFragmentManager().findFragmentByTag("settings_page") != null && getSupportFragmentManager().findFragmentByTag("settings_page").isVisible()) {
            setTitle(ResourceUtils.getString(R.string.setting));
        }
    }
}