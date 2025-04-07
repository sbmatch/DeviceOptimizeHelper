package com.sbmatch.deviceopt.Interface;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sbmatch.deviceopt.BuildConfig;
import com.sbmatch.deviceopt.utils.ToastUtils;

import java.util.concurrent.atomic.AtomicBoolean;

import rikka.shizuku.Shizuku;

public class ShizukuUserServiceFactory implements AbstractIUserServiceFactory {
    private IBinder userServiceIBinder;
    private OnBinderCallbackListener callback;
    private final static String TAG = ShizukuUserServiceFactory.class.getSimpleName();
    private final AtomicBoolean isBind = new AtomicBoolean(false);
    private ShizukuUserServiceFactory(){

    }

    public static ShizukuUserServiceFactory get() {
        return new ShizukuUserServiceFactory();
    }

    private final IBinder.DeathRecipient deathRecipient = () -> {
        ToastUtils.toast("RemoteIBinder is Died");
        userServiceIBinder = null;
        isBind.set(false);
        bindUserService(callback);
    };

    private final Shizuku.UserServiceArgs userServiceArgs = new Shizuku.UserServiceArgs(new ComponentName(ActivityThread.currentApplication(), ShizukuUserServiceImpl.class))
            .daemon(false)
            .processNameSuffix("service")
            .debuggable(BuildConfig.DEBUG)
            .version(BuildConfig.VERSION_CODE);


    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder binder) {
            StringBuilder res = new StringBuilder();
            res.append("onServiceConnected: ").append(componentName.getClassName()).append('\n');
            if (binder != null && binder.pingBinder()) {
                userServiceIBinder = binder;
                isBind.set(true);
                try {
                    callback.onUserServiceReady(binder, componentName.getClassName());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                try {
                    binder.linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
                Log.i(TAG, res.toString());
            } else {
                res.append("invalid binder for ").append(componentName).append(" received");
                Log.e(TAG, res.toString());
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    public boolean bindUserService(OnBinderCallbackListener callbackListener) {
        this.callback = callbackListener;
        Shizuku.bindUserService(userServiceArgs, connection);
        return isBind.get();
    }

    @Override
    public void unbindUserService() {
        try {
            userServiceIBinder.unlinkToDeath(deathRecipient, 0);
            Shizuku.unbindUserService(userServiceArgs, connection, false);
        }catch (Throwable ignored){}

    }
}
