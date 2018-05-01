package ingore.sangee.ingorelib;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import bullyfox.sangeeth.testube.component.DataRack;
import bullyfox.sangeeth.testube.managers.AppSettings;
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
    String APP_ID,DEVICE_ID;
    List<DataRack> rack;
    AppSettings settings;
    int count=0;
    Profile myprofile;

    public Ingore(Context context, Activity activity) {
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
    }

    public void initIngore()
    {
        registerCount();
        accureProfile();
    }

    public void registerEvent(String name,String value)
    {
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(Calendar.getInstance().getTime());
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
        server.connectWithPOST(activity,context.getString(R.string.baseURL)+"regevents.php",eventrack);
    }

    public int getAppSessionCount()
    {
        return count;
    }

    public Profile getUserProfile()
    {
        return myprofile;
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
        server.connectWithGET(context.getString(R.string.baseURL)+"getupdates.php?AppID="+APP_ID);
    }

    public void invokeFuturePrompt()
    {
        futurePrompt();
    }

    public void invokeBugPrompt(Exception exception)
    {
        String stackTrace = Log.getStackTraceString(exception);
        bugPrompt(stackTrace);
    }

    public void invokeWhatsAppPrompt(String number)
    {
        whatsappPrompt(number);
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
        server.connectWithGET(context.getString(R.string.baseURL)+"getprofile.php?DeviceId="+DEVICE_ID);
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
        server.connectWithPOST(activity,context.getString(R.string.baseURL)+"setprofile.php",racks);
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

        changelog.setText(changeLog);
         updatenow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Create an event that shows user is trying to update
                EasyAppMod easyAppMod = new EasyAppMod(context);
                registerEvent("UPDATED_INITIATED_FROM",easyAppMod.getAppName()+" "+easyAppMod.getAppVersionCode());

                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Url));
                activity.startActivity(i);
            }
        });

        update.show();
    }

    private void bugPrompt(final String stacktrace)
    {
        final Dialog ask=new Dialog(activity);
        ask.setCancelable(true);
        ask.setContentView(R.layout.bug_prompt);
        Window window = ask.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);

        Button send=(Button)ask.findViewById(R.id.send);

        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                //Create an event that shows a bug report is fired
                EasyAppMod easyAppMod = new EasyAppMod(context);
                registerEvent("BUG_REPORTED_BY_USER",easyAppMod.getAppName()+" "+easyAppMod.getAppVersionCode());

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
                EditText steps=(EditText)ask.findViewById(R.id.steps);
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(Calendar.getInstance().getTime());
                racks.add(new DataRack("DeviceID",DEVICE_ID));
                racks.add(new DataRack("AppID",APP_ID));
                racks.add(new DataRack("AppVersion",easyAppMod.getAppVersionCode()));
                racks.add(new DataRack("Timestamp",date));
                racks.add(new DataRack("Stacktrace",stacktrace));
                racks.add(new DataRack("Steps",steps.getText().toString()));
                server.connectWithPOST(activity,context.getString(R.string.baseURL)+"regbug.php",racks);
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

        send.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                //Future prompting
                EasyAppMod easyAppMod = new EasyAppMod(context);
                registerEvent("FEATURE_SUGGESTED",easyAppMod.getAppName()+" "+easyAppMod.getAppVersionCode());

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
                EditText suggestion=(EditText)ask.findViewById(R.id.suggestion);
                String date = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(Calendar.getInstance().getTime());
                racks.add(new DataRack("DeviceID",DEVICE_ID));
                racks.add(new DataRack("AppID",APP_ID));
                racks.add(new DataRack("AppVersion",easyAppMod.getAppVersionCode()));
                racks.add(new DataRack("Message",suggestion.getText().toString()));
                racks.add(new DataRack("Timestamp",date));
                server.connectWithPOST(activity,context.getString(R.string.baseURL)+"regfeature.php",racks);
                ask.dismiss();
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
                    registerEvent("RATING_DONTSHOW_FROM",easyAppMod.getAppName()+" "+easyAppMod.getAppVersionCode());

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
                    registerEvent("RATING_INITIATED_FROM",easyAppMod.getAppName()+" "+easyAppMod.getAppVersionCode());

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
                server.connectWithPOST(activity,context.getString(R.string.baseURL)+"setprofile.php",racks);
                ask.dismiss();
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
        String date = new SimpleDateFormat("yyyy-MM-dd hh:mm a").format(Calendar.getInstance().getTime());
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
        if (settings.retriveSettings("isFirst").equals(""))
        {
            count++;
            settings.saveSettings("isFirst",String.valueOf(count));
        }
        else
        {
            count=Integer.parseInt(settings.retriveSettings("isFirst"));
            count++;
            settings.saveSettings("isFirst",String.valueOf(count));
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
        server.connectWithPOST(activity,context.getString(R.string.baseURL)+"regdevices.php",rack);
    }
}
