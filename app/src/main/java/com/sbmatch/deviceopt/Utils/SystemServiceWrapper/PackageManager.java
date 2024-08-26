package com.sbmatch.deviceopt.Utils.SystemServiceWrapper;

import android.content.ComponentName;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.SuspendDialogInfo;
import android.os.Build;
import android.os.IInterface;
import android.os.Process;
import android.util.ArraySet;

import com.sbmatch.deviceopt.Utils.ContextUtil;
import com.sbmatch.deviceopt.Utils.ReflectUtil;
import com.sbmatch.deviceopt.Utils.UserHandle;

import java.lang.reflect.Method;
import java.util.List;

public class PackageManager {
    private final IInterface manager;
    public PackageManager(IInterface manager){
        this.manager = manager;
    }


    public PackageInfo getPackageInfo(String packageName){
        try {
            return (PackageInfo) ReflectUtil.callObjectMethod2(manager, "getPackageInfo", packageName, 0, UserHandle.myUserId());
        }catch (Throwable e){
            return null;
        }
    }

    public ApplicationInfo getApplicationInfo(String packageName){
        try {
            return (ApplicationInfo) ReflectUtil.callObjectMethod2(manager, "getApplicationInfo", packageName , 0 , UserHandle.myUserId());
        }catch (Throwable e){
            return null;
        }
    }

    public String getApkPath(String packageName) {
        return getApplicationInfo(packageName).sourceDir;
    }

    public List<ApplicationInfo> getInstalledApplications(Object flag) {

        ArraySet<String> a = new ArraySet<>();
        try {
            Object parceledListSlice = ReflectUtil.callObjectMethod2(manager, "getInstalledApplications", flag, UserHandle.myUserId());
            // 通过反射调用 getList 方法
            Method getListMethod = parceledListSlice.getClass().getDeclaredMethod("getList");
            getListMethod.setAccessible(true);
            return (List<ApplicationInfo>) getListMethod.invoke(parceledListSlice);
        }catch (Throwable e){
            return null;
        }
    }


    public String getAppNameForPackageName(String packageName) {
        return (String) getApplicationInfo(packageName).loadLabel(ContextUtil.getContext().getPackageManager());
    }

    public int getUidForPackageName(String packageName){
        return (int) getApplicationInfo(packageName).uid;
    }

    public String getNameForUid(int uid){
        return (String) ReflectUtil.callObjectMethod2(manager,  "getNameForUid", uid);
    }

    public int checkUidPermission(String permName, int uid){
        return (int) ReflectUtil.callObjectMethod2(manager, "checkUidPermission", permName, uid);
    }

    public String[] getPackagesForUid(int uid){
        return (String[]) ReflectUtil.callObjectMethod2(manager, "getPackagesForUid", uid);
    }


    public boolean getApplicationHiddenSettingAsUser(String packageName){
        return (boolean) ReflectUtil.callObjectMethod2(manager, "getApplicationHiddenSettingAsUser", packageName, UserHandle.myUserId());
    }

    public void setApplicationHiddenSettingAsUser(String packageName, boolean hidden){
        ReflectUtil.callObjectMethod2(manager, "setApplicationHiddenSettingAsUser", packageName, hidden, UserHandle.myUserId());
    }

    public void setComponentEnabledSetting(ComponentName componentName,
                                           int newState,
                                           int flags,
                                           int userId,
                                           String callingPackage){

    }

    public boolean getBlockUninstallForUser(String packageName){
        return (boolean) ReflectUtil.callObjectMethod2(manager, "getBlockUninstallForUser", packageName, UserHandle.myUserId());
    }

    public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall){
      return (boolean) ReflectUtil.callObjectMethod2(manager, "setBlockUninstallForUser", packageName, blockUninstall, UserHandle.myUserId());
    }


    public boolean isPackageSuspendedForUser(String packageName){
        return (boolean) ReflectUtil.callObjectMethod2(manager, "isPackageSuspendedForUser", packageName, UserHandle.myUserId());
    }

    public String[] setPackagesSuspendedAsUser(String[] packageNames, boolean suspended, SuspendDialogInfo dialogInfo){
        int callingUid = Process.myUid();  // 获取调用者的 UID
        String resultCallingPackage = (callingUid == 2000)
                ? "com.android.shell"
                : (callingUid == 1000)
                ? "android"
                : getNameForUid(callingUid);

        switch (Build.VERSION.SDK_INT){
            case 35:
                return (String[]) ReflectUtil.callObjectMethod2(manager, "setPackagesSuspendedAsUser",
                        packageNames,
                        suspended,
                        null,
                        null,
                        dialogInfo,
                        ReflectUtil.getStaticObjectField(android.content.pm.PackageManager.class, "FLAG_SUSPEND_QUARANTINED"),
                        resultCallingPackage,
                        UserHandle.myUserId(),
                        -1);

            default:
                return (String[]) ReflectUtil.callObjectMethod2(manager, "setPackagesSuspendedAsUser",
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
