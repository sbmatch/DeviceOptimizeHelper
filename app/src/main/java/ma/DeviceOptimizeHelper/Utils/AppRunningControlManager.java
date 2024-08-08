package ma.DeviceOptimizeHelper.Utils;

import android.content.Intent;
import android.os.IInterface;

import java.util.List;

public class AppRunningControlManager {
    private IInterface appRunningControlManager;
    public AppRunningControlManager(Object manager){
        appRunningControlManager = (IInterface) ReflectUtil.getObjectField(manager,"mService");
    }

    public void setBlackListEnable(boolean isEnable){
        ReflectUtil.callObjectMethod2(appRunningControlManager, "setBlackListEnable", isEnable);
    }

    public void setDisallowRunningList(List<String> list, Intent intent){
        ReflectUtil.callObjectMethod2(appRunningControlManager, "setDisallowRunningList", list, intent);
    }

    public List<String> getNotDisallowList(){
        return (List<String>) ReflectUtil.callObjectMethod2(appRunningControlManager, "getNotDisallowList");
    }
}
