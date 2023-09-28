package ma.DeviceOptimizeHelper.Utils;

import static ma.DeviceOptimizeHelper.Utils.UserManagerUtils.getIdentifier;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.util.ArraySet;

import java.lang.reflect.Method;
import java.util.List;

import ma.DeviceOptimizeHelper.BaseApplication.BaseApplication;

public class PackageManagerUtils {

    private static Object IPackageManager(){
        try {
            // 通过反射获取 android.content.pm.IPackageManager$Stub 类
            @SuppressLint("PrivateApi")
            Class<?> cStub = Class.forName("android.content.pm.IPackageManager$Stub");
            // 获取 asInterface 方法，用于创建接口的实例
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            // 通过 ServiceManager 获取实例
            return asInterface.invoke(null, ServiceManager.getSystemService("package"));
        } catch (Exception e2) {
            // 捕获异常并抛出运行时异常
            throw new RuntimeException(e2);
        }
    }

    public static Object IPackageManagerNative(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub = Class.forName("android.content.pm.IPackageManagerNative$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            // 通过 ServiceManager 获取实例
            return asInterface.invoke(null, ServiceManager.getSystemService("package_native"));
        } catch (Exception e2) {
            // 捕获异常并抛出运行时异常
            throw new RuntimeException(e2);
        }
    }


    public ArraySet<String> getAllPackageName(Context context) {

        ArraySet<String> allpkgs = new ArraySet<>();
        @SuppressLint("QueryPermissionsNeeded")
        List<PackageInfo> packageInfoList = context.getPackageManager().getInstalledPackages(0);
        for (PackageInfo packageInfo : packageInfoList){
            allpkgs.add(packageInfo.packageName);
        }
        return allpkgs;
    }

    public  String getAppNameForPackageName(Context context, String packageName) {
        try{
            @SuppressLint("QueryPermissionsNeeded")
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName,0);
            return  packageInfo.applicationInfo.loadLabel(BaseApplication.getContext().getPackageManager()).toString();
        }catch (PackageManager.NameNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public Drawable getAppIconForPackageName(Context context, String packageName) {
        try{
            @SuppressLint("QueryPermissionsNeeded")
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(packageName,0);
            return packageInfo.applicationInfo.loadIcon(context.getPackageManager());
        }catch (PackageManager.NameNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public static boolean getBlockUninstallForUserReflect(String packageName){

        try {
            // 调用 getBlockUninstallForUser 方法并获取返回值
            return (boolean) IPackageManager().getClass().getMethod("getBlockUninstallForUser", String.class, int.class).invoke(IPackageManager(), packageName, getIdentifier());
        } catch (Exception e2) {
            // 捕获异常并抛出运行时异常
            throw new RuntimeException(e2);
        }
    }

}
