package com.ma.enterprisemodepolicymanager.ViewModels;

import androidx.lifecycle.ViewModel;

import com.ma.enterprisemodepolicymanager.IDeviceOptService;
import com.miui.enterprise.IEnterpriseManager;

import com.ma.enterprisemodepolicymanager.BinderContainer;

public class FragmentShareIBinder extends ViewModel {
    private static IDeviceOptService deviceOptService;
    private static IEnterpriseManager enterpriseManager;
    public void setDeviceOptService(BinderContainer binderContainer) {
        deviceOptService = IDeviceOptService.Stub.asInterface(binderContainer.getBinder());
        System.out.println("检查代理驱动对象活性...  "+deviceOptService.asBinder().pingBinder());
    }
    public void setEnterpriseManager(BinderContainer binderContainer){
        enterpriseManager = IEnterpriseManager.Stub.asInterface(binderContainer.getBinder());
    }

    public IEnterpriseManager getEnterpriseManager() {
        return enterpriseManager;
    }

    public IDeviceOptService getDeviceOptService() {
        return deviceOptService;
    }
}
