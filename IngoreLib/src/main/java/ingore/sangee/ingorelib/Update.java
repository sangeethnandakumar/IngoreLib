package ingore.sangee.ingorelib;

/**
 * Created by Sangee's Lap on 3/25/2018.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Update {

    @SerializedName("AppID")
    @Expose
    public String appID;
    @SerializedName("Package")
    @Expose
    public String _package;
    @SerializedName("LatestVersion")
    @Expose
    public String latestVersion;
    @SerializedName("ChangeLog")
    @Expose
    public String changeLog;
    @SerializedName("CriticalUpdate")
    @Expose
    public String criticalUpdate;
    @SerializedName("UpdateLink")
    @Expose
    public String updateLink;

}