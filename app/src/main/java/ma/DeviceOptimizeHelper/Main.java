package ma.DeviceOptimizeHelper;

import static ma.DeviceOptimizeHelper.Utils.ServiceManager.getSystemService;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.IAccountManager;
import android.accounts.IAccountManagerResponse;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

import ma.DeviceOptimizeHelper.Utils.UserManagerUtils;

public class Main {

    private static  IAccountManager iAccountManager = IAccountManager.Stub.asInterface(getSystemService("account"));
    private static IDeviceIdleController iDeviceIdleController = IDeviceIdleController.Stub.asInterface(getSystemService("deviceidle"));
    private static Context context;
    private static final IAccountManagerResponse accountResponse = new accountManagerResponse();

    private static ServiceThread serviceThread = new ServiceThread("MaBaoGuo");
    private static Handler handler;

    public static void main(String[] args) {

        // 必须使用root权限执行
        if (Binder.getCallingUid() == 0 || Binder.getCallingUid() == 1000){
            Looper.prepare();
            if (args.length == 0){
                context = retrieveSystemContext();
                serviceThread.start();
            }else if (args.length == 2){
                String name = args[0];
                boolean value = Boolean.parseBoolean(args[1]);
                UserManagerUtils.setUserRestrictionReflect(name, value);
            }
        }else {
            System.err.print("      You must execute with root privileges\n");
        }

    }


    private static void removeAccount(Account account) throws RemoteException {
        iAccountManager.removeAccountAsUser(accountResponse , account, false, UserManagerUtils.getIdentifier());
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


    static class accountManagerResponse extends IAccountManagerResponse.Stub{

        /**
         * @param value
         * @throws RemoteException
         */
        @Override
        public void onResult(Bundle value) throws RemoteException {
            Set<String> keys = value.keySet();
            Message msg = Message.obtain();
            for (String key: keys){
                if (key.contains(AccountManager.KEY_ACCOUNT_TYPE)){
                    msg.obj = value.get(key);
                    handler.sendMessage(msg);
                }
            }
        }

        /**
         * @param errorCode
         * @param errorMessage
         * @throws RemoteException
         */
        @Override
        public void onError(int errorCode, String errorMessage) throws RemoteException {
            System.err.print("errorMsg: "+errorMessage+"\n");
        }

    }

    static class ChildCallback implements Handler.Callback{
        @Override
        public boolean handleMessage(Message msg) {

            System.out.println("removed the account type: "+msg.obj);

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
                if (sysPowerSaveList.length > 0){
                    for (String sPwt : sysPowerSaveList){
                        iDeviceIdleController.removeSystemPowerWhitelistApp(sPwt);
                        System.err.print("正在移除系统级优化白名单: "+sPwt+"\n");
                    }
                    System.out.print("共 "+sysPowerSaveList.length+" 个系统级电池优化白名单已移除\n");
                }

                String[] userPowerSaveList = iDeviceIdleController.getUserPowerWhitelist();
                if (userPowerSaveList.length > 0){
                    for (String uPws: userPowerSaveList){
                        iDeviceIdleController.removePowerSaveWhitelistApp(uPws);
                        System.out.print("正在移除用户级优化白名单: "+uPws+"\n");
                    }
                    System.out.print("共 "+userPowerSaveList.length+" 个用户级电池优化白名单已移除\n");
                }

                for (Account account: iAccountManager.getAccountsAsUser(null, 0 , "com.android.settings")){
                    removeAccount(account);
                }

            }catch (Exception e){
                e.printStackTrace();
                throw new SecurityException(e);
            }
            serviceThread.getLooper().quitSafely();
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
            throw new RuntimeException(e);
        }
    }

//
//    public static class MultiJarClassLoader extends ClassLoader {
//        private List<DexClassLoader> dexClassLoaders;
//
//        public MultiJarClassLoader(ClassLoader parentClassLoader) {
//            super(parentClassLoader);
//            dexClassLoaders = new ArrayList<>();
//        }
//
//        public void addJar(String jarPath) {
//            DexClassLoader dexClassLoader = new DexClassLoader(
//                    jarPath,
//                    null,
//                    null, // 额外的库路径，可以为 null
//                    getParent() // 父类加载器
//            );
//            dexClassLoaders.add(dexClassLoader);
//        }
//
//        @Override
//        protected Class<?> findClass(String className) throws ClassNotFoundException {
//            // 遍历所有的 DexClassLoader 实例，尝试加载类
//            for (DexClassLoader dexClassLoader : dexClassLoaders) {
//                try {
//                    return dexClassLoader.loadClass(className);
//                } catch (ClassNotFoundException ignored) {
//                    // 忽略类未找到的异常，继续下一个 DexClassLoader
//                }
//            }
//            throw new ClassNotFoundException("Class not found: " + className);
//        }
//    }


}
