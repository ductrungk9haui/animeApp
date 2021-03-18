package animes.englishsubtitle.freemovieseries.database;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.models.Work;
import animes.englishsubtitle.freemovieseries.network.model.ActiveStatus;
import animes.englishsubtitle.freemovieseries.network.model.User;
import animes.englishsubtitle.freemovieseries.network.model.config.AdsConfig;
import animes.englishsubtitle.freemovieseries.network.model.config.AppConfig;
import animes.englishsubtitle.freemovieseries.network.model.config.Configuration;
import animes.englishsubtitle.freemovieseries.network.model.config.PaymentConfig;
import animes.englishsubtitle.freemovieseries.utils.PreferenceUtils;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "com.oxoo.spagreen.db";

    //config table
    private static final String CONFIG_TABLE_NAME = "configurations";
    private static final String CONFIG_COLUMN_ID = "id";
    private static final String CONFIG_COLUMN_MENU = "menu";
    private static final String CONFIG_COLUMN_PROGRAM_GUIDE_ENABLE = "program_guide";
    private static final String CONFIG_COLUMN_MANDATORY_LOGIN = "mandatory_login";
    private static final String CONFIG_COLUMN_GENRE_SHOW = "genre_show";
    private static final String CONFIG_COLUMN_COUNTRY_SHOW = "country_show";
    private static final String CONFIG_COLUMN_ADS_ENABLE = "ads_enable";
    private static final String CONFIG_COLUMN_AD_NETWOTK_NAME = "ad_network_name";
    private static final String CONFIG_COLUMN_ADMOB_APP_ID = "admob_app_id";
    private static final String CONFIG_COLUMN_ADMOB_BANNER_ID = "admob_banner_id";
    private static final String CONFIG_COLUMN_ADMOB_INTERSTITIAL_ID = "admob_interstitial_id";
    private static final String CONFIG_COLUMN_ADMOB_NATIVE_ID = "admob_native_id";
    private static final String CONFIG_COLUMN_ADMOB_REWARDEDVIDEO_ID = "admob_rewardedvideo_id";
    //private static final String CONFIG_COLUMN_ADMOB_APPOPEN_ID = "admob_appopen_ads_id";
    private static final String CONFIG_COLUMN_FAN_BANNER_ID = "fan_banner_id";
    private static final String CONFIG_COLUMN_FAN_NATIVE_ID = "fan_native_id";
    private static final String CONFIG_COLUMN_FAN_INTERSTITIAL_ID = "fan_interstitial_id";
    private static final String CONFIG_COLUMN_STARTAPP_ID = "startapp_id";

    private static final String PAYMENT_CONFIG_CURRENCY_SYMBOL = "payment_config_currency_symbol";
    private static final String PAYMENT_CONFIG_PAYPAL_EMAIL = "payment_config_paypal_email";
    private static final String PAYMENT_CONFIG_PAYPAL_CLIENT_ID = "payment_config_paypal_client_id";
    private static final String PAYMENT_CONFIG_STRIPE_PUBLISH_KEY = "payment_config_stripe_publishable_key";
    private static final String PAYMENT_CONFIG_STRIPE_SECRET_KEY = "payment_config_stripe_secret_key";
    private static final String PAYMENT_CONFIG_PAYMENTWALL_PROJECTT_KEY = "payment_config_paymentwall_project_key";
    private static final String PAYMENT_CONFIG_PAYMENTWALL_SECRET_KEY = "payment_config_paymentwall_secret_key";
    private static final String PAYMENT_CONFIG_CURRENCY = "payment_config_currency";
    private static final String PAYMENT_CONFIG_EXCHANGE_RATE = "exchange_rate";
    private static final String PAYMENT_CONFIG_RAZOR_PAY_KEY_ID = "razorpay_key_id";
    private static final String PAYMENT_CONFIG_RAZOR_PAY_KEY_SECRETE = "razorpay_key_secrete";
    private static final String PAYMENT_CONFIG_PAYPAL_ENABLE = "paypal_enable";
    private static final String PAYMENT_CONFIG_STRIPE_ENABLE = "stripe_enable";
    private static final String PAYMENT_CONFIG_PAYMENTWALL_ENABLE = "paymentwall_enable";
    private static final String PAYMENT_CONFIG_RAZORPAY_ENABLE = "razorpay_enable";
    //private static final String PAYMENT_CONFIG_RAZORPAY_EXCHANGE_RATE = "razorpay_exchange_rate";

    //subscription table
    private static final String SUBS_TABLE_NAME = "subscription_table";
    private static final String SUBS_COLUMN_ID = "id";
    private static final String SUBS_COLUMN_STATUS = "status";
    private static final String SUBS_COLUMN_PACKAGE_TITLE = "package_title";
    private static final String SUBS_COLUMN_EXPIRE_DATE = "expire_date";
    private static final String SUBS_COLUMN_EXPIRE_TIME = "expire_time";
    //user data table
    private static final String USER_TABLE_NAME = "user_table";
    private static final String USER_COLUMN_ID = "id";
    private static final String USER_COLUMN_NAME = "user_name";
    private static final String USER_COLUMN_USER_ID = "user_id";
    private static final String USER_COLUMN_EMAIL = "user_email";
    // private static final String USER_COLUMN_PHONE = "user_phone";
    private static final String USER_COLUMN_STATUS = "status";
    private static final String USER_COLUMN_PROFILE_IMAGE_URL = "user_profile_image";

    private static final String MOVIE_TABLE_NAME = "movie_table";
    public static final String LAST_MOVIE = "user_last_movie";
    //download table
    public static final String DOWNLOAD_TABLE_NAME = "download_table";
    public static final String DOWNLOAD_COLUMN_ID = "id";
    public static final String WORK_ID = "work_id";
    public static final String DOWNLOAD_ID = "download_id";
    public static final String FILE_NAME = "file_name";
    public static final String TOTAL_SIZE = "total_size";
    public static final String DOWNLOAD_SIZE = "download_size";
    public static final String DOWNLOAD_STATUS = "download_status";
    public static final String URL = "url";
    public static final String URL_SUB_LIST = "url_sub_list";
    public static final String APP_CLOSE_STATUS = "app_close_statuss";

    public static final String MAP_MOVIE_TABLE_NAME = "movie_table";
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_CONFIG_TABLE());
        sqLiteDatabase.execSQL(CREATE_SUBSCRIPTION_STATUS_TABLE());
        sqLiteDatabase.execSQL(CREATE_USER_DATA_TABLE());
        sqLiteDatabase.execSQL(CREATE_DOWNLOAD_DATA_TABLE());
        sqLiteDatabase.execSQL(CREATE_CONFIG_MOVIE_TABLE_NAME());
    }


    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + CONFIG_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + SUBS_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + USER_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + DOWNLOAD_TABLE_NAME);
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + MOVIE_TABLE_NAME);
        onCreate(sqLiteDatabase);

/*

        if (oldVersion < 2) {
            sqLiteDatabase.execSQL(DATABASE_ALTER_USER_1);
            sqLiteDatabase.execSQL(DATABASE_ALTER_CONFIG_1);
        }
*/

    }

    /* private static final String DATABASE_ALTER_USER_1 = "ALTER TABLE "
              + USER_TABLE_NAME + " ADD COLUMN " + USER_COLUMN_PHONE + " TEXT;";

      private static final String DATABASE_ALTER_CONFIG_1 = "ALTER TABLE "
              + CONFIG_TABLE_NAME + " ADD COLUMN " + PAYMENT_CONFIG_RAZORPAY_EXCHANGE_RATE + " TEXT;";
  */
    private String CREATE_CONFIG_MOVIE_TABLE_NAME() {
        return "CREATE TABLE IF NOT EXISTS " + MOVIE_TABLE_NAME +
                " (" + LAST_MOVIE + " TEXT" + ")";
    }

    public long insertMapMovie(String maps) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(LAST_MOVIE, maps);

        long id = db.insert(MOVIE_TABLE_NAME, null, contentValues);
        db.close();

        return id;
    }
    public void deleteAllMapMovie() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + MOVIE_TABLE_NAME);
        db.close();
    }
    public String getMapMovie() {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();
        String out = "";
        User user = new User();

        Cursor cursor = db.rawQuery("SELECT * FROM " + MOVIE_TABLE_NAME, null);

        if (cursor != null)
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    // prepare appConfig object
                    out = cursor.getString(cursor.getColumnIndex(LAST_MOVIE));
                    cursor.moveToNext();
                }
            }

        // close the db connection
        cursor.close();
        if(out.equals("{}")) out = "";
        return out;
    }

    //config table
    private String CREATE_CONFIG_TABLE() {
        return "CREATE TABLE IF NOT EXISTS " + CONFIG_TABLE_NAME +
                " (" + CONFIG_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                CONFIG_COLUMN_MENU + " TEXT," +
                CONFIG_COLUMN_PROGRAM_GUIDE_ENABLE + " INTEGER DEFAULT 0," +
                CONFIG_COLUMN_MANDATORY_LOGIN + " INTEGER DEFAULT 0," +
                CONFIG_COLUMN_GENRE_SHOW + " INTEGER DEFAULT 0," +
                CONFIG_COLUMN_COUNTRY_SHOW + " INTEGER DEFAULT 0," +

                CONFIG_COLUMN_ADS_ENABLE + " TEXT," +
                CONFIG_COLUMN_AD_NETWOTK_NAME + " TEXT," +
                CONFIG_COLUMN_ADMOB_APP_ID + " TEXT," +
                CONFIG_COLUMN_ADMOB_BANNER_ID + " TEXT," +
                CONFIG_COLUMN_ADMOB_INTERSTITIAL_ID + " TEXT," +
                CONFIG_COLUMN_ADMOB_NATIVE_ID + " TEXT," +
                CONFIG_COLUMN_ADMOB_REWARDEDVIDEO_ID + " TEXT," +
                //  CONFIG_COLUMN_ADMOB_APPOPEN_ID + " TEXT," +
                CONFIG_COLUMN_FAN_BANNER_ID + " TEXT," +
                CONFIG_COLUMN_FAN_NATIVE_ID + " TEXT," +
                CONFIG_COLUMN_FAN_INTERSTITIAL_ID + " TEXT," +
                CONFIG_COLUMN_STARTAPP_ID + " TEXT," +

                PAYMENT_CONFIG_CURRENCY_SYMBOL + " TEXT," +
                PAYMENT_CONFIG_PAYPAL_EMAIL + " TEXT," +
                PAYMENT_CONFIG_PAYPAL_CLIENT_ID + " TEXT," +
                PAYMENT_CONFIG_EXCHANGE_RATE + " TEXT," +
                PAYMENT_CONFIG_STRIPE_PUBLISH_KEY + " TEXT," +
                PAYMENT_CONFIG_STRIPE_SECRET_KEY + " TEXT," +
                PAYMENT_CONFIG_PAYMENTWALL_PROJECTT_KEY + " TEXT," +
                PAYMENT_CONFIG_PAYMENTWALL_SECRET_KEY + " TEXT," +
                PAYMENT_CONFIG_RAZOR_PAY_KEY_ID + " TEXT," +
                PAYMENT_CONFIG_RAZOR_PAY_KEY_SECRETE + " TEXT," +
                PAYMENT_CONFIG_PAYPAL_ENABLE + " INTEGER DEFAULT 0," +
                PAYMENT_CONFIG_STRIPE_ENABLE + " INTEGER DEFAULT 0," +
                PAYMENT_CONFIG_PAYMENTWALL_ENABLE + " INTEGER DEFAULT 0," +
                PAYMENT_CONFIG_RAZORPAY_ENABLE + " INTEGER DEFAULT 0," +
                //PAYMENT_CONFIG_RAZORPAY_EXCHANGE_RATE + " TEXT," +
                PAYMENT_CONFIG_CURRENCY + " TEXT" + ")";
    }

    public long insertConfigurationData(Configuration configuration) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();

        contentValues.put(CONFIG_COLUMN_MENU, configuration.getAppConfig().getMenu());
        contentValues.put(CONFIG_COLUMN_PROGRAM_GUIDE_ENABLE, configuration.getAppConfig().getProgramGuideEnable());
        contentValues.put(CONFIG_COLUMN_MANDATORY_LOGIN, configuration.getAppConfig().getMandatoryLogin());
        contentValues.put(CONFIG_COLUMN_GENRE_SHOW, configuration.getAppConfig().getGenreVisible());
        contentValues.put(CONFIG_COLUMN_COUNTRY_SHOW, configuration.getAppConfig().getCountryVisible());

        contentValues.put(CONFIG_COLUMN_ADS_ENABLE, configuration.getAdsConfig().getAdsEnable());
        contentValues.put(CONFIG_COLUMN_AD_NETWOTK_NAME, configuration.getAdsConfig().getMobileAdsNetwork());
        contentValues.put(CONFIG_COLUMN_ADMOB_APP_ID, configuration.getAdsConfig().getAdmobAppId());
        contentValues.put(CONFIG_COLUMN_ADMOB_BANNER_ID, configuration.getAdsConfig().getAdmobBannerAdsId());
        contentValues.put(CONFIG_COLUMN_ADMOB_INTERSTITIAL_ID, configuration.getAdsConfig().getAdmobInterstitialAdsId());
        contentValues.put(CONFIG_COLUMN_ADMOB_NATIVE_ID, configuration.getAdsConfig().getAdmobNativeAdsId());
        contentValues.put(CONFIG_COLUMN_ADMOB_REWARDEDVIDEO_ID, configuration.getAdsConfig().getAdmobRewardedVideoAdsId());
        //contentValues.put(CONFIG_COLUMN_ADMOB_APPOPEN_ID, configuration.getAdsConfig().getAdmobAppOpenAdsId());
        contentValues.put(CONFIG_COLUMN_FAN_BANNER_ID, configuration.getAdsConfig().getFanBannerAdsPlacementId());
        contentValues.put(CONFIG_COLUMN_FAN_NATIVE_ID, configuration.getAdsConfig().getFanNativeAdsPlacementId());
        contentValues.put(CONFIG_COLUMN_FAN_INTERSTITIAL_ID, configuration.getAdsConfig().getFanInterstitialAdsPlacementId());
        contentValues.put(CONFIG_COLUMN_STARTAPP_ID, configuration.getAdsConfig().getStartappAppId());

        contentValues.put(PAYMENT_CONFIG_CURRENCY_SYMBOL, configuration.getPaymentConfig().getCurrencySymbol());
        contentValues.put(PAYMENT_CONFIG_PAYPAL_EMAIL, configuration.getPaymentConfig().getPaypalEmail());
        contentValues.put(PAYMENT_CONFIG_PAYPAL_CLIENT_ID, configuration.getPaymentConfig().getPaypalClientId());
        contentValues.put(PAYMENT_CONFIG_STRIPE_PUBLISH_KEY, configuration.getPaymentConfig().getStripePublishableKey());
        contentValues.put(PAYMENT_CONFIG_STRIPE_SECRET_KEY, configuration.getPaymentConfig().getStripeSecretKey());
        contentValues.put(PAYMENT_CONFIG_PAYMENTWALL_PROJECTT_KEY, configuration.getPaymentConfig().getPaymentwallProjectKey());
        contentValues.put(PAYMENT_CONFIG_PAYMENTWALL_SECRET_KEY, configuration.getPaymentConfig().getPaymentwallSecretKey());
        contentValues.put(PAYMENT_CONFIG_CURRENCY, configuration.getPaymentConfig().getCurrency());
        contentValues.put(PAYMENT_CONFIG_EXCHANGE_RATE, configuration.getPaymentConfig().getExchangeRate());
        contentValues.put(PAYMENT_CONFIG_RAZOR_PAY_KEY_ID, configuration.getPaymentConfig().getRazorpayKeyId());
        contentValues.put(PAYMENT_CONFIG_RAZOR_PAY_KEY_SECRETE, configuration.getPaymentConfig().getRazorpayKeySecret());
        contentValues.put(PAYMENT_CONFIG_PAYPAL_ENABLE, configuration.getPaymentConfig().getPaypalEnable());
        contentValues.put(PAYMENT_CONFIG_STRIPE_ENABLE, configuration.getPaymentConfig().getStripeEnable());
        contentValues.put(PAYMENT_CONFIG_PAYMENTWALL_ENABLE, configuration.getPaymentConfig().getPaymentwallEnable());
        contentValues.put(PAYMENT_CONFIG_RAZORPAY_ENABLE, configuration.getPaymentConfig().getRazorpayEnable());
        //contentValues.put(PAYMENT_CONFIG_RAZORPAY_EXCHANGE_RATE, configuration.getPaymentConfig().getRazorpayExchangeRate());

        long id = db.insert(CONFIG_TABLE_NAME, null, contentValues);
        db.close();

        return id;
    }

    public Configuration getConfigurationData() {
        SQLiteDatabase db = this.getReadableDatabase();
        Configuration configuration = new Configuration();
        AppConfig appConfig = new AppConfig();
        AdsConfig adsConfig = new AdsConfig();
        PaymentConfig paymentConfig = new PaymentConfig();

        Cursor cursor = db.rawQuery("SELECT * FROM " + CONFIG_TABLE_NAME, null);

        if (cursor != null)
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {

                    appConfig.setMenu(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_MENU)));
                    appConfig.setProgramGuideEnable(cursor.getInt(cursor.getColumnIndex(CONFIG_COLUMN_PROGRAM_GUIDE_ENABLE)) > 0);
                    appConfig.setMandatoryLogin(cursor.getInt(cursor.getColumnIndex(CONFIG_COLUMN_MANDATORY_LOGIN)) > 0);
                    appConfig.setGenreVisible(cursor.getInt(cursor.getColumnIndex(CONFIG_COLUMN_GENRE_SHOW)) > 0);
                    appConfig.setCountryVisible(cursor.getInt(cursor.getColumnIndex(CONFIG_COLUMN_COUNTRY_SHOW)) > 0);

                    adsConfig.setAdsEnable(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_ADS_ENABLE)));
                    adsConfig.setMobileAdsNetwork(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_AD_NETWOTK_NAME)));
                    adsConfig.setAdmobAppId(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_ADMOB_APP_ID)));
                    adsConfig.setAdmobBannerAdsId(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_ADMOB_BANNER_ID)));
                    adsConfig.setAdmobInterstitialAdsId(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_ADMOB_INTERSTITIAL_ID)));
                    adsConfig.setAdmobNativeAdsId(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_ADMOB_NATIVE_ID)));
                    adsConfig.setAdmobRewardedVideoAdsId(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_ADMOB_REWARDEDVIDEO_ID)));
                    //adsConfig.setAdmobAppOpenAdsId(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_ADMOB_APPOPEN_ID)));
                    adsConfig.setFanNativeAdsPlacementId(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_FAN_NATIVE_ID)));
                    adsConfig.setFanBannerAdsPlacementId(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_FAN_BANNER_ID)));
                    adsConfig.setFanInterstitialAdsPlacementId(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_FAN_INTERSTITIAL_ID)));
                    adsConfig.setStartappAppId(cursor.getString(cursor.getColumnIndex(CONFIG_COLUMN_STARTAPP_ID)));

                    paymentConfig.setCurrencySymbol(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_CURRENCY_SYMBOL)));
                    paymentConfig.setCurrency(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_CURRENCY)));
                    paymentConfig.setPaypalEmail(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_PAYPAL_EMAIL)));
                    paymentConfig.setPaypalClientId(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_PAYPAL_CLIENT_ID)));
                    paymentConfig.setStripePublishableKey(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_STRIPE_PUBLISH_KEY)));
                    paymentConfig.setStripeSecretKey(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_STRIPE_SECRET_KEY)));
                    paymentConfig.setPaymentwallProjectKey(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_PAYMENTWALL_PROJECTT_KEY)));
                    paymentConfig.setPaymentwallSecretKey(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_PAYMENTWALL_SECRET_KEY)));
                    paymentConfig.setExchangeRate(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_EXCHANGE_RATE)));
                    paymentConfig.setRazorpayKeyId(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_RAZOR_PAY_KEY_ID)));
                    paymentConfig.setRazorpayKeySecret(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_RAZOR_PAY_KEY_SECRETE)));
                    paymentConfig.setPaypalEnable(cursor.getInt(cursor.getColumnIndex(PAYMENT_CONFIG_PAYPAL_ENABLE)) > 0);
                    paymentConfig.setStripeEnable(cursor.getInt(cursor.getColumnIndex(PAYMENT_CONFIG_STRIPE_ENABLE)) > 0);
                    paymentConfig.setPaymentwallEnable(cursor.getInt(cursor.getColumnIndex(PAYMENT_CONFIG_PAYMENTWALL_ENABLE)) > 0);
                    paymentConfig.setRazorpayEnable(cursor.getInt(cursor.getColumnIndex(PAYMENT_CONFIG_RAZORPAY_ENABLE)) > 0);
                    // paymentConfig.setRazorpayExchangeRate(cursor.getString(cursor.getColumnIndex(PAYMENT_CONFIG_RAZORPAY_EXCHANGE_RATE)));

                    cursor.moveToNext();
                }

                configuration.setAppConfig(appConfig);
                configuration.setAdsConfig(adsConfig);
                configuration.setPaymentConfig(paymentConfig);
            }

        cursor.close();
        return configuration;
    }

    public int getConfigurationCount() {
        String countQuery = "SELECT  * FROM " + CONFIG_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public void deleteAllAppConfig() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + CONFIG_TABLE_NAME);
        db.close();
    }

    public int updateConfigurationData(Configuration configuration, long id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();

        contentValues.put(CONFIG_COLUMN_MENU, configuration.getAppConfig().getMenu());
        contentValues.put(CONFIG_COLUMN_PROGRAM_GUIDE_ENABLE, configuration.getAppConfig().getProgramGuideEnable());
        contentValues.put(CONFIG_COLUMN_MANDATORY_LOGIN, configuration.getAppConfig().getMandatoryLogin());
        contentValues.put(CONFIG_COLUMN_GENRE_SHOW, configuration.getAppConfig().getGenreVisible());
        contentValues.put(CONFIG_COLUMN_COUNTRY_SHOW, configuration.getAppConfig().getCountryVisible());

        contentValues.put(CONFIG_COLUMN_ADS_ENABLE, configuration.getAdsConfig().getAdsEnable());
        contentValues.put(CONFIG_COLUMN_AD_NETWOTK_NAME, configuration.getAdsConfig().getMobileAdsNetwork());
        contentValues.put(CONFIG_COLUMN_ADMOB_APP_ID, configuration.getAdsConfig().getAdmobAppId());
        contentValues.put(CONFIG_COLUMN_ADMOB_BANNER_ID, configuration.getAdsConfig().getAdmobBannerAdsId());
        contentValues.put(CONFIG_COLUMN_ADMOB_INTERSTITIAL_ID, configuration.getAdsConfig().getAdmobInterstitialAdsId());
        contentValues.put(CONFIG_COLUMN_ADMOB_NATIVE_ID, configuration.getAdsConfig().getAdmobNativeAdsId());
        contentValues.put(CONFIG_COLUMN_ADMOB_REWARDEDVIDEO_ID, configuration.getAdsConfig().getAdmobRewardedVideoAdsId());
        //contentValues.put(CONFIG_COLUMN_ADMOB_APPOPEN_ID, configuration.getAdsConfig().getAdmobAppOpenAdsId());
        contentValues.put(CONFIG_COLUMN_FAN_BANNER_ID, configuration.getAdsConfig().getFanBannerAdsPlacementId());
        contentValues.put(CONFIG_COLUMN_FAN_NATIVE_ID, configuration.getAdsConfig().getFanNativeAdsPlacementId());
        contentValues.put(CONFIG_COLUMN_FAN_INTERSTITIAL_ID, configuration.getAdsConfig().getFanInterstitialAdsPlacementId());
        contentValues.put(CONFIG_COLUMN_STARTAPP_ID, configuration.getAdsConfig().getStartappAppId());

        contentValues.put(PAYMENT_CONFIG_CURRENCY_SYMBOL, configuration.getPaymentConfig().getCurrencySymbol());
        contentValues.put(PAYMENT_CONFIG_PAYPAL_EMAIL, configuration.getPaymentConfig().getPaypalEmail());
        contentValues.put(PAYMENT_CONFIG_PAYPAL_CLIENT_ID, configuration.getPaymentConfig().getPaypalClientId());
        contentValues.put(PAYMENT_CONFIG_STRIPE_PUBLISH_KEY, configuration.getPaymentConfig().getStripePublishableKey());
        contentValues.put(PAYMENT_CONFIG_STRIPE_SECRET_KEY, configuration.getPaymentConfig().getStripeSecretKey());
        contentValues.put(PAYMENT_CONFIG_PAYMENTWALL_PROJECTT_KEY, configuration.getPaymentConfig().getPaymentwallProjectKey());
        contentValues.put(PAYMENT_CONFIG_PAYMENTWALL_SECRET_KEY, configuration.getPaymentConfig().getPaymentwallSecretKey());
        contentValues.put(PAYMENT_CONFIG_CURRENCY, configuration.getPaymentConfig().getCurrency());
        contentValues.put(PAYMENT_CONFIG_EXCHANGE_RATE, configuration.getPaymentConfig().getExchangeRate());
        contentValues.put(PAYMENT_CONFIG_RAZOR_PAY_KEY_ID, configuration.getPaymentConfig().getRazorpayKeyId());
        contentValues.put(PAYMENT_CONFIG_RAZOR_PAY_KEY_SECRETE, configuration.getPaymentConfig().getRazorpayKeySecret());
        contentValues.put(PAYMENT_CONFIG_PAYPAL_ENABLE, configuration.getPaymentConfig().getPaypalEnable());
        contentValues.put(PAYMENT_CONFIG_STRIPE_ENABLE, configuration.getPaymentConfig().getStripeEnable());
        contentValues.put(PAYMENT_CONFIG_PAYMENTWALL_ENABLE, configuration.getPaymentConfig().getPaymentwallEnable());
        contentValues.put(PAYMENT_CONFIG_RAZORPAY_ENABLE, configuration.getPaymentConfig().getRazorpayEnable());
        // contentValues.put(PAYMENT_CONFIG_RAZORPAY_EXCHANGE_RATE, configuration.getPaymentConfig().getRazorpayExchangeRate());

        // updating row
        return db.update(CONFIG_TABLE_NAME, contentValues, CONFIG_COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});
    }


    //subscription table
    private String CREATE_SUBSCRIPTION_STATUS_TABLE() {
        return "CREATE TABLE IF NOT EXISTS " + SUBS_TABLE_NAME +
                " (" + SUBS_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                SUBS_COLUMN_STATUS + " TEXT," +
                SUBS_COLUMN_PACKAGE_TITLE + " TEXT," +
                SUBS_COLUMN_EXPIRE_TIME + " INTEGER," +
                SUBS_COLUMN_EXPIRE_DATE + " TEXT" + ")";
    }

    public long insertActiveStatusData(ActiveStatus activeStatus) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(SUBS_COLUMN_STATUS, activeStatus.getStatus());
        contentValues.put(SUBS_COLUMN_PACKAGE_TITLE, activeStatus.getPackageTitle());
        contentValues.put(SUBS_COLUMN_EXPIRE_DATE, activeStatus.getExpireDate());
        contentValues.put(SUBS_COLUMN_EXPIRE_TIME, PreferenceUtils.getExpireTime());

        long id = db.insert(SUBS_TABLE_NAME, null, contentValues);
        db.close();
        return id;
    }

    public ActiveStatus getActiveStatusData() {
        SQLiteDatabase db = this.getReadableDatabase();
        ActiveStatus activeStatus = new ActiveStatus();

        Cursor cursor = db.rawQuery("SELECT * FROM " + SUBS_TABLE_NAME, null);

        if (cursor != null)
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    // prepare appConfig object
                    activeStatus.setStatus(cursor.getString(cursor.getColumnIndex(SUBS_COLUMN_STATUS)));
                    activeStatus.setPackageTitle(cursor.getString(cursor.getColumnIndex(SUBS_COLUMN_PACKAGE_TITLE)));
                    activeStatus.setExpireDate(cursor.getString(cursor.getColumnIndex(SUBS_COLUMN_EXPIRE_DATE)));
                    activeStatus.setExpireTime(cursor.getLong(cursor.getColumnIndex(SUBS_COLUMN_EXPIRE_TIME)));

                    cursor.moveToNext();
                }
            }

        // close the db connection
        cursor.close();
        return activeStatus;
    }

    public void deleteAllActiveStatusData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + SUBS_TABLE_NAME);
        db.close();
    }

    public int getActiveStatusCount() {
        String countQuery = "SELECT  * FROM " + SUBS_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public int updateActiveStatus(ActiveStatus activeStatus, long row) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues contentValues = new ContentValues();
        contentValues.put(SUBS_COLUMN_STATUS, activeStatus.getStatus());
        contentValues.put(SUBS_COLUMN_PACKAGE_TITLE, activeStatus.getPackageTitle());
        contentValues.put(SUBS_COLUMN_EXPIRE_DATE, activeStatus.getExpireDate());
        contentValues.put(SUBS_COLUMN_EXPIRE_TIME, PreferenceUtils.getExpireTime());

        // updating row
        return db.update(SUBS_TABLE_NAME, contentValues, SUBS_COLUMN_ID + " = ?",
                new String[]{String.valueOf(row)});
    }

    //user data table
    private String CREATE_USER_DATA_TABLE() {
        return "CREATE TABLE IF NOT EXISTS " + USER_TABLE_NAME +
                " (" + USER_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                USER_COLUMN_NAME + " TEXT," +
                USER_COLUMN_EMAIL + " TEXT," +
                //  USER_COLUMN_PHONE + " TEXT," +
                USER_COLUMN_STATUS + " TEXT," +
                USER_COLUMN_PROFILE_IMAGE_URL + " TEXT," +
                USER_COLUMN_USER_ID + " TEXT" + ")"
                ;
    }

    public long insertUserData(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_COLUMN_NAME, user.getName());
        contentValues.put(USER_COLUMN_EMAIL, user.getEmail());
        // contentValues.put(USER_COLUMN_PHONE, user.getPhone());
        //contentValues.put(USER_COLUMN_STATUS, user.getStatus());
        contentValues.put(USER_COLUMN_PROFILE_IMAGE_URL, user.getImageUrl());
        contentValues.put(USER_COLUMN_USER_ID, user.getUserId());

        long id = db.insert(USER_TABLE_NAME, null, contentValues);
        db.close();
        return id;
    }

    public User getUserData() {
        // get readable database as we are not inserting anything
        SQLiteDatabase db = this.getReadableDatabase();
        User user = new User();

        Cursor cursor = db.rawQuery("SELECT * FROM " + USER_TABLE_NAME, null);

        if (cursor != null)
            if (cursor.moveToFirst()) {
                while (!cursor.isAfterLast()) {
                    // prepare appConfig object
                    user.setUserId(cursor.getString(cursor.getColumnIndex(USER_COLUMN_USER_ID)));
                    user.setName(cursor.getString(cursor.getColumnIndex(USER_COLUMN_NAME)));
                    user.setEmail(cursor.getString(cursor.getColumnIndex(USER_COLUMN_EMAIL)));
                    //    user.setPhone(cursor.getString(cursor.getColumnIndex(USER_COLUMN_PHONE)));
                    user.setImageUrl(cursor.getString(cursor.getColumnIndex(USER_COLUMN_PROFILE_IMAGE_URL)));
                    user.setStatus(cursor.getString(cursor.getColumnIndex(USER_COLUMN_STATUS)));

                    cursor.moveToNext();
                }
            }

        // close the db connection
        cursor.close();
        return user;

    }

    public long updateUserData(User user, long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(USER_COLUMN_NAME, user.getName());
        contentValues.put(USER_COLUMN_EMAIL, user.getEmail());
        //contentValues.put(USER_COLUMN_PHONE, user.getPhone());
        contentValues.put(USER_COLUMN_USER_ID, user.getUserId());
        contentValues.put(USER_COLUMN_PROFILE_IMAGE_URL, user.getImageUrl());
        contentValues.put(USER_COLUMN_STATUS, user.getStatus());

        // updating row
        return db.update(USER_TABLE_NAME, contentValues, USER_COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)});

    }

    public void deleteUserData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + USER_TABLE_NAME);
        db.close();
    }

    public int getUserDataCount() {
        String countQuery = "SELECT  * FROM " + USER_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    private String CREATE_DOWNLOAD_DATA_TABLE() {
        return "CREATE TABLE IF NOT EXISTS " + DOWNLOAD_TABLE_NAME +
                " (" + DOWNLOAD_COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WORK_ID + " TEXT," +
                DOWNLOAD_ID + " INTEGER," +
                TOTAL_SIZE + " TEXT," +
                DOWNLOAD_SIZE + " TEXT," +
                URL + " TEXT," +
                URL_SUB_LIST + " TEXT," +
                FILE_NAME + " TEXT," +
                APP_CLOSE_STATUS + " TEXT," +
                DOWNLOAD_STATUS + " TEXT" + ")";
    }

    public int getDownloadDataCount() {
        String countQuery = "SELECT  * FROM " + DOWNLOAD_TABLE_NAME;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(countQuery, null);

        int count = cursor.getCount();
        cursor.close();

        // return count
        return count;
    }

    public long insertWork(Work work) {
        // get writable database as we want to write data
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        // `id` and `timestamp` will be inserted automatically.
        // no need to add them
        values.put(WORK_ID, work.getWorkId());
        values.put(DOWNLOAD_ID, work.getDownloadId());
        values.put(TOTAL_SIZE, work.getTotalSize());
        values.put(DOWNLOAD_SIZE, work.getDownloadSize());
        values.put(URL, work.getUrl());
        values.put(URL_SUB_LIST, work.getSubListJson());
        values.put(FILE_NAME, work.getFileName());
        values.put(APP_CLOSE_STATUS, work.getAppCloseStatus());
        values.put(DOWNLOAD_STATUS, work.getDownloadStatus());
        // insert row
        long id = db.insert(DOWNLOAD_TABLE_NAME, null, values);
        // close db connection
        db.close();
        // return newly inserted row id
        return id;
    }

    public int updateWork(Work work) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WORK_ID, work.getWorkId());
        values.put(DOWNLOAD_ID, work.getDownloadId());
        values.put(TOTAL_SIZE, work.getTotalSize());
        values.put(DOWNLOAD_SIZE, work.getDownloadSize());
        values.put(URL, work.getUrl());
        values.put(URL_SUB_LIST, work.getSubListJson());
        values.put(FILE_NAME, work.getFileName());
        values.put(APP_CLOSE_STATUS, work.getAppCloseStatus());
        values.put(DOWNLOAD_STATUS, work.getDownloadStatus());

        Log.d("workId 2:", work.getWorkId());

        // updating row
        return db.update(DOWNLOAD_TABLE_NAME, values, WORK_ID + " = ?",
                new String[]{work.getWorkId()});
    }

    public void deleteByDownloadId(int downloadId) {
        String sql = "DELETE FROM " + DOWNLOAD_TABLE_NAME + " WHERE " + DOWNLOAD_ID + "=" + downloadId;
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(sql);
    }

    public void deleteAllDownloadData() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from " + DOWNLOAD_TABLE_NAME);
        db.close();
    }

    public Work getWorkByDownloadId(int downloadId) {
        String sql = "SELECT * FROM " + DOWNLOAD_TABLE_NAME + " WHERE " + DOWNLOAD_ID + "=" + downloadId;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(sql, null);
        Work work = new Work();
        if (cursor.moveToFirst()) {

            work.setId(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_COLUMN_ID)));
            work.setWorkId(cursor.getString(cursor.getColumnIndex(WORK_ID)));
            work.setDownloadId(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_ID)));
            work.setFileName(cursor.getString(cursor.getColumnIndex(FILE_NAME)));
            work.setTotalSize(cursor.getString(cursor.getColumnIndex(TOTAL_SIZE)));
            work.setDownloadSize(cursor.getString(cursor.getColumnIndex(DOWNLOAD_SIZE)));
            work.setDownloadStatus(cursor.getString(cursor.getColumnIndex(DOWNLOAD_STATUS)));
            work.setUrl(cursor.getString(cursor.getColumnIndex(URL)));
            work.setSubListJon(cursor.getString(cursor.getColumnIndex(URL_SUB_LIST)));
            work.setAppCloseStatus(cursor.getString(cursor.getColumnIndex(APP_CLOSE_STATUS)));
        }
        return work;
    }
    public List<Work> getAllWork() {
        List<Work> works = new ArrayList<>();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + DOWNLOAD_TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (cursor != null)
            if (cursor.moveToFirst()) {
                do {
                    Work work = new Work();
                    work.setId(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_COLUMN_ID)));
                    work.setWorkId(cursor.getString(cursor.getColumnIndex(WORK_ID)));
                    work.setDownloadId(cursor.getInt(cursor.getColumnIndex(DOWNLOAD_ID)));
                    work.setFileName(cursor.getString(cursor.getColumnIndex(FILE_NAME)));
                    work.setTotalSize(cursor.getString(cursor.getColumnIndex(TOTAL_SIZE)));
                    work.setDownloadSize(cursor.getString(cursor.getColumnIndex(DOWNLOAD_SIZE)));
                    work.setDownloadStatus(cursor.getString(cursor.getColumnIndex(DOWNLOAD_STATUS)));
                    work.setUrl(cursor.getString(cursor.getColumnIndex(URL)));
                    work.setSubListJon(cursor.getString(cursor.getColumnIndex(URL_SUB_LIST)));
                    work.setAppCloseStatus(cursor.getString(cursor.getColumnIndex(APP_CLOSE_STATUS)));
                    works.add(work);
                } while (cursor.moveToNext());
            }

        // close db connection
        db.close();

        // return works list
        return works;
    }
}
