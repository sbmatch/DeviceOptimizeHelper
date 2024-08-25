package com.sbmatch.deviceopt.activity.base;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.rosan.dhizuku.api.Dhizuku;
import com.sbmatch.deviceopt.Utils.FilesUtils;
import com.sbmatch.deviceopt.Utils.NotificationHelper;
import com.tencent.mmkv.MMKV;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ma.DeviceOptimizeHelper.BuildConfig;


public class BaseApplication extends Application {
    public static final String LogCrashAddSystemInfoPrefix = "制造商: " + Build.MANUFACTURER +
            ", 型号: " + Build.MODEL +
            ", Android版本: " + Build.VERSION.RELEASE +
            ", SDK版本: " + Build.VERSION.SDK_INT +
            "\nApp版本: "+ BuildConfig.VERSION_NAME +
            ", App构建类型: " + BuildConfig.BUILD_TYPE +
            "\n";

    private static NotificationHelper notificationHelper;

    @Override
    protected void attachBaseContext(Context base) {
        //调用父类的attachBaseContext方法
        super.attachBaseContext(base);
        //如果当前系统版本大于等于Android P
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //添加隐藏API的排除
            HiddenApiBypass.addHiddenApiExemptions("");
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        String rootDir = MMKV.initialize(this);
        System.out.println("mmkv root: " + rootDir);

        notificationHelper = NotificationHelper.getInstance(this);

        System.out.println("Init Dhizuku: "+Dhizuku.init(this));

        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(this));
    }

    public static File getLogsDir(Context context) {
        File cacheDir = context.getExternalCacheDir();
        // 创建一个名为"logs"的子目录
        File logsDir = new File(cacheDir, "logs");
        FilesUtils.createDir(logsDir.getAbsolutePath());
        return logsDir;
    }

    public static File getLogFile(Context context,String name) {
        // 创建异常信息文件
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
        // 将时间戳转换为字符串
        String timestamp = dateFormat.format(new Date());
        // 获取logs文件夹
        return new File(getLogsDir(context),name+"_" + timestamp + ".log");
    }

    public static class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {
        private final Context context;
        Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        public GlobalExceptionHandler(Context context) {
            this.context = context;
        }
        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {
            String logPath = getLogFile(context,"crash").getAbsolutePath();
            //获取崩溃日志文件的绝对路径
            String stackTraceContext =  getStackTrace(throwable);
            //将崩溃日志写入文件
            FilesUtils.writeToFile(logPath,stackTraceContext, false);

            // 使用系统分享发送文件
            Intent intent = new Intent(Intent.ACTION_SEND);
            // 设置分享文件的类型
            intent.setType("text/plain");
            // 将文件转换为Uri
            intent.putExtra(Intent.EXTRA_TEXT, stackTraceContext);
            // 添加权限
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (!stackTraceContext.contains("binder haven't been received")) {
                notificationHelper.showNotification("msgToast", "崩溃日志(点我可拉起分享)",stackTraceContext,888, false, intent, null, null);
            }

            // 调用默认的异常处理
            defaultHandler.uncaughtException(thread,throwable);
        }

        public static String getStackTrace(Throwable throwable) {
            // 将 Throwable 对象的堆栈信息转换为字符串形式
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            throwable.printStackTrace(pw);
            return LogCrashAddSystemInfoPrefix + sw;
        }
    }
}
