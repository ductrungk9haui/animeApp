package animes.englishsubtitle.freemovieseries.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.appodeal.ads.Appodeal;
import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import animes.englishsubtitle.freemovieseries.Config;
import animes.englishsubtitle.freemovieseries.MainActivity;
import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.adapters.CommonGridAdapter;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.models.CommonModels;
import animes.englishsubtitle.freemovieseries.models.home_content.Video;
import animes.englishsubtitle.freemovieseries.network.RetrofitClient;
import animes.englishsubtitle.freemovieseries.network.apis.TvSeriesApi;
import animes.englishsubtitle.freemovieseries.network.model.config.AdsConfig;
import animes.englishsubtitle.freemovieseries.utils.ApiResources;
import animes.englishsubtitle.freemovieseries.utils.Constants;
import animes.englishsubtitle.freemovieseries.utils.NetworkInst;
import animes.englishsubtitle.freemovieseries.utils.PreferenceUtils;
import animes.englishsubtitle.freemovieseries.utils.ads.BannerAds;
import animes.englishsubtitle.freemovieseries.view.SwipeRefreshLayout;

public class TvSeriesFragment extends Fragment {
    @BindView(R.id.adView) RelativeLayout mAdView;
    @BindView(R.id.item_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout mShimmerLayout;
    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    Unbinder mUnbinder;
    private CommonGridAdapter mAdapter;
    private List<CommonModels> mListCommonModels =new ArrayList<>();
    private ApiResources mApiResources;
    private boolean mIsLoading =false;
    private int mPageCount =1;
    private MainActivity mActivity;
    private static final int HIDE_THRESHOLD = 20;
    private int mScrolledDistance = 0;
    private boolean mControlsVisible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tvseries, container, false);
        mActivity = (MainActivity) getActivity();
        mUnbinder = ButterKnife.bind(this,view);
        mActivity.setTitle(getResources().getString(R.string.tv_series));
        mSwipeRefreshLayout.setToolbar(mActivity.getToolbar());
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponent(view);
    }

    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
    private void initComponent(View view) {
        mApiResources =new ApiResources();
        mShimmerLayout.startShimmer();
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 3);
        if( !(PreferenceUtils.isLoggedIn(getContext()) && PreferenceUtils.isActivePlan(getContext()))){
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
        mRecyclerView.setLayoutManager(gridLayoutManager);
       // mRecyclerView.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(getActivity(), 0), true));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mAdapter = new CommonGridAdapter(getActivity(),getContext(), mListCommonModels);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);

                if (!recyclerView.canScrollVertically(1) && !mIsLoading) {
                    mActivity.setFailure(false);
                    mPageCount = mPageCount +1;
                    mIsLoading = true;

                    mProgressBar.setVisibility(View.VISIBLE);

                    getTvSeriesData(mPageCount);
                }
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                if (mScrolledDistance > HIDE_THRESHOLD && mControlsVisible) {
                    mActivity.animateSearchBar(true);
                    mControlsVisible = false;
                    mScrolledDistance = 0;
                } else if (mScrolledDistance < -HIDE_THRESHOLD && !mControlsVisible) {
                    mActivity.animateSearchBar(false);
                    mControlsVisible = true;
                    mScrolledDistance = 0;
                }

                if((mControlsVisible && dy>0) || (!mControlsVisible && dy<0)) {
                    mScrolledDistance += dy;
                }


            }
        });

        if (new NetworkInst(getContext()).isNetworkAvailable()){
            getTvSeriesData(mPageCount);
        }else {
            mShimmerLayout.stopShimmer();
            mShimmerLayout.setVisibility(View.GONE);
            mActivity.setFailure(true,getResources().getString(R.string.no_internet));
        }


        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                mPageCount =1;
                mActivity.setFailure(false);
                mListCommonModels.clear();
                mRecyclerView.removeAllViews();
                mAdapter.setNotifyDataSetChanged();
                if (new NetworkInst(getContext()).isNetworkAvailable()){
                    getTvSeriesData(mPageCount);
                }else {
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mActivity.setFailure(true,getResources().getString(R.string.no_internet));
                }
            }
        });

        //loadAd();

    }


    @Override
    public void onStart() {
        super.onStart();
    }

    private void loadAd(){
        AdsConfig adsConfig = new DatabaseHelper(getContext()).getConfigurationData().getAdsConfig();
        if (PreferenceUtils.isLoggedIn(mActivity)) {
            if (!PreferenceUtils.isActivePlan(mActivity)) {
                if (adsConfig.getAdsEnable().equals("1")) {
                    if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                        BannerAds.ShowAdmobBannerAds(mActivity, mAdView);
                    } else if (adsConfig.getMobileAdsNetwork().equals(Constants.START_APP)) {
                        // BannerAds.showStartAppBanner(activity, adView);
                        Appodeal.setBannerViewId(R.id.appodealBannerView_fragment_tvseries);
                        Appodeal.show(mActivity,Appodeal.BANNER_VIEW);
                    } else if(adsConfig.getMobileAdsNetwork().equals(Constants.NETWORK_AUDIENCE)) {
                        BannerAds.showFANBanner(getContext(), mAdView);
                    }
                }
            }
        }else{
            if (adsConfig.getAdsEnable().equals("1")) {
                if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                    BannerAds.ShowAdmobBannerAds(mActivity, mAdView);
                } else if (adsConfig.getMobileAdsNetwork().equals(Constants.START_APP)) {
                    // BannerAds.showStartAppBanner(activity, adView);
                    Appodeal.setBannerViewId(R.id.appodealBannerView_fragment_tvseries);
                    Appodeal.show(mActivity,Appodeal.BANNER_VIEW);
                } else if(adsConfig.getMobileAdsNetwork().equals(Constants.NETWORK_AUDIENCE)) {
                    BannerAds.showFANBanner(getContext(), mAdView);
                }
            }
        }

    }

    private void getTvSeriesData(int pageNum){
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        TvSeriesApi api = retrofit.create(TvSeriesApi.class);
        Call<List<Video>> call = api.getTvSeries(Config.API_KEY, pageNum);
        call.enqueue(new Callback<List<Video>>() {
            @Override
            public void onResponse(Call<List<Video>> call, retrofit2.Response<List<Video>> response) {
                if (response.code() == 200){
                    mIsLoading =false;
                    if(mSwipeRefreshLayout == null)return;
                    mProgressBar.setVisibility(View.GONE);
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (response.body().size() ==  0 && mPageCount ==1){
                        mActivity.setFailure(true);
                    }else {
                        mActivity.setFailure(false);
                    }
                    boolean newAdd =false;
                    for (int i = 0; i < response.body().size(); i++){
                        Video video = response.body().get(i);
                        CommonModels models =new CommonModels();
                        models.setImageUrl(video.getThumbnailUrl());
                        models.setTitle(video.getTitle());
                        models.setVideoType("tvseries");
                        models.setReleaseDate(video.getRelease());
                        models.setQuality(video.getVideoQuality());
                        models.setIsPaid(video.getIsPaid());
                        models.setStatus_movie(video.getStatusMovie());
                        models.setCount_status_movie(video.getCountStatusMovie());
                        models.setId(video.getVideosId());
                        mListCommonModels.add(models);
                        newAdd = true;
                    }
                    if(newAdd){
                        mAdapter.setNotifyDataSetChanged();
                    }

                }else {
                    mIsLoading =false;
                    mProgressBar.setVisibility(View.GONE);
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);
                    if (mPageCount ==1){
                        mActivity.setFailure(true);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Video>> call, Throwable t) {
                mIsLoading =false;
                mProgressBar.setVisibility(View.GONE);
                mShimmerLayout.stopShimmer();
                mShimmerLayout.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                if (mPageCount ==1){
                    mActivity.setFailure(true);
                }
            }
        });

    }

    boolean isSearchBarHide = false;
}
