package com.sbmatch.deviceopt.utils.SystemServiceWrapper;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.IInterface;

import com.sbmatch.deviceopt.utils.ReflectUtils;

import java.util.List;

public class AppRunningControlManager {
    private static final String ACTION_APP_RUNNING_BLOCK = "com.miui.securitycore.APP_RUNNING_BLOCK";
    private static final String PACKAGE_SECURITY_CORE = "com.miui.securitycore";
    private Intent mDisAllowRunningHandleIntent;
    private IInterface appRunningControlManager;

    @SuppressLint("WrongConstant")
    public AppRunningControlManager(Object manager){
        appRunningControlManager = (IInterface) ReflectUtils.getObjectField(manager,"mService");
        Intent intent = new Intent(ACTION_APP_RUNNING_BLOCK);
        this.mDisAllowRunningHandleIntent = intent;
        intent.setPackage(PACKAGE_SECURITY_CORE);
        this.mDisAllowRunningHandleIntent.setFlags(0x10800000);
    }

    public void setBlackListEnable(boolean isEnable){
        ReflectUtils.callObjectMethod2(appRunningControlManager, "setBlackListEnable", isEnable);
    }

    public void setDisallowRunningList(List<String> list){
        ReflectUtils.callObjectMethod2(appRunningControlManager, "setDisallowRunningList", list, this.mDisAllowRunningHandleIntent);
    }

    public List<String> getNotDisallowList(){
        return (List<String>) ReflectUtils.callObjectMethod2(appRunningControlManager, "getNotDisallowList");
    }
}
