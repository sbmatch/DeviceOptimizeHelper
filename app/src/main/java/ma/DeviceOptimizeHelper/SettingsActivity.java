package ma.DeviceOptimizeHelper;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener;
import com.rosan.dhizuku.api.DhizukuUserServiceArgs;
import com.rosan.dhizuku.shared.DhizukuVariables;

import java.lang.reflect.Field;
import java.util.Objects;

import ma.DeviceOptimizeHelper.Utils.CommandExecutor;
import ma.DeviceOptimizeHelper.Utils.DhizukuUtils;
import ma.DeviceOptimizeHelper.Utils.UserManagerUtils;
import ma.DeviceOptimizeHelper.Utils.UserService;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";
    public static PreferenceScreen preferenceScreen;
    public static ArraySet<SwitchPreferenceCompat> switchPreferenceCompatArraySet = new ArraySet<>();
    private static ArraySet<String> getALLUserRestrictions;
    public static SwitchPreferenceCompat switchPreferenceCompat;
    public static IUserService userService;
    private static String command;
    private static SettingsActivity.ServiceThread2 serviceThread2 = new ServiceThread2("你干嘛哎呦");

    private static Context context;

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

        command = "app_process -Djava.class.path="+getApkPath(this)+"  /system/bin  " + Main.class.getName() + " ";

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
        menu.add(Menu.NONE,10000,0,getResIdReflect("enable_all_policy"));
        menu.add(Menu.NONE,10001,1,getResIdReflect("disallow_all_policy"));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case 10000:
                // 启用全部
                try {
                    oneKeyChange(true);
                } catch (RemoteException e) {
                    e.printStackTrace();
                 }
                break;
            case 10001:
                try {
                    oneKeyChange(false);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
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

    private static void bindDhizukuservice(){
        DhizukuUserServiceArgs args = new DhizukuUserServiceArgs(new ComponentName(context, UserService.class));
        boolean bind = Dhizuku.bindUserService(args, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                userService = IUserService.Stub.asInterface(service);
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("Dhizuku",name+" service is Disconnected");
            }
        });
    }

    private static void oneKeyChange(boolean z) throws RemoteException {

        try {
            String value  = z ? "true" : "false";
            CommandExecutor.executeCommand(command +" "+value, true);
            for (SwitchPreferenceCompat compat: switchPreferenceCompatArraySet){
                compat.setChecked(z);
            }
        }catch (Exception e){
            if (userService != null){

                StringBuilder setErrorList = new StringBuilder();
                int i = 0;

                for (SwitchPreferenceCompat compat: switchPreferenceCompatArraySet){
                    try {
                        if (z){
                            userService.addUserRestriction(DhizukuVariables.COMPONENT_NAME, compat.getKey());
                            compat.setChecked(true);
                        }else {
                            userService.clearUserRestriction(DhizukuVariables.COMPONENT_NAME, compat.getKey());
                            compat.setChecked(false);
                        }
                    }catch (SecurityException e1){
                        i++;
                        setErrorList.append(e1.getMessage()).append("\n\n");
                    }
                }

                String title = context.getString(getResIdReflect("set_error_count_title"));

                new MaterialAlertDialogBuilder(context).setMessage(setErrorList).setTitle(String.format(title,i)).setPositiveButton("好的",null).create().show();
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

            context = requireContext();

            if (!Dhizuku.isPermissionGranted()){
                new MaterialAlertDialogBuilder(requireContext()).setTitle("权限检查")
                        .setMessage("本应用支持 root 和 Dhizuku 两种模式, 让我们试试申请Dhizuku权限, 如果可以请在接下来的权限申请对话框中允许授权")
                        .setPositiveButton("好的",  (dialog, which) -> Dhizuku.requestPermission(new DhizukuRequestPermissionListener() {
                            @Override
                            public void onRequestPermission(int grantResult) {
                                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                                    bindDhizukuservice();
                                }
                            }
                        })).setNegativeButton("取消",null).create().show();
            }else {
                bindDhizukuservice();
            }

            preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
            getALLUserRestrictions = UserManagerUtils.getALLUserRestrictionsReflectForUserManager();

            handler = new Handler(serviceThread2.getLooper(), msg -> {

                String key = (String) msg.obj;

                try {
                    switch (msg.arg1){
                        case 0:
                            switch (msg.what){
                                case 2:
                                    CommandExecutor.executeCommand(command+key +" false", true);
                                    break;
                                case 3:
                                    userService.clearUserRestriction(DhizukuVariables.COMPONENT_NAME, key);
                                    break;
                                default:
                            }
                            break;
                        case 1:
                            switch (msg.what){
                                case 2:
                                    CommandExecutor.executeCommand(command+key +" true", true);
                                    break;
                                case 3:
                                    userService.addUserRestriction(DhizukuVariables.COMPONENT_NAME, key);
                                    break;
                                default:
                            }
                            break;
                        default:
                    }
                }catch (RuntimeException e){
                    e.printStackTrace();
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

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

                    try {
                        message.what = 2;
                        CommandExecutor.executeCommand(command, true);
                    }catch (RuntimeException e){
                        if (userService != null){
                            message.what = 3;
                        }else {
                            isAllowSwitch = false;
                        }
                    }

                    handler.sendMessage(message);

                    return isAllowSwitch;
                });
                switchPreferenceCompatArraySet.add(switchPreferenceCompat);
                preferenceScreen.addPreference(switchPreferenceCompat);
            }


            setPreferenceScreen(preferenceScreen); // 将这些都显示出来
        }

    }


    private static class ServiceThread2 extends HandlerThread {
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