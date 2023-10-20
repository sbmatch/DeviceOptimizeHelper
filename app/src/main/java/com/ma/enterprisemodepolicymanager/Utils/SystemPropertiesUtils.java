package com.ma.enterprisemodepolicymanager.Utils;

import java.lang.reflect.Method;

public class SystemPropertiesUtils {
    private Class<?> clazz;
    private Method getMethod;
    private Method getBooleanMethod;
    public SystemPropertiesUtils(){

    }

    private Class<?> getClazz() throws ClassNotFoundException {
        if (clazz == null) clazz = Class.forName("android.os.SystemProperties");
        return clazz;
    }

    private Method getGetMethod() throws ClassNotFoundException, NoSuchMethodException {
        if (getMethod == null) getMethod = getClazz().getMethod("get", String.class);
        return getMethod;
    }

    private Method getGetBooleanMethod() throws ClassNotFoundException, NoSuchMethodException {
        if (getBooleanMethod == null) getBooleanMethod = getClazz().getMethod("getBoolean", String.class, boolean.class);
        return getBooleanMethod;
    }

    public String get(String key){
        try {
            return (String) getGetMethod().invoke(null, key);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public boolean getBoolean(String key, boolean def){
        try {
            return (boolean) getGetBooleanMethod().invoke(null, key, def);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

}
