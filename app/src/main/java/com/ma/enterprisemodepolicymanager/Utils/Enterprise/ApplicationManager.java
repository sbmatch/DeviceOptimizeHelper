package com.ma.enterprisemodepolicymanager.Utils.Enterprise;

import android.content.ComponentName;
import android.os.IInterface;

import com.ma.enterprisemodepolicymanager.Utils.UserManager;
import com.miui.enterprise.sdk.IEpDeletePackageObserver;
import com.miui.enterprise.sdk.IEpInstallPackageObserver;

import java.lang.reflect.Method;
import java.util.List;

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

    private Method getGetApplicationRestrictionMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("getApplicationRestriction", int.class);
    }

    private Method getSetApplicationRestrictionMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("setApplicationRestriction", int.class, int.class);
    }

    private Method getGetDisallowedRunningAppListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("getDisallowedRunningAppList",int.class);
    }

    private Method getSetDisallowedRunningAppListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("setDisallowedRunningAppList", List.class,int.class);
    }

    private Method getRemoveDeviceAdminMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("removeDeviceAdmin",ComponentName.class, int.class);
    }

    private Method getSetApplicationBlackListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("setApplicationBlackList", List.class, int.class);
    }

    private Method getGetApplicationBlackListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("getApplicationBlackList", int.class);
    }

    private Method getSetApplicationWhiteListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("setApplicationWhiteList", List.class, int.class);
    }

    private Method getGetApplicationWhiteListMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("getApplicationWhiteList", int.class);
    }

    private Method getEnableAccessibilityServiceMethod() throws NoSuchMethodException {
        return manager.getClass().getMethod("enableAccessibilityService", ComponentName.class, boolean.class);
    }

    private Method getSetApplicationEnabledMethod() throws NoSuchMethodException{
        return manager.getClass().getMethod("setApplicationEnabled", String.class, boolean.class , int.class);
    }

    private Method getSetNotificaitonFilterMethod() throws NoSuchMethodException{
        return manager.getClass().getMethod("setNotificaitonFilter", String.class, String.class, String.class, int.class);
    }

    private Method getKillProcessMethod() throws NoSuchMethodException{
        return manager.getClass().getMethod("killProcess", String.class, int.class);
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

    public void setApplicationEnabled(String packageName, boolean enable, int userId){
        try {
            getSetApplicationEnabledMethod().invoke(manager, packageName, enable, userId);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void setNotificaitonFilter(String pkg, String channelId, String type, boolean allow){
        try {
            getSetNotificaitonFilterMethod().invoke(manager, pkg, channelId, type, allow);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public void enableAccessibilityService(ComponentName componentName, boolean enabled){
        try {
            getEnableAccessibilityServiceMethod().invoke(manager, componentName, enabled);
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

    public int getApplicationRestriction(){
        try {
            return (int) getGetApplicationRestrictionMethod().invoke(manager, UserManager.myUserId());
        }catch (Throwable ee) {
            throw new RuntimeException(ee);
        }
    }

    public void setApplicationRestriction(int mode){
        try {
            getSetApplicationRestrictionMethod().invoke(manager, mode, UserManager.myUserId());
        }catch (Throwable ee) {
            throw new RuntimeException(ee);
        }
    }

    public List<String> getDisallowedRunningAppList(){
        try {
            return (List<String>) getGetDisallowedRunningAppListMethod().invoke(manager, UserManager.myUserId());
        }catch (Throwable ee) {
            throw new RuntimeException(ee);
        }
    }

    public void setDisallowedRunningAppList(List<String> packages){
        try {
            getSetDisallowedRunningAppListMethod().invoke(manager,packages,UserManager.myUserId());
        }catch (Throwable ee) {
            throw new RuntimeException(ee);
        }
    }

    public boolean removeDeviceAdmin(ComponentName component){
        try {
            return (boolean) getRemoveDeviceAdminMethod().invoke(manager, component, UserManager.myUserId());
        }catch (Throwable ee) {
            throw new RuntimeException(ee);
        }
    }

    public void setApplicationWhiteList(List<String> packages, int userId){
        try {
            getSetApplicationWhiteListMethod().invoke(manager, packages, userId);
        }catch (Throwable ee) {
            throw new RuntimeException(ee);
        }
    }

    public List<String> getApplicationWhiteList(int userId){
        try {
            return (List<String>) getGetApplicationWhiteListMethod().invoke(manager, userId);
        }catch (Throwable ee) {
            throw new RuntimeException(ee);
        }
    }

    public void setApplicationBlackList(List<String> packages, int userId){
        try {
            getSetApplicationBlackListMethod().invoke(manager, packages, userId);
        }catch (Throwable ee) {
            throw new RuntimeException(ee);
        }
    }

    public List<String> getApplicationBlackList(int userId){
        try {
            return (List<String>) getGetApplicationBlackListMethod().invoke(manager, userId);
        }catch (Throwable ee) {
            throw new RuntimeException(ee);
        }
    }

    public void killProcess(String packageName, int userId){
        try {
            getKillProcessMethod().invoke(manager, packageName, userId);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }
}
