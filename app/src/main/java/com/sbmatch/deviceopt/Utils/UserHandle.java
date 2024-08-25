package com.sbmatch.deviceopt.Utils;

public class UserHandle {
    public static int myUserId(){
        return (int) ReflectUtil.callStaticObjectMethod(android.os.UserHandle.class, "myUserId");
    }

    public static android.os.UserHandle of(int userId){
        return (android.os.UserHandle) ReflectUtil.callStaticObjectMethod(android.os.UserHandle.class, "of", userId);
    }
}
