package ma.DeviceOptimizeHelper.BaseApplication;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.rosan.dhizuku.api.Dhizuku;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

public class BaseApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            HiddenApiBypass.addHiddenApiExemptions("");
        }
        Dhizuku.init(base);

        // 设置全局异常处理器
        Thread.setDefaultUncaughtExceptionHandler((t, e) -> handleCrash(e));

    }

    private void handleCrash(Throwable throwable) {
        // 在这里你可以根据自己的需求进行处理，例如显示对话框或者保存堆栈信息到文件等
        String stackTrace = getStackTrace(throwable);
        new MaterialAlertDialogBuilder(getApplicationContext()).setTitle("崩溃了🤣👉🤡").setMessage(stackTrace).setPositiveButton("Ok", null).create().show();
    }

    private String getStackTrace(Throwable throwable) {
        // 将 Throwable 对象的堆栈信息转换为字符串形式
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        throwable.printStackTrace(pw);
        return sw.toString();
    }

}
