package ma.DeviceOptimizeHelper;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accounts.Account;
import android.accounts.IAccountManager;
import android.accounts.IAccountManagerResponse;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.ArraySet;
import android.util.SparseArray;

import androidx.collection.ArrayMap;

import com.miui.enterprise.sdk.ApplicationManager;
import com.miui.enterprise.sdk.DeviceManager;
import com.miui.enterprise.sdk.PhoneManager;
import com.miui.enterprise.sdk.RestrictionsManager;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import dalvik.system.DexClassLoader;
import ma.DeviceOptimizeHelper.Utils.AccessibilityManagerUtils;
import ma.DeviceOptimizeHelper.Utils.PackageManagerUtils;
import ma.DeviceOptimizeHelper.Utils.UserManagerUtils;

public class Main {
    // 引入 Android 的 IAccountManager 接口，用于操作帐户管理
    private static final IAccountManager iAccountManager = IAccountManager.Stub.asInterface(getSystemService("account"));

    // 引入 Android 的 IDeviceIdleController 接口，用于控制设备的空闲状态
    private static final IDeviceIdleController iDeviceIdleController = IDeviceIdleController.Stub.asInterface(getSystemService("deviceidle"));
    // 用于保存 Android 上下文对象
    private static Context context;
    // 用于处理帐户管理响应的回调对象
    private static final IAccountManagerResponse accountResponse = new accountManagerResponse();
    // 创建一个线程对象，用于执行后台服务
    private static final ServiceThread serviceThread = new ServiceThread("MaBaoGuo");
    // 用于处理消息的处理程序
    private static Handler handler;

    private static MultiJarClassLoader classLoader;

    private static RestrictionsManager restrictionsManager;
    private static DeviceManager deviceManager;
    private static ApplicationManager applicationManager;
    private static PhoneManager phoneManager;


    public static void main(String[] args) {

        if (Binder.getCallingUid() == 0 || Binder.getCallingUid() == 1000) {
            Looper.prepare();
            context = retrieveSystemContext();
            // 用于保存父类加载器
            ClassLoader parentClassloader = context.getClassLoader();
            // 创建一个自定义的类加载器，用于加载外部 JAR 文件
            classLoader = new MultiJarClassLoader(parentClassloader);
            classLoader.addJar("/system/framework/services.jar");
            classLoader.addJar("/system_ext/framework/miui-framework.jar");

            restrictionsManager = RestrictionsManager.getInstance();
            deviceManager = DeviceManager.getInstance();
            applicationManager = ApplicationManager.getInstance();
            phoneManager = PhoneManager.getInstance();

            // 判断参数长度
            switch (args.length) {

                case 1:
                    // 有一个参数
                    if (args[0].equals("disable")){
                        for (String key : getDisallowsFieldReflect()){
                            restrictionsManager.setRestriction(key,true);
                            System.out.println("已设置 "+sRestrictionArray.get(key) +" --> "+(restrictionsManager.hasRestriction(key) ? "禁用" : "启用"));
                        }
                    }

                    //判断参数为remove 启动一个进程移除设备上的电池优化白名单和同步账号
                    if (args[0].equals("remove")) {
                        serviceThread.start();
                        if (Binder.getCallingUid() == 1000){
                            try {
                                for (Account account : iAccountManager.getAccountsAsUser(null, getIdentifier(), "com.android.settings")) {
                                    removeAccount(account);
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }
                    }

                    if (args[0].equals("true") || args[0].equals("false")){
                        // 若不是 根据这个参数的值启用或禁用设备上所有可用的限制策略
                        boolean value = Boolean.parseBoolean(args[0]);
                        for (String key : UserManagerUtils.getALLUserRestrictionsReflectForUserManager()) {
                            setUserRestrictionReflect(key, value);
                        }
                    }
                    break;
                case 2:
                    // 有两个参数 根据提供的参数设置对应key的值
                    String name = args[0];
                    boolean newValue = Boolean.parseBoolean(args[1]);
                    setUserRestrictionReflect(name, newValue);
                    break;
                default:
                    System.err.print("好小子， 总爱给我玩点新花样");
            }
        } else {
            System.err.print("   You must execute with root privileges!   ");
        }

    }

    private static final SparseArray<String> sMsgArray = new SparseArray<String>() {{
        put(RestrictionsManager.DISABLE, "禁用");
        put(RestrictionsManager.ENABLE, "启用");
        put(RestrictionsManager.OPEN, "开启");
        put(RestrictionsManager.CLOSE, "关闭");
        put(RestrictionsManager.FORCE_OPEN, "强制开启且无法关闭");
    }};

    private static final SparseArray<String> sAppPrivilegeArray = new SparseArray<String>() {{
        put(ApplicationManager.FLAG_DEFAULT, "默认(用于清除授予的特殊权限)");
        put(ApplicationManager.FLAG_ALLOW_AUTOSTART, "自启动");
        put(ApplicationManager.FLAG_KEEP_ALIVE, "保活");
        put(ApplicationManager.FLAG_PREVENT_UNINSTALLATION, "防卸载");
        put(ApplicationManager.FLAG_GRANT_ALL_RUNTIME_PERMISSION, "授予运行时权限");
    }};

    private static final ArrayMap<String,String> sRestrictionArray = new androidx.collection.ArrayMap<String, String>() {{
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

    }};

    private static final SparseArray<String> sAppRestrictModeArray = new SparseArray<String>() {{
        put(ApplicationManager.RESTRICTION_MODE_BLACK_LIST, "黑名单模式");
        put(ApplicationManager.RESTRICTION_MODE_WHITE_LIST, "白名单模式");
        put(ApplicationManager.RESTRICTION_MODE_DEFAULT, "默认模式");
    }};


    public static List<String> getXSpaceBlackReflect(){
        try {
            Class<?> cStub =  classLoader.findClass("com.miui.enterprise.sdk.ApplicationManager");
            return (List<String>) cStub.getMethod("getXSpaceBlack").invoke(applicationManager);
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new RuntimeException(e2);
        }
    }

    public static void setXSpaceBlackReflect(List<String> packages){
        try {
            Class<?> cStub =  classLoader.findClass("com.miui.enterprise.sdk.ApplicationManager");
            cStub.getMethod("setXSpaceBlack", List.class).invoke(applicationManager, packages);
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new RuntimeException(e2);
        }
    }


    public static ArraySet<String> getControlStatusFieldReflect(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub =  classLoader.findClass("com.miui.enterprise.sdk.RestrictionsManager");
            ArraySet<String> fields= new ArraySet<>();
            for (Field value : cStub.getFields()){
                if (value.getName().contains("_STATE")){
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
            Class<?> cStub =  classLoader.findClass("com.miui.enterprise.sdk.RestrictionsManager");
            ArraySet<String> fields= new ArraySet<>();
            for (Field value : cStub.getFields()){
                if (value.getName().contains("DISALLOW_")){
                    fields.add((String) value.get(null));
                }
            }
            return fields;
        } catch (Exception e2) {
            e2.printStackTrace();
            throw new SecurityException(e2);
        }
    }


    private static void removeAccount(Account account) throws RemoteException {
        iAccountManager.removeAccountAsUser(accountResponse, account, false, getIdentifier());
    }

//    private static void launchMainActivity() {
//        PackageManager packageManager = context.getPackageManager();
//        Intent intent = new Intent("android.accounts.AccountAuthenticator");
//        @SuppressLint("QueryPermissionsNeeded")
//        List<ResolveInfo> resolveInfos = packageManager.queryIntentServices(intent,0);
//        Set<String> serviceNames = new HashSet<>();
//        if (resolveInfos != null && !resolveInfos.isEmpty()){
//            for (ResolveInfo resolve : resolveInfos){
//                ServiceInfo serviceInfo = resolve.serviceInfo;
//                String serviceName = serviceInfo.name;
//                serviceNames.add(serviceName);
//            }
//            System.out.print("设备上共有: "+serviceNames.size()+" 个Authentication\n");
//
//        }
//
//    }

    public static class accountManagerResponse extends IAccountManagerResponse.Stub {

        @Override
        public void onResult(Bundle value) throws RemoteException {
            // 获取value中的key
            Set<String> keys = value.keySet();
            // 遍历key，发送消息
            for (String key : keys) {
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = value.get(key + ":" + value.get(key));
                handler.sendMessage(msg);
            }
        }

        @Override
        public void onError(int errorCode, String errorMessage) throws RemoteException {
            // 发送消息
            Message msg = Message.obtain();
            msg.what = 2;
            msg.arg1 = errorCode;
            msg.obj = errorMessage;
            handler.sendMessage(msg);
        }
    }

    static class ChildCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    System.out.println(msg.obj);
                    break;
                case 2:
                    System.out.println("Can't remove errorMessage: " + msg.obj + ", errorCode: " + msg.arg1);
                    break;
                default:

            }
            return false;
        }
    }


    static class ServiceThread extends HandlerThread {
        public ServiceThread(String name) {
            super(name);
            this.setName(name);
        }


        @Override
        public synchronized void start() {
            super.start();

            handler = new Handler(serviceThread.getLooper(), new ChildCallback());


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
                        System.out.println(key+" 被设置为 "+sMsgArray.get(restrictionsManager.getControlStatus(key))+" 已取消此限制");
                        restrictionsManager.setControlStatus(key, RestrictionsManager.ENABLE);
                    }
                }

                for (String key : getDisallowsFieldReflect()){
                    if (restrictionsManager.hasRestriction(key)){
                        restrictionsManager.setRestriction(key,false);
                        System.out.println("已取消 "+sRestrictionArray.get(key)+" 的限制"+", 当前状态为: "+(restrictionsManager.hasRestriction(key) ? "禁用" : "启用"));
                    }
                }

                if (!applicationManager.getDisallowedRunningAppList().isEmpty()){
                    System.out.println("获取到运行黑名单:"+ Collections.singletonList(applicationManager.getDisallowedRunningAppList()));
                    applicationManager.setDisallowedRunningAppList(null);
                    System.out.println("运行黑名单已清空");
                }

                if (!applicationManager.getApplicationWhiteList().isEmpty()){
                    System.out.println("获取到安装白名单:"+ Collections.singletonList(applicationManager.getApplicationWhiteList()));
                    applicationManager.setApplicationWhiteList(null);
                    System.out.println("安装白名单已清空");
                }

                if (!applicationManager.getApplicationBlackList().isEmpty()){
                    System.out.println("获取到安装黑名单:"+ Collections.singletonList(applicationManager.getApplicationBlackList()));
                    applicationManager.setApplicationBlackList(null);
                    System.out.println("安装黑名单已清空");
                }


                if (applicationManager.isTrustedAppStoreEnabled()){
                    applicationManager.enableTrustedAppStore(false);
                    System.out.println("已关闭可信任的应用商店");
                }

                if (!applicationManager.getTrustedAppStore().isEmpty()){
                    System.out.println("受信任的应用商店列表: "+Collections.singletonList(applicationManager.getTrustedAppStore()));
                    applicationManager.addTrustedAppStore(null);
                    System.out.println("受信任的应用商店列表已清空");
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
                    System.out.println("获取到双开黑名单:"+ Collections.singletonList(getXSpaceBlackReflect()));
                    setXSpaceBlackReflect(null);
                    System.out.println("双开黑名单已清空");
                }

                for (String packageName : (String[]) PackageManagerUtils.IPackageManagerNative().getClass().getMethod("getAllPackages").invoke(PackageManagerUtils.IPackageManagerNative())){
                    if (applicationManager.getApplicationSettings(packageName) != ApplicationManager.FLAG_DEFAULT){
                        System.out.println(packageName+" 被授权 "+sAppPrivilegeArray.get(applicationManager.getApplicationSettings(packageName)));
                        applicationManager.setApplicationSettings(packageName, ApplicationManager.FLAG_DEFAULT);
                        System.out.println(packageName+" 的特权已被撤销");
                    }
                }

                for (AccessibilityServiceInfo serviceInfo : AccessibilityManagerUtils.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC, getIdentifier())){
                        ServiceInfo componentInfo = serviceInfo.getResolveInfo().serviceInfo;
                        ComponentName componentName = new ComponentName(componentInfo.packageName, componentInfo.name);
                        applicationManager.enableAccessibilityService(componentName, false);
                        System.out.println("已撤销授予 "+PackageManagerUtils.getAppNameForPackageName(context.createPackageContext(componentName.getPackageName(), Context.CONTEXT_IGNORE_SECURITY) ,componentName.getPackageName(), 0, getIdentifier())+" 的无障碍授权");
                }

                for (ComponentName componentName : getDeviceAdminsReflect()){
                    if (!componentName.toString().contains("dhizuku")){
                        System.out.println(componentName+" 设备管理员移除结果: "+(applicationManager.removeDeviceAdmin(componentName) ? "成功" : "失败"));
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            handler.postDelayed(() -> {
                serviceThread.getLooper().quitSafely();
            }, 3000);

        }

    }

    public static List<ComponentName> getDeviceAdminsReflect(){

        try{
            @SuppressLint("PrivateApi")
            Class<?> cStub = Class.forName("android.app.admin.IDevicePolicyManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            Object obj = asInterface.invoke(null, getSystemService(Context.DEVICE_POLICY_SERVICE));

            return (List<ComponentName>) obj.getClass().getMethod("getActiveAdmins", int.class).invoke(obj, getIdentifier());

        }catch (Exception e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }

    }

    private static int getIdentifier() {

        try {
            return (int) UserHandle.class.getMethod("getIdentifier").invoke(Process.myUserHandle());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static IBinder getSystemService(String name) {
        try {
            //获取android.os.ServiceManager的getService方法
            @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            //设置getService方法的可访问性
            getServiceMethod.setAccessible(true);
            //调用getService方法，传入name参数，获取IBinder对象
            return (IBinder) getServiceMethod.invoke(null, name);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException |
                 ClassNotFoundException | NoSuchMethodException e) {
            //抛出运行时异常
            throw new RuntimeException(e);
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
            Object obj = asInterface.invoke(null, getSystemService("user"));

            //获取IUserManager的setUserRestriction方法
            Method setUserRestrictionMethod = obj.getClass().getMethod("setUserRestriction", String.class, boolean.class, int.class);

            //调用setUserRestriction方法
            setUserRestrictionMethod.invoke(obj, key, value, getIdentifier());

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
            Object obj = asInterface.invoke(null, getSystemService("user"));

            return (Bundle) obj.getClass().getMethod("getUserRestrictions", int.class).invoke(obj, getIdentifier());
        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }

//    private static ArraySet<String> getAllowInPowerSave(){
//
//        try{
//            @SuppressLint("PrivateApi")
//            Class<?> clazz = Class.forName("com.android.server.SystemConfig");
//            Object obj = clazz.getMethod("getInstance").invoke(null);
//            Method getAllowInPowerSaveMethod = clazz.getMethod("getAllowInPowerSave");
//            return (ArraySet<String>) getAllowInPowerSaveMethod.invoke(obj);
//        }catch (NullPointerException | InvocationTargetException | ClassNotFoundException |
//                NoSuchMethodException | IllegalAccessException e){
//            throw new RuntimeException(e);
//        }
//
//    }

//    private static ArrayMap<String,List<String>> getNotificationPolicy(String name){
//        try {
//            @SuppressLint("PrivateApi")
//            Class<?> cStub =  Class.forName("android.app.INotificationManager$Stub");
//            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
//            Object mNotificationManager = asInterface.invoke(null, getSystemService("notification"));
//            Object policyObject = mNotificationManager.getClass().getMethod("getNotificationPolicy", String.class).invoke(mNotificationManager,name);
//
//            List<String> policy= new ArrayList<>();
//            ArrayMap<String,List<String>> listArrayMap = new ArrayMap<>();
//
//            if (policyObject != null){
//                for (Field value : policyObject.getClass().getFields()){
//                    if (!value.getName().contains("CREATOR")){
//                        policy.add(value.getName()+":"+value.get(policyObject));
//                    }
//                }
//                listArrayMap.put(name,policy);
//            }
//            return listArrayMap;
//        } catch (Exception e2) {
//            throw new SecurityException(e2);
//        }
//    }

    private static Context retrieveSystemContext() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Constructor<?> activityThreadConstructor = activityThreadClass.getDeclaredConstructor();
            activityThreadConstructor.setAccessible(true);
            Object activityThread = activityThreadConstructor.newInstance();
            Method getSystemContextMethod = activityThread.getClass().getDeclaredMethod("getSystemContext");
            getSystemContextMethod.setAccessible(true);
            return (Context) getSystemContextMethod.invoke(activityThread);

        } catch (ClassNotFoundException | InvocationTargetException | IllegalAccessException |
                 InstantiationException | NoSuchMethodException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public static class MultiJarClassLoader extends ClassLoader {
        private List<DexClassLoader> dexClassLoaders;

        public MultiJarClassLoader(ClassLoader parentClassLoader) {
            super(parentClassLoader);
            dexClassLoaders = new ArrayList<>();
        }

        public void addJar(String jarPath) {
            DexClassLoader dexClassLoader = new DexClassLoader(
                    jarPath,
                    null,
                    null, // 额外的库路径，可以为 null
                    getParent() // 父类加载器
            );
            dexClassLoaders.add(dexClassLoader);
        }

        @Override
        protected Class<?> findClass(String className) throws ClassNotFoundException {
            // 遍历所有的 DexClassLoader 实例，尝试加载类
            for (DexClassLoader dexClassLoader : dexClassLoaders) {
                try {
                    return dexClassLoader.loadClass(className);
                } catch (ClassNotFoundException ignored) {
                    // 忽略类未找到的异常，继续下一个 DexClassLoader
                }
            }
            throw new ClassNotFoundException("Class not found: " + className);
        }
    }

}
