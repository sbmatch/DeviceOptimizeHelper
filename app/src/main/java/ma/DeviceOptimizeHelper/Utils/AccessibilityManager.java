package ma.DeviceOptimizeHelper.Utils;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.os.IInterface;

import java.lang.reflect.Method;
import java.util.List;

public class AccessibilityManager {
    private final IInterface manager;
    private Method getEnabledAccessibilityServiceListMethod;

    public AccessibilityManager(IInterface manager){
        this.manager = manager;
    }

    private Method getEnabledAccessibilityServiceListMethod() throws NoSuchMethodException {
        if (getEnabledAccessibilityServiceListMethod == null){
            getEnabledAccessibilityServiceListMethod = manager.getClass().getMethod("getEnabledAccessibilityServiceList", int.class, int.class);
        }
        return getEnabledAccessibilityServiceListMethod;
    }

    public List<AccessibilityServiceInfo> getEnabledAccessibilityServiceList(int feedbackType, int userId){

        try {
            Method method = getEnabledAccessibilityServiceListMethod();
            return (List<AccessibilityServiceInfo>) method.invoke(manager, feedbackType , userId);
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

}
