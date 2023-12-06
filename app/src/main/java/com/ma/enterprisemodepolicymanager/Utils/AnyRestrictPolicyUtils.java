package com.ma.enterprisemodepolicymanager.Utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.SparseArray;

import androidx.collection.ArrayMap;

import com.ma.enterprisemodepolicymanager.Main;
import com.ma.enterprisemodepolicymanager.Utils.Enterprise.EnterpriseManager;
import com.miui.enterprise.sdk.ApplicationManager;
import com.miui.enterprise.sdk.DeviceManager;
import com.miui.enterprise.sdk.IEpInstallPackageObserver;
import com.miui.enterprise.sdk.PhoneManager;
import com.miui.enterprise.sdk.RestrictionsManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.ma.enterprisemodepolicymanager.BaseApplication.App;

public class AnyRestrictPolicyUtils {
    static PackageManager packageManager = ServiceManager.getPackageManager();
    static IDeviceIdleController iDeviceIdleController = IDeviceIdleController.Stub.asInterface(ServiceManager.getService("deviceidle"));
    static RestrictionsManager restrictionsManager;
    static DeviceManager deviceManager;
    static com.ma.enterprisemodepolicymanager.Utils.Enterprise.DeviceManager ideviceManager ;
    static com.ma.enterprisemodepolicymanager.Utils.Enterprise.ApplicationManager iapplicationManager;
    static ApplicationManager applicationManager ;
    //public static PhoneManager phoneManager = PhoneManager.getInstance();

    static {
        if (ServiceManager.checkService("EnterpriseManager") != null){
            deviceManager = DeviceManager.getInstance();
            restrictionsManager = RestrictionsManager.getInstance();
            applicationManager = ApplicationManager.getInstance();
            iapplicationManager = ServiceManager.getEnterpriseManager().getApplicationManager();
            ideviceManager = ServiceManager.getEnterpriseManager().getDeviceManager();
        }
    }
    private AnyRestrictPolicyUtils(){

    }

    public static void setAllEntRestrict(boolean newValue){
        for (String key : getDisallowsFieldReflect()){
            restrictionsManager.setRestriction(key, newValue);
            System.out.println("Info: 企业限制策略 "+ sEntRestrictionArray.get(key) +" ("+key+") --> "+(restrictionsManager.hasRestriction(key) ? "已禁用" : "正常"));
        }
    }

    public static void setAllUserRestrict(boolean newValue){
        for (String key : UserManager.getALLUserRestrictionsReflectForUserManager()){
            AnyRestrictPolicyUtils.setUserRestrictionReflect(key, newValue);
        }
        System.out.println("Info: 设置全部用户限制...");
    }

    public static void setEntRestrict(String key, boolean newValue){
        if (getDisallowsFieldReflect().contains(key)){
            restrictionsManager.setRestriction(key,newValue);
            System.out.println("Info: 企业限制策略 "+ sEntRestrictionArray.get(key) +" ("+key+") --> "+(restrictionsManager.hasRestriction(key) ? "已禁用" : "已恢复"));
        }else {
            System.err.println("Error: 企业限制字段 "+key+" 不存在，请阅读相关源码或文档");
        }
    }

    public static void setUserRestrict(String key, boolean newValue){
        if (UserManager.getALLUserRestrictionsReflectForUserManager().contains(key)){
            setUserRestrictionReflect(key, newValue);
            System.out.println("Info: 设置用户限制 "+key +" "+newValue);
        }else {
            System.out.println("Info: 用户限制字段 "+key+" 不存在，请阅读相关源码或文档");
        }
    }

    public static int getApplicationSettings(String packageName){
        return applicationManager.getApplicationSettings(packageName, UserManager.myUserId());
    }

    public static String getControlStatus(String key){
        return String.valueOf(restrictionsManager.getControlStatus(key));
    }

    public static void setControlStatus(String key, int value){
        restrictionsManager.setControlStatus(key, value);
        System.out.println("Info: 设置 "+sRestrictionStateArray.get(key) +" --> "+sSystemSwitchStatusArray.get(value));
    }

    public static void forceInstallByEnt(String path, IEpInstallPackageObserver observer){

        if (FilesUtils.isFileExists(path)){
            System.out.println("Info: 正在静默安装...  路径: "+path);
            applicationManager.installPackage(path, 0x004000000, observer);
        }else {
            System.err.println("Error: 指定的安装包不存在");
        }
    }


    public static ArraySet<String> getInstalledApplications(){
        System.out.println("Info: 获取所有已安装应用列表...");
        return packageManager.getInstalledApplications();
    }

    public static List<String> getAllPackages(){
        System.out.println("Info: 获取所有软件包...");
        return packageManager.getAllPackages();
    }

    public static String getDefaultHome(){
        try {
            String defaultHome = (String) DeviceManager.class.getMethod("getDefaultHome").invoke(deviceManager);
            System.out.println("Info: 获取默认桌面...  "+defaultHome);
            return defaultHome;
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public static void setDefaultHome(String pkgName){
        try {
            DeviceManager.class.getMethod("setDefaultHome", String.class).invoke(deviceManager, pkgName);
            System.out.println("Info: 设置默认桌面...  "+pkgName);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public static String getAPIVersion(){
        try {
            Class<?> clazz = Class.forName("com.miui.enterprise.sdk.DeviceManager");
            for (Field field: clazz.getFields()){
                if (field.getName().equals("VERSION")){
                    field.setAccessible(true);
                    return (String) field.get(null);
                }
            }
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
        return null;
    }

    public static void enableUsbDebug(boolean enable){
        ideviceManager.enableUsbDebug(enable);
        System.out.println("Info: 设置Usb调试开关...  "+enable);
    }

    public static boolean isEnableAdb(){
        return getInt(Settings.Secure.ADB_ENABLED) > 0;
    }


    public static int getInt(String key){
        return Settings.Secure.getInt(App.getContext().getContentResolver(), key, 0);
    }


    public static String generateListSettings(List<String> value) {
        StringBuilder sb = new StringBuilder();
        if (value == null) {
            value = new ArrayList();
        }
        for (String single : value) {
            sb.append(single).append("\n");
        }
        if (sb.length() > 0) {
            sb.deleteCharAt(sb.length() - 1);
        }
        return sb.toString();
    }
    public static void setUrlBlackList(List<String> urls){
        //deviceManager.setUrlBlackList(urls);
        Settings.Secure.putString(App.getContext().getContentResolver(),"ep_url_black_list",generateListSettings(urls));
        System.out.println("Info: 设置Url黑名单...  "+urls);
    }

    public static List<String> getUrlBlackList(){
        List<String> getUrlBlackList = deviceManager.getUrlBlackList();
        System.out.println("Info: 获取Url黑名单");
        return getUrlBlackList;
    }

    public static void setHiddenWatermark(boolean status){
        deviceManager.setHiddenWatermark(status, UserManager.myUserId());
        System.out.println("Info: 设置水印开关...  "+status);
    }

    public static boolean getHiddenWatermark(){
        boolean isHiddenWatermark = deviceManager.getHiddenWatermark(UserManager.myUserId());
        System.out.println("Info: 获取水印状态...  "+isHiddenWatermark);
        return isHiddenWatermark;
    }

    public static Bitmap captureScreen(){
        System.out.println("Info: 获取屏幕截图...  ");
        return deviceManager.captureScreen();
    }

    public static void setBrowserRestriction(int mode){
        System.out.println("Info: 设置浏览器限制...  "+sAppRestrictModeArray.get(mode));
        deviceManager.setBrowserRestriction(mode);
    }

    public static void setWifiConnRestriction(int mode){
        ideviceManager.setWifiConnRestriction(mode);
        System.out.println("Info: 设置Wifi限制...  "+sAppRestrictModeArray.get(mode));
    }

    public static int getWifiConnRestriction(){
        int getWifiConnRestriction = ideviceManager.getWifiConnRestriction();
        System.out.println("Info: 获取Wifi限制...  "+sAppRestrictModeArray.get(getWifiConnRestriction));
        return getWifiConnRestriction;
    }

    public static List<String> getWifiApSsidWhiteList(){
        List<String> list = ideviceManager.getWifiApSsidWhiteList();
        System.out.println("Info: 获取WifiApSsid白名单... "+list);
        return list;
    }

    public static void setWifiApSsidWhiteList(List<String> ssids){
        deviceManager.setWifiApSsidWhiteList(ssids, UserManager.myUserId());
        System.out.println("Info: 设置WifiApSsid白名单... "+ssids);
    }

    public static List<String> getWifiApSsidBlackList(){
        List<String> list = ideviceManager.getWifiApBssidBlackList();
        System.out.println("Info: 获取WifiApSsid黑名单... "+list);
        return list;
    }

    public static void deviceReboot(){
        System.out.println("Info: 强制重启...");
        deviceManager.deviceReboot();
    }

    public static void deviceShutDown(){
        System.out.println("Info: 强制关机...");
        deviceManager.deviceShutDown();
    }

    public static boolean hasRestriction(String pkg){
        try {
            return restrictionsManager.hasRestriction(pkg);
        }catch (Throwable e){
            e.printStackTrace();
            return false;
        }
    }

    public static void recoveryFactory(boolean formatSdcard){
        System.out.println("Info: 强制恢复出厂设置... 附加选项: 格式化sdcard? "+formatSdcard);
        ideviceManager.recoveryFactory(formatSdcard);
    }

    public static void removeDeviceIdleAndAllRestrict(){
        try {
            String[] sysPowerSaveList = iDeviceIdleController.getSystemPowerWhitelist();
            for (String sPwt : sysPowerSaveList) {
                iDeviceIdleController.removeSystemPowerWhitelistApp(sPwt);
                System.out.println("正在移除系统级优化白名单: " + sPwt + "\n");
            }

            String[] userPowerSaveList = iDeviceIdleController.getUserPowerWhitelist();
            if (userPowerSaveList.length > 0) {
                for (String uPws : userPowerSaveList) {
                    iDeviceIdleController.removePowerSaveWhitelistApp(uPws);
                    System.out.println("正在移除用户级优化白名单: " + uPws);
                }
                System.out.println("共 " + userPowerSaveList.length + " 个用户级电池优化白名单已移除");
            }

            // 取消<强制开启且无法关闭>限制
            for (String key : getControlStatusFieldReflect()){
                if (restrictionsManager.getControlStatus(key) != RestrictionsManager.ENABLE){
                    System.out.println(sRestrictionStateArray.get(key)+" 被设置为 "+sSystemSwitchStatusArray.get(restrictionsManager.getControlStatus(key))+" 已取消此限制");
                    restrictionsManager.setControlStatus(key, RestrictionsManager.ENABLE);
                }
            }

            for (String key : getDisallowsFieldReflect()){
                if (restrictionsManager.hasRestriction(key)){
                    restrictionsManager.setRestriction(key,false);
                    System.out.println("已取消 "+sEntRestrictionArray.get(key)+" 的限制"+", 当前状态为: "+(restrictionsManager.hasRestriction(key) ? "禁用" : "启用"));
                }
            }

            if (!applicationManager.getDisallowedRunningAppList().isEmpty()){
                System.out.println("已清空运行黑名单:"+ Collections.singletonList(applicationManager.getDisallowedRunningAppList()));
                applicationManager.setDisallowedRunningAppList(null);
            }

            if (!applicationManager.getApplicationWhiteList().isEmpty()){
                System.out.println("已清空安装白名单:"+ Collections.singletonList(applicationManager.getApplicationWhiteList()));
                applicationManager.setApplicationWhiteList(null);
            }

            if (!applicationManager.getApplicationBlackList().isEmpty()){
                System.out.println("已清空安装黑名单:"+ Collections.singletonList(applicationManager.getApplicationBlackList()));
                applicationManager.setApplicationBlackList(null);
            }


            if (applicationManager.isTrustedAppStoreEnabled()){
                applicationManager.enableTrustedAppStore(false);
                System.out.println("已关闭可信任的应用商店");
            }

            if (!applicationManager.getTrustedAppStore().isEmpty()){
                System.out.println("已清空受信任的应用商店列表: "+Collections.singletonList(applicationManager.getTrustedAppStore()));
                applicationManager.addTrustedAppStore(null);
            }


            switch (applicationManager.getApplicationRestriction()){
                case ApplicationManager.RESTRICTION_MODE_DEFAULT:
                    break;
                case ApplicationManager.RESTRICTION_MODE_BLACK_LIST:
                case ApplicationManager.RESTRICTION_MODE_WHITE_LIST:
                    applicationManager.setApplicationRestriction(ApplicationManager.RESTRICTION_MODE_DEFAULT);
                    System.out.println("限制模式已更改为: "+sAppRestrictModeArray.get(applicationManager.getApplicationRestriction()));
                    break;
            }


            if (!getXSpaceBlackReflect().isEmpty()){
                System.out.println("已清空双开黑名单:"+ Collections.singletonList(getXSpaceBlackReflect()));
                setXSpaceBlackReflect(null);
            }

            for (String packageName : ServiceManager.getPackageManager().getAllPackages()){
                if (applicationManager.getApplicationSettings(packageName) != ApplicationManager.FLAG_DEFAULT){
                    System.out.println("已撤销 "+ Main.getAppName(packageName)+" 被授予的特权 --> "+(sAppPrivilegeArray.get(applicationManager.getApplicationSettings(packageName)) != null ? sAppPrivilegeArray.get(applicationManager.getApplicationSettings(packageName)) : applicationManager.getApplicationSettings(packageName)));
                    applicationManager.setApplicationSettings(packageName, ApplicationManager.FLAG_DEFAULT);
                }
            }

            for (AccessibilityServiceInfo serviceInfo : ServiceManager.getAccessibilityManager().getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK, UserManager.myUserId())){
                ServiceInfo componentInfo = serviceInfo.getResolveInfo().serviceInfo;
                ComponentName componentName = new ComponentName(componentInfo.packageName, componentInfo.name);
                applicationManager.enableAccessibilityService(componentName, false);
                System.out.println("已撤销授予 "+ Main.getAppName(componentName.getPackageName())+" 的无障碍权限");
            }

            if (getDeviceAdminsReflect() != null){
                for (ComponentName componentName : getDeviceAdminsReflect()){
                    if (!componentName.toString().contains("dhizuku")){
                        System.out.println(componentName+" 设备管理员移除结果: "+(applicationManager.removeDeviceAdmin(componentName) ? "成功" : "失败"));
                    }
                    enableNotificationsReflect(componentName.getPackageName(), false);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final ArrayMap<String,String> getInstalledApplicationsArray = new ArrayMap<String, String>(){{
        if (App.getContext() != null){
            for (String pkg : packageManager.getInstalledApplications()){
                put(pkg, packageManager.getAppNameForPackageName(App.getContext(),pkg));
            }
        }
    }};

    public static final ArrayMap<String,String> sEntDeviceRestrictionArray = new androidx.collection.ArrayMap<String, String>() {{
        try {
            Class<?> clazz = Class.forName("com.miui.enterprise.settings.EnterpriseSettings$Device");
            for (Field field : clazz.getFields()){
                put(field.getName(), (String) field.get(null));
            }
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }};

    public static final ArrayMap<Integer,String> sSystemSwitchStatusArray = new ArrayMap<Integer, String>() {{
        put(RestrictionsManager.DISABLE, "禁用");
        put(RestrictionsManager.ENABLE, "启用");
        put(RestrictionsManager.OPEN, "开启");
        put(RestrictionsManager.CLOSE, "关闭");
        put(RestrictionsManager.FORCE_OPEN, "强制开启且无法关闭");
    }};

    public static final SparseArray<String> sAppPrivilegeArray = new SparseArray<String>() {{
        put(ApplicationManager.FLAG_DEFAULT, "默认(清除授予的特殊权限)");
        put(ApplicationManager.FLAG_ALLOW_AUTOSTART, "自启动");
        put(ApplicationManager.FLAG_KEEP_ALIVE, "保活");
        put(ApplicationManager.FLAG_PREVENT_UNINSTALLATION, "防卸载");
        put(ApplicationManager.FLAG_GRANT_ALL_RUNTIME_PERMISSION, "授予运行时权限");
    }};

    public static final ArrayMap<String,String> sRestrictionStateArray = new androidx.collection.ArrayMap<String, String>() {{
        put(RestrictionsManager.GPS_STATE, "GPS开关");
        put(RestrictionsManager.BLUETOOTH_STATE, "蓝牙开关");
        put(RestrictionsManager.AIRPLANE_STATE, "飞行模式开关");
        put(RestrictionsManager.WIFI_STATE, "WIFI开关");
        put(RestrictionsManager.NFC_STATE, "NFC开关");
    }};

    public static final ArrayMap<String,String> sEntRestrictionArray = new androidx.collection.ArrayMap<String, String>() {{
        put("disallow_backup", "系统备份功能");
        put("disallow_camera", "相机功能");
        put("disallow_system_update", "系统更新");
        put("disallow_landscape_statusbar", "横屏功能");
        put("disallow_sdcard", "SD卡挂载功能");
        put("disallow_tether", "热点功能");
        put("disallow_auto_sync", "自动同步功能");
        put("disallow_imeiread", "IMEI访问");
        put("disallow_usbdebug", "USB调试功能");
        put("disallow_mtp", "MTP功能");
        put("disallow_otg", "OTG功能");
        put("disallow_vpn", "VPN功能");
        put("disallow_screencapture", "截屏功能");
        put("disallow_change_language", "系统语言切换");
        put("disallow_fingerprint", "指纹");
        put("disallow_status_bar", "系统状态栏");
        put("disallow_mi_account", "小米账号");
        put("disallow_microphone", "麦克风");
        put("disallow_key_menu", "MENU键");
        put("disallow_key_home", "HOME键");
        put("disallow_key_back", "BACK键");
        put("disallow_timeset", "日期设定功能");
        put("disable_usb_device", "USB设备");
        put("disallow_safe_mode", "安全模式");
        put("disallow_factoryreset", "恢复出厂设置");
        put("disable_accelerometer", "加速度传感器");

    }};

    public static final ArrayMap<Integer,String> sAppRestrictModeArray = new ArrayMap<Integer, String>() {{
        put(ApplicationManager.RESTRICTION_MODE_BLACK_LIST, "黑名单模式");
        put(ApplicationManager.RESTRICTION_MODE_WHITE_LIST, "白名单模式");
        put(ApplicationManager.RESTRICTION_MODE_DEFAULT, "默认模式");
    }};


    public static List<String> getXSpaceBlackReflect(){
        try {
            Class<?> cStub =  Class.forName("com.miui.enterprise.sdk.ApplicationManager");
            return (List<String>) cStub.getMethod("getXSpaceBlack").invoke(applicationManager);
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new RuntimeException(e2);
        }
    }

    public static void setXSpaceBlackReflect(List<String> packages){
        try {
            Class<?> cStub =  Class.forName("com.miui.enterprise.sdk.ApplicationManager");
            cStub.getMethod("setXSpaceBlack", List.class).invoke(applicationManager, packages);
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new RuntimeException(e2);
        }
    }


    public static ArraySet<String> getControlStatusFieldReflect(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub =  Class.forName("com.miui.enterprise.sdk.RestrictionsManager");
            ArraySet<String> fields= new ArraySet<>();
            for (Field value : cStub.getFields()){
                if (value.getName().endsWith("_STATE")){
                    fields.add((String) value.get(null));
                }
            }
            return fields;
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new SecurityException(e2);
        }
    }

    public static ArraySet<String> getDisallowsFieldReflect(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub =  Class.forName("com.miui.enterprise.sdk.RestrictionsManager");
            ArraySet<String> fields= new ArraySet<>();
            for (Field value : cStub.getFields()){
                if (value.getName().startsWith("DISA") && value.getType().equals(String.class)){
                    fields.add((String) value.get(null));
                }
            }
            return fields;
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new SecurityException(e2);
        }
    }

    public static List<ComponentName> getDeviceAdminsReflect(){

        try{
            @SuppressLint("PrivateApi")
            Class<?> cStub = Class.forName("android.app.admin.IDevicePolicyManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            Object obj = asInterface.invoke(null, Main.getSystemService(Context.DEVICE_POLICY_SERVICE));

            return (List<ComponentName>) obj.getClass().getMethod("getActiveAdmins", int.class).invoke(obj, UserManager.myUserId());

        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //设置用户禁止
    private static void setUserRestrictionReflect(String key, boolean value) {
        try {
            //获取IUserManager的Stub类
            @SuppressLint("PrivateApi")
            Class<?> cStub = Class.forName("android.os.IUserManager$Stub");
            //获取IUserManager的asInterface方法
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            //获取IUserManager的getSystemService方法
            Object obj = asInterface.invoke(null, Main.getSystemService("user"));

            //获取IUserManager的setUserRestriction方法
            Method setUserRestrictionMethod = obj.getClass().getMethod("setUserRestriction", String.class, boolean.class, int.class);

            //调用setUserRestriction方法
            setUserRestrictionMethod.invoke(obj, key, value, UserManager.myUserId());

        } catch (Exception e2) {
            //抛出异常
            throw new RuntimeException(e2);
        }
        //输出setUserRestriction的key和value
        System.out.println("设置用户限制: " + key + " --> " + value);
    }

    private static Bundle getUserRestrictionsReflect() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub = Class.forName("android.os.IUserManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            Object obj = asInterface.invoke(null, Main.getSystemService("user"));

            return (Bundle) obj.getClass().getMethod("getUserRestrictions", int.class).invoke(obj, UserManager.myUserId());
        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }

    public static void enableNotificationsReflect(String packageName, boolean enabled){

        try {
            Class<?> clazz = Class.forName("com.miui.enterprise.sdk.ApplicationManager");
            clazz.getMethod("enableNotifications", String.class, boolean.class).invoke(applicationManager, packageName, enabled);
        }catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException |
                InvocationTargetException e) {
            e.printStackTrace();
        }

    }
}
