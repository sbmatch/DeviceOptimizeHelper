package ma.DeviceOptimizeHelper.Utils;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.os.IInterface;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

public class NotificationManager {

    private IInterface manager;
    private Method updateNotificationChannelForPackageMethod;
    private static Method unlockFieldsMethod;
    private Method setNotificationsEnabledForPackageMethod;
    private Method areNotificationsEnabledForPackageMethod;
    private Method unlockAllNotificationChannelsMethod;
    private Method updateNotificationChannelGroupForPackageMethod;
    private Method isImportanceLockedMethod;
    private Method setInterruptionFilterMethod;
    private Method setNotificationPolicyMethod;
    private Method isNotificationPolicyAccessGrantedForPackageMethod;
    private Method setNotificationPolicyAccessGrantedMethod;
    public NotificationManager(IInterface manager){
        this.manager = manager;
    }

    private Method getUpdateNotificationChannelForPackageMethod() throws NoSuchMethodException {

        if (updateNotificationChannelForPackageMethod == null){
            updateNotificationChannelForPackageMethod = manager.getClass().getMethod("updateNotificationChannelForPackage", String.class , int.class , NotificationChannel.class);
        }
        return updateNotificationChannelForPackageMethod;
    }

    private Method getSetNotificationsEnabledForPackageMethod() throws NoSuchMethodException {
        if (setNotificationsEnabledForPackageMethod == null){
            setNotificationsEnabledForPackageMethod = manager.getClass().getMethod("setNotificationsEnabledForPackage", String.class, int.class, boolean.class);
        }
        return setNotificationsEnabledForPackageMethod;
    }

    private Method getAreNotificationsEnabledForPackageMethod() throws NoSuchMethodException {
        if (areNotificationsEnabledForPackageMethod == null){
            areNotificationsEnabledForPackageMethod = manager.getClass().getMethod("areNotificationsEnabledForPackage", String.class, int.class);
        }
        return areNotificationsEnabledForPackageMethod;
    }

    private Method getUpdateNotificationChannelGroupForPackageMethod() throws NoSuchMethodException {
        if (updateNotificationChannelGroupForPackageMethod == null){
            updateNotificationChannelGroupForPackageMethod = manager.getClass().getMethod("updateNotificationChannelGroupForPackage", String.class, int.class, NotificationChannelGroup.class);
        }
        return updateNotificationChannelGroupForPackageMethod;
    }

    private Method getUnlockAllNotificationChannelsMethod() throws NoSuchMethodException {
        if (unlockAllNotificationChannelsMethod == null){
            unlockAllNotificationChannelsMethod = manager.getClass().getMethod("unlockAllNotificationChannels");
        }
        return unlockAllNotificationChannelsMethod;
    }

    private Method getIsImportanceLockedMethod() throws NoSuchMethodException {
        if (isImportanceLockedMethod == null){
            isImportanceLockedMethod = manager.getClass().getMethod("isImportanceLocked", String.class, int.class);
        }
        return isImportanceLockedMethod;
    }

    private Method getSetInterruptionFilterMethod() throws NoSuchMethodException {
        if (setInterruptionFilterMethod == null){
            setInterruptionFilterMethod = manager.getClass().getMethod("setInterruptionFilter", String.class, int.class);
        }
        return setInterruptionFilterMethod;
    }

    private Method getSetNotificationPolicyMethod() throws NoSuchMethodException {
        if (setNotificationPolicyMethod == null){
            setNotificationPolicyMethod =  manager.getClass().getMethod("setNotificationPolicy", System.class, android.app.NotificationManager.Policy.class);
        }
        return setNotificationPolicyMethod;
    }

    private Method getIsNotificationPolicyAccessGrantedForPackageMethod() throws NoSuchMethodException {
        if (isNotificationPolicyAccessGrantedForPackageMethod == null){
            isNotificationPolicyAccessGrantedForPackageMethod = manager.getClass().getMethod("isNotificationPolicyAccessGrantedForPackage",String.class);
        }
        return isNotificationPolicyAccessGrantedForPackageMethod;
    }

    private Method getSetNotificationPolicyAccessGrantedMethod() throws NoSuchMethodException {
        if (setNotificationPolicyAccessGrantedMethod == null){
            setNotificationPolicyAccessGrantedMethod = manager.getClass().getMethod("setNotificationPolicyAccessGranted", String.class, boolean.class);
        }
        return setNotificationPolicyAccessGrantedMethod;
    }

    public List<NotificationChannel> getNotificationChannelsForPackage(String pkg, boolean includeDeleted){

        try {

            Object parceledListSlice = manager.getClass().getMethod("getNotificationChannelsForPackage",String.class , int.class, boolean.class).invoke(manager ,pkg, ServiceManager.getPackageManager().getUidForPackageName(pkg),includeDeleted);

            // 通过反射调用 getList 方法
            assert parceledListSlice != null;
            Method getListMethod = parceledListSlice.getClass().getDeclaredMethod("getList");
            getListMethod.setAccessible(true);

            return (List<NotificationChannel>) getListMethod.invoke(parceledListSlice);

        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void updateNotificationChannelForPackage(String pkg, int uid, NotificationChannel channel){
        try {
            Method updateNotificationChannelForPackage = getUpdateNotificationChannelForPackageMethod();
            updateNotificationChannelForPackage.invoke(manager, pkg, uid, channel);
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setNotificationsEnabledForPackage(String pkg, int uid, boolean enabled){
        try {
            getSetNotificationsEnabledForPackageMethod().invoke(manager, pkg, uid, enabled);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public boolean areNotificationsEnabledForPackage(String pkg, int uid){
        try {
            return (boolean) getAreNotificationsEnabledForPackageMethod().invoke(manager, pkg, uid);
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public void setNotificationPolicy(String pkg, android.app.NotificationManager.Policy policy){
        try {
            getSetNotificationPolicyMethod().invoke(manager, pkg, policy);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public boolean isNotificationPolicyAccessGrantedForPackage(String pkg){
        try {
            return (boolean) getIsNotificationPolicyAccessGrantedForPackageMethod().invoke(manager, pkg);
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public void setNotificationPolicyAccessGranted(String pkg, boolean granted){
        try {
            getSetNotificationPolicyAccessGrantedMethod().invoke(manager, pkg, granted);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void setInterruptionFilter(String pkg, int interruptionFilter){
        try {
            getSetInterruptionFilterMethod().invoke(manager, pkg, interruptionFilter);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void unlockAllNotificationChannels(){
        try {
           getUnlockAllNotificationChannelsMethod().invoke(manager);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public void updateNotificationChannelGroupForPackage(String pkg, int uid, NotificationChannelGroup group){
        try {
            getUpdateNotificationChannelGroupForPackageMethod().invoke(manager, pkg, uid, group);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public boolean isImportanceLocked(String pkg, int uid){
        try {
           return (boolean) getIsImportanceLockedMethod().invoke(manager, pkg, uid);
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void unlockFields(NotificationChannel channel ,int field){
        try {
            if (unlockFieldsMethod == null){
                unlockFieldsMethod = channel.getClass().getMethod("unlockFields", int.class);
            }
            unlockFieldsMethod.invoke(channel, field);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    public static void setBlockable(NotificationChannel channel ,boolean blockable){
        try {
            channel.getClass().getMethod("setBlockable",boolean.class).invoke(channel, blockable);
        } catch (IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            e.printStackTrace();
        }
    }

    public static boolean isBlockable(NotificationChannel channel){
        try {
            return (boolean) channel.getClass().getMethod("isBlockable").invoke(channel);
        } catch (IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public static int getUserLockedFields(NotificationChannel channel){
        try {
            return (int) channel.getClass().getMethod("getUserLockedFields").invoke(channel);
        } catch (IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

}
