package com.sbmatch.deviceopt.Interface;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.sbmatch.deviceopt.Utils.ContextUtil;
import com.sbmatch.deviceopt.Utils.ToastUtils;

import ma.DeviceOptimizeHelper.BuildConfig;
import rikka.shizuku.Shizuku;

public class ShizukuUserServiceFactory implements AbstractIUserServiceFactory {
    private OnUserServiceCallbackListener userServiceCallbackListener;
    private IBinder userServiceIBinder;
    private final static String TAG = ShizukuUserServiceFactory.class.getSimpleName();
    public ShizukuUserServiceFactory(){

    }

    private final IBinder.DeathRecipient deathRecipient = () -> {
        ToastUtils.toast(TAG +": 服务死亡");
        userServiceIBinder = null;
    };

    private final Shizuku.UserServiceArgs userServiceArgs = new Shizuku.UserServiceArgs(new ComponentName(ContextUtil.getContext(), ShizukuUserServiceImpl.class))
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
                userServiceCallbackListener.onUserServiceReady(binder);
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
            try {
                bindUserService();
            }catch (Throwable ignored){}
        }
    };


    public void setOnUserServiceCallbackListener(OnUserServiceCallbackListener callback) {
        this.userServiceCallbackListener= callback;
    }

    @Override
    public void bindUserService() {
        Shizuku.bindUserService(userServiceArgs, connection);
    }

    @Override
    public void unbindUserService() {
        try {
            userServiceIBinder.unlinkToDeath(deathRecipient, 0);
        }catch (Throwable ignored){}
        Shizuku.unbindUserService(userServiceArgs, connection, true);
    }
}
