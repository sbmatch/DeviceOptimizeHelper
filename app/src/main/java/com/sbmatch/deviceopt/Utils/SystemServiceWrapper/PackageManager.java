package com.sbmatch.deviceopt.utils.SystemServiceWrapper;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.SuspendDialogInfo;
import android.os.Build;
import android.os.IInterface;
import android.os.Process;
import android.util.Log;

import com.sbmatch.deviceopt.utils.ReflectUtils;
import com.sbmatch.deviceopt.utils.UserHandle;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

public class PackageManager {
    private final IInterface manager;
    public PackageManager(IInterface manager){
        this.manager = manager;
    }


    public PackageInfo getPackageInfo(String packageName){
        try {
            return (PackageInfo) ReflectUtils.callObjectMethod2(manager, "getPackageInfo", packageName, 0, UserHandle.myUserId());
        }catch (Throwable e){
            return null;
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName){
        try {
            return (ApplicationInfo) ReflectUtils.callObjectMethod2(manager, "getApplicationInfo", packageName , 0 , UserHandle.myUserId());
        }catch (Throwable e){
            return null;
        }
    }

    public String getApkPath(String packageName) {
        return getApplicationInfo(packageName).sourceDir;
    }

    public List<ApplicationInfo> getInstalledApplications(Object flag) {

        try {
            Object parceledListSlice = ReflectUtils.callObjectMethod2(manager, "getInstalledApplications", flag, UserHandle.myUserId());
            // 通过反射调用 getList 方法
            Method getListMethod = parceledListSlice.getClass().getDeclaredMethod("getList");
            getListMethod.setAccessible(true);
            return (List<ApplicationInfo>) getListMethod.invoke(parceledListSlice);

        }catch (Throwable e){
            return null;
        }
    }


    public String getAppNameForPackageName(String packageName) {
        return (String) getApplicationInfo(packageName).loadLabel(ActivityThread.currentApplication().getPackageManager());
    }

    public int getUidForPackageName(String packageName){
        return getApplicationInfo(packageName).uid;
    }

    public String getNameForUid(int uid){
        return (String) ReflectUtils.callObjectMethod2(manager,  "getNameForUid", uid);
    }

    public int checkUidPermission(String permName, int uid){
        return (int) ReflectUtils.callObjectMethod2(manager, "checkUidPermission", permName, uid);
    }

    public String[] getPackagesForUid(int uid){
        return (String[]) ReflectUtils.callObjectMethod2(manager, "getPackagesForUid", uid);
    }


    public boolean getApplicationHiddenSettingAsUser(String packageName){
        return (boolean) ReflectUtils.callObjectMethod2(manager, "getApplicationHiddenSettingAsUser", packageName, UserHandle.myUserId());
    }

    public void setApplicationHiddenSettingAsUser(String packageName, boolean hidden){
        ReflectUtils.callObjectMethod2(manager, "setApplicationHiddenSettingAsUser", packageName, hidden, UserHandle.myUserId());
    }

    public void setComponentEnabledSetting(ComponentName componentName,
                                           int newState,
                                           int flags,
                                           int userId,
                                           String callingPackage){

    }

    public boolean getBlockUninstallForUser(String packageName){
        return (boolean) ReflectUtils.callObjectMethod2(manager, "getBlockUninstallForUser", packageName, UserHandle.myUserId());
    }

    public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall){
      return (boolean) ReflectUtils.callObjectMethod2(manager, "setBlockUninstallForUser", packageName, blockUninstall, UserHandle.myUserId());
    }


    public boolean isPackageSuspendedForUser(String packageName){
        return (boolean) ReflectUtils.callObjectMethod2(manager, "isPackageSuspendedForUser", packageName, UserHandle.myUserId());
    }

    public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, SuspendDialogInfo dialogInfo){
        int callingUid = Process.myUid();  // 获取调用者的 UID
        String resultCallingPackage = (callingUid == 2000)
                ? "com.android.shell"
                : (callingUid == 1000) || (callingUid == 0)
                ? "android"
                : getNameForUid(callingUid);

        Log.i(this.getClass().getSimpleName(), Arrays.toString(packageNames)+", "+suspended +", "+resultCallingPackage);
        switch (Build.VERSION.SDK_INT){
            case 35:
                return (String[]) ReflectUtils.callObjectMethod2(manager, "setPackagesSuspendedAsUser",
                        packageNames,
                        suspended,
                        null,
                        null,
                        dialogInfo,
                        ReflectUtils.getObjectField(android.content.pm.PackageManager.class, "FLAG_SUSPEND_QUARANTINED"),
                        resultCallingPackage,
                        UserHandle.myUserId(),
                        -1);

            default:
                return (String[]) ReflectUtils.callObjectMethod2(manager, "setPackagesSuspendedAsUser",
                        packageNames,
                        suspended,
                        null,
                        null,
                        dialogInfo,
                        resultCallingPackage,
                        UserHandle.myUserId());
        }
    }

}
