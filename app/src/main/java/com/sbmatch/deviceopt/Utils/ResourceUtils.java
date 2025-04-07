package com.sbmatch.deviceopt.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.Singleton;

import com.sbmatch.deviceopt.AppGlobals;

public class ResourceUtils extends Singleton<ResourceUtils> {
    @Override
    protected ResourceUtils create() {
        return new ResourceUtils().get();
    }

    public static String getString(int resId){
        try {
            return AppGlobals.getResources().getString(resId);
        }catch (Resources.NotFoundException e){
            return null;
        }
    }

    public static String getString(Context context, int resId){
        try {
            return context.getResources().getString(resId);
        }catch (Resources.NotFoundException e){
            return null;
        }
    }
}
