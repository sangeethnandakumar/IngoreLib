# Integrate Ingore

* Add your AccessToken into app manifest

```xml
<meta-data 
           android:name="AccessToken" 
           android:value="XXXXXXXXXXXXXXXXXX" />
```

* Create an instance of Ingore on the onCreate() of activity

```java
public class MainActivity extends Activity {
    //Create an Ingore global object
    private Ingore ingore;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        //Inialise object
        ingore = new Ingore(ReportingMode.RELEASE_MODE , this);
    }
```

* If you need WhatsApp chat assistance initialise Ingore with your WhatsApp number. Make sure the country code should come before mobile number except '+'. (91 is for India)

```java
  ingore = new Ingore(ReportingMode.RELEASE_MODE , this, "91XXXXXXXXXX");
```

* Sync data with Ingore servers on your onStart() methord

```java
@Override
    protected void onStart() {
        super.onStart();
        //Sync data with Ingore Server
        ingore.initIngore();
    }
```
# Before Getting Started

## Please Read This Before Using The Service
> Make sure you are not calling **ingore.initIngore()** everywhere. This will lead to a higher network congession and impact perfomance

Rather use **ingore.initIngore()** at critical points where device info need to be synced with Ingore servers

> Use **ingore.initIngore()** only during **onStart()** of the activity

If Ingore found that the user is new, It prompts the user to enter his Name and Email. This is an automated task and without completing the form, User is not allowed to use the app

![Image of Ingore Profile Identification Service](https://github.com/sangeethnandakumar/IngoreLib/blob/master/profile.jpg?raw=true)

> Email is not mandatory however name is usually required, This is to identify the user on Ingore Console

To get the profile info Ingore collected, You can use
```java
  Profile myprofile = ingore.getUserProfile();
```

If you want to bypass this procedure, For instance you are already collected User details and no need for Ingore to ask again, Then pass a profile while ingore.initIngore() is called;

```java
  Profile profile = new Profile('Daniel', 'Erik', 'danielerik@someone.com');
  //OR
  Profile profile = new Profile('Daniel', 'Erik');
```
And pass it to Ingore during initialisation

```java
  ingore.initIngore(profile);
```

Or if you wan't to display the Profile prompt manually, Consider this

```java
  ingore.invokeProfilePrompt();
```

# Ingore Services

## I. Update Notification Service

![Image of Ingore Update Notifier](https://github.com/sangeethnandakumar/IngoreLib/blob/master/update.jpg?raw=true)

Ingore can help your users notified about new app releases. Just call the **invokeUpdatePrompt()** whenever you need to check for updates

> It's better to use **invokeUpdatePrompt()** after **initIngore()** during **onStart()**

```java
@Override
    protected void onStart() {
        super.onStart();
        //Sync data with Ingore Server
        ingore.initIngore();
        //Check for updates
        ingore.invokeUpdatePrompt();
    }
```
> In order to notify users about updates, you need to enroll an update on online Ingore Dashboard

There are 2 types of updates
* Critical Update
* Normal Update

If you enroll a critical update, Then the user is not allowed to use the app anymore without being updated to new version. If you enroll a normal update then he is presented with a changelog and he can discard the update prompt and use app

## II. User Feedback Service

![Image of Ingore Feedback Service](https://github.com/sangeethnandakumar/IngoreLib/blob/master/feature.jpg?raw=true)

Ingore makes it easy to collect feedbacks from your userbase

> Just call invokeFuturePrompt() to display a feedback form and collect his suggession on the fly with 1 line of code

```java
Button collectFeedback = (Button) findViewById(R.id.collectFeedback);
collectFeedback.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Display user suggession prompt
                ingore.invokeFuturePrompt();
            }
        });
```

## III. Bug Reporting Service
Strike with a bug? Collect info from online Ingore Dashboard

> Call invokeBugPrompt() whenever an exception hits your app

```java
try
{
  // Buggy code here
}
catch(Exception e)
{
  //Report with Ingore
  invokeBugPrompt("MUSIC NOT FOUND EXCEPTION", "MainActivity.java", "playMusic()", e)
}
```

## IV. WhatsApp Support Service

![Image of Ingore WhatsApp Support Service](https://github.com/sangeethnandakumar/IngoreLib/blob/master/whatsapp.jpg?raw=true)

If your users are able to chat with you on WhatsApp, You can give them support and assistance. If you prefer so, Simply call the invokeWhatsAppPrompt()

> Inorder to use invokeWhatsAppPrompt(), You need to initialise Ingore with your WhatsApp Number as mentioned above

```java
Button chatOnWhatsApp = (Button) findViewById(R.id.chatOnWhatsApp);
chatOnWhatsApp.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Display user whatsApp chat prompt
                ingore.invokeWhatsAppPrompt();
            }
        });
```

> You will get the users full name and the App and Version name along WhatsApp chat (Note: Users may able to delete those prior sending)

## V. Play Rating Support Service

![Image of Ingore Rating Support Service](https://github.com/sangeethnandakumar/IngoreLib/blob/master/rate.jpg?raw=true)

Remember users to take some time to rate your app. Present them with a prompt manually

```java
Button rateOnPlaystore = (Button) findViewById(R.id.rateOnPlaystore);
rateOnPlaystore.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Display user whatsApp chat prompt
                ingore.invokeRateOnPlaystore("Hi, Are you enjoying this app? If yes then please take a second to rate it");
            }
        });
```

If you need to display a rating prompt during prime intervals of your app usage, Consider this

```java
@Override
    protected void onStart() {
        super.onStart();
        //Sync data with Ingore Server
        ingore.initIngore();
        //Request to rate (Prompts only during prime intervals)
        invokeRateOnPlaystoreOnPrimeIntervals("Hi, Are you enjoying this app? If yes then please take a second to rate it");
    }
```

## VI. Promotion Service
Engage your users with ads made by you. May be to promote your other apps or something

> You need to enroll an Ad on online Ingore Console first. Ads are served randomly

```java
public void displayMyAd()
{
  //Call the Ad service
  ingore.invokeAdsPrompt();
}
```
## VII. Banning Service
Ingore can ban some devices from using your app. If you found some users are illegaly exploiting our app, Try blocking the device from running your app.

> Enroll a device that need to be banned on online Ingore Console and the users will be not able to use it until you unblocked explicitly. You can provide custom message for him to state why you banned him. He may be ale to chat on WhatsApp when you initialise Ingore with WhatsApp number

Blocking is automatic during initIngore() callback

> Blocking service will work only when device is online. Else the blocker will not work

## VIII. Event Registering Service
Discover when events happened inside your app like someone is attempted to rate it or someone scorred a high score

```java
Button rateOnPlaystore = (Button) findViewById(R.id.rateOnPlaystore);
rateOnPlaystore.setOnClickListener( new OnClickListener() {

            @Override
            public void onClick(View v) {
                // Register as an event happened
                ingore.registerEvent("ATTEPMTED_TO_RATE_MY_APP" , "After scorring high score");
            }
        });
```

## IX. App Usage Counter Service
Ingore let you collect how much time does your app opened by a user. This data is collected automatically and reported during syncing
> Some devices does not support collecting app count usage, This is a known issue

```java
  int app_count = 0;
  app_count = getAppSessionCount();
```

## X. Messaging Service
Enroll messages in online Ingore console, and get them directly to your app. Use this service to provide some sort of notifications to your users

App usage counter is automatic. If you need to retrive usage count, Use this
```java
  public void displayMessages()
  {
    ingore.getMessages(new OnMessagesListner()
    {
    
      @Override
      public void todaysMessages(List<Message>) {
      
      }
      
      @Override
      public void futureMessages(List<Message>) {
      
      }
    });
  }
```
