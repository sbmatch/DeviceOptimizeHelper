package ma.DeviceOptimizeHelper.Utils;


import android.annotation.SuppressLint;
import android.os.IInterface;
import android.os.Process;
import android.os.UserHandle;
import android.util.ArraySet;

import java.lang.reflect.Field;

public class UserManager {

    IInterface manager;
    public UserManager(IInterface manager){
        this.manager = manager;
    }

    public static int myUserId(){
        try {
            return (int) ReflectUtil.callStaticObjectMethod(UserHandle.class, "myUserId");
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public ArraySet<String> getALLUserRestrictionsWithReflect(){
        ArraySet<String> fields= new ArraySet<>();
        for (Field field : ReflectUtil.getFieldsByClass(android.os.UserManager.class)){
            if (field.getName().contains("DISALLOW_")){
                fields.add((String) ReflectUtil.getValueByField(field, null));
            }
        }
        return fields;
    }

    public void setUserRestrictionWithReflect(String key, boolean value){
        ReflectUtil.callObjectMethod2(manager, "setUserRestriction", key, value, myUserId());
    }

    public boolean hasUserRestriction(String restrictionKey){
        return (boolean) ReflectUtil.callObjectMethod2(manager, "hasUserRestriction", restrictionKey, myUserId());
    }
}
