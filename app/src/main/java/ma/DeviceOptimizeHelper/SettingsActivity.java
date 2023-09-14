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
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import java.lang.reflect.Field;

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
               handler.post(new Runnable() {
                   @Override
                   public void run() {
                       runOnUiThread(new Runnable() {
                           @Override
                           public void run() {
                               for (SwitchPreferenceCompat compat: switchPreferenceCompatArraySet){
                                   compat.setChecked(true);
                               }
                           }
                       });
                   }
               });
                break;
            case 10001:
                oneKeyChange(false);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                for (SwitchPreferenceCompat compat: switchPreferenceCompatArraySet){
                                    compat.setChecked(false);
                                }
                            }
                        });
                    }
                });
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
        CommandExecutor.executeCommand(command +" "+value, true);
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

            preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
            getALLUserRestrictions = UserManagerUtils.getALLUserRestrictionsReflectForUserManager();

            handler = new Handler(serviceThread2.getLooper(), msg -> {

                switch (msg.arg1){
                    case 0:
                        CommandExecutor.executeCommand(command+msg.obj +" false", true);
                        break;
                    case 1:
                        CommandExecutor.executeCommand(command+msg.obj +" true", true);
                        break;
                    default:
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
                switchPreferenceCompat.setOnPreferenceClickListener(preference -> {

                    Message message = Message.obtain();
                    message.obj = preference.getKey();
                    message.arg1 = switchPreferenceCompat.isChecked() ? 1 : 0;
                    handler.sendMessage(message);

                    return false;
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