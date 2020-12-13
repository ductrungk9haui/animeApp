package tvseries.koreandramaengsub.freemovieapp.nav_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.facebook.shimmer.ShimmerFrameLayout;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import tvseries.koreandramaengsub.freemovieapp.Config;
import tvseries.koreandramaengsub.freemovieapp.MainActivity;
import tvseries.koreandramaengsub.freemovieapp.R;
import tvseries.koreandramaengsub.freemovieapp.adapters.CommonGridAdapter;
import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.models.CommonModels;
import tvseries.koreandramaengsub.freemovieapp.models.Movie;
import tvseries.koreandramaengsub.freemovieapp.network.RetrofitClient;
import tvseries.koreandramaengsub.freemovieapp.network.apis.FavouriteApi;
import tvseries.koreandramaengsub.freemovieapp.network.model.config.AdsConfig;
import tvseries.koreandramaengsub.freemovieapp.utils.ApiResources;
import tvseries.koreandramaengsub.freemovieapp.utils.Constants;
import tvseries.koreandramaengsub.freemovieapp.utils.NetworkInst;
import tvseries.koreandramaengsub.freemovieapp.utils.PreferenceUtils;
import tvseries.koreandramaengsub.freemovieapp.utils.SpacingItemDecoration;
import tvseries.koreandramaengsub.freemovieapp.utils.ToastMsg;
import tvseries.koreandramaengsub.freemovieapp.utils.Tools;
import tvseries.koreandramaengsub.freemovieapp.utils.ads.BannerAds;
import tvseries.koreandramaengsub.freemovieapp.view.SwipeRefreshLayout;

public class FavoriteFragment extends Fragment {
    @BindView(R.id.adView) RelativeLayout mAdView;
    @BindView(R.id.item_progress_bar) ProgressBar mProgressBar;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout mShimmerLayout;
    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.coordinator_lyt) CoordinatorLayout mCoordinatorLayout ;
    @BindView(R.id.tv_noitem) TextView mTvNoItem;
    @BindView(R.id.recyclerView) RecyclerView RecyclerView;
    Unbinder mUnbinder;
    private CommonGridAdapter mAdapter;
    private List<CommonModels> mListCommonModels =new ArrayList<>();
    private ApiResources mApiResources;
    private boolean mIsLoading =false;
    private int mPageCount =1,checkPass=0;
    private MainActivity mActivity;
    private static final int HIDE_THRESHOLD = 20;
    private int mScrolledDistance = 0;
    private boolean mControlsVisible = true;
    private String userId = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_movies, container, false);
        mActivity = (MainActivity) getActivity();
        mUnbinder = ButterKnife.bind(this,view);
        mActivity.setTitle(getResources().getString(R.string.favorite));
        mSwipeRefreshLayout.setToolbar(mActivity.getToolbar());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initComponent(view);
        loadAd();

    }

    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
    private void initComponent(View view) {
        mApiResources =new ApiResources();
        mShimmerLayout.startShimmer();
        userId = PreferenceUtils.getUserId(getContext());

        //----favorite's recycler view-----------------
        RecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        RecyclerView.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(getActivity(), 0), true));
        RecyclerView.setHasFixedSize(true);
        RecyclerView.setNestedScrollingEnabled(false);
        mAdapter = new CommonGridAdapter(getContext(), mListCommonModels);
        RecyclerView.setAdapter(mAdapter);
        RecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (!recyclerView.canScrollVertically(1) && !mIsLoading) {
                    mPageCount = mPageCount +1;
                    mIsLoading = true;
                    mProgressBar.setVisibility(View.VISIBLE);
                    getData(userId, mPageCount);
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

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                RecyclerView.removeAllViews();
                mPageCount =1;
                mListCommonModels.clear();
                mAdapter.notifyDataSetChanged();

                if (new NetworkInst(getContext()).isNetworkAvailable()){
                    getData(userId, mPageCount);
                }else {
                    mTvNoItem.setText(getString(R.string.no_internet));
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mCoordinatorLayout.setVisibility(View.VISIBLE);
                }

            }
        });


        if (new NetworkInst(getContext()).isNetworkAvailable()){
            if (userId == null){
                mTvNoItem.setText(getString(R.string.please_login_first_to_see_favorite_list));
                mShimmerLayout.stopShimmer();
                mShimmerLayout.setVisibility(View.GONE);
                mCoordinatorLayout.setVisibility(View.VISIBLE);
            }else {
                getData(userId, mPageCount);
            }
        }else {
            mTvNoItem.setText(getString(R.string.no_internet));
            mShimmerLayout.stopShimmer();
            mShimmerLayout.setVisibility(View.GONE);
            mCoordinatorLayout.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void loadAd() {
        AdsConfig adsConfig = new DatabaseHelper(getContext()).getConfigurationData().getAdsConfig();
        if (adsConfig.getAdsEnable().equals("1")) {

            if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                BannerAds.ShowAdmobBannerAds(mActivity, mAdView);

            } else if (adsConfig.getMobileAdsNetwork().equals(Constants.START_APP)) {
                BannerAds.showStartAppBanner(mActivity, mAdView);

            } else if(adsConfig.getMobileAdsNetwork().equals(Constants.NETWORK_AUDIENCE)) {
                BannerAds.showFANBanner(getContext(), mAdView);
            }
        }
    }

    private void getData(String userID, int pageNum){
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        FavouriteApi api = retrofit.create(FavouriteApi.class);
        Call<List<Movie>> call = api.getFavoriteList(Config.API_KEY, userID, pageNum);
        call.enqueue(new Callback<List<Movie>>() {
            @Override
            public void onResponse(Call<List<Movie>> call, Response<List<Movie>> response) {
                if (response.code() == 200){
                    mIsLoading =false;
                    mSwipeRefreshLayout.setRefreshing(false);
                    mProgressBar.setVisibility(View.GONE);
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(View.GONE);

                    if (response.body().size() == 0 && mPageCount ==1){
                        mCoordinatorLayout.setVisibility(View.VISIBLE);
                        mTvNoItem.setText("No items here");
                        mPageCount = 1;
                    }else {
                        mCoordinatorLayout.setVisibility(View.GONE);
                    }

                    for (int i = 0; i < response.body().size(); i++){
                        CommonModels models =new CommonModels();
                        models.setImageUrl(response.body().get(i).getThumbnailUrl());
                        models.setTitle(response.body().get(i).getTitle());
                        models.setQuality(response.body().get(i).getVideoQuality());

                        if (response.body().get(i).getIsTvseries().equals("0")){
                            models.setVideoType("movie");
                        }else {
                            models.setVideoType("tvseries");
                        }
                        models.setId(response.body().get(i).getVideosId());
                        mListCommonModels.add(models);
                    }

                    mAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Movie>> call, Throwable t) {
                mIsLoading =false;
                mProgressBar.setVisibility(View.GONE);
                mSwipeRefreshLayout.setRefreshing(false);
                mShimmerLayout.stopShimmer();
                mShimmerLayout.setVisibility(View.GONE);
                if (userId == null){
                    new ToastMsg(getActivity()).toastIconError(getString(R.string.please_login_first_to_see_favorite_list));
                }else {
                    new ToastMsg(getActivity()).toastIconError(getString(R.string.fetch_error));
                }

                if (mPageCount ==1){
                    mCoordinatorLayout.setVisibility(View.VISIBLE);
                }
            }
        });

    }

}