package ma.DeviceOptimizeHelper.Utils;

import android.annotation.SuppressLint;
import android.app.admin.IDevicePolicyManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.os.IInterface;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuBinderWrapper;
import com.rosan.dhizuku.shared.DhizukuVariables;

import java.lang.reflect.Field;

import ma.DeviceOptimizeHelper.BaseApplication.BaseApplication;

public class DevicePolicyManager {
    private IInterface manager;
    public DevicePolicyManager(IInterface manager){
        this.manager = manager;
    }

    @SuppressLint("SoonBlockedPrivateApi")
    public static android.app.admin.DevicePolicyManager binderWrapperDevicePolicyManager() {
        try {
            Context context = BaseApplication.getContext().createPackageContext(DhizukuVariables.PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
            android.app.admin.DevicePolicyManager manager = (android.app.admin.DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            Field field = manager.getClass().getDeclaredField("mService");
            field.setAccessible(true);
            IDevicePolicyManager oldInterface = (IDevicePolicyManager) field.get(manager);
            if (oldInterface instanceof DhizukuBinderWrapper) return manager;
            assert oldInterface != null;
            IBinder oldBinder = oldInterface.asBinder();
            IBinder newBinder = Dhizuku.binderWrapper(oldBinder);
            IDevicePolicyManager newInterface = IDevicePolicyManager.Stub.asInterface(newBinder);
            field.set(manager, newInterface);
            return manager;
        } catch (NoSuchFieldException |
                 IllegalAccessException |
                 PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
