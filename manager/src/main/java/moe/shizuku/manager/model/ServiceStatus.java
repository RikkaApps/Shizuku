package moe.shizuku.manager.model;

import moe.shizuku.api.ShizukuService;

public class ServiceStatus {

    private int uid;
    private int version;
    private String secontext;

    public ServiceStatus() {
    }

    public boolean isRunning() {
        return ShizukuService.pingBinder();
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public String getSEContext() {
        return secontext;
    }

    public void setSEContext(String secontext) {
        this.secontext = secontext;
    }
}
