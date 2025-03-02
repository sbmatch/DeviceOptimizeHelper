package com.sbmatch.deviceopt.Interface;

public interface AbstractIUserServiceFactory {
    boolean bindUserService(OnBinderCallbackListener callbackListener);
    void unbindUserService();
}
