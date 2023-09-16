package ma.DeviceOptimizeHelper.BaseApplication;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;

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
        Thread.setDefaultUncaughtExceptionHandler(new GlobalExceptionHandler(context));
    }



    public static class GlobalExceptionHandler implements Thread.UncaughtExceptionHandler {

        private Thread.UncaughtExceptionHandler defaultHandler;
        private Context context;

        public GlobalExceptionHandler(Context context) {
            defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
            this.context = context;
        }

        @Override
        public void uncaughtException(@NonNull Thread thread, @NonNull Throwable throwable) {

            File cacheDir = context.getCacheDir();
            if (cacheDir != null) {
                // 创建一个名为"logs"的子目录
                File logsDir = new File(cacheDir, "logs");
                FilesUtils.createDir(logsDir.getAbsolutePath());

                // 创建异常信息文件
                @SuppressLint("SimpleDateFormat")
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
                String timestamp = dateFormat.format(new Date());
                String fileName = "crash_" + timestamp + ".log";
                File logFile = new File(logsDir, fileName);

                // 获取系统信息
                String manufacturer = Build.MANUFACTURER;
                String model = Build.MODEL;
                String version = Build.VERSION.RELEASE;
                int sdkVersion = Build.VERSION.SDK_INT;

                // 创建包含系统信息的日志字符串
                String systemInfo = "Manufacturer: " + manufacturer + ", Model: " + model + ", Android Version: " + version + ", SDK Version: " + sdkVersion;

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.putExtra(Intent.EXTRA_SUBJECT, "崩溃日志已记录");
                SettingsActivity.commandExecutor.executeCommand("logcat -v threadtime -b crash -d *:v ", new CommandExecutor.CommandResultListener() {
                    @Override
                    public void onSuccess(String output) {

                        String stackTraceContext = !output.isEmpty() ? output : getStackTrace(throwable) ;
                        // 使用系统分享发送文件
                        intent.putExtra(Intent.EXTRA_TEXT, systemInfo+"\n\n"+stackTraceContext);

                        FilesUtils.writeToFile(logFile.getAbsolutePath(),systemInfo+"\n\n"+stackTraceContext);

                        Looper.prepare();
                        // 启动系统分享
                        context.startActivity(intent);
                        Toast.makeText(context, "请将崩溃日志提交给开发者用于修复bug", Toast.LENGTH_SHORT).show();
                        Looper.loop();
                        defaultHandler.uncaughtException(thread,throwable);
                    }

                    @Override
                    public void onError(String error, Exception e) {

                    }
                }, false, false);

            }
        }

        private static String getStackTrace(Throwable throwable) {
            // 将 Throwable 对象的堆栈信息转换为字符串形式
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            throwable.printStackTrace(pw);
            return sw.toString();
        }

        private static void restartActivity(Context context,Class<?> clazz){
            Intent i = new Intent(context, clazz);
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            context.startActivity(i);
        }

        private static void restartApp(Context context) {
            Intent intent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }

            // 结束当前进程
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }
    }

}
