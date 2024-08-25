package com.sbmatch.deviceopt.Utils.SystemServiceWrapper;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.SuspendDialogInfo;
import android.os.IInterface;

import com.rosan.dhizuku.api.Dhizuku;
import com.sbmatch.deviceopt.Utils.ContextUtil;
import com.sbmatch.deviceopt.Utils.ReflectUtil;
import com.sbmatch.deviceopt.Utils.UserHandle;

import java.util.Collection;

public class DevicePolicyManager {
    private IInterface manager;
    private static DevicePolicyManager devicePolicyManager;
    private android.app.admin.DevicePolicyManager dpm;
    private static ComponentName adminComponent;

    public DevicePolicyManager(IInterface manager){
        this.manager = manager;
    }

    private DevicePolicyManager(Context context){
        this.dpm = (android.app.admin.DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }
    public static DevicePolicyManager get(){
        if (devicePolicyManager == null) devicePolicyManager = new DevicePolicyManager();
        return devicePolicyManager;
    }
    public static DevicePolicyManager get(Context context){
        if (devicePolicyManager == null) {
            devicePolicyManager = new DevicePolicyManager(context);
            if (adminComponent == null) adminComponent = Dhizuku.getOwnerComponent();
        }
        return devicePolicyManager;
    }
    private DevicePolicyManager(){
        this.dpm = (android.app.admin.DevicePolicyManager) ContextUtil.currentApplication().getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    public static Collection<Object> getDelegationScopesWithReflect(){
        return ReflectUtil.getFieldsByPrefixMatch(android.app.admin.DevicePolicyManager.class, "DELEGATION_").values();
    }

    public void setOrganizationName(String name) {
        if (manager != null) ReflectUtil.callObjectMethod2(manager,
                "setOrganizationName",
                Dhizuku.getOwnerComponent(),
                Dhizuku.getOwnerPackageName(),
                name);
        if (dpm != null) dpm.setOrganizationName(adminComponent, name);
    }

    public void addUserRestriction(String key) {
        if (manager != null) ReflectUtil.callObjectMethod2(manager, "setUserRestriction", Dhizuku.getOwnerComponent(), Dhizuku.getOwnerPackageName(), key, true, true);
        if (dpm != null) dpm.addUserRestriction(adminComponent, key);
    }

    public void clearUserRestriction(String key) {
        if (manager != null) ReflectUtil.callObjectMethod2(manager, "setUserRestriction", Dhizuku.getOwnerComponent(), Dhizuku.getOwnerPackageName(), key, false, true);
        if (dpm != null) dpm.clearUserRestriction(adminComponent, key);
    }

    public void setUninstallBlocked(String packageName, boolean blockUninstall) {
        if (dpm != null) dpm.setUninstallBlocked(adminComponent, packageName, blockUninstall);
        if (manager != null) ReflectUtil.callObjectMethod2(manager, "setUninstallBlocked", Dhizuku.getOwnerComponent(), Dhizuku.getOwnerPackageName(), packageName, blockUninstall);
    }

    public boolean isUninstallBlocked(String packageName) {
        if (dpm != null) return dpm.isUninstallBlocked(adminComponent, packageName);
        if (manager != null) return (boolean) ReflectUtil.callObjectMethod2(manager, "isUninstallBlocked", packageName);
        return false;
    }
    public String[] setPackagesSuspended(String[] packageNames, boolean suspended, SuspendDialogInfo dialogInfo) {
        if (dpm != null) return dpm.setPackagesSuspended(adminComponent, packageNames, suspended);
        if (manager != null) return (String[]) ReflectUtil.callObjectMethod2(manager, "setPackagesSuspended", Dhizuku.getOwnerComponent(), Dhizuku.getOwnerPackageName(), packageNames, suspended);
        return new String[0];
    }
    
    public boolean isPackageSuspended(String packageName) {
        if (dpm != null) return (boolean) ReflectUtil.callObjectMethod2(dpm,"isPackageSuspended", adminComponent, packageName);
        if (manager != null) return (boolean) ReflectUtil.callObjectMethod2(manager,"isPackageSuspended", Dhizuku.getOwnerComponent(), Dhizuku.getOwnerPackageName(), packageName);
        return false;
    }

    public ComponentName getDeviceOwnerComponentOnUser(){
        return (ComponentName) ReflectUtil.callObjectMethod2(manager, "getDeviceOwnerComponentOnUser", UserHandle.myUserId());
    }
}
