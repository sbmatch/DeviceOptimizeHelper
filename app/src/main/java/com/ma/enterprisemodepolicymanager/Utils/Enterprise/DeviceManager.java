package com.ma.enterprisemodepolicymanager.Utils.Enterprise;

import android.os.IInterface;

import com.ma.enterprisemodepolicymanager.Utils.UserManager;

import java.lang.reflect.Method;
import java.util.List;

public class DeviceManager {
    private final IInterface manager;
    public DeviceManager(IInterface manager){
        this.manager = manager;
    }

    private Method getEnableUsbDebugMethod() throws NoSuchMethodException {
       return manager.getClass().getMethod("enableUsbDebug", boolean.class);
    }

    private Method getRecoveryFactoryMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("recoveryFactory", boolean.class);
    }

    private Method getGetWifiConnRestrictionMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("getWifiConnRestriction",int.class);
    }

    private Method getSetWifiConnRestrictionMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("setWifiConnRestriction", int.class, int.class);
    }

    private Method getGetWifiApSsidBlackListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("getWifiApSsidBlackList", int.class);
    }

    private Method getSetWifiApSsidBlackListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("setWifiApSsidBlackList", List.class, int.class);
    }

    private Method getGetWifiApSsidWhiteListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("getWifiApSsidWhiteList", int.class);
    }

    private Method getSetWifiApSsidWhiteListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("setWifiApSsidWhiteList", List.class, int.class);
    }

    private Method getGetUrlBlackListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("getUrlBlackList", int.class);
    }

    private Method getSetUrlBlackListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("setUrlBlackList", List.class, int.class);
    }

    public void enableUsbDebug(boolean enable){
        try {
            getEnableUsbDebugMethod().invoke(manager,enable);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public List<String> getUrlBlackList(int userId){
        try {
            return (List<String>) getGetUrlBlackListMethod().invoke(manager, userId);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void setUrlBlackList(List<String> urls, int userId) {
        try {
            getSetUrlBlackListMethod().invoke(manager, urls, userId);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public List<String> getWifiApBssidBlackList(){
        try {
            return (List<String>) getGetWifiApSsidBlackListMethod().invoke(manager, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public List<String> getWifiApSsidWhiteList(){
        try {
            return (List<String>) getGetWifiApSsidWhiteListMethod().invoke(manager, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void setWifiApSsidWhiteList(List<String> ssids){
        try {
            getSetWifiApSsidWhiteListMethod().invoke(manager, ssids, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void recoveryFactory(boolean formatSdcard){
        try {
            getRecoveryFactoryMethod().invoke(manager, formatSdcard);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public int getWifiConnRestriction(){
        try {
            return (int) getGetWifiConnRestrictionMethod().invoke(manager, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void setWifiConnRestriction(int mode){
        try {
            getSetWifiConnRestrictionMethod().invoke(manager, mode, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

}
