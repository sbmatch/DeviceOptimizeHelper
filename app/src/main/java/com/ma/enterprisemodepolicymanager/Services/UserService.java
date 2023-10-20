package com.ma.enterprisemodepolicymanager.Services;


import android.app.admin.DevicePolicyManager;
import android.app.admin.IDevicePolicyManager;
import android.content.Context;
import android.os.RemoteException;

import androidx.annotation.Keep;

import com.ma.enterprisemodepolicymanager.IUserService;
import com.ma.enterprisemodepolicymanager.Utils.ServiceManager;

public abstract class UserService extends IUserService.Stub {
    private final IDevicePolicyManager idevicePolicyManager = IDevicePolicyManager.Stub.asInterface(ServiceManager.getService("device_policy"));
    private Context context;

    private DevicePolicyManager devicePolicyManager;

    @Keep
    public UserService(Context context) {
        this.context = context;
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onCreate() throws RemoteException {
        System.out.println("++++++++++++onCreate+++++++++++++++");
    }

    @Override
    public void onDestroy() throws RemoteException {
        System.out.println("++++++++++++onDestroy+++++++++++++++");
        System.exit(0);
    }

//    @Override
//    public Bundle getApplicationRestrictions(ComponentName who, String callerPackage, String packageName) throws RemoteException {
//        return idevicePolicyManager.getApplicationRestrictions(who, callerPackage, packageName);
//    }
//
//    @Override
//    public void setApplicationRestrictions(ComponentName who, String callerPackage, String packageName, Bundle settings) throws RemoteException {
//        idevicePolicyManager.setApplicationRestrictions(who, callerPackage, packageName, settings);
//    }
//
//    @Override
//    public int getPermissionGrantState(ComponentName admin, String callerPackage, String packageName, String permission) throws RemoteException {
//        return idevicePolicyManager.getPermissionGrantState(admin, callerPackage, packageName, permission);
//    }
//
//    @Override
//    public boolean setPermissionGrantState(ComponentName admin, String callerPackage, String packageName, String permission, int grantState) throws RemoteException {
//        return devicePolicyManager.setPermissionGrantState(admin, packageName, permission, grantState);
//    }
//
//    @Override
//    public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled) throws RemoteException {
//        ServiceManager.getNotificationManager().setNotificationsEnabledForPackage(pkg, uid, enabled);
//    }
//
//    @Override
//    public void setUserRestriction(ComponentName who, String key, boolean enable, boolean parent) throws RemoteException {
//        idevicePolicyManager.setUserRestriction(who, key, enable, parent);
//    }
}