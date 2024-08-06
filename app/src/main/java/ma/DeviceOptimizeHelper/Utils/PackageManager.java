package ma.DeviceOptimizeHelper.Utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.IInterface;
import android.util.ArraySet;

import java.lang.reflect.Method;
import java.util.List;

public class PackageManager {

    private final IInterface manager;

    public PackageManager(IInterface manager){
        this.manager = manager;
    }


    public PackageInfo getPackageInfo(String packageName){
        try {
            return (PackageInfo) ReflectUtil.callObjectMethod(manager, "getPackageInfo", manager.getClass(), new Class[]{String.class, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? long.class : int.class , int.class}, packageName, 0, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName){
        try {
            return (ApplicationInfo) ReflectUtil.callObjectMethod(manager, "getApplicationInfo", manager.getClass(), new Class[]{String.class, (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) ? long.class : int.class , int.class}, packageName , 0 , UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public String getApkPath(String packageName) {
        return getApplicationInfo(packageName).sourceDir;
    }

    public ArraySet<String> getInstalledPackageName(long flag) {

        ArraySet<String> a = new ArraySet<>();
        try {
            Object parceledListSlice = ReflectUtil.callObjectMethod2(manager, "getInstalledApplications", flag, UserManager.myUserId());
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


    public String getAppNameForPackageName(String packageName) {
        return (String) getApplicationInfo(packageName).loadLabel(ContextUtils.getContext().getPackageManager());
    }

    public int getUidForPackageName(String packageName){
        return (int) getApplicationInfo(packageName).uid;
    }

    public String getNameForUid(int uid){
        return (String) ReflectUtil.callObjectMethod(manager,  "getNameForUid", manager.getClass(), new Class[]{int.class}, new Object[]{uid});
    }

    public int checkUidPermission(String permName, int uid){
        return (int) ReflectUtil.callObjectMethod(manager, "checkUidPermission", manager.getClass(), new Class[]{String.class, int.class}, new Object[]{permName, uid});
    }

}
