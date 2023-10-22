package com.ma.enterprisemodepolicymanager.ViewModels;

import android.content.ContentResolver;
import android.content.IContentService;

import androidx.lifecycle.ViewModel;

import com.ma.enterprisemodepolicymanager.Model.BinderParcel;
import com.ma.enterprisemodepolicymanager.IDeviceOptService;
import com.miui.enterprise.IEnterpriseManager;


public class FragmentShareIBinder extends ViewModel {
    private static IDeviceOptService deviceOptService;
    private static IEnterpriseManager enterpriseManager;
    private static IContentService contentService;
    private static Object obj;
    public void setDeviceOptService(BinderParcel binderParcel) {
        deviceOptService = IDeviceOptService.Stub.asInterface(binderParcel.getBinder());
        System.out.println("检查代理驱动对象活性...  "+deviceOptService.asBinder().pingBinder());
    }
    public void setEnterpriseManager(BinderParcel binderParcel){
        enterpriseManager = IEnterpriseManager.Stub.asInterface(binderParcel.getBinder());
    }

    public void setContentService(BinderParcel systemContentService){
        contentService = IContentService.Stub.asInterface(systemContentService.getBinder());
    }

    public void setObj(Object obj) {
        FragmentShareIBinder.obj = obj;
    }

    public Object getObj() {
        return obj;
    }

    public IContentService getContentService() {
        return contentService;
    }

    public IEnterpriseManager getEnterpriseManager() {
        return enterpriseManager;
    }

    public IDeviceOptService getDeviceOptService() {
        return deviceOptService;
    }
}
