package ma.DeviceOptimizeHelper.BaseApplication;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;

import com.rosan.dhizuku.api.Dhizuku;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import ma.DeviceOptimizeHelper.BuildConfig;
import ma.DeviceOptimizeHelper.Utils.ContextUtils;
import ma.DeviceOptimizeHelper.Utils.FilesUtils;
import ma.DeviceOptimizeHelper.Utils.NotificationHelper;

public class BaseApplication extends Application {
    private static Context context;
    public static final String LogCrashAddSystemInfoPrefix = "供应商: " + Build.MANUFACTURER +
            ", 型号: " + Build.MODEL +
            ", Android版本: " + Build.VERSION.RELEASE +
            ", SDK版本: " + Build.VERSION.SDK_INT +
            "\n App版本: "+ BuildConfig.VERSION_NAME +
            ", App构建类型: " + BuildConfig.BUILD_TYPE +
            "\n\n";

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
        //初始化Dhizuku
        Dhizuku.init(base);
        //将当前上下文设置为base
        context = base;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        notificationHelper = NotificationHelper.newInstance();

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

            notificationHelper.showNotification("msgToast", "崩溃日志(点我可拉起分享)",stackTraceContext,888, false, intent, null, null);

            // 调用默认的异常处理
            //defaultHandler.uncaughtException(thread,throwable);
        }

        public static String getStackTrace(Throwable throwable) {
            // 将 Throwable 对象的堆栈信息转换为字符串形式
            java.io.StringWriter sw = new java.io.StringWriter();
            java.io.PrintWriter pw = new java.io.PrintWriter(sw);
            throwable.printStackTrace(pw);
            return LogCrashAddSystemInfoPrefix + sw;
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
}
