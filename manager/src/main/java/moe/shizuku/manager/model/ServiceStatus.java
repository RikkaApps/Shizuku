package moe.shizuku.manager.model;

import moe.shizuku.ShizukuState;
import moe.shizuku.api.ShizukuClientV3;

public class ServiceStatus {

    private ShizukuState v2Status = ShizukuState.createUnknown();
    private int uid;
    private int version;

    public ServiceStatus() {
    }

    public ShizukuState getV2Status() {
        return v2Status;
    }

    public void setV2Status(ShizukuState v2Status) {
        this.v2Status = v2Status;
    }

    public boolean isV3Running() {
        return ShizukuClientV3.isAlive();
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
}
