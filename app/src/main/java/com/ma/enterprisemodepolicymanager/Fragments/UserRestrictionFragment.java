package com.ma.enterprisemodepolicymanager.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.ma.enterprisemodepolicymanager.BuildConfig;
import com.ma.enterprisemodepolicymanager.IUserService;
import com.ma.enterprisemodepolicymanager.Utils.CheckRootPermissionTask;
import com.ma.enterprisemodepolicymanager.Utils.CommandExecutor;
import com.ma.enterprisemodepolicymanager.Utils.ServiceManager;
import com.ma.enterprisemodepolicymanager.Utils.UserManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import com.ma.enterprisemodepolicymanager.Utils.PackageManager;
import com.ma.enterprisemodepolicymanager.Utils.ResourcesUtils;

public class UserRestrictionFragment extends PreferenceFragmentCompat {

    private PreferenceScreen preferenceScreen;
    private PreferenceCategory preferenceCategory;
    private SharedPreferences sharedPreferences;
    private ArraySet<SwitchPreferenceCompat> switchPreferenceCompatArraySet = new ArraySet<>();
    private Handler handler;
    private int count;
    private IUserService userService;
    private boolean dialogShown = false;
    private CommandExecutor commandExecutor = CommandExecutor.getInstance();
    private PackageManager packageManager = ServiceManager.getPackageManager();
    private final ServiceThread2 serviceThread2 = new ServiceThread2("你干嘛哎呦");
    private final Context context = requireContext();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (sharedPreferences == null)
            sharedPreferences = requireContext().getSharedPreferences("main_sharePreference", Context.MODE_PRIVATE);
        // 开发者是个小黑子
        if (!serviceThread2.isAlive()) serviceThread2.start();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.add(Menu.NONE, 10000, 0, ResourcesUtils.getResIdReflect("enable_all_policy"));
        menu.add(Menu.NONE, 10001, 1, ResourcesUtils.getResIdReflect("disallow_all_policy"));
        super.onCreateOptionsMenu(menu, inflater);
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
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return super.onOptionsItemSelected(item);
    }


    // 获取 SharedPreferences
    @SuppressLint("ResourceAsColor")
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // 获取所有用户的限制
        ArraySet<String> getALLUserRestrictions = UserManager.getALLUserRestrictionsReflectForUserManager();

        // 如果sharedPreferences为空，则获取sharedPreferences
        if (sharedPreferences == null) {
            sharedPreferences = getPreferenceManager().getSharedPreferences();
        }

        // 创建一个 Handler 对象，将它关联到指定线程的 Looper 上
        // 这里的 serviceThread2 是一个线程对象，通过 getLooper() 获取它的消息循环
        handler = new Handler(serviceThread2.getLooper(), msg -> {
            // 获取限制策略的键
            String key = (String) msg.obj;
            // 获取开关的值
            int newValue = msg.arg1;

            switch (newValue) {
                case 0: // 当 newValue 的值为 0 时，禁用指定的限制策略
                    commandExecutor.executeCommand("app_process -Djava.class.path=" + PackageManager.getApkPath(packageManager, BuildConfig.APPLICATION_ID) + "  /system/bin   ma.DeviceOptimizeHelper.Main  " + key + " false", new CommandExecutor.CommandResultListener() {
                        @Override
                        public void onSuccess(String output) {
                            Looper.prepare();
                            Toast.makeText(context, "已禁用此限制策略", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error, Exception e) {
                            try {
//                                if (userService != null) {
//                                    // 使用 dhizuku 提供的权限执行任务
//                                    userService.setUserRestriction(DhizukuVariables.COMPONENT_NAME, key, false, true);
//                                    Looper.prepare();
//                                    Toast.makeText(context, "已禁用此限制策略", Toast.LENGTH_SHORT).show();
//                                }
                            } catch (Exception e1) {
                                e1.printStackTrace();
                                Looper.prepare();
                                Toast.makeText(context, "任务执行失败", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }, true, true);
                    break;
                case 1: // 当 newValue 的值为 1 时，启用指定的限制策略
                    // 使用 root 权限执行任务
                    commandExecutor.executeCommand("app_process -Djava.class.path=" + PackageManager.getApkPath(packageManager, BuildConfig.APPLICATION_ID) + "  /system/bin   ma.DeviceOptimizeHelper.Main  " + key + " true", new CommandExecutor.CommandResultListener() {
                        @Override
                        public void onSuccess(String output) {
                            Looper.prepare();
                            Toast.makeText(context, "已启用此限制策略", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(String error, Exception e) {
                            try {
//                                if (userService != null) {
//                                    // 使用 dhizuku 提供的权限执行任务
//                                    //userService.setUserRestriction(DhizukuVariables.COMPONENT_NAME, key, true, true);
//                                    Looper.prepare();
//                                    Toast.makeText(context, "已启用此限制策略", Toast.LENGTH_SHORT).show();
//                                }
                            } catch (Exception e2) {
                                e2.printStackTrace();
                                Looper.prepare();
                                Toast.makeText(getContext(), "任务执行失败", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, true, true);
                    break;
                default:
                    // 如果 newValue 的值不是 0 或 1，则不执行任何操作
            }

            return true;
        });


        if ((sharedPreferences.getBoolean("isGrantDhizuku", false) || sharedPreferences.getBoolean("isGrantRoot", false))) {
            Toast.makeText(context, "欢迎使用", Toast.LENGTH_SHORT).show();
        } else {
            new MaterialAlertDialogBuilder(context).setTitle("应用说明").setMessage("本应用支持 Dhizuku 与 Root 两种使用方式，其中Root模式可设置所有系统支持的限制策略，Dhizuku模式下各家深度定制ROM对<设备所有者>权限的限制则各有不同，接下来我们会向您请求这两种权限, 优先级为: Root > Dhizuku ，请注意: 在我们获取到Dhizuku权限后会继续尝试申请Root权限, 现在，我们将尝试申请您设备上的Dhizuku权限, 成功后会继续尝试申请Root权限 \n如果您了解自己在干什么，请点击继续按钮")
                    .setPositiveButton("继续", (dialog, which) -> {
                        //tryRequestsDhizukuPermission(context, true);
                        dialog.cancel();
                    }).setNegativeButton("取消", null).create().show();
        }

        // 获取根布局，如果不存在则创建一个
        if (preferenceScreen == null) {
            preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
        }

        // 创建首选项分类
        preferenceCategory = new PreferenceCategory(requireContext());
        preferenceCategory.setIconSpaceReserved(false);

        // 将动态生成的分类添加进首选项的根布局中
        preferenceScreen.addPreference(preferenceCategory);

        String TAG = "创建SwitchPreference";
        // 动态创建SwitchPreferenceCompat, 属于是有多少就创建多少
        for (String key : getALLUserRestrictions) {

            SwitchPreferenceCompat switchPreferenceCompat = new SwitchPreferenceCompat(requireContext());
            switchPreferenceCompat.setKey(key);
            switchPreferenceCompat.setTitle(key);
            switchPreferenceCompat.setIconSpaceReserved(false);
            switchPreferenceCompat.setDefaultValue(ServiceManager.getUserManager().isUserRestrictionsReflectForKey(key));
            // 添加限制策略的描述 目前支持中，英文
            switchPreferenceCompat.setSummary(ResourcesUtils.getResIdReflect(key));
            // 添加开关变化监听器
            switchPreferenceCompat.setOnPreferenceChangeListener((preference, newValue) -> {
                Message message = Message.obtain();
                message.obj = preference.getKey();
                message.arg1 = (boolean) newValue ? 1 : 0;
                handler.sendMessage(message); // 发送消息

                Log.i("SwitchPreferenceChangeListener", "newValue(创建新值): " + newValue);

                return (sharedPreferences.getBoolean("isGrantDhizuku", false) || sharedPreferences.getBoolean("isGrantRoot", false));
            });
            // 将动态生成的SwitchPreferenceCompat对象添加进一个列表中
            switchPreferenceCompatArraySet.add(switchPreferenceCompat);
            // 将动态生成的SwitchPreferenceCompat对象添加进首选项 的分类布局中
            preferenceCategory.addPreference(switchPreferenceCompat);

        }

        preferenceCategory.setTitle("* 注: 限制策略的数量受Android版本的影响");
        setPreferenceScreen(preferenceScreen); // 将这些都显示出来

    }

    @Override
    public boolean onPreferenceTreeClick(@NonNull Preference preference) {
        CheckRootPermissionTask task = new CheckRootPermissionTask(hasRootPermission -> {
            sharedPreferences.edit().putBoolean("isGrantRoot", hasRootPermission).apply();
        });
        task.execute();

        return super.onPreferenceTreeClick(preference);
    }

    private void oneKeyChange(boolean z) {

        // 重写了一键切换限制策略的实现，现在会首先使用Dhizuku进行执行， 遇到无法设置的限制则尝试使用root进行设置

        StringBuffer stringBuffer = new StringBuffer();
        boolean isDhizuku = sharedPreferences.getBoolean("isGrantDhizuku", false);
        boolean isRoot = sharedPreferences.getBoolean("isGrantRoot", false);

        if (isDhizuku || isRoot) {
            for (SwitchPreferenceCompat compat : switchPreferenceCompatArraySet) {
                try {
                    //userService.setUserRestriction(DhizukuVariables.COMPONENT_NAME, compat.getKey(),true,true);
                    compat.setChecked(z);
                } catch (Exception e1) {

                    if (e1.getMessage().contains(compat.getKey())) {
                        stringBuffer.append(compat.getKey()).append("\n");
                    }
                    count = stringBuffer.toString().split("\n").length;

                    commandExecutor.executeCommand("app_process -Djava.class.path=" + PackageManager.getApkPath(packageManager, BuildConfig.APPLICATION_ID) + "  /system/bin   ma.DeviceOptimizeHelper.Main  " + compat.getKey() + z, new CommandExecutor.CommandResultListener() {
                        @Override
                        public void onSuccess(String output) {
                            if (!dialogShown) {
                                dialogShown = true; // 设置标志，表示已经弹出了对话框
                                compat.setChecked(z);
                                String title = String.format(getString(ResourcesUtils.getResIdReflect("set_error_count_title")), count, z ? "启用" : "禁用");
                                new MaterialAlertDialogBuilder(context).setTitle(title).setMessage(stringBuffer.toString()).setPositiveButton("Ok", null).create().show();

                            }
                        }

                        @Override
                        public void onError(String error, Exception e) {
                            if (!dialogShown) {
                                dialogShown = true; // 设置标志，表示已经弹出了对话框
                                String title = String.format(getString(ResourcesUtils.getResIdReflect("set_error_count_title")), count, "失败");
                                new MaterialAlertDialogBuilder(context).setTitle(title).setMessage(stringBuffer.toString()).setPositiveButton("Ok", null).create().show();
                            }
                        }
                    }, true, true);
                }
            }
            dialogShown = false;
        } else {
            Toast.makeText(context, "🤣👉🤡", Toast.LENGTH_SHORT).show();
        }
    }
}