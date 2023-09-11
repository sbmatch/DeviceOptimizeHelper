package android.app.admin;

import android.app.admin.SystemUpdatePolicy;
import android.content.ComponentName;

interface IDevicePolicyManager{
    void setSystemUpdatePolicy(in ComponentName who, in SystemUpdatePolicy policy);
    SystemUpdatePolicy getSystemUpdatePolicy();
}