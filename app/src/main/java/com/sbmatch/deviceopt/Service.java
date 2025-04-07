package com.sbmatch.deviceopt;


import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.icu.util.CurrencyAmount;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Process;
import android.util.Log;

import com.sbmatch.deviceopt.utils.CommandExecutor;
import com.sbmatch.deviceopt.utils.ContextUtil;
import com.sbmatch.deviceopt.utils.FrameworkJarClassLoader;
import com.sbmatch.deviceopt.utils.ReflectUtils;
import com.sbmatch.deviceopt.utils.ResourceUtils;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.ActivityManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.AppOpsManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.PackageManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.ServiceManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.UserManager;
import com.sbmatch.deviceopt.bean.ProcessInfo;

import java.util.ArrayList;
import java.util.List;

public class Service{

    // 创建一个线程对象，用于执行后台服务
    private static ServiceThread serviceThread;
    // 用于处理消息的处理程序
    private static Handler handler;
    private static final FrameworkJarClassLoader loader = FrameworkJarClassLoader.getInstance();
    private static final ActivityManager mActivityManager = ServiceManager.getActivityManager();
    private static final PackageManager mPackageManager = ServiceManager.getPackageManager();
    private static final AppOpsManager mAppOpsManager = ServiceManager.getAppOpsManager();
    private static UserManager userManager;

    public static void main(String[] args) {

        if (Process.myUid() == Process.ROOT_UID || Process.myUid() == Process.SYSTEM_UID ||Process.myUid() == Process.SHELL_UID) {
            if (Looper.getMainLooper() == null) Looper.prepareMainLooper();

            //ReLinker.loadLibrary(ContextUtil.createPackageContext(BuildConfig.APPLICATION_ID),"mmkv");
            userManager = UserManager.get();
            serviceThread = new ServiceThread("RemoteService", args);
            try {
                serviceThread.start();
                serviceThread.join();
            }catch (Throwable e){
                Intent intent = new Intent(Service.class.getName()+"_command_failure");
                intent.setPackage(BuildConfig.APPLICATION_ID);
                intent.putExtra("command_failure", e);
                mActivityManager.broadcastIntent(intent, true);
                System.err.println(Log.getStackTraceString(e));
            }

            CommandExecutor.get().executeCommand("top -o NAME,PID,USER,COMMAND -q -b -n 1", new CommandExecutor.CommandResultListener() {
                @Override
                public void onSuccess(String output) {

                    for (ProcessInfo info : appProcessTopData(output)) {
                        if (info.getProcessName().equals("deviceopt_server") && info.getPid() != Process.myPid()) {
                            System.out.println("Info: killing old process " + info.getPid() + "\n");
                            CommandExecutor.get().executeCommand("kill -9 " + info.getPid(),null ,false);
                        }
                    }
                }

                @Override
                public void onError(String error) {

                }
            }, false);

            Looper.loop();
        }
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

    private static void setUserRestrict(String key, boolean z) {
        // 设置用户限制
        Context context = ContextUtil.createPackageContext(BuildConfig.APPLICATION_ID);
        userManager.setUserRestriction(key, z);
        System.out.println((ResourceUtils.getString(context, ReflectUtils.getResIdReflect(key)) == null ? key : ResourceUtils.getString(context, ReflectUtils.getResIdReflect(key)))  +" set newStatus "+z);

    }

    @SuppressLint("PrivateApi")
    private static boolean isValidRestriction(String restriction){
        return (boolean) ReflectUtils.callStaticObjectMethod(loader.findClass("/system/framework/services.jar","com.android.server.pm.UserRestrictionsUtils"),"isValidRestriction", restriction);
    }

   public static class ServiceThread extends HandlerThread {
        private final String[] args;
        public ServiceThread(String name, String[] args) {
            super(name);
            this.setName(name);
            this.args = args;
        }

        @Override
        public synchronized void start() {
            super.start();

            handler = new Handler(serviceThread.getLooper());

            switch (args.length){
                case 0 -> {

                }

                case 1 -> {
                    switch (args[0]){
                        case "--start" -> {
                            // 启动服务
                            System.out.println("Starting service...");
                        }
                        case "--stop" -> {
                            // 停止服务
                            System.out.println("Stopping service...");
                        }
                    }

                    if (args[0].startsWith("content://")){
                        Cursor cursor;
                        ContentProviderClient providerClient = ActivityThread.systemMain().getSystemContext().getContentResolver().acquireUnstableContentProviderClient(Uri.parse(args[0]));
                        if (providerClient != null){
                            System.out.println("ProviderClient: "+providerClient);
                        }
                        providerClient.close();
                    }
                }
               case 2 -> {

               }
               case 3 -> {
                    switch (args[0]){
                        case "--user_restrict" -> {
                            if (isValidRestriction(args[1])) {
                                setUserRestrict(args[1], Boolean.parseBoolean(args[2]));
                            }else {
                                throw new RuntimeException("未知限制策略 "+args[1]);
                            }
                        }
                    }
               }
            }

            quit();

        }

    }

}
