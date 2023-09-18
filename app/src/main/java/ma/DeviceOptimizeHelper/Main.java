package ma.DeviceOptimizeHelper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.IAccountManager;
import android.accounts.IAccountManagerResponse;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Binder;
import android.os.Build;
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
import android.os.UserManager;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import dalvik.system.DexClassLoader;
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


    public static void main(String[] args){

        if (Binder.getCallingUid() == 0 || Binder.getCallingUid() == 1000){
            Looper.prepare();
            context = retrieveSystemContext();
            // 用于保存父类加载器
            ClassLoader parentClassloader = context.getClassLoader();
            // 创建一个自定义的类加载器，用于加载外部 JAR 文件
            MultiJarClassLoader classLoader = new MultiJarClassLoader(parentClassloader);
            classLoader.addJar("/system/framework/services.jar");

            // 判断参数长度
            switch (args.length){
                case 0:
                    // 没有参数 启动一个进程移除设备上的电池优化白名单和同步账号
                    serviceThread.start();
                    break;
                case 1:
                    // 有一个参数 根据这个参数的值启用或禁用设备上所有可用的限制策略
                    boolean value = Boolean.parseBoolean(args[0]);
                    for (String key: UserManagerUtils.getALLUserRestrictionsReflectForUserManager()){
                        setUserRestrictionReflect(key, value);
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


    public static Context createPackageContext(String packageName) {
        try {
            // 获取Application类对象（这是一个Android系统类）
            Class<?> applicationClass = Class.forName("android.app.Application");

            // 获取createPackageContext方法
            Method createPackageContextMethod = applicationClass.getDeclaredMethod(
                    "createPackageContext", String.class, int.class);
            // 调用createPackageContext方法
            Context packageContext = (Context) createPackageContextMethod.invoke(
                    null, packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
            return packageContext;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private static void removeAccount(Account account) throws RemoteException {
        iAccountManager.removeAccountAsUser(accountResponse , account, false, getIdentifier());
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

    public static class accountManagerResponse extends IAccountManagerResponse.Stub{

        @Override
        public void onResult(Bundle value) throws RemoteException {
            // 获取value中的key
            Set<String> keys = value.keySet();
            // 遍历key，发送消息
            for (String key: keys){
                Message msg = Message.obtain();
                msg.what = 1;
                msg.obj = value.get(key+":"+value.get(key));
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

    static class ChildCallback implements Handler.Callback{
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    System.out.println(msg.obj);
                    break;
                case 2:
                    System.out.println("Can't remove errorMessage: "+msg.obj +", errorCode: "+msg.arg1);
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

            handler = new Handler(serviceThread.getLooper(),new ChildCallback());

            try{
                String[] sysPowerSaveList = iDeviceIdleController.getSystemPowerWhitelist();
                for (String sPwt : sysPowerSaveList) {
                    iDeviceIdleController.removeSystemPowerWhitelistApp(sPwt);
                    Log.i("Main", "正在移除系统级优化白名单: " + sPwt + "\n");
                }

                String[] userPowerSaveList = iDeviceIdleController.getUserPowerWhitelist();
                if (userPowerSaveList.length > 0){
                    for (String uPws: userPowerSaveList){
                        iDeviceIdleController.removePowerSaveWhitelistApp(uPws);
                        System.out.println("正在移除用户级优化白名单: "+uPws);
                    }
                    System.out.println("共 "+userPowerSaveList.length+" 个用户级电池优化白名单已移除");
                }

                for (Account account: iAccountManager.getAccountsAsUser(null, getIdentifier(), "com.android.settings")){
                    removeAccount(account);
                    System.out.println("尝试移除账号: "+account.type);
                }

            }catch (Exception ignored){

            }
            serviceThread.getLooper().quitSafely();
        }

    }

    private static int getIdentifier(){

        try {
            return (int) UserHandle.class.getMethod("getIdentifier").invoke(Process.myUserHandle());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    private static IBinder getSystemService(String name){
        try {
            @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            getServiceMethod.setAccessible(true);
            return (IBinder) getServiceMethod.invoke(null, name);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    //设置用户禁止
    private static void setUserRestrictionReflect(String key, boolean value){
        try {
            //获取IUserManager的Stub类
            @SuppressLint("PrivateApi")
            Class<?> cStub =  Class.forName("android.os.IUserManager$Stub");
            //获取IUserManager的asInterface方法
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            //获取IUserManager的getSystemService方法
            Object obj = asInterface.invoke(null, getSystemService("user"));

            //获取IUserManager的setUserRestriction方法
            Method setUserRestrictionMethod =  obj.getClass().getMethod("setUserRestriction",String.class, boolean.class, int.class);

            //调用setUserRestriction方法
            setUserRestrictionMethod.invoke(obj,key,value,getIdentifier());

        } catch (Exception e2) {
            //抛出异常
            throw new RuntimeException(e2);
        }
        //输出setUserRestriction的key和value
        System.out.println("setUserRestriction: "+key+" set to "+getUserRestrictionsReflect().getBoolean(key));
    }

    private static Bundle getUserRestrictionsReflect(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub =  Class.forName("android.os.IUserManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            Object obj = asInterface.invoke(null, getSystemService("user"));

            return (Bundle) obj.getClass().getMethod("getUserRestrictions",int.class).invoke(obj,getIdentifier());
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
            return  (Context) getSystemContextMethod.invoke(activityThread);

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
