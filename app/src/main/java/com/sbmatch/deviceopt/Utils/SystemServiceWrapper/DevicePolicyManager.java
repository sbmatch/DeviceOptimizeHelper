package com.sbmatch.deviceopt.Utils.SystemServiceWrapper;

import android.app.admin.IDevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.SuspendDialogInfo;
import android.os.IInterface;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuBinderWrapper;
import com.sbmatch.deviceopt.Utils.ContextUtil;
import com.sbmatch.deviceopt.Utils.ReflectUtils;

import java.util.Collection;

public class DevicePolicyManager {
    private static DevicePolicyManager devicePolicyManager;
    private android.app.admin.DevicePolicyManager dpm;
    private static ComponentName adminComponent;

    private DevicePolicyManager(String targetPackage){
        android.app.admin.DevicePolicyManager dpm2 = (android.app.admin.DevicePolicyManager)
                ContextUtil.createPackageContext(targetPackage).getSystemService(Context.DEVICE_POLICY_SERVICE);
        IInterface anInterface = (IInterface) ReflectUtils.getObjectField(dpm2, "mService");
        switch (targetPackage){
            case "com.rosan.dhizuku": //适配Dhizuku
                if (anInterface instanceof DhizukuBinderWrapper)  dpm = dpm2;
                ReflectUtils.setObjectField(dpm2,
                        "mService",
                        IDevicePolicyManager.Stub.asInterface(Dhizuku.binderWrapper(anInterface.asBinder())));
                dpm = dpm2;
                break;
            default:

        }

    }
    public static DevicePolicyManager get(){
        adminComponent = getDeviceOwnerComponent();
        devicePolicyManager = new DevicePolicyManager(getDeviceOwnerComponent().getPackageName());
        return devicePolicyManager;
    }

    public static DevicePolicyManager get(Context mContext){
        devicePolicyManager = new DevicePolicyManager(mContext);
        return devicePolicyManager;
    }

    private DevicePolicyManager(Context context){
        dpm = (android.app.admin.DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }


    public static Collection<Object> getDelegationScopesWithReflect(){
        return ReflectUtils.getFieldsByPrefixMatch(android.app.admin.DevicePolicyManager.class, "DELEGATION_").values();
    }

    public void setApplicationHidden(String packageName, boolean state) {
        dpm.setApplicationHidden(adminComponent, packageName, state);
    }

    public void setOrganizationName(String name) {
        dpm.setOrganizationName(adminComponent, name);
    }

    public CharSequence getOrganizationName(){
        return (CharSequence) ReflectUtils.callObjectMethod2(dpm, "getDeviceOwnerOrganizationName");
    }

    public boolean addUserRestriction(String key) {
        dpm.addUserRestriction(adminComponent, key);
        return UserManager.get().hasUserRestriction(key);
    }

    public boolean clearUserRestriction(String key) {
        dpm.clearUserRestriction(adminComponent, key);
        return UserManager.get().hasUserRestriction(key);
    }

    public void setUninstallBlocked(String packageName, boolean blockUninstall) {
        dpm.setUninstallBlocked(adminComponent, packageName, blockUninstall);
    }

    public boolean isUninstallBlocked(String packageName) {
        return dpm.isUninstallBlocked(adminComponent, packageName);
    }
    public String[] setPackagesSuspended(String[] packageNames, boolean suspended, SuspendDialogInfo dialogInfo) {
        return dpm.setPackagesSuspended(adminComponent, packageNames, suspended);
    }
    
    public boolean isPackageSuspended(String packageName) {
        return (boolean) ReflectUtils.callObjectMethod2(dpm,"isPackageSuspended", adminComponent, packageName);
    }

    public static ComponentName getDeviceOwnerComponent(){
        return (ComponentName) ReflectUtils.callObjectMethod2(ServiceManager.getServiceInterface("device_policy", "android.app.admin.IDevicePolicyManager"), "getDeviceOwnerComponent", true);
    }

    public boolean setPermissionGrantState(String packageName,String permission, int grantState){
        return dpm.setPermissionGrantState(adminComponent, packageName, permission, grantState);
    }

    public void setScreenCaptureDisabled(boolean disabled){
        dpm.setScreenCaptureDisabled(adminComponent, disabled);
    }

    public boolean getScreenCaptureDisabled() {
        return dpm.getScreenCaptureDisabled(adminComponent);
    }

    public void setCameraDisabled(boolean disabled){
        dpm.setCameraDisabled(adminComponent, disabled);
    }

    public boolean getCameraDisabled(){
        return dpm.getCameraDisabled(adminComponent);
    }
}
