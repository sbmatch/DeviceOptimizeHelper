package com.sbmatch.deviceopt;

import android.app.Application;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.tencent.mmkv.MMKV;

import java.util.logging.Logger;

public class AppGlobals {
    private static final String TAG = "AppGlobals";
    private static Application sApplication;
    public static Handler sMainHandler = initMainHandler();

    public static void init(Application application) {
        sApplication = application;
    }
    public static <T> T getSystemService(String service) {
        return (T) sApplication.getSystemService(service);
    }

    public static <T> T getSystemService(Class<T> serviceClass){
        return (T) sApplication.getSystemService(serviceClass);
    }

    public static void loadLibrary(final String library){
        System.loadLibrary(library);
    }

//    public static void loadLibrary(final String library,
//                                   final ReLinker.LoadListener listener){
//        ReLinker.loadLibrary(sApplication, library, listener);
//    }

    private static Handler initMainHandler() {
        return new Handler(Looper.getMainLooper());
    }

    public static Resources getResources(){
        return sApplication.getResources();
    }
    public static Context createPackageContext(String pkg) {
        try {
            return sApplication.createPackageContext(pkg, Context.CONTEXT_IGNORE_SECURITY);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, e.getMessage(), e);
            return null;
        }
    }

    public static Logger getLogger() {
        return Logger.getLogger(TAG);
    }
    public static Logger getLogger(String tag) {
        return Logger.getLogger(tag);
    }
    public static ContentResolver getContentResolver() {
        return sApplication.getContentResolver();
    }

    public static MMKV getMMKV() {
        return MMKV.defaultMMKV();
    }

    public static MMKV getMMKV(String mmapId) {
        return MMKV.mmkvWithID(mmapId);
    }
}
