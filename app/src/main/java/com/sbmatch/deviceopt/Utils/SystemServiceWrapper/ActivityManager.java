package com.sbmatch.deviceopt.utils.SystemServiceWrapper;

import android.app.IActivityController;
import android.app.IProcessObserver;
import android.content.Intent;
import android.os.IInterface;

import com.sbmatch.deviceopt.utils.ReflectUtils;
import com.sbmatch.deviceopt.utils.UserHandle;

public class ActivityManager {

    private final IInterface manager;

    public ActivityManager(IInterface manager){
        this.manager = manager;
    }

    public void forceStopPackage(String packageName){
        ReflectUtils.callObjectMethod2(manager,"forceStopPackage", packageName, UserHandle.myUserId());
    }

    public void setActivityController(IActivityController watcher, boolean imAMonkey){
        ReflectUtils.callObjectMethod2(manager, "setActivityController", watcher, imAMonkey);
    }
    public void registerProcessObserver(IProcessObserver observer){
        ReflectUtils.callObjectMethod2(manager, "registerProcessObserver", observer);
    }
    public void unregisterProcessObserver(IProcessObserver observer){
        ReflectUtils.callObjectMethod2(manager, "unregisterProcessObserver", observer);
    }

    public void attachApplication(Object app, long startSeq){
        ReflectUtils.callObjectMethod2(manager, "attachApplication", app, startSeq);
    }

    public void broadcastIntent(Intent intent, boolean sticky){
        ReflectUtils.callObjectMethod2(manager, "broadcastIntent", null,
                intent,
                null,
                null,
                -1,
                null,
                null,
                null,
                0,
                null,
                false,
                sticky,
                -1);
    }
}
