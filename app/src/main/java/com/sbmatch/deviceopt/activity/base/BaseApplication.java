package com.sbmatch.deviceopt.activity.base;

import android.annotation.SuppressLint;
import android.os.Build;
import android.util.Log;

import com.kongzue.baseframework.BaseApp;
import com.kongzue.baseframework.interfaces.OnBugReportListener;
import com.kongzue.dialogx.DialogX;
import com.kongzue.dialogx.dialogs.BottomDialog;
import com.rosan.dhizuku.api.Dhizuku;
import com.sbmatch.deviceopt.AppGlobals;
import com.sbmatch.deviceopt.Utils.FilesUtils;
import com.tencent.mmkv.MMKV;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ma.DeviceOptimizeHelper.BuildConfig;


public class BaseApplication extends BaseApp<BaseApplication> {
    public static final String logCrashAddSystemInfoPrefix = "制造商: " + Build.MANUFACTURER +
            ", 型号: " + Build.MODEL +
            ", Android版本: " + Build.VERSION.RELEASE +
            ", SDK版本: " + Build.VERSION.SDK_INT +
            "\nApp版本: " + BuildConfig.VERSION_NAME +
            ", App构建类型: " + BuildConfig.BUILD_TYPE +
            "\n";

    public BaseApplication() {
        AppGlobals.init(this);
    }

    @Override
    public void init() {

        setOnCrashListener(new OnBugReportListener() {
            @Override
            public boolean onCrash(Exception e, final File crashLogFile) {
                String logPath = getLogFile("crash").getAbsolutePath();
                //将崩溃日志写入文件
                FilesUtils.writeToFile(logPath, Log.getStackTraceString(e.fillInStackTrace()), false);
                runOnMain(() -> {
                    BottomDialog.show("崩溃了", Log.getStackTraceString(e.fillInStackTrace())).setOkButton("我知道了");
                });
                return false;
            }
        });

        HiddenApiBypass.addHiddenApiExemptions("");

        String mMkvRootDir = MMKV.initialize(this);

        // 创建一个名为"logs"的子目录
        File logsDir = new File(getExternalCacheDir(), "logs");
        FilesUtils.createDir(logsDir.getAbsolutePath());
        MMKV.defaultMMKV().encode("logPath", logsDir.getAbsolutePath());

        DialogX.init(this);
        DialogX.onlyOnePopTip = true;
        DialogX.implIMPLMode = DialogX.IMPL_MODE.DIALOG_FRAGMENT;

        if (isInstallApp("com.rosan.dhizuku")) {
            Dhizuku.init(this);
        }
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
