package com.sbmatch.deviceopt.Interface;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.SuspendDialogInfo;
import android.os.RemoteException;

import androidx.annotation.Keep;

import com.sbmatch.deviceopt.Utils.SystemServiceWrapper.DevicePolicyManager;

import java.util.List;

import ma.DeviceOptimizeHelper.IUserService;

public class DhizukuUserServiceImpl extends IUserService.Stub {
    DevicePolicyManager devicePolicyManager;
    @Keep
    public DhizukuUserServiceImpl(Context context) {
        devicePolicyManager = DevicePolicyManager.get(context);
    }

    @Override
    public void onCreate() throws RemoteException {

    }

    @Override
    public void onDestroy() throws RemoteException {

    }

    @Override
    public void setApplicationHidden(String packageName, boolean state) throws RemoteException {

    }

    @Override
    public void setOrganizationName(String name) throws RemoteException{
        devicePolicyManager.setOrganizationName(name);
    }

    @Override
    public void clearUserRestriction(ComponentName who, String key) throws RemoteException {
        devicePolicyManager.clearUserRestriction(key);
    }

    @Override
    public void addUserRestriction(ComponentName who, String key) throws RemoteException {
        devicePolicyManager.addUserRestriction(key);
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
        //devicePolicyManager.setUninstallBlocked(Dhizuku.getOwnerComponent(), Dhizuku.getOwnerPackageName(), packageName, blockUninstall);
        return false;
    }

    @Override
    public String[] setPackagesSuspended(String[] packageNames, boolean suspended, SuspendDialogInfo dialogInfo) throws RemoteException {
        return devicePolicyManager.setPackagesSuspended(packageNames, suspended, dialogInfo);
    }

    @Override
    public boolean isPackageSuspended(String packageName) throws RemoteException {
        return devicePolicyManager.isPackageSuspended(packageName);
    }

    @Override
    public void forceStopPackage(String packageName) throws RemoteException {

    }


}