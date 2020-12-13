package tvseries.koreandramaengsub.freemovieapp;

public class Config {

    // copy your api url from php admin dashboard & paste below
    public static final String API_SERVER_URL = "https://koreandrama.cdramacollections.com/rest-api/";

    //copy your api key from php admin dashboard & paste below
    public static final String API_KEY = "avbze8bhmfod3zxy973be4ob";

    //copy your terms url from php admin dashboard & paste below
    public static final String TERMS_URL = "https://koreandrama.cdramacollections.com/terms/";

    // download option for non subscribed user
    public static final boolean ENABLE_DOWNLOAD_TO_ALL = true;

    //enable RTL
    public static boolean ENABLE_RTL = true;

    public static Boolean isOpenChildFragment = false;

    //youtube video auto play
    public static boolean YOUTUBE_VIDEO_AUTO_PLAY = false;

    //enable external player
    public static final boolean ENABLE_EXTERNAL_PLAYER = false;

    //default theme
    public static boolean DEFAULT_DARK_THEME_ENABLE = false;

    // First, you have to configure firebase to enable facebook, phone and google login
    // facebook authentication
    public static final boolean ENABLE_FACEBOOK_LOGIN = false;

    //Phone authentication
    public static final boolean ENABLE_PHONE_LOGIN = true;

    //Google authentication
    public static final boolean ENABLE_GOOGLE_LOGIN = true;



/*
    // copy your api url from php admin dashboard & paste below
    public static final String API_SERVER_URL = "http://192.168.1.60/v120_test/api/v100/";

    //copy your api key from php admin dashboard & paste below
    public static final String API_KEY = "c550f8f5e59f449";

    //copy your api username and password from php admin dashboard & paste below
    public static final String API_USER_NAME = "Admin";
    public static final String API_PASSWORD = "022400dffec5b44";

    //copy your terms url from php admin dashboard & paste below
    public static final String TERMS_URL = "https://spagreen.net/tearms-of-services/";

    //youtube video auto play
    public static boolean YOUTUBE_VIDEO_AUTO_PLAY = false;

    //default theme
    public static boolean DEFAULT_DARK_THEME_ENABLE = true;

    // First, you have to configure firebase to enable facebook, phone and google login
    // facebook authentication
    public static final boolean ENABLE_FACEBOOK_LOGIN = true;

    //Phone authentication
    public static final boolean ENABLE_PHONE_LOGIN = true;

    //Google authentication
    public static final boolean ENABLE_GOOGLE_LOGIN = true;

    */
}
