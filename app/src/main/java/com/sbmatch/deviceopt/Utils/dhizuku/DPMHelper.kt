package com.sbmatch.deviceopt.utils.dhizuku

import android.annotation.SuppressLint
import android.app.ActivityThread
import android.app.admin.DevicePolicyManager
import android.app.admin.IDevicePolicyManager
import android.content.ComponentName
import android.content.Context
import android.content.pm.IPackageInstaller
import android.content.pm.PackageInstaller
import android.os.Build
import com.rosan.dhizuku.api.Dhizuku
import com.rosan.dhizuku.api.Dhizuku.binderWrapper
import com.rosan.dhizuku.api.DhizukuBinderWrapper
import com.sbmatch.deviceopt.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow

private fun getAppContext(): Context{
    return ActivityThread.currentApplication()
}

fun DevicePolicyManager.isOrgProfile(receiver: ComponentName): Boolean {
    return Build.VERSION.SDK_INT >= 30 && this.isProfileOwnerApp(BuildConfig.APPLICATION_ID) && isManagedProfile(receiver) && isOrganizationOwnedDeviceWithManagedProfile
}

@SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
fun binderWrapperDevicePolicyManager(): DevicePolicyManager? {
    try {
        val context = getAppContext().createPackageContext(Dhizuku.getOwnerComponent().packageName, Context.CONTEXT_IGNORE_SECURITY)
        val manager = context.getSystemService(Context.DEVICE_POLICY_SERVICE) as DevicePolicyManager
        val field = manager.javaClass.getDeclaredField("mService")
        field.isAccessible = true
        val oldInterface = field[manager] as IDevicePolicyManager
        if (oldInterface is DhizukuBinderWrapper) return manager
        val oldBinder = oldInterface.asBinder()
        val newBinder = binderWrapper(oldBinder)
        val newInterface = IDevicePolicyManager.Stub.asInterface(newBinder)
        field[manager] = newInterface
        return manager
    } catch (_: Exception) {
        dhizukuErrorStatus.value = 1
    }
    return null
}

@SuppressLint("PrivateApi", "SoonBlockedPrivateApi")
fun binderWrapperPackageInstaller(): PackageInstaller? {
    try {
        val context = getAppContext().createPackageContext(Dhizuku.getOwnerComponent().packageName, Context.CONTEXT_IGNORE_SECURITY)
        val installer = context.packageManager.packageInstaller
        val field = installer.javaClass.getDeclaredField("mInstaller")
        field.isAccessible = true
        val oldInterface = field[installer] as IPackageInstaller
        if (oldInterface is DhizukuBinderWrapper) return installer
        val oldBinder = oldInterface.asBinder()
        val newBinder = binderWrapper(oldBinder)
        val newInterface = IPackageInstaller.Stub.asInterface(newBinder)
        field[installer] = newInterface
        return installer
    } catch (_: Exception) {
        dhizukuErrorStatus.value = 1
    }
    return null
}

val dhizukuErrorStatus = MutableStateFlow(0)

fun dhizukuPermissionGranted() =
    try {
        Dhizuku.isPermissionGranted()
    } catch(_: Exception) {
        false
    }
