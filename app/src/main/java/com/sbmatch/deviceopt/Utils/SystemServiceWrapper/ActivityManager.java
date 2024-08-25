package com.sbmatch.deviceopt.Utils.SystemServiceWrapper;

import android.app.IActivityController;
import android.app.IProcessObserver;
import android.content.Intent;
import android.os.IInterface;

import com.sbmatch.deviceopt.Utils.ReflectUtil;
import com.sbmatch.deviceopt.Utils.UserHandle;

public class ActivityManager {

    private final IInterface manager;

    public ActivityManager(IInterface manager){
        this.manager = manager;
    }

    public void forceStopPackage(String packageName){
        ReflectUtil.callObjectMethod2(manager,"forceStopPackage", packageName, UserHandle.myUserId());
    }

    public void setActivityController(IActivityController watcher, boolean imAMonkey){
        ReflectUtil.callObjectMethod2(manager, "setActivityController", watcher, imAMonkey);
    }
    public void registerProcessObserver(IProcessObserver observer){
        ReflectUtil.callObjectMethod2(manager, "registerProcessObserver", observer);
    }
    public void unregisterProcessObserver(IProcessObserver observer){
        ReflectUtil.callObjectMethod2(manager, "unregisterProcessObserver", observer);
    }

    public void broadcastIntent(Intent intent, boolean sticky){
        ReflectUtil.callObjectMethod2(manager, "broadcastIntent", null,
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
