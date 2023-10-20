// IEpInstallPackageObserver.aidl
package com.miui.enterprise.sdk;

import android.os.Bundle;
// Declare any non-default types here with import statements

interface IEpInstallPackageObserver {
    void onPackageInstalled(String packageName, int returnCode, String msg, inout Bundle extras);
}