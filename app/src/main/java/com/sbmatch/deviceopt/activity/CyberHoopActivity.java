package com.sbmatch.deviceopt.activity;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.SuspendDialogInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.collection.ArraySet;
import androidx.core.app.NavUtils;
import androidx.core.content.PermissionChecker;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.sbmatch.deviceopt.Interface.OnUserServiceCallbackListener;
import com.sbmatch.deviceopt.Interface.ShizukuUserServiceFactory;
import com.sbmatch.deviceopt.Utils.ReflectUtil;
import com.sbmatch.deviceopt.Utils.SystemServiceWrapper.ServiceManager;
import com.sbmatch.deviceopt.Utils.ToastUtils;
import com.sbmatch.deviceopt.ViewModel.InterfaceViewModel;
import com.tencent.mmkv.MMKV;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import ma.DeviceOptimizeHelper.IUserService;
import ma.DeviceOptimizeHelper.R;
import rikka.lifecycle.ViewModelLazy;
import rikka.material.preference.MaterialSwitchPreference;
import rikka.preference.MainSwitchPreference;
import rikka.preference.SimpleMenuPreference;
import rikka.shizuku.Shizuku;
import rikka.widget.mainswitchbar.OnMainSwitchChangeListener;

public class CyberHoopActivity extends AppCompatActivity implements Shizuku.OnRequestPermissionResultListener, OnUserServiceCallbackListener{

    private final static String TAG = CyberHoopActivity.class.getSimpleName();
    private final static Handler mMainHandler = new Handler(Looper.getMainLooper());
    private final Shizuku.OnRequestPermissionResultListener REQUEST_PERMISSION_RESULT_LISTENER = this;
    private static IUserService userService;

    private final ShizukuUserServiceFactory shizukuUserServiceFactory = new ShizukuUserServiceFactory();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cyberhoop_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(null);
        }

        Shizuku.addRequestPermissionResultListener(REQUEST_PERMISSION_RESULT_LISTENER);
        shizukuUserServiceFactory.setOnUserServiceCallbackListener(this);

        new ServiceThread("魔仙堡执勤中").start();

        try {
            if (Shizuku.checkSelfPermission() != PackageManager.PERMISSION_GRANTED) {
                Shizuku.requestPermission(4718);
            }
        } catch (Exception e) {
            ToastUtils.toast("Shizuku 未安装或未激活");
        }

        if (userService == null && Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) shizukuUserServiceFactory.bindUserService();

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

    @Override
    public void onRequestPermissionResult(int requestCode, int grantResult) {
        if (grantResult == PermissionChecker.PERMISSION_GRANTED) {
            switch (requestCode) {
                case 4718:
                    shizukuUserServiceFactory.bindUserService();
                    ToastUtils.toast("Shizuku 已授权");
                    break;
            }
        }
    }

    @Override
    public void onUserServiceReady(IBinder service) {
        if (service != null && service.pingBinder()) {
            userService = IUserService.Stub.asInterface(service);
            ToastUtils.toast("准备就绪");
        }
    }


    public static class SettingsFragment extends PreferenceFragmentCompat implements OnMainSwitchChangeListener, Preference.OnPreferenceChangeListener {
        private PreferenceScreen preferenceScreen;
        private final com.sbmatch.deviceopt.Utils.SystemServiceWrapper.PackageManager packageManager = ServiceManager.getPackageManager;
        private static final ArraySet<String> packageNameList = new ArraySet<>();
        private static final ConcurrentHashMap<CharSequence,CharSequence> DisableMode = new ConcurrentHashMap<CharSequence, CharSequence>(){{
            put("通用模式一", "0");
            put("小米专用模式", "4");
        }};

        private SuspendDialogInfo dialogInfo;
        private final ArrayList<String> mDisableRunPackages = new ArrayList<>();
        private final MMKV disableMode_mmkv = MMKV.mmkvWithID("disable_mode");

        static {
            packageNameList.add("com.tencent.tmgp"); // 腾讯游戏包名前缀
            packageNameList.add("com.tencent.lolm");
            packageNameList.add("com.tencent.mf.uam");
            packageNameList.add("com.tencent.letsgo");
            packageNameList.add("com.tencent.nfsonline");
            packageNameList.add("com.tencent.KiHan");
            packageNameList.add("com.netease.party"); // 蛋仔派对
            packageNameList.add("com.netease.l22"); // 永劫无间
            packageNameList.add("com.netease.sky"); // 光遇
            packageNameList.add("com.netease.dfjs"); //巅峰极速
            packageNameList.add("com.hypergryph.arknights"); // 明日方舟
            packageNameList.add("com.miHoYo"); // 米哈游包名前缀
            packageNameList.add("com.bf.sgs.hdexp"); // 三国杀
            packageNameList.add("com.yoka"); // 游卡
            packageNameList.add("com.ea");
        }

        @Override
        public void onResume() {
            super.onResume();
            if (mDisableRunPackages.size() > 0){
                mDisableRunPackages.removeIf(pkg -> {
                    if (packageManager.getPackageInfo(pkg) == null)
                        preferenceScreen.removePreference(findPreference(pkg));
                    return packageManager.getPackageInfo(pkg) == null;
                });
            }
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            SuspendDialogInfo.Builder sdb = new SuspendDialogInfo.Builder();
            sdb.setTitle("提示");
            sdb.setMessage("此应用被禁止启动");
            dialogInfo = sdb.build();
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

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
            restrictMode.setEnabled(true);
            restrictMode.setSummaryProvider(preference -> ((SimpleMenuPreference)preference).getEntry());
            restrictMode.setOnPreferenceChangeListener((preference, newValue) -> {
                Log.i(this.getTag(), ((SimpleMenuPreference)preference).getEntry()+" -> "+newValue);
                disableMode_mmkv.putInt(preference.getKey(), Integer.parseInt((String) newValue));
                if (((MainSwitchPreference) findPreference("mainStatus")).isChecked()) {
                    ((MainSwitchPreference) findPreference("mainStatus")).setChecked(false);
                    switch (Integer.parseInt((String) newValue)) {
                        case 0:
                            ReflectUtil.callObjectMethod2(userService, "setBlackListEnable", false);
                            break;
                        case 4:
                            ReflectUtil.callObjectMethod2(userService, "setPackagesSuspended", mDisableRunPackages.toArray(new String[0]), false, dialogInfo);
                            break;
                    }
                }
                ToastUtils.toast("已切换模式, 请启用紧箍咒");

                return true;
            });
            runMode.addPreference(restrictMode);
            disableMode_mmkv.putInt(restrictMode.getKey(), Integer.parseInt(restrictMode.getValue()));
            MainSwitchPreference mainSwitchPref = new MainSwitchPreference(requireContext());
            mainSwitchPref.setKey("mainStatus");
            mainSwitchPref.setTitle(getString(R.string.cyberhoop_page));
            mainSwitchPref.addOnSwitchChangeListener(this);
            mCategory.addPreference(mainSwitchPref);

            for (ApplicationInfo appInfo : packageManager.getInstalledApplications(PackageManager.MATCH_ALL)){
                if (packageNameList.stream().anyMatch(appInfo.packageName::contains)){
                    mDisableRunPackages.add(appInfo.packageName);
                    MaterialSwitchPreference gameAppPref = new MaterialSwitchPreference(requireContext());
                    gameAppPref.setTitle(packageManager.getAppNameForPackageName(appInfo.packageName));
                    gameAppPref.setIcon(appInfo.loadIcon(requireContext().getPackageManager()));
                    gameAppPref.setKey(appInfo.packageName);
                    gameAppPref.setSummaryProvider(preference -> packageManager.getPackageInfo(preference.getKey()).versionName);
                    gameAppPref.setOnPreferenceChangeListener(this);
                    mAppListCategory.addPreference(gameAppPref);
                }
            }
            setPreferenceScreen(preferenceScreen);

        }

        @Override
        public void onSwitchChanged(Switch switchView, boolean isChecked) {
            disableMode_mmkv.putBoolean("mainStatus", isChecked);
            switch (disableMode_mmkv.getInt("restrictMode",-1)){
                case 0:
                    ReflectUtil.callObjectMethod2(userService, "setPackagesSuspended", mDisableRunPackages.toArray(new String[0]), disableMode_mmkv.getBoolean("mainStatus", false), dialogInfo);
                    break;
                case 4:
                    ReflectUtil.callObjectMethod2(userService, "setBlackListEnable", isChecked);
                    if (isChecked) ReflectUtil.callObjectMethod2(userService, "setDisallowRunningList", mDisableRunPackages);
                    break;
            }

            if (isChecked) {
                ReflectUtil.callObjectMethod2(userService, "addUserRestriction", null, "no_install_apps");
            }else {
                ReflectUtil.callObjectMethod2(userService, "clearUserRestriction", null, "no_install_apps");
            }

            mDisableRunPackages.forEach(item -> {
                ReflectUtil.callObjectMethod2(userService, "setBlockUninstallForUser", item ,isChecked);
                ReflectUtil.callObjectMethod2(userService,"forceStopPackage", item);
                ((MaterialSwitchPreference)findPreference(item)).setChecked(isChecked);
            });
        }

        @Override
        public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
            String key = preference.getKey();
            ArrayList<String> list = (ArrayList<String>) mDisableRunPackages.clone();
            if (!(boolean)newValue){
                list.removeIf(s -> s.equals(key));
            }else {
                // 如果 newValue 为 false，添加键到列表中
                if (!mDisableRunPackages.contains(key)) {
                    mDisableRunPackages.add(key);
                }
            }
            switch (disableMode_mmkv.getInt("restrictMode",-1)){
                case 0:
                    ReflectUtil.callObjectMethod2(userService, "setPackagesSuspended", new String[]{preference.getKey()}, newValue, dialogInfo);
                    break;
                case 4:
                    ReflectUtil.callObjectMethod2(userService, "setDisallowRunningList", list);
                    break;
            }
            ReflectUtil.callObjectMethod2(userService, "setBlockUninstallForUser", key ,newValue);
            boolean mainStatus = disableMode_mmkv.getBoolean("mainStatus", false);
            if (!mainStatus) ToastUtils.toast("请先启用紧箍咒的主开关");
            return mainStatus;
        }
    }

    public static class ServiceThread extends HandlerThread {
        public ServiceThread(String name) {
            super(name);
        }
        @Override
        public synchronized void start() {
            super.start();

        }
    }

}