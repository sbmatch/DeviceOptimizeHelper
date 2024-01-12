//
// Decompiled by Jadx - 540ms
//
package com.ma.enterprisemodepolicymanager.Utils;

import static android.os.Build.VERSION_CODES.P;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ReflectUtil {
    private ReflectUtil(){

    }
    public static Object callAnyObjectMethod(Class<?> cls, Object obj, String methodName, Class<?>[] parameterTypes, Object... args)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName, parameterTypes);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(obj, args);
        } catch (Throwable e) {throw new RuntimeException(e);}
    }

    public static Object callObjectMethod(Object obj, String methodName, Class<?> cls)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(obj);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }
    public static Object callObjectMethod(Object obj, String methodName, Class<?> cls, Class<?>[] parameterTypes, Object... args)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName, parameterTypes);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(obj, args);
        }catch (Throwable e){ throw new RuntimeException(e); }
    }

    public static Object callObjectMethod2(Object obj, String methodName, Class<?>[] parameterTypes, Object... args)  {
        try {
            Method method = obj.getClass().getDeclaredMethod(methodName, parameterTypes);
            method.setAccessible(true);
            return method.invoke(obj, args);
        }catch (Throwable e){ throw new RuntimeException(e); }
    }

    public static Object callStaticObjectMethod(Class<?> cls, String methodName, Class<?>[] parameterTypes, Object... args)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName, parameterTypes);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(null, args);
        }catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Object callStaticObjectMethod(Class<?> cls, String methodName)  {
        try {
            Method declaredMethod = cls.getDeclaredMethod(methodName);
            declaredMethod.setAccessible(true);
            return declaredMethod.invoke(null);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Object getObjectField(Object obj, Class<?> cls, String fieldName) {
        try {
            Field declaredField = cls.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(obj);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Object getObjectField(Object obj, String fieldName) {
        try {
            Field declaredField = obj.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(obj);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Object getStaticObjectField(Class<?> cls, String fieldName) {
        try {
            Field declaredField = cls.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            return declaredField.get(null);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    @TargetApi(P)
    public static Object getStaticObjectField(String clsName, String fieldName) {
        try {
            List<Field> allInstanceFields = HiddenApiBypass.getStaticFields(Class.forName(clsName));
            for (Field field : allInstanceFields){
                if (field.getName().equals(fieldName)) {
                    field.setAccessible(true);
                    return field.get(null);
                }
            }
            return null;
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static void setObjectField(Object obj, Class<?> cls, String fieldName, Object newValue) {
        try {
            Field declaredField = cls.getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(obj, newValue);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static void setObjectField(Object obj, String fieldName, Object newValue) {
        try {
            Field declaredField = obj.getClass().getDeclaredField(fieldName);
            declaredField.setAccessible(true);
            declaredField.set(obj, newValue);
        } catch (Throwable e) { throw new RuntimeException(e); }
    }

    public static Class<?> getClazzByClassLoader(ClassLoader classLoader, String clazzName){
        try {
            return classLoader.loadClass(clazzName);
        }catch (Throwable e){
           throw new RuntimeException(e);
        }
    }

    @SuppressLint("PrivateApi")
    @TargetApi(P)
    public static Object getObjectInstance(String className, Object... args){
        try {
            return HiddenApiBypass.newInstance(Class.forName(className), args);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    @SuppressLint("PrivateApi")
    @TargetApi(P)
    public static Object getObjectInstance(String className){
        try {
            return HiddenApiBypass.newInstance(Class.forName(className));
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }
}
