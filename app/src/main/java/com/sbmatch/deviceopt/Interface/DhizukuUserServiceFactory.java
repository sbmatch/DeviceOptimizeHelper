package com.sbmatch.deviceopt.Interface;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuUserServiceArgs;

import java.util.logging.Logger;

public class DhizukuUserServiceFactory implements AbstractIUserServiceFactory {
    private OnBinderCallbackListener callback;
    private final static String TAG = DhizukuUserServiceFactory.class.getSimpleName();
    private final Logger logger = Logger.getLogger(TAG);
    private DhizukuUserServiceFactory() {
        if (!Dhizuku.isPermissionGranted()) {
            throw new RuntimeException("Dhizuku permission not granted!");
        }
    }


    public static DhizukuUserServiceFactory getInstance(){
        return new DhizukuUserServiceFactory();
    }

    private final DhizukuUserServiceArgs dhizukuUserServiceArgs = new DhizukuUserServiceArgs(new ComponentName(ActivityThread.currentApplication().getBaseContext(), DhizukuUserServiceImpl.class));
    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            if (callback != null && service != null) {
                logger.info( "Dhizuku 用户服务已连接!");
                try {
                    callback.onUserServiceReady(service, name.getClassName());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            logger.severe( "Dhizuku 用户服务断开连接 "+name.getClassName());
            try {
                callback.onUserServiceDisconnected(name.getClassName());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    };


    @Override
    public boolean bindUserService(OnBinderCallbackListener callbackListener) {
        logger.info( "+++++++++++++++尝试绑定用户服务++++++++++++++");
        callback = callbackListener;
        return Dhizuku.bindUserService(dhizukuUserServiceArgs, connection);
    }

    @Override
    public void unbindUserService() {
        logger.severe("***************解绑用户服务*****************");
        Dhizuku.unbindUserService(connection);
    }
}
