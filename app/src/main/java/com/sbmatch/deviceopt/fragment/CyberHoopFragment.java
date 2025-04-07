package com.sbmatch.deviceopt.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.SuspendDialogInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.sbmatch.deviceopt.utils.ReflectUtils;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.DevicePolicyManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.PackageManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.ServiceManager;
import com.sbmatch.deviceopt.utils.ToastUtils;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import ma.DeviceOptimizeHelper.IUserService;
import rikka.material.preference.MaterialSwitchPreference;
import rikka.preference.MainSwitchPreference;
import rikka.preference.SimpleMenuPreference;
import rikka.widget.mainswitchbar.OnMainSwitchChangeListener;

public class CyberHoopFragment extends PreferenceFragmentCompat implements OnMainSwitchChangeListener, Preference.OnPreferenceChangeListener {
    private PreferenceScreen preferenceScreen;
    private IUserService userService;
    private static final Handler mHandle = new Handler(Looper.getMainLooper());
    private final DevicePolicyManager dpm = DevicePolicyManager.get();
    private final PackageManager packageManager = ServiceManager.getPackageManager();
    private final ConcurrentHashMap<CharSequence,CharSequence> DisableMode = new ConcurrentHashMap<>() {{
        put("通用模式一", "0");
        put("通用模式二", "1");
        put("小米专用模式", "4");
    }};

    private SuspendDialogInfo dialogInfo;
    private final Set<String> mDisableRunPackages = new HashSet<String>(){{
        add("com.tencent.tmgp"); // 腾讯游戏包名前缀
        add("com.tencent.lolm");
        add("com.tencent.mf.uam");
        add("com.tencent.letsgo");
        add("com.tencent.nfsonline");
        add("com.tencent.KiHan");
        add("com.netease.party"); // 蛋仔派对
        add("com.netease.l22"); // 永劫无间
        add("com.netease.sky"); // 光遇
        add("com.netease.dfjs"); //巅峰极速
        add("com.hypergryph.arknights"); // 明日方舟
        add("com.miHoYo"); // 米哈游包名前缀
        add("com.bf.sgs.hdexp"); // 三国杀
        add("com.yoka"); // 游卡
        add("com.ea");
    }};
    private final Set<String> packageNameList = new HashSet<>();

    private final BroadcastReceiver PackageBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            // 预留一个bug: 当同时卸载多个应用时 此处应该崩溃

            String pkgName = intent.getDataString().split(":")[1];
            Log.i("PackageBroadcast", pkgName);

            if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) {
                Set<String> tempSet = MMKV.defaultMMKV().getStringSet("appItemList", null);
                Log.i(getTag(), "remove pkg " + pkgName);
                // 如果应用被卸载了, 同步移除对应Preference
                traversePreferences(findPreference("appInstalledList")).forEach(item -> {
                  if (item.equals(pkgName)) {
                      tempSet.remove(pkgName);
                      MMKV.defaultMMKV().putStringSet("appItemList", tempSet);
                      preferenceScreen.removePreference(findPreference(item));
                  }
                });
            }
//            if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)){
//                Set<String> tempSet2 = MMKV.defaultMMKV().getStringSet("appItemList", null);
//                Log.i(getTag(), "add pkg "+pkgName);
//                tempSet2.add(pkgName);
//                MMKV.defaultMMKV().putStringSet("appItemList", tempSet2);
//            }

        }
    };
    private final IntentFilter intentFilterFromPkg = new IntentFilter(){{
        addAction(Intent.ACTION_PACKAGE_REMOVED);
        addAction(Intent.ACTION_PACKAGE_ADDED);
        addDataScheme("package");
        setPriority(Integer.MAX_VALUE);
    }};
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        SuspendDialogInfo.Builder sdb = new SuspendDialogInfo.Builder();
        sdb.setTitle("提示");
        sdb.setMessage("此应用被禁止启动");
        dialogInfo = sdb.build();

        IBinder iBinder = requireArguments().getBinder("serviceBinder_S");

        if (iBinder != null && iBinder.pingBinder()) {
            userService = IUserService.Stub.asInterface(iBinder);
            ToastUtils.toast("准备就绪");
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireContext().registerReceiver(PackageBroadcast, intentFilterFromPkg);

        if (preferenceScreen == null) preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());

        // 创建首选项分类
        PreferenceCategory mCategory = new PreferenceCategory(requireContext());
        mCategory.setIconSpaceReserved(false);
        PreferenceCategory runMode = new PreferenceCategory(requireContext());
        runMode.setIconSpaceReserved(false);
        runMode.setTitle("运行模式");

        PreferenceCategory mAppListCategory = new PreferenceCategory(requireContext());
        mAppListCategory.setIconSpaceReserved(false);
        mAppListCategory.setTitle("已安装的游戏列表");
        mAppListCategory.setKey("appInstalledList");
        // 将动态生成的分类添加进首选项的根布局中
        preferenceScreen.addPreference(mCategory);
        preferenceScreen.addPreference(runMode);
        preferenceScreen.addPreference(mAppListCategory);

        SimpleMenuPreference restrictMode = new SimpleMenuPreference(requireContext());
        restrictMode.setTitle("限制模式");
        restrictMode.setKey("restrictMode");
        restrictMode.setEntries(DisableMode.keySet().toArray(new CharSequence[0]));
        restrictMode.setEntryValues(DisableMode.values().toArray(new CharSequence[0]));
        restrictMode.setDefaultValue("4");
        restrictMode.setSummaryProvider(preference -> ((SimpleMenuPreference)preference).getEntry());
        restrictMode.setOnPreferenceChangeListener((preference, newValue) -> {
            Log.i(this.getTag(), ((SimpleMenuPreference)preference).getEntry()+" -> "+newValue);
            MMKV.defaultMMKV().putInt("last_restrict_mode", Integer.parseInt(((SimpleMenuPreference)preference).getValue()));
            Logger.getGlobal().info("上一次的限制模式: "+MMKV.defaultMMKV().getInt("last_restrict_mode", -1));
            MMKV.defaultMMKV().putInt(preference.getKey(), Integer.parseInt((String) newValue));
            String[] waitOpList = traversePreferences(findPreference("appInstalledList")).stream().filter(s -> ((MaterialSwitchPreference) findPreference(s)).isChecked()).toArray(String[]::new);

            if (MMKV.defaultMMKV().getBoolean("mainStatus", false)){
                switch (Integer.parseInt((String) newValue)) {
                    case 0 ->
                            ReflectUtils.callObjectMethod2(userService, "setPackagesSuspended", waitOpList, true, dialogInfo);
                    case 1 -> dpm.setPackagesSuspended(waitOpList, true, null);
                    case 4 -> {
                        ReflectUtils.callObjectMethod2(userService, "setBlackListEnable", true);
                        ReflectUtils.callObjectMethod2(userService, "setDisallowRunningList", Arrays.asList(waitOpList));
                    }
                }
            }
            return true;
        });
        runMode.addPreference(restrictMode);
        MMKV.defaultMMKV().putInt(restrictMode.getKey(), Integer.parseInt(restrictMode.getValue()));
        MainSwitchPreference mainSwitchPref = new MainSwitchPreference(requireContext());
        mainSwitchPref.setKey("mainStatus");
        mainSwitchPref.setTitle(getString(ReflectUtils.getResIdReflect("cyberhoop_page")));
        mainSwitchPref.addOnSwitchChangeListener(this);
        mCategory.addPreference(mainSwitchPref);

        for (ApplicationInfo appInfo : packageManager.getInstalledApplications(android.content.pm.PackageManager.MATCH_ALL)){
            if (mDisableRunPackages.stream().anyMatch(appInfo.packageName::contains)){
                packageNameList.add(appInfo.packageName);
                MaterialSwitchPreference gameAppPref = new MaterialSwitchPreference(requireContext());
                gameAppPref.setTitle(packageManager.getAppNameForPackageName(appInfo.packageName));
                gameAppPref.setIcon(appInfo.loadIcon(requireContext().getPackageManager()));
                gameAppPref.setKey(appInfo.packageName);
                mHandle.postDelayed(() -> gameAppPref.setDependency("mainStatus"), 100);
                gameAppPref.setSummaryProvider(preference -> {
                    if (packageManager.getPackageInfo(preference.getKey()) != null) {
                        return packageManager.getPackageInfo(preference.getKey()).versionName;
                    }
                    return null;
                });
                gameAppPref.setOnPreferenceChangeListener(this);
                mAppListCategory.addPreference(gameAppPref);
            }
        }

        MMKV.defaultMMKV().putStringSet("appItemList", packageNameList);

        setPreferenceScreen(preferenceScreen);

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        requireContext().unregisterReceiver(PackageBroadcast);
    }

    @Override
    public void onSwitchChanged(Switch switchView, boolean isChecked) {
        MMKV.defaultMMKV().putBoolean("mainStatus", isChecked);
        switch (MMKV.defaultMMKV().getInt("restrictMode",-1)){
            case 0:
                ReflectUtils.callObjectMethod2(userService, "setPackagesSuspended", MMKV.defaultMMKV().getStringSet("appItemList", null).toArray(new String[0]), isChecked, dialogInfo);
                break;
            case 1:
                dpm.setPackagesSuspended(MMKV.defaultMMKV().getStringSet("appItemList", null).toArray(new String[0]), isChecked, null);
                break;
            case 4:
                ReflectUtils.callObjectMethod2(userService, "setBlackListEnable", isChecked);
                break;
        }

        if (isChecked) {
            ReflectUtils.callObjectMethod2(userService, "addUserRestriction",  "no_install_apps");
        }else {
            ReflectUtils.callObjectMethod2(userService, "clearUserRestriction","no_install_apps");
        }

        traversePreferences(findPreference("appInstalledList")).forEach(item -> {
            ReflectUtils.callObjectMethod2(userService, "setBlockUninstallForUser", item ,isChecked);
            ReflectUtils.callObjectMethod2(userService,"forceStopPackage", item);
            ((MaterialSwitchPreference)findPreference(item)).setChecked(isChecked);
        });

    }

    @Override
    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
        String key = preference.getKey();
        Set<String> list = traversePreferences(findPreference("appInstalledList")).stream().filter(s -> ((MaterialSwitchPreference) findPreference(s)).isChecked()).collect(Collectors.toSet());

        switch (MMKV.defaultMMKV().getInt("restrictMode",-1)){
            case 0:
                ReflectUtils.callObjectMethod2(userService, "setPackagesSuspended", new String[]{preference.getKey()}, newValue, dialogInfo);
                break;
            case 1:
                dpm.setPackagesSuspended(new String[]{preference.getKey()}, (Boolean) newValue, null);
                break;
            case 4:
                ReflectUtils.callObjectMethod2(userService, "setBlackListEnable", true);
                ReflectUtils.callObjectMethod2(userService, "setDisallowRunningList", new ArrayList<>(list));
                break;
        }
        ReflectUtils.callObjectMethod2(userService, "setBlockUninstallForUser", key , newValue);
        ReflectUtils.callObjectMethod2(userService,"forceStopPackage", key);
        return true;
    }


    /**
     *
     * 遍历指定 PreferenceCategory 中所有 Preference
     *
     * @return 返回Set集合, 其中包含 Preference key
     */
    public Set<String> traversePreferences(PreferenceCategory preferenceCategory) {

        Set<String> tempSet = new HashSet<>();
        for (int i = 0; i < preferenceCategory.getPreferenceCount(); i++) {
            Preference preference = preferenceCategory.getPreference(i);

            if (preference instanceof PreferenceCategory) {
                traversePreferences((PreferenceCategory) preference);
            }

            if (preference instanceof MaterialSwitchPreference){
                tempSet.add(preference.getKey());
            }
        }
        return tempSet;
    }

}