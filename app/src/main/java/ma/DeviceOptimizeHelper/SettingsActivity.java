package ma.DeviceOptimizeHelper;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.ArrayMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;
import androidx.preference.SwitchPreferenceCompat;

import java.util.Locale;

import ma.DeviceOptimizeHelper.Utils.CommandExecutor;
import ma.DeviceOptimizeHelper.Utils.UserManagerUtils;

public class SettingsActivity extends AppCompatActivity implements PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    private static final String TITLE_TAG = "settingsActivityTitle";
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
        }

        handler = new Handler(Looper.myLooper());
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Save current activity title so we can set it again after a configuration change
        outState.putCharSequence(TITLE_TAG, getTitle());
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (getSupportFragmentManager().popBackStackImmediate()) {
            return true;
        }
        return super.onSupportNavigateUp();
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

        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

            PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(requireContext());
            ArrayMap<String, String> getALLUserRestrictions = UserManagerUtils.getALLUserRestrictionsForFramework();

            // 动态创建SwitchPreferenceCompat, 属于是有多少就创建多少
            for (String key : getALLUserRestrictions.keySet()) {

                Locale currentLocale = getResources().getConfiguration().getLocales().get(0);

                SwitchPreferenceCompat switchPreferenceCompat = new SwitchPreferenceCompat(requireContext());
                switchPreferenceCompat.setKey(getALLUserRestrictions.get(key));
                switchPreferenceCompat.setTitle(getALLUserRestrictions.get(key));
                if (currentLocale.getLanguage().equals("zh")){
                    int summaryResId = getResources().getIdentifier(getALLUserRestrictions.get(key),"string",requireContext().getPackageName());
                    switchPreferenceCompat.setSummary(summaryResId);
                }else {
                    switchPreferenceCompat.setSummary(key);
                }

                // 添加开关变化监听器
                switchPreferenceCompat.setOnPreferenceChangeListener((preference, newValue) -> {
                    // 拼接命令
                    String command = "app_process -Djava.class.path="+getApkPath(requireContext())+"  /system/bin  " + Main.class.getName() + " "+preference.getKey() +" "+newValue;
                    // 执行命令
                    CommandExecutor.executeCommand(command, true);

                    Toast.makeText(requireContext(), preference.getKey() + " set to "+ newValue, Toast.LENGTH_SHORT).show();

                    return true;
                });
                preferenceScreen.addPreference(switchPreferenceCompat);
            }

            setPreferenceScreen(preferenceScreen); // 将这些都显示出来
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