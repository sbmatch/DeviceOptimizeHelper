package com.ma.enterprisemodepolicymanager.BaseApplication;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.ma.enterprisemodepolicymanager.Av.CrashInfoActivity;
import com.ma.enterprisemodepolicymanager.BuildConfig;
import com.ma.enterprisemodepolicymanager.Main;
import com.ma.enterprisemodepolicymanager.Utils.FilesUtils;
import com.ma.enterprisemodepolicymanager.Utils.ServiceManager;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class App extends Application {
    private static Context context;
    private static Handler uiHandler = new Handler(Looper.getMainLooper());
    private static SharedPreferences sharedPreferences;
    @Override
    protected void attachBaseContext(Context base) {
        //调用父类的attachBaseContext方法
        super.attachBaseContext(base);
        //如果当前系统版本大于等于Android P
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //添加隐藏API的排除
            HiddenApiBypass.addHiddenApiExemptions("");
        }
        //将当前上下文设置为base
        context = base;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(getContext()));
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
            String stackTraceContent = getStackTrace(throwable);
            //将崩溃日志写入文件
            FilesUtils.writeToFile(logPath,stackTraceContent, false);

            if (stackTraceContent.contains("android.os.DeadObjectException")){
                String command = "nohup app_process -Djava.class.path=" + ServiceManager.getPackageManager().getApplicationInfo(BuildConfig.APPLICATION_ID).sourceDir + "  /system/bin/sh  --nice-name=deviceopt_server " + Main.class.getName();

                stackTraceContent = "在电脑上执行以下命令启动服务:\n"+"adb shell \""+command+"\"\n\n" +
                        "在shell命令行环境执行以下命令启动服务:\n" +
                        ""+command+" > /data/local/tmp/ent.log &";
            }

            Intent crash = new Intent(getContext(), CrashInfoActivity.class);
            crash.putExtra(Intent.EXTRA_TEXT,stackTraceContent);
            crash.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            getContext().startActivity(crash);

            defaultHandler.uncaughtException(thread,throwable);
        }

        public static String getStackTrace(Throwable throwable) {
            // 将 Throwable 对象的堆栈信息转换为字符串形式
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        }

    }

    public static Context getContext() {
        return context;
    }

    public static void restartApp(Context context) {

        // 获取当前应用的启动Intent
        Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        // 如果获取到了，则把其中的一些信息清除
        if (intent!= null) {
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        // 结束当前进程
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }
}