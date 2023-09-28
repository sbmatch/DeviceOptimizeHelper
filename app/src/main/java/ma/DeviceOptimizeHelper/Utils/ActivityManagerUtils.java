package ma.DeviceOptimizeHelper.Utils;

import android.annotation.SuppressLint;
import android.os.IBinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActivityManagerUtils {

    private static Object IActivityManager(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub = Class.forName("android.app.IActivityManager$Stub");
            // 获取 asInterface 方法，用于创建接口的实例
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            // 通过 ServiceManager 获取实例
            return asInterface.invoke(null, ServiceManager.getSystemService("activity"));
        } catch (Exception e2) {
            // 捕获异常并抛出运行时异常
            throw new RuntimeException(e2);
        }
    }

    public static void forceStopPackage(String packageName, int userId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        IActivityManager().getClass().getMethod("forceStopPackage", String.class , int.class).invoke(IActivityManager(), packageName, userId);

    }

}
