package ma.DeviceOptimizeHelper.Model;

public class ProcessInfo {
    private String processName;
    private int pid;
    private String user;

    public ProcessInfo(String processName, int pid, String user) {
        this.processName = processName;
        this.pid = pid;
        this.user = user;
    }

    public String getProcessName() {
        return processName;
    }

    public int getPid() {
        return pid;
    }

    public String getUser() {
        return user;
    }
}
