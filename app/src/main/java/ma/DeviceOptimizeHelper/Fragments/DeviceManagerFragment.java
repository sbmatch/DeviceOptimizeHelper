package ma.DeviceOptimizeHelper.Fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ma.DeviceOptimizeHelper.IDeviceOptService;
import ma.DeviceOptimizeHelper.Utils.AnyRestrictPolicyUtils;
import ma.DeviceOptimizeHelper.Utils.PackageManager;
import ma.DeviceOptimizeHelper.Utils.ServiceManager;
import ma.DeviceOptimizeHelper.ViewModels.FragmentShareIBinder;
import rikka.preference.SimpleMenuPreference;

public class DeviceManagerFragment extends PreferenceFragmentCompat {
    private PreferenceScreen preferenceScreen;
    private IDeviceOptService deviceOptService;
    private PackageManager packageManager = ServiceManager.getPackageManager();
    private FragmentShareIBinder shareIBinder;
    private SharedPreferences sharedPreferences;
    private boolean isCanUse;

    public DeviceManagerFragment(){

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        shareIBinder = new ViewModelProvider(this).get(FragmentShareIBinder.class);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onCreatePreferences(@Nullable Bundle savedInstanceState, @Nullable String rootKey) {
        deviceOptService = shareIBinder.getDeviceOptService();
        // 获取根布局，如果不存在则创建一个
        if (preferenceScreen == null) {
            preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
        }
        preferenceScreen.removeAll();

        sharedPreferences = requireContext().getSharedPreferences("main_sharePreference", Context.MODE_PRIVATE);
        sharedPreferences.edit().putBoolean("deviceFragment", true).apply();

        isCanUse = deviceOptService != null;

        PreferenceCategory deviceRestrictionCategory = new PreferenceCategory(requireContext());
        deviceRestrictionCategory.setIconSpaceReserved(false);
        deviceRestrictionCategory.setTitle("设备管控");
        preferenceScreen.addPreference(deviceRestrictionCategory);

        ArrayMap<String,String> appLists = getHomeAppLists();
        List<String> packageNames = new ArrayList<>(appLists.keySet());
        List<String> appNames = new ArrayList<>(appLists.values());
        SimpleMenuPreference defaultHome = new SimpleMenuPreference(requireContext());
        defaultHome.setTitle("设置默认桌面应用");
        defaultHome.setEntries(appNames.toArray(new CharSequence[appNames.size()]));
        defaultHome.setEntryValues(packageNames.toArray(new CharSequence[packageNames.size()]));
        defaultHome.setDefaultValue(AnyRestrictPolicyUtils.getDefaultHome());
        defaultHome.setOnPreferenceChangeListener((preference, newValue) -> {
            if (isCanUse) {
                try {
                    deviceOptService.setDefaultHome((String) newValue);
                    defaultHome.setIcon(getAppIconForPackageName((String) newValue));
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
            return isCanUse;
        });
        defaultHome.setSummaryProvider(preference ->
                packageManager.getAppNameForPackageName(requireContext(), defaultHome.getValue()));
        defaultHome.setIcon(getAppIconForPackageName(AnyRestrictPolicyUtils.getDefaultHome()));
        deviceRestrictionCategory.addPreference(defaultHome);

        SwitchPreferenceCompat switchUsbDebug = new SwitchPreferenceCompat(requireContext());
        switchUsbDebug.setTitle("设置USB调试开关状态");
        switchUsbDebug.setDefaultValue(AnyRestrictPolicyUtils.isEnableAdb());
        switchUsbDebug.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                if (isCanUse) {
                    try {
                        deviceOptService.enableUsbDebug((Boolean) newValue);
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
                return isCanUse;
            }
        });
        deviceRestrictionCategory.addPreference(switchUsbDebug);

        Preference entRecoveryFactory = new Preference(requireContext());
        entRecoveryFactory.setTitle("强制恢复出厂设置");
        entRecoveryFactory.setSummary("擦除用户数据。该操作无需用户参与。");
        entRecoveryFactory.setIconSpaceReserved(false);
        entRecoveryFactory.setOnPreferenceClickListener(preference -> {
            SwitchMaterial formatSdcard = new SwitchMaterial(requireContext());
            formatSdcard.setUseMaterialThemeColors(true);
            formatSdcard.setText("是否同时格式化sdcard");
            formatSdcard.setPadding(45,0,45,0);
            formatSdcard.setDefaultFocusHighlightEnabled(true);
            formatSdcard.setChecked(sharedPreferences.getBoolean("formatSdcard",false));
            formatSdcard.setOnCheckedChangeListener((buttonView, isChecked) -> sharedPreferences.edit().putBoolean("formatSdcard", isChecked).apply());
            new MaterialAlertDialogBuilder(requireContext()).setTitle("此操作不可逆, 将强制恢复出厂").setView(formatSdcard)
                    .setPositiveButton("确认重置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                deviceOptService.recoveryFactory(sharedPreferences.getBoolean("formatSdcard",false));
                            } catch (RemoteException e) {
                                throw new RuntimeException(e);
                            }
                        }
                    })
                    .setNegativeButton("取消", null).create().show();
            return isCanUse;
        });
        deviceRestrictionCategory.addPreference(entRecoveryFactory);

        ArrayMap<Integer,String> sRestrictMode = AnyRestrictPolicyUtils.sAppRestrictModeArray;
        List<String> value = new ArrayList<>(sRestrictMode.values());
        // 使用 Stream API 将整数列表转换字符串列表
        List<String> key = new ArrayList<>(sRestrictMode.keySet().stream().map(String::valueOf).collect(Collectors.toList()));

        SimpleMenuPreference browserRestriction = new SimpleMenuPreference(requireContext());
        browserRestriction.setTitle("设置浏览器限制模式");
        browserRestriction.setIconSpaceReserved(false);
        browserRestriction.setKey(AnyRestrictPolicyUtils.sEntDeviceRestrictionArray.get("HOST_RESRTICTION_MODE"));
        browserRestriction.setDefaultValue(String.valueOf(AnyRestrictPolicyUtils.getInt(browserRestriction.getKey())));
        browserRestriction.setEntries(value.toArray(new CharSequence[value.size()]));
        browserRestriction.setEntryValues(key.toArray(new CharSequence[key.size()]));
        browserRestriction.setSummaryProvider(preference -> AnyRestrictPolicyUtils.sAppRestrictModeArray.get(AnyRestrictPolicyUtils.getInt(preference.getKey())));
        browserRestriction.setOnPreferenceChangeListener((preference, newValue) -> {
            try {
                deviceOptService.setBrowserRestriction(Integer.parseInt((String) newValue));
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
            return isCanUse;
        });
        deviceRestrictionCategory.addPreference(browserRestriction);

        Preference reboot = new Preference(requireContext());
        reboot.setTitle("强制重启设备");
        reboot.setIconSpaceReserved(false);
        reboot.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                try {
                    deviceOptService.deviceReboot();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                return deviceOptService != null;
            }
        });
        deviceRestrictionCategory.addPreference(reboot);

        Preference deviceShutDown = new Preference(requireContext());
        deviceShutDown.setTitle("强制关闭设备");
        deviceShutDown.setIconSpaceReserved(false);
        deviceShutDown.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(@NonNull Preference preference) {
                try {
                    deviceOptService.deviceShutDown();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                return deviceOptService != null;
            }
        });
        deviceRestrictionCategory.addPreference(deviceShutDown);

        setPreferenceScreen(preferenceScreen);
    }

    public String generateListSettings(List<String> value) {
        StringBuilder sb = new StringBuilder();
        if (value == null) {
            value = new ArrayList();
        }
        for (String single : value) {
            sb.append(single).append(";");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
    private Drawable getAppIconForPackageName(String packageName) {
        return packageManager.getApplicationInfo(packageName).loadIcon(requireContext().getPackageManager());
    }

    private void showDialog(String title, String msg, View view, DialogInterface.OnClickListener positive){
        new MaterialAlertDialogBuilder(requireContext()).setTitle(title).setMessage(msg).setView(view)
                .setPositiveButton("确定", positive).setNegativeButton("取消", null).create().show();
    }

    public ArrayMap<String, String> getHomeAppLists(){
        ArrayMap<String,String> arrayMap = new ArrayMap<>();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.intent.action.MAIN");
        intentFilter.addCategory("android.intent.category.HOME");
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        List<ResolveInfo> queryIntentActivities = packageManager.queryIntentActivities(intent,
                null,
                android.content.pm.PackageManager.MATCH_ALL);
        for (ResolveInfo resolveInfo : queryIntentActivities){
            String packageName = resolveInfo.activityInfo.packageName;
            if (!packageName.equals("com.android.settings")) {
                arrayMap.put(packageName, packageManager.getAppNameForPackageName(requireContext(),packageName));
            }
        }
        return arrayMap;
    }
}
