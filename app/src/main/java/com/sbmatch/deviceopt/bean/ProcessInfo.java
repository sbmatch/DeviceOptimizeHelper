package com.sbmatch.deviceopt.bean;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProcessInfo that)) return false;

        if (getPid() != that.getPid()) return false;
        if (!getProcessName().equals(that.getProcessName())) return false;
        return getUser().equals(that.getUser());
    }

    @Override
    public int hashCode() {
        int result = getProcessName().hashCode();
        result = 31 * result + getPid();
        result = 31 * result + getUser().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "ProcessInfo{" +
                "processName='" + processName + '\'' +
                ", pid=" + pid +
                ", user='" + user + '\'' +
                '}';
    }
}