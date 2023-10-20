package ma.DeviceOptimizeHelper.Utils;

import android.os.IInterface;

import java.lang.reflect.Method;

public class AppOpsManager {
    private IInterface manager;
    private Method checkOperationMethod;
    public AppOpsManager(IInterface manager){
        this.manager = manager;
    }

    private Method getCheckOperationMethod() throws NoSuchMethodException {
        if (checkOperationMethod == null){
            checkOperationMethod = manager.getClass().getMethod("checkOperation", int.class, int.class, String.class);
        }
        return checkOperationMethod;
    }


    public int checkOperation(int op, int uid, String packageName){
        try {
            Method checkOperation = getCheckOperationMethod();
            return (int) checkOperation.invoke(manager, op, uid, packageName);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

    public static int permissionToOpCode(String permission){
        try {
            Class<?> cStub =  Class.forName("android.app.AppOpsManager");
            Method permissionToOpCodeMethod = cStub.getMethod("permissionToOpCode", String.class);
            return (int) permissionToOpCodeMethod.invoke(ServiceManager.getService("appops"), permission);
        }catch (Throwable e){
            throw new RuntimeException(e);
        }
    }

}
