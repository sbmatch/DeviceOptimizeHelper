package com.sbmatch.deviceopt.utils.SystemServiceWrapper;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import com.sbmatch.deviceopt.utils.FilesUtils;
import com.sbmatch.deviceopt.utils.ReflectUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServiceManager {

    private static Object appRunningControlManager;

    private ServiceManager() {
        /* not instantiable */
    }

    public static IInterface getServiceInterface(String service, String type) {
        try {
            @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            IBinder binder = (IBinder) getServiceMethod.invoke(null, service);
            Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException |
                 ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static IBinder getService(String service) {
        try {
            @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            return (IBinder) getServiceMethod.invoke(null, service);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException |
                 ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("PrivateApi")
    public static String[] listServices() {
        try {
            return (String[]) Class.forName("android.os.ServiceManager").getMethod("listServices").invoke(null);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException |
                 ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private final static ActivityManager am = new ActivityManager(getServiceInterface("activity", "android.app.IActivityManager"));
    private final static PackageManager pm = new PackageManager(getServiceInterface("package", "android.content.pm.IPackageManager"));
    private final static AppOpsManager appops = new AppOpsManager(getServiceInterface("appops", "com.android.internal.app.IAppOpsService"));

    public static ActivityManager getActivityManager() {
        return am;
    }

    public static PackageManager getPackageManager() {
        return pm;
    }

    public static AppOpsManager getAppOpsManager() { return appops; }

    public static AppRunningControlManager getAppRunningControlManager() {
        if (FilesUtils.isFileExists("/system/system_ext/framework/miui-framework.jar")) {
            try {
                appRunningControlManager = ReflectUtils.callStaticObjectMethod(Class.forName("miui.security.AppRunningControlManager"), "getInstance");
            } catch (ClassNotFoundException e) {
                Log.e("ServiceManager", e.getMessage(), e.getCause());
            }
            return new AppRunningControlManager(appRunningControlManager);
        }
        return null;
    }
}

