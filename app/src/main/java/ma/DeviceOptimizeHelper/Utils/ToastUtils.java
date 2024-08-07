package ma.DeviceOptimizeHelper.Utils;

import android.widget.Toast;

public class ToastUtils {
    public static void toast(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) {
            builder.append(object);
            builder.append("\n");
        }
        if (builder.length() > 0) builder.setLength(builder.length() - 1);
        Toast.makeText(ContextUtils.currentApplication().getApplicationContext(), builder.toString(), Toast.LENGTH_SHORT).show();
    }

}
