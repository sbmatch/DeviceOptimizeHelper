package ma.DeviceOptimizeHelper.Utils;

import android.annotation.SuppressLint;
import android.content.Context;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ContextUtils {

    public static Context retrieveSystemContext() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Constructor<?> activityThreadConstructor = activityThreadClass.getDeclaredConstructor();
            activityThreadConstructor.setAccessible(true);
            Object activityThread = activityThreadConstructor.newInstance();
            Method getSystemContextMethod = activityThread.getClass().getDeclaredMethod("getSystemContext");
            getSystemContextMethod.setAccessible(true);
            return (Context) getSystemContextMethod.invoke(activityThread);

        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Context createPackageContext(String packageName){
       try {
           return retrieveSystemContext().createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
       }catch (Throwable e){
           e.printStackTrace();
           throw new RuntimeException(e);
       }
    }
}
