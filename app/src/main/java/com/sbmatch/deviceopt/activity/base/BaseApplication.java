package com.sbmatch.deviceopt.activity.base;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.color.DynamicColors;
import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.google.firebase.remoteconfig.ConfigUpdate;
import com.google.firebase.remoteconfig.ConfigUpdateListener;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigException;
import com.kongzue.baseframework.BaseApp;
import com.kongzue.baseframework.interfaces.OnBugReportListener;
import com.kongzue.baseframework.util.AppManager;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.MessageDialog;
import com.kongzue.dialogx.dialogs.PopTip;
import com.rosan.dhizuku.api.Dhizuku;
import com.sbmatch.deviceopt.AppGlobals;
import com.sbmatch.deviceopt.BuildConfig;
import com.sbmatch.deviceopt.R;
import com.sbmatch.deviceopt.utils.FilesUtils;
import com.tencent.mmkv.MMKV;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

import timber.log.Timber;


public class BaseApplication extends BaseApp<BaseApplication> {
    public static final String logCrashAddSystemInfoPrefix = "制造商: " + Build.MANUFACTURER +
            ", 型号: " + Build.MODEL +
            ", Android版本: " + Build.VERSION.RELEASE +
            ", SDK版本: " + Build.VERSION.SDK_INT +
            "\nApp版本: " + BuildConfig.VERSION_NAME +
            ", App构建类型: " + BuildConfig.BUILD_TYPE +
            "\n";
    private FirebaseRemoteConfig mFirebaseRemoteConfig ;

    public BaseApplication() {

    }

    @Override
    public void init() {

        AppGlobals.init(this);

        HiddenApiBypass.addHiddenApiExemptions("");

        DialogX.init(this);
        //开启沉浸式适配
        DialogX.enableImmersiveMode = true;
        //是否自动在主线程执行
        DialogX.autoRunOnUIThread = true;
        DialogX.onlyOnePopNotification = false;
        DialogX.onlyOnePopTip = false;

        String mMkvRootDir = MMKV.initialize(this);

        // 创建一个名为"logs"的子目录
        File logsDir = new File(getExternalCacheDir(), "logs");
        FilesUtils.createDir(logsDir.getAbsolutePath());
        MMKV.defaultMMKV().encode("logPath", logsDir.getAbsolutePath());

        FirebaseApp.initializeApp(this);

        if (isInstallApp("com.rosan.dhizuku")) {
            Dhizuku.init(this);
        }

        setOnCrashListener(new OnBugReportListener() {
            @Override
            public boolean onCrash(Exception e, final File crashLogFile) {
                String logPath = getLogFile("crash").getAbsolutePath();
                FirebaseCrashlytics.getInstance().recordException(e);
                //将崩溃日志写入文件
                FilesUtils.writeToFile(logPath, Log.getStackTraceString(e.fillInStackTrace()), false);
                PopTip.show("崩溃日志已保存至" + logPath).showLong().iconWarning().bringToFront();
                MessageDialog.show("崩溃了", Log.getStackTraceString(e.fillInStackTrace())).setOkButton("我知道了").setTitleIcon(R.drawable.warning);
                return false;
            }
        });
    }

    @Override
    public void initSDKs() {
        super.initSDKs();
//        MobileAds.initialize(this, initializationStatus -> {
//            AppGlobals.getLogger().info("AdMob SDK 初始化完成");
//        });
        // 让主题跟随系统
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);


        mFirebaseRemoteConfig = FirebaseRemoteConfig.getInstance();

        mFirebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(Executors.newSingleThreadExecutor(), task -> {
                    if (task.isSuccessful()) {
                        boolean updated = task.getResult();
                        AppGlobals.getLogger().info("Config params updated: " + updated);
                    }
                });

        mFirebaseRemoteConfig.addOnConfigUpdateListener(new ConfigUpdateListener() {
            @Override
            public void onUpdate(ConfigUpdate configUpdate) {
                AppGlobals.getLogger().info("Updated keys: " + configUpdate.getUpdatedKeys());
                mFirebaseRemoteConfig.activate().addOnCompleteListener(new OnCompleteListener<Boolean>() {
                    @Override
                    public void onComplete(@NonNull Task<Boolean> task) {
                        if (task.isSuccessful()) {
                            AppGlobals.getLogger().info("Remote Config was activated");
                        }
                    }
                });
            }

            @Override
            public void onError(@NonNull FirebaseRemoteConfigException error) {
                AppGlobals.getLogger().severe(Log.getStackTraceString(error));
            }

        });
    }

    public static File getLogFile(String name) {
        // 创建异常信息文件
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
        // 将时间戳转换为字符串
        String timestamp = dateFormat.format(new Date());
        // 获取logs文件夹
        File file = new File(MMKV.defaultMMKV().decodeString("logPath"), name + "_" + timestamp + ".log");
        FilesUtils.writeToFile(file.getAbsolutePath(), logCrashAddSystemInfoPrefix, true);
        return file;
    }
}
