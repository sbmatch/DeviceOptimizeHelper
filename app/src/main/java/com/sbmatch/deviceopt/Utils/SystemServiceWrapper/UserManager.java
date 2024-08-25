package com.sbmatch.deviceopt.Utils.SystemServiceWrapper;


import android.os.IInterface;

import com.sbmatch.deviceopt.Utils.ReflectUtil;
import com.sbmatch.deviceopt.Utils.UserHandle;

import java.util.Collection;

public class UserManager {
    private IInterface manager;

    public UserManager(IInterface manager){
        this.manager = manager;
    }

    public Collection<?> getALLUserRestrictionsWithReflect(){
       return ReflectUtil.getFieldsByPrefixMatch(android.os.UserManager.class, "DISALLOW_").values();
    }

    public boolean hasUserRestriction(String restrictionKey){
        return (boolean) ReflectUtil.callObjectMethod2(manager, "hasUserRestriction", restrictionKey, UserHandle.myUserId());
    }

    public void setUserRestriction(String key, boolean value){
        ReflectUtil.callObjectMethod2(manager, "setUserRestriction", key, value, UserHandle.myUserId());
    }
}
