package ma.DeviceOptimizeHelper;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.lang.reflect.Field;
import java.util.Objects;

import ma.DeviceOptimizeHelper.Utils.CommandExecutor;
import ma.DeviceOptimizeHelper.Utils.UserManagerUtils;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";
    public static PreferenceScreen preferenceScreen;
    public static ArraySet<SwitchPreferenceCompat> switchPreferenceCompatArraySet = new ArraySet<>();
    private static ArraySet<String> getALLUserRestrictions;
    public static SwitchPreferenceCompat switchPreferenceCompat;

    private static String command;
    private static SettingsActivity.ServiceThread2 serviceThread2 = new ServiceThread2("你干嘛哎呦");

    private static Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                setTitle(R.string.title_activity_settings);
            }
        });

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(false);
            actionBar.setBackgroundDrawable(null);
        }

        command = "app_process -Djava.class.path="+getApkPath(SettingsActivity.this)+"  /system/bin  " + Main.class.getName() + " ";

        // 开发者是个小黑子
        serviceThread2.start();

    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,10000,0,"启用所有策略");
        menu.add(Menu.NONE,10001,1,"禁用所有策略");
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 10000:
                // 启用全部
               oneKeyChange(true);
                break;
            case 10001:
                oneKeyChange(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
    }

    private static void oneKeyChange(boolean z){
        String value  = z ? "true" : "false";
        try {
            CommandExecutor.executeCommand(command +" "+value, true);
            for (SwitchPreferenceCompat compat: switchPreferenceCompatArraySet){
                compat.setChecked(z);
            }
        }catch (RuntimeException e){

            if (Objects.requireNonNull(e.getCause()).toString().contains("Permission denied")) {
                new MaterialAlertDialogBuilder(preferenceScreen.getContext()).setTitle("你干🦄").setMessage("没有 root 权限暂时用不了哦🤣👉🤡").setNegativeButton("好的", null).create().show();
            }
        }
    }

    /**
     * @param caller The fragment requesting navigation
     * @param pref   The preference requesting the fragment
     * @return
     */
    @Override
    public boolean onPreferenceStartFragment(@NonNull PreferenceFragmentCompat caller, @NonNull Preference pref) {
        return true;
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {
        boolean isAllowSwitch = true;
        @SuppressLint("ResourceAsColor")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
            getALLUserRestrictions = UserManagerUtils.getALLUserRestrictionsReflectForUserManager();

// 创建一个 Handler 对象，将它关联到指定线程的 Looper 上
// 这里的 serviceThread2 是一个线程对象，通过 getLooper() 获取它的消息循环
            handler = new Handler(serviceThread2.getLooper(), msg -> {
                try {
                    // 根据消息的 arg1 字段的值执行不同的操作
                    switch (msg.arg1){
                        // TODO 不用arg1，改用有意义的变量名，你操作下，我不好debug
                        case 0:
                            // 当 arg1 的值为 0 时，执行命令 command+msg.obj+" false"
                            // 这似乎是将 msg.obj 作为参数添加到 command 后，并设置为 false
                            CommandExecutor.executeCommand(command + msg.obj + " false", true);
                            break;
                        case 1:
                            // 当 arg1 的值为 1 时，执行命令 command+msg.obj+" true"
                            // 这似乎是将 msg.obj 作为参数添加到 command 后，并设置为 true
                            CommandExecutor.executeCommand(command + msg.obj + " true", true);
                            break;
                        default:
                            // 如果 arg1 的值不是 0 或 1，则不执行任何操作
                    }
                } catch (RuntimeException e) {
                    e.printStackTrace();
                }
                // 处理消息成功，返回 true
                return true;
            });

            // 动态创建SwitchPreferenceCompat, 属于是有多少就创建多少
            for (String key : getALLUserRestrictions) {

                switchPreferenceCompat = new SwitchPreferenceCompat(requireContext());
                switchPreferenceCompat.setKey(key);
                switchPreferenceCompat.setTitle(key);
                // 从系统中获取策略限制的启用状态
                switchPreferenceCompat.setChecked(UserManagerUtils.isUserRestrictionsReflectForKey(key));
                // 添加限制策略的描述 目前支持中，英文
                switchPreferenceCompat.setSummary(getResIdReflect(key));

                // 添加开关变化监听器
                switchPreferenceCompat.setOnPreferenceChangeListener((preference, newValue) -> {
                    Message message = Message.obtain();
                    message.obj = preference.getKey();
                    message.arg1 = (boolean) newValue ? 1 : 0;
                    handler.sendMessage(message);

                    try {
                        Log.d("执行指令", "onCreatePreferences: "+message.obj.toString()+" "+command);
                        CommandExecutor.executeCommand(command, true);
                    }catch (RuntimeException e){
                        isAllowSwitch = false;
                        new MaterialAlertDialogBuilder(requireActivity()).setTitle("抓到虫子啦"+"🐞").setMessage(e.fillInStackTrace()+"").setNegativeButton("好的",null).create().show();
                    }

                    return isAllowSwitch;
                });
                switchPreferenceCompatArraySet.add(switchPreferenceCompat);
                preferenceScreen.addPreference(switchPreferenceCompat);
            }


            setPreferenceScreen(preferenceScreen); // 将这些都显示出来
        }

    }


    private  static class ServiceThread2 extends HandlerThread {
        public ServiceThread2(String name) {
            super(name);
        }
    }

    private static int getResIdReflect(String key){
        try{

            Class<?> clazz = R.string.class;
            Field field = clazz.getField(key);
            return field.getInt(null);
        }catch (Resources.NotFoundException | NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static String getApkPath(Context context){
        String apkPath;
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            apkPath = applicationInfo.sourceDir;
            return apkPath;
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}