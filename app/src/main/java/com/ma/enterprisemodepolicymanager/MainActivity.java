package com.ma.enterprisemodepolicymanager;

import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.IInterface;
import android.os.Looper;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.ma.enterprisemodepolicymanager.BaseApplication.App;
import com.ma.enterprisemodepolicymanager.Fragments.ApplicationManagerFragment;
import com.ma.enterprisemodepolicymanager.Fragments.DeviceManagerFragment;
import com.ma.enterprisemodepolicymanager.Fragments.RestrictionsManagerFragment;
import com.ma.enterprisemodepolicymanager.Utils.CommandExecutor;
import com.ma.enterprisemodepolicymanager.Utils.Enterprise.EnterpriseManager;
import com.ma.enterprisemodepolicymanager.Utils.FilesUtils;
import com.ma.enterprisemodepolicymanager.Utils.PackageManager;
import com.ma.enterprisemodepolicymanager.Utils.ResourcesUtils;
import com.ma.enterprisemodepolicymanager.Utils.ServiceManager;
import com.ma.enterprisemodepolicymanager.ViewModels.FragmentShareIBinder;

import java.io.File;
import java.lang.reflect.Method;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private final CommandExecutor commandExecutor = CommandExecutor.getInstance();
    public static Handler MainHandle = new Handler(Looper.getMainLooper());
    private static FragmentManager fragmentManager;
    private static SharedPreferences sharedPreferences;
    private static ActionBar actionBar;
    Intent remoteLinuxProcessBroadcast;
    FragmentShareIBinder shareIBinder;
    private static final PackageManager packageManager = ServiceManager.getPackageManager();
    private static IBinder.DeathRecipient deviceoptIBinder = () -> {
        sharedPreferences.edit().putBoolean("remoteProcessBinderlinkToDeath", false).apply();
        sharedPreferences.edit().putBoolean("remoteProcessBinder", false).apply();
        System.err.println("驱动服务端死亡");
    };

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

        sharedPreferences = getSharedPreferences("main_sharePreference", MODE_PRIVATE);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.ma.enterprisemodepolicymanager.deviceOptSendBroadcast");
        intentFilter.setPriority(Integer.MAX_VALUE);
        remoteLinuxProcessBroadcast = registerReceiver(null, intentFilter);

        fragmentManager = getSupportFragmentManager();
        fragmentManager.setFragmentResultListener("applicationFragment", this, new FragmentResultListener() {
            @Override
            public void onFragmentResult(@NonNull String requestKey, @NonNull Bundle result) {
                if (requestKey.equals("applicationFragment"))
                    sharedPreferences.edit().putBoolean("applicationFragment", true).apply();
            }
        });

        // 监听BackStackChanged事件，当BackStack的顺序发生变化时，且栈为0时
        fragmentManager.addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                actionBar.setDisplayHomeAsUpEnabled(false);
            }
        });

        fragmentManager.beginTransaction()
                .replace(R.id.settings, HeaderFragment.class, null)
                .setReorderingAllowed(true)
                .commit();

        shareIBinder =  new ViewModelProvider(this).get(FragmentShareIBinder.class);

        try {
            shareIBinder.setDeviceOptService(remoteLinuxProcessBroadcast.getParcelableExtra("deviceOptServiceBinder"));
            shareIBinder.setEnterpriseManager(remoteLinuxProcessBroadcast.getParcelableExtra("enterpriseManagerBinder"));
            shareIBinder.setContentService(remoteLinuxProcessBroadcast.getParcelableExtra("contentResolverBinder"));

            if (shareIBinder.getDeviceOptService().asBinder().isBinderAlive()) {
                sharedPreferences.edit().putBoolean("remoteProcessBinder", true).apply();
            }
        }catch (Throwable e){
            e.printStackTrace();
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


    private static IInterface getService(IBinder binder, String type){
        try {
            Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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
                File shareFile = FilesUtils.getLatestFileInDirectory(App.getLogsDir(getBaseContext()).getAbsolutePath());
                // 添加权限
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                if (output.isEmpty()) {
                    if (shareFile != null) {
                        // 将文件转换为Uri
                        intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getBaseContext(), BuildConfig.APPLICATION_ID+".provider", shareFile));
                        // 启动分享
                        getApplicationContext().startActivity(intent);
                    } else {
                        Looper.prepare();
                        Toast.makeText(App.getContext(), "暂无崩溃日志", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                    }
                } else {
                    intent.putExtra(Intent.EXTRA_TEXT, output);
                    App.getContext().startActivity(intent);
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
            if (ServiceManager.getService("EnterpriseManager") == null) {
                showDialog(requireContext(),"👎","您可能没有激活企业模式或者不是MIUI系统.",null);
            }
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            if (preferenceScreen == null || getPreferenceManager().getSharedPreferences() == null)
                preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());

            PreferenceCategory mainCategory = new PreferenceCategory(requireContext());
            mainCategory.setIconSpaceReserved(false);
            //mainCategory.setSummary("企业SDK版本: "+ AnyRestrictPolicyUtils.getAPIVersion());
            preferenceScreen.addPreference(mainCategory);

            Preference entRestrict = new Preference(requireContext());
            entRestrict.setKey("entRestrictSysFeature");
            entRestrict.setIconSpaceReserved(false);
            entRestrict.setTitle("系统功能管控");
            entRestrict.setSummary("飞行模式、蓝牙、加速度传感器、自动云同步、系统备份、相机、恢复出厂设置、指纹传感器、IMEI读取、录音功能、MTP功能、OTG功能、截屏功能、外置SD卡挂载、网络共享（包括蓝牙，WiFi，usb）、修改系统时间、USB调试功能、VPN功能、GPS功能、NFC功能、WiFi功能。");
            entRestrict.setOnPreferenceClickListener(preference -> {
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
              return true;
            });
            mainCategory.addPreference(entRestrict);

            Preference entAppRestrict = new Preference(requireContext());
            entAppRestrict.setKey("entAppRestrict");
            entAppRestrict.setIconSpaceReserved(false);
            entAppRestrict.setTitle("应用管控");
            entAppRestrict.setSummary("静默安装卸载、清除应用数据、清除应用缓存、运行时权限授予、防卸载、应用保活、应用安装黑白名单、静默激活注销设备管理器、静默激活注销辅助服务功能、杀应用进程、清除最近任务、应用运行黑白名单、添加可信应用市场。");
            entAppRestrict.setOnPreferenceClickListener(preference -> {
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
                    return true;
                }
            });
            mainCategory.addPreference(entDeviceRestrict);

            setPreferenceScreen(preferenceScreen);
        }

    }

}