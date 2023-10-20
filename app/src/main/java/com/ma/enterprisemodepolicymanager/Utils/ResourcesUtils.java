package com.ma.enterprisemodepolicymanager.Utils;

import android.content.res.Resources;


import com.ma.enterprisemodepolicymanager.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;



public class ResourcesUtils {
    public static int getResIdReflect(String key) {
        //获取R.string.class对象
        try {
            Class<?> clazz = R.string.class;
            //获取key对应的字段
            Field field = clazz.getField(key);
            //获取字段的值
            return field.getInt(null);
        } catch (Resources.NotFoundException | NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
            //抛出异常
        }
        //返回0
        return 0;
    }

    public static String getResNameStringValueReflect(Resources res,String key) {
        try {
            Class<?> clazz = Class.forName("android.content.res.Resources");
            Method getStringMethod = clazz.getMethod("getString", int.class);
            return (String) getStringMethod.invoke(res, getResIdReflect(key));
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}
