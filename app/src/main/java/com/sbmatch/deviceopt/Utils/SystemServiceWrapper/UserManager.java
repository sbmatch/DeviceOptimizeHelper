package com.sbmatch.deviceopt.Utils.SystemServiceWrapper;


import android.content.Context;

import com.sbmatch.deviceopt.Utils.ContextUtil;
import com.sbmatch.deviceopt.Utils.ReflectUtil;

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
        this.um = (android.os.UserManager) ContextUtil.getContext().getSystemService(Context.USER_SERVICE);
    }

    public static UserManager get() {
        if (userManager == null) userManager = new UserManager();
        return userManager;
    }


    public static Collection<?> getALLUserRestrictionsWithReflect(){
       return ReflectUtil.getFieldsByPrefixMatch(android.os.UserManager.class, "DISALLOW_").values();
    }

    public boolean hasUserRestriction(String restrictionKey){
        return (boolean) ReflectUtil.callObjectMethod2(um, "hasUserRestriction", restrictionKey);
    }

    public void setUserRestriction(String key, boolean value){
        ReflectUtil.callObjectMethod2(um, "setUserRestriction", key, value);
    }
}
