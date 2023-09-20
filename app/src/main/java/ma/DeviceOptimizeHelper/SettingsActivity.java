package ma.DeviceOptimizeHelper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.util.ArraySet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

import ma.DeviceOptimizeHelper.BaseApplication.BaseApplication;
import ma.DeviceOptimizeHelper.Utils.CheckRootPermissionTask;
import ma.DeviceOptimizeHelper.Utils.CommandExecutor;
import ma.DeviceOptimizeHelper.Utils.FilesUtils;
import ma.DeviceOptimizeHelper.Utils.UserManagerUtils;
import ma.DeviceOptimizeHelper.Utils.UserService;


// TODO 注释！！！可以用codegeex或者chatgpt一键生成即可（文心就是垃圾）

// TODO 新功能加注释！！！

// TODO 修bug的提交，请把commit描述写清楚！！！！！！
// TODO 修bug的提交，请把commit描述写清楚！！！！！！
// TODO 修bug的提交，请把commit描述写清楚！！！！！！

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";
    @SuppressLint("StaticFieldLeak")
    public static PreferenceScreen preferenceScreen;
    public static ArraySet<SwitchPreferenceCompat> switchPreferenceCompatArraySet = new ArraySet<>();
    public static CommandExecutor commandExecutor = CommandExecutor.getInstance();
    public static IUserService userService;
    private static String command;
    private static final SettingsActivity.ServiceThread2 serviceThread2 = new ServiceThread2("你干嘛哎呦");
    public static Context context;
    public int count;
    public boolean dialogShown = false;
    private static SharedPreferences sharedPreferences;
    public static Handler mHandle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);

        if (savedInstanceState == null) {
            // 如果savedInstanceState为空，则添加HeaderFragment
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new HeaderFragment())
                    .commit();
        } else {
            // 如果savedInstanceState不为空，则设置标题
            setTitle(savedInstanceState.getCharSequence(TITLE_TAG));
        }
        // 监听BackStackChanged事件，当BackStack的顺序发生变化时，且栈为0时，设置标题
        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                setTitle(R.string.title_activity_settings);
            }
        });

        // 获取ActionBar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar!= null) {
            // 如果ActionBar不为空，则设置不显示HomeAsUp按钮
            actionBar.setDisplayHomeAsUpEnabled(false);
            // 如果ActionBar为空，则设置ActionBar的背景图片为null
            actionBar.setBackgroundDrawable(null);
        }

        command = "app_process -Djava.class.path="+getApkPath(this)+"  /system/bin   ma.DeviceOptimizeHelper.Main  ";

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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.add(Menu.NONE,10000,0,getResIdReflect("enable_all_policy"));
        menu.add(Menu.NONE,10001,1,getResIdReflect("disallow_all_policy"));
        menu.add(Menu.NONE,10002,2,getResIdReflect("share_runtime_logs"));
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
                case 10002:
                    share_runtime_logs();
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

    private final ActivityResultLauncher<Intent> getSyncAccounts = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {

            });


    public static Handler getmHandle() {
        return (mHandle != null) ? mHandle : (new Handler(Looper.getMainLooper()));
    }

    private void share_runtime_logs(){

        SettingsActivity.commandExecutor.executeCommand("logcat -b main -b crash -d ", new CommandExecutor.CommandResultListener() {
            @Override
            public void onSuccess(String output) {
                // 写入日志文件
                new Thread(() -> {
                    FilesUtils.writeToFile(BaseApplication.getLogFile(context,"runtime_logs").getAbsolutePath(),BaseApplication.systemInfo+"\n\n"+output, false);
                    // 使用系统分享发送文件
                    Intent intent = new Intent(Intent.ACTION_SEND);
                    // 设置分享文件的类型
                    intent.setType("text/plain");
                    // 获取最新的文件
                    File shareFile = FilesUtils.getLatestFileInDirectory(BaseApplication.getLogsDir(context).getAbsolutePath());
                    // 将文件转换为Uri
                    intent.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(context, "ma.DeviceOptimizeHelper.provider", shareFile));
                    // 添加权限
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_ACTIVITY_NEW_TASK);
                    // 启动分享
                    getApplicationContext().startActivity(intent);
                }).start();
            }

            @Override
            public void onError(String error, Exception e) {
                e.printStackTrace();

            }
        }, false, false);
    }


    private  void oneKeyChange(boolean z) {

        // 重写了一键切换限制策略的实现，现在会首先使用Dhizuku进行执行， 遇到无法设置的限制则尝试使用root进行设置

        StringBuffer stringBuffer = new StringBuffer();
        boolean isDhizuku = sharedPreferences.getBoolean("isGrantDhizuku",false);
        boolean isRoot = sharedPreferences.getBoolean("isGrantRoot",false);

        if (isDhizuku || isRoot) {

            getmHandle().post(() -> {
                // 在 catch 块之前添加一个标志
                for (SwitchPreferenceCompat compat : switchPreferenceCompatArraySet) {
                    try {
                        if (z) {
                            userService.addUserRestriction(DhizukuVariables.COMPONENT_NAME, compat.getKey());
                            runOnUiThread(()-> { compat.setChecked(true);});
                        } else {
                            userService.clearUserRestriction(DhizukuVariables.COMPONENT_NAME, compat.getKey());
                            runOnUiThread(()-> { compat.setChecked(false);});
                        }

                    } catch (Exception e1) {

                        if (e1.getMessage().contains(compat.getKey())){
                            stringBuffer.append(compat.getKey()).append("\n");
                        }
                        count = stringBuffer.toString().split("\n").length;

                        commandExecutor.executeCommand(command + compat.getKey() + z, new CommandExecutor.CommandResultListener() {
                            @Override
                            public void onSuccess(String output) {
                                if (!dialogShown) {
                                    dialogShown = true; // 设置标志，表示已经弹出了对话框

                                    runOnUiThread(()-> {
                                        compat.setChecked(z);
                                        String title = String.format(getString(getResIdReflect("set_error_count_title")),count, z ? "启用" : "禁用" );
                                        new MaterialAlertDialogBuilder(context).setTitle(title).setMessage(stringBuffer.toString()).setPositiveButton("Ok",null).create().show();
                                    });
                                 }
                            }

                            @Override
                            public void onError(String error, Exception e) {
                                if (!dialogShown) {
                                    dialogShown = true; // 设置标志，表示已经弹出了对话框
                                    runOnUiThread(() ->{
                                        String title = String.format(getString(getResIdReflect("set_error_count_title")),count, "失败");
                                        new MaterialAlertDialogBuilder(context).setTitle(title).setMessage(stringBuffer.toString()).setPositiveButton("Ok",null).create().show();
                                    });
                                }
                            }
                        }, true, true);
                    }
                }
                dialogShown = false;
            });

        }else {
            Toast.makeText(context, "🤣👉🤡", Toast.LENGTH_SHORT).show();
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

        Handler handler;
        // 获取 SharedPreferences
        @SuppressLint("ResourceAsColor")
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

 // 引入context
            context = requireContext();

 // 获取所有用户的限制
            ArraySet<String> getALLUserRestrictions = UserManagerUtils.getALLUserRestrictionsReflectForUserManager();

 // 如果sharedPreferences为空，则获取sharedPreferences
            if (sharedPreferences == null){
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
                        commandExecutor.executeCommand(command + key + " false", new CommandExecutor.CommandResultListener() {
                            @Override
                            public void onSuccess(String output) {
                                Looper.prepare();
                                Toast.makeText(context, "已禁用此限制策略", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String error, Exception e) {
                                try {
                                    if (userService != null) {
                                        // 使用 dhizuku 提供的权限执行任务
                                        userService.clearUserRestriction(DhizukuVariables.COMPONENT_NAME, key);
                                        Looper.prepare();
                                        Toast.makeText(context, "已禁用此限制策略", Toast.LENGTH_SHORT).show();
                                    }
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
                        commandExecutor.executeCommand(command + key + " true", new CommandExecutor.CommandResultListener() {
                            @Override
                            public void onSuccess(String output) {
                                Looper.prepare();
                                Toast.makeText(context, "已启用此限制策略", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onError(String error, Exception e) {
                                try {
                                    if (userService != null) {
                                        // 使用 dhizuku 提供的权限执行任务
                                        userService.addUserRestriction(DhizukuVariables.COMPONENT_NAME, key);
                                        Looper.prepare();
                                        Toast.makeText(context, "已启用此限制策略", Toast.LENGTH_SHORT).show();
                                    }
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


            if ((sharedPreferences.getBoolean("isGrantDhizuku",false) || sharedPreferences.getBoolean("isGrantRoot", false))){
                Toast.makeText(context, "欢迎使用", Toast.LENGTH_SHORT).show();
            } else {
                new MaterialAlertDialogBuilder(context).setTitle("应用说明").setMessage("本应用支持 Dhizuku 与 Root 两种使用方式，其中Root模式可设置所有系统支持的限制策略，Dhizuku模式下各家深度定制ROM对<设备所有者>权限的限制则各有不同，接下来我们会向您请求这两种权限, 优先级为: Root > Dhizuku ，请注意: 在我们获取到Dhizuku权限后会继续尝试申请Root权限, 现在，我们将尝试申请您设备上的Dhizuku权限, 成功后会继续尝试申请Root权限 \n如果您了解自己在干什么，请点击继续按钮")
                        .setPositiveButton("继续", (dialog, which) -> {
                            tryRequestsDhizukuPermission(context);
                            dialog.cancel();
                        }).setNegativeButton("取消", null).create().show();
            }

            // 获取根布局，如果不存在则创建一个
            if (preferenceScreen == null) {
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
                switchPreferenceCompat.setDefaultValue(UserManagerUtils.isUserRestrictionsReflectForKey(key));
                // 添加限制策略的描述 目前支持中，英文
                switchPreferenceCompat.setSummary(getResIdReflect(key));
                // 添加开关变化监听器
                switchPreferenceCompat.setOnPreferenceChangeListener((preference, newValue) -> {
                    Message message = Message.obtain();
                    message.obj = preference.getKey();
                    message.arg1 = (boolean) newValue ? 1 : 0;
                    handler.sendMessage(message); // 发送消息

                    return (sharedPreferences.getBoolean("isGrantDhizuku",false)  || sharedPreferences.getBoolean("isGrantRoot", false));
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
        public boolean onPreferenceTreeClick(@NonNull Preference preference) {

            CheckRootPermissionTask task = new CheckRootPermissionTask(hasRootPermission -> {
                sharedPreferences.edit().putBoolean("isGrantRoot", hasRootPermission).apply();
            });
            task.execute();

            return super.onPreferenceTreeClick(preference);

        }

        @Override
        public void onResume() {

            if (sharedPreferences.getBoolean("first_checkRoot",false)){
                // 创建一个CheckRootPermissionTask实例
                CheckRootPermissionTask task = new CheckRootPermissionTask(hasRootPermission -> {
                    // 将hasRootPermission设置到sharedPreferences中
                    sharedPreferences.edit().putBoolean("isGrantRoot", hasRootPermission).apply();
                });
                // 执行task
                task.execute();
            }
            bindDhizukuservice();

            super.onResume();
        }


        private void bindDhizukuservice(){

            DhizukuUserServiceArgs args = new DhizukuUserServiceArgs(new ComponentName(context, UserService.class));

            try{
                Dhizuku.bindUserService(args, new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        if (userService == null){
                            userService = IUserService.Stub.asInterface(service);
                        }
                        sharedPreferences.edit().putBoolean("isGrantDhizuku",true).apply();
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        Log.e("Dhizuku",name+"  is Disconnected");
                        bindDhizukuservice();
                    }
                });
            }catch (IllegalStateException e){
                e.printStackTrace();
                sharedPreferences.edit().putBoolean("isGrantDhizuku",false).apply();
            }
        }

        public void tryRequestRoot(){
           if (!sharedPreferences.getBoolean("first_checkRoot",false)){
               commandExecutor.executeCommand(command,  new CommandExecutor.CommandResultListener() {
                   @Override
                   public void onSuccess(String output) {

                       sharedPreferences.edit().putBoolean("first_checkRoot",true).apply();

                       CheckRootPermissionTask task = new CheckRootPermissionTask(hasRootPermission -> {
                           sharedPreferences.edit().putBoolean("isGrantRoot", hasRootPermission).apply();
                       });
                       task.execute();

                       Looper.prepare();
                       Toast.makeText(context, "已授权Root", Toast.LENGTH_SHORT).show();
                   }

                   @Override
                   public void onError(String error, Exception e) {
                       Log.e("CommandExecutor","root权限授权失败",e);
                   }

               }, true, true);
           }
        }

        public  void tryRequestsDhizukuPermission(Context context){
            try {
                if (!Dhizuku.isPermissionGranted()){
                    new MaterialAlertDialogBuilder(context).setTitle("权限检查")
                            .setMessage("好的! 让我们试试申请Dhizuku权限, 如果可以,请在接下来的权限申请对话框中允许授权")
                            .setPositiveButton("好的",  (dialog, which) -> Dhizuku.requestPermission(new DhizukuRequestPermissionListener() {
                                @Override
                                public void onRequestPermission(int grantResult) {
                                    if (grantResult == PackageManager.PERMISSION_GRANTED) {
                                        sharedPreferences.edit().putBoolean("isGrantDhizuku",true).apply();
                                        tryRequestRoot();
                                        Looper.prepare();
                                        Toast.makeText(context, "Dhizuku 已授权", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })).setNegativeButton("取消",null).create().show();
                }
            }catch (IllegalStateException e){
                e.printStackTrace();
                Toast.makeText(context, "Dhizuku 未安装或未激活", Toast.LENGTH_SHORT).show();
                tryRequestRoot();
            }
        }


    }


    private static class ServiceThread2 extends HandlerThread {
        public ServiceThread2(String name) {
            super(name);
        }
    }

    private static int getResIdReflect(String key){
        //获取R.string.class对象
        try{
            Class<?> clazz = R.string.class;
            //获取key对应的字段
            Field field = clazz.getField(key);
            //获取字段的值
            return field.getInt(null);
        }catch (Resources.NotFoundException | NoSuchFieldException | IllegalAccessException e){
            e.printStackTrace();
            //抛出异常
            Looper.prepare();
            //显示提示信息
            Toast.makeText(context, "捕获到崩溃，已写入日志文件", Toast.LENGTH_SHORT).show();
        }
        //返回0
        return 0;
    }

    public static String getApkPath(Context context){
        //获取apk路径
        String apkPath;
        try {
            //获取packageManager
            PackageManager packageManager = context.getPackageManager();
            //获取applicationInfo
            ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            //获取apk路径
            apkPath = applicationInfo.sourceDir;
            return apkPath;
        } catch (PackageManager.NameNotFoundException e) {
            //抛出异常
            throw new RuntimeException(e);
        }
    }

}