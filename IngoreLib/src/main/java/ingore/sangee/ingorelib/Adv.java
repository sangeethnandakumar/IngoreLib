package ingore.sangee.ingorelib;

/**
 * Created by Sangee's Lap on 5/24/2018.
 */

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Adv {

    @SerializedName("AdsID")
    @Expose
    public String adsID;
    @SerializedName("AdTheme")
    @Expose
    public String adTheme;
    @SerializedName("AdText")
    @Expose
    public String adText;
    @SerializedName("AdURL")
    @Expose
    public String adURL;
    @SerializedName("AdRedirect")
    @Expose
    public String adRedirect;

}