package com.sbmatch.deviceopt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.kongzue.baseframework.BaseActivity;
import com.kongzue.baseframework.interfaces.FragmentLayout;
import com.kongzue.baseframework.interfaces.Layout;
import com.kongzue.baseframework.util.JumpParameter;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener;
import com.sbmatch.deviceopt.Interface.DhizukuUserServiceFactory;
import com.sbmatch.deviceopt.Interface.OnBinderCallbackListener;
import com.sbmatch.deviceopt.Utils.CloudCheckAppUpdateManager;
import com.sbmatch.deviceopt.Utils.CommandExecutor;
import com.sbmatch.deviceopt.Utils.FilesUtils;
import com.sbmatch.deviceopt.Utils.NotificationHelper;
import com.sbmatch.deviceopt.Utils.ReflectUtils;
import com.sbmatch.deviceopt.Utils.SystemServiceWrapper.DevicePolicyManager;
import com.sbmatch.deviceopt.ViewModel.ViewModelUtils;
import com.sbmatch.deviceopt.activity.CyberHoopActivity;
import com.sbmatch.deviceopt.activity.SettingsActivity;
import com.sbmatch.deviceopt.activity.base.BaseApplication;
import com.sbmatch.deviceopt.fragment.UserRestrictFragment;
import com.tencent.mmkv.MMKV;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import ma.DeviceOptimizeHelper.BuildConfig;
import ma.DeviceOptimizeHelper.IUserService;
import ma.DeviceOptimizeHelper.R;

@SuppressLint("NonConstantResourceId")
@Layout(R.layout.main_activity)
@FragmentLayout(R.id.user_restrict_policy)
public class MainActivity extends BaseActivity {
    private final CommandExecutor commandExecutor = CommandExecutor.get();
    private ViewModelUtils viewModelU;
    private final Bundle bundle = new Bundle();
    private final Logger logger = Logger.getGlobal();
    private static final NotificationHelper notificationHelper = NotificationHelper.getInstance();

    @Override
    public void initViews() {
        setupActionBar();
        DynamicColors.applyToActivityIfAvailable(this);
        // 让主题跟随系统
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

    }

    @Override
    public void initDatas(JumpParameter parameter) {

        viewModelU = new ViewModelProvider(this).get(ViewModelUtils.class);

        CloudCheckAppUpdateManager.Init("https://giaosha.lanzoul.com/b07jrp2yf", "4d24");

        if (isLightMode()) setDarkStatusBarTheme(true);

        if (getSupportFragmentManager().findFragmentByTag("userRestrict") == null){
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.user_restrict_policy, UserRestrictFragment.class, bundle, "userRestrict")
                    .commit();
        }

        if (!Dhizuku.isPermissionGranted()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("需要Dhizuku权限")
                    .setMessage("本应用需要Dhizuku权限才能正常运行。Dhizuku权限允许应用执行一些系统级别的操作，例如禁用某些系统功能。\n\n请授予Dhizuku权限。")
                    .setPositiveButton("前往授权", (dialog, which) -> {
                        tryRequestsDhizukuPermission();
                        dialog.dismiss();
                    })
                    .setNegativeButton("稍后再说", null)
                    .setCancelable(false)
                    .show();
        }else {
             DhizukuUserServiceFactory.getInstance().bindUserService(new OnBinderCallbackListener() {
                @Override
                public void onUserServiceReady(IBinder service, String ImplClass) throws RemoteException {
                    logger.info("onUserServiceReady: "+ImplClass);
                    IUserService userService = IUserService.Stub.asInterface(service);
                    try {
                        PopTip.show("invoke method isPackageSuspended "+userService.isPackageSuspended(BuildConfig.APPLICATION_ID));
                    } catch (RemoteException e) {
                        logger.severe(Log.getStackTraceString(e));
                    }
                }
                @Override
                public void onUserServiceDisconnected(String ImplClass) throws RemoteException {

                }
            });
        }



    }

    @Override
    public void setEvents() {

    }

    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setBackgroundDrawable(null);
        }
    }

    public boolean isLightMode() {
        int nightModeFlags = AppGlobals.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        return nightModeFlags != Configuration.UI_MODE_NIGHT_YES;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 10000, 0, ReflectUtils.getResIdReflect("enable_all_policy"));
        menu.add(Menu.NONE, 10001, 1, ReflectUtils.getResIdReflect("disallow_all_policy"));
        menu.add(Menu.NONE, 10002, 2, ReflectUtils.getResIdReflect("share_runtime_logs"));

        Intent cyberHoopActivity = new Intent(this, CyberHoopActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //menu.add(Menu.NONE, 10010, 3, ReflectUtils.getResIdReflect("cyberhoop_page")).setIntent(cyberHoopActivity);

        Intent settingActivity = new Intent(this, SettingsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        menu.add(Menu.NONE, 10011, 10, ReflectUtils.getResIdReflect("setting")).setIntent(settingActivity);

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);

        MenuItem search = menu.findItem(R.id.search_menu);

        androidx.appcompat.widget.SearchView searchView = (androidx.appcompat.widget.SearchView) search.getActionView();
        if (searchView != null) {
            searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    UserRestrictFragment fragment = (UserRestrictFragment) getSupportFragmentManager()
                            .findFragmentByTag("userRestrict");
                    if (fragment != null) fragment.getFilter().filter(newText);
                    return true;
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case 10000 -> viewModelU.savePreferenceSwitchNewStatus(true);
            case 10001 -> viewModelU.savePreferenceSwitchNewStatus(false);
            case 10002 -> share_runtime_logs();
        }
        return super.onOptionsItemSelected(item);
    }


    private void share_runtime_logs() {
        // -b main 是指只显示主日志缓冲区（main buffer）的日志。主日志缓冲区包含了系统启动以来的所有核心系统日志。
        // -b crash 是指只显示崩溃日志缓冲区（crash buffer）的日志。这个缓冲区包含了系统崩溃或ANR（Application Not Responding）时的日志。
        // -d 是指倒序输出（descending order）。这意味着新的日志条目将首先显示，旧的条目将后显示。
        commandExecutor.executeCommand("logcat -v threadtime -b crash -b main -d ", new CommandExecutor.CommandResultListener() {
            @Override
            public void onSuccess(String output) {
                FilesUtils.writeToFile(BaseApplication.getLogFile("runtime").getAbsolutePath(), output, true);
                // 使用系统分享发送文件
                Intent intent = new Intent(Intent.ACTION_SEND);
                // 设置分享文件的类型
                intent.setType("text/plain");
                // 获取最新的文件
                File shareFile = FilesUtils.getLatestFileInDirectory(MMKV.defaultMMKV().decodeString("logPath"));
                // 将文件转换为Uri
                intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getApplication(), "ma.DeviceOptimizeHelper.provider", shareFile));
                // 添加权限
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                // 启动分享
                startActivity(intent);
            }

            @Override
            public void onError(String error) {
                BottomDialog.show("Error", error).setCancelButton("我知道了");
            }
        }, false);
    }



    public void tryRequestsDhizukuPermission() {
        try {
            Dhizuku.requestPermission(new DhizukuRequestPermissionListener() {
                @Override
                public void onRequestPermission(int grantResult) {
                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                        // 获得所有DelegatedScopes授权委托
                        String[] delegatedScopes = DevicePolicyManager.getDelegationScopesWithReflect().toArray(new String[0]);
                        Dhizuku.setDelegatedScopes(delegatedScopes);
                        MMKV.defaultMMKV().putString("_execMode", "dpm");
                        AppGlobals.sMainHandler.post((() -> PopTip.show("已授权, 欢迎使用")));
                    }
                }
            });
        } catch (Exception e) {
            toast("Dhizuku 未安装或未激活");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 999 && Arrays.stream(grantResults).anyMatch(g -> g == 0)){
            notificationHelper.createNotificationChannel("msgToast", "异常通知", 4, null);
        }
    }

}