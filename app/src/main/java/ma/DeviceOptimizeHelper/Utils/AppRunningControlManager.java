package ma.DeviceOptimizeHelper.Utils;

import android.content.Intent;

import java.util.List;

public class AppRunningControlManager {
    private Object appRunningControlManager;
    public AppRunningControlManager(Object manager){
        appRunningControlManager = manager;
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
