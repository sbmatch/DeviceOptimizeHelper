package com.ma.enterprisemodepolicymanager;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.ma.enterprisemodepolicymanager.Fragments.ApplicationManagerFragment;
import com.ma.enterprisemodepolicymanager.Fragments.DeviceManagerFragment;
import com.ma.enterprisemodepolicymanager.Utils.CommandExecutor;
import com.ma.enterprisemodepolicymanager.Utils.FilesUtils;
import com.ma.enterprisemodepolicymanager.Utils.ResourcesUtils;
import com.ma.enterprisemodepolicymanager.Utils.ServiceManager;
import com.ma.enterprisemodepolicymanager.ViewModels.FragmentShareIBinder;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.File;

import com.ma.enterprisemodepolicymanager.BaseApplication.BaseApplication;
import com.ma.enterprisemodepolicymanager.Fragments.RestrictionsManagerFragment;
import com.ma.enterprisemodepolicymanager.Utils.AnyRestrictPolicyUtils;


// TODO 注释！！！可以用codegeex或者chatgpt一键生成即可（文心就是垃圾）

// TODO 新功能加注释！！！

// TODO 修bug的提交，请把commit描述写清楚！！！！！！

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final CommandExecutor commandExecutor = CommandExecutor.getInstance();
    public static Handler MainHandle;
    private static FragmentManager fragmentManager;
    private static SharedPreferences sharedPreferences;
    private static ActionBar actionBar;
    private static Intent remoteLinuxProcessBroadcast;
    private static FragmentShareIBinder shareIBinder;
    private static IBinder.DeathRecipient deviceoptIbinder = () -> {
        sharedPreferences.edit().putBoolean("remoteProcessBinderlinkToDeath", false).apply();
        sharedPreferences.edit().putBoolean("remoteProcessBinder", false).apply();
        System.err.println("驱动服务端死亡");
    };;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        // 深色模式适配
        View decorView = getWindow().getDecorView();
        int flags = decorView.getSystemUiVisibility();

        if ((getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES) {
            // 如果是深色模式，则设置状态栏文字为白色
            flags &= ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

        } else {
            // 如果不是深色模式，则设置状态栏文字为黑色
            flags |= View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
        }

        decorView.setSystemUiVisibility(flags);

        actionBar = getSupportActionBar();
        actionBar.setBackgroundDrawable(null);  // 如果ActionBar为空，则设置ActionBar的背景图片为null

        if (sharedPreferences == null) sharedPreferences = getSharedPreferences("main_sharePreference", MODE_PRIVATE);

        sharedPreferences.edit().putBoolean("remoteProcessBinder", false).apply();
        sharedPreferences.edit().putBoolean("remoteProcessBinderlinkToDeath", false).apply();

        fragmentManager = getSupportFragmentManager();
        fragmentManager.setFragmentResultListener("applicationFragment", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (requestKey.equals("applicationFragment"))
                    sharedPreferences.edit().putBoolean("applicationFragment", true).apply();
            }
        });

        if (ServiceManager.getService("EnterpriseManager").pingBinder()) {
            fragmentManager.beginTransaction()
                    .replace(R.id.settings, HeaderFragment.class, null)
                    .setReorderingAllowed(true)
                    .commit();
        }else {
            showDialog(this,"小丑? 一级警报!","您可能没有激活企业模式或者不是MIUI系统.",null);
        }

        // 监听BackStackChanged事件，当BackStack的顺序发生变化时，且栈为0时
        fragmentManager.addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        });

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("ma.deviceOptimizeHelper.deviceOptSendBroadcast");
        intentFilter.setPriority(Integer.MAX_VALUE);

        remoteLinuxProcessBroadcast = registerReceiver(null, intentFilter, RECEIVER_EXPORTED);

        shareIBinder =  new ViewModelProvider(this).get(FragmentShareIBinder.class);

        if (remoteLinuxProcessBroadcast != null){
            System.out.println("已获取广播, 正在解析...");
            BinderContainer deviceOptServiceBinder = remoteLinuxProcessBroadcast.getParcelableExtra("deviceOptServiceBinder");
            BinderContainer enterpriseManagerBinder = remoteLinuxProcessBroadcast.getParcelableExtra("enterpriseManagerBinder");

            if (enterpriseManagerBinder.getBinder().pingBinder()) shareIBinder.setEnterpriseManager(enterpriseManagerBinder);

            if (deviceOptServiceBinder.getBinder().pingBinder()) {
                System.out.println("已获取远程代理驱动对象...");
                shareIBinder.setDeviceOptService(deviceOptServiceBinder);
                sharedPreferences.edit().putBoolean("remoteProcessBinder", true).apply();
                if (!sharedPreferences.getBoolean("remoteProcessBinderlinkToDeath", false)) {
                    try {
                        deviceOptServiceBinder.getBinder().linkToDeath(deviceoptIbinder, 0);
                        sharedPreferences.edit().putBoolean("remoteProcessBinderlinkToDeath", true).apply();
                    } catch (RemoteException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }else {
            Toast.makeText(this, "服务端进程未运行...", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 10002, 2, ResourcesUtils.getResIdReflect("share_runtime_logs"));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == 10002) shareLogs();
        if (item.getItemId() == android.R.id.home) // 返回到appListFragment
            fragmentManager.popBackStack(); // 弹出Fragment回退栈

        return true;
    }

    private static void showDialog(Context context,String title, String msg, DialogInterface.OnClickListener positive){
        new MaterialAlertDialogBuilder(context).setTitle(title).setMessage(msg)
                .setPositiveButton("确定", positive).setNegativeButton("取消", null).create().show();
    }

    private void shareLogs() {
        // -b main 是指只显示主日志缓冲区（main buffer）的日志。主日志缓冲区包含了系统启动以来的所有核心系统日志。
        // -b crash 是指只显示崩溃日志缓冲区（crash buffer）的日志。这个缓冲区包含了系统崩溃或ANR（Application Not Responding）时的日志。
        // -d 是指倒序输出（descending order）。这意味着新的日志条目将首先显示，旧的条目将后显示。
        commandExecutor.executeCommand("logcat -b crash -d", new CommandExecutor.CommandResultListener() {
            @Override
            public void onSuccess(String output) {
                Log.e("CrashInfo", output);
                // 使用系统分享发送文件
                Intent intent = new Intent(Intent.ACTION_SEND);
                // 设置分享文件的类型
                intent.setType("text/plain");
                // 获取最新的文件
                File shareFile = FilesUtils.getLatestFileInDirectory(BaseApplication.getLogsDir(getBaseContext()).getAbsolutePath());
                // 添加权限
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                if (output.isEmpty()) {
                    if (shareFile != null) {
                        // 将文件转换为Uri
                        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getBaseContext(), "ma.DeviceOptimizeHelper.provider", shareFile));
                        // 启动分享
                        getApplicationContext().startActivity(intent);
                    } else {
                        Looper.prepare();
                        Toast.makeText(BaseApplication.getContext(), "暂无崩溃日志", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    intent.putExtra(Intent.EXTRA_TEXT, output);
                    BaseApplication.getContext().startActivity(intent);
                }
            }

            @Override
            public void onError(String error, Exception e) {
                e.printStackTrace();

            }
        }, false, false);
    }


    public static class HeaderFragment extends PreferenceFragmentCompat {
        private PreferenceScreen preferenceScreen;
        @Override
        public void onCreate(@Nullable Bundle savedInstanceState) {

            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            if (preferenceScreen == null || getPreferenceManager().getSharedPreferences() == null)
                preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());

            PreferenceCategory mainCategory = new PreferenceCategory(requireContext());
            mainCategory.setIconSpaceReserved(false);
            mainCategory.setSummary("企业SDK版本: "+ AnyRestrictPolicyUtils.getAPIVersion());
            preferenceScreen.addPreference(mainCategory);

            Preference entRestrict = new Preference(requireContext());
            entRestrict.setKey("entRestrictSysFeature");
            entRestrict.setIconSpaceReserved(false);
            entRestrict.setTitle("系统功能管控");
            entRestrict.setSummary("飞行模式、蓝牙、加速度传感器、自动云同步、系统备份、相机、恢复出厂设置、指纹传感器、IMEI读取、录音功能、MTP功能、OTG功能、截屏功能、外置SD卡挂载、网络共享（包括蓝牙，WiFi，usb）、修改系统时间、USB调试功能、VPN功能、GPS功能、NFC功能、WiFi功能。");
            entRestrict.setOnPreferenceClickListener(preference -> {
                if (sharedPreferences.getBoolean("remoteProcessBinder", false)){

                    actionBar.setDisplayHomeAsUpEnabled(true);
                    if (getParentFragmentManager().findFragmentByTag("restrict") == null) {
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.settings, RestrictionsManagerFragment.class, null,"restrict")
                                .setReorderingAllowed(true).addToBackStack(null)
                                .commit();
                    }else {
                        System.out.println("从堆栈中获取fragment...");
                        getParentFragmentManager().popBackStack("restrict",  FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }else {
                    Toast.makeText(requireContext(), "服务未运行...", Toast.LENGTH_SHORT).show();
                }
              return true;
            });
            mainCategory.addPreference(entRestrict);

            Preference entAppRestrict = new Preference(requireContext());
            entAppRestrict.setKey("entAppRestrict");
            entAppRestrict.setIconSpaceReserved(false);
            entAppRestrict.setTitle("应用管控");
            entAppRestrict.setSummary("静默安装卸载、清除应用数据、清除应用缓存、运行时权限授予、防卸载、应用保活、应用安装黑白名单、静默激活注销设备管理器、静默激活注销辅助服务功能、杀应用进程、清除最近任务、应用运行黑白名单、添加可信应用市场。");
            entAppRestrict.setOnPreferenceClickListener(preference -> {
                if (sharedPreferences.getBoolean("remoteProcessBinder", false)){

                    actionBar.setDisplayHomeAsUpEnabled(true);
                    if (getParentFragmentManager().findFragmentByTag("application") == null){
                        getParentFragmentManager().beginTransaction()
                                .replace(R.id.settings, ApplicationManagerFragment.class ,null, "application")
                                .setReorderingAllowed(true).addToBackStack(null)
                                .commit();
                    }else {
                        System.out.println("从堆栈中获取fragment...");
                        getParentFragmentManager().popBackStack("application",  FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    }
                }else {
                    Toast.makeText(requireContext(), "服务未运行...", Toast.LENGTH_SHORT).show();
                }
                return true;
            });
            mainCategory.addPreference(entAppRestrict);

            Preference entDeviceRestrict = new Preference(requireContext());
            entDeviceRestrict.setKey("entDeviceRestrict");
            entDeviceRestrict.setIconSpaceReserved(false);
            entDeviceRestrict.setTitle("设备管控");
            entDeviceRestrict.setSummary("获取当前设备Root情况、强制关闭设备、强制重启设备、恢复出厂设置、格式化外部sd卡（如果存在）、WiFi黑白名单、截屏。");
            entDeviceRestrict.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(@NonNull Preference preference) {
                    if (sharedPreferences.getBoolean("remoteProcessBinder", false)){

                        actionBar.setDisplayHomeAsUpEnabled(true);
                        if (getParentFragmentManager().findFragmentByTag("device") == null){
                            getParentFragmentManager().beginTransaction()
                                    .replace(R.id.settings, DeviceManagerFragment.class,null, "device")
                                    .setReorderingAllowed(true).addToBackStack(null)
                                    .commit();
                        }else {
                            System.out.println("从堆栈中获取fragment...");
                            getParentFragmentManager().popBackStack("device",FragmentManager.POP_BACK_STACK_INCLUSIVE);
                        }
                    }else {
                        Toast.makeText(requireContext(), "服务未运行...", Toast.LENGTH_SHORT).show();
                    }
                    return true;
                }
            });
            mainCategory.addPreference(entDeviceRestrict);

            setPreferenceScreen(preferenceScreen);
        }

    }


//    public void requestRoot() {
//        if (!sharedPreferences.getBoolean("first_checkRoot", false)) {
//            commandExecutor.executeCommand("whoami", new CommandExecutor.CommandResultListener() {
//                @Override
//                public void onSuccess(String output) {
//                    sharedPreferences.edit().putBoolean("first_checkRoot", true).apply();
//                    CheckRootPermissionTask task = new CheckRootPermissionTask(hasRootPermission -> {
//                        sharedPreferences.edit().putBoolean("isGrantRoot", hasRootPermission).apply();
//                    });
//                    task.execute();
//                    Looper.prepare();
//                    Toast.makeText(getApplicationContext(), "已授权Root", Toast.LENGTH_SHORT).show();
//                }
//
//                @Override
//                public void onError(String error, Exception e) {
//                    Log.e("CommandExecutor", "root权限授权失败", e);
//                }
//
//            }, true, false);
//        }
//    }

}