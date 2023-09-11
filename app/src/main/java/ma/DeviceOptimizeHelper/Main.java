package ma.DeviceOptimizeHelper;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.IAccountManager;
import android.accounts.IAccountManagerResponse;
import android.annotation.SuppressLint;
import android.content.Context;
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

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Main {

    private static  IAccountManager iAccountManager = IAccountManager.Stub.asInterface(getSystemService("account"));
    //private static IActivityManager iActivityManager = IActivityManager.Stub.asInterface(getSystemService("activity"));
    //private static IDevicePolicyManager iDevicePolicyManager = IDevicePolicyManager.Stub.asInterface(getSystemService(Context.DEVICE_POLICY_SERVICE));
    private static IDeviceIdleController iDeviceIdleController = IDeviceIdleController.Stub.asInterface(getSystemService("deviceidle"));
    private static Context context;
    private static final IAccountManagerResponse accountResponse = new accountManagerResponse();
    private static ServiceThread serviceThread =  new ServiceThread("MaBaoGuo");
    private static Handler handler;

    public static void main(String[] args) {

        Looper.prepare();

        context = retrieveSystemContext();

        serviceThread.start();
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


    private static StringBuilder exec(String cmd){

        java.lang.Process process;
        BufferedReader successResult;
        BufferedReader errorResult;
        StringBuilder successMsg = new StringBuilder();
        StringBuilder errorMsg = new StringBuilder();

        try {
            process = Runtime.getRuntime().exec(cmd);
            successResult = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorResult = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String line;
            while (( line = successResult.readLine()) != null) {
                successMsg.append(line).append("\n");
            }
            while (( line = errorResult.readLine()) != null) {
                errorMsg.append(line).append("\n");
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return successMsg;
    }

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
                System.out.print("key: "+key+", valve: "+value.get(key)+"\n");
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


                for (Account account: iAccountManager.getAccountsAsUser(null, getIdentifier(), "com.android.settings")){
                    removeAccount(account);
                }

                setUserRestrictionReflect(UserManager.DISALLOW_OUTGOING_BEAM, true); // 禁止使用Beam
                setUserRestrictionReflect(UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES_GLOBALLY, true); // 全局禁止安装未知来源应用
                setUserRestrictionReflect(UserManager.DISALLOW_FACTORY_RESET,true); // 禁止恢复出厂设置
                setUserRestrictionReflect(UserManager.DISALLOW_PRINTING,true); // 禁止打印机
                setUserRestrictionReflect(UserManager.DISALLOW_APPS_CONTROL,true); // 禁止控制应用(卸载，禁用，清除数据，强制停止，清除默认应用)
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_DATE_TIME,true); // 禁止手动更改时间与日期
                // setUserRestrictionReflect("no_oem_unlock",true); // 禁止oem解锁
                setUserRestrictionReflect("no_record_audio",true); // 禁止录音
                setUserRestrictionReflect("no_camera", true); // 禁止相机
                setUserRestrictionReflect(UserManager.DISALLOW_CAMERA_TOGGLE, true); // 禁止切换相机
                setUserRestrictionReflect(UserManager.DISALLOW_BLUETOOTH, true); // 禁止蓝牙
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_BLUETOOTH, true); // 禁止更改蓝牙配置
                setUserRestrictionReflect(UserManager.DISALLOW_ADD_WIFI_CONFIG, true); // 禁止添加WiFi
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_WIFI, true); // 禁止配置WIFI
                setUserRestrictionReflect(UserManager.DISALLOW_WIFI_DIRECT, true); // 禁止WIFI直连
                setUserRestrictionReflect(UserManager.DISALLOW_SET_WALLPAPER,true); // 禁止设置壁纸
                setUserRestrictionReflect(UserManager.DISALLOW_NETWORK_RESET,true); // 禁止重置网络
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_LOCALE,true); // 禁止更改语言
                setUserRestrictionReflect(UserManager.DISALLOW_ADJUST_VOLUME, true); // 禁止更改声音且强制静音
                setUserRestrictionReflect(UserManager.DISALLOW_CONTENT_CAPTURE, true); // 禁止屏幕捕获
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_CELL_BROADCASTS, true); // 禁止配置小区广播
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_SCREEN_TIMEOUT,true); // 禁止更改屏幕超时
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_CREDENTIALS,true); // 禁止更改用户凭据
                setUserRestrictionReflect(UserManager.DISALLOW_WIFI_TETHERING, true); // 禁止WiFI热点
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_TETHERING, true); // 禁止更改WIFI热点配置
                setUserRestrictionReflect(UserManager.DISALLOW_SMS,true); // 禁止使用短信
                setUserRestrictionReflect(UserManager.DISALLOW_AIRPLANE_MODE, true); // 禁止飞行模式
                setUserRestrictionReflect(UserManager.DISALLOW_OUTGOING_CALLS, true); // 禁止打电话(紧急电话除外)
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_MOBILE_NETWORKS, true); // 禁止数据网络
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_PRIVATE_DNS, true); // 禁止私人DNS
                setUserRestrictionReflect(UserManager.DISALLOW_CREATE_WINDOWS, true); // 禁止创建某些类型的窗口
                setUserRestrictionReflect(UserManager.DISALLOW_SHARE_LOCATION, true); // 禁止定位
                setUserRestrictionReflect(UserManager.DISALLOW_USB_FILE_TRANSFER, true); // 禁止通过USB传输文件
                setUserRestrictionReflect(UserManager.DISALLOW_CONFIG_BRIGHTNESS, true); // 禁止更改亮度
                setUserRestrictionReflect(UserManager.DISALLOW_ADD_USER, true); // 禁止添加用户（双开）
                setUserRestrictionReflect(UserManager.DISALLOW_REMOVE_USER, true); // 禁止移除用户
                setUserRestrictionReflect(UserManager.DISALLOW_INSTALL_APPS, true); // 禁止安装应用
                setUserRestrictionReflect(UserManager.DISALLOW_UNINSTALL_APPS, true); // 禁止卸载应用

            }catch (Exception e){
                e.printStackTrace();
                throw new SecurityException(e);
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


    private static void setUserRestrictionReflect(String key, boolean value){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub =  Class.forName("android.os.IUserManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            Object obj = asInterface.invoke(null, getSystemService("user"));

            Method setUserRestrictionMethod =  obj.getClass().getMethod("setUserRestriction",String.class, boolean.class, int.class);

            if (getUserRestrictionsReflect().getBoolean(key)){
                setUserRestrictionMethod.invoke(obj,key,false,getIdentifier());
            }else {
                setUserRestrictionMethod.invoke(obj,key,value,getIdentifier());
            }

        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
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


    private static ArraySet<String> getAllowInPowerSave(){

        try{
            @SuppressLint("PrivateApi")
            Class<?> clazz = Class.forName("com.android.server.SystemConfig");
            Object obj = clazz.getMethod("getInstance").invoke(null);
            Method getAllowInPowerSaveMethod = clazz.getMethod("getAllowInPowerSave");
            return (ArraySet<String>) getAllowInPowerSaveMethod.invoke(obj);
        }catch (NullPointerException | InvocationTargetException | ClassNotFoundException |
                NoSuchMethodException | IllegalAccessException e){
            throw new RuntimeException(e);
        }

    }

    private static ArrayMap<String,List<String>> getNotificationPolicy(String name){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub =  Class.forName("android.app.INotificationManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            Object mNotificationManager = asInterface.invoke(null, getSystemService("notification"));
            Object policyObject = mNotificationManager.getClass().getMethod("getNotificationPolicy", String.class).invoke(mNotificationManager,name);

            List<String> policy= new ArrayList<>();
            ArrayMap<String,List<String>> listArrayMap = new ArrayMap<>();

            if (policyObject != null){
                for (Field value : policyObject.getClass().getFields()){
                    if (!value.getName().contains("CREATOR")){
                        policy.add(value.getName()+":"+value.get(policyObject));
                    }
                }
                listArrayMap.put(name,policy);
            }
            return listArrayMap;
        } catch (Exception e2) {
            throw new SecurityException(e2);
        }
    }

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

}
