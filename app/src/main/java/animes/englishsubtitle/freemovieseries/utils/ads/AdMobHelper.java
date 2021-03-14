package animes.englishsubtitle.freemovieseries.utils.ads;

import android.app.Activity;
import android.util.Log;
import android.view.View;

import com.google.android.ads.nativetemplates.TemplateView;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.DetailsActivity;
import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.network.model.config.AdsConfig;

public class AdMobHelper implements AdsHelper {
    Activity mActivity;
    private InterstitialAd mInterstitialAd;
    private RewardedAd mRewardedAd;
    private final String TAG = "AdMobHelper";
    private boolean isInterstitialAdasShowReq = false;
    private boolean isRewardVideoAdasShowReq = false;
    List<UnifiedNativeAd> mUnifiedNativeAds = new ArrayList<>();

    public AdMobHelper(Activity activity) {
        mActivity = activity;
    }


    @Override
    public void initRewardVideoAds() {
        AdsConfig adsConfig = new DatabaseHelper(mActivity.getApplicationContext()).getConfigurationData().getAdsConfig();
        if (mRewardedAd == null || !mRewardedAd.isLoaded()) {
            mRewardedAd = new RewardedAd(mActivity, adsConfig.getAdmobRewardedVideoAdsId());
            mRewardedAd.loadAd(
                    new AdRequest.Builder().build(),
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onRewardedAdLoaded() {
                            // Ad successfully loaded.
                            Log.d(TAG, "onRewardedAdLoaded");
                            if (isRewardVideoAdasShowReq) {
                                showRewardVideoAds();
                            }
                        }

                        @Override
                        public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                            Log.d(TAG, "onRewardedAdFailedToLoad");
                        }
                    });
        }

    }

    @Override
    public void initInterstitialAds() {
        AdsConfig adsConfig = new DatabaseHelper(mActivity.getApplicationContext()).getConfigurationData().getAdsConfig();
        mInterstitialAd = new InterstitialAd(mActivity.getApplicationContext());
        mInterstitialAd.setAdUnitId(adsConfig.getAdmobInterstitialAdsId());
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                Log.d(TAG, "onAdLoaded");
                if (isInterstitialAdasShowReq) {
                    showInterstitialAds();
                }
            }

            @Override
            public void onAdFailedToLoad(LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                Log.d(TAG, "onAdFailedToLoad");
            }

            @Override
            public void onAdClicked() {
                super.onAdClicked();
                Log.d(TAG, "onAdClicked");
            }

            @Override
            public void onAdImpression() {
                super.onAdImpression();
                Log.d(TAG, "onAdImpression");
            }

            @Override
            public void onAdLeftApplication() {
                super.onAdLeftApplication();
                Log.d(TAG, "onAdLeftApplication");
            }

            @Override
            public void onAdOpened() {
                super.onAdOpened();
                Log.d(TAG, "onAdOpened");
                initInterstitialAds();
            }
        });
    }


    @Override
    public void initNativeAds() {

    }

    @Override
    public void showRewardVideoAds() {
        Log.d(TAG, "showRewardVideoAds");
        if (mRewardedAd == null || !mRewardedAd.isLoaded()) {
            isRewardVideoAdasShowReq = true;
            return;
        }
        RewardedAdCallback adCallback =
                new RewardedAdCallback() {
                    @Override
                    public void onRewardedAdOpened() {
                        Log.d(TAG, "onRewardedAdOpened");
                        if ((mActivity instanceof DetailsActivity)) {
                            ((DetailsActivity) mActivity).onAdStart();
                        }
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        // Ad closed.
                        // Preload the next video ad.
                        initRewardVideoAds();
                        Log.d(TAG, "onRewardedAdClosed");
                        if ((mActivity instanceof DetailsActivity)) {
                            ((DetailsActivity) mActivity).onAdFinish();
                        }
                    }

                    @Override
                    public void onUserEarnedReward(RewardItem rewardItem) {
                        // User earned reward.
                        Log.d(TAG, "onUserEarnedReward");
                    }

                    @Override
                    public void onRewardedAdFailedToShow(AdError adError) {
                        // Ad failed to display
                        Log.d(TAG, "onRewardedAdFailedToShow");
                    }
                };
        mRewardedAd.show(mActivity, adCallback);
    }

    @Override
    public void showInterstitialAds() {
        if (mInterstitialAd == null) {
            isInterstitialAdasShowReq = true;
            initInterstitialAds();
            return;
        }
        mInterstitialAd.show();
        isInterstitialAdasShowReq = false;

    }

    @Override
    public void showNativeAds(View adContainer, boolean isNothing) {
        TemplateView templateView = adContainer.findViewById(R.id.admob_nativead_template);
        templateView.setVisibility(View.VISIBLE);
        String nativeAdId = new DatabaseHelper(mActivity).getConfigurationData().getAdsConfig().getAdmobNativeAdsId();
        MobileAds.initialize(mActivity);
        AdLoader adLoader = new AdLoader.Builder(mActivity, nativeAdId)
                .forUnifiedNativeAd(new UnifiedNativeAd.OnUnifiedNativeAdLoadedListener() {
                    @Override
                    public void onUnifiedNativeAdLoaded(UnifiedNativeAd unifiedNativeAd) {
                        mUnifiedNativeAds.add(unifiedNativeAd);
                        if (unifiedNativeAd != null) {
                            Log.e(TAG, "onUnifiedNativeAdLoaded");
                            templateView.setNativeAd(unifiedNativeAd);
                        }
                    }
                }).build();
        adLoader.loadAd(new AdRequest.Builder().build());
    }

    @Override
    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onDestroy() {
        for (UnifiedNativeAd ad : mUnifiedNativeAds) {
            if (ad != null) {
                ad.destroy();
                Log.e(TAG, "Admob Native ad destroyed");
            }
        }

    }
}
