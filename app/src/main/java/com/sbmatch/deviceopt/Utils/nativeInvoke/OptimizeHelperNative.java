package com.sbmatch.deviceopt.utils.nativeInvoke;

import android.util.Singleton;

import com.sbmatch.deviceopt.AppGlobals;

public class OptimizeHelperNative extends Singleton<OptimizeHelperNative> {

    static {
        AppGlobals.loadLibrary("DeviceOptimizeHelper");
    }

    private OptimizeHelperNative() {

    }

    private static final Singleton<OptimizeHelperNative> instance = new OptimizeHelperNative(); // 静态内部类实现

    @Override
    protected OptimizeHelperNative create() {
        return new OptimizeHelperNative().get();
    }

    public static OptimizeHelperNative getInstance() {
        return instance.get();
    }

    public native void optimize();

}
