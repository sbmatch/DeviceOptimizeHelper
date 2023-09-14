package ma.DeviceOptimizeHelper.Utils;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.app.admin.IDevicePolicyManager;
import android.content.Context;
import android.os.IBinder;

import com.rosan.dhizuku.api.Dhizuku;
import com.rosan.dhizuku.api.DhizukuBinderWrapper;

import java.lang.reflect.Field;

public class DhizukuUtils {
    @SuppressLint("SoonBlockedPrivateApi")
    public static DevicePolicyManager binderWrapperDevicePolicyManager(Context context) {
        try {
            DevicePolicyManager manager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
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
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
