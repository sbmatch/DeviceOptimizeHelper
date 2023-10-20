// IDeviceOptService.aidl
package com.ma.enterprisemodepolicymanager;

import com.miui.enterprise.sdk.IEpDeletePackageObserver;
import com.miui.enterprise.sdk.IEpInstallPackageObserver;


// Declare any non-default types here with import statements

interface IDeviceOptService {
      void setUserRestrict(String key, boolean newValue) = 1000;
      void setEntRestrict(String key, boolean newValue) = 1001;
      void setAllEntRestrict(boolean newValue) = 1002;
      void setAllUserRestrict(boolean newValue) = 1003;
      void forceInstallByEnt(String path, in IEpInstallPackageObserver observer) = 1004;
      void removeDeviceIdleAndAllRestrict() = 1005;
      void setControlStatus(String key, int value) = 1006;
      List<String> getAllPackages() = 1007;
      String getDefaultHome() = 1008;
      void setDefaultHome(String pkgName) = 1009;
      void setHiddenWatermark(boolean status) = 1010;
      boolean getHiddenWatermark() = 1011;
      String getAPIVersion() = 1012;
      void enableUsbDebug(boolean enable) = 1013;
      List<String> getUrlBlackList() = 1014;
      void setUrlBlackList(in List<String> urls) = 1015;
      void setBrowserRestriction(int mode) = 1016;
      Bitmap captureScreen() = 1017;
      int getWifiConnRestriction() = 1018;
      void setWifiConnRestriction(int mode) = 1019;
      void deviceReboot() = 1020;
      void deviceShutDown() = 1021;
      void recoveryFactory(boolean formatSdcard) = 1022;
      List<String> getWifiApSsidBlackList() = 1023;
      List<String> getWifiApSsidWhiteList() = 1025;
      void deletePackage(String packageName, in IEpDeletePackageObserver observer) = 1026;
      String execCommand(String commands) = 1027;

}