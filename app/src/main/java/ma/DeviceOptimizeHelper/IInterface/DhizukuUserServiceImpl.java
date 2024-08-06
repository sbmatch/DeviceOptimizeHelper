package ma.DeviceOptimizeHelper.IInterface;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Looper;
import android.os.RemoteException;

import androidx.annotation.Keep;

import com.rosan.dhizuku.shared.DhizukuVariables;

import ma.DeviceOptimizeHelper.IUserService;
import ma.DeviceOptimizeHelper.Utils.ContextUtils;

public class DhizukuUserServiceImpl extends IUserService.Stub {
    DevicePolicyManager devicePolicyManager;
    @Keep
    public DhizukuUserServiceImpl(Context context) {
        devicePolicyManager = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDestroy() {
    }

    @Override
    @SuppressLint("DiscouragedPrivateApi")
    public void setApplicationHidden(String packageName, boolean state) throws RemoteException {
        devicePolicyManager.setApplicationHidden(DhizukuVariables.COMPONENT_NAME, packageName, state);
    }

    @Override
    public void setOrganizationName(String name) {
        devicePolicyManager.setOrganizationName(DhizukuVariables.COMPONENT_NAME, name);
    }
    /**
     * @param who
     * @param key
     * @throws RemoteException
     */
    @Override
    public void clearUserRestriction(ComponentName who, String key) throws RemoteException {
        devicePolicyManager.clearUserRestriction(who, key);
    }

    /**
     * @param who
     * @param key
     * @throws RemoteException
     */
    @Override
    public void addUserRestriction(ComponentName who, String key) throws RemoteException {
        devicePolicyManager.addUserRestriction(who, key);
    }
}