package animes.englishsubtitle.freemovieseries.nav_fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import animes.englishsubtitle.freemovieseries.adapters.CountryAdapter;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.models.CommonModels;
import animes.englishsubtitle.freemovieseries.models.home_content.AllCountry;
import animes.englishsubtitle.freemovieseries.network.RetrofitClient;
import animes.englishsubtitle.freemovieseries.network.apis.CountryApi;
import animes.englishsubtitle.freemovieseries.network.model.config.AdsConfig;
import animes.englishsubtitle.freemovieseries.utils.ApiResources;
import animes.englishsubtitle.freemovieseries.utils.Constants;
import animes.englishsubtitle.freemovieseries.utils.NetworkInst;
import animes.englishsubtitle.freemovieseries.utils.PreferenceUtils;
import animes.englishsubtitle.freemovieseries.utils.SpacingItemDecoration;
import animes.englishsubtitle.freemovieseries.utils.ToastMsg;
import animes.englishsubtitle.freemovieseries.utils.Tools;
import animes.englishsubtitle.freemovieseries.utils.ads.BannerAds;
import animes.englishsubtitle.freemovieseries.view.SwipeRefreshLayout;

public class CountryFragment extends Fragment {
    @BindView(R.id.adView) RelativeLayout mAdView;
    @BindView(R.id.shimmer_view_container) ShimmerFrameLayout mShimmerLayout;
    @BindView(R.id.swipe_layout) SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerView) RecyclerView mRecyclerView;
    Unbinder mUnbinder;
    private ApiResources mApiResources;
    private List<CommonModels> mListCommonModels = new ArrayList<>();
    private CountryAdapter mAdapter;
    private MainActivity mActivity;
    private static final int HIDE_THRESHOLD = 20;
    private int mScrolledDistance = 0;
    private boolean mControlsVisible = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_country, container, false);
        mActivity = (MainActivity) getActivity();
        mUnbinder = ButterKnife.bind(this,view);
        mActivity.setTitle(getResources().getString(R.string.country));
        mSwipeRefreshLayout.setToolbar(mActivity.getToolbar());
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        mRecyclerView.addItemDecoration(new SpacingItemDecoration(3, Tools.dpToPx(getActivity(), 10), true));
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setNestedScrollingEnabled(false);
        mAdapter = new CountryAdapter(mActivity, mListCommonModels, "");
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

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

        mShimmerLayout.startShimmer();

        if (new NetworkInst(getContext()).isNetworkAvailable()){
            getAllCountry();
        }else {
            mShimmerLayout.stopShimmer();
            mShimmerLayout.setVisibility(View.GONE);
            mActivity.setFailure(true,getString(R.string.no_internet));
        }

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mActivity.setFailure(false);
                mRecyclerView.removeAllViews();
                mListCommonModels.clear();
                mAdapter.notifyDataSetChanged();

                if (new NetworkInst(getContext()).isNetworkAvailable()){
                    getAllCountry();
                }else {
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);
                    mActivity.setFailure(true,getString(R.string.no_internet));
                }
            }
        });
        loadAd();
    }

    private void loadAd(){
        AdsConfig adsConfig = new DatabaseHelper(getContext()).getConfigurationData().getAdsConfig();
        if (PreferenceUtils.isLoggedIn(mActivity)){
            if (!PreferenceUtils.isActivePlan(mActivity)) {
                if (adsConfig.getAdsEnable().equals("1")) {
                    if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                        BannerAds.ShowAdmobBannerAds(getContext(), mAdView);

                    } else if (adsConfig.getMobileAdsNetwork().equals(Constants.APPODEAL)) {
                        //BannerAds.showStartAppBanner(getContext(), adView);
                        Appodeal.setBannerViewId(R.id.appodealBannerView_fragment_country);
                        Appodeal.show(getActivity(),Appodeal.BANNER_VIEW);
                    } else if(adsConfig.getMobileAdsNetwork().equals(Constants.NETWORK_AUDIENCE)) {
                        BannerAds.showFANBanner(getContext(), mAdView);
                    }
                }
            }
        }else {
            if (adsConfig.getAdsEnable().equals("1")) {

                if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                    BannerAds.ShowAdmobBannerAds(getContext(), mAdView);

                } else if (adsConfig.getMobileAdsNetwork().equals(Constants.APPODEAL)) {
                    //BannerAds.showStartAppBanner(getContext(), adView);
                    Appodeal.setBannerViewId(R.id.appodealBannerView_fragment_country);
                    Appodeal.show(getActivity(),Appodeal.BANNER_VIEW);
                } else if(adsConfig.getMobileAdsNetwork().equals(Constants.NETWORK_AUDIENCE)) {
                    BannerAds.showFANBanner(getContext(), mAdView);
                }
            }
        }

    }


    private void getAllCountry(){
        Retrofit retrofit = RetrofitClient.getRetrofitInstance();
        CountryApi api = retrofit.create(CountryApi.class);
        Call<List<AllCountry>> call = api.getAllCountry(Config.API_KEY);
        call.enqueue(new Callback<List<AllCountry>>() {
            @Override
            public void onResponse(Call<List<AllCountry>> call, retrofit2.Response<List<AllCountry>> response) {
                if (response.code() == 200){
                    if(mSwipeRefreshLayout == null)return;
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(View.GONE);
                    mSwipeRefreshLayout.setRefreshing(false);

                    if (response.body().size() == 0){
                        mActivity.setFailure(true);
                    }else {
                        mActivity.setFailure(false);
                    }

                    for (int i = 0; i < response.body().size(); i++) {
                        AllCountry country = response.body().get(i);
                        CommonModels models = new CommonModels();
                        models.setId(country.getCountryId());
                        models.setTitle(country.getName());
                        models.setImageUrl(country.getImageUrl());
                        mListCommonModels.add(models);
                    }
                    mAdapter.notifyDataSetChanged();
                }else {
                    mSwipeRefreshLayout.setRefreshing(false);
                    mShimmerLayout.stopShimmer();
                    mShimmerLayout.setVisibility(View.GONE);
                    new ToastMsg(getActivity()).toastIconError(getString(R.string.fetch_error));
                    mActivity.setFailure(true);
                }
            }

            @Override
            public void onFailure(Call<List<AllCountry>> call, Throwable t) {
                mSwipeRefreshLayout.setRefreshing(false);
                mShimmerLayout.stopShimmer();
                mShimmerLayout.setVisibility(View.GONE);
                new ToastMsg(getActivity()).toastIconError(getString(R.string.fetch_error));
                mActivity.setFailure(true);
            }
        });
    }
    public void onDestroy() {
        super.onDestroy();
        mUnbinder.unbind();
    }
}
