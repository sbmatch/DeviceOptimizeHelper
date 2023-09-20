package ma.DeviceOptimizeHelper.BaseApplication;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.util.LogPrinter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rosan.dhizuku.api.Dhizuku;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ma.DeviceOptimizeHelper.SettingsActivity;
import ma.DeviceOptimizeHelper.Utils.CommandExecutor;
import ma.DeviceOptimizeHelper.Utils.FilesUtils;

public class BaseApplication extends Application {
    public Context context;
    public static String systemInfo;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("");
        }
        Dhizuku.init(base);
        this.context = base;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        // 获取系统信息
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String version = Build.VERSION.RELEASE;
        int sdkVersion = Build.VERSION.SDK_INT;
        // 创建包含系统信息的日志字符串
        systemInfo = "Manufacturer: " + manufacturer + ", Model: " + model + ", Android Version: " + version + ", SDK Version: " + sdkVersion;

        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(context));
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
        String timestamp = dateFormat.format(new Date());
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
            FilesUtils.writeToFile(logPath,systemInfo+"\n"+stackTraceContext, false);

            Toast.makeText(context, "崩溃已写入Android/data/../cache/logs", Toast.LENGTH_SHORT).show();

            // 调用默认的异常处理
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

    public static void showDialog(Context context , String msg){
        SettingsActivity.getmHandle().post(() -> new MaterialAlertDialogBuilder(context).setTitle("好消息！ 特大的好消息! 崩溃啦!").setMessage(msg).setNegativeButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                restartApp(context);
            }
        }).create().show());
    }
}
