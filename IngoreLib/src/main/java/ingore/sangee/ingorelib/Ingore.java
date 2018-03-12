package ingore.sangee.ingorelib;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import java.util.ArrayList;
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
import github.nisrulz.easydeviceinfo.base.EasySensorMod;
import github.nisrulz.easydeviceinfo.base.EasySimMod;
import github.nisrulz.easydeviceinfo.base.NetworkType;

/**
 * Created by Sangee's Lap on 3/10/2018.
 */

public class Ingore {
    Context context;
    Activity activity;
    String USER_ID,APP_ID,DEVICE_ID;
    List<DataRack> rack;
    AppSettings settings;
    int count=0;

    //Configs
        //UPDATES
        boolean reportUpdate=true;
        boolean forceUpdate=false;

    public Ingore(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        settings=new AppSettings(context);
        rack=new ArrayList<>();

        try {
            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = ai.metaData;
            //GET AppID
            APP_ID = bundle.getString("AppID");
            //GET DeviceID
            DEVICE_ID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            rack.add(new DataRack("DeviceId",DEVICE_ID));
            rack.add(new DataRack("AppId",APP_ID));
            isFingerprint();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }


    //INGORE CONFIGS
    public void setUpdateReporter(boolean reportUpdate)
    {
        this.reportUpdate=reportUpdate;
    }
    public void setForceUpdate(boolean forceUpdate)
    {
        this.forceUpdate=forceUpdate;
    }


    // INGORE CORE CALLS
    public void initIngore(){
        registerCount();
    }

    public void registerEvent(String name,String value)
    {
        List<DataRack> eventrack=new ArrayList<>();
        eventrack.add(new DataRack("DeviceID",DEVICE_ID));
        eventrack.add(new DataRack("AppID",APP_ID));
        eventrack.add(new DataRack("EventName",name));
        eventrack.add(new DataRack("EventValue",value));
        eventrack.add(new DataRack("Flags","not_set"));
        WebServer server=new WebServer(context);
        server.setOnServerStatusListner(new WebServer.OnServerStatusListner() {
            @Override
            public void onServerResponded(String s) {
                Toast.makeText(context, s, Toast.LENGTH_LONG).show();
            }

            @Override
            public void onServerRevoked() {

            }
        });
        server.connectWithPOST(activity,"http://ingore.sangeethnandakumar.com/regevents.php",eventrack);
    }


    //INGORE UI
    private void signinPrompt()
    {
        Dialog ask=new Dialog(activity);
        ask.setCancelable(false);
        ask.setContentView(R.layout.signin_prompt);
        Window window = ask.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        ask.show();
    }

    private void bugPrompt()
    {
        Dialog ask=new Dialog(activity);
        ask.setCancelable(true);
        ask.setContentView(R.layout.bug_prompt);
        Window window = ask.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        ask.show();
    }

    private void futurePrompt()
    {
        Dialog ask=new Dialog(activity);
        ask.setCancelable(true);
        ask.setContentView(R.layout.future_prompt);
        Window window = ask.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        ask.show();
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
        rack.add(new DataRack("CurrentDate", String.valueOf(easyConfigMod.getCurrentDate().getTime())));
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
                Toast.makeText(context, "Error", Toast.LENGTH_SHORT).show();
            }
        });
        server.connectWithPOST(activity,"http://ingore.sangeethnandakumar.com/regdevices.php",rack);
    }
}
