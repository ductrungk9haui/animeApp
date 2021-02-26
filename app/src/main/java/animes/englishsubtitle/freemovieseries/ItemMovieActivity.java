package animes.englishsubtitle.freemovieseries;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.analytics.FirebaseAnalytics;

import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.adapters.CommonGridAdapter;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.models.CommonModels;
import animes.englishsubtitle.freemovieseries.models.home_content.Video;
import animes.englishsubtitle.freemovieseries.network.RetrofitClient;
import animes.englishsubtitle.freemovieseries.network.apis.MovieApi;
import animes.englishsubtitle.freemovieseries.network.model.config.AdsConfig;
import animes.englishsubtitle.freemovieseries.utils.Constants;
import animes.englishsubtitle.freemovieseries.utils.NetworkInst;
import animes.englishsubtitle.freemovieseries.utils.PreferenceUtils;
import animes.englishsubtitle.freemovieseries.utils.RtlUtils;
import animes.englishsubtitle.freemovieseries.utils.ads.BannerAds;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;

public class ItemMovieActivity extends AppCompatActivity {


    private ShimmerFrameLayout shimmerFrameLayout;
    private RecyclerView recyclerView;
    private CommonGridAdapter mAdapter;
    private List<CommonModels> list =new ArrayList<>();

    private boolean isLoading=false;
    private ProgressBar progressBar;
    private int pageCount=1;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String id="",type="";
    private CoordinatorLayout coordinatorLayout;
    private TextView tvNoItem;
    private RelativeLayout adView;
    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        RtlUtils.setScreenDirection(this);
        SharedPreferences sharedPreferences = getSharedPreferences("push", MODE_PRIVATE);
        boolean isDark = sharedPreferences.getBoolean("dark", false);

        if (isDark) {
            setTheme(R.style.AppThemeDark);
        } else {
            setTheme(R.style.AppThemeLight);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_show);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //---analytics-----------
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        Bundle bundle = new Bundle();
        bundle.putString(FirebaseAnalytics.Param.ITEM_ID, "id");
        bundle.putString(FirebaseAnalytics.Param.ITEM_NAME, "movie_activity");
        bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "activity");
        mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle);


        getSupportActionBar().setTitle(getIntent().getStringExtra("title"));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        adView=findViewById(R.id.adView);
        coordinatorLayout=findViewById(R.id.coordinator_lyt);
        progressBar=findViewById(R.id.item_progress_bar);
        shimmerFrameLayout=findViewById(R.id.shimmer_view_container);
        shimmerFrameLayout.startShimmer();

        swipeRefreshLayout=findViewById(R.id.swipe_layout);
        tvNoItem=findViewById(R.id.tv_noitem);


        //----movie's recycler view-----------------
        recyclerView = findViewById(R.id.recyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 3);
        if( !(PreferenceUtils.isLoggedIn(this) && PreferenceUtils.isActivePlan(this))){
            gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
                @Override
                public int getSpanSize(int position) {
                    switch ((position + 1) % 10) {
                        case 0:
                            return 3;
                        default:
                            return 1;
                    }
                }
            });
        }

        recyclerView.setLayoutManager(gridLayoutManager);
        //recyclerView.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(this, 8), true));
        recyclerView.setHasFixedSize(true);
        recyclerView.setNestedScrollingEnabled(false);

        mAdapter = new CommonGridAdapter(this,this, list);
        recyclerView.setAdapter(mAdapter);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !isLoading) {

                    pageCount=pageCount+1;
                    isLoading = true;

                    progressBar.setVisibility(View.VISIBLE);

                    getMovie(pageCount);
                }
            }
        });

        id = getIntent().getStringExtra("id");
        type =getIntent().getStringExtra("type");

        if (new NetworkInst(this).isNetworkAvailable()){
            initData();
        }else {
            tvNoItem.setText(getString(R.string.no_internet));
            shimmerFrameLayout.stopShimmer();
            shimmerFrameLayout.setVisibility(View.GONE);
            coordinatorLayout.setVisibility(View.VISIBLE);
        }


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                coordinatorLayout.setVisibility(View.GONE);

                pageCount=1;

                list.clear();
                recyclerView.removeAllViews();
                mAdapter.setNotifyDataSetChanged();

                if (new NetworkInst(ItemMovieActivity.this).isNetworkAvailable()){
                    initData();
                }else {
                    tvNoItem.setText(getString(R.string.no_internet));
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });

        //loadAd();
    }


    private void initData(){
        if (id == null){
            getMovie(pageCount);
        }else if (type.equals("country")){
            getMovieByCountryId(id, pageCount);
        }else {
            getMovieByGenreId(id, pageCount);
        }

    }

    private void getMovieByGenreId(String id, int pageNum) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        MovieApi api = retrofit.create(MovieApi.class);
        Call<List<Video>> call = api.getMovieByGenreId(Config.API_KEY, id, pageNum);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, retrofit2.Response<List<Video>> response) {
                if (response.code() == 200){
                    isLoading=false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.body().size() == 0 && pageCount==1){
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }else {
                        coordinatorLayout.setVisibility(View.GONE);
                    }
                    boolean newAdd = false;
                    for (int i = 0; i < response.body().size(); i++){
                        Video video = response.body().get(i);
                        CommonModels models =new CommonModels();
                        models.setImageUrl(video.getThumbnailUrl());
                        models.setTitle(video.getTitle());
                        models.setQuality(video.getVideoQuality());
                        models.setIsPaid(video.getIsPaid());
                        models.setReleaseDate(video.getRelease());
                        if (video.getIsTvseries().equals("1") ) {
                            models.setVideoType("tvseries");
                        } else {
                            models.setVideoType("movie");
                        }
                        newAdd = true;

                        models.setId(video.getVideosId());
                        list.add(models);
                    }
                    if(newAdd){
                        mAdapter.setNotifyDataSetChanged();
                    }
                }else {
                    isLoading=false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (pageCount==1){
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                isLoading=false;
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (pageCount==1){
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void getMovieByCountryId(String id, int pageNum) {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        MovieApi api = retrofit.create(MovieApi.class);
        Call<List<Video>> call = api.getMovieByCountryId(Config.API_KEY, id, pageNum);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, retrofit2.Response<List<Video>> response) {
                if (response.code() == 200){
                    isLoading=false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.body().size() == 0 && pageCount==1){
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }else {
                        coordinatorLayout.setVisibility(View.GONE);
                    }
                    boolean newAdd = false;
                    for (int i = 0; i < response.body().size(); i++){
                        Video video = response.body().get(i);
                        CommonModels models =new CommonModels();
                        models.setImageUrl(video.getThumbnailUrl());
                        models.setTitle(video.getTitle());
                        models.setQuality(video.getVideoQuality());
                        models.setReleaseDate(video.getRelease());
                        if (video.getIsTvseries().equals("1") ) {
                            models.setVideoType("tvseries");
                        } else {
                            models.setVideoType("movie");
                        }

                        newAdd =true;
                        models.setId(video.getVideosId());
                        list.add(models);
                    }

                    if(newAdd){
                        mAdapter.setNotifyDataSetChanged();
                    }
                }else {
                    isLoading=false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (pageCount==1){
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                isLoading=false;
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (pageCount==1){
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private void loadAd(){
        AdsConfig adsConfig = new DatabaseHelper(ItemMovieActivity.this).getConfigurationData().getAdsConfig();
        if (PreferenceUtils.isLoggedIn(this)) {
            if (!PreferenceUtils.isActivePlan(this)) {
                if (adsConfig.getAdsEnable().equals("1")) {
                    if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                        BannerAds.ShowAdmobBannerAds(ItemMovieActivity.this, adView);
                    } else if (adsConfig.getMobileAdsNetwork().equals(Constants.START_APP)) {
                        //BannerAds.showStartAppBanner(ItemMovieActivity.this, adView);
                        BannerAds.showAppodealBanner(ItemMovieActivity.this,R.id.appodealBannerView_movie);
                    } else if(adsConfig.getMobileAdsNetwork().equals(Constants.NETWORK_AUDIENCE)) {
                        BannerAds.showFANBanner(ItemMovieActivity.this, adView);
                    }
                }
            }
        }else{
            if (adsConfig.getAdsEnable().equals("1")) {
                if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                    BannerAds.ShowAdmobBannerAds(ItemMovieActivity.this, adView);
                } else if (adsConfig.getMobileAdsNetwork().equals(Constants.START_APP)) {
                    //BannerAds.showStartAppBanner(ItemMovieActivity.this, adView);
                    BannerAds.showAppodealBanner(ItemMovieActivity.this,R.id.appodealBannerView_movie);
                } else if(adsConfig.getMobileAdsNetwork().equals(Constants.NETWORK_AUDIENCE)) {
                    BannerAds.showFANBanner(ItemMovieActivity.this, adView);
                }
            }
        }
    }


    private void getMovie(int pageNum){
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        MovieApi api = retrofit.create(MovieApi.class);
        Call<List<Video>> call = api.getMovies(Config.API_KEY, pageNum);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, retrofit2.Response<List<Video>> response) {
                if (response.code() == 200){
                    isLoading=false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);

                    if (response.body().size() == 0 && pageCount==1){
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }else {
                        coordinatorLayout.setVisibility(View.GONE);
                    }
                    boolean newAdd = false;
                    for (int i = 0; i < response.body().size(); i++){
                        Video video = response.body().get(i);
                        CommonModels models =new CommonModels();
                        models.setImageUrl(video.getThumbnailUrl());
                        models.setTitle(video.getTitle());
                        models.setQuality(video.getVideoQuality());
                        models.setReleaseDate(video.getRelease());
                        if (video.getIsTvseries().equals("1") ) {
                            models.setVideoType("tvseries");
                        } else {
                            models.setVideoType("movie");
                        }

                        newAdd = true;
                        models.setId(video.getVideosId());
                        list.add(models);
                    }
                    if(newAdd){
                        mAdapter.setNotifyDataSetChanged();
                    }

                }else {
                    isLoading=false;
                    progressBar.setVisibility(View.GONE);
                    shimmerFrameLayout.stopShimmer();
                    shimmerFrameLayout.setVisibility(View.GONE);
                    swipeRefreshLayout.setRefreshing(false);
                    if (pageCount==1){
                        coordinatorLayout.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                isLoading=false;
                progressBar.setVisibility(View.GONE);
                shimmerFrameLayout.stopShimmer();
                shimmerFrameLayout.setVisibility(View.GONE);
                swipeRefreshLayout.setRefreshing(false);
                if (pageCount==1){
                    coordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}
