package ma.DeviceOptimizeHelper.Utils;

import android.annotation.SuppressLint;

public class OsUtils {
    private static final SystemPropertiesUtils systemProperties = new SystemPropertiesUtils();
    public static boolean isMiui(){
        try {
            return systemProperties.getBoolean("persist.sys.miui_optimization", !"1".equals(systemProperties.get("ro.miui.cts")));
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }
}
