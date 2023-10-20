package ma.DeviceOptimizeHelper;

import android.accounts.Account;
import android.accounts.IAccountManager;
import android.accounts.IAccountManagerResponse;
import android.annotation.SuppressLint;
import android.app.IProcessObserver;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;

import com.miui.enterprise.IEnterpriseManager;
import com.miui.enterprise.sdk.ApplicationManager;
import com.miui.enterprise.sdk.IEpInstallPackageObserver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dalvik.system.DexClassLoader;
import ma.DeviceOptimizeHelper.Model.ProcessInfo;
import ma.DeviceOptimizeHelper.Services.DeviceOptServiceImpl;
import ma.DeviceOptimizeHelper.Utils.ActivityManager;
import ma.DeviceOptimizeHelper.Utils.AnyRestrictPolicyUtils;
import ma.DeviceOptimizeHelper.Utils.ContextUtils;
import ma.DeviceOptimizeHelper.Utils.Enterprise.EnterpriseManager;
import ma.DeviceOptimizeHelper.Utils.FilesUtils;
import ma.DeviceOptimizeHelper.Utils.OsUtils;
import ma.DeviceOptimizeHelper.Utils.PackageManager;
import ma.DeviceOptimizeHelper.Utils.ServiceManager;
import ma.DeviceOptimizeHelper.Utils.ShellUtils;
import ma.DeviceOptimizeHelper.Utils.UserManager;

public class Main {
    // 引入 Android 的 IAccountManager 接口，用于操作帐户管理
    private static final IAccountManager iAccountManager = IAccountManager.Stub.asInterface(getSystemService("account"));
  // 用于保存 Android 上下文对象
    private static Context context;
    private static final PackageManager packageManager = ServiceManager.getPackageManager();
    private static final ActivityManager activityManager = ServiceManager.getActivityManager();
    public static ApplicationManager applicationManager = ApplicationManager.getInstance();
    // 用于处理帐户管理响应的回调对象
    private static final IAccountManagerResponse accountResponse = new accountManagerResponse();
    // 创建一个线程对象，用于执行后台服务
    private static ServiceThread serviceThread;
    // 用于处理消息的处理程序
    private static ClassLoader parentClassloader = ClassLoader.getSystemClassLoader();
    private static DeviceOptServiceImpl iDeviceOptService = DeviceOptServiceImpl.getInstance();
    private static final Intent intent;

    static {
        intent = new Intent("ma.deviceOptimizeHelper.deviceOptSendBroadcast")
                .setPackage("ma.DeviceOptimizeHelper")
                .putExtra("enterpriseManagerBinder", new BinderContainer(ServiceManager.getService("EnterpriseManager")))
                .putExtra("deviceOptServiceBinder",new BinderContainer(iDeviceOptService));
    }

    private static final IProcessObserver processObserver = new IProcessObserver.Stub() {
        @Override
        public void onForegroundActivitiesChanged(int pid, int uid, boolean foregroundActivities) throws RemoteException {
            String packageName = packageManager.getNameForUid(uid);
            if (packageName.equals("ma.DeviceOptimizeHelper")){
                activityManager.broadcastIntent(intent, true);
                System.out.println("监听到App运行, 发送广播...");
            }

        }

        @Override
        public void onForegroundServicesChanged(int pid, int uid, int serviceTypes) throws RemoteException {
            String packageName = packageManager.getNameForUid(uid);
        }

        @Override
        public void onProcessDied(int pid, int uid) throws RemoteException {
            String packageName = packageManager.getNameForUid(uid);
            if (packageName.equals("ma.DeviceOptimizeHelper")) {
                activityManager.unbroadcastIntent(intent);
                System.out.println("监听到App关闭, 取消广播...");
            }
        }
    };

    public static void main(String[] args) {

        if (Looper.getMainLooper() == null) {
            Looper.prepareMainLooper();
        }

        if (serviceThread == null){
            serviceThread = new ServiceThread("DeviceOpt", args);
        }

        int pid = Process.myPid();
        int uid = Process.myUid();


        if (Binder.getCallingUid() == 0 || Binder.getCallingUid() == 1000 || Binder.getCallingUid() == 2000) {

            System.out.println("欢迎使用! uid:"+uid +" pid:"+pid);

            activityManager.registerProcessObserver(processObserver);
            serviceThread.start();
            serviceThread.quitSafely();

            List<ProcessInfo> processInfos = appProcessTopData(ShellUtils.execCommand("top -o NAME,PID,USER,COMMAND -q -b -n 1"));

            for (ProcessInfo info : processInfos){
                if (info.getProcessName().equals("deviceopt_server") && info.getPid() != pid){
                    System.out.println("Info: Killing old processPid "+info.getPid()+" "+ ShellUtils.execCommand("kill -9 " + info.getPid()));
                }
             }

        }

        Looper.loop();
        System.exit(0);
    }

    public static List<ProcessInfo> appProcessTopData(String topData) {
        List<ProcessInfo> processInfoList = new ArrayList<>();
        // 将字符串按行拆分
        String[] lines = topData.split("\n");

        for (String line : lines) {
            // 按空格拆分每行的数据
            String[] parts = line.trim().split(" +");

            if (parts.length >= 4) {
                String command = parts[3];
                if (command.contains("app_process")){
                    // 创建 ProcessInfo 对象并添加到列表
                    String processName = parts[0];
                    int pid = Integer.parseInt(parts[1]);
                    String user = parts[2];
                    ProcessInfo processInfo = new ProcessInfo(processName, pid, user);
                    processInfoList.add(processInfo);
                }

            }
        }
        return processInfoList;
    }


    private static class ServiceThread extends HandlerThread {
        String[] args;
        private final Handler handler = new Handler(Looper.getMainLooper());
        public ServiceThread(String name, String[] args) {
            super(name);
            this.setName(name);
            this.args = args;
        }

        @Override
        public void run() {
            super.run();

            if (OsUtils.isMiui()){
                if (FilesUtils.isFileExists("/data/system/theme_config/theme_compatibility.xml")){
                    context = ContextUtils.retrieveSystemContext();
                }
            }else {
                context = ContextUtils.retrieveSystemContext();
            }

            // 判断参数长度
            switch (args.length) {

                case 1:
                    //启动一个进程移除设备上的电池优化白名单和同步账号以及各种限制
                    if (args[0].equals("removeDeviceIdleAndAllRestrict")) {

                        if (Binder.getCallingUid() == 1000){
                            try {
                                for (Account account : iAccountManager.getAccountsAsUser(null, UserManager.myUserId(), "com.android.settings")) {
                                    iAccountManager.removeAccountAsUser(accountResponse, account, false, UserManager.myUserId());
                                }
                            }catch (Exception e){
                                e.printStackTrace();
                            }
                        }else {
                            AnyRestrictPolicyUtils.removeDeviceIdleAndAllRestrict();
                        }
                    }
                    break;
                case 2:
                    // 有两个参数 根据提供的参数设置对应key的值
                   if (args[1].equals("true") || args[1].equals("false")) {
                       boolean newValue2 = Boolean.parseBoolean(args[1]);
                       switch (args[0]){
                           case "setAllUserRestrict":
                               AnyRestrictPolicyUtils.setAllUserRestrict(newValue2);
                               break;
                           case "setAllEntRestrict":
                               AnyRestrictPolicyUtils.setAllEntRestrict(newValue2);
                               break;
                       }
                   }else if (args[0].equals("forceInstallByEnt")){
                       AnyRestrictPolicyUtils.forceInstallByEnt(args[1], new IEpInstallPackageObserver.Stub() {
                           @Override
                           public void onPackageInstalled(String s, int i, String s1, Bundle bundle) {
                               if (packageManager.isPackageAvailable(s)){
                                   applicationManager.setApplicationSettings(s, ApplicationManager.FLAG_GRANT_ALL_RUNTIME_PERMISSION);
                                   System.out.println("Info: 安装完成... "+s +" 已授权所有运行时权限, 返回代码: "+i);
                               }else {
                                   System.err.println("Error: 安装失败... 返回代码:"+i);
                               }

                           }
                       });
                   } else if (args[0].equals("setWifiApSsidBlackList")){

                   }

                    break;
                case 3:
                    // 有三个参数 根据提供的参数设置对应key的值
                    String key3 = args[1];
                    boolean newValue3 = Boolean.parseBoolean(args[2]);
                    switch (args[0]){
                        case "setEntRestrict":
                            AnyRestrictPolicyUtils.setEntRestrict(key3, newValue3);
                            break;
                        case "setUserRestrict":
                            AnyRestrictPolicyUtils.setUserRestrict(key3, newValue3);
                            break;
                    }
                    break;
                default:

            }
        }
    }

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
            }
        }

        @Override
        public void onError(int errorCode, String errorMessage) throws RemoteException {
            // 发送消息
            Message msg = Message.obtain();
            msg.what = 2;
            msg.arg1 = errorCode;
            msg.obj = errorMessage;
        }
    }

    public static String getAppName(String packageName) throws android.content.pm.PackageManager.NameNotFoundException {
       return (context != null) ? packageManager.getAppNameForPackageName(context.createPackageContext(packageName, Context.CONTEXT_IGNORE_SECURITY) ,packageName) : packageName;
    }

//    public static int getApplicationAutoStart(Context context, String packageName){
//        try {
//            Class<?> clazz = Class.forName("android.miui.AppOpsUtils");
//            return (int) clazz.getMethod("getApplicationAutoStart", Context.class, String.class).invoke(null, context, packageName);
//        }catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException |
//                InvocationTargetException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e);
//        }
//    }

//    public static void setApplicationAutoStart(Context context, String packageName, boolean autoStart){
//        try {
//            Class<?> clazz = Class.forName("android.miui.AppOpsUtils");
//            clazz.getMethod("setApplicationAutoStart", Context.class, String.class, boolean.class).invoke(null, context, packageName, autoStart);
//        }catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException |
//                InvocationTargetException e) {
//            e.printStackTrace();
//        }
//    }


    public static IBinder getSystemService(String name) {
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

    private static String myProcessName(){
        try {
            Class<?> clazz = Process.class;
            return (String) clazz.getMethod("myProcessName").invoke(null);
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
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
