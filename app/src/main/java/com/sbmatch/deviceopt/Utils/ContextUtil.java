package com.sbmatch.deviceopt.utils;

import android.annotation.SuppressLint;
import android.app.ActivityThread;
import android.app.ContextImpl;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Process;

import java.lang.reflect.Constructor;

public class ContextUtil {
    private ContextUtil() {

    }

    private static ActivityThread getActivityThread(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Constructor<?> activityThreadConstructor = activityThreadClass.getDeclaredConstructor();
            activityThreadConstructor.setAccessible(true);
            return (ActivityThread) activityThreadConstructor.newInstance();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public static ContextImpl getSystemContext() {

        // 如果调用方是系统进程则获取SystemContext
        if (Process.myUid() == Process.ROOT_UID || Process.myUid() == Process.SYSTEM_UID | Process.myUid() == Process.SHELL_UID){
            return ActivityThread.systemMain().getSystemContext();
        }
        return null;
    }


    public static Context createPackageContext(String packageName){
        try {
            return ActivityThread.currentActivityThread().getApplication().createPackageContext(packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
        }catch (PackageManager.NameNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    public static Context createContextAsUser(){
        int userId = UserHandle.myUserId();
        return (Context) ReflectUtils.callObjectMethod2(
                getSystemContext(),
                "createContextAsUser",
                UserHandle.of(userId),
                0);
    }
}
