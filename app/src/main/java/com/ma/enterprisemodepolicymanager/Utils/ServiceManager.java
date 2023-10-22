package com.ma.enterprisemodepolicymanager.Utils;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.ma.enterprisemodepolicymanager.Utils.Enterprise.EnterpriseManager;

public final class ServiceManager {

    private ServiceManager() {
        /* not instantiable */
    }
    private static ActivityManager activityManager;
    private static AccessibilityManager accessibilityManager;
    private static PackageManager packageManager;
    private static PackageManager packageNativeManager;
    private static UserManager userManager;
    private static WindowManager windowManager;
    private static NotificationManager notificationManager;
    private static AppOpsManager appOpsManager;
    private static DevicePolicyManager devicePolicyManager;
    private static ClipboardManager clipboardManager;
    private static EnterpriseManager enterpriseManager;
    private static IInterface getService(String service, String type){
        try {
            @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            IBinder binder = (IBinder) getServiceMethod.invoke(null, service);
            Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static IBinder getService(String service){
        try {
            @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getMethod("getService", String.class);
            return  (IBinder) getServiceMethod.invoke(null, service);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static IBinder checkService(String name){
        try {
            @SuppressLint({"DiscouragedPrivateApi", "PrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getMethod("checkService", String.class);
            return  (IBinder) getServiceMethod.invoke(null, name);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static String[] listServices() {
        try {
            return (String[]) Class.forName("android.os.ServiceManager").getMethod("listServices").invoke(null);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static ActivityManager getActivityManager(){
        if (activityManager == null) {
            activityManager = new ActivityManager(getService("activity", "android.app.IActivityManager"));
        }
        return activityManager;
    }

    public static AccessibilityManager getAccessibilityManager(){
        if (accessibilityManager == null){
            accessibilityManager = new AccessibilityManager(getService("accessibility", "android.view.accessibility.IAccessibilityManager"));
        }
        return accessibilityManager;
    }

    public static PackageManager getPackageManager(){
        if (packageManager == null){
            packageManager = new PackageManager(getService("package", "android.content.pm.IPackageManager"));
        }
        return packageManager;
    }

    public static PackageManager getPackageNativeManager(){
        if (packageNativeManager == null){
            packageNativeManager = new PackageManager(getService("package_native","android.content.pm.IPackageManagerNative"));
        }
        return packageNativeManager;
    }

    public static ClipboardManager getClipboardManager(){
        if (clipboardManager == null) clipboardManager = new ClipboardManager(getService("clipboard","android.content.IClipboard"));
        return clipboardManager;
    }

    public static UserManager getUserManager(){
        if (userManager == null){
            userManager = new UserManager(getService("user","android.os.IUserManager"));
        }
        return userManager;
    }

    public static WindowManager getWindowManager(){
        if (windowManager == null){
            windowManager = new WindowManager(getService("window","android.view.IWindowManager"));
        }
        return windowManager;
    }

    public static NotificationManager getNotificationManager(){
        if (notificationManager == null){
            notificationManager = new NotificationManager(getService("notification","android.app.INotificationManager"));
        }

        return notificationManager;
    }

    public static AppOpsManager getAppOpsManager(){
        if (appOpsManager == null){
            appOpsManager = new AppOpsManager(getService("appops","com.android.internal.app.IAppOpsService"));
        }
        return appOpsManager;
    }

    public static DevicePolicyManager getDevicePolicyManager() {
        if (devicePolicyManager == null) {
            devicePolicyManager = new DevicePolicyManager(getService("device_policy", " android.app.admin.IDevicePolicyManager"));
        }
        return devicePolicyManager;
    }

    public static EnterpriseManager getEnterpriseManager(){
        if (enterpriseManager == null) enterpriseManager = new EnterpriseManager(getService("EnterpriseManager","com.miui.enterprise.IEnterpriseManager"));
        return enterpriseManager;
    }
}
