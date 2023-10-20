package com.ma.enterprisemodepolicymanager.Utils;

import android.app.IApplicationThread;
import android.app.IProcessObserver;
import android.content.ComponentName;
import android.content.IIntentReceiver;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IInterface;

import java.lang.reflect.Method;
import java.util.List;

public class ActivityManager {

    private final IInterface manager;
    private final PackageManager packageManager = ServiceManager.getPackageManager();
    private Method forceStopPackageMethod;
    private Method broadcastIntentMethod, unbroadcastIntentMethod;
    private Method getRunningAppProcessesMethod;
    private Method registerProcessObserverMethod, unregisterProcessObserverMethod;
    private Method startServiceMethod;
    private Method getServicesMethod;

    public ActivityManager(IInterface manager) {
        this.manager = manager;
    }

    private Method getForceStopPackageMethod() throws NoSuchMethodException {
        if (forceStopPackageMethod == null) {
            forceStopPackageMethod = manager.getClass().getMethod("forceStopPackage", String.class, int.class);
        }
        return forceStopPackageMethod;
    }

    private Method getBroadcastIntentMethod() throws NoSuchMethodException {
        if (broadcastIntentMethod == null) broadcastIntentMethod = manager.getClass().getMethod(  "broadcastIntent",
                IApplicationThread.class,
                Intent.class,
                String.class,
                IIntentReceiver.class,
                int.class,
                String.class,
                Bundle.class,
                String[].class,
                int.class,
                Bundle.class,
                boolean.class,
                boolean.class,
                int.class
        );
        return broadcastIntentMethod;
    }

    private Method getUnbroadcastIntentMethod() throws NoSuchMethodException {
        if (unbroadcastIntentMethod == null) unbroadcastIntentMethod = manager.getClass().getMethod("unbroadcastIntent", IApplicationThread.class, Intent.class, int.class);
        return unbroadcastIntentMethod;
    }

    private Method getGetRunningAppProcessesMethod() throws NoSuchMethodException {
        if (getRunningAppProcessesMethod == null) getRunningAppProcessesMethod = manager.getClass().getMethod("getRunningAppProcesses");
        return getRunningAppProcessesMethod;
    }

    private Method getRegisterProcessObserverMethod() throws NoSuchMethodException {
        if (registerProcessObserverMethod == null) registerProcessObserverMethod = manager.getClass().getMethod("registerProcessObserver", IProcessObserver.class);
        return registerProcessObserverMethod;
    }

    private Method getUnregisterProcessObserverMethod() throws NoSuchMethodException {
        if (unregisterProcessObserverMethod == null) unregisterProcessObserverMethod = manager.getClass().getMethod("unregisterProcessObserver", IProcessObserver.class);
        return unregisterProcessObserverMethod;
    }

    private Method getStartServiceMethod() throws NoSuchMethodException {
        if (startServiceMethod == null ) startServiceMethod = manager.getClass().getMethod("startService",
                IApplicationThread.class,
                Intent.class,
                String.class,
                boolean.class,
                String.class,
                String.class,
                int.class
                );
        return startServiceMethod;
    }

    private Method getGetServicesMethod() throws NoSuchMethodException {
        if (getServicesMethod == null) getServicesMethod = manager.getClass().getMethod("getServices",int.class, int.class);
        return getServicesMethod;
    }

    public void registerProcessObserver(IProcessObserver observer){
        try {
            getRegisterProcessObserverMethod().invoke(manager, observer);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void unregisterProcessObserver(IProcessObserver observer){
        try {
            getUnregisterProcessObserverMethod().invoke(manager, observer);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public ComponentName startService(Intent intent, boolean requireForeground){
        try {
            return (ComponentName) getStartServiceMethod().invoke(manager, null, intent, null, requireForeground, packageManager.getNameForUid(Binder.getCallingUid()), null, -1);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public List<android.app.ActivityManager.RunningAppProcessInfo> getRunningAppProcesses(){
        try {
            return (List<android.app.ActivityManager.RunningAppProcessInfo>) getGetRunningAppProcessesMethod().invoke(manager);
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public List<android.app.ActivityManager.RunningServiceInfo> getServices(int maxNum){
        try {
            return (List<android.app.ActivityManager.RunningServiceInfo>) getGetServicesMethod().invoke(manager, maxNum, 0);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void broadcastIntent(Intent intent, boolean sticky){
        try {
            getBroadcastIntentMethod().invoke(manager ,null,
                    intent,
                    null,
                    null,
                    -1,
                    null,
                    null,
                    null,
                    0,
                    null,
                    false,
                    sticky,
                    -1);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }



    public void unbroadcastIntent(Intent intent){
        try {
            getUnbroadcastIntentMethod().invoke(manager, null, intent, -1);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void forceStopPackage(String packageName) {
        try {
            Method method = getForceStopPackageMethod();
            method.invoke(manager, packageName, -2);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public static boolean isServiceRunning(ActivityManager activityManager, String className) {
        try {
            List<android.app.ActivityManager.RunningServiceInfo> info = activityManager.getServices(Integer.MAX_VALUE);
            if (info == null || info.size() == 0) return false;
            for (android.app.ActivityManager.RunningServiceInfo aInfo : info) {
                if (className.contains(aInfo.service.getClassName())) return true;
            }
            return false;
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

}
