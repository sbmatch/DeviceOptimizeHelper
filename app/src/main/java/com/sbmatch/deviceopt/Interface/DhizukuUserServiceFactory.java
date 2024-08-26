package com.sbmatch.deviceopt.Interface;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuUserServiceArgs;
import com.sbmatch.deviceopt.Utils.ContextUtil;
import com.sbmatch.deviceopt.Utils.ToastUtils;

public class DhizukuUserServiceFactory implements AbstractIUserServiceFactory {

    private static DhizukuUserServiceFactory dhizukuUserServiceFactory;
    private OnUserServiceCallbackListener callbackListener;
    private final static String TAG = DhizukuUserServiceFactory.class.getSimpleName();

    public DhizukuUserServiceFactory() {
        Log.i(TAG, "************* Dhizuku 用户服务工厂************");
    }

    public static DhizukuUserServiceFactory get() {
        if (dhizukuUserServiceFactory == null) dhizukuUserServiceFactory = new DhizukuUserServiceFactory();
        return dhizukuUserServiceFactory;
    }

    private final IBinder.DeathRecipient deathRecipient = () -> {
        ToastUtils.toast(TAG +": 服务死亡");
    };

    private final DhizukuUserServiceArgs dhizukuUserServiceArgs = new DhizukuUserServiceArgs(new ComponentName(ContextUtil.getContext(), DhizukuUserServiceImpl.class));
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            if (callbackListener != null && service != null && service.pingBinder()) {
                Log.i(TAG, " Dhizuku 用户服务已连接");
                callbackListener.onUserServiceReady(service);
                try {
                    service.linkToDeath(deathRecipient, 0);
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }

            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.e("Dhizuku", name + " 断开连接");
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
        Log.i(TAG, "**********尝试解绑用户服务*************");
        Dhizuku.unbindUserService(connection);
    }
}
