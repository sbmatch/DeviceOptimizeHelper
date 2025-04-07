package com.sbmatch.deviceopt.utils;

import android.os.Handler;
import android.os.Looper;

import com.kongzue.dialogx.dialogs.PopTip;

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
            PopTip.show(builder.toString());
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
            PopTip.show( builder.toString());
        });
    }

}
