package com.sbmatch.deviceopt;

import android.accounts.IAccountManager;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.IDeviceIdleController;
import android.os.Looper;
import android.os.Process;
import android.os.UserHandle;
import android.system.Os;
import android.util.ArraySet;
import android.util.SparseArray;

import androidx.collection.ArrayMap;

import com.sbmatch.deviceopt.Utils.ContextUtil;
import com.sbmatch.deviceopt.Utils.SystemServiceWrapper.PackageManager;
import com.sbmatch.deviceopt.Utils.SystemServiceWrapper.ServiceManager;
import com.sbmatch.deviceopt.Utils.SystemServiceWrapper.UserManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import dalvik.system.DexClassLoader;

public class Main {
     // 用于保存 Android 上下文对象
    private static Context context;
    // 创建一个线程对象，用于执行后台服务
    private static final ServiceThread serviceThread = new ServiceThread("MaBaoGuo");
    // 用于处理消息的处理程序
    private static Handler handler;

    private static MultiJarClassLoader classLoader;
    static final PackageManager packageManager = ServiceManager.getPm();

    static ArrayList<String> exemptList = new ArrayList<>();

    static {
        exemptList.add("com.android.deskclock");
        exemptList.add("com.google.android.gms");
        exemptList.add("com.tencent.mm");

    }

    public static void main(String[] args) {

        if (Binder.getCallingUid() == 0 || Binder.getCallingUid() == 2000) {

            if (Looper.getMainLooper() == null) Looper.prepareMainLooper();

            context = ContextUtil.getContext();
            // 用于保存父类加载器
            ClassLoader parentClassloader = context.getClassLoader();
            // 创建一个自定义的类加载器，用于加载外部 JAR 文件
            classLoader = new MultiJarClassLoader(parentClassloader);
            classLoader.addJar("/system/framework/services.jar");
            classLoader.addJar("/system_ext/framework/miui-framework.jar");

            try {
                // 如果是 root 用户 则切换到 system
                if (Binder.getCallingUid() == 0) Os.setuid(1000);

                // 判断参数长度
                switch (args.length) {
                    case 2:
                        break;
                }
            }catch (Throwable e){
                System.err.println(e);
            }

        }

    }

    private static final SparseArray<String> sMsgArray = new SparseArray<String>() {{
        put(0, "禁用");
        put(1, "启用");
        put(2, "开启");
        put(3, "关闭");
        put(4, "强制开启且无法关闭");
    }};

    private static final SparseArray<String> sAppPrivilegeArray = new SparseArray<String>() {{
        put(0, "默认(用于清除授予的特殊权限)");
        put(8, "自启动");
        put(1, "保活");
        put(4, "防卸载");
        put(16, "授予运行时权限");
    }};

    private static final ArrayMap<String,String> sRestrictionArray = new ArrayMap<String, String>() {{
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
        put(2, "黑名单模式");
        put(1, "白名单模式");
        put(0, "默认模式");
    }};


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
            throw new RuntimeException(e2);
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

//    private static void findAccountAuthenticatorApps() {
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

    static class ServiceThread extends HandlerThread {
        public ServiceThread(String name) {
            super(name);
            this.setName(name);
        }


        @Override
        public synchronized void start() {
            super.start();

            handler = new Handler(serviceThread.getLooper());

            try {

            } catch (Exception e) {
                e.printStackTrace();
            }

            handler.postDelayed(() -> {
                serviceThread.getLooper().quitSafely();
            }, 3000);

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
