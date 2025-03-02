package com.sbmatch.deviceopt.activity;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.core.app.NavUtils;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.hchen.himiuix.MiuiAlertDialog;
import com.hchen.himiuix.MiuiPreference;
import com.hchen.himiuix.MiuiPreferenceCategory;
import com.kongzue.baseframework.BaseActivity;
import com.kongzue.baseframework.interfaces.FragmentLayout;
import com.kongzue.baseframework.interfaces.Layout;
import com.kongzue.baseframework.util.JumpParameter;
import com.kongzue.dialogx.dialogs.PopTip;
import com.sbmatch.deviceopt.AppGlobals;
import com.sbmatch.deviceopt.Utils.CloudCheckAppUpdateManager;
import com.sbmatch.deviceopt.Utils.SystemServiceWrapper.DevicePolicyManager;
import com.sbmatch.deviceopt.Utils.preference.Preference;

import ma.DeviceOptimizeHelper.R;

@SuppressLint("NonConstantResourceId")
@Layout(R.layout.settings_activity)
@FragmentLayout(R.id.settings)
public class SettingsActivity extends BaseActivity {

    public final Bundle bundle = new Bundle();

    @Override
    public void initViews() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(null);
        }
        if (isLightMode()) setDarkStatusBarTheme(true);
    }

    @Override
    public void initDatas(JumpParameter parameter) {
        if (getSupportFragmentManager().findFragmentByTag("settings_page") == null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, SettingsFragment.class, bundle, "settings_page")
                    .commit();
        }
    }

    @Override
    public void setEvents() {

    }

    public boolean isLightMode() {
        int nightModeFlags = AppGlobals.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags != Configuration.UI_MODE_NIGHT_YES;
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

    public static class SettingsFragment extends PreferenceFragmentCompat implements Preference.OnPreferenceClickListener {
        private final DevicePolicyManager dpm = DevicePolicyManager.get();
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
            MiuiPreferenceCategory settingsGroup = new MiuiPreferenceCategory(requireContext());
            settingsGroup.setKey("settings_group");
            MiuiPreferenceCategory aboutGroup = new MiuiPreferenceCategory(requireContext());
            aboutGroup.setKey("about_group");
            preferenceScreen.addPreference(settingsGroup);
            preferenceScreen.addPreference(aboutGroup);

            MiuiPreference custom_orgName = new MiuiPreference(requireContext());
            custom_orgName.setKey("custom_orgName_custom_org_name");
            custom_orgName.setTitle("设置个性组织名");
            custom_orgName.setTipText(String.valueOf(dpm.getOrganizationName()).equals("null") ? "" : String.valueOf(dpm.getOrganizationName()));
            custom_orgName.setOnPreferenceClickListener(this);
            settingsGroup.addPreference(custom_orgName);

            MiuiPreference checkUpdate = new MiuiPreference(requireContext());
            checkUpdate.setKey("check_update");
            checkUpdate.setTitle("检查更新");
            checkUpdate.setOnPreferenceClickListener(this);
            aboutGroup.addPreference(checkUpdate);

            setPreferenceScreen(preferenceScreen);
        }

        @Override
        public boolean onPreferenceClick(@NonNull androidx.preference.Preference preference) {
            switch (preference.getKey()){
                case "check_update" -> {
                    PopTip.show("正在检查更新...");
                    String verMsg = CloudCheckAppUpdateManager.checkHasNewVersion() ? "有新版本可用" : "已是最新版本";
                    AppGlobals.sMainHandler.postDelayed(() -> {
                        PopTip.show(verMsg);
                        ((MiuiPreference)findPreference(preference.getKey())).setTipText(verMsg);
                        }, 2000);
                }

                case "custom_orgName_custom_org_name" -> {
                    new MiuiAlertDialog(requireContext())
                            .setTitle(findPreference(preference.getKey()).getTitle())
                            .setEditTextHint("自定义的组织名")
                            .setEditText(((MiuiPreference)findPreference(preference.getKey())).getTipText() != null ? ((MiuiPreference)findPreference(preference.getKey())).getTipText() : "", (dialog, s) -> {
                                if (s != null && s.length() > 0){
                                    dpm.setOrganizationName((String) s);
                                    PopTip.show("已修改为 "+ s);
                                }else {
                                    dpm.setOrganizationName((String) s);
                                    PopTip.show("已清除自定义 组织名");
                                }
                                ((MiuiPreference)findPreference(preference.getKey())).setTipText((String) dpm.getOrganizationName());
                            })
                            .setEditTextAutoKeyboard(true)
                            .setEnableEditTextView(true)
                            .setPositiveButton("确定", null)
                            .show();
                }
            }
            return true;
        }
    }
}