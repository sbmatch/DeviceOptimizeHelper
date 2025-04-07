package com.sbmatch.deviceopt.Interface;

import android.os.IBinder;
import android.os.IInterface;
import android.os.RemoteException;

public interface OnBinderCallbackListener {
    void onUserServiceReady(IBinder service, String ImplClass) throws RemoteException;
    void onUserServiceDisconnected(String ImplClass) throws RemoteException;
}
