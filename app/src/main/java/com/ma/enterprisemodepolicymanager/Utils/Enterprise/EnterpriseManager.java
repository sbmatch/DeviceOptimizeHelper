package com.ma.enterprisemodepolicymanager.Utils.Enterprise;

import android.os.IBinder;
import android.os.IInterface;

import java.lang.reflect.Method;

public class EnterpriseManager {

    public static final String APN_MANAGER = "apn_manager";
    public static final String APPLICATION_MANAGER = "application_manager";
    public static final String DEVICE_MANAGER = "device_manager";
    public static final String PHONE_MANAGER = "phone_manager";
    public static final String RESTRICTIONS_MANAGER = "restrictions_manager";
    public static final String SERVICE_NAME = "EnterpriseManager";

    private IInterface manager;
    private static Method getServiceMethod;
    private static DeviceManager deviceManager;
    private static ApplicationManager applicationManager;
    public EnterpriseManager(IInterface manager){
        this.manager = manager;
    }

    private Method getGetServiceMethod() throws NoSuchMethodException {
        if (getServiceMethod == null) getServiceMethod = manager.getClass().getMethod("getService", String.class);
        return getServiceMethod;
    }

    private IInterface getService(String serviceName, String type){
        try {
            IBinder binder = (IBinder) getGetServiceMethod().invoke(manager,serviceName);
            Method asInterfaceMethod = Class.forName(type + "$Stub").getMethod("asInterface", IBinder.class);
            return (IInterface) asInterfaceMethod.invoke(null, binder);
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public DeviceManager getDeviceManager(){
       try {
           if (deviceManager == null) deviceManager = new DeviceManager(getService(DEVICE_MANAGER,"com.miui.enterprise.IDeviceManager"));
           return deviceManager;
       }catch (Throwable e){
           throw new RuntimeException(e);
       }
    }

    public ApplicationManager getApplicationManager(){
       try {
           if (applicationManager == null) applicationManager = new ApplicationManager(getService(APPLICATION_MANAGER,"com.miui.enterprise.IApplicationManager"));
           return applicationManager;
       }catch (Throwable e){
           throw new RuntimeException(e);
       }
    }

}
