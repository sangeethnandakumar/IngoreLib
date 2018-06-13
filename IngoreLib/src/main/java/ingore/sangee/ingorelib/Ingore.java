package ingore.sangee.ingorelib;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

import bullyfox.sangeeth.testube.component.DataRack;
import bullyfox.sangeeth.testube.managers.AppSettings;
import bullyfox.sangeeth.testube.managers.SuperDatabase;
import bullyfox.sangeeth.testube.network.WebServer;
import github.nisrulz.easydeviceinfo.base.EasyAppMod;
import github.nisrulz.easydeviceinfo.base.EasyBatteryMod;
import github.nisrulz.easydeviceinfo.base.EasyConfigMod;
import github.nisrulz.easydeviceinfo.base.EasyDeviceMod;
import github.nisrulz.easydeviceinfo.base.EasyDisplayMod;
import github.nisrulz.easydeviceinfo.base.EasyFingerprintMod;
import github.nisrulz.easydeviceinfo.base.EasyMemoryMod;
import github.nisrulz.easydeviceinfo.base.EasyNetworkMod;
import github.nisrulz.easydeviceinfo.base.EasySimMod;
import github.nisrulz.easydeviceinfo.base.NetworkType;

/**
 * Created by Sangee's Lap on 3/10/2018.
 */

public class Ingore
{
    Context context;
    Activity activity;
    String APP_ID,DEVICE_ID,BASE_URL;
    List<DataRack> rack;
    AppSettings settings;
    int count=0;
    Profile myprofile;
    String whatsappnumber;
    private SharedPreferences prefs;
    private SharedPreferences.Editor editor;
    private int totalCount;
    private WebServer generalserver;
    private String SESSION;

    public Ingore(ReportMode mode,Context context, Activity activity) {
        generalserver=new WebServer(context);

        if (mode==ReportMode.DEBUG_MODE)
        {
            BASE_URL="http://192.168.43.207/ingore.sangeethnandakumar.com/ingore.v2/";
        }
        else if (mode==ReportMode.RELEASE_MODE)
        {
            BASE_URL="http://ingore.sangeethnandakumar.com/ingore.v2/";
        }

        this.context = context;
        this.activity = activity;
        settings=new AppSettings(context);
        rack=new ArrayList<>();

        try
        {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            //GET AppID
            APP_ID = bundle.getString("AppID");
            //GET DeviceID
            DEVICE_ID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            rack.add(new DataRack("DeviceId",DEVICE_ID));
            rack.add(new DataRack("AppId",APP_ID));
            isFingerprint();
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Ingore(ReportMode mode,Context context, Activity activity,String whatsappnumber) {
        generalserver=new WebServer(context);

        if (mode==ReportMode.DEBUG_MODE)
        {
            BASE_URL="http://192.168.43.207/ingore.sangeethnandakumar.com/ingore.v2/";
        }
        else if (mode==ReportMode.RELEASE_MODE)
        {
            BASE_URL="http://ingore.sangeethnandakumar.com/ingore.v2/";
        }

        this.whatsappnumber=whatsappnumber;
        this.context = context;
        this.activity = activity;
        settings=new AppSettings(context);
        rack=new ArrayList<>();

        try
        {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            //GET AppID
            APP_ID = bundle.getString("AppID");
            //GET DeviceID
            DEVICE_ID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            rack.add(new DataRack("DeviceId",DEVICE_ID));
            rack.add(new DataRack("AppId",APP_ID));
            isFingerprint();
        }
        catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }




    // INGORE CORE CALLS
    public void initIngore(Profile profile)
    {
        registerCount();
        myprofile=profile;
        myprofile.setDeviceId(DEVICE_ID);
        uploadProfile(profile);
        checkBlocked();
    }

    public void initIngore()
    {
        registerCount();
        accureProfile();
        checkBlocked();
    }

    public void registerEvent(String name,String value)
    {
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a").format(Calendar.getInstance().getTime());
        List<DataRack> eventrack=new ArrayList<>();
        eventrack.add(new DataRack("DeviceID",DEVICE_ID));
        eventrack.add(new DataRack("AppID",APP_ID));
        eventrack.add(new DataRack("EventName",name));
        eventrack.add(new DataRack("EventValue",value));
        eventrack.add(new DataRack("Flags","not_set"));
        eventrack.add(new DataRack("Date",date));
        WebServer server=new WebServer(context);
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s) {
            }

            @Override
            public void onServerRevoked() {

            }
        });
        server.connectWithPOST(activity,BASE_URL+"Events/registerEvent.php",eventrack);
    }

    public int getAppSessionCount()
    {
        return count;
    }

    public Profile getUserProfile()
    {
        return myprofile;
    }

    public void getMessages(OnMessagesListner reciver)
    {
        final OnMessagesListner myreciver=reciver;

        final List<Message> todays=new ArrayList<>();
        final List<Message> future=new ArrayList<>();

        WebServer server=new WebServer(activity);
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s)
            {
                Gson gson=new Gson();
                List<Message> messages = gson.fromJson(s, new TypeToken<List<Message>>(){}.getType());
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

                //Check if succesfully parsed
                if (messages!=null)
                {
                    for (int i=0;i<messages.size();i++)
                    {
                        if (messages.get(i).expiary.equals(date))
                        {
                            todays.add(messages.get(i));
                        }
                        else
                        {
                            future.add(messages.get(i));
                        }
                    }
                }
                else
                {
                    messages=new ArrayList<>();
                }

                myreciver.todaysMessages(todays);
                myreciver.futureMessages(future);
            }

            @Override
            public void onServerRevoked()
            {
                myreciver.todaysMessages(todays);
                myreciver.futureMessages(future);
            }
        });
        List<DataRack> racks=new ArrayList<>();
        racks.add(new DataRack("AppID",APP_ID));
        server.connectWithPOST(activity,BASE_URL + "Messages/getMessages.php",racks);
    }

    public interface OnMessagesListner
    {
        void todaysMessages(List<Message> messages);
        void futureMessages(List<Message> messages);
    }

    public void logStep(String session, String step)
    {
        SESSION=session;
        EasyAppMod app = new EasyAppMod(context);
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a").format(Calendar.getInstance().getTime());
        ingoreDB().sqlInject("REPLACE INTO `usecase` VALUES('"+session+"','"+DEVICE_ID+"','"+APP_ID+"','"+app.getAppVersion()+"','"+date+"','"+step+"')");
    }

    public void stopIngore()
    {
        EasyAppMod app = new EasyAppMod(context);
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a").format(Calendar.getInstance().getTime());
        ingoreDB().sqlInject("REPLACE INTO `usecase` VALUES('"+SESSION+"','"+DEVICE_ID+"','"+APP_ID+"','"+app.getAppVersion()+"','"+date+"','App Stopped')");
        generalserver.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s) {
                ingoreDB().sqlInject("DELETE FROM `usecase`");
            }

            @Override
            public void onServerRevoked() {

            }
        });
        List<DataRack> racks=new ArrayList<>();
        racks.add(new DataRack("Log",ingoreDB().sqlEjectJSON("SELECT * FROM `usecase`")));
        generalserver.connectWithPOST(activity,BASE_URL + "Usecases/registerUsecase.php",racks);
    }




    //UI Invokes
    public void invokeUpdatePrompt()
    {
        WebServer server=new WebServer(context);
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s)
            {
                try
                {
                    Gson gson=new Gson();
                    List<Update> updates = gson.fromJson(s, new TypeToken<List<Update>>(){}.getType());

                    //CHECK IF A NEWER UPDATE
                    EasyAppMod easyAppMod = new EasyAppMod(context);
                    double currentVersion=Double.parseDouble(easyAppMod.getAppVersionCode());
                    double foundVersion=Double.parseDouble(updates.get(0).latestVersion);
                    if (currentVersion<foundVersion)
                    {
                        if (updates.get(0).criticalUpdate.equals("true"))
                        {
                            updatePrompt(updates.get(0).changeLog,updates.get(0).updateLink,true);
                        }
                        else
                        {
                            updatePrompt(updates.get(0).changeLog,updates.get(0).updateLink,false);
                        }
                    }
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }

            @Override
            public void onServerRevoked() {

            }
        });
        List<DataRack> racks=new ArrayList<>();
        racks.add(new DataRack("AppId",APP_ID));
        server.connectWithPOST(activity, BASE_URL+"Updates/getUpdates.php",racks);
    }

    public void invokeFuturePrompt()
    {
        futurePrompt();
    }

    public void invokeBugPrompt(String title, String onClass, String onFunction, Exception exception)
    {
        String stackTrace = Log.getStackTraceString(exception);
        bugPrompt(title,onClass, onFunction,stackTrace);
    }

    public void invokeWhatsAppPrompt()
    {
        if (whatsappnumber==null)
        {
            Toast.makeText(context, "This application does not support WhatsApp chat feature", Toast.LENGTH_SHORT).show();
        }
        else
        {
            whatsappPrompt(whatsappnumber);
        }
    }

    public void invokeRateOnPlaystore(String request)
    {
        rateAndReview(request);
    }

    public void invokeRateOnPlaystoreOnPrimeIntervals(String request)
    {
        if (count>4)
        {
            if (isPrime(count))
            {
                invokeRateOnPlaystore(request);
            }
        }
    }

    public void invokeProfilePrompt()
    {
        profilePrompt();
    }

    public void invokeAdsPrompt()
    {
        WebServer server=new WebServer(context);
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s) {
                Adv ads =new Gson().fromJson(s,Adv.class);
                adPrompt(ads.adText, ads.adTheme,ads.adURL,ads.adRedirect);
            }

            @Override
            public void onServerRevoked() {

            }
        });
        server.connectWithGET(BASE_URL + "Ads/getAds.php");
    }

    public String generateSession()
    {
        final String ALLOWED_CHARACTERS ="ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final Random random=new Random();
        final StringBuilder sb=new StringBuilder(4);
        for(int i=0;i<4;++i)
            sb.append(ALLOWED_CHARACTERS.charAt(random.nextInt(ALLOWED_CHARACTERS.length())));
        return sb.toString();
    }





    //Configuration tweeks
    public void tweekConfig_resetAppSessionCount()
    {
        count=0;
    }

    public void tweekConfig_dontShowRateAndReview(boolean key)
    {
        if (key)
        {
            settings.saveSettings("dontShowRateAndReview","true");
        }
        else
        {
            settings.saveSettings("dontShowRateAndReview","false");
        }
    }





    //Private functions
    private boolean isPrime(int num)
    {
        int temp;
        boolean isPrime=true;
        for(int i=2;i<=num/2;i++)
        {
            temp=num%i;
            if(temp==0)
            {
                isPrime=false;
                break;
            }
        }
        if(isPrime)
            return true;
        else
            return false;
    }

    private void accureProfile()
    {
        WebServer server=new WebServer(context);
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner()
        {
            @Override
            public void onServerResponded(String s)
            {
                try
                {
                    Gson gson=new Gson();
                    myprofile=gson.fromJson(s,Profile.class);
                    //Invoke profile if not registered
                    if (myprofile==null)
                    {
                        profilePrompt();
                    }
                }
                catch (Exception e)
                {
                }
            }

            @Override
            public void onServerRevoked() {}
        });
        List<DataRack> racks=new ArrayList<>();
        racks.add(new DataRack("DeviceId",DEVICE_ID));
        server.connectWithPOST(activity,BASE_URL+"Username/getProfile.php",racks);
    }

    private void uploadProfile(Profile profile)
    {
        WebServer server=new WebServer(context);
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner()
        {
            @Override
            public void onServerResponded(String s)
            {
            }

            @Override
            public void onServerRevoked() {}
        });
        List<DataRack> racks=new ArrayList<>();
        racks.add(new DataRack("DeviceID",DEVICE_ID));
        racks.add(new DataRack("Fname",profile.getFirstname()));
        racks.add(new DataRack("Lname",profile.getLastname()));
        racks.add(new DataRack("Email",profile.getEMail()));
        server.connectWithPOST(activity,BASE_URL+"Username/setProfile.php",racks);
    }

    private void checkBlocked()
    {
        WebServer server=new WebServer(context);
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s) {
                Gson gson=new Gson();
                Block block=gson.fromJson(s,Block.class);
                if (block!=null)
                {
                    if (block.getStatus().equals("BLOCKED")) {
                        blockPrompt(block.getReason());
                    }
                }
            }

            @Override
            public void onServerRevoked() {

            }
        });
        List<DataRack> racks=new ArrayList<>();
        racks.add(new DataRack("DeviceId",DEVICE_ID));
        racks.add(new DataRack("AppId",APP_ID));
        server.connectWithPOST(activity,BASE_URL+"Blocks/getBlocks.php",racks);
    }

    private SuperDatabase ingoreDB()
    {
        String DB_SCHEME="CREATE TABLE IF NOT EXISTS `usecase`(" +
                "`SessionID` VARCHAR(5)," +
                "`DeviceID` VARCHAR(50)," +
                "`AppID` VARCHAR(50)," +
                "`AppVersion` VARCHAR(25)," +
                "`Timestamp` VARCHAR(50)," +
                "`Step` VARCHAR(250)" +
                ");" +
                "ALTER TABLE `usecase` ADD PRIMARY KEY( `SessionID`, `DeviceID`, `AppID`, `AppVersion`, `Timestamp`, `Step`); ";
        SuperDatabase database=new SuperDatabase(context,"ingore_db",DB_SCHEME);
        return database;
    }

    private void truncateUsecaseTable()
    {
        ingoreDB().sqlInject("DELETE FROM `usecase`");
    }




    //INGORE UI
    private void updatePrompt(String changeLog, final String Url, boolean critical)
    {
        Dialog update=new Dialog(activity);

        if(critical)
        {
            update.setCancelable(false);
        }
        else
        {
            update.setCancelable(true);
        }

        update.setContentView(R.layout.update_prompt);
        Window window = update.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        TextView changelog=(TextView)update.findViewById(R.id.changelog);
        Button updatenow=(Button)update.findViewById(R.id.updatenow);

        changelog.setText(changeLog.replace("> ","\n\u2022 "));


         updatenow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Create an event that shows user is trying to update
                EasyAppMod easyAppMod = new EasyAppMod(context);
                registerEvent("UPDATE_INITIATED_FROM",easyAppMod.getAppName()+" "+easyAppMod.getAppVersionCode()+" "+easyAppMod.getAppVersion());

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Url));
                activity.startActivity(i);
            }
        });

        update.show();
    }

    private void bugPrompt(final String title, final String onClass, final String onFunction, final String stacktrace)
    {
        final Dialog ask=new Dialog(activity);
        ask.setCancelable(true);
        ask.setContentView(R.layout.bug_prompt);
        Window window = ask.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        Button send=(Button)ask.findViewById(R.id.send);
        ImageView bug=(ImageView)ask.findViewById(R.id.bug);
        TextView errortitle=(TextView)ask.findViewById(R.id.errortitle);
        TextView errorpoint=(TextView)ask.findViewById(R.id.errorpoint);
        EditText stktrace=(EditText)ask.findViewById(R.id.stacktrace);

        errortitle.setText(title);
        errorpoint.setText(onFunction+"\n"+onClass);
        stktrace.setText(stacktrace);

        bug.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LinearLayout log=(LinearLayout)ask.findViewById(R.id.log);
                if (log.getVisibility()==View.GONE)
                {
                    log.setVisibility(View.VISIBLE);
                }
                else
                {
                    log.setVisibility(View.GONE);
                }
            }
        });

        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                //Create an event that shows a bug report is fired
                EasyAppMod easyAppMod = new EasyAppMod(context);
                registerEvent("BUG_REPORTED_FROM",easyAppMod.getAppName()+" "+easyAppMod.getAppVersionCode()+" "+easyAppMod.getAppVersion());

                WebServer server=new WebServer(context);
                server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
                    @Override
                    public void onServerResponded(String s) {
                        Toast.makeText(context, "Thank you for reporting the bug. We will fix it as soon as possible", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onServerRevoked() {

                    }
                });
                List<DataRack> racks=new ArrayList<>();
                EditText steps=(EditText)ask.findViewById(R.id.steps);
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a").format(Calendar.getInstance().getTime());
                racks.add(new DataRack("DeviceID",DEVICE_ID));
                racks.add(new DataRack("AppID",APP_ID));
                racks.add(new DataRack("AppVersion",easyAppMod.getAppVersionCode()));
                racks.add(new DataRack("Timestamp",date));
                racks.add(new DataRack("Title",title));
                racks.add(new DataRack("Class",onClass));
                racks.add(new DataRack("Function",onFunction));
                racks.add(new DataRack("Stacktrace",stacktrace));
                racks.add(new DataRack("Steps",steps.getText().toString()));
                server.connectWithPOST(activity,BASE_URL+"Bugs/registerBug.php",racks);
                ask.dismiss();
            }
        });

        ask.show();
    }

    private void futurePrompt()
    {
        final Dialog ask=new Dialog(activity);
        ask.setCancelable(true);
        ask.setContentView(R.layout.future_prompt);
        Window window = ask.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        Button send=(Button)ask.findViewById(R.id.send);
        final EditText suggestion=(EditText)ask.findViewById(R.id.suggestion);

        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if(suggestion.getText().length()>=5)
                {
                    //Future prompting
                    EasyAppMod easyAppMod = new EasyAppMod(context);
                    registerEvent("FEATURE_SUGGESTED",suggestion.getText().toString());

                    WebServer server=new WebServer(context);
                    server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
                        @Override
                        public void onServerResponded(String s) {
                        }

                        @Override
                        public void onServerRevoked() {

                        }
                    });
                    List<DataRack> racks=new ArrayList<>();
                    String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a").format(Calendar.getInstance().getTime());
                    racks.add(new DataRack("DeviceID",DEVICE_ID));
                    racks.add(new DataRack("AppID",APP_ID));
                    racks.add(new DataRack("AppVersion",easyAppMod.getAppVersionCode()));
                    racks.add(new DataRack("Message",suggestion.getText().toString()));
                    racks.add(new DataRack("Timestamp",date));
                    server.connectWithPOST(activity,BASE_URL+"Feature/registerFeature.php",racks);
                    ask.dismiss();
                }
                else
                {
                    suggestion.setError("Please suggest properly");
                }

            }
        });


        ask.show();
    }

    private void rateAndReview(String request)
    {
        if (!settings.retriveSettings("dontShowRateAndReview").equals("true"))
        {
            final Dialog ask=new Dialog(activity);
            ask.setCancelable(true);
            ask.setContentView(R.layout.rating_prompt);
            Window window = ask.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
            Button rate=(Button)ask.findViewById(R.id.ratenow);
            TextView ratetext=(TextView)ask.findViewById(R.id.ratetext);
            TextView dontshow=(TextView)ask.findViewById(R.id.dontshow);
            ratetext.setText(request);
            dontshow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    //Rating declined
                    EasyAppMod easyAppMod = new EasyAppMod(context);
                    registerEvent("RATING_DONTSHOW_FROM",easyAppMod.getAppName()+" "+easyAppMod.getAppVersionCode()+" "+easyAppMod.getAppVersion());
                    settings.saveSettings("dontShowRateAndReview","true");
                    ask.dismiss();
                }
            });
            rate.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {

                    //Rating initiated
                    EasyAppMod easyAppMod = new EasyAppMod(context);
                    registerEvent("RATING_ATTEMPT_FROM",easyAppMod.getAppName()+" "+easyAppMod.getAppVersionCode()+" "+easyAppMod.getAppVersion());

                    Intent i = new Intent(Intent.ACTION_VIEW);
                    i.setData(Uri.parse(getPlayLink()));
                    activity.startActivity(i);
                }
            });
            ask.show();
        }
    }

    private void whatsappPrompt(final String number)
    {
        final Dialog ask=new Dialog(activity);
        ask.setCancelable(true);
        ask.setContentView(R.layout.whatsapp_prompt);
        Window window = ask.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        Button chat=(Button)ask.findViewById(R.id.chat);

        chat.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                registerEvent("WHATSAPP_CHAT","Tried");
                if (myprofile!=null)
                {
                    PackageManager packageManager = context.getPackageManager();
                    Intent i = new Intent(Intent.ACTION_VIEW);
                    try
                    {
                        EasyAppMod app = new EasyAppMod(context);
                        String message= URLEncoder.encode("My name is *"+myprofile.getFirstname()+" "+myprofile.getLastname()+"*\nI'm using *"+app.getAppName()+" "+app.getAppVersionCode()+" "+app.getAppVersion()+"*\nI would like to initialise a conversation with support desk", "UTF-8");
                        String url = "https://api.whatsapp.com/send?phone="+ number +"&text="+message;
                        i.setPackage("com.whatsapp");
                        i.setData(Uri.parse(url));
                        if (i.resolveActivity(packageManager) != null)
                        {
                            context.startActivity(i);
                        }
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
                else
                {
                 accureProfile();
                }
            }
        });

        ask.show();
    }

    private void profilePrompt()
    {
        final Dialog ask=new Dialog(activity);
        ask.setCancelable(false);
        ask.setContentView(R.layout.profile_prompt);
        Window window = ask.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        Button start=(Button)ask.findViewById(R.id.send);

        final EditText fname=(EditText)ask.findViewById(R.id.fname);
        final EditText lname=(EditText)ask.findViewById(R.id.lastname);
        final EditText email=(EditText)ask.findViewById(R.id.email);


        start.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (fname.getText().length()>1)
                {
                    if(lname.getText().length()>0)
                    {
                        if (email.length()>0)
                        {
                            if (isValidEmail(email.getText()))
                            {
                                registerEvent("EMAIL_PROVIDED",email.getText().toString());
                                WebServer server=new WebServer(context);
                                server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
                                    @Override
                                    public void onServerResponded(String s) {
                                    }

                                    @Override
                                    public void onServerRevoked() {

                                    }
                                });
                                List<DataRack> racks=new ArrayList<>();
                                racks.add(new DataRack("DeviceID",DEVICE_ID));
                                racks.add(new DataRack("Fname",fname.getText().toString()));
                                racks.add(new DataRack("Lname",lname.getText().toString()));
                                racks.add(new DataRack("Email",email.getText().toString()));
                                server.connectWithPOST(activity,BASE_URL+"Username/setProfile.php",racks);
                                ask.dismiss();
                            }
                            else
                            {
                                email.setError("Invalid email format");
                            }
                        }
                        else
                        {
                            WebServer server=new WebServer(context);
                            server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
                                @Override
                                public void onServerResponded(String s) {
                                }

                                @Override
                                public void onServerRevoked() {

                                }
                            });
                            List<DataRack> racks=new ArrayList<>();
                            racks.add(new DataRack("DeviceID",DEVICE_ID));
                            racks.add(new DataRack("Fname",fname.getText().toString()));
                            racks.add(new DataRack("Lname",lname.getText().toString()));
                            racks.add(new DataRack("Email",email.getText().toString()));
                            server.connectWithPOST(activity,BASE_URL+"Username/setProfile.php",racks);
                            ask.dismiss();
                        }
                    }
                    else
                    {
                        lname.setError("Proper lastname required");
                    }
                }
                else
                {
                    fname.setError("Proper firstname required");
                }
            }
        });


        ask.show();
    }

    private boolean isValidEmail(CharSequence target)
    {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    private void blockPrompt(String reasontext)
    {
        registerEvent("APP_BLOCKED",reasontext);
        final Dialog ask=new Dialog(activity);
        ask.setCancelable(false);
        ask.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ask.setContentView(R.layout.block_prompt);
        Window window = ask.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        Button contact=(Button)ask.findViewById(R.id.contact);
        Button close=(Button)ask.findViewById(R.id.close);
        TextView reason=(TextView)ask.findViewById(R.id.reason);

        reason.setText(reasontext);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.finish();
                System.exit(0);
            }
        });

        contact.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (whatsappnumber==null)
                {
                    Toast.makeText(context, "Sorry, The developer of this application does not provide a medium to communicate over WhatsApp", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    whatsappPrompt(whatsappnumber);
                }
            }
        });


        ask.show();
    }

    private void adPrompt(String buttontext, String themecolor, final String adurl, final String adredirecturl)
    {
        registerEvent("AD_DISPLAYED",buttontext);
        final Dialog ask=new Dialog(activity);
        ask.setCancelable(true);
        ask.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        ask.setContentView(R.layout.ads_prompt);
        Window window = ask.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);
        Button adcenter=(Button)ask.findViewById(R.id.adcenter);
        adcenter.setText(buttontext);
        ImageView adview=(ImageView)ask.findViewById(R.id.adview);
        Glide.with(context)
                .load(adurl)
                .asGif()
                .into(adview);

        int parseColor = Color.parseColor(themecolor);
        adcenter.setBackgroundColor(parseColor);

        adcenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                registerEvent("LAST_AD_EXPLORED",adurl);
                activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(adredirecturl)));
            }
        });
        ask.show();
    }





    //INGORE ANELETIC SERVICE MODULES
    private String getPlayLink()
    {
        EasyAppMod easyAppMod = new EasyAppMod(context);
        String playlink="https://play.google.com/store/apps/details?id="+easyAppMod.getPackageName();
        return playlink;
    }

    private void isFingerprint()
    {
        EasyFingerprintMod easyFingerprintMod = new EasyFingerprintMod(context);
        rack.add(new DataRack("IsFingerprint",String.valueOf(easyFingerprintMod.isFingerprintSensorPresent())));
        getConfigs();
    }

    private void getConfigs()
    {
        EasyConfigMod easyConfigMod = new EasyConfigMod(context);
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS a").format(Calendar.getInstance().getTime());
        rack.add(new DataRack("CurrentDate", date));
        rack.add(new DataRack("IsSDCard", String.valueOf(easyConfigMod.hasSdCard())));
        getNetwork();
    }

    private void getNetwork()
    {
        EasyNetworkMod easyNetworkMod=new EasyNetworkMod(context);
        @NetworkType
        int networkType = easyNetworkMod.getNetworkType();
        switch (networkType) {
            case NetworkType.CELLULAR_UNKNOWN:
                rack.add(new DataRack("NetworkType", "CELLULAR_UNKNOWN"));
                break;
            case NetworkType.CELLULAR_UNIDENTIFIED_GEN:
                rack.add(new DataRack("NetworkType", "CELLULAR_UNIDENTIFIED_GEN"));
                break;
            case NetworkType.CELLULAR_2G:
                rack.add(new DataRack("NetworkType", "CELLULAR_2G"));
                break;
            case NetworkType.CELLULAR_3G:
                rack.add(new DataRack("NetworkType", "CELLULAR_3G"));
                break;
            case NetworkType.CELLULAR_4G:
                rack.add(new DataRack("NetworkType", "CELLULAR_4G"));
                break;
            case NetworkType.WIFI_WIFIMAX:
                rack.add(new DataRack("NetworkType", "WIFI_WIFIMAX"));
                break;
            case NetworkType.UNKNOWN:
            default:
                rack.add(new DataRack("NetworkType", "UNKNOWN"));
                break;
        }
        getMemory();
    }

    private void getMemory()
    {
        EasyMemoryMod easyMemoryMod = new EasyMemoryMod(context);
        rack.add(new DataRack("TotalRAM",String.valueOf(easyMemoryMod.convertToMb(easyMemoryMod.getTotalRAM()))));
        rack.add(new DataRack("AvailInternal",String.valueOf(easyMemoryMod.convertToMb(easyMemoryMod.getAvailableInternalMemorySize()))));
        rack.add(new DataRack("AvailExternal",String.valueOf(easyMemoryMod.convertToMb(easyMemoryMod.getAvailableExternalMemorySize()))));
        rack.add(new DataRack("TotalInternal",String.valueOf(easyMemoryMod.convertToMb(easyMemoryMod.getTotalInternalMemorySize()))));
        rack.add(new DataRack("TotalExternal",String.valueOf(easyMemoryMod.convertToMb(easyMemoryMod.getTotalExternalMemorySize()))));
        getAppdetails();
    }

    private void getAppdetails()
    {
        EasyAppMod easyAppMod = new EasyAppMod(context);
        rack.add(new DataRack("Activity",activity.getClass().getSimpleName()));
        rack.add(new DataRack("Package",easyAppMod.getPackageName()));
        rack.add(new DataRack("AppStore",easyAppMod.getStore()));
        rack.add(new DataRack("AppName",easyAppMod.getAppName()));
        rack.add(new DataRack("AppVersion",easyAppMod.getAppVersion()));
        rack.add(new DataRack("AppVersionCode",easyAppMod.getAppVersionCode()));
        getBattery();
    }

    private void getBattery()
    {
        EasyBatteryMod easyBatteryMod = new EasyBatteryMod(context);
        rack.add(new DataRack("BatteryCharge",String.valueOf(easyBatteryMod.getBatteryPercentage())));
        rack.add(new DataRack("IsCharging",String.valueOf(easyBatteryMod.isDeviceCharging())));
        getDevice();
    }

    private void getDevice()
    {
        EasyDeviceMod easyDeviceMod = new EasyDeviceMod(context);
        rack.add(new DataRack("Manufacturer",easyDeviceMod.getManufacturer()));
        rack.add(new DataRack("Model",easyDeviceMod.getModel()));
        rack.add(new DataRack("OSCodename",easyDeviceMod.getOSCodename()));
        rack.add(new DataRack("OSVersion",easyDeviceMod.getOSVersion()));
        rack.add(new DataRack("Product",easyDeviceMod.getProduct()));
        rack.add(new DataRack("Device",easyDeviceMod.getDevice()));
        rack.add(new DataRack("Board",easyDeviceMod.getBoard()));
        rack.add(new DataRack("Hardware",easyDeviceMod.getHardware()));
        rack.add(new DataRack("Rooted",String.valueOf(easyDeviceMod.isDeviceRooted())));
        rack.add(new DataRack("Brand",easyDeviceMod.getBuildBrand()));
        rack.add(new DataRack("Host",easyDeviceMod.getBuildHost()));
        rack.add(new DataRack("Time",String.valueOf(easyDeviceMod.getBuildTime())));
        getDisplay();
    }

    private void getDisplay()
    {
        EasyDisplayMod easyDisplayMod = new EasyDisplayMod(context);
        rack.add(new DataRack("ScreenResolution",easyDisplayMod.getResolution()));
        rack.add(new DataRack("ScreenDensity",easyDisplayMod.getDensity()));
        rack.add(new DataRack("ScreenSize",String.valueOf(easyDisplayMod.getPhysicalSize())));
        getSIM();
    }

    private void getSIM()
    {
        EasySimMod easySimMod = new EasySimMod(context);
        rack.add(new DataRack("Country",easySimMod.getCountry()));
        rack.add(new DataRack("Carrier",easySimMod.getCarrier()));
        rack.add(new DataRack("ActiveTraffic",checkNetworkStatus()));
        pushIngore();
    }

    private void registerCount()
    {
        prefs = activity.getPreferences(Context.MODE_PRIVATE);
        editor = prefs.edit();
        totalCount = prefs.getInt("counter", 0);
        totalCount++;
        editor.putInt("counter", totalCount);
        editor.commit();
        count=totalCount;
        if(String.valueOf(count).equals(""))
        {
            count=1;
        }
        rack.add(new DataRack("AppUsage",String.valueOf(count)));
        isFingerprint();
    }

    private String checkNetworkStatus()
    {
        ConnectivityManager connectivitymanager = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] networkInfo = connectivitymanager.getAllNetworkInfo();
        String traffic="Unknown";
        for (NetworkInfo netInfo : networkInfo)
        {
            if (netInfo.getTypeName().equalsIgnoreCase("WIFI"))
                if (netInfo.isConnected())
                    traffic="WiFi";
            if (netInfo.getTypeName().equalsIgnoreCase("MOBILE"))
                if (netInfo.isConnected())
                    traffic="MobileData";
        }
        return traffic;
    }

    private void pushIngore()
    {
        WebServer server=new WebServer(context);
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s) {
            }

            @Override
            public void onServerRevoked() {
            }
        });
        server.connectWithPOST(activity,BASE_URL+"Devices/registerDevice.php",rack);
    }
}
