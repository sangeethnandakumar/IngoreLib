package ingore.sangee.ingorelib;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Sangee's Lap on 6/10/2018.
 */

public class Message {
    @SerializedName("MsgID")
    @Expose
    public String msgID;
    @SerializedName("AppID")
    @Expose
    public String appID;
    @SerializedName("Title")
    @Expose
    public String title;
    @SerializedName("Message")
    @Expose
    public String message;
    @SerializedName("Expiary")
    @Expose
    public String expiary;
    @SerializedName("Flag")
    @Expose
    public String flag;
}
