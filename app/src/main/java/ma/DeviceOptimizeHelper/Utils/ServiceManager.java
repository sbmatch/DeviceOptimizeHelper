package ma.DeviceOptimizeHelper.Utils;

import android.annotation.SuppressLint;
import android.os.IBinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ServiceManager {

    public static IBinder getSystemService(String name){
        try {
            @SuppressLint({"PrivateApi", "DiscouragedPrivateApi"})
            Method getServiceMethod = Class.forName("android.os.ServiceManager").getDeclaredMethod("getService", String.class);
            getServiceMethod.setAccessible(true);
            return (IBinder) getServiceMethod.invoke(null, name);
        } catch (NullPointerException | IllegalAccessException | InvocationTargetException | ClassNotFoundException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
