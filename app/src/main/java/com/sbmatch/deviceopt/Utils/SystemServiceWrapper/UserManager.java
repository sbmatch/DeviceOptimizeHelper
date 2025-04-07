package com.sbmatch.deviceopt.utils.SystemServiceWrapper;


import android.app.ActivityThread;
import android.content.Context;

import com.sbmatch.deviceopt.utils.ReflectUtils;

import java.util.Collection;

public class UserManager {
    private final android.os.UserManager um;
    private static UserManager userManager;
    private UserManager(Context context){
        this.um = (android.os.UserManager) context.getSystemService(Context.USER_SERVICE);
    }

    public static UserManager get(Context context) {
        if (userManager == null) userManager = new UserManager(context);
        return userManager;
    }

    private UserManager(){
        this.um = (android.os.UserManager) ActivityThread.currentApplication().getSystemService(Context.USER_SERVICE);
    }

    public static UserManager get() {
        if (userManager == null) userManager = new UserManager();
        return userManager;
    }


    public static Collection<?> getALLUserRestrictionsByReflect(){
       return ReflectUtils.getFieldsByPrefixMatch(android.os.UserManager.class, "DISALLOW_").values();
    }

    public boolean hasUserRestriction(String restrictionKey){
        return (boolean) ReflectUtils.callObjectMethod2(um, "hasUserRestriction", restrictionKey);
    }

    public void setUserRestriction(String key, boolean value){
        ReflectUtils.callObjectMethod2(um, "setUserRestriction", key, value);
    }
}
