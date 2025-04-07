package com.sbmatch.deviceopt.Interface;

import android.app.ActivityThread;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuUserServiceArgs;
import com.sbmatch.deviceopt.AppGlobals;

import java.util.logging.Logger;

public class DhizukuUserServiceFactory implements AbstractIUserServiceFactory {
    private OnBinderCallbackListener callback;
    private final static String TAG = "DhizukuUserServiceFactory";
    private DhizukuUserServiceFactory() {
        if (!Dhizuku.isPermissionGranted()) {
            AppGlobals.getLogger(TAG).severe("Dhizuku permission not granted!");
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
                AppGlobals.getLogger(TAG).info( "Dhizuku 用户服务已连接!");
                try {
                    service.linkToDeath(() -> {
                        AppGlobals.getLogger(TAG).severe( "Dhizuku UserService Remote Process is Die， So exec unbindUserService method -->"+name.getClassName());
                        unbindUserService();
                    },0);
                    callback.onUserServiceReady(service, name.getClassName());
                } catch (RemoteException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            AppGlobals.getLogger(TAG).severe( "##############Dhizuku 用户服务断开连接 "+name.getClassName());
            try {
                callback.onUserServiceDisconnected(name.getClassName());
            } catch (RemoteException e) {
                throw new RuntimeException(e);
            }
        }
    };


    @Override
    public boolean bindUserService(OnBinderCallbackListener callbackListener) {
        callback = callbackListener;
        return Dhizuku.bindUserService(dhizukuUserServiceArgs, connection);
    }

    @Override
    public void unbindUserService() {
        Dhizuku.stopUserService(dhizukuUserServiceArgs);
        Dhizuku.unbindUserService(connection);
    }

    /**
     * Use example:
     * DhizukuUserServiceFactory serviceFactory = DhizukuUserServiceFactory.getInstance();
     *
     * serviceFactory.bindUserService(new OnBinderCallbackListener() {
     *                 @Override
     *                 public void onUserServiceReady(IBinder service, String ImplClass) throws RemoteException {
     *
     *                     IUserService userService = IUserService.Stub.asInterface(service);
     *                     AppGlobals.getLogger("DhizukuUserServiceFactory").info("call isPackageSuspended method: "+userService.isPackageSuspended("android"));
     *                     serviceFactory.unbindUserService();
     *                 }
     *
     *                 @Override
     *                 public void onUserServiceDisconnected(String ImplClass) throws RemoteException {
     *                    AppGlobals.getLogger("DhizukuUserServiceFactory").severe("UserService disconnected: "+ ImplClass);
     *                 }
     *             });
     */
}
