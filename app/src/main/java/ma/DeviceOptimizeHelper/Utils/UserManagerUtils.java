package ma.DeviceOptimizeHelper.Utils;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Process;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class UserManagerUtils {

    public static ArrayMap<String, String> getALLUserRestrictionsForFramework(){

        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub =  Class.forName("android.os.UserManager");
            ArrayMap<String, String> fields= new ArrayMap<>();
            for (Field value : cStub.getFields()){
                if (value.getName().contains("DISALLOW_")){
                    fields.put(value.getName(), (String) value.get(null));
                }
            }
            Log.i(UserManagerUtils.class.getSimpleName(), Collections.singletonList(fields).toString());
            return fields;
        } catch (Exception e2) {
            throw new SecurityException(e2);
        }
    }

    public static void setUserRestrictionReflect(String key, boolean value){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub =  Class.forName("android.os.IUserManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            Object obj = asInterface.invoke(null, ServiceManager.getSystemService("user"));
            Method setUserRestrictionMethod =  obj.getClass().getMethod("setUserRestriction",String.class, boolean.class, int.class);
            setUserRestrictionMethod.invoke(obj,key, value, getIdentifier());
        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
        System.out.println("setUserRestriction: "+key+" set to "+getUserRestrictionsReflect().getBoolean(key));
    }

    public static Bundle getUserRestrictionsReflect(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub =  Class.forName("android.os.IUserManager$Stub");
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            Object obj = asInterface.invoke(null, ServiceManager.getSystemService("user"));
            return (Bundle) obj.getClass().getMethod("getUserRestrictions",int.class).invoke(obj, getIdentifier());
        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
    }

    public static int getIdentifier(){

        try {
            return (int) UserHandle.class.getMethod("getIdentifier").invoke(Process.myUserHandle());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

}
