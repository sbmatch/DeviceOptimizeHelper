package com.ma.enterprisemodepolicymanager.Utils.Enterprise;

import android.os.IInterface;

import com.ma.enterprisemodepolicymanager.Utils.UserManager;
import com.miui.enterprise.sdk.IEpDeletePackageObserver;
import com.miui.enterprise.sdk.IEpInstallPackageObserver;

import java.lang.reflect.Method;

public class ApplicationManager {
    private final IInterface manager;

    public ApplicationManager(IInterface manager){
        this.manager = manager;
    }

    private Method getDeletePackageMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("deletePackage", String.class,int.class, IEpDeletePackageObserver.class, int.class);
    }

    private Method getInstallPackageMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("installPackage", String.class, int.class, IEpInstallPackageObserver.class, int.class);
    }

    private Method getGrantRuntimePermissionMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("grantRuntimePermission", String.class, String.class, int.class);
    }

    private Method getSetApplicationSettingsMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("setApplicationSettings", String.class, int.class, int.class);
    }

    public void deletePackage(String packageName, int flag, IEpDeletePackageObserver observer){
        try {
            getDeletePackageMethod().invoke(manager,packageName, flag, observer, UserManager.myUserId());
        }catch (Throwable e){
            throw  new RuntimeException(e);
        }
    }

    public void installPackage(String path, IEpInstallPackageObserver observer){
        try {
            getInstallPackageMethod().invoke(manager, path, 0x004000000 ,observer,UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void setApplicationSettings(String packageName, int flags){
        try {
            getSetApplicationSettingsMethod().invoke(manager, packageName,flags, UserManager.myUserId());
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void grantRuntimePermission(String packageName, String permission){
        try {
            getGrantRuntimePermissionMethod().invoke(manager, packageName, permission);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

}
