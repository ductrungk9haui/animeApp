package animes.englishsubtitle.freemovieseries.utils;

import android.app.ActivityManager;
import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.onesignal.OneSignal;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Request;

import animes.englishsubtitle.freemovieseries.utils.ads.AudienceNetworkHelper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import animes.englishsubtitle.freemovieseries.Config;
import animes.englishsubtitle.freemovieseries.NotificationClickHandler;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.network.RetrofitClient;
import animes.englishsubtitle.freemovieseries.network.apis.SubscriptionApi;
import animes.englishsubtitle.freemovieseries.network.model.ActiveStatus;

public class MyAppClass extends Application {

    public static final String NOTIFICATION_CHANNEL_ID = "download_channel_id";
    private static Context mContext;

    //private static AppOpenManager appOpenManager;


    @Override
    public void onCreate() {
        super.onCreate();

//        if (AudienceNetworkAds.isInAdsProcess(this)) {
//            return;
//        }

        MobileAds.initialize(
                this,
                new OnInitializationCompleteListener() {
                    @Override
                    public void onInitializationComplete(InitializationStatus initializationStatus) {}
                });

       // appOpenManager = new AppOpenManager(this);

        Picasso.setSingletonInstance(getCustomPicasso());

        mContext = this;
        createNotificationChannel();


        // OneSignal Initialization
        /*OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationClickHandler(mContext))
                //.inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();*/
        //OneSignal new setup
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new NotificationClickHandler(mContext))
                .inFocusDisplaying(OneSignal.OSInFocusDisplayOption.Notification)
                .unsubscribeWhenNotificationsAreDisabled(true)
                .init();

        SharedPreferences preferences = getSharedPreferences("push", MODE_PRIVATE);
        if (preferences.getBoolean("status", true)) {
            OneSignal.setSubscription(true);
        } else {
            OneSignal.setSubscription(false);
        }

        //

        if (!getFirstTimeOpenStatus()) {
            if (Config.DEFAULT_DARK_THEME_ENABLE) {
                changeSystemDarkMode(true);
            } else {
                changeSystemDarkMode(false);
            }
            saveFirstTimeOpenStatus(true);
        }

        // fetched and save the user active status if user is logged in
        String userId = PreferenceUtils.getUserId(this);
        if (userId != null && !userId.equals("")) {
            updateActiveStatus(userId);
        }

        // Initialize the Audience Network SDK
        AudienceNetworkHelper.initialize(this);
     }

    private Picasso getCustomPicasso() {
        Picasso.Builder builder = new Picasso.Builder(this);
        //set 12% of available app memory for image cachecc
        builder.memoryCache(new LruCache(getBytesForMemCache(20)));
        //set request transformer
        Picasso.RequestTransformer requestTransformer =  new Picasso.RequestTransformer() {
            @Override
            public Request transformRequest(Request request) {
                Log.d("image request", request.toString());
                return request;
            }
        };
        builder.requestTransformer(requestTransformer);

        builder.listener(new Picasso.Listener() {
            @Override
            public void onImageLoadFailed(Picasso picasso, Uri uri,
                                          Exception exception) {
                Log.d("image load error", uri.getPath());
            }
        });

        return builder.build();
    }

    private int getBytesForMemCache(int percent) {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager)
                getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);

        double availableMemory= mi.availMem;

        return (int)(percent*availableMemory/100);
    }


    private void createNotificationChannel() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NotificationName",
                    NotificationManager.IMPORTANCE_DEFAULT);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }


    public void changeSystemDarkMode(boolean dark) {
        SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
        editor.putBoolean("dark", dark);
        editor.apply();
    }

    public void saveFirstTimeOpenStatus(boolean dark) {
        SharedPreferences.Editor editor = getSharedPreferences("push", MODE_PRIVATE).edit();
        editor.putBoolean("firstTimeOpen", true);
        editor.apply();

    }

    public boolean getFirstTimeOpenStatus() {
        SharedPreferences preferences = getSharedPreferences("push", MODE_PRIVATE);
        return preferences.getBoolean("firstTimeOpen", false);
    }

    public static Context getContext() {
        return mContext;
    }

    private void updateActiveStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(Config.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, Response<ActiveStatus> response) {
                if (response.code() == 200) {
                    ActiveStatus activeStatus = response.body();
                    DatabaseHelper db = new DatabaseHelper(getApplicationContext());
                    db.deleteAllActiveStatusData();
                    db.insertActiveStatusData(activeStatus);
                }
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }


}
