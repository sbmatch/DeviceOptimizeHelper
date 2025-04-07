package com.sbmatch.deviceopt.Interface;

import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.SuspendDialogInfo;
import android.os.RemoteException;

import androidx.annotation.Keep;

import java.util.List;
import java.util.logging.Logger;

import ma.DeviceOptimizeHelper.IUserService;

public class DhizukuUserServiceImpl extends IUserService.Stub {
    DevicePolicyManager devicePolicyManager;
    private final Logger logger = Logger.getLogger("DhizukuUserServiceImpl");
    private final static ComponentName admin = com.sbmatch.deviceopt.utils.SystemServiceWrapper.DevicePolicyManager.getDeviceOwnerComponent();
    @Keep
    public DhizukuUserServiceImpl(Context context) {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onCreate() throws RemoteException {
        logger.info("DhizukuUserServiceImpl onCreate");
    }

    @Override
    public void onDestroy() throws RemoteException {
        logger.info("DhizukuUserServiceImpl onDestroy");
        System.exit(0);
    }

    @Override
    public void setApplicationHidden(String packageName, boolean state) throws RemoteException {
        devicePolicyManager.setApplicationHidden(admin, packageName, state);
    }

    @Override
    public void setOrganizationName(String name) throws RemoteException{
        devicePolicyManager.setOrganizationName(admin, name);
    }

    @Override
    public void clearUserRestriction(String key) throws RemoteException {
        devicePolicyManager.clearUserRestriction(admin, key);
    }

    @Override
    public void addUserRestriction(String key) throws RemoteException {
        devicePolicyManager.addUserRestriction(admin, key);
    }

    @Override
    public void setBlackListEnable(boolean isEnable) throws RemoteException {

    }

    @Override
    public void setDisallowRunningList(List<String> list) throws RemoteException {

    }

    @Override
    public List<String> getNotDisallowList() throws RemoteException {
        return null;
    }

    @Override
    public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall) throws RemoteException {
        devicePolicyManager.setUninstallBlocked(admin, packageName, blockUninstall);
        return isPackageSuspended(packageName);
    }

    @Override
    public String[] setPackagesSuspended(String[] packageNames, boolean suspended, SuspendDialogInfo dialogInfo) throws RemoteException {
        return devicePolicyManager.setPackagesSuspended(admin, packageNames, suspended);
    }

    @Override
    public boolean isPackageSuspended(String packageName) throws RemoteException {
        try {
            return devicePolicyManager.isPackageSuspended(admin, packageName);
        }catch (PackageManager.NameNotFoundException e){
            return false;
        }
    }

    @Override
    public void forceStopPackage(String packageName) throws RemoteException {

    }


}