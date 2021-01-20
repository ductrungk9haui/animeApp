package tvseries.koreandramaengsub.freemovieapp;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.mediarouter.app.MediaRouteButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.work.Data;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.balysv.materialripple.MaterialRippleLayout;
import com.downloader.PRDownloader;
import com.downloader.Status;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.cast.CastPlayer;
import com.google.android.exoplayer2.ext.rtmp.RtmpDataSourceFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.MergingMediaSource;
import com.google.android.exoplayer2.source.SingleSampleMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.Util;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.google.android.gms.cast.MediaInfo;
import com.google.android.gms.cast.MediaMetadata;
import com.google.android.gms.cast.MediaQueueItem;
import com.google.android.gms.cast.MediaStatus;
import com.google.android.gms.cast.framework.CastButtonFactory;
import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.cast.framework.CastSession;
import com.google.android.gms.cast.framework.CastState;
import com.google.android.gms.cast.framework.media.RemoteMediaClient;
import com.google.android.gms.common.images.WebImage;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import at.huber.youtubeExtractor.VideoMeta;
import at.huber.youtubeExtractor.YouTubeExtractor;
import at.huber.youtubeExtractor.YtFile;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import tvseries.koreandramaengsub.freemovieapp.adapters.CastCrewAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.CommentsAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.DownloadAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.EpisodeAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.HomePageAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.ProgramAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.RelatedTvAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.ServerAdapter;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.database.continueWatching.ContinueWatchingModel;
import tvseries.koreandramaengsub.freemovieapp.database.continueWatching.ContinueWatchingViewModel;
import tvseries.koreandramaengsub.freemovieapp.models.CastCrew;
import tvseries.koreandramaengsub.freemovieapp.models.CommonModels;
import tvseries.koreandramaengsub.freemovieapp.models.EpiModel;
import tvseries.koreandramaengsub.freemovieapp.models.GetCommentsModel;
import tvseries.koreandramaengsub.freemovieapp.models.PostCommentModel;
import tvseries.koreandramaengsub.freemovieapp.models.Program;
import tvseries.koreandramaengsub.freemovieapp.models.SubtitleModel;
import tvseries.koreandramaengsub.freemovieapp.models.Work;
import tvseries.koreandramaengsub.freemovieapp.models.single_details.Cast;
import tvseries.koreandramaengsub.freemovieapp.models.single_details.Director;
import tvseries.koreandramaengsub.freemovieapp.models.single_details.DownloadLink;
import tvseries.koreandramaengsub.freemovieapp.models.single_details.Episode;
import tvseries.koreandramaengsub.freemovieapp.models.single_details.Genre;
import tvseries.koreandramaengsub.freemovieapp.models.single_details.RelatedMovie;
import tvseries.koreandramaengsub.freemovieapp.models.single_details.Season;
import tvseries.koreandramaengsub.freemovieapp.models.single_details.SingleDetails;
import tvseries.koreandramaengsub.freemovieapp.models.single_details.Subtitle;
import tvseries.koreandramaengsub.freemovieapp.models.single_details.Video;
import tvseries.koreandramaengsub.freemovieapp.models.single_details_tv.AdditionalMediaSource;
import tvseries.koreandramaengsub.freemovieapp.models.single_details_tv.AllTvChannel;
import tvseries.koreandramaengsub.freemovieapp.models.single_details_tv.ProgramGuide;
import tvseries.koreandramaengsub.freemovieapp.models.single_details_tv.SingleDetailsTV;
import tvseries.koreandramaengsub.freemovieapp.network.RetrofitClient;
import tvseries.koreandramaengsub.freemovieapp.network.apis.CommentApi;
import tvseries.koreandramaengsub.freemovieapp.network.apis.FavouriteApi;
import tvseries.koreandramaengsub.freemovieapp.network.apis.SingleDetailsApi;
import tvseries.koreandramaengsub.freemovieapp.network.apis.SingleDetailsTVApi;
import tvseries.koreandramaengsub.freemovieapp.network.apis.SubscriptionApi;
import tvseries.koreandramaengsub.freemovieapp.network.model.ActiveStatus;
import tvseries.koreandramaengsub.freemovieapp.network.model.FavoriteModel;
import tvseries.koreandramaengsub.freemovieapp.network.model.config.AdsConfig;
import tvseries.koreandramaengsub.freemovieapp.service.DownloadWorkManager;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.PreferenceUtils;
import tvseries.koreandramaengsub.freemovieapp.utils.RtlUtils;
import tvseries.koreandramaengsub.freemovieapp.utils.ToastMsg;
import tvseries.koreandramaengsub.freemovieapp.utils.Tools;
import tvseries.koreandramaengsub.freemovieapp.utils.ads.BannerAds;
import tvseries.koreandramaengsub.freemovieapp.utils.ads.PopUpAds;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.android.gms.ads.AdActivity.CLASS_NAME;
import static tvseries.koreandramaengsub.freemovieapp.utils.Constants.CATEGORY_TYPE;
import static tvseries.koreandramaengsub.freemovieapp.utils.Constants.CONTENT_ID;
import static tvseries.koreandramaengsub.freemovieapp.utils.Constants.CONTENT_TITLE;
import static tvseries.koreandramaengsub.freemovieapp.utils.Constants.IMAGE_URL;
import static tvseries.koreandramaengsub.freemovieapp.utils.Constants.IS_FROM_CONTINUE_WATCHING;
import static tvseries.koreandramaengsub.freemovieapp.utils.Constants.POSITION;
import static tvseries.koreandramaengsub.freemovieapp.utils.Constants.SERVER_TYPE;
import static tvseries.koreandramaengsub.freemovieapp.utils.Constants.STREAM_URL;

public class DetailsActivity extends AppCompatActivity implements CastPlayer.SessionAvailabilityListener, ProgramAdapter.OnProgramClickListener, EpisodeAdapter.OnTVSeriesEpisodeItemClickListener,
        RelatedTvAdapter.RelatedTvClickListener {
    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final int PRELOAD_TIME_S = 20;
    public static final String TAG = DetailsActivity.class.getSimpleName();

    @BindView(R.id.adView)
    RelativeLayout mAdView;
    @BindView(R.id.llbottom)
    LinearLayout mLLBottom;
    @BindView(R.id.tv_details)
    TextView mTvDes;
    @BindView(R.id.tv_release_date)
    TextView mTvRelease;
    @BindView(R.id.text_name)
    TextView mTvName;
    @BindView(R.id.text_imdb)
    TextView mTvImdb;
    @BindView(R.id.tv_director)
    TextView mTvDirector;
    @BindView(R.id.tv_genre)
    TextView mTvGenre;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.add_fav)
    ImageView mImgAddFav;
    @BindView(R.id.add_fav2)
    ImageView mImgAddFav2;
    @BindView(R.id.img_back)
    ImageView mImgBack;
    @BindView(R.id.webView)
    WebView mWebView;
    @BindView(R.id.progressBar)
    ProgressBar mProgressBar;
    @BindView(R.id.play)
    RelativeLayout mLPlay;
    @BindView(R.id.rv_related)
    RecyclerView mRvRelated;
    @BindView(R.id.tv_related)
    TextView mTvRelated;
    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout mShimmerLayout;
    @BindView(R.id.btn_comment)
    Button mBtnComment;
    @BindView(R.id.et_comment)
    EditText mEtComment;
    @BindView(R.id.recyclerView_comment)
    RecyclerView mRvComment;
    @BindView(R.id.llcomments)
    RelativeLayout mLLcomment;
    @BindView(R.id.video_view)
    PlayerView mSimpleExoPlayerView;
    @BindView(R.id.player_layout)
    View mPlayerLayout;
    @BindView(R.id.img_full_scr)
    ImageView mImgFull;
    @BindView(R.id.external_player_iv)
    ImageView mExternalPlayerIv;
    @BindView(R.id.volumn_layout)
    LinearLayout mVolumnControlLayout;
    @BindView(R.id.volumn_seekbar)
    SeekBar mVolumnSeekbar;
    @BindView(R.id.rv_server_list)
    RecyclerView mRvServer;
    @BindView(R.id.season_spinner)
    Spinner mSeasonSpinner;
    @BindView(R.id.spinner_container)
    RelativeLayout mSeasonSpinnerContainer;
    @BindView(R.id.img_subtitle)
    ImageView mImgSubtitle;
    @BindView(R.id.media_route_button)
    MediaRouteButton mMediaRouteButton;
    @BindView(R.id.chrome_cast_tv)
    TextView mChromeCastTv;
    @BindView(R.id.cast_control_view)
    PlayerControlView mCastControlView;
    @BindView(R.id.tv_layout)
    LinearLayout mTvLayout;
    @BindView(R.id.p_shedule_layout)
    LinearLayout mSheduleLayout;
    @BindView(R.id.tv_title_tv)
    TextView mTvTitleTv;
    @BindView(R.id.program_guide_rv)
    RecyclerView mProgramRv;
    @BindView(R.id.tv_top_layout)
    LinearLayout mTvTopLayout;
    @BindView(R.id.tv_thumb_iv)
    ImageView mTvThumbIv;
    @BindView(R.id.watch_status_tv)
    TextView mWatchStatusTv;
    @BindView(R.id.time_tv)
    TextView mTimeTv;
    @BindView(R.id.program_type_tv)
    TextView mProgramTv;
    @BindView(R.id.rewind_layout)
    LinearLayout mExoRewind;
    @BindView(R.id.forward_layout)
    LinearLayout mExoForward;
    @BindView(R.id.seekbar_layout)
    LinearLayout mSeekbarLayout;
    @BindView(R.id.live_tv)
    TextView mLiveTv;
    @BindView(R.id.cast_rv)
    RecyclerView mCastRv;
    @BindView(R.id.pro_guide_tv)
    TextView mProGuideTv;
    @BindView(R.id.watch_live_tv)
    TextView mWatchLiveTv;
    @BindView(R.id.content_details)
    RelativeLayout mContentDetails;
    @BindView(R.id.subscribe_layout)
    LinearLayout mSubscriptionLayout;
    @BindView(R.id.subscribe_bt)
    Button mSubscribeBt;
    @BindView(R.id.topbar)
    LinearLayout mTopBarLayout;
    @BindView(R.id.description_layout)
    RelativeLayout mDescriptionLayout;
    @BindView(R.id.lyt_parent)
    MaterialRippleLayout mDescriptionContainer;
    @BindView(R.id.watch_now_bt)
    Button mWatchNowBt;
    @BindView(R.id.download_bt)
    Button mDownloadBt;
    @BindView(R.id.poster_iv)
    ImageView mPosterIv;
    @BindView(R.id.image_thumb)
    ImageView mThumbIv;
    @BindView(R.id.genre_tv)
    TextView mDGenryTv;
    @BindView(R.id.img_server)
    ImageView mServerIv;
    @BindView(R.id.series_layout)
    RelativeLayout mSeriesLayout;
    @BindView(R.id.seriest_title_tv)
    TextView mSeriesTitleTv;
    @BindView(R.id.linear_share)
    View mTopShareLayout;
    private Unbinder mUnbinder;
    private ContinueWatchingViewModel mContinueViewModel;
    private boolean isFromContinueWatching = false;
    private long resumePosition = 0L;
    private static long playerCurrentPosition = 0L;
    private static long mediaDuration = 0L;

    private EpisodeAdapter mEpisodeAdapter;
    private ServerAdapter mServerAdapter;
    private HomePageAdapter mRelatedAdapter;
    private RelatedTvAdapter mRelatedTvAdapter;
    private CastCrewAdapter mCastCrewAdapter;
    private List<CommonModels> mListServer = new ArrayList<>();
    private List<CommonModels> mListRelated = new ArrayList<>();
    private List<GetCommentsModel> mListComment = new ArrayList<>();
    private List<CommonModels> mListDownload = new ArrayList<>();
    private List<CommonModels> mListInternalDownload = new ArrayList<>();
    private List<CommonModels> mListExternalDownload = new ArrayList<>();
    private List<CastCrew> mCastCrews = new ArrayList<>();
    private HashMap<String, Integer> mMapMovies = new HashMap<String, Integer>();
    private String mStrDirector = "", strCast = "", mStrGenre = "";
    private String mType = "", mId = "";
    private String V_URL = "";
    private boolean mIsFav = false;
    private CommentsAdapter mCommentsAdapter;
    public static SimpleExoPlayer mPlayer;
    private CastContext mCastContext;
    public static boolean mIsPlaying, mIsFullScr;
    private int mPlayerHeight;
    public static boolean mIsVideo = true;
    private FirebaseAnalytics mFirebaseAnalytics;
    private String mStrSubtitle = "Null";
    public static MediaSource mMediaSource = null;
    private List<SubtitleModel> mListSub = new ArrayList<>();
    private AlertDialog mAlertDialog;
    private String mMediaUrl;
    private boolean tv = false;
    private String mDownload_check = "";
    private String mSeason;
    private String mEpisod;
    private String movieTitle;
    private String mSeriesTitle;
    private CastPlayer mCastPlayer;
    private boolean mCastSession;
    private String mTitle, mImdb_rating;
    String mCastImageUrl;
    private ProgramAdapter mProgramAdapter;
    List<Program> mPrograms = new ArrayList<>();
    boolean mIsDark;
    private OrientationEventListener myOrientationEventListener;
    private String mServerType;
    private boolean fullScreenByClick;
    private String mCurrentProgramTime;
    private String mCurrentProgramTitle;
    private String mUserId;
    private String mYoutubeDownloadUr;
    private String mUrlType = "";
    private boolean mActiveMovie;
    private RelativeLayout mRlTouch;
    private boolean mIntLeft, mIntRight;
    private int sWidth, sHeight;
    private long diffX, diffY;
    private Display display;
    private Point size;
    private float downX, downY;
    private AudioManager mAudioManager;
    private int mAspectClickCount = 1;
    private DatabaseHelper mDBHelper;
    public boolean check_download = false;
    private static DetailsActivity instance;
    private RewardedAd mRewardedAd;
    public boolean mCheckExist = false;
    public boolean mCheckFailLink = false;
    public boolean mCheckFinish = false;
    boolean mIsLoading;
    private AdsConfig adsConfig;

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        mIsDark = sharedPreferences.getBoolean("dark", false);
        if (mIsDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        mUnbinder = ButterKnife.bind(this);
        instance = this;
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });
        // loadAdReward();
        mDBHelper = new DatabaseHelper(DetailsActivity.this);
        mAudioManager = (AudioManager) getApplicationContext().getSystemService(Context.AUDIO_SERVICE);
        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "details_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);

        if (!mDBHelper.getMapMovie().equals("")) {
            setMapMovies(mDBHelper.getMapMovie());
        }
        if (mIsDark) {
            mTvTopLayout.setBackgroundColor(getResources().getColor(R.color.black_window_light));
            mSheduleLayout.setBackground(getResources().getDrawable(R.drawable.rounded_black_transparent));
            mEtComment.setBackground(getResources().getDrawable(R.drawable.round_grey_transparent));
            mBtnComment.setTextColor(getResources().getColor(R.color.grey_20));
            mTopBarLayout.setBackgroundColor(getResources().getColor(R.color.dark));
            mSubscribeBt.setBackground(getResources().getDrawable(R.drawable.btn_rounded_dark));
            //tvName.setTextColor(getResources().getColor(R.color.black_window_light));
            mDescriptionContainer.setBackground(getResources().getDrawable(R.drawable.gradient_black_transparent));
        }

        // chrome cast
        CastButtonFactory.setUpMediaRouteButton(getApplicationContext(), mMediaRouteButton);
        mCastContext = CastContext.getSharedInstance(this);
        mCastPlayer = new CastPlayer(mCastContext);
        mCastPlayer.setSessionAvailabilityListener(this);

        // cast button will show if the cast device will be available
        if (mCastContext.getCastState() != CastState.NO_DEVICES_AVAILABLE)
            mMediaRouteButton.setVisibility(View.VISIBLE);
        // start the shimmer effect
        mShimmerLayout.startShimmer();
        mPlayerHeight = mLPlay.getLayoutParams().height;
        mProgressBar.setMax(100); // 100 maximum value for the progress value
        mProgressBar.setProgress(50);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptEnabled(true);
        mWebView.setWebChromeClient(new WebChromeClient());

        mType = getIntent().getStringExtra("vType");
        mId = getIntent().getStringExtra("id");
        mCastSession = getIntent().getBooleanExtra("castSession", false);

        // getting user login info for favourite button visibility
        mUserId = mDBHelper.getUserData().getUserId();
        if (PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
            mImgAddFav.setVisibility(VISIBLE);
            mImgAddFav2.setVisibility(VISIBLE);
        } else {
            mImgAddFav.setVisibility(GONE);
            mImgAddFav2.setVisibility(GONE);
        }
        mCommentsAdapter = new CommentsAdapter(this, mListComment);
        mRvComment.setLayoutManager(new LinearLayoutManager(this));
        mRvComment.setHasFixedSize(true);
        mRvComment.setNestedScrollingEnabled(false);
        mRvComment.setAdapter(mCommentsAdapter);
        getComments();
        if (!isNetworkAvailable()) {
            new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.no_internet));
        }
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                clear_previous();
                initGetData();
            }
        });
        //handle Continue watching task
        isFromContinueWatching = getIntent().getBooleanExtra(IS_FROM_CONTINUE_WATCHING, false);
        try {
            if (isFromContinueWatching) {
                //get info
                mId = getIntent().getStringExtra(CONTENT_ID);
                mType = getIntent().getStringExtra(CATEGORY_TYPE);
                mServerType = getIntent().getStringExtra(SERVER_TYPE);
                playerCurrentPosition = getIntent().getLongExtra(POSITION, 0);
                resumePosition = playerCurrentPosition;
                mTitle = getIntent().getStringExtra(CONTENT_TITLE);
                mCastImageUrl = getIntent().getStringExtra(IMAGE_URL);
                mMediaUrl = getIntent().getStringExtra(STREAM_URL);
                hideDescriptionLayout();
                showSeriesLayout();

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        mContinueViewModel = new ViewModelProvider(this).get(ContinueWatchingViewModel.class);
        loadAd();
    }

    public static DetailsActivity getInstance() {
        return instance;
    }

    private void loadRewardedAd() {
        if (mRewardedAd == null || !mRewardedAd.isLoaded()) {
            adsConfig = new DatabaseHelper(DetailsActivity.this).getConfigurationData().getAdsConfig();
            mRewardedAd = new RewardedAd(this, "ca-app-pub-3940256099942544/5224354917");
            mIsLoading = true;
            mRewardedAd.loadAd(
                    new AdRequest.Builder().build(),
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onRewardedAdLoaded() {
                            // Ad successfully loaded.
                            DetailsActivity.this.mIsLoading = false;
                            //Toast.makeText(DetailsActivity.this, "onRewardedAdLoaded", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                            // Ad failed to load.
                            DetailsActivity.this.mIsLoading = false;
//                            Toast.makeText(DetailsActivity.this, "onRewardedAdFailedToLoad", Toast.LENGTH_SHORT)
//                                    .show();
                        }
                    });
        }
    }

    public void showRewardedVideo() {
        //showVideoButton.setVisibility(View.INVISIBLE);
        if (mRewardedAd.isLoaded()) {
            RewardedAdCallback adCallback =
                    new RewardedAdCallback() {
                        @Override
                        public void onRewardedAdOpened() {
                            // Ad opened.
                            Toast.makeText(DetailsActivity.this, "Watch ads to download video", Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onRewardedAdClosed() {
                            // Ad closed.
                            //Toast.makeText(DetailsActivity.this, "onRewardedAdClosed", Toast.LENGTH_SHORT).show();
                            if (mCheckFinish != true) {
                                new ToastMsg(DetailsActivity.this).toastIconError("Oops! Please watch ads to start!");
                            }
                            // Preload the next video ad.
                            // DetailsActivity.this.loadRewardedAd();
                            mCheckFinish = false;
                        }

                        @Override
                        public void onUserEarnedReward(RewardItem rewardItem) {
                            // User earned reward.
                            //Toast.makeText(DetailsActivity.this, "onUserEarnedReward", Toast.LENGTH_SHORT).show();
                            if (mCheckFailLink) {
                                new ToastMsg(DetailsActivity.this).toastIconError("Fail Link, Plz tell us on facebook group to fix ASAP");
                            } else if (mCheckExist && !mCheckFailLink) {
                                new ToastMsg(DetailsActivity.this).toastIconError("File already exist.");
                            } else if (!mCheckExist && !mCheckFailLink) {
                                new ToastMsg(DetailsActivity.this).toastIconSuccess("Started.");
                            }
                            mCheckFinish = true;
                            mCheckFailLink = false;
                        }

                        @Override
                        public void onRewardedAdFailedToShow(AdError adError) {
                            // Ad failed to display
//                            Toast.makeText(DetailsActivity.this, "onRewardedAdFailedToShow", Toast.LENGTH_SHORT)
//                                    .show();
                        }
                    };
            mRewardedAd.show(this, adCallback);
        }
    }


    private void updateContinueWatchingData() {
        if (!mType.equals("tv")) {
            try {
                long position = playerCurrentPosition;
                long duration = mediaDuration;
                float progress = 0;
                if (position != 0 && duration != 0) {
                    progress = calculateProgress(position, duration);
                }

                //---update into continueWatching------
                ContinueWatchingModel model = new ContinueWatchingModel(mId, mTitle + " " + mEpisod,
                        mCastImageUrl, progress, position, mMediaUrl,
                        mType, mServerType);
                mContinueViewModel.update(model);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }
    }

    private long calculateProgress(long position, long duration) {
        return (position * 100 / duration);
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void controlFullScreenPlayer() {
        if (mIsFullScr) {
            fullScreenByClick = false;
            mIsFullScr = false;
            mSwipeRefreshLayout.setVisibility(VISIBLE);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

            if (mIsVideo) {
                mLPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, mPlayerHeight));
            } else {
                mLPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, mPlayerHeight));
            }

            // reset the orientation
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

        } else {

            fullScreenByClick = true;
            mIsFullScr = true;
            mSwipeRefreshLayout.setVisibility(GONE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            if (mIsVideo) {
                mLPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            } else {
                mLPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));

            }

            // reset the orientation
            //setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        }
    }


    @Override
    protected void onStart() {
        super.onStart();

        if (!Config.ENABLE_EXTERNAL_PLAYER) {
            mExternalPlayerIv.setVisibility(GONE);
        }
        initGetData();
        if (mAudioManager != null) {
            mVolumnSeekbar.setMax(mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC));
            int currentVolumn = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            mVolumnSeekbar.setProgress(currentVolumn);
        }

        mVolumnSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                if (b) {
                    //volumnTv.setText(i+"");
                    mAudioManager.setStreamVolume(mPlayer.getAudioStreamType(), i, 0);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mCastPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {

                if (playWhenReady && playbackState == CastPlayer.STATE_READY) {
                    mProgressBar.setVisibility(View.GONE);

                    Log.e("STATE PLAYER:::", String.valueOf(mIsPlaying));

                } else if (playbackState == CastPlayer.STATE_READY) {
                    mProgressBar.setVisibility(View.GONE);
                    Log.e("STATE PLAYER:::", String.valueOf(mIsPlaying));
                } else if (playbackState == CastPlayer.STATE_BUFFERING) {
                    mProgressBar.setVisibility(VISIBLE);

                    Log.e("STATE PLAYER:::", String.valueOf(mIsPlaying));
                } else {
                    Log.e("STATE PLAYER:::", String.valueOf(mIsPlaying));
                }

            }
        });

        mSimpleExoPlayerView.setControllerVisibilityListener(new PlayerControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility == 0) {
                    mImgBack.setVisibility(VISIBLE);

                    if (mType.equals("tv") || mType.equals("tvseries")) {
                        mImgFull.setVisibility(VISIBLE);
                    } else {
                        mImgFull.setVisibility(GONE);
                    }

                    // invisible download icon for live tv
                    if (mDownload_check.equals("1")) {
                        if (!tv) {
                            if (mActiveMovie) {
                                mServerIv.setVisibility(VISIBLE);
                            }
                        } else {
                        }
                    } else {
                    }

                    if (mListSub.size() != 0) {
                        mImgSubtitle.setVisibility(VISIBLE);
                    }
                    //imgSubtitle.setVisibility(VISIBLE);
                } else {
                    mImgBack.setVisibility(GONE);
                    mImgFull.setVisibility(GONE);
                    mImgSubtitle.setVisibility(GONE);
                    mVolumnControlLayout.setVisibility(GONE);
                }
            }
        });


    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void setPlayerNormalScreen() {
        mSwipeRefreshLayout.setVisibility(VISIBLE);
        mLPlay.setVisibility(GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        //close embed link playing
        if (mWebView.getVisibility() == VISIBLE) {
            if (mWebView != null) {
                Intent intent = new Intent(DetailsActivity.this, DetailsActivity.class);
                intent.putExtra("vType", mType);
                intent.putExtra("id", mId);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }
        }

        if (mIsVideo) {
            mLPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, mPlayerHeight));

        } else {
            mLPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, mPlayerHeight));
        }
    }

    @SuppressLint("SourceLockedOrientationActivity")
    public void setPlayerFullScreen() {
        mSwipeRefreshLayout.setVisibility(GONE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        if (mIsVideo) {
            mLPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        } else {
            mLPlay.setLayoutParams(new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT));
        }
    }

    private void openDownloadServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_download_server_dialog, null);
        LinearLayout internalDownloadLayout = view.findViewById(R.id.internal_download_layout);
        LinearLayout externalDownloadLayout = view.findViewById(R.id.external_download_layout);
        if (mListExternalDownload.isEmpty()) {
            externalDownloadLayout.setVisibility(GONE);
        }
        if (mListInternalDownload.isEmpty()) {
            internalDownloadLayout.setVisibility(GONE);
        }
        RecyclerView internalServerRv = view.findViewById(R.id.internal_download_rv);
        RecyclerView externalServerRv = view.findViewById(R.id.external_download_rv);
        DownloadAdapter internalDownloadAdapter = new DownloadAdapter(this, mListInternalDownload, true);
        internalServerRv.setLayoutManager(new LinearLayoutManager(this));
        internalServerRv.setHasFixedSize(true);
        internalServerRv.setAdapter(internalDownloadAdapter);

        DownloadAdapter externalDownloadAdapter = new DownloadAdapter(this, mListExternalDownload, true);
        externalServerRv.setLayoutManager(new LinearLayoutManager(this));
        externalServerRv.setHasFixedSize(true);
        externalServerRv.setAdapter(externalDownloadAdapter);

        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (isDownloading()) {
                    Toast.makeText(DetailsActivity.this, "Go to Downloads to follow download progress ", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.show();

    }

    private boolean isDownloading() {
        List<Work> works = mDBHelper.getAllWork();
        for (Work work : works) {
            if (work.getDownloadId() == 0 && !work.getDownloadStatus().equals(getApplicationContext().getString(R.string.download_waiting))) {
                return true;
            }
            if (PRDownloader.getStatus(work.getDownloadId()) == Status.RUNNING) {
                return true;
            }
        }
        return false;
    }

    private void openServerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(this).inflate(R.layout.layout_server_dialog, null);
        RecyclerView serverRv = view.findViewById(R.id.serverRv);
        mServerAdapter = new ServerAdapter(this, mListServer, "movie");
        serverRv.setLayoutManager(new LinearLayoutManager(this));
        serverRv.setHasFixedSize(true);
        serverRv.setAdapter(mServerAdapter);

        ImageView closeIv = view.findViewById(R.id.close_iv);
        builder.setView(view);

        final AlertDialog dialog = builder.create();
        dialog.show();

        closeIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        final ServerAdapter.OriginalViewHolder[] viewHolder = {null};
        mServerAdapter.setOnItemClickListener(new ServerAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, CommonModels obj, int position, ServerAdapter.OriginalViewHolder holder) {
                releasePlayer();
                resetCastPlayer();
                preparePlayer(obj);

                //serverAdapter.chanColor(viewHolder[0], position);
                //holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                //viewHolder[0] = holder;
            }

            @Override
            public void getFirstUrl(String url) {
                mMediaUrl = url;
            }

            @Override
            public void hideDescriptionLayout() {
                mDescriptionLayout.setVisibility(GONE);
                mLPlay.setVisibility(VISIBLE);
                dialog.dismiss();

            }
        });

    }

    public void preparePlayer(CommonModels obj) {
        mActiveMovie = true;
        setPlayerFullScreen();
        mMediaUrl = obj.getStremURL();
        if (!mCastSession) {
            initMoviePlayer(obj.getStremURL(), obj.getServerType(), DetailsActivity.this);

            mListSub.clear();
            if (obj.getListSub() != null) {
                mListSub.addAll(obj.getListSub());
            }

            if (mListSub.size() != 0) {
                mImgSubtitle.setVisibility(VISIBLE);
            } else {
                mImgSubtitle.setVisibility(GONE);
            }

        } else {
            if (obj.getServerType().toLowerCase().equals("embed")) {

                mCastSession = false;
                mCastPlayer.setSessionAvailabilityListener(null);
                mCastPlayer.release();

                // invisible control ui of exoplayer
                mPlayer.setPlayWhenReady(true);
                mSimpleExoPlayerView.setUseController(true);

                // invisible control ui of casting
                mCastControlView.setVisibility(GONE);
                mChromeCastTv.setVisibility(GONE);


            } else {
                showQueuePopup(DetailsActivity.this, getMediaInfo());
            }
        }
    }

    void clear_previous() {

        strCast = "";
        mStrDirector = "";
        mStrGenre = "";
        mListDownload.clear();
        mListInternalDownload.clear();
        mListExternalDownload.clear();
        mPrograms.clear();
        mCastCrews.clear();
    }

    private void prepareSubtitleList(Context context, List<SubtitleModel> list) {

    }


    public void showSubtitleDialog(Context context, List<SubtitleModel> list) {
        ViewGroup viewGroup = findViewById(android.R.id.content);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.custom_dialog_subtitle, viewGroup, false);
        ImageView cancel = dialogView.findViewById(R.id.cancel);

        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerView);
        SubtitleAdapter adapter = new SubtitleAdapter(context, list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        mAlertDialog = builder.create();
        mAlertDialog.show();

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAlertDialog.cancel();
            }
        });

    }

    @Override
    public void onCastSessionAvailable() {
        mCastSession = true;

        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mTitle);
        //movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist");
        movieMetadata.addImage(new WebImage(Uri.parse(mCastImageUrl)));
        MediaInfo mediaInfo = new MediaInfo.Builder(mMediaUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(MimeTypes.VIDEO_UNKNOWN)
                .setMetadata(movieMetadata).build();

        //array of media sources
        final MediaQueueItem[] mediaItems = {new MediaQueueItem.Builder(mediaInfo).build()};

        mCastPlayer.loadItems(mediaItems, 0, 3000, Player.REPEAT_MODE_OFF);

        // visible control ui of casting
        mCastControlView.setVisibility(VISIBLE);
        mCastControlView.setPlayer(mCastPlayer);
        mCastControlView.setVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility == GONE) {
                    mCastControlView.setVisibility(VISIBLE);
                    mChromeCastTv.setVisibility(VISIBLE);
                }
            }
        });

        // invisible control ui of exoplayer
        mPlayer.setPlayWhenReady(false);
        mSimpleExoPlayerView.setUseController(false);
    }

    @Override
    public void onCastSessionUnavailable() {
        // make cast session false
        mCastSession = false;
        // invisible control ui of exoplayer
        mPlayer.setPlayWhenReady(true);
        mSimpleExoPlayerView.setUseController(true);

        // invisible control ui of casting
        mCastControlView.setVisibility(GONE);
        mChromeCastTv.setVisibility(GONE);
    }

    public void initServerTypeForTv(String serverType) {
        this.mServerType = serverType;
    }

    @Override
    public void onProgramClick(Program program) {
        if (program.getProgramStatus().equals("onaired")) {
            showExoControlForTv();
            initMoviePlayer(program.getVideoUrl(), "tv", this);
            mTimeTv.setText(program.getTime());
            mProgramTv.setText(program.getTitle());
        } else {
            new ToastMsg(DetailsActivity.this).toastIconError("Not Yet");
        }
    }


    //this method will be called when related tv channel is clicked
    @Override
    public void onRelatedTvClicked(CommonModels obj) {
        mType = obj.getVideoType();
        mId = obj.getId();
        initGetData();
    }

    // this will call when any episode is clicked
    //if it is embed player will go full screen
    @Override
    public void onEpisodeItemClickTvSeries(String type,EpiModel obj, int position) {
        //PopUpAds.ShowAdmobInterstitialAds(this);
        if (PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
            if (!PreferenceUtils.isActivePlan(DetailsActivity.this)) {
                if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.START_APP)) {
                    PopUpAds.showAppodealInterstitialAds(DetailsActivity.this);
                } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                    PopUpAds.ShowAdmobInterstitialAds(DetailsActivity.this);
                }
            }
        }
        if (type.equalsIgnoreCase("embed")) {
            CommonModels model = new CommonModels();
            model.setStremURL(obj.getStreamURL());
            model.setServerType(obj.getServerType());
            model.setListSub(null);
            releasePlayer();
            resetCastPlayer();
            mMapMovies.put(mId, position);
            mDBHelper.deleteAllMapMovie();
            mDBHelper.insertMapMovie(getMapMovies());
            if (mMapMovies.containsKey(mId) && mMapMovies.get(mId) != null) {
                mRvServer.scrollToPosition(mMapMovies.get(mId) - 1);
            }
            mActiveMovie = true;
            preparePlayer(model);
        } else {
            mMapMovies.put(mId, position);
            mDBHelper.deleteAllMapMovie();
            mDBHelper.insertMapMovie(getMapMovies());
            mActiveMovie = true;
            if (mMapMovies.containsKey(mId) && mMapMovies.get(mId) != null) {
                mRvServer.scrollToPosition(mMapMovies.get(mId));
            }
            if (obj != null) {
                if (obj.getSubtitleList().size() != 0) {
                    mListSub.clear();
                    mListSub.addAll(obj.getSubtitleList());
                    mImgSubtitle.setVisibility(VISIBLE);
                } else {
                    mListSub.clear();
                    mImgSubtitle.setVisibility(GONE);
                }

                initMoviePlayer(obj.getStreamURL(), obj.getServerType(), DetailsActivity.this);
            }
            if (mListSub.size() > 0) {
                setSelectedSubtitle(mMediaSource, mListSub.get(0).getUrl(), DetailsActivity.this);
            }
        }
    }

    private class SubtitleAdapter extends RecyclerView.Adapter<SubtitleAdapter.OriginalViewHolder> {
        private List<SubtitleModel> items = new ArrayList<>();
        private Context ctx;

        public SubtitleAdapter(Context context, List<SubtitleModel> items) {
            this.items = items;
            ctx = context;
        }

        @Override
        public SubtitleAdapter.OriginalViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            SubtitleAdapter.OriginalViewHolder vh;
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_subtitle, parent, false);
            vh = new SubtitleAdapter.OriginalViewHolder(v);
            return vh;
        }

        @Override
        public void onBindViewHolder(SubtitleAdapter.OriginalViewHolder holder, final int position) {
            final SubtitleModel obj = items.get(position);
            holder.name.setText(obj.getLanguage());

            holder.lyt_parent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setSelectedSubtitle(mMediaSource, obj.getUrl(), ctx);
                    mAlertDialog.cancel();
                }
            });

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class OriginalViewHolder extends RecyclerView.ViewHolder {
            public TextView name;
            private View lyt_parent;

            public OriginalViewHolder(View v) {
                super(v);
                name = v.findViewById(R.id.name);
                lyt_parent = v.findViewById(R.id.lyt_parent);
            }
        }

    }

    private void loadAd() {
        adsConfig = mDBHelper.getConfigurationData().getAdsConfig();
        if (PreferenceUtils.isLoggedIn(this)) {
            if (!PreferenceUtils.isActivePlan(this)) {
                if (adsConfig.getAdsEnable().equals("1")) {

                    if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                        BannerAds.ShowAdmobBannerAds(this, mAdView);
                        //PopUpAds.ShowAdmobInterstitialAds(this);

                    } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.START_APP)) {

                        //   PopUpAds.showStartappInterstitialAds(DetailsActivity.this);
                        BannerAds.showAppodealBanner(DetailsActivity.this, R.id.appodealBannerView);
                    } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.NETWORK_AUDIENCE)) {
                        BannerAds.showFANBanner(this, mAdView);
                        PopUpAds.showFANInterstitialAds(DetailsActivity.this);
                    }

                }
            }
        } else {
            if (adsConfig.getAdsEnable().equals("1")) {
                if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                    BannerAds.ShowAdmobBannerAds(this, mAdView);
                    //PopUpAds.ShowAdmobInterstitialAds(this);

                } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.START_APP)) {

                    //   PopUpAds.showStartappInterstitialAds(DetailsActivity.this);
                    BannerAds.showAppodealBanner(DetailsActivity.this, R.id.appodealBannerView);
                } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.NETWORK_AUDIENCE)) {
                    BannerAds.showFANBanner(this, mAdView);
                    PopUpAds.showFANInterstitialAds(DetailsActivity.this);
                }
            }
        }

    }

    private void initGetData() {
        if (!mType.equals("tv")) {

            //----related rv----------
            mRelatedAdapter = new HomePageAdapter(this, mListRelated);
            mRvRelated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                    false));
            mRvRelated.setHasFixedSize(true);
            mRvRelated.setAdapter(mRelatedAdapter);

            if (mType.equals("tvseries")) {

                mSeasonSpinnerContainer.setVisibility(VISIBLE);

                mRvServer.setVisibility(VISIBLE);
                mServerIv.setVisibility(GONE);

                mRvRelated.removeAllViews();
                mListRelated.clear();
                mRvServer.removeAllViews();
                mListServer.clear();
                mListServer.clear();

               /* mDownloadBt.setVisibility(GONE);
                mWatchNowBt.setVisibility(GONE);*/

                // cast & crew adapter
                mCastCrewAdapter = new CastCrewAdapter(this, mCastCrews);
                mCastRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
                mCastRv.setHasFixedSize(true);
                mCastRv.setAdapter(mCastCrewAdapter);

                getSeriesData(mType, mId);

                if (mListSub.size() == 0) {
                    mImgSubtitle.setVisibility(GONE);
                }

            } else {
                mImgFull.setVisibility(GONE);
                mListServer.clear();
                mRvRelated.removeAllViews();
                mListRelated.clear();
                if (mListSub.size() == 0) {
                    mImgSubtitle.setVisibility(GONE);
                }

                // cast & crew adapter
                mCastCrewAdapter = new CastCrewAdapter(this, mCastCrews);
                mCastRv.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
                mCastRv.setHasFixedSize(true);
                mCastRv.setAdapter(mCastCrewAdapter);

                getMovieData(mType, mId);

                final ServerAdapter.OriginalViewHolder[] viewHolder = {null};
            }

            if (PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
                getFavStatus();
            }

        } else {
            tv = true;
            mImgSubtitle.setVisibility(GONE);
            mLLcomment.setVisibility(GONE);
            mServerIv.setVisibility(GONE);

            mRvServer.setVisibility(VISIBLE);
            mDescriptionLayout.setVisibility(GONE);
            mLPlay.setVisibility(VISIBLE);

            // hide exo player some control
            hideExoControlForTv();

            mTvLayout.setVisibility(VISIBLE);

            // hide program guide if its disable from api
            if (!PreferenceUtils.isProgramGuideEnabled(DetailsActivity.this)) {
                mProGuideTv.setVisibility(GONE);
                mProgramRv.setVisibility(GONE);

            }

            mWatchStatusTv.setText(getString(R.string.watching_on) + " " + getString(R.string.app_name));

            mTvRelated.setText(getString(R.string.all_tv_channel));

            mRvServer.removeAllViews();
            mListServer.clear();
            mRvRelated.removeAllViews();
            mListRelated.clear();

            mProgramAdapter = new ProgramAdapter(mPrograms, this);
            mProgramAdapter.setOnProgramClickListener(this);
            mProgramRv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            mProgramRv.setHasFixedSize(true);
            mProgramRv.setAdapter(mProgramAdapter);

            //----related rv----------
            //relatedTvAdapter = new LiveTvHomeAdapter(this, listRelated, TAG);
            mRelatedTvAdapter = new RelatedTvAdapter(mListRelated, DetailsActivity.this);
            mRvRelated.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            mRvRelated.setHasFixedSize(true);
            mRvRelated.setAdapter(mRelatedTvAdapter);
            mRelatedTvAdapter.setListener(DetailsActivity.this);

            mImgAddFav.setVisibility(GONE);
            mImgAddFav2.setVisibility(GONE);

            mServerAdapter = new ServerAdapter(this, mListServer, "tv");
            mRvServer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            mRvServer.setHasFixedSize(true);
            mRvServer.setAdapter(mServerAdapter);
            getTvData(mType, mId);
            mLLBottom.setVisibility(GONE);

            final ServerAdapter.OriginalViewHolder[] viewHolder = {null};
            mServerAdapter.setOnItemClickListener(new ServerAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(View view, CommonModels obj, int position, ServerAdapter.OriginalViewHolder holder) {
                    mMediaUrl = obj.getStremURL();

                    if (!mCastSession) {
                        initMoviePlayer(obj.getStremURL(), obj.getServerType(), DetailsActivity.this);

                    } else {

                        if (obj.getServerType().toLowerCase().equals("embed")) {

                            mCastSession = false;
                            mCastPlayer.setSessionAvailabilityListener(null);
                            mCastPlayer.release();

                            // invisible control ui of exoplayer
                            mPlayer.setPlayWhenReady(true);
                            mSimpleExoPlayerView.setUseController(true);

                            // invisible control ui of casting
                            mCastControlView.setVisibility(GONE);
                            mChromeCastTv.setVisibility(GONE);
                        } else {
                            showQueuePopup(DetailsActivity.this, getMediaInfo());
                        }
                    }

                    mServerAdapter.chanColor(viewHolder[0], position);
                    holder.name.setTextColor(getResources().getColor(R.color.colorPrimary));
                    viewHolder[0] = holder;
                }

                @Override
                public void getFirstUrl(String url) {
                    mMediaUrl = url;
                }

                @Override
                public void hideDescriptionLayout() {

                }
            });


        }
    }

    private void openWebActivity(String s, Context context, String videoType) {

        if (mIsPlaying) {
            mPlayer.release();
        }
        mProgressBar.setVisibility(GONE);
        mPlayerLayout.setVisibility(GONE);

        mWebView.loadUrl(s);
        mWebView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setSupportMultipleWindows(true);
        webSettings.setJavaScriptEnabled(true);
        mWebView.setVisibility(VISIBLE);

    }

    public void initMoviePlayer(String url, String type, Context context) {
        Log.e("vTYpe :: ", type);
        mUrlType = type;
        if (type.equals("embed") || type.equals("vimeo") || type.equals("gdrive") || type.equals("youtube-live")) {
            mIsVideo = false;
            openWebActivity(url, context, type);
        } else {
            mIsVideo = true;
            initVideoPlayer(url, context, type);
        }
    }

    public void initVideoPlayer(String url, Context context, String type) {
        mServerType = type;
        mProgressBar.setVisibility(VISIBLE);
        if (!mType.equals("tv")) {
            ContinueWatchingModel model = new ContinueWatchingModel(mId, mTitle + " " + mEpisod, mCastImageUrl, 0, 0, url, mType, type);
            mContinueViewModel.insert(model);
        }
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.release();
        }
        mWebView.setVisibility(GONE);
        mPlayerLayout.setVisibility(VISIBLE);

        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        TrackSelection.Factory videoTrackSelectionFactory = new
                AdaptiveTrackSelection.Factory(bandwidthMeter);

        DefaultTrackSelector trackSelector = new
                DefaultTrackSelector(videoTrackSelectionFactory);
        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector);
        // player.setPlayWhenReady(true);
        //simpleExoPlayerView.setPlayer(player);

        Uri uri = Uri.parse(url);

        if (type.equals("hls")) {
            mMediaSource = hlsMediaSource(uri, context);
        } else if (type.equals("youtube")) {
            Log.e("youtube url  :: ", url);
            extractYoutubeUrl(url, context, 18);
        } else if (type.equals("youtube-live")) {
            Log.e("youtube url  :: ", url);
            extractYoutubeUrl(url, context, 133);
        } else if (type.equals("rtmp")) {
            mMediaSource = rtmpMediaSource(uri);
        } else {
            mMediaSource = mediaSource(uri, context);
        }

        //Toast.makeText(context, "castSession:"+getCastSessionObj()+"", Toast.LENGTH_SHORT).show();
        mPlayer.prepare(mMediaSource, true, false);
        mSimpleExoPlayerView.setPlayer(mPlayer);
        mPlayer.setPlayWhenReady(true);
        if (resumePosition > 0) {
            mPlayer.seekTo(resumePosition);
            mPlayer.setPlayWhenReady(true);
        }

        mPlayer.addListener(new Player.DefaultEventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playWhenReady && playbackState == Player.STATE_READY) {

                    mIsPlaying = true;
                    mProgressBar.setVisibility(View.GONE);
                } else if (playbackState == Player.STATE_READY) {
                    mProgressBar.setVisibility(View.GONE);
                    mIsPlaying = false;
                } else if (playbackState == Player.STATE_BUFFERING) {
                    mIsPlaying = false;
                    mProgressBar.setVisibility(VISIBLE);
                } else if (playbackState == Player.STATE_ENDED) {
                    //---delete into continueWatching------
                    ContinueWatchingModel model = new ContinueWatchingModel(mId, mTitle + " " + mEpisod,
                            mCastImageUrl, 0, 0, mMediaUrl,
                            mType, mServerType);
                    mContinueViewModel.delete(model);
                } else {
                    // player paused in any state
                    mIsPlaying = false;
                    playerCurrentPosition = mPlayer.getCurrentPosition();
                    mediaDuration = mPlayer.getDuration();
                }
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void extractYoutubeUrl(String url, final Context context, final int tag) {
        new YouTubeExtractor(context) {
            @Override
            public void onExtractionComplete(SparseArray<YtFile> ytFiles, VideoMeta vMeta) {
                if (ytFiles != null) {
                    int itag = tag;
                    String downloadUrl = ytFiles.get(itag).getUrl();
                    mYoutubeDownloadUr = downloadUrl;
                    Log.e("YOUTUBE::", String.valueOf(downloadUrl));
                    try {

                        MediaSource mediaSource = mediaSource(Uri.parse(downloadUrl), context);
                        mPlayer.prepare(mediaSource, true, false);
                        if (Config.YOUTUBE_VIDEO_AUTO_PLAY) {
                            mPlayer.setPlayWhenReady(true);
                        } else {
                            mPlayer.setPlayWhenReady(false);
                        }
                        if (resumePosition > 0) {
                            mPlayer.seekTo(resumePosition);
                            mPlayer.setPlayWhenReady(true);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        }.extract(url, true, true);
    }

    private MediaSource rtmpMediaSource(Uri uri) {
        MediaSource videoSource = null;
        RtmpDataSourceFactory dataSourceFactory = new RtmpDataSourceFactory();
        videoSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);

        return videoSource;
    }

    private MediaSource hlsMediaSource(Uri uri, Context context) {
        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context,
                Util.getUserAgent(context, "oxoo"), bandwidthMeter);

        MediaSource videoSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(uri);


        return videoSource;
    }

    private MediaSource mediaSource(Uri uri, Context context) {
        return new ExtractorMediaSource.Factory(
                new DefaultHttpDataSourceFactory("exoplayer")).
                createMediaSource(uri);
    }


    public void setSelectedSubtitle(MediaSource mediaSource, String subtitle, Context context) {
        MergingMediaSource mergedSource;
        if (subtitle != null) {
            Uri subtitleUri = Uri.parse(subtitle);

            Format subtitleFormat = Format.createTextSampleFormat(
                    null, // An identifier for the track. May be null.
                    MimeTypes.TEXT_VTT, // The mime type. Must be set correctly.
                    Format.NO_VALUE, // Selection flags for the track.
                    "en"); // The subtitle language. May be null.

            DefaultDataSourceFactory dataSourceFactory = new DefaultDataSourceFactory(context,
                    Util.getUserAgent(context, CLASS_NAME), new DefaultBandwidthMeter());


            MediaSource subtitleSource = new SingleSampleMediaSource
                    .Factory(dataSourceFactory)
                    .createMediaSource(subtitleUri, subtitleFormat, C.TIME_UNSET);


            mergedSource = new MergingMediaSource(mediaSource, subtitleSource);
            mPlayer.prepare(mergedSource, false, false);
            mPlayer.setPlayWhenReady(true);
            //resumePlayer();

        } else {
            Toast.makeText(context, "there is no subtitle", Toast.LENGTH_SHORT).show();
        }
    }

    private void addToFav() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FavouriteApi api = retrofit.create(FavouriteApi.class);
        Call<FavoriteModel> call = api.addToFavorite(Config.API_KEY, mUserId, mId);
        call.enqueue(new Callback<FavoriteModel>() {
            @Override
            public void onResponse(Call<FavoriteModel> call, retrofit2.Response<FavoriteModel> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        new ToastMsg(DetailsActivity.this).toastIconSuccess(response.body().getMessage());
                        mIsFav = true;
                        mImgAddFav.setBackgroundResource(R.drawable.ic_favorite_white);
                        mImgAddFav2.setBackgroundResource(R.drawable.ic_favorite_white);
                    } else {
                        new ToastMsg(DetailsActivity.this).toastIconError(response.body().getMessage());
                    }
                } else {
                    new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.error_toast));
                }
            }

            @Override
            public void onFailure(Call<FavoriteModel> call, Throwable t) {
                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.error_toast));

            }
        });

    }

    private void paidControl(String isPaid) {
        if (isPaid.equals("1")) {
            if (PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
                if (PreferenceUtils.isActivePlan(DetailsActivity.this)) {
                    if (PreferenceUtils.isValid(DetailsActivity.this)) {
                        mContentDetails.setVisibility(VISIBLE);
                        mSubscriptionLayout.setVisibility(GONE);
                        Log.e("SUBCHECK", "validity: " + PreferenceUtils.isValid(DetailsActivity.this));

                    } else {
                        Log.e("SUBCHECK", "not valid");
                        /*contentDetails.setVisibility(GONE);
                        subscriptionLayout.setVisibility(VISIBLE);*/
                        PreferenceUtils.updateSubscriptionStatus(DetailsActivity.this);
                        //paidControl(isPaid);
                    }
                } else {
                    Log.e("SUBCHECK", "not active plan");
                    mContentDetails.setVisibility(GONE);
                    mSubscriptionLayout.setVisibility(VISIBLE);
                }
            } else {
                startActivity(new Intent(DetailsActivity.this, FirebaseSignUpActivity.class));
                finish();
            }

        } else {
            //free content
            mContentDetails.setVisibility(VISIBLE);
            mSubscriptionLayout.setVisibility(GONE);
        }
    }

    private void getActiveStatus(String userId) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SubscriptionApi subscriptionApi = retrofit.create(SubscriptionApi.class);

        Call<ActiveStatus> call = subscriptionApi.getActiveStatus(Config.API_KEY, userId);
        call.enqueue(new Callback<ActiveStatus>() {
            @Override
            public void onResponse(Call<ActiveStatus> call, retrofit2.Response<ActiveStatus> response) {
                ActiveStatus activeStatus = response.body();
                if (!activeStatus.getStatus().equals("active")) {
                    mContentDetails.setVisibility(GONE);
                    mSubscriptionLayout.setVisibility(VISIBLE);
                } else {
                    mContentDetails.setVisibility(VISIBLE);
                    mSubscriptionLayout.setVisibility(GONE);
                }

                PreferenceUtils.updateSubscriptionStatus(DetailsActivity.this);
            }

            @Override
            public void onFailure(Call<ActiveStatus> call, Throwable t) {
                t.printStackTrace();
            }
        });

    }

    private void getTvData(final String vtype, final String vId) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SingleDetailsTVApi api = retrofit.create(SingleDetailsTVApi.class);
        Call<SingleDetailsTV> call = api.getSingleDetails(Config.API_KEY, vtype, vId);
        call.enqueue(new Callback<SingleDetailsTV>() {
            @Override
            public void onResponse(Call<SingleDetailsTV> call, retrofit2.Response<SingleDetailsTV> response) {
                if (response.code() == 200) {
                    if (mSwipeRefreshLayout == null) return;
                    if (response.body() != null) {
                        mSwipeRefreshLayout.setRefreshing(false);
                        mShimmerLayout.stopShimmer();
                        mShimmerLayout.setVisibility(GONE);
                        paidControl(response.body().getIsPaid());

                        SingleDetailsTV detailsModel = response.body();

                        mTitle = detailsModel.getTvName();
                        mTvName.setText(mTitle);
                        mTvName.setVisibility(GONE);
                        mTvTitleTv.setText(mTitle);

                        mImdb_rating = detailsModel.getTvImdb();
                        mTvImdb.setText(mImdb_rating);
                        mTvImdb.setVisibility(GONE);

                        if (!detailsModel.getDescription().equals("")) {
                            mTvDes.setText(detailsModel.getDescription());
                        }
                        V_URL = detailsModel.getStreamUrl();
                        mCastImageUrl = detailsModel.getThumbnailUrl();

                        Picasso.get().load(detailsModel.getThumbnailUrl()).placeholder(R.drawable.album_art_placeholder)
                                .into(mTvThumbIv);

                        CommonModels model = new CommonModels();
                        model.setTitle("HD");
                        model.setStremURL(V_URL);
                        model.setServerType(detailsModel.getStreamFrom());
                        mListServer.add(model);

                        mCurrentProgramTime = detailsModel.getCurrentProgramTime();
                        mCurrentProgramTitle = detailsModel.getCurrentProgramTitle();

                        mTimeTv.setText(mCurrentProgramTime);
                        mProgramTv.setText(mCurrentProgramTitle);
                        if (PreferenceUtils.isProgramGuideEnabled(DetailsActivity.this)) {
                            List<ProgramGuide> programGuideList = response.body().getProgramGuide();
                            for (int i = 0; i < programGuideList.size(); i++) {
                                ProgramGuide programGuide = programGuideList.get(i);
                                Program program = new Program();
                                program.setId(programGuide.getId());
                                program.setTitle(programGuide.getTitle());
                                program.setProgramStatus(programGuide.getProgramStatus());
                                program.setTime(programGuide.getTime());
                                program.setVideoUrl(programGuide.getVideoUrl());

                                mPrograms.add(program);
                            }

                            if (mPrograms.size() <= 0) {
                                mProGuideTv.setVisibility(GONE);
                                mProgramRv.setVisibility(GONE);
                            } else {
                                mProGuideTv.setVisibility(VISIBLE);
                                mProgramRv.setVisibility(VISIBLE);
                                mProgramAdapter.notifyDataSetChanged();
                            }
                        }
                        //all tv channel data
                        List<AllTvChannel> allTvChannelList = response.body().getAllTvChannel();
                        for (int i = 0; i < allTvChannelList.size(); i++) {
                            AllTvChannel allTvChannel = allTvChannelList.get(i);
                            CommonModels models = new CommonModels();
                            models.setImageUrl(allTvChannel.getPosterUrl());
                            models.setTitle(allTvChannel.getTvName());

                            models.setVideoType("tv");
                            models.setIsPaid(allTvChannel.getIsPaid());
                            models.setId(allTvChannel.getLiveTvId());
                            mListRelated.add(models);
                        }
                        if (mListRelated.size() == 0) {
                            mTvRelated.setVisibility(GONE);
                        }
                        mRelatedTvAdapter.notifyDataSetChanged();

                        //additional media source data
                        List<AdditionalMediaSource> serverArray = response.body().getAdditionalMediaSource();
                        for (int i = 0; i < serverArray.size(); i++) {
                            AdditionalMediaSource jsonObject = serverArray.get(i);
                            CommonModels models = new CommonModels();
                            models.setTitle(jsonObject.getLabel());
                            models.setStremURL(jsonObject.getUrl());
                            models.setServerType(jsonObject.getSource());

                            mListServer.add(models);
                        }
                        mServerAdapter.notifyDataSetChanged();
                    }
                }

            }

            @Override
            public void onFailure(Call<SingleDetailsTV> call, Throwable t) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });

    }

    private void getSeriesData(String vtype, String vId) {
        final List<String> seasonList = new ArrayList<>();
        final List<String> seasonListForDownload = new ArrayList<>();
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SingleDetailsApi api = retrofit.create(SingleDetailsApi.class);
        Call<SingleDetails> call = api.getSingleDetails(Config.API_KEY, vtype, vId);
        call.enqueue(new Callback<SingleDetails>() {
            @Override
            public void onResponse(Call<SingleDetails> call, retrofit2.Response<SingleDetails> response) {
                if (response.code() == 200) {
                    if (mSwipeRefreshLayout == null) return;
                    mSwipeRefreshLayout.setRefreshing(false);
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(GONE);

                    SingleDetails singleDetails = response.body();
                    String isPaid = singleDetails.getIsPaid();
                    paidControl(isPaid);

                    mTitle = singleDetails.getTitle();
                    mImdb_rating = singleDetails.getImdb_rating();
                    mSeriesTitleTv.setText(mTitle);
                    mCastImageUrl = singleDetails.getThumbnailUrl();
                    mSeriesTitle = mTitle;
                    mTvName.setText(mTitle);
                    mTvImdb.setText(mImdb_rating);
                    mTvRelease.setText("Release On : " + singleDetails.getRelease());
                    if (!singleDetails.getDescription().equals("")) {
                        mTvDes.setText(singleDetails.getDescription());
                    }

                    Picasso.get().load(singleDetails.getPosterUrl()).placeholder(R.drawable.album_art_placeholder_large)
                            .into(mPosterIv);
                    Picasso.get().load(singleDetails.getThumbnailUrl()).placeholder(R.drawable.poster_placeholder)
                            .into(mThumbIv);

                    mDownload_check = singleDetails.getEnableDownload();

                    //----director---------------
                    for (int i = 0; i < singleDetails.getDirector().size(); i++) {
                        Director director = singleDetails.getDirector().get(i);
                        if (i == singleDetails.getDirector().size() - 1) {
                            mStrDirector = mStrDirector + director.getName();
                        } else {
                            mStrDirector = mStrDirector + director.getName() + ", ";
                        }
                    }
                    mTvDirector.setText(mStrDirector);

                    //----cast---------------
                    for (int i = 0; i < singleDetails.getCast().size(); i++) {
                        Cast cast = singleDetails.getCast().get(i);

                        CastCrew castCrew = new CastCrew();
                        castCrew.setId(cast.getStarId());
                        castCrew.setName(cast.getName());
                        castCrew.setUrl(cast.getUrl());
                        castCrew.setImageUrl(cast.getImageUrl());
                        mCastCrews.add(castCrew);
                    }
                    mCastCrewAdapter.notifyDataSetChanged();
                    //---genre---------------
                    for (int i = 0; i < singleDetails.getGenre().size(); i++) {
                        Genre genre = singleDetails.getGenre().get(i);
                        if (i == singleDetails.getCast().size() - 1) {
                            mStrGenre = mStrGenre + genre.getName();
                        } else {
                            if (i == singleDetails.getGenre().size() - 1) {
                                mStrGenre = mStrGenre + genre.getName();
                            } else {
                                mStrGenre = mStrGenre + genre.getName() + ", ";
                            }
                        }
                    }
                    setGenreText();

                    //----related tv series---------------
                    for (int i = 0; i < singleDetails.getRelatedTvseries().size(); i++) {
                        RelatedMovie relatedTvSeries = singleDetails.getRelatedTvseries().get(i);

                        CommonModels models = new CommonModels();
                        models.setTitle(relatedTvSeries.getTitle());
                        models.setImageUrl(relatedTvSeries.getThumbnailUrl());
                        models.setId(relatedTvSeries.getVideosId());
                        models.setVideoType("tvseries");
                        models.setIsPaid(relatedTvSeries.getIsPaid());
                        mListRelated.add(models);
                    }
                    if (mListRelated.size() == 0) {
                        mTvRelated.setVisibility(GONE);
                    }
                    mRelatedAdapter.notifyDataSetChanged();

                    //----seasson------------
                    for (int i = 0; i < singleDetails.getSeason().size(); i++) {
                        Season season = singleDetails.getSeason().get(i);

                        CommonModels models = new CommonModels();
                        String season_name = season.getSeasonsName();
                        models.setTitle(season.getSeasonsName());
                        seasonList.add("#SV: " + season.getSeasonsName());
                        seasonListForDownload.add(season.getSeasonsName());

                        //----episode------
                        List<EpiModel> epList = new ArrayList<>();
                        epList.clear();
                        for (int j = 0; j < singleDetails.getSeason().get(i).getEpisodes().size(); j++) {
                            Episode episode = singleDetails.getSeason().get(i).getEpisodes().get(j);

                            EpiModel model = new EpiModel();
                            model.setSeson(season_name);
                            model.setEpi(episode.getEpisodesName());
                            model.setStreamURL(episode.getFileUrl());
                            model.setServerType(episode.getFileType());
                            model.setImageUrl(episode.getImageUrl());
                            model.setSubtitleList(episode.getSubtitle());
                            epList.add(model);
                        }
                        models.setListEpi(epList);
                        mListServer.add(models);
                        if (seasonList.size() > 0) {
                            setSeasonData(seasonList, singleDetails.getSeason());
                        }

                    }
                }
            }

            @Override
            public void onFailure(Call<SingleDetails> call, Throwable t) {

            }
        });
    }

    private void onLoadDataFinish() {
        if (isFromContinueWatching) {
            onWatchNowClick();
            playerCurrentPosition = 0L;
            resumePosition = 0;
            isFromContinueWatching = false;
        }
    }


    private void setSeasonData(List<String> seasonData, List<Season> seasonList) {

        ArrayAdapter aa = new ArrayAdapter(this, android.R.layout.simple_spinner_item, seasonData);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        //Setting the ArrayAdapter data on the Spinner
        mSeasonSpinner.setAdapter(aa);

        mSeasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int position, long l) {
                mRvServer.removeAllViewsInLayout();
                mRvServer.setLayoutManager(new LinearLayoutManager(DetailsActivity.this,
                        RecyclerView.HORIZONTAL, false));
                mEpisodeAdapter = new EpisodeAdapter(DetailsActivity.this,
                        mListServer.get(position).getListEpi());
                if (mMapMovies.containsKey(mId) && mMapMovies.get(mId) != null) {
                    mEpisodeAdapter.setNowPlaying(mMapMovies.get(mId));
                }
                mRvServer.setAdapter(mEpisodeAdapter);

                if (mMapMovies.containsKey(mId) && mMapMovies.get(mId) != null) {
                    mRvServer.scrollToPosition(mMapMovies.get(mId) - 1);
                }
                mEpisodeAdapter.setOnEmbedItemClickListener(DetailsActivity.this);
                //get download link
                mListExternalDownload.clear();
                mListInternalDownload.clear();
                for (int i = 0; i < seasonList.get(position).getDownloadLinks().size(); i++) {
                    DownloadLink downloadLink = seasonList.get(position).getDownloadLinks().get(i);
                    CommonModels models = new CommonModels();
                    models.setTitle(downloadLink.getLabel());
                    models.setStremURL(downloadLink.getDownloadUrl());
                    models.setFileSize(downloadLink.getFileSize());
                    models.setResulation(downloadLink.getResolution());
                    models.setInAppDownload(downloadLink.isInAppDownload());
                    if (downloadLink.isInAppDownload()) {
                        mListInternalDownload.add(models);
                    } else {
                        mListExternalDownload.add(models);
                    }
                }
                onLoadDataFinish();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    public String getMapMovies() {
        Gson gson = new Gson();
        return gson.toJson(mMapMovies);
    }

    public void setMapMovies(String json) {
        Gson gson = new Gson();
        Type type = new TypeToken<HashMap<String, Integer>>() {
        }.getType();
        mMapMovies = gson.fromJson(json, type);
    }

    private void setGenreText() {

        mTvGenre.setText(mStrGenre);

        mDGenryTv.setText(mStrGenre);

    }

    private void getMovieData(String vtype, String vId) {
        strCast = "";
        mStrDirector = "";
        mStrGenre = "";
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        SingleDetailsApi api = retrofit.create(SingleDetailsApi.class);
        Call<SingleDetails> call = api.getSingleDetails(Config.API_KEY, vtype, vId);
        call.enqueue(new Callback<SingleDetails>() {
            @Override
            public void onResponse(Call<SingleDetails> call, retrofit2.Response<SingleDetails> response) {
                if (response.code() == 200) {
                    if (mShimmerLayout == null) return;
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    SingleDetails singleDetails = response.body();
                    paidControl(singleDetails.getIsPaid());
                    mDownload_check = singleDetails.getEnableDownload();
                    mCastImageUrl = singleDetails.getThumbnailUrl();
                    if (mDownload_check.equals("1")) {
                        mDownloadBt.setVisibility(VISIBLE);
                    } else {
                        //mDownloadBt.setVisibility(GONE);
                    }
                    mTitle = singleDetails.getTitle();
                    mImdb_rating = singleDetails.getImdb_rating();
                    movieTitle = mTitle;

                    mTvName.setText(mTitle);
                    mTvImdb.setText(mImdb_rating);
                    mTvRelease.setText("Release On : " + singleDetails.getRelease());
                    if (!singleDetails.getDescription().equals("")) {
                        mTvDes.setText(singleDetails.getDescription());
                    }

                    Picasso.get().load(singleDetails.getPosterUrl()).placeholder(R.drawable.album_art_placeholder_large)
                            .into(mPosterIv);
                    Picasso.get().load(singleDetails.getThumbnailUrl()).placeholder(R.drawable.poster_placeholder)
                            .into(mThumbIv);

                    //----director---------------
                    for (int i = 0; i < singleDetails.getDirector().size(); i++) {
                        Director director = response.body().getDirector().get(i);
                        if (i == singleDetails.getDirector().size() - 1) {
                            mStrDirector = mStrDirector + director.getName();
                        } else {
                            mStrDirector = mStrDirector + director.getName() + ", ";
                        }
                    }
                    mTvDirector.setText(mStrDirector);

                    //----cast---------------
                    for (int i = 0; i < singleDetails.getCast().size(); i++) {
                        Cast cast = singleDetails.getCast().get(i);

                        CastCrew castCrew = new CastCrew();
                        castCrew.setId(cast.getStarId());
                        castCrew.setName(cast.getName());
                        castCrew.setUrl(cast.getUrl());
                        castCrew.setImageUrl(cast.getImageUrl());

                        mCastCrews.add(castCrew);

                    }
                    mCastCrewAdapter.notifyDataSetChanged();

                    //---genre---------------
                    for (int i = 0; i < singleDetails.getGenre().size(); i++) {
                        Genre genre = singleDetails.getGenre().get(i);
                        if (i == singleDetails.getCast().size() - 1) {
                            mStrGenre = mStrGenre + genre.getName();
                        } else {
                            if (i == singleDetails.getGenre().size() - 1) {
                                mStrGenre = mStrGenre + genre.getName();
                            } else {
                                mStrGenre = mStrGenre + genre.getName() + ", ";
                            }
                        }
                    }
                    mTvGenre.setText(mStrGenre);
                    mDGenryTv.setText(mStrGenre);

                    //-----server----------
                    List<Video> serverList = new ArrayList<>();
                    serverList.addAll(singleDetails.getVideos());
                    for (int i = 0; i < serverList.size(); i++) {
                        Video video = serverList.get(i);

                        CommonModels models = new CommonModels();
                        models.setTitle(video.getLabel());
                        models.setStremURL(video.getFileUrl());
                        models.setServerType(video.getFileType());

                        if (video.getFileType().equals("mp4")) {
                            V_URL = video.getFileUrl();
                        }

                        //----subtitle-----------
                        List<Subtitle> subArray = new ArrayList<>();
                        subArray.addAll(singleDetails.getVideos().get(i).getSubtitle());
                        if (subArray.size() != 0) {

                            List<SubtitleModel> list = new ArrayList<>();
                            for (int j = 0; j < subArray.size(); j++) {
                                Subtitle subtitle = subArray.get(j);
                                SubtitleModel subtitleModel = new SubtitleModel();
                                subtitleModel.setUrl(subtitle.getUrl());
                                subtitleModel.setLanguage(subtitle.getLanguage());
                                list.add(subtitleModel);
                            }
                            if (i == 0) {
                                mListSub.addAll(list);
                            }
                            models.setListSub(list);
                        } else {
                            models.setSubtitleURL(mStrSubtitle);
                        }
                        mListServer.add(models);
                    }

                    if (mServerAdapter != null) {
                        mServerAdapter.notifyDataSetChanged();
                    }

                    //----related post---------------
                    for (int i = 0; i < singleDetails.getRelatedMovie().size(); i++) {
                        RelatedMovie relatedMovie = singleDetails.getRelatedMovie().get(i);
                        CommonModels models = new CommonModels();
                        models.setTitle(relatedMovie.getTitle());
                        models.setImageUrl(relatedMovie.getThumbnailUrl());
                        models.setId(relatedMovie.getVideosId());
                        models.setVideoType("movie");
                        models.setIsPaid(relatedMovie.getIsPaid());
                        models.setIsPaid(relatedMovie.getIsPaid());
                        mListRelated.add(models);
                    }

                    if (mListRelated.size() == 0) {
                        mTvRelated.setVisibility(GONE);
                    }
                    mRelatedAdapter.notifyDataSetChanged();

                    //----download list---------
                    mListExternalDownload.clear();
                    mListInternalDownload.clear();
                    for (int i = 0; i < singleDetails.getDownloadLinks().size(); i++) {
                        DownloadLink downloadLink = singleDetails.getDownloadLinks().get(i);

                        CommonModels models = new CommonModels();
                        models.setTitle(downloadLink.getLabel());
                        models.setStremURL(downloadLink.getDownloadUrl());
                        models.setFileSize(downloadLink.getFileSize());
                        models.setResulation(downloadLink.getResolution());
                        models.setInAppDownload(downloadLink.isInAppDownload());
                        if (downloadLink.isInAppDownload()) {
                            mListInternalDownload.add(models);
                        } else {
                            mListExternalDownload.add(models);
                        }
                    }
                    onLoadDataFinish();
                } else {
                    mSwipeRefreshLayout.setRefreshing(false);
                }

            }

            @Override
            public void onFailure(Call<SingleDetails> call, Throwable t) {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void getFavStatus() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FavouriteApi api = retrofit.create(FavouriteApi.class);
        Call<FavoriteModel> call = api.verifyFavoriteList(Config.API_KEY, mUserId, mId);
        call.enqueue(new Callback<FavoriteModel>() {
            @Override
            public void onResponse(Call<FavoriteModel> call, retrofit2.Response<FavoriteModel> response) {
                if (response.code() == 200) {
                    if (mImgAddFav == null) return;
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        mIsFav = true;
                        mImgAddFav.setBackgroundResource(R.drawable.ic_favorite_white);
                        mImgAddFav.setVisibility(VISIBLE);
                        mImgAddFav2.setBackgroundResource(R.drawable.ic_favorite_white);
                        mImgAddFav2.setVisibility(VISIBLE);
                    } else {
                        mIsFav = false;
                        mImgAddFav.setBackgroundResource(R.drawable.ic_favorite_border_white);
                        mImgAddFav.setVisibility(VISIBLE);
                        mImgAddFav2.setBackgroundResource(R.drawable.ic_favorite_border_white);
                        mImgAddFav2.setVisibility(VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<FavoriteModel> call, @NotNull Throwable t) {

            }
        });

    }

    private void removeFromFav() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FavouriteApi api = retrofit.create(FavouriteApi.class);
        Call<FavoriteModel> call = api.removeFromFavorite(Config.API_KEY, mUserId, mId);
        call.enqueue(new Callback<FavoriteModel>() {
            @Override
            public void onResponse(Call<FavoriteModel> call, retrofit2.Response<FavoriteModel> response) {
                if (response.code() == 200) {
                    if (response.body().getStatus().equalsIgnoreCase("success")) {
                        mIsFav = false;
                        new ToastMsg(DetailsActivity.this).toastIconSuccess(response.body().getMessage());
                        mImgAddFav.setBackgroundResource(R.drawable.ic_favorite_border_white);
                        mImgAddFav2.setBackgroundResource(R.drawable.ic_favorite_border_white);
                    } else {
                        mIsFav = true;
                        new ToastMsg(DetailsActivity.this).toastIconError(response.body().getMessage());
                        mImgAddFav.setBackgroundResource(R.drawable.ic_favorite_white);
                        mImgAddFav2.setBackgroundResource(R.drawable.ic_favorite_white);
                    }
                }
            }

            @Override
            public void onFailure(Call<FavoriteModel> call, Throwable t) {
                new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.fetch_error));
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void addComment(String videoId, String userId, final String comments) {

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        CommentApi api = retrofit.create(CommentApi.class);
        Call<PostCommentModel> call = api.postComment(Config.API_KEY, videoId, userId, comments);
        call.enqueue(new Callback<PostCommentModel>() {
            @Override
            public void onResponse(Call<PostCommentModel> call, retrofit2.Response<PostCommentModel> response) {
                if (response.body().getStatus().equals("success")) {
                    mRvComment.removeAllViews();
                    mListComment.clear();
                    getComments();
                    mEtComment.setText("");
                    new ToastMsg(DetailsActivity.this).toastIconSuccess(response.body().getMessage());
                } else {
                    new ToastMsg(DetailsActivity.this).toastIconError(response.body().getMessage());
                }
            }

            @Override
            public void onFailure(Call<PostCommentModel> call, Throwable t) {

            }
        });
    }

    private void getComments() {

        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        CommentApi api = retrofit.create(CommentApi.class);
        Call<List<GetCommentsModel>> call = api.getAllComments(Config.API_KEY, mId);
        call.enqueue(new Callback<List<GetCommentsModel>>() {
            @Override
            public void onResponse(Call<List<GetCommentsModel>> call, retrofit2.Response<List<GetCommentsModel>> response) {
                if (response.code() == 200) {
                    mListComment.addAll(response.body());

                    mCommentsAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<GetCommentsModel>> call, Throwable t) {

            }
        });

    }

    public void hideDescriptionLayout() {
        mDescriptionLayout.setVisibility(GONE);
        mLPlay.setVisibility(VISIBLE);
    }

    public void showSeriesLayout() {
        mSeriesLayout.setVisibility(VISIBLE);
        mTopShareLayout.setVisibility(GONE);
    }

    public void showDescriptionLayout() {
        mDescriptionLayout.setVisibility(VISIBLE);
        mLPlay.setVisibility(GONE);
        mSeriesLayout.setVisibility(GONE);
        mTopShareLayout.setVisibility(VISIBLE);
        Objects.requireNonNull(mRvServer.getAdapter()).notifyDataSetChanged();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.e("OnPause", "isPlaying: " + mIsPlaying);
        if (mIsPlaying && mPlayer != null) {
            //Log.e("PLAY:::","PAUSE");
            mPlayer.setPlayWhenReady(false);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        //castManager.removeProgressWatcher(this);

        Log.e("onStop", "isPlaying: " + mIsPlaying);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        updateContinueWatchingData();
        resetCastPlayer();
        releasePlayer();
        mUnbinder.unbind();
    }

    @Override
    public void onBackPressed() {
        if (mActiveMovie) {
            setPlayerNormalScreen();
            if (mPlayer != null) {
                mPlayer.setPlayWhenReady(false);
                mPlayer.stop();
            }
            showDescriptionLayout();
            mActiveMovie = false;
        } else {
            releasePlayer();
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        //startPlayer();
        if (mPlayer != null) {
            if (mType.equals("youtube") || mType.equals("youtube-live")) {
                if (Config.YOUTUBE_VIDEO_AUTO_PLAY) {
                    mPlayer.setPlayWhenReady(true);
                } else {
                    mPlayer.setPlayWhenReady(false);
                }
            } else {
                mPlayer.setPlayWhenReady(true);
            }
        }
    }

    public void releasePlayer() {
        if (mPlayer != null) {
            mPlayer.setPlayWhenReady(true);
            mPlayer.stop();
            mPlayer.release();
            mPlayer = null;
            mSimpleExoPlayerView.setPlayer(null);
            //simpleExoPlayerView = null;
        }
    }

    public void setMediaUrlForTvSeries(String url, String season, String episod) {
        mMediaUrl = url;
        this.mSeason = season;
        this.mEpisod = episod;
    }

    public boolean getCastSession() {
        return mCastSession;
    }

    public void resetCastPlayer() {
        if (mCastPlayer != null) {
            mCastPlayer.setPlayWhenReady(false);
            mCastPlayer.release();
        }
    }

    public void showQueuePopup(final Context context, final MediaInfo mediaInfo) {
        CastSession castSession =
                CastContext.getSharedInstance(context).getSessionManager().getCurrentCastSession();
        if (castSession == null || !castSession.isConnected()) {
            Log.w(TAG, "showQueuePopup(): not connected to a cast device");
            return;
        }
        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();
        if (remoteMediaClient == null) {
            Log.w(TAG, "showQueuePopup(): null RemoteMediaClient");
            return;
        }
        MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(
                true).setPreloadTime(PRELOAD_TIME_S).build();
        MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};
        remoteMediaClient.queueLoad(newItemArray, 0,
                MediaStatus.REPEAT_MODE_REPEAT_OFF, null);

    }

    public void playNextCast(MediaInfo mediaInfo) {

        //simpleExoPlayerView.setPlayer(castPlayer);
        mSimpleExoPlayerView.setUseController(false);
        mCastControlView.setVisibility(VISIBLE);
        mCastControlView.setPlayer(mCastPlayer);
        //simpleExoPlayerView.setDefaultArtwork();
        mCastControlView.setVisibilityListener(new PlaybackControlView.VisibilityListener() {
            @Override
            public void onVisibilityChange(int visibility) {
                if (visibility == GONE) {
                    mCastControlView.setVisibility(VISIBLE);
                    mChromeCastTv.setVisibility(VISIBLE);
                }
            }
        });
        CastSession castSession =
                CastContext.getSharedInstance(this).getSessionManager().getCurrentCastSession();

        if (castSession == null || !castSession.isConnected()) {
            Log.w(TAG, "showQueuePopup(): not connected to a cast device");
            return;
        }

        final RemoteMediaClient remoteMediaClient = castSession.getRemoteMediaClient();

        if (remoteMediaClient == null) {
            Log.w(TAG, "showQueuePopup(): null RemoteMediaClient");
            return;
        }
        MediaQueueItem queueItem = new MediaQueueItem.Builder(mediaInfo).setAutoplay(
                true).setPreloadTime(PRELOAD_TIME_S).build();
        MediaQueueItem[] newItemArray = new MediaQueueItem[]{queueItem};

        remoteMediaClient.queueLoad(newItemArray, 0,
                MediaStatus.REPEAT_MODE_REPEAT_OFF, null);
        mCastPlayer.setPlayWhenReady(true);

    }

    public MediaInfo getMediaInfo() {
        MediaMetadata movieMetadata = new MediaMetadata(MediaMetadata.MEDIA_TYPE_MOVIE);
        movieMetadata.putString(MediaMetadata.KEY_TITLE, mTitle);
        //movieMetadata.putString(MediaMetadata.KEY_ALBUM_ARTIST, "Test Artist");
        movieMetadata.addImage(new WebImage(Uri.parse(mCastImageUrl)));
        MediaInfo mediaInfo = new MediaInfo.Builder(mMediaUrl)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(MimeTypes.VIDEO_UNKNOWN)
                .setMetadata(movieMetadata).build();

        return mediaInfo;

    }

    public void downloadVideo(final String url) {

        if (Build.VERSION.SDK_INT >= 23) {
            if (checkPermission()) {
                // Code for above or equal 23 API Oriented Device
                // Your Permission granted already .Do next code
                Handler handler = new Handler();
                Runnable runnable = new Runnable() {
                    public void run() {
                        downloadFile(url);
                    }
                };
                handler.post(runnable);

            } else {
                requestPermission(); // Code for permission
            }
        } else {

            // Code for Below 23 API Oriented Device
            // Do next code

            Handler handler = new Handler();
            Runnable runnable = new Runnable() {
                public void run() {
                    downloadFile(url);
                }
            };
            handler.post(runnable);
        }
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (result == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            return false;
        }
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Toast.makeText(this, "Write External Storage permission allows us to do store images. Please allow this permission in App Settings.", Toast.LENGTH_LONG).show();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new ToastMsg(DetailsActivity.this).toastIconSuccess("Now You can download.");
                    Log.e("value", "Permission Granted, Now you can use local drive .");
                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                }
                break;
        }
    }

    public void downloadFile(String url) {
        String fileName = "";
        int notificationId = new Random().nextInt(100 - 1) - 1;
        Log.d("id:", notificationId + "");

        if (url == null || url.isEmpty()) {
            return;
        }

        if (mType.equals("movie")) {
            fileName = mTvName.getText().toString();
        } else {
            fileName = mSeriesTitle + "_" + mSeason + "_" + mEpisod;
        }

        String path = Constants.getDownloadDir(DetailsActivity.this);

        String fileExt = url.substring(url.lastIndexOf('.')); // output like .mkv
        fileName = fileName + fileExt;

        fileName = fileName.replaceAll(" ", "_");
        fileName = fileName.replaceAll(":", "_");

        File file = new File(path, "e_" + fileName); // e_ for encode
        if (file.exists()) {
            new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.file_already_downloaded));
            return;
        }

        //download with workManager
        String dir = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS).toString();
        Data data = new Data.Builder()
                .putString("url", url)
                .putString("dir", dir)
                .putString("fileName", fileName)
                .build();

        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(DownloadWorkManager.class)
                .setInputData(data)
                .build();

        String workId = request.getId().toString();
        Constants.workId = workId;
        WorkManager.getInstance().enqueue(request);
    }

    public void hideExoControlForTv() {
        mExoRewind.setVisibility(GONE);
        mExoForward.setVisibility(GONE);
        mLiveTv.setVisibility(VISIBLE);
        mSeekbarLayout.setVisibility(GONE);
    }

    public void showExoControlForTv() {
        mExoRewind.setVisibility(VISIBLE);
        mExoForward.setVisibility(VISIBLE);
        mLiveTv.setVisibility(GONE);
        mSeekbarLayout.setVisibility(VISIBLE);
        mWatchLiveTv.setVisibility(VISIBLE);
        mLiveTv.setVisibility(GONE);
        mWatchStatusTv.setText(getResources().getString(R.string.watching_catch_up_tv));
    }

    private void getScreenSize() {
        display = getWindowManager().getDefaultDisplay();
        size = new Point();
        display.getSize(size);
        sWidth = size.x;
        sHeight = size.y;
        //Toast.makeText(this, "fjiaf", Toast.LENGTH_SHORT).show();
    }

    public class RelativeLayoutTouchListener implements View.OnTouchListener {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:

                    //touch is start
                    downX = event.getX();
                    downY = event.getY();
                    if (event.getX() < (sWidth / 2)) {

                        //here check touch is screen left or right side
                        mIntLeft = true;
                        mIntRight = false;

                    } else if (event.getX() > (sWidth / 2)) {

                        //here check touch is screen left or right side
                        mIntLeft = false;
                        mIntRight = true;
                    }
                    break;

                case MotionEvent.ACTION_UP:

                case MotionEvent.ACTION_MOVE:

                    //finger move to screen
                    float x2 = event.getX();
                    float y2 = event.getY();

                    diffX = (long) (Math.ceil(event.getX() - downX));
                    diffY = (long) (Math.ceil(event.getY() - downY));

                    if (Math.abs(diffY) > Math.abs(diffX)) {
                        if (mIntLeft) {
                            //if left its for brightness

                            if (downY < y2) {
                                //down swipe brightness decrease
                            } else if (downY > y2) {
                                //up  swipe brightness increase
                            }

                        } else if (mIntRight) {

                            //if right its for audio
                            if (downY < y2) {
                                //down swipe volume decrease
                                mAudioManager.adjustVolume(AudioManager.ADJUST_LOWER, AudioManager.FLAG_PLAY_SOUND);

                            } else if (downY > y2) {
                                //up  swipe volume increase
                                mAudioManager.adjustVolume(AudioManager.ADJUST_RAISE, AudioManager.FLAG_PLAY_SOUND);
                            }
                        }
                    }
            }
            return true;
        }


    }

    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }


    @OnClick(R.id.img_back)
    void onBackClick() {
        if (mActiveMovie) {
            setPlayerNormalScreen();
            if (mPlayer != null) {
                mPlayer.setPlayWhenReady(false);
                mPlayer.stop();
            }
            showDescriptionLayout();
            mActiveMovie = false;
        } else {
            finish();
        }
    }

    @OnClick(R.id.img_subtitle)
    void onSubtitleClick() {
        showSubtitleDialog(DetailsActivity.this, mListSub);
    }

    @OnClick(R.id.btn_comment)
    void onCommentBtnClick() {
        if (!PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
            startActivity(new Intent(DetailsActivity.this, LoginActivity.class));
            new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.login_first));
        } else if (mEtComment.getText().toString().equals("")) {
            new ToastMsg(DetailsActivity.this).toastIconError(getString(R.string.comment_empty));
        } else {
            String comment = mEtComment.getText().toString();
            addComment(mId, PreferenceUtils.getUserId(DetailsActivity.this), comment);
        }
    }

    @OnClick({R.id.add_fav, R.id.add_fav2})
    void onFavClick() {
        if (mIsFav) {
            removeFromFav();
        } else {
            addToFav();
        }
    }

    @OnClick(R.id.img_full_scr)
    void onFullScrClick() {
        controlFullScreenPlayer();
    }

    @OnClick(R.id.volumn_control_iv)
    void onVolumnControlClick() {
        mVolumnControlLayout.setVisibility(VISIBLE);
    }

    @OnClick(R.id.aspect_ratio_iv)
    void onAspectRatioClick() {
        if (mAspectClickCount == 1) {
            //Toast.makeText(DetailsActivity.this, "Fill", Toast.LENGTH_SHORT).show();
            mSimpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FILL);
            mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mAspectClickCount = 2;
        } else if (mAspectClickCount == 2) {
            //Toast.makeText(DetailsActivity.this, "Fit", Toast.LENGTH_SHORT).show();
            mSimpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mAspectClickCount = 3;
        } else if (mAspectClickCount == 3) {
            //Toast.makeText(DetailsActivity.this, "Zoom", Toast.LENGTH_SHORT).show();
            mSimpleExoPlayerView.setResizeMode(AspectRatioFrameLayout.RESIZE_MODE_FIT);
            mPlayer.setVideoScalingMode(C.VIDEO_SCALING_MODE_SCALE_TO_FIT_WITH_CROPPING);
            mAspectClickCount = 1;
        }
    }

    @OnClick(R.id.external_player_iv)
    void onExternalPlayerClick() {
        if (mMediaUrl != null) {
            if (!tv) {
                // set player normal/ potrait screen if not tv
                mDescriptionLayout.setVisibility(VISIBLE);
                setPlayerNormalScreen();
            }
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse(mMediaUrl), "video/*");
            startActivity(Intent.createChooser(intent, "Complete action using"));
        }
    }

    @OnClick(R.id.watch_now_bt)
    void onWatchNowClick() {
        if (mType.equals("tvseries")) {
            if (mEpisodeAdapter != null) {
                mEpisodeAdapter.getWatchEpisode();
                return;
            }
            Toast.makeText(DetailsActivity.this, "Please select an episode", Toast.LENGTH_SHORT).show();
            return;
        }
        //PopUpAds.ShowAdmobInterstitialAds(DetailsActivity.this);
        if (PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
            if (!PreferenceUtils.isActivePlan(DetailsActivity.this)) {
                if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.START_APP)) {
                    PopUpAds.showAppodealInterstitialAds(DetailsActivity.this);
                } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                    PopUpAds.ShowAdmobInterstitialAds(DetailsActivity.this);
                }
            }
        }
        if (!mListServer.isEmpty()) {
            if (mListServer.size() == 1) {
                releasePlayer();
                resetCastPlayer();
                preparePlayer(mListServer.get(0));
                mDescriptionLayout.setVisibility(GONE);
                mLPlay.setVisibility(VISIBLE);
            } else {
                openServerDialog();
            }
        } else {
            Toast.makeText(DetailsActivity.this, R.string.no_video_found, Toast.LENGTH_SHORT).show();
        }
    }

    @OnClick(R.id.download_bt)
    void onDownloadClick() {
        if (PreferenceUtils.isLoggedIn(DetailsActivity.this)) {
            //PopUpAds.ShowAdmobInterstitialAds(DetailsActivity.this);
//                    DetailsActivity.getInstance().loadAdReward();
            //loadRewardedAd();

            if (!mListInternalDownload.isEmpty() || !mListExternalDownload.isEmpty()) {
                if (Config.ENABLE_DOWNLOAD_TO_ALL) {
                    openDownloadServerDialog();
                } else {
                    if (PreferenceUtils.isLoggedIn(DetailsActivity.this) && PreferenceUtils.isActivePlan(DetailsActivity.this)) {
                        openDownloadServerDialog();
                    } else {
                        Toast.makeText(DetailsActivity.this, R.string.download_not_permitted, Toast.LENGTH_SHORT).show();
                        Log.e("Download", "not permitted");
                    }
                }
            } else {
                Toast.makeText(DetailsActivity.this, R.string.no_download_server_found, Toast.LENGTH_SHORT).show();
            }
        } else {
            Intent intent = new Intent(DetailsActivity.this, FirebaseSignUpActivity.class);
            startActivity(intent);
        }
    }

    @OnClick(R.id.watch_live_tv)
    void onWatchLiveClick() {
        hideExoControlForTv();
        initMoviePlayer(mMediaUrl, mServerType, DetailsActivity.this);

        mWatchStatusTv.setText(getString(R.string.watching_on) + " " + getString(R.string.app_name));
        mWatchLiveTv.setVisibility(GONE);

        mTimeTv.setText(mCurrentProgramTime);
        mProgramTv.setText(mCurrentProgramTitle);
    }

    @OnClick({R.id.share_iv2, R.id.share_iv, R.id.share_iv3})
    void onShare2Click() {
        if (mTitle == null) {
            new ToastMsg(DetailsActivity.this).toastIconError("Title should not be empty.");
            return;
        }
        Tools.share(DetailsActivity.this, mTitle);
    }

    @OnClick(R.id.img_server)
    void onSeverClick() {
        openServerDialog();
    }

    @OnClick(R.id.subscribe_bt)
    void onSubscribeClick() {
        if (mUserId == null) {
            new ToastMsg(DetailsActivity.this).toastIconError(getResources().getString(R.string.subscribe_error));
            startActivity(new Intent(DetailsActivity.this, LoginActivity.class));
            finish();
        } else {
            startActivity(new Intent(DetailsActivity.this, PurchasePlanActivity.class));
        }
    }

    @OnClick({R.id.des_back_iv, R.id.back_iv})
    void onDesBackClick() {
        if (mActiveMovie) {
            setPlayerNormalScreen();
            if (mPlayer != null) {
                mPlayer.setPlayWhenReady(false);
                mPlayer.stop();
            }
            showDescriptionLayout();
            mActiveMovie = false;
        } else {
            releasePlayer();
            finish();
        }

    }
}

