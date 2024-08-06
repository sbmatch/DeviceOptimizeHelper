package ma.DeviceOptimizeHelper.Utils;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Process;
import android.os.UserHandle;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class ContextUtils {
    private ContextUtils() {

    }

    private static Object getActivityThreadWithReflect(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Constructor<?> activityThreadConstructor = activityThreadClass.getDeclaredConstructor();
            activityThreadConstructor.setAccessible(true);
            return activityThreadConstructor.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static Context getContext() {

        // 如果调用方是系统进程则获取SystemContext
        if (Process.myUid() == 0 || Process.myUid() == 1000){
            try {
                Object activityThread = getActivityThreadWithReflect();
                Method getSystemContextMethod = activityThread.getClass().getDeclaredMethod("getSystemContext");
                getSystemContextMethod.setAccessible(true);
                return (Context) getSystemContextMethod.invoke(activityThread);
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }

        return currentApplication().getBaseContext();
    }

    public static Application currentApplication(){
        return (Application) ReflectUtil.callObjectMethod2(getActivityThreadWithReflect(), "currentApplication");
    }

    public static Context createPackageContext(String packageName){
        return (Context) ReflectUtil.callObjectMethod2(getContext(),"createPackageContext",packageName, Context.CONTEXT_IGNORE_SECURITY);
    }

    public static Context createContextAsUser(){
        int userId = UserManager.myUserId();
        UserHandle user = (UserHandle) ReflectUtil.callStaticObjectMethod(
                UserHandle.class,
                "of",
                new Class[]{int.class},
                userId);
        return (Context) ReflectUtil.callObjectMethod2(
                getContext(),
                "createContextAsUser",
                user,
                0);
    }
}
