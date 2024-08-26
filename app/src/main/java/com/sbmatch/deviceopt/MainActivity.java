package com.sbmatch.deviceopt;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.ArraySet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.content.FileProvider;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.color.DynamicColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener;
import com.sbmatch.deviceopt.Interface.DhizukuUserServiceFactory;
import com.sbmatch.deviceopt.Interface.OnUserServiceCallbackListener;
import com.sbmatch.deviceopt.Utils.CommandExecutor;
import com.sbmatch.deviceopt.Utils.ContextUtil;
import com.sbmatch.deviceopt.Utils.FilesUtils;
import com.sbmatch.deviceopt.Utils.NotificationHelper;
import com.sbmatch.deviceopt.Utils.ReflectUtil;
import com.sbmatch.deviceopt.Utils.SystemServiceWrapper.DevicePolicyManager;
import com.sbmatch.deviceopt.Utils.SystemServiceWrapper.UserManager;
import com.sbmatch.deviceopt.Utils.ToastUtils;
import com.sbmatch.deviceopt.activity.CyberHoopActivity;
import com.sbmatch.deviceopt.activity.SettingsActivity;
import com.sbmatch.deviceopt.activity.base.BaseApplication;

import java.io.File;
import java.lang.reflect.Field;

import ma.DeviceOptimizeHelper.IUserService;
import ma.DeviceOptimizeHelper.R;
import rikka.material.preference.MaterialSwitchPreference;

public class MainActivity extends AppCompatActivity implements OnUserServiceCallbackListener{

    private static final ArraySet<MaterialSwitchPreference> switchPreferenceArraySet = new ArraySet<>();
    private static final CommandExecutor commandExecutor = CommandExecutor.getInstance();
    private final DhizukuUserServiceFactory dhizukuUserServiceFactory = DhizukuUserServiceFactory.get();
    private static IUserService userService;
    public int count;
    public boolean dialogShown = false;
    public static Handler mHandle = new Handler(Looper.getMainLooper());
    private final HeaderFragment headerFragment = new HeaderFragment();
    static final NotificationHelper notificationHelper = NotificationHelper.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getTheme().applyStyle(rikka.material.preference.R.style.ThemeOverlay_Rikka_Material3_Preference, true);
        DynamicColors.applyToActivityIfAvailable(this);
        // 让主题跟随系统
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        setContentView(R.layout.main_activity);

        // 获取ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // 如果ActionBar为空，则设置ActionBar的背景图片为null
            actionBar.setBackgroundDrawable(null);
        }

        dhizukuUserServiceFactory.setOnCallbackListener(this);

        if (!notificationHelper.hasPostPermission()) notificationHelper.requestPermission(this, new String[]{"android.permission.POST_NOTIFICATIONS"}, 999);

        if (!Dhizuku.isPermissionGranted()) {
            new MaterialAlertDialogBuilder(this)
                    .setTitle("需要Dhizuku权限")
                    .setMessage("本应用需要Dhizuku权限才能正常运行。Dhizuku权限允许应用执行一些系统级别的操作，例如禁用某些系统功能。\n\n请授予Dhizuku权限。")
                    .setPositiveButton("前往授权", (dialog, which) -> {
                        tryRequestsDhizukuPermission();
                        dialog.dismiss();
                    })
                    .setNegativeButton("稍后再说", null)
                    .create()
                    .show();
        }else {
           if (userService == null) dhizukuUserServiceFactory.bindUserService();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE, 10000, 0, getResIdReflect("enable_all_policy"));
        menu.add(Menu.NONE, 10001, 1, getResIdReflect("disallow_all_policy"));
        menu.add(Menu.NONE, 10002, 2, getResIdReflect("share_runtime_logs"));

        Intent cyberHoopActivity = new Intent(this, CyberHoopActivity.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        menu.add(Menu.NONE, 10010, 3, getResIdReflect("cyberhoop_page")).setIntent(cyberHoopActivity);

        Intent settingActivity = new Intent(this, SettingsActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        menu.add(Menu.NONE, 10011, 10, getResIdReflect("setting")).setIntent(settingActivity);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        try {
            switch (item.getItemId()) {
                case 10000:
                    // 启用全部
                    oneKeyChange(true);
                    break;
                case 10001:
                    oneKeyChange(false);
                    break;
                case 10002:
                    share_runtime_logs();
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dhizukuUserServiceFactory.unbindUserService();
    }

//    private final ActivityResultLauncher<Intent> getSyncAccounts = registerForActivityResult(
//            new ActivityResultContracts.StartActivityForResult(), result -> {
//
//            });


    public static Handler getMainUIHandle() {
        return (mHandle != null) ? mHandle : new Handler(Looper.getMainLooper());
    }

    private void share_runtime_logs() {
        // -b main 是指只显示主日志缓冲区（main buffer）的日志。主日志缓冲区包含了系统启动以来的所有核心系统日志。
        // -b crash 是指只显示崩溃日志缓冲区（crash buffer）的日志。这个缓冲区包含了系统崩溃或ANR（Application Not Responding）时的日志。
        // -d 是指倒序输出（descending order）。这意味着新的日志条目将首先显示，旧的条目将后显示。
        MainActivity.commandExecutor.executeCommand("logcat -b main -b crash -d ", new CommandExecutor.CommandResultListener() {
            @Override
            public void onSuccess(String output) {
                // 写入日志文件
                new Thread(() -> {
                    FilesUtils.writeToFile(BaseApplication.getLogFile(getApplication(), "runtime_logs").getAbsolutePath(), BaseApplication.LogCrashAddSystemInfoPrefix + "\n\n" + output, false);
                    // 使用系统分享发送文件
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    // 设置分享文件的类型
                    intent.setType("text/plain");
                    // 获取最新的文件
                    File shareFile = FilesUtils.getLatestFileInDirectory(BaseApplication.getLogsDir(getApplication()).getAbsolutePath());
                    // 将文件转换为Uri
                    intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(getApplication(), "ma.DeviceOptimizeHelper.provider", shareFile));
                    // 添加权限
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 启动分享
                    startActivity(intent);
                }).start();
            }

            @Override
            public void onError(String error, Exception e) {
                e.printStackTrace();

            }
        }, false, false);
    }


    private void oneKeyChange(boolean z) {

        // 重写了一键切换限制策略的实现，现在会首先使用Dhizuku进行执行， 遇到无法设置的限制则尝试使用root进行设置

        StringBuffer stringBuffer = new StringBuffer();
        getMainUIHandle().post(() -> {
            // 在 catch 块之前添加一个标志
            for (MaterialSwitchPreference compat : switchPreferenceArraySet) {
                try {
                    if (z) {
                        userService.addUserRestriction(compat.getKey());
                        runOnUiThread(() -> {
                            compat.setChecked(true);
                        });
                    } else {
                        userService.clearUserRestriction(compat.getKey());
                        runOnUiThread(() -> {
                            compat.setChecked(false);
                        });
                    }

                } catch (Exception e1) {

                    if (e1.getMessage().contains(compat.getKey())) {
                        stringBuffer.append(compat.getKey()).append("\n");
                    }
                    count = stringBuffer.toString().split("\n").length;

                    if (!dialogShown) {
                        dialogShown = true; // 设置标志，表示已经弹出了对话框
                        runOnUiThread(() -> {
                            String title = String.format(getString(getResIdReflect("set_error_count_title")), count, "失败");
                            new MaterialAlertDialogBuilder(MainActivity.this).setTitle(title).setMessage(stringBuffer.toString()).setPositiveButton("Ok", null).create().show();
                        });
                    }
                }
            }
            dialogShown = false;
        });
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
                        // 绑定 Dhizuku UserService
                        dhizukuUserServiceFactory.bindUserService();
                        ToastUtils.toast("Dhizuku 已授权");
                    }
                }
            });
        } catch (Exception e) {
            ToastUtils.toast("Dhizuku 未安装或未激活");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 999 && grantResults[0] == 0){
            notificationHelper.createNotificationChannel("msgToast", "异常通知", 4, null);
        }
    }

    @Override
    public void onUserServiceReady(IBinder service) {
        userService = IUserService.Stub.asInterface(service);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.user_restrict_policy, headerFragment)
                .commit();
        ToastUtils.toast("欢迎使用");
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {
        private Handler handler;
        private PreferenceScreen preferenceScreen;
        private final UserManager userManager = UserManager.get(ContextUtil.getContext());
        public HeaderFragment(){

        }
        @SuppressLint("ResourceAsColor")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            // 创建一个 Handler 对象，将它关联到指定线程的 Looper 上
            // 这里的 serviceThread2 是一个线程对象，通过 getLooper() 获取它的消息循环
            if (handler == null) {
                handler = new Handler(Looper.getMainLooper(), msg -> {
                    // 获取限制策略的键
                    String key = (String) msg.obj;
                    // 获取开关的值
                    int newValue = msg.arg1;

                    switch (newValue) {
                        case 0: // 当 newValue 的值为 0 时，禁用指定的限制策略
                            try {
                                // 使用 dhizuku 提供的权限执行任务
                                if (userService != null) ReflectUtil.callObjectMethod2(userService, "clearUserRestriction", Dhizuku.getOwnerComponent(), key);
                                ToastUtils.toast("已禁用此限制策略");
                            } catch (Throwable e1) {
                                notificationHelper.showNotification("msgToast", "禁用失败", e1.getMessage(), 888, true, new Intent(), null, null);
                                e1.printStackTrace();
                           }
                            break;
                        case 1: // 当 newValue 的值为 1 时，启用指定的限制策略
                            try {
                                // 使用 dhizuku 提供的权限执行任务
                                if (userService != null) ReflectUtil.callObjectMethod2(userService, "addUserRestriction", Dhizuku.getOwnerComponent(), key);
                                ToastUtils.toast("已启用此限制策略");
                            } catch (Throwable e2) {
                                notificationHelper.showNotification("msgToast", "启用失败", e2.getMessage(), 888, true, new Intent(), null, null);
                                e2.printStackTrace();
                            }
                            break;
                        default:
                            // 如果 newValue 的值不是 0 或 1，则不执行任何操作
                    }

                    return true;
                });
            }
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            // 获取根布局，如果不存在则创建一个
            if (preferenceScreen == null) preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());

            // 创建首选项分类
            PreferenceCategory preferenceCategory = new PreferenceCategory(requireContext());
            preferenceCategory.setIconSpaceReserved(false);

            // 将动态生成的分类添加进首选项的根布局中
            preferenceScreen.addPreference(preferenceCategory);

            // 动态创建SwitchPreference
            for (Object key : UserManager.getALLUserRestrictionsWithReflect()) {
                MaterialSwitchPreference switchPreferenceCompat = new MaterialSwitchPreference(requireContext());
                switchPreferenceCompat.setKey((String) key);
                switchPreferenceCompat.setTitle((CharSequence) key);
                switchPreferenceCompat.setIconSpaceReserved(false);
                switchPreferenceCompat.setDefaultValue(userManager.hasUserRestriction((String) key));
                // 添加限制策略的描述 目前支持中，英文
                switchPreferenceCompat.setSummaryProvider(preference -> {
                    if (getResIdReflect((String) key) == -1) return key.toString();
                    return getString(getResIdReflect((String) key));
                });
                // 添加开关变化监听器
                switchPreferenceCompat.setOnPreferenceChangeListener((preference, newValue) -> {
                    Message message = Message.obtain();
                    message.obj = preference.getKey();
                    message.arg1 = (boolean) newValue ? 1 : 0;
                    handler.sendMessage(message); // 发送消息

                    // 延迟500ms判断是否设置成功并更新ui
                    getMainUIHandle().postDelayed(() -> {
                        SwitchPreferenceCompat switchPrefer = findPreference(preference.getKey());
                        switchPrefer.setChecked(userManager.hasUserRestriction(preference.getKey()));
                    }, 500);

                    return true;
                });
                // 将动态生成的SwitchPreferenceCompat对象添加进一个列表中
                switchPreferenceArraySet.add(switchPreferenceCompat);
                // 将动态生成的SwitchPreferenceCompat对象添加进首选项 的分类布局中
                preferenceCategory.addPreference(switchPreferenceCompat);
            }

            preferenceCategory.setTitle("* 注: 限制策略的数量受Android版本及OEM厂商的影响");

            setPreferenceScreen(preferenceScreen); // 将这些都显示出来
        }
    }


     public static int getResIdReflect(String key) {
        //获取R.string.class对象
        try {
            Class<?> clazz = R.string.class;
            //获取key对应的字段
            Field field = clazz.getField(key);
            //获取字段的值
            return field.getInt(null);
        } catch (Resources.NotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return -1;
    }

}