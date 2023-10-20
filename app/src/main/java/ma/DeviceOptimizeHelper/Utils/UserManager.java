package ma.DeviceOptimizeHelper.Utils;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.IInterface;
import android.os.Process;
import android.os.UserHandle;
import android.util.ArraySet;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class UserManager {

    private IInterface manager;
    private Method setUserRestrictionMethod;
    private Method getUserRestrictionsMethod;
    public UserManager(IInterface manager){
        this.manager = manager;
    }

    private Method setUserRestrictionMethod() throws NoSuchMethodException {

        if (setUserRestrictionMethod == null){
            setUserRestrictionMethod = manager.getClass().getMethod("setUserRestriction",String.class, boolean.class, int.class);
        }
        return setUserRestrictionMethod;

    }

    private Method getUserRestrictionsMethod() throws NoSuchMethodException {
        if (getUserRestrictionsMethod == null){
            getUserRestrictionsMethod = manager.getClass().getMethod("getUserRestrictions", int.class);
        }
        return getUserRestrictionsMethod;
    }

    public static ArraySet<String> getALLUserRestrictionsReflectForUserManager(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub =  Class.forName("android.os.UserManager");
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

    public void setUserRestrictionReflect(String key, boolean value){
        try {
            Method setUserRestrictionMethod =  setUserRestrictionMethod();
            setUserRestrictionMethod.invoke(manager ,key, value, myUserId());
        } catch (Exception e2) {
            throw new RuntimeException(e2);
        }
        System.out.println("setUserRestriction: "+key+" set to "+isUserRestrictionsReflectForKey(key));
    }

    public boolean isUserRestrictionsReflectForKey(String key){
        try {
            // 调用 getUserRestrictions 方法获取用户限制
            Bundle userRestrictions = (Bundle) getUserRestrictionsMethod().invoke(manager, myUserId());
            // 根据给定的键获取用户限制的布尔值
            return userRestrictions.getBoolean(key);
        } catch (Exception e2) {
            // 捕获异常并抛出运行时异常
            throw new RuntimeException(e2);
        }
    }


    public static int myUserId(){
        try {
            return (int) UserHandle.class.getMethod("myUserId").invoke(Process.myUserHandle());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

}
