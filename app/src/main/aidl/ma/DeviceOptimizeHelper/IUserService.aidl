// IUserService.aidl
package ma.DeviceOptimizeHelper;

import android.os.UserHandle;
import android.content.ComponentName;

interface IUserService {
    void onCreate() = 1;
    void onDestroy() = 2;

    // 20以内的transact code是保留给未来的Dhizuku APi使用的。
    // transact codes up to 20 are reserved for future Dhizuku API.
    //void uninstall(String packageName) = 21;

    void setApplicationHidden(String packageName, boolean state) = 22;

    void setOrganizationName(String name) = 23;

    //void lockNow() = 24;

    //void switchCameraDisabled() = 25;

    //void setGlobalProxy(String url) = 26;

    //UserHandle createUser(String name) = 27;

    //void removeUser(int userId) = 28;

    void clearUserRestriction(in ComponentName who, in String key) = 29;

    void addUserRestriction(in ComponentName who, in String key) = 30;

    //boolean canUsbDataSignalingBeDisabled() = 31;

    //void setUsbDataSignalingEnabled (boolean enabled) = 32;

    //boolean isUsbDataSignalingEnabled() = 33;
}