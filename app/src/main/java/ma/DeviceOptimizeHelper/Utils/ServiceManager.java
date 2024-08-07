package ma.DeviceOptimizeHelper.Utils;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServiceManager {

    private static Object appRunningControlManager;

    private ServiceManager() {
        /* not instantiable */
    }

    private static IInterface getServiceInterface(String service, String type) {
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

    public static IBinder checkService(String name) {
        try {
            @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getMethod("checkService", String.class);
            return (IBinder) getServiceMethod.invoke(null, name);
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

    public final static ActivityManager getActivityManager = new ActivityManager(getServiceInterface("activity", "android.app.IActivityManager"));
    public final static UserManager getUserManager = new UserManager(getServiceInterface("user", "android.os.IUserManager"));
    public final static PackageManager getPackageManager = new PackageManager(getServiceInterface("package", "android.content.pm.IPackageManager"));

    //public final static NotificationManager getNotificationManager = new NotificationManager(getService("notification","android.app.INotificationManager"));

    public static AppRunningControlManager getAppRunningControlManager() {
        if (FilesUtils.isFileExists("/system/system_ext/framework/miui-framework.jar")) {
            try {
                appRunningControlManager = ReflectUtil.callStaticObjectMethod(Class.forName("miui.security.AppRunningControlManager"), "getInstance");
            } catch (ClassNotFoundException e) {
                Log.e("ServiceManager", e.getMessage(), e.getCause());
            }
            return new AppRunningControlManager(appRunningControlManager);
        }
        return null;
    }
}

