package ma.DeviceOptimizeHelper;

import android.annotation.SuppressLint;
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
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuRequestPermissionListener;
import com.rosan.dhizuku.api.DhizukuUserServiceArgs;
import com.rosan.dhizuku.shared.DhizukuVariables;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.lang.reflect.Field;

import ma.DeviceOptimizeHelper.Utils.CommandExecutor;
import ma.DeviceOptimizeHelper.Utils.FilesUtils;
import ma.DeviceOptimizeHelper.Utils.UserManagerUtils;
import ma.DeviceOptimizeHelper.Utils.UserService;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";
    public static PreferenceScreen preferenceScreen;
    public static ArraySet<SwitchPreferenceCompat> switchPreferenceCompatArraySet = new ArraySet<>();
    private static ArraySet<String> getALLUserRestrictions;
    private static String isDhizukuFilePath ;
    public static CommandExecutor commandExecutor = CommandExecutor.getInstance();
    public static IUserService userService;
    private static String command;
    private static SettingsActivity.ServiceThread2 serviceThread2 = new ServiceThread2("你干嘛哎呦");

    public static Context context;

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

        isDhizukuFilePath = new File(getFilesDir(),"isDhizuku").getAbsolutePath();

        // 开发者是个小黑子
        if (!serviceThread2.isAlive()){
            serviceThread2.start();
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
        outState.putBoolean("isDhizuku", FilesUtils.isFileExists(isDhizukuFilePath));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,10000,0,getResIdReflect("enable_all_policy"));
        menu.add(Menu.NONE,10001,1,getResIdReflect("disallow_all_policy"));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        try{
            switch (item.getItemId()){
                case 10000:
                    // 启用全部
                    oneKeyChange(true);
                    break;
                case 10001:
                    oneKeyChange(false);
                    break;
            }
        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
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

    private static boolean bindDhizukuservice(){

        DhizukuUserServiceArgs args = new DhizukuUserServiceArgs(new ComponentName(context, UserService.class));

        try{
            Dhizuku.bindUserService(args, new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    if (userService == null){
                        userService = IUserService.Stub.asInterface(service);
                    }else {
                        FilesUtils.createFile(isDhizukuFilePath);
                    }
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    Log.e("Dhizuku",name+"  is Disconnected");
                }
            });
        }catch (IllegalStateException e){
            FilesUtils.delete(isDhizukuFilePath);
            e.printStackTrace();

        }
        return FilesUtils.isFileExists(isDhizukuFilePath);
    }

    private  void oneKeyChange(boolean z) {
        String value  = z ? "true" : "false";
        commandExecutor.executeCommand(command + " " + value, new CommandExecutor.CommandResultListener() {
            @Override
            public void onSuccess(String output) {
                runOnUiThread(() -> {
                    for (SwitchPreferenceCompat compat: switchPreferenceCompatArraySet){
                        compat.setChecked(z);
                    }
                });
                Looper.prepare();
                Toast.makeText(context, "任务执行完毕", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error, Exception e) {

                if (error.contains("Permission denied")){
                    if (userService != null){
                        StringBuilder setErrorList = new StringBuilder();
                        runOnUiThread(() -> {
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

                        });
                    }
                    Looper.prepare();
                    Toast.makeText(context, "任务执行失败", Toast.LENGTH_SHORT).show();
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


    public static void tryRequestRoot(){
        commandExecutor.executeCommand(command,  new CommandExecutor.CommandResultListener() {
            @Override
            public void onSuccess(String output) {
                Looper.prepare();
                Toast.makeText(context, "已授权Root", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error, Exception e) {
               Log.e("CommandExecutor","root权限授权失败",e);
            }

        }, true, true);
    }

    public static void tryRequestsDhizukuPermission(Context context){
        try {
            if (!Dhizuku.isPermissionGranted()){
                new MaterialAlertDialogBuilder(context).setTitle("权限检查")
                        .setMessage("好的! 让我们试试申请Dhizuku权限, 如果可以,请在接下来的权限申请对话框中允许授权")
                        .setPositiveButton("好的",  (dialog, which) -> Dhizuku.requestPermission(new DhizukuRequestPermissionListener() {
                            @Override
                            public void onRequestPermission(int grantResult) {
                                if (grantResult == PackageManager.PERMISSION_GRANTED) {
                                    bindDhizukuservice();
                                    tryRequestRoot();
                                    Looper.prepare();
                                    Toast.makeText(context, "Dhizuku 已授权", Toast.LENGTH_SHORT).show();
                                }
                            }
                        })).setNegativeButton("取消",null).create().show();
            }else {
                bindDhizukuservice();
            }
        }catch (IllegalStateException e){
            tryRequestRoot();
            FilesUtils.delete(isDhizukuFilePath);
            Toast.makeText(context, "Dhizuku 未安装或未激活", Toast.LENGTH_SHORT).show();
        }
    }

    public static class HeaderFragment extends PreferenceFragmentCompat {
        Handler handler;
        @SuppressLint("ResourceAsColor")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            context = requireContext();
            getALLUserRestrictions = UserManagerUtils.getALLUserRestrictionsReflectForUserManager();

            if (savedInstanceState == null) {
                savedInstanceState = new Bundle();
            }

            savedInstanceState.putBoolean("isGrantRoot", isRooted());

            if (FilesUtils.isFileExists(isDhizukuFilePath) || savedInstanceState.getBoolean("isGrantRoot")){
                Toast.makeText(context, "欢迎使用", Toast.LENGTH_SHORT).show();
            }else {
                new MaterialAlertDialogBuilder(context).setTitle("应用说明").setMessage("本应用支持 Dhizuku 与 Root 两种使用方式，其中Root模式可设置所有系统支持的限制策略，Dhizuku模式下各家深度定制ROM对<设备所有者>权限的限制则各有不同，接下来我们会向您请求这两种权限, 优先级为: Root > Dhizuku ，请注意: 在我们获取到Dhizuku权限后会继续尝试申请Root权限, 现在，我们将尝试申请您设备上的Dhizuku权限, 成功后会继续尝试申请Root权限 \n如果您了解自己在干什么，请点击继续按钮")
                        .setPositiveButton("继续", (dialog, which) -> {
                            tryRequestsDhizukuPermission(context);
                            dialog.cancel();
                        }).setNegativeButton("取消",null).create().show();
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
                        commandExecutor.executeCommand(command + key + " false", new CommandExecutor.CommandResultListener() {
                            @Override
                            public void onSuccess(String output) {
                                Looper.prepare();
                                Toast.makeText(context, "已禁用此限制策略", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String error, Exception e) {

                                    Looper.prepare();
                                    Toast.makeText(context, "Root方式执行失败，尝试使用Dhizuku执行...", Toast.LENGTH_SHORT).show();
                                    try {
                                        if (userService != null){
                                            // 使用 dhizuku 提供的权限执行任务
                                            userService.clearUserRestriction(DhizukuVariables.COMPONENT_NAME, key);
                                            Toast.makeText(context, "已禁用此限制策略", Toast.LENGTH_SHORT).show();
                                        }
                                        Toast.makeText(context, "任务执行失败", Toast.LENGTH_SHORT).show();
                                    } catch (RemoteException e1) {
                                        e1.printStackTrace();
                                    }

                            }
                        }, true, true);
                        break;
                    case 1: // 当 newValue 的值为 1 时，启用指定的限制策略
                        // 使用 root 权限执行任务
                        commandExecutor.executeCommand(command + key + " true", new CommandExecutor.CommandResultListener() {
                            @Override
                            public void onSuccess(String output) {
                                Looper.prepare();
                                Toast.makeText(context, "已启用此限制策略"+ output, Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String error, Exception e) {
                                Looper.prepare();
                                Toast.makeText(context, "尝试使用Dhizuku执行...", Toast.LENGTH_SHORT).show();
                                try {
                                    if (userService != null){
                                        // 使用 dhizuku 提供的权限执行任务
                                        userService.addUserRestriction(DhizukuVariables.COMPONENT_NAME, key);
                                        Toast.makeText(context, "已启用此限制策略", Toast.LENGTH_SHORT).show();
                                    }
                                    Toast.makeText(context, "任务执行失败", Toast.LENGTH_SHORT).show();
                                } catch (Exception e2) {
                                    e2.printStackTrace();
                                }
                            }
                        }, true, true);
                        break;
                    default:
                        // 如果 newValue 的值不是 0 或 1，则不执行任何操作
                }

                return true;
            });

            // 获取根布局，如果不存在则创建一个
            if (preferenceScreen == null){
                preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
            }

            // 创建首选项分类
            PreferenceCategory preferenceCategory = new PreferenceCategory(requireContext());
            preferenceCategory.setIconSpaceReserved(false);

            // 将动态生成的分类添加进首选项的根布局中
            preferenceScreen.addPreference(preferenceCategory);

            // 动态创建SwitchPreferenceCompat, 属于是有多少就创建多少
            for (String key : getALLUserRestrictions) {
                SwitchPreferenceCompat switchPreferenceCompat = new SwitchPreferenceCompat(requireContext());
                switchPreferenceCompat.setKey(key);
                switchPreferenceCompat.setTitle(key);
                switchPreferenceCompat.setIconSpaceReserved(false);
                // 添加限制策略的描述 目前支持中，英文
                switchPreferenceCompat.setSummary(getResIdReflect(key));
                // 添加开关变化监听器
                switchPreferenceCompat.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(@NonNull Preference preference, Object newValue) {
                        Message message = Message.obtain();
                        message.obj =  preference.getKey();
                        message.arg1 = (boolean)newValue ? 1 : 0;
                        handler.sendMessage(message); // 发送消息

                        return isRooted() || bindDhizukuservice();
                    }
                });
                // 将动态生成的SwitchPreferenceCompat对象添加进一个列表中
                switchPreferenceCompatArraySet.add(switchPreferenceCompat);
                // 将动态生成的SwitchPreferenceCompat对象添加进首选项的分类布局中
                preferenceCategory.addPreference(switchPreferenceCompat);
            }
            preferenceCategory.setTitle("* 注: 限制策略的数量受Android版本的影响");
            setPreferenceScreen(preferenceScreen); // 将这些都显示出来

        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            if (savedInstanceState != null){
                savedInstanceState.putBoolean("isGrantRoot",isRooted());
            }
        }


        public static boolean isRooted() {

            Process process = null;
            try {
                // 尝试执行一个需要root权限的命令，例如 "su"
                process = Runtime.getRuntime().exec("su"+"\n");
                int exitCode = process.exitValue();
                // 如果上述命令执行成功，说明应用具有root权限
                return exitCode == 0;
            } catch (Exception e) {
                // 如果出现异常，说明应用没有root权限
                return false;
            }finally {
                if (process != null){
                    process.destroy();
                }
            }
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