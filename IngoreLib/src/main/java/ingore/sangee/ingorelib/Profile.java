package ingore.sangee.ingorelib;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sangee's Lap on 5/1/2018.
 */

public class Profile {
    @SerializedName("DeviceId")
    @Expose
    private String deviceId;
    @SerializedName("Firstname")
    @Expose
    private String firstname;
    @SerializedName("Lastname")
    @Expose
    private String lastname;
    @SerializedName("EMail")
    @Expose
    private String eMail;

    public Profile(String firstname, String lastname, String eMail) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.eMail = eMail;
    }

    public Profile(String firstname, String lastname) {
        this.firstname = firstname;
        this.lastname = lastname;
        this.eMail = "";
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getEMail() {
        return eMail;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }
}
