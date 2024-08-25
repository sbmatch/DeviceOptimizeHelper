package com.sbmatch.deviceopt.activity;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.preference.EditTextPreference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.rosan.dhizuku.api.Dhizuku;
import com.sbmatch.deviceopt.Utils.NotificationHelper;
import com.sbmatch.deviceopt.Utils.SystemServiceWrapper.DevicePolicyManager;

import ma.DeviceOptimizeHelper.R;
import rikka.material.app.MaterialActivity;

public class SettingsActivity extends MaterialActivity {

    //private static final DhizukuUserServiceFactory dhizukuUserServiceFactory = new DhizukuUserServiceFactory();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(null);
        }

        if (Dhizuku.isPermissionGranted() && savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment.class, new Bundle())
                    .commit();
        }

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate up using NavUtils, should return to parent Activity
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        private final NotificationHelper notificationHelper = NotificationHelper.getInstance();
        private final DevicePolicyManager dpm = DevicePolicyManager.get();
        public SettingsFragment() {
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());

            EditTextPreference editTextPreference = new EditTextPreference(requireContext());
            editTextPreference.setTitle("自定义组织名");
            editTextPreference.setDialogTitle("请输入自定义组织名");
            editTextPreference.setKey("customOrgName");
            editTextPreference.setSummaryProvider(preference -> {
                String text = ((EditTextPreference)preference).getText();
                dpm.setOrganizationName(text);
                return text;
            });
            preferenceScreen.addPreference(editTextPreference);

            setPreferenceScreen(preferenceScreen);
        }
    }
}