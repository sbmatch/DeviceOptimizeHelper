package com.ma.enterprisemodepolicymanager.Utils;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Looper;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ContextUtils {

    private ContextUtils() {

    }
    public static Object retrieveSystemContext() {
        try {
            @SuppressLint("PrivateApi")
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Constructor<?> activityThreadConstructor = activityThreadClass.getDeclaredConstructor();
            activityThreadConstructor.setAccessible(true);
            Object activityThread = activityThreadConstructor.newInstance();
            Method getSystemContextMethod = activityThread.getClass().getDeclaredMethod("getSystemContext");
            getSystemContextMethod.setAccessible(true);
            return getSystemContextMethod.invoke(activityThread);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static ContentResolver getContentResolver() {
        Object obj = retrieveSystemContext();
        try {
            return (ContentResolver) obj.getClass().getMethod("getContentResolver").invoke(obj);
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static Context createPackageContext(String packageName){
       try {
           Object obj =  retrieveSystemContext();
           return (Context) obj.getClass().getMethod("createPackageContext", String.class,int.class).invoke(obj,packageName, Context.CONTEXT_INCLUDE_CODE | Context.CONTEXT_IGNORE_SECURITY);
       }catch (Throwable e){
           e.printStackTrace();
           throw new RuntimeException(e);
       }
    }
}
