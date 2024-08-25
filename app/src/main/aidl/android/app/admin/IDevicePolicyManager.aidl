package android.app.admin;

import android.app.admin.FactoryResetProtectionPolicy;
import android.content.ComponentName;

interface IDevicePolicyManager{

    void setUserRestriction(in ComponentName who, in String key, boolean enable, boolean parent);
    Bundle getUserRestrictions(in ComponentName who, boolean parent);

    void setKeepUninstalledPackages(in ComponentName admin, in String callerPackage, in List<String> packageList);
    List<String> getKeepUninstalledPackages(in ComponentName admin, in String callerPackage);

    CharSequence getOrganizationName(in ComponentName admin, String callerPackageName);

    void setUninstallBlocked(in ComponentName admin, in String callerPackage, in String packageName, boolean uninstallBlocked);
    boolean isUninstallBlocked(in ComponentName admin, in String packageName);

    boolean setApplicationHidden(in ComponentName admin, in String callerPackage, in String packageName, boolean hidden, boolean parent);
    boolean isApplicationHidden(in ComponentName admin, in String callerPackage, in String packageName, boolean parent);

    void setDeviceOwnerLockScreenInfo(in ComponentName who, CharSequence deviceOwnerInfo);
    CharSequence getDeviceOwnerLockScreenInfo();

    String[] setPackagesSuspended(in ComponentName admin, in String callerPackage, in String[] packageNames, boolean suspended);
    boolean isPackageSuspended(in ComponentName admin, in String callerPackage, String packageName);

    void setFactoryResetProtectionPolicy(in ComponentName who, in FactoryResetProtectionPolicy policy);
    FactoryResetProtectionPolicy getFactoryResetProtectionPolicy(in ComponentName who);
    boolean isFactoryResetProtectionPolicySupported();
}