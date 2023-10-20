package com.ma.enterprisemodepolicymanager.Utils;

import android.os.IInterface;

public class DevicePolicyManager {
    private IInterface manager;
    public DevicePolicyManager(IInterface devicePolicy){
        this.manager = devicePolicy;
    }
}
