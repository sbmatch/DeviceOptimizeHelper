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

public abstract class UserService extends IUserService.Stub {
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

    @Override
    public boolean canUsbDataSignalingBeDisabled() throws RemoteException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return devicePolicyManager.canUsbDataSignalingBeDisabled();
        }
        return false;
    }

    @Override
    public void setUsbDataSignalingEnabled(boolean enabled) throws RemoteException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            devicePolicyManager.setUsbDataSignalingEnabled(enabled);
        }
    }

    @Override
    public boolean isUsbDataSignalingEnabled() throws RemoteException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return devicePolicyManager.isUsbDataSignalingEnabled();
        }
        return false;
    }

}