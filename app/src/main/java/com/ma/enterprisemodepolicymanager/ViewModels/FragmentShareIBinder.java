package com.ma.enterprisemodepolicymanager.ViewModels;

import androidx.lifecycle.ViewModel;

import com.ma.enterprisemodepolicymanager.Model.BinderParcel;
import com.ma.enterprisemodepolicymanager.IDeviceOptService;
import com.miui.enterprise.IEnterpriseManager;


public class FragmentShareIBinder extends ViewModel {
    private static IDeviceOptService deviceOptService;
    private static IEnterpriseManager enterpriseManager;
    public void setDeviceOptService(BinderParcel binderParcel) {
        deviceOptService = IDeviceOptService.Stub.asInterface(binderParcel.getBinder());
        System.out.println("检查代理驱动对象活性...  "+deviceOptService.asBinder().pingBinder());
    }
    public void setEnterpriseManager(BinderParcel binderParcel){
        enterpriseManager = IEnterpriseManager.Stub.asInterface(binderParcel.getBinder());
    }

    public IEnterpriseManager getEnterpriseManager() {
        return enterpriseManager;
    }

    public IDeviceOptService getDeviceOptService() {
        return deviceOptService;
    }
}
