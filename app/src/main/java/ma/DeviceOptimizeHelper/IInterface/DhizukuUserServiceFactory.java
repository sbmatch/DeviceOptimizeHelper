package ma.DeviceOptimizeHelper.IInterface;

import android.content.ComponentName;
import android.content.Context;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuUserServiceArgs;

import ma.DeviceOptimizeHelper.IUserService;
import ma.DeviceOptimizeHelper.Utils.ContextUtils;

public class DhizukuUserServiceFactory implements AbstractIUserServiceFactory{
    private IUserService userService;
    private IUserServiceCallback callback;
    private final static String TAG = DhizukuUserServiceFactory.class.getSimpleName();
    public DhizukuUserServiceFactory(IUserServiceCallback serviceCallback){
        callback = serviceCallback;
    }

    @Override
    public void createIUserService() {
        DhizukuUserServiceArgs args = new DhizukuUserServiceArgs(new ComponentName(ContextUtils.getContext(), DhizukuUserServiceImpl.class));
        Dhizuku.bindUserService(args, new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                userService = IUserService.Stub.asInterface(service);
                Log.i(TAG,"UserService Is Ready."+" name:"+name);
                if (callback != null) {
                    callback.onUserServiceReady(userService);
                }
            }
            @Override
            public void onServiceDisconnected(ComponentName name) {
                Log.e("Dhizuku", name + "  is Disconnected");
            }
        });
    }

}
