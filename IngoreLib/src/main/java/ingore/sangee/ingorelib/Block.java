package ingore.sangee.ingorelib;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sangee's Lap on 5/1/2018.
 */

public class Block {
    @SerializedName("DeviceId")
    @Expose
    private String deviceId;
    @SerializedName("AppID")
    @Expose
    private String appID;
    @SerializedName("Status")
    @Expose
    private String status;
    @SerializedName("Reason")
    @Expose
    private String reason;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getAppID() {
        return appID;
    }

    public void setAppID(String appID) {
        this.appID = appID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

}
