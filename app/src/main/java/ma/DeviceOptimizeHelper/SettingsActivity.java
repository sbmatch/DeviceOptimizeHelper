package ma.DeviceOptimizeHelper;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
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

import ma.DeviceOptimizeHelper.Utils.CommandExecutor;
import ma.DeviceOptimizeHelper.Utils.UserManagerUtils;
import ma.DeviceOptimizeHelper.Utils.UserService;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";
    public static PreferenceScreen preferenceScreen;
    public static ArraySet<SwitchPreferenceCompat> switchPreferenceCompatArraySet = new ArraySet<>();
    private static ArraySet<String> getALLUserRestrictions;
    public static SwitchPreferenceCompat switchPreferenceCompat;
    public static CommandExecutor commandExecutor = CommandExecutor.getInstance();
    public static IUserService userService;
    private static String command;
    private static SettingsActivity.ServiceThread2 serviceThread2 = new ServiceThread2("你干嘛哎呦");

    public static Context context;
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

        command = "app_process -Djava.class.path="+getApkPath(this)+"  /system/bin   ma.DeviceOptimizeHelper.Main  ";

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
        String value  = z ? "true" : "false";
        commandExecutor.executeCommand(command + " " + value, new CommandExecutor.CommandResultListener() {
            @Override
            public void onSuccess(String output) {
                Looper.prepare();
                for (SwitchPreferenceCompat compat: switchPreferenceCompatArraySet){
                    compat.setChecked(z);
                }
                Toast.makeText(context, "任务执行完毕", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error, Exception e) {
                Looper.prepare();
                if (error.contains("Permission denied")){
                    Toast.makeText(context, "尝试使用 root 权限执行失败", Toast.LENGTH_SHORT).show();

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
                            }catch (SecurityException | RemoteException e1){
                                i++;
                                setErrorList.append(e1.getMessage()).append("\n\n");
                            }
                        }

                        String title = context.getString(getResIdReflect("set_error_count_title"));
                        new MaterialAlertDialogBuilder(context).setMessage(setErrorList).setTitle(String.format(title,i)).setPositiveButton("Ok",null).create().show();
                    }else {
                        Toast.makeText(context, "尝试使用 Dhizuku 执行任务失败", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }, true, true);

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

        @SuppressLint("ResourceAsColor")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            context = requireContext();

            try {
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
            }catch (IllegalStateException e){
                e.printStackTrace();
            }

            preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
            getALLUserRestrictions = UserManagerUtils.getALLUserRestrictionsReflectForUserManager();

// 创建一个 Handler 对象，将它关联到指定线程的 Looper 上
// 这里的 serviceThread2 是一个线程对象，通过 getLooper() 获取它的消息循环
            handler = new Handler(serviceThread2.getLooper(), msg -> {
                // 获取限制策略的键
                String key = (String) msg.obj;
                // 获取开关的值
                int newValue = msg.arg1;
                try {
                    switch (newValue){
                        // TODO 不用arg1，改用有意义的变量名，你操作下，我不好debug
                        case 0: // 当 newValue 的值为 0 时，禁用指定的限制策略
                            switch (msg.what){
                                case 2: // 使用 root 权限执行任务
                                    commandExecutor.executeCommand(command + key + " false", new CommandExecutor.CommandResultListener(){
                                        @Override
                                        public void onSuccess(String output) {
                                            Toast.makeText(context, "已禁用此限制策略", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(String error, Exception e) {

                                        }
                                    }, true, true);
                                    break;
                                case 3: // 使用 dhizuku 提供的权限执行任务
                                    userService.clearUserRestriction(DhizukuVariables.COMPONENT_NAME, key);
                                    break;
                                default:
                            }
                            break;
                        case 1: // 当 newValue 的值为 1 时，启用指定的限制策略
                            switch (msg.what){
                                case 2:
                                    commandExecutor.executeCommand(command + key + " true", new CommandExecutor.CommandResultListener() {
                                        @Override
                                        public void onSuccess(String output) {
                                            Toast.makeText(context, "已启用此限制策略", Toast.LENGTH_SHORT).show();
                                        }

                                        @Override
                                        public void onError(String error, Exception e) {

                                        }

                                    }, true, true);
                                    break;
                                case 3:
                                    userService.addUserRestriction(DhizukuVariables.COMPONENT_NAME, key);
                                    break;
                                default:
                            }
                        default:
                            // 如果 newValue 的值不是 0 或 1，则不执行任何操作
                    }
                } catch (RuntimeException | RemoteException e) {
                    e.printStackTrace();
                }
                return false;
            });

            // 动态创建SwitchPreferenceCompat, 属于是有多少就创建多少
            for (String key : getALLUserRestrictions) {
                switchPreferenceCompat = new SwitchPreferenceCompat(requireContext());
                switchPreferenceCompat.setKey(key);
                switchPreferenceCompat.setTitle(key);
                // 添加限制策略的描述 目前支持中，英文
                switchPreferenceCompat.setSummary(getResIdReflect(key));
                // 添加开关变化监听器
                switchPreferenceCompat.setOnPreferenceChangeListener(preferenceChangeListener);
                // 将动态生成的SwitchPreferenceCompat对象添加进一个列表中
                switchPreferenceCompatArraySet.add(switchPreferenceCompat);
                // 将动态生成的SwitchPreferenceCompat对象添加进首选项的根布局中
                preferenceScreen.addPreference(switchPreferenceCompat);
            }
            setPreferenceScreen(preferenceScreen); // 将这些都显示出来
        }

        @Override
        public void onResume() {
            super.onResume();
            // 从系统中获取策略限制的启用状态
            for (SwitchPreferenceCompat compat: switchPreferenceCompatArraySet){
                compat.setChecked(UserManagerUtils.isUserRestrictionsReflectForKey(compat.getKey()));
            }
        }

        public static Preference.OnPreferenceChangeListener preferenceChangeListener = new Preference.OnPreferenceChangeListener() {
            boolean z = false;
            @Override
            public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {

                Message message = Message.obtain();
                message.obj = preference.getKey(); // 获取限制策略的键
                message.arg1 = (boolean) newValue ? 1 : 0;

                commandExecutor.executeCommand(command,  new CommandExecutor.CommandResultListener() {
                    @Override
                    public void onSuccess(String output) {
                        z = true;
                        message.what = 2; // 如果使用root执行默认任务成功则将任务以root权限执行
                        Log.i("CommandExecutor",output);
                    }

                    @Override
                    public void onError(String error, Exception e) {
                        Log.e("CommandExecutor", error, e);
                        if (userService != null){ // 设备没有root， 或者未授权root 则尝试使用 dhizuku 执行任务
                            message.what = 3; // 如果 shizuku 服务存活 则尝试使用 dhizuku 执行任务
                        }
                        Looper.prepare();
                        Toast.makeText(context, "未授权root权限", Toast.LENGTH_SHORT).show();
                    }

                }, true, true);


                handler.sendMessage(message); // 发送消息

                return z;
            }
        };

    }


    private static class ServiceThread2 extends HandlerThread {
        public ServiceThread2(String name) {
            super(name);
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
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