package com.sbmatch.deviceopt.utils;

public class UserHandle {
    public static int myUserId(){
        return (int) ReflectUtils.callStaticObjectMethod(android.os.UserHandle.class, "myUserId");
    }

    public static android.os.UserHandle of(int userId){
        return (android.os.UserHandle) ReflectUtils.callStaticObjectMethod(android.os.UserHandle.class, "of", userId);
    }
}
