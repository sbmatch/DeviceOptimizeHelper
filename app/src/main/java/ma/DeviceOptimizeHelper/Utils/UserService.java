package ma.DeviceOptimizeHelper.Utils;


import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.net.ProxyInfo;
import android.net.Uri;
import android.os.Build;
import android.os.RemoteException;
import android.os.UserHandle;

import androidx.annotation.Keep;
import androidx.core.os.UserHandleCompat;

import com.rosan.dhizuku.shared.DhizukuVariables;

import ma.DeviceOptimizeHelper.IUserService;

public class UserService extends IUserService.Stub {
    private Context context;

    private DevicePolicyManager devicePolicyManager;

    @Keep
    public UserService(Context context) {
        this.context = context;
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @SuppressLint("MissingPermission")
    @Override
    public void uninstall(String packageName) {
        PackageInstaller packageInstaller = context.getPackageManager().getPackageInstaller();
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(), Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ? PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE : PendingIntent.FLAG_UPDATE_CURRENT);
        packageInstaller.uninstall(packageName, pendingIntent.getIntentSender());
    }

    @Override
    @SuppressLint("DiscouragedPrivateApi")
    public void setApplicationHidden(String packageName, boolean state) throws RemoteException {
        devicePolicyManager.setApplicationHidden(DhizukuVariables.COMPONENT_NAME, packageName, state);
    }

    @Override
    public void setOrganizationName(String name) {
        devicePolicyManager.setOrganizationName(DhizukuVariables.COMPONENT_NAME, name);
    }

    @Override
    public void lockNow() {
        devicePolicyManager.lockNow();
    }

    @Override
    public void switchCameraDisabled() {
        boolean currentState = devicePolicyManager.getCameraDisabled(DhizukuVariables.COMPONENT_NAME);
        devicePolicyManager.setCameraDisabled(DhizukuVariables.COMPONENT_NAME, !currentState);
    }

    @Override
    public void setGlobalProxy(String url) {
        ProxyInfo proxy = null;
        if (!url.isEmpty()) {
            if (url.startsWith("http") || url.startsWith("https")) {
                Uri uri = Uri.parse(url);
                proxy = ProxyInfo.buildPacProxy(uri);
            } else {
                String[] urlElements = url.split(":");
                if (urlElements.length != 2) return;
                proxy = ProxyInfo.buildDirectProxy(urlElements[0], Integer.parseInt(urlElements[1]));
            }
        }
    }

    @SuppressLint("InlinedApi")
    @Override
    public UserHandle createUser(String name) {
        return devicePolicyManager.createAndManageUser(DhizukuVariables.COMPONENT_NAME, name, DhizukuVariables.COMPONENT_NAME, null, DevicePolicyManager.LEAVE_ALL_SYSTEM_APPS_ENABLED);
    }

    @Override
    public void removeUser(int userId) {
        devicePolicyManager.removeUser(DhizukuVariables.COMPONENT_NAME, UserHandleCompat.getUserHandleForUid(userId * 100000));
    }

    /**
     * @param who
     * @param key
     * @throws RemoteException
     */
    @Override
    public void clearUserRestriction(ComponentName who, String key) throws RemoteException {
        devicePolicyManager.clearUserRestriction(who, key);
    }

    /**
     * @param who
     * @param key
     * @throws RemoteException
     */
    @Override
    public void addUserRestriction(ComponentName who, String key) throws RemoteException {
        devicePolicyManager.addUserRestriction(who, key);
    }

}