package com.sbmatch.deviceopt.Interface;

import android.content.Context;
import android.content.pm.SuspendDialogInfo;
import android.os.RemoteException;

import androidx.annotation.Keep;

import com.sbmatch.deviceopt.utils.SystemServiceWrapper.ActivityManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.AppRunningControlManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.PackageManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.ServiceManager;
import com.sbmatch.deviceopt.utils.SystemServiceWrapper.UserManager;

import java.util.List;

import ma.DeviceOptimizeHelper.IUserService;

public class ShizukuUserServiceImpl extends IUserService.Stub {
        Context mContext;
        UserManager um;
        AppRunningControlManager runningControlManager;
        PackageManager packageManager;
        ActivityManager activityManager;
        private static final String TAG = ShizukuUserServiceImpl.class.getSimpleName();
        @Keep
        public ShizukuUserServiceImpl(Context context) {
            this.mContext = context;
            this.um = UserManager.get(context);
            this.runningControlManager = ServiceManager.getAppRunningControlManager();
            this.packageManager = ServiceManager.getPackageManager();
            this.activityManager = ServiceManager.getActivityManager();
        }

        @Override
        public void onCreate() throws RemoteException {

        }

        @Override
        public void onDestroy() throws RemoteException {

        }

        @Override
        public void setApplicationHidden(String packageName, boolean state) throws RemoteException {
                packageManager.setApplicationHiddenSettingAsUser(packageName, state);
        }

        @Override
        public void setOrganizationName(String name) throws RemoteException {

        }

        @Override
        public void clearUserRestriction(String key) throws RemoteException {
                um.setUserRestriction(key, false);
        }

        @Override
        public void addUserRestriction(String key) throws RemoteException {
                um.setUserRestriction(key, true);
        }

        @Override
        public void setBlackListEnable(boolean isEnable){
                runningControlManager.setBlackListEnable(isEnable);
        }

        @Override
        public void setDisallowRunningList(List<String> list){
                runningControlManager.setDisallowRunningList(list);
        }

        @Override
        public List<String> getNotDisallowList(){
                return runningControlManager.getNotDisallowList();
        }

        @Override
        public boolean setBlockUninstallForUser(String packageName, boolean blockUninstall) throws RemoteException {
                return packageManager.setBlockUninstallForUser(packageName, blockUninstall);
        }

        @Override
        public String[] setPackagesSuspended(String[] packageNames, boolean suspended, SuspendDialogInfo dialogInfo) throws RemoteException {
                return packageManager.setPackagesSuspendedAsUser(packageNames, suspended, dialogInfo);
        }

        @Override
        public boolean isPackageSuspended(String packageName) throws RemoteException {
                return packageManager.isPackageSuspendedForUser(packageName);
        }

        @Override
        public void forceStopPackage(String packageName) throws RemoteException {
                activityManager.forceStopPackage(packageName);
        }

}
