package com.sbmatch.deviceopt.Utils;

import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

public class ToastUtils {
    private final static Handler handler = new Handler(Looper.getMainLooper());
    public static void toast(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) {
            builder.append(object);
            builder.append("\n");
        }
        if (builder.length() > 0) builder.setLength(builder.length() - 1);
        handler.post(() -> {
            Toast.makeText(ContextUtil.getContext(), builder.toString(), Toast.LENGTH_SHORT).show();
        });
    }

    public static void toastLong(Object... objects) {
        StringBuilder builder = new StringBuilder();
        for (Object object : objects) {
            builder.append(object);
            builder.append("\n");
        }
        if (builder.length() > 0) builder.setLength(builder.length() - 1);
        handler.post(() -> {
            Toast.makeText(ContextUtil.getContext(), builder.toString(), Toast.LENGTH_LONG).show();
        });
    }

}
