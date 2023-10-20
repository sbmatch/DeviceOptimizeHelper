package ma.DeviceOptimizeHelper.Utils;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.ChangedPackages;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.IInterface;
import android.util.ArraySet;
import android.util.DisplayMetrics;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import ma.DeviceOptimizeHelper.BaseApplication.BaseApplication;

public class PackageManager {

    private final IInterface manager;
    private Method getInstalledApplicationsMethod;
    private Method getApplicationInfoMethod;
    private Method getNameForUidMethod;
    private Method checkPermissionMethod;
    private Method getPackageInfoMethod;
    private Method isPackageAvailableMethod;
    private Method getHomeActivitiesMethod;
    private Method getChangedPackagesMethod;
    private Method queryIntentActivitiesMethod;

    public PackageManager(IInterface manager){
        this.manager = manager;
    }

    private Method getInstalledApplicationsMethod() throws NoSuchMethodException {
        if (getInstalledApplicationsMethod == null){
            getInstalledApplicationsMethod = manager.getClass().getMethod("getInstalledApplications", (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? long.class : int.class , int.class);
        }
        return getInstalledApplicationsMethod;
    }

    private Method getApplicationInfoMethod() throws NoSuchMethodException {
        if (getApplicationInfoMethod == null){
            getApplicationInfoMethod = manager.getClass().getMethod("getApplicationInfo", String.class, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ? long.class : int.class) , int.class);
        }
        return getApplicationInfoMethod;
    }

    private Method getGetPackageInfoMethod() throws NoSuchMethodException {
        if (getPackageInfoMethod == null){
            getPackageInfoMethod = manager.getClass().getMethod("getPackageInfo", String.class, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? long.class : int.class , int.class);
        }
        return getPackageInfoMethod;
    }

    private Method getNameForUidMethod() throws NoSuchMethodException {
        if (getNameForUidMethod == null){
            getNameForUidMethod = manager.getClass().getMethod("getNameForUid", int.class);
        }
        return getNameForUidMethod;
    }

    private Method getCheckPermissionMethod() throws NoSuchMethodException {
        if (checkPermissionMethod == null){
            checkPermissionMethod = manager.getClass().getMethod("checkPermission",String.class, String.class, int.class);
        }
        return checkPermissionMethod;
    }

    private Method getIsPackageAvailableMethod() throws NoSuchMethodException {
        if (isPackageAvailableMethod == null){
            isPackageAvailableMethod = manager.getClass().getMethod("isPackageAvailable", String.class, int.class);
        }
        return isPackageAvailableMethod;
    }

    private Method getQueryIntentActivitiesMethod() throws NoSuchMethodException {
        if (queryIntentActivitiesMethod == null) queryIntentActivitiesMethod = manager.getClass().getMethod("queryIntentActivities", Intent.class, String.class, long.class, int.class);
        return queryIntentActivitiesMethod;
    }
    private Method getGetHomeActivitiesMethod() throws NoSuchMethodException {
        if (getHomeActivitiesMethod == null){
            getHomeActivitiesMethod = manager.getClass().getMethod("getHomeActivities", List.class);
        }
        return getHomeActivitiesMethod;
    }

    private Method getGetChangedPackagesMethod() throws NoSuchMethodException {
        if (getChangedPackagesMethod == null){
            getChangedPackagesMethod = manager.getClass().getMethod("getChangedPackages", int.class, int.class);
        }
        return getChangedPackagesMethod;
    }

    public ComponentName getHomeActivities(List<ResolveInfo> outHomeCandidates){
        try {
            return (ComponentName) getGetHomeActivitiesMethod().invoke(manager, outHomeCandidates);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName){
        try {
            return (ApplicationInfo) getApplicationInfoMethod().invoke(manager, packageName , 0 , UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public List<ResolveInfo> queryIntentActivities(Intent intent, String resolvedType, long flags){
        try {
            Object obj = getQueryIntentActivitiesMethod().invoke(manager, intent, resolvedType, flags, UserManager.myUserId());
            return (List<ResolveInfo>) obj.getClass().getMethod("getList").invoke(obj);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public static String getApkPath(PackageManager packageManager, String packageName) {
        //获取applicationInfo
        ApplicationInfo applicationInfo = packageManager.getApplicationInfo(packageName);
        return applicationInfo.sourceDir;
    }
    public ArraySet<String> getInstalledApplications() {

        ArraySet<String> a = new ArraySet<>();

        try {
            Object parceledListSlice = getInstalledApplicationsMethod().invoke(manager, 0, UserManager.myUserId());
            // 通过反射调用 getList 方法
            Method getListMethod = parceledListSlice.getClass().getDeclaredMethod("getList");
            getListMethod.setAccessible(true);
            for (ApplicationInfo applicationInfo : (List<ApplicationInfo>) getListMethod.invoke(parceledListSlice)){
                a.add(applicationInfo.packageName);
            }
            return a;
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public List<String> getAllPackages(){
        try {
            return (List<String>) manager.getClass().getMethod("getAllPackages").invoke(manager);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public String getAppNameForPackageName(Context context ,String packageName) {
        try{
            ApplicationInfo appInfo = (ApplicationInfo) getApplicationInfoMethod().invoke(manager, packageName , 0 , UserManager.myUserId());
            return (String) appInfo.loadLabel(context.getPackageManager());
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public int getUidForPackageName(String packageName){

        try{
            ApplicationInfo appInfo = (ApplicationInfo) getApplicationInfoMethod().invoke(manager, packageName , 0 , UserManager.myUserId());

            return (int) appInfo.uid;
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public ChangedPackages getChangedPackages(){
        try {
            return (ChangedPackages) getGetChangedPackagesMethod().invoke(manager, 0, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }


    public String getNameForUid(int uid){
        try {
            Method getNameForUid = getNameForUidMethod();
            return (String) getNameForUid.invoke(manager, uid);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public PackageInfo getPackageInfo(String packageName){
        try {
            return (PackageInfo) getGetPackageInfoMethod().invoke(manager, packageName, 0, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

   public boolean isPackageAvailable(String packageName){
        try {
            return (boolean) getIsPackageAvailableMethod().invoke(manager, packageName, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public int checkPermission(String permName, String pkgName){
        try {
            Log.i("PackageManager","checkPermission "+ permName +" call from pkgName "+pkgName);
            Method checkPermission = getCheckPermissionMethod();
            return (int) checkPermission.invoke(manager, permName, pkgName, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public boolean isSystemApp(String packageName) {

        try {
            ApplicationInfo appInfo = (ApplicationInfo) getApplicationInfoMethod().invoke(manager, packageName , 0 , UserManager.myUserId());
            return (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public boolean getBlockUninstallForUserReflect(String packageName){
        try {
            // 调用 getBlockUninstallForUser 方法并获取返回值
            return (boolean) manager.getClass().getMethod("getBlockUninstallForUser", String.class, int.class).invoke(manager, packageName, UserManager.myUserId());
        } catch (Exception e2) {
            // 捕获异常并抛出运行时异常
            throw new RuntimeException(e2);
        }
    }

    private String getTag(){
        return PackageManager.class.getSimpleName();
    }
}
