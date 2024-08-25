package com.sbmatch.deviceopt.Interface;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuUserServiceArgs;
import com.sbmatch.deviceopt.Utils.ToastUtils;

import ma.DeviceOptimizeHelper.BuildConfig;

public class DhizukuUserServiceFactory implements AbstractIUserServiceFactory {

    private static DhizukuUserServiceFactory dhizukuUserServiceFactory;
    private OnUserServiceCallbackListener callbackListener;
    private final static String TAG = DhizukuUserServiceFactory.class.getSimpleName();

    public DhizukuUserServiceFactory() {
        Log.i(TAG, "*************实例化************");
    }

    public static DhizukuUserServiceFactory get() {
        if (dhizukuUserServiceFactory == null) dhizukuUserServiceFactory = new DhizukuUserServiceFactory();
        return dhizukuUserServiceFactory;
    }

    private final IBinder.DeathRecipient deathRecipient = () -> {
        ToastUtils.toast(TAG +": 服务死亡");
    };

    private final DhizukuUserServiceArgs dhizukuUserServiceArgs = new DhizukuUserServiceArgs(new ComponentName(BuildConfig.APPLICATION_ID, DhizukuUserServiceImpl.class.getName()));
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            if (callbackListener != null && service != null) {
                Log.i(TAG, "Dhizuku userService is ready.");
                callbackListener.onUserServiceReady(service);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("Dhizuku", name + "  is Disconnected");
        }
    };

    public void setOnCallbackListener(OnUserServiceCallbackListener mOnCallbackListener) {
        Log.i(TAG, "设置回调监听器: "+mOnCallbackListener);
        this.callbackListener = mOnCallbackListener;
    }

    @Override
    public void bindUserService() {
        Log.i(TAG, "**********尝试绑定用户服务*************");
        Dhizuku.bindUserService(dhizukuUserServiceArgs, connection);
    }

    @Override
    public void unbindUserService() {
        Dhizuku.unbindUserService(connection);
    }
}
