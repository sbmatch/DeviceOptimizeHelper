package com.sbmatch.deviceopt.utils.SystemServiceWrapper;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.SuspendDialogInfo;
import android.os.Binder;
import android.os.Build;
import android.os.UserHandle;

import com.kongzue.dialogx.dialogs.PopNotification;
import com.sbmatch.deviceopt.utils.ReflectUtils;
import com.sbmatch.deviceopt.utils.dhizuku.DPMHelperKt;

import java.util.Collection;

public class DevicePolicyManager {
    private static DevicePolicyManager devicePolicyManager;
    private android.app.admin.DevicePolicyManager dpm;
    private static ComponentName adminComponent;

    private DevicePolicyManager(String targetPackage){

        switch (targetPackage){
            case "com.rosan.dhizuku": //适配Dhizuku
                dpm = DPMHelperKt.binderWrapperDevicePolicyManager();
                break;
            default:

        }

    }

    public static DevicePolicyManager get(){
        try {
            adminComponent = getDeviceOwnerComponent();
            devicePolicyManager = new DevicePolicyManager(getDeviceOwnerComponent().getPackageName());
            return devicePolicyManager;
        }catch (Throwable e) {
            PopNotification.show("此设备上没有已激活DeviceOwner的应用程序。").showLong().iconError();
            return null;
        }
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
        try {
            return (CharSequence) ReflectUtils.callObjectMethod2(dpm, "getDeviceOwnerOrganizationName");
        }catch (Throwable e){
            return null;
        }
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
       try {
           return (ComponentName) ReflectUtils.callObjectMethod2(ServiceManager.getServiceInterface("device_policy", "android.app.admin.IDevicePolicyManager"), "getDeviceOwnerComponent", true);
       }catch (Throwable e) {return null;}
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

    public boolean bindDeviceAdminService(Intent serviceIntent, ServiceConnection conn,
                   Context.BindServiceFlags flags){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            return dpm.bindDeviceAdminServiceAsUser(adminComponent, serviceIntent, conn, flags, UserHandle.getUserHandleForUid(Binder.getCallingUid()));
        }
        return false;
    }

    public void setCameraDisabled(boolean disabled){
        dpm.setCameraDisabled(adminComponent, disabled);
    }

    public boolean getCameraDisabled(){
        return dpm.getCameraDisabled(adminComponent);
    }
}
