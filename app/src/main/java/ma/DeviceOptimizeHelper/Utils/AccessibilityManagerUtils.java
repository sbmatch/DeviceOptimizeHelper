package ma.DeviceOptimizeHelper.Utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.os.IBinder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class AccessibilityManagerUtils {
    private static Object IAccessibilityManager(){
        try {
            @SuppressLint("PrivateApi")
            Class<?> cStub = Class.forName("android.view.accessibility.IAccessibilityManager$Stub");
            // 获取 asInterface 方法，用于创建接口的实例
            Method asInterface = cStub.getMethod("asInterface", IBinder.class);
            // 通过 ServiceManager 获取实例
            return asInterface.invoke(null, ServiceManager.getSystemService(Context.ACCESSIBILITY_SERVICE));
        } catch (Exception e2) {
            // 捕获异常并抛出运行时异常
            throw new RuntimeException(e2);
        }
    }

    public static List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackType, int userId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
       return (List<AccessibilityServiceInfo>) IAccessibilityManager().getClass().getMethod("getEnabledAccessibilityServiceList", int.class, int.class).invoke(IAccessibilityManager(), feedbackType, userId);
    }

}
