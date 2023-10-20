package android.app.admin;

import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;
import android.os.RemoteCallback;
import android.os.Bundle;

interface IDevicePolicyManager{
    //void setSystemUpdatePolicy(in ComponentName who, in SystemUpdatePolicy policy);
    SystemUpdatePolicy getSystemUpdatePolicy();
    void setUserRestriction(in ComponentName who, String key, boolean enable, boolean parent);
    Bundle getApplicationRestrictions(in ComponentName who, String callerPackage, String packageName);
    void setApplicationRestrictions(in ComponentName who, String callerPackage, String packageName, in Bundle settings);
    int getPermissionGrantState(in ComponentName admin, String callerPackage, String packageName, String permission);
    //void setPermissionGrantState(in ComponentName admin, String callerPackage, String packageName, String permission, int grantState, in RemoteCallback resultReceiver);

}