package com.ma.enterprisemodepolicymanager.Services;

import android.graphics.Bitmap;
import android.os.RemoteException;

import com.ma.enterprisemodepolicymanager.IDeviceOptService;
import com.ma.enterprisemodepolicymanager.Utils.AnyRestrictPolicyUtils;
import com.ma.enterprisemodepolicymanager.Utils.ServiceManager;
import com.ma.enterprisemodepolicymanager.Utils.ShellUtils;
import com.miui.enterprise.sdk.IEpDeletePackageObserver;
import com.miui.enterprise.sdk.IEpInstallPackageObserver;

import java.util.List;

import com.ma.enterprisemodepolicymanager.Utils.PackageManager;

public class DeviceOptServiceImpl extends IDeviceOptService.Stub {
    private static final String TAG = "DeviceOptServiceImpl";
    private static DeviceOptServiceImpl deviceOptService;
    private static final PackageManager packageManager = ServiceManager.getPackageManager();

    private DeviceOptServiceImpl() {

    }

    public static synchronized DeviceOptServiceImpl getInstance() {
        if (deviceOptService == null) {
            deviceOptService = new DeviceOptServiceImpl();
        }
        return deviceOptService;
    }

    @Override
    public void setUserRestrict(String key, boolean newValue) {
        AnyRestrictPolicyUtils.setUserRestrict(key, newValue);
    }

    @Override
    public void setEntRestrict(String key, boolean newValue) {
        AnyRestrictPolicyUtils.setEntRestrict(key, newValue);
    }

    @Override
    public void setAllEntRestrict(boolean newValue) {
        AnyRestrictPolicyUtils.setAllEntRestrict(newValue);
    }

    @Override
    public void setAllUserRestrict(boolean newValue) {
        AnyRestrictPolicyUtils.setAllUserRestrict(newValue);
    }

    @Override
    public void forceInstallByEnt(String path, IEpInstallPackageObserver observer) {
        AnyRestrictPolicyUtils.forceInstallByEnt(path, observer);
    }

    @Override
    public void removeDeviceIdleAndAllRestrict() {

    }

    @Override
    public void setControlStatus(String key, int value) {
        AnyRestrictPolicyUtils.setControlStatus(key, value);
    }

    @Override
    public List<String> getAllPackages() {
        return AnyRestrictPolicyUtils.getAllPackages();
    }

    @Override
    public String getDefaultHome() {
        return AnyRestrictPolicyUtils.getDefaultHome();
    }

    @Override
    public void setDefaultHome(String pkgName) {
        AnyRestrictPolicyUtils.setDefaultHome(pkgName);
    }

    @Override
    public void setHiddenWatermark(boolean status) throws RemoteException {
        AnyRestrictPolicyUtils.setHiddenWatermark(status);
    }

    @Override
    public boolean getHiddenWatermark() throws RemoteException {
        return AnyRestrictPolicyUtils.getHiddenWatermark();
    }

    @Override
    public String getAPIVersion() throws RemoteException {
        return AnyRestrictPolicyUtils.getAPIVersion();
    }

    @Override
    public void enableUsbDebug(boolean enable) throws RemoteException {
        AnyRestrictPolicyUtils.enableUsbDebug(enable);
    }

    @Override
    public List<String> getUrlBlackList() throws RemoteException {
        return AnyRestrictPolicyUtils.getUrlBlackList();
    }

    @Override
    public void setUrlBlackList(List<String> urls) throws RemoteException {
        AnyRestrictPolicyUtils.setUrlBlackList(urls);
    }

    @Override
    public void setBrowserRestriction(int mode) throws RemoteException {
        AnyRestrictPolicyUtils.setBrowserRestriction(mode);
    }

    @Override
    public Bitmap captureScreen() throws RemoteException {
        return AnyRestrictPolicyUtils.captureScreen();
    }

    @Override
    public int getWifiConnRestriction() {
        return AnyRestrictPolicyUtils.getWifiConnRestriction();
    }

    @Override
    public void setWifiConnRestriction(int mode) {
        AnyRestrictPolicyUtils.setWifiConnRestriction(mode);
    }

    @Override
    public void deviceReboot() {
        AnyRestrictPolicyUtils.deviceReboot();
    }

    @Override
    public void deviceShutDown() {
        AnyRestrictPolicyUtils.deviceShutDown();
    }

    @Override
    public void recoveryFactory(boolean formatSdcard) {
        AnyRestrictPolicyUtils.recoveryFactory(formatSdcard);
    }

    @Override
    public List<String> getWifiApSsidBlackList() {
        return AnyRestrictPolicyUtils.getWifiApSsidBlackList();
    }



    @Override
    public List<String> getWifiApSsidWhiteList() throws RemoteException {
        return AnyRestrictPolicyUtils.getWifiApSsidWhiteList();
    }

    @Override
    public void deletePackage(String packageName, IEpDeletePackageObserver observer) throws RemoteException {
        //AnyRestrictPolicyUtils.deletePackage(packageName);
        ServiceManager.getEnterpriseManager().getApplicationManager().deletePackage(packageName, packageManager.isSystemApp(packageName) ? 4 : 2, observer);
    }

    @Override
    public String execCommand(String commands) throws RemoteException {
        return ShellUtils.execCommand(commands);
    }


}