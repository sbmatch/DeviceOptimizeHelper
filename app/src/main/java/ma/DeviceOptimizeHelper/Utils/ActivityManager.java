package ma.DeviceOptimizeHelper.Utils;

import android.annotation.SuppressLint;
import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ActivityManager {

    private final IInterface manager;

    public ActivityManager(IInterface manager){
        this.manager = manager;
    }

    public void forceStopPackage(String packageName){
        ReflectUtil.callObjectMethod2(manager,"forceStopPackage", packageName, UserManager.myUserId());
    }

}
