package com.sbmatch.deviceopt.utils.SystemServiceWrapper;

import android.os.IInterface;

import com.sbmatch.deviceopt.utils.ReflectUtils;

public class AppOpsManager {
    private final IInterface manager;

    public AppOpsManager(IInterface manager){
        this.manager = manager;
    }

    public void setMode(int code, int uid, String packageName, int mode){
        ReflectUtils.callObjectMethod2(manager, "setMode", code, uid, packageName, mode);
    }
}
