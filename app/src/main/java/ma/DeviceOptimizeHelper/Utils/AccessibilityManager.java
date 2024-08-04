package ma.DeviceOptimizeHelper.Utils;

import android.accessibilityservice.AccessibilityServiceInfo;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

public class AccessibilityManager {
    private static Object IAccessibilityManager = ServiceManager.getService("accessibility");

    public static List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackType, int userId) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
       return (List<AccessibilityServiceInfo>) IAccessibilityManager.getClass().getMethod("getEnabledAccessibilityServiceList", int.class, int.class).invoke(IAccessibilityManager, feedbackType, userId);
    }

}
