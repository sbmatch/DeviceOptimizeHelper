// IUserService.aidl
package ma.DeviceOptimizeHelper;

import android.os.UserHandle;
import android.content.ComponentName;
import android.os.PersistableBundle;
import android.content.pm.SuspendDialogInfo;

interface IUserService {
    void onCreate() = 1;
    void onDestroy() = 2;

    // 20以内的transact code是保留给未来的Dhizuku APi使用的。
    // transact codes up to 20 are reserved for future Dhizuku API.

    // DevicePolicyManager
    //void uninstall(String packageName) = 21;
    void setApplicationHidden(String packageName, boolean state) = 22;
    void setOrganizationName(String name) = 23;

    void clearUserRestriction(in ComponentName who, in String key) = 29;
    void addUserRestriction(in ComponentName who, in String key) = 30;

    // AppRunningControlManager
    void setBlackListEnable(boolean isEnable) = 40;
    void setDisallowRunningList(in List<String> list) = 41;
    List<String> getNotDisallowList() = 42;

    // PackageManager
    boolean setBlockUninstallForUser(String packageName, boolean blockUninstall) = 50;
    String[] setPackagesSuspended(in String[] packageNames, boolean suspended, in SuspendDialogInfo dialogInfo) = 51;
    boolean isPackageSuspended(String packageName) = 52;

    // ActivityManager
    void forceStopPackage(String packageName) = 60;

}