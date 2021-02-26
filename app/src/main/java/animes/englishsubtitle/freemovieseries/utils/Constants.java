package animes.englishsubtitle.freemovieseries.utils;

import android.content.Context;
import android.os.Environment;

import java.io.File;
import java.util.List;

import animes.englishsubtitle.freemovieseries.models.single_details.Country;
import animes.englishsubtitle.freemovieseries.models.single_details.Genre;
import animes.englishsubtitle.freemovieseries.network.model.TvCategory;

public class Constants {

    public static final String ADMOB = "admob";
    public static final String START_APP = "startApp";
    public static final String NETWORK_AUDIENCE = "fan";

    public static String workId;

    //public static String DOWNLOAD_DIR = Environment.getExternalStorageDirectory().toString()+File.separator + Environment.DIRECTORY_DOWNLOADS + File.separator;

    public static String getDownloadDir(Context context) {
        return context.getDir(Environment.DIRECTORY_DOWNLOADS,context.MODE_PRIVATE).toString() + File.separator;
    }

    public static final String USER_LOGIN_STATUS = "login_status";

    public static List<Genre> genreList = null;
    public static List<Country> countryList = null;
    public static List<TvCategory> tvCategoryList = null;

    //room related constants
    public static final String ROOM_DB_NAME = "continue_watching_db";

    public static final String CONTENT_ID = "content_id";
    public static final String CONTENT_TITLE = "title";
    public static final String IMAGE_URL = "image_url";
    public static final String PROGRESS = "progress";
    public static final String POSITION ="position";
    public static final String STREAM_URL = "stream_url";
    public static final String CATEGORY_TYPE = "category_type";
    public static final String SERVER_TYPE = "server_type";
    public static final String IS_FROM_CONTINUE_WATCHING = "continue_watching_bool";
    public static final String YOUTUBE = "youtube";
    public static final String YOUTUBE_LIVE = "youtube_live";


}
