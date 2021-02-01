package tvseries.koreandramaengsub.freemovieapp.fragments;


import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appodeal.ads.Appodeal;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.github.islamkhsh.CardSliderViewPager;
import com.google.android.ads.nativetemplates.TemplateView;
import com.ixidev.gdpr.GDPRChecker;
import com.viewpagerindicator.CirclePageIndicator;
import com.viewpagerindicator.LinePageIndicator;
import com.viewpagerindicator.TabPageIndicator;
import com.viewpagerindicator.TitlePageIndicator;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import tvseries.koreandramaengsub.freemovieapp.Config;
import tvseries.koreandramaengsub.freemovieapp.ItemMovieActivity;
import tvseries.koreandramaengsub.freemovieapp.ItemSeriesActivity;
import tvseries.koreandramaengsub.freemovieapp.MainActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.adapters.CountryAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.GenreAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.GenreHomeAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.HomePageAdapter;
import tvseries.koreandramaengsub.freemovieapp.adapters.SliderAdapter;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.models.CommonModels;
import tvseries.koreandramaengsub.freemovieapp.models.GenreModel;
import tvseries.koreandramaengsub.freemovieapp.models.home_content.AllCountry;
import tvseries.koreandramaengsub.freemovieapp.models.home_content.AllGenre;
import tvseries.koreandramaengsub.freemovieapp.models.home_content.FeaturesGenreAndMovie;
import tvseries.koreandramaengsub.freemovieapp.models.home_content.HomeContent;
import tvseries.koreandramaengsub.freemovieapp.models.home_content.LatestMovie;
import tvseries.koreandramaengsub.freemovieapp.models.home_content.LatestTvseries;
import tvseries.koreandramaengsub.freemovieapp.models.home_content.Slider;
import tvseries.koreandramaengsub.freemovieapp.models.home_content.TopviewTvseries;
import tvseries.koreandramaengsub.freemovieapp.models.home_content.Video;
import tvseries.koreandramaengsub.freemovieapp.network.RetrofitClient;
import tvseries.koreandramaengsub.freemovieapp.network.apis.HomeContentApi;
import tvseries.koreandramaengsub.freemovieapp.network.model.config.AdsConfig;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.NetworkInst;
import tvseries.koreandramaengsub.freemovieapp.utils.PreferenceUtils;
import tvseries.koreandramaengsub.freemovieapp.utils.ads.BannerAds;
import tvseries.koreandramaengsub.freemovieapp.utils.ads.NativeAds;
import tvseries.koreandramaengsub.freemovieapp.view.SwipeRefreshLayout;


public class HomeFragment extends Fragment {
    @BindView(R.id.adView) RelativeLayout mAdView;
    @BindView(R.id.adView1) RelativeLayout mAdView1;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout mShimmerLayout;
    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.scrollView) NestedScrollView mScrollView;
    @BindView(R.id.slider_layout) View mSliderLayout;
    @BindView(R.id.genre_rv) RecyclerView mGenreRv;
    @BindView(R.id.country_rv) RecyclerView mCountryRv;
    @BindView(R.id.genre_layout) RelativeLayout mGenreLayout;
    @BindView(R.id.country_layout) RelativeLayout mCountryLayout;
    @BindView(R.id.admob_nativead_template) TemplateView admobNativeAdView;
    @BindView(R.id.admob_nativead_template_1) TemplateView admobNativeAdView_1;
    @BindView(R.id.c_viewPager) CardSliderViewPager mCViewPager;
    @BindView(R.id.recyclerView) RecyclerView mRecyclerViewMovie;
    @BindView(R.id.recyclerViewTvSeries) RecyclerView mRecyclerViewTvSeries;
    @BindView(R.id.recyclerView_by_genre) RecyclerView mRecyclerViewGenre;
    @BindView(R.id.recyclerViewTopviewTvSeries) RecyclerView mRecyclerViewTopviewTvSeries;
    @BindView(R.id.title_pager_indicator) LinePageIndicator mPagerIndicator;

    private ArrayList<CommonModels> listSlider = new ArrayList<>();
    private Timer timer;
    private GenreAdapter genreAdapter;
    private CountryAdapter countryAdapter;
    private HomePageAdapter adapterMovie, adapterSeries, adapterTopviewSeries;
    private List<CommonModels> listMovie = new ArrayList<>();
    private List<CommonModels> listTv = new ArrayList<>();
    private List<CommonModels> listSeries = new ArrayList<>();
    private List<CommonModels> listTopViewSeries = new ArrayList<>();
    private List<CommonModels> genreList = new ArrayList<>();
    private List<CommonModels> countryList = new ArrayList<>();
    private List<GenreModel> listGenre = new ArrayList<>();
    private GenreHomeAdapter genreHomeAdapter;
    private MainActivity mActivity;
    private DatabaseHelper db = new DatabaseHelper(getContext());
    Unbinder mUnbinder;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mActivity = (MainActivity) getActivity();
        mUnbinder = ButterKnife.bind(this, view);
        mActivity.setTitle(getResources().getString(R.string.home));
        mSwipeRefreshLayout.setToolbar(mActivity.getToolbar());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = new DatabaseHelper(getContext());

        if (db.getConfigurationData().getAppConfig().getGenreVisible()) {
            mGenreLayout.setVisibility(View.VISIBLE);
        }
        if (db.getConfigurationData().getAppConfig().getCountryVisible()) {
            mCountryLayout.setVisibility(View.VISIBLE);
        }
        //----init timer slider--------------------
        timer = new Timer();
        // --- genre recycler view ---------
        mGenreRv.setLayoutManager(new LinearLayoutManager(mActivity, RecyclerView.HORIZONTAL, false));
        mGenreRv.setHasFixedSize(true);
        mGenreRv.setNestedScrollingEnabled(false);
        genreAdapter = new GenreAdapter(getActivity(), genreList, "genre", "home");
        mGenreRv.setAdapter(genreAdapter);

        // --- country recycler view ---------
        mCountryRv.setLayoutManager(new LinearLayoutManager(mActivity, RecyclerView.HORIZONTAL, false));
        mCountryRv.setHasFixedSize(true);
        mCountryRv.setNestedScrollingEnabled(false);
        countryAdapter = new CountryAdapter(getActivity(), countryList, "home");
        mCountryRv.setAdapter(countryAdapter);

        //----featured tv recycler view-----------------
//        recyclerViewTv = view.findViewById(R.id.recyclerViewTv);
//        recyclerViewTv.setLayoutManager(new LinearLayoutManager(activity, LinearLayoutManager.HORIZONTAL, false));
//        recyclerViewTv.setHasFixedSize(true);
//        recyclerViewTv.setNestedScrollingEnabled(false);
//        adapterTv = new LiveTvHomeAdapter(getContext(), listTv, "MainActivity");
//        recyclerViewTv.setAdapter(adapterTv);

        //----movie's recycler view-----------------
        mRecyclerViewMovie.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewMovie.setHasFixedSize(true);
        mRecyclerViewMovie.setNestedScrollingEnabled(false);
        adapterMovie = new HomePageAdapter(getContext(), listMovie);
        mRecyclerViewMovie.setAdapter(adapterMovie);

        //----series's recycler view-----------------
        mRecyclerViewTvSeries.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewTvSeries.setHasFixedSize(true);
        mRecyclerViewTvSeries.setNestedScrollingEnabled(false);
        adapterSeries = new HomePageAdapter(getActivity(), listSeries);
        mRecyclerViewTvSeries.setAdapter(adapterSeries);

        //----topview series's recycler view-----------------
        mRecyclerViewTopviewTvSeries.setLayoutManager(new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false));
        mRecyclerViewTopviewTvSeries.setHasFixedSize(true);
        mRecyclerViewTopviewTvSeries.setNestedScrollingEnabled(false);
        adapterTopviewSeries = new HomePageAdapter(getActivity(), listTopViewSeries);
        mRecyclerViewTopviewTvSeries.setAdapter(adapterTopviewSeries);



        //----genre's recycler view--------------------
        mRecyclerViewGenre.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerViewGenre.setHasFixedSize(true);
        mRecyclerViewGenre.setNestedScrollingEnabled(false);
        genreHomeAdapter = new GenreHomeAdapter(getContext(), listGenre);
        mRecyclerViewGenre.setAdapter(genreHomeAdapter);

        mShimmerLayout.startShimmer();

        if (new NetworkInst(getContext()).isNetworkAvailable()) {

            getHomeContent();

        } else {
            mShimmerLayout.stopShimmer();
            mShimmerLayout.setVisibility(View.GONE);
            mActivity.setFailure(true,getString(R.string.no_internet));
            mScrollView.setVisibility(View.GONE);
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
               /* mRecyclerViewMovie.removeAllViews();
//                recyclerViewTv.removeAllViews();
                mRecyclerViewTvSeries.removeAllViews();
                mRecyclerViewTopviewTvSeries.removeAllViews();
                mRecyclerViewGenre.removeAllViews();
                mGenreRv.removeAllViews();
                mCountryRv.removeAllViews();*/

                genreList.clear();
                countryList.clear();
                listMovie.clear();
                listSeries.clear();
                listTopViewSeries.clear();
                listSlider.clear();
                listTv.clear();
                listGenre.clear();


                if (new NetworkInst(getContext()).isNetworkAvailable()) {

                    getHomeContent();

                } else {
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mActivity.setFailure(true,getString(R.string.no_internet));
                    mScrollView.setVisibility(View.GONE);
                }
            }
        });


        mScrollView.setOnScrollChangeListener(new NestedScrollView.OnScrollChangeListener() {
            @Override
            public void onScrollChange(NestedScrollView v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                if (scrollY < oldScrollY) { // up
                    mActivity.animateSearchBar(false);
                }
                if (scrollY > oldScrollY) { // down
                    mActivity.animateSearchBar(true);
                }
            }
        });
//TRUNG
        getAdDetails();
    }


    private void getHomeContent() {
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        HomeContentApi api = retrofit.create(HomeContentApi.class);
        Call<HomeContent> call = api.getHomeContent(Config.API_KEY);
        call.enqueue(new Callback<HomeContent>() {
            @Override
            public void onResponse(Call<HomeContent> call, retrofit2.Response<HomeContent> response) {
                 if (response.code() == 200){
                     if(mSwipeRefreshLayout ==null){
                         return;
                     }
                    mSwipeRefreshLayout.setRefreshing(false);
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(View.GONE);
                    mScrollView.setVisibility(View.VISIBLE);
                    mActivity.setFailure(false);

                    //slider data
                    Slider slider = response.body().getSlider();
                    if (slider.getSliderType().equalsIgnoreCase("disable")) {
                        mSliderLayout.setVisibility(View.GONE);
                    }else if (slider.getSliderType().equalsIgnoreCase("movie")){

                    }else if (slider.getSliderType().equalsIgnoreCase("image")){

                    }

                    SliderAdapter sliderAdapter = new SliderAdapter(slider.getSlide());
                    mCViewPager.setAdapter(sliderAdapter);
                    mPagerIndicator.setViewPager(mCViewPager);
                    sliderAdapter.notifyDataSetChanged();

                    //genre data
                     if (db.getConfigurationData().getAppConfig().getGenreVisible()) {
                         for (int i = 0; i < response.body().getAllGenre().size(); i++) {
                             AllGenre genre = response.body().getAllGenre().get(i);
                             CommonModels models = new CommonModels();
                             models.setId(genre.getGenreId());
                             models.setTitle(genre.getName());
                             models.setImageUrl(genre.getImageUrl());
                             genreList.add(models);
                         }
                         genreAdapter.notifyDataSetChanged();
                     }

                     //country data
                     if (db.getConfigurationData().getAppConfig().getCountryVisible()) {
                         for (int i = 0; i < response.body().getAllCountry().size(); i++) {
                             AllCountry country = response.body().getAllCountry().get(i);
                             CommonModels models = new CommonModels();
                             models.setId(country.getCountryId());
                             models.setTitle(country.getName());
                             models.setImageUrl(country.getImageUrl());
                             countryList.add(models);
                         }
                         countryAdapter.notifyDataSetChanged();
                     }

                     //tv channel data
//                     for (int i = 0; i < response.body().getFeaturedTvChannel().size(); i++){
//                         FeaturedTvChannel tvChannel = response.body().getFeaturedTvChannel().get(i);
//                         CommonModels models = new CommonModels();
//                         models.setImageUrl(tvChannel.getPosterUrl());
//                         models.setTitle(tvChannel.getTvName());
//                         models.setVideoType("tv");
//                         models.setId(tvChannel.getLiveTvId());
//                         models.setIsPaid(tvChannel.getIsPaid());
//                         listTv.add(models);
//                     }
//                     adapterTv.notifyDataSetChanged();

                     //latest movies data
                     for (int i = 0; i < response.body().getLatestMovies().size(); i++){
                         LatestMovie movie = response.body().getLatestMovies().get(i);
                         CommonModels models = new CommonModels();
                         models.setImageUrl(movie.getThumbnailUrl());
                         models.setTitle(movie.getTitle());
                         models.setVideoType("movie");
                         models.setReleaseDate(movie.getRelease());
                         models.setQuality(movie.getVideoQuality());
                         models.setId(movie.getVideosId());
                         models.setIsPaid(movie.getIsPaid());
                         listMovie.add(models);
                     }
                     adapterMovie.notifyDataSetChanged();

                     //latest tv series
                     for (int i = 0; i < response.body().getLatestTvseries().size(); i++){
                         LatestTvseries tvSeries = response.body().getLatestTvseries().get(i);
                         CommonModels models = new CommonModels();
                         models.setImageUrl(tvSeries.getThumbnailUrl());
                         models.setTitle(tvSeries.getTitle());
                         models.setVideoType("tvseries");
                         models.setReleaseDate(tvSeries.getRelease());
                         models.setQuality(tvSeries.getVideoQuality());
                         models.setId(tvSeries.getVideosId());
                         models.setIsPaid(tvSeries.getIsPaid());
                         listSeries.add(models);
                     }
                     adapterSeries.notifyDataSetChanged();

                     //topview
                    for (int i = 0; i < response.body().getTopviewTvseries().size(); i++){
                         TopviewTvseries topviewtvSeries = response.body().getTopviewTvseries().get(i);
                         CommonModels models = new CommonModels();
                         models.setImageUrl(topviewtvSeries.getThumbnailUrl());
                         models.setTitle(topviewtvSeries.getTitle());
                         models.setVideoType("tvseries");
                         models.setReleaseDate(topviewtvSeries.getRelease());
                         models.setQuality(topviewtvSeries.getVideoQuality());
                         models.setId(topviewtvSeries.getVideosId());
                         models.setIsPaid(topviewtvSeries.getIsPaid());
                         listTopViewSeries.add(models);
                     }
                     adapterTopviewSeries.notifyDataSetChanged();

                     //get data by genre
                     for (int i = 0; i < response.body().getFeaturesGenreAndMovie().size(); i++){
                         FeaturesGenreAndMovie genreAndMovie = response.body().getFeaturesGenreAndMovie().get(i);
                         GenreModel models = new GenreModel();

                         models.setName(genreAndMovie.getName());
                         models.setId(genreAndMovie.getGenreId());
                         List<CommonModels> listGenreMovie = new ArrayList<>();
                         for (int j = 0; j < genreAndMovie.getVideos().size(); j++){
                             Video video = genreAndMovie.getVideos().get(j);
                             CommonModels commonModels = new CommonModels();

                             commonModels.setId(video.getVideosId());
                             commonModels.setTitle(video.getTitle());
                             commonModels.setIsPaid(video.getIsPaid());

                             if (video.getIsTvseries().equals("0")) {
                                 commonModels.setVideoType("movie");
                             } else {
                                 commonModels.setVideoType("tvseries");
                             }

                             commonModels.setReleaseDate(video.getRelease());
                             commonModels.setQuality(video.getVideoQuality());
                             commonModels.setImageUrl(video.getThumbnailUrl());

                             listGenreMovie.add(commonModels);
                         }
                         models.setList(listGenreMovie);

                         listGenre.add(models);
                         genreHomeAdapter.notifyDataSetChanged();
                     }

                }else {
                     mSwipeRefreshLayout.setRefreshing(false);
                     mShimmerLayout.stopShimmer();
                     mShimmerLayout.setVisibility(View.GONE);
                     mActivity.setFailure(false);
                     mScrollView.setVisibility(View.GONE);
                 }

            }

            @Override
            public void onFailure(Call<HomeContent> call, Throwable t) {
                mSwipeRefreshLayout.setRefreshing(false);
                mShimmerLayout.stopShimmer();
                mShimmerLayout.setVisibility(View.GONE);
                mActivity.setFailure(false);
                mScrollView.setVisibility(View.GONE);

            }
        });
    }

    private void loadAd() {
        AdsConfig adsConfig = new DatabaseHelper(getContext()).getConfigurationData().getAdsConfig();
        if (PreferenceUtils.isLoggedIn(mActivity)) {
            if (!PreferenceUtils.isActivePlan(mActivity)) {
                if (adsConfig.getAdsEnable().equals("1")) {

                    if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                        //BannerAds.ShowAdmobBannerAds(getContext(), mAdView);
                       // BannerAds.ShowAdmobBannerAds(getContext(), mAdView1);

                        admobNativeAdView.setVisibility(View.VISIBLE);
                        NativeAds.showAdmobNativeAds(getActivity(), admobNativeAdView);

                        admobNativeAdView_1.setVisibility(View.VISIBLE);
                        NativeAds.showAdmobNativeAds(getActivity(), admobNativeAdView_1);

                    } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.START_APP)) {
                        // BannerAds.showStartAppBanner(getContext(), adView);
                        Appodeal.setBannerViewId(R.id.appodealBannerView1_home);
                        Appodeal.show(mActivity, Appodeal.BANNER_VIEW);
                    } else if(adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.NETWORK_AUDIENCE)) {
                        BannerAds.showFANBanner(getContext(), mAdView);
                        BannerAds.showFANBanner(getContext(), mAdView1);
                    }
                }
            }
        }else{
            if (adsConfig.getAdsEnable().equals("1")) {

                if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                    BannerAds.ShowAdmobBannerAds(getContext(), mAdView);
                    BannerAds.ShowAdmobBannerAds(getContext(), mAdView1);

                } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.START_APP)) {
                    // BannerAds.showStartAppBanner(getContext(), adView);
                    Appodeal.setBannerViewId(R.id.appodealBannerView1_home);
                    Appodeal.show(mActivity, Appodeal.BANNER_VIEW);
                } else if(adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.NETWORK_AUDIENCE)) {
                    BannerAds.showFANBanner(getContext(), mAdView);
                    BannerAds.showFANBanner(getContext(), mAdView1);
                }
            }
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
        NativeAds.releaseAdmobNativeAd();
    }

    private void getAdDetails() {
        DatabaseHelper db = new DatabaseHelper(getContext());
        AdsConfig adsConfig = db.getConfigurationData().getAdsConfig();

        new GDPRChecker()
                .withContext(mActivity)
                .withPrivacyUrl(Config.TERMS_URL) // your privacy url
                .withPublisherIds(adsConfig.getAdmobAppId()) // your admob account Publisher id
                //.withTestMode("9424DF76F06983D1392E609FC074596C") // remove this on real project
                .check();

        loadAd();
    }

    @Override
    public void onStart() {
        super.onStart();

        mShimmerLayout.startShimmer();
    }

    @Override
    public void onPause() {
        super.onPause();
        mShimmerLayout.stopShimmer();
        timer.cancel();
    }

    @Override
    public void onResume() {
        super.onResume();
    }
    @OnClick({R.id.btn_more_movie,R.id.movie_layout})
    void onBtnMoreMovieClick(){
        Intent intent = new Intent(getContext(), ItemMovieActivity.class);
        intent.putExtra("title", "Movies");
        getActivity().startActivity(intent);
    }
    @OnClick({R.id.btn_more_series,R.id.btn_more_series1})
    void onBtnMoreSeriesClick(){
        Intent intent = new Intent(getContext(), ItemSeriesActivity.class);
        intent.putExtra("title", "TV Series");
        getActivity().startActivity(intent);
    }
}
