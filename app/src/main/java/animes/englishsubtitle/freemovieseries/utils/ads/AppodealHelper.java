package animes.englishsubtitle.freemovieseries.utils.ads;

import android.app.Activity;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.View;

import com.appodeal.ads.Appodeal;
import com.appodeal.ads.InterstitialCallbacks;
import com.appodeal.ads.NativeAd;
import com.appodeal.ads.NativeCallbacks;
import com.appodeal.ads.RewardedVideoCallbacks;
import com.appodeal.ads.native_ad.views.NativeAdViewAppWall;
import com.appodeal.ads.native_ad.views.NativeAdViewContentStream;
import com.explorestack.consent.ConsentManager;

import java.util.ArrayList;
import java.util.List;

import animes.englishsubtitle.freemovieseries.DetailsActivity;
import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.utils.Constants;

import static android.content.Context.MODE_PRIVATE;

public class AppodealHelper implements AdsHelper {
    private final String TAG = "AppodealHelper";
    List<AdContainerObj> mAdsContainers = new ArrayList<>();
    private boolean isInterstitialAdasShowReq = false;
    private boolean isRewardVideoAdasShowReq = false;
    private Activity mActivity;

    public AppodealHelper(Activity activity) {
        mActivity = activity;
    }


    @Override
    public void setActivity(Activity activity) {
        mActivity = activity;
    }

    @Override
    public void onDestroy() {

    }


    @Override
    public void initInterstitialAds() {
        Log.d(TAG, " InterstitialAds initialize");
        String nativeAdId = new DatabaseHelper(mActivity).getConfigurationData().getAdsConfig().getStartappAppId();
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("push", MODE_PRIVATE);
        boolean consent = sharedPreferences.getBoolean(Constants.CONSENT, true);
        Log.d(TAG, "Consent : " + consent + " Manager: " + ConsentManager.getInstance(mActivity).getConsent());
        Appodeal.initialize(mActivity, nativeAdId, Appodeal.INTERSTITIAL, consent);
        Appodeal.setInterstitialCallbacks(new InterstitialCallbacks() {
            @Override
            public void onInterstitialLoaded(boolean b) {
                Log.d(TAG, "onInterstitialLoaded");
                if (isInterstitialAdasShowReq) {
                    showInterstitialAds();
                }
            }

            @Override
            public void onInterstitialFailedToLoad() {
                Log.d(TAG, "onInterstitialFailedToLoad");
            }

            @Override
            public void onInterstitialShown() {
                Log.d(TAG, "onInterstitialShown");
                if ((mActivity instanceof DetailsActivity)) {
                    ((DetailsActivity) mActivity).onAdStart();
                }
            }

            @Override
            public void onInterstitialShowFailed() {
                Log.d(TAG, "onInterstitialShowFailed");
            }

            @Override
            public void onInterstitialClicked() {
                Log.d(TAG, "onInterstitialClicked");
            }

            @Override
            public void onInterstitialClosed() {
                Log.d(TAG, "onInterstitialClosed");
                if ((mActivity instanceof DetailsActivity)) {
                    ((DetailsActivity) mActivity).onAdFinish();
                }
            }

            @Override
            public void onInterstitialExpired() {
                Log.d(TAG, "onInterstitialExpired");
            }

        });
    }


    @Override
    public void initRewardVideoAds() {
        Log.d(TAG, " RewardVideoAds initialize");
        String appodealAppId = new DatabaseHelper(mActivity).getConfigurationData().getAdsConfig().getStartappAppId();
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("push", MODE_PRIVATE);
        boolean consent = sharedPreferences.getBoolean(Constants.CONSENT, true);
        Log.d(TAG, "Consent : " + consent + " Manager: " + ConsentManager.getInstance(mActivity).getConsent());
        Appodeal.initialize(mActivity, appodealAppId, Appodeal.REWARDED_VIDEO, consent);
        Appodeal.setRewardedVideoCallbacks(new RewardedVideoCallbacks() {
            @Override
            public void onRewardedVideoLoaded(boolean b) {
                Log.d(TAG, "onRewardedVideoLoaded");
                if (isRewardVideoAdasShowReq) {
                    showRewardVideoAds();
                }
            }

            @Override
            public void onRewardedVideoFailedToLoad() {
                Log.d(TAG, "onRewardedVideoFailedToLoad");
            }

            @Override
            public void onRewardedVideoShown() {
                Log.d(TAG, "onRewardedVideoShown");
                if ((mActivity instanceof DetailsActivity)) {
                    ((DetailsActivity) mActivity).onAdStart();
                }
            }

            @Override
            public void onRewardedVideoShowFailed() {
                Log.d(TAG, "onRewardedVideoShowFailed");
            }

            @Override
            public void onRewardedVideoFinished(double v, String s) {
                Log.d(TAG, "onRewardedVideoFinished");
            }

            @Override
            public void onRewardedVideoClosed(boolean b) {
                Log.d(TAG, "onRewardedVideoClosed");
                if ((mActivity instanceof DetailsActivity)) {
                    ((DetailsActivity) mActivity).onAdFinish();
                }
            }

            @Override
            public void onRewardedVideoExpired() {
                Log.d(TAG, "onRewardedVideoExpired");
            }

            @Override
            public void onRewardedVideoClicked() {
                Log.d(TAG, "onRewardedVideoClicked");
            }
        });
    }

    @Override
    public void initNativeAds() {
        if (Appodeal.isInitialized(Appodeal.NATIVE)) return;
        Log.d(TAG, " initialize");
        mAdsContainers.clear();
        String nativeAdId = new DatabaseHelper(mActivity).getConfigurationData().getAdsConfig().getStartappAppId();
        SharedPreferences sharedPreferences = mActivity.getSharedPreferences("push", MODE_PRIVATE);
        boolean consent = sharedPreferences.getBoolean(Constants.CONSENT, true);
        Log.d(TAG, "Consent : " + consent + " Manager: " + ConsentManager.getInstance(mActivity).getConsent());
        Appodeal.initialize(mActivity, nativeAdId, Appodeal.NATIVE, consent);
        Appodeal.setNativeCallbacks(new NativeCallbacks() {
            @Override
            public void onNativeLoaded() {
                Log.d(TAG, "onNativeLoaded");
                if (mAdsContainers.size() > 0) {
                    for (AdContainerObj adContainer : mAdsContainers) {
                        showNativeAds(adContainer.adContainer, adContainer.isContentStream);
                        mAdsContainers.remove(adContainer);
                    }
                }
            }

            @Override
            public void onNativeFailedToLoad() {
                Log.d(TAG, "onNativeFailedToLoad");
            }

            @Override
            public void onNativeShown(NativeAd nativeAd) {
                Log.d(TAG, "onNativeShown");
            }

            @Override
            public void onNativeShowFailed(NativeAd nativeAd) {
                Log.d(TAG, "onNativeShowFailed");
            }

            @Override
            public void onNativeClicked(NativeAd nativeAd) {
                Log.d(TAG, "onNativeClicked");

            }

            @Override
            public void onNativeExpired() {
                Log.d(TAG, "onNativeExpired");
            }
        });
    }

    @Override
    public void showInterstitialAds() {
        if (Appodeal.isInitialized(Appodeal.INTERSTITIAL)) {
            Appodeal.show(mActivity, Appodeal.INTERSTITIAL);
            isInterstitialAdasShowReq = false;
            return;
        }
        isInterstitialAdasShowReq = true;
    }

    @Override
    public void showNativeAds(View adContainer, boolean isContentStream) {
        Log.d(TAG, "show");
        if (Appodeal.isInitialized(Appodeal.NATIVE) && Appodeal.isLoaded(Appodeal.NATIVE)) {
            List<NativeAd> loadedNativeAds = Appodeal.getNativeAds(1);
            if (!loadedNativeAds.isEmpty()) {
                Log.d(TAG, "isShow");
                if (isContentStream) {
                    NativeAdViewContentStream adView = adContainer.findViewById(R.id.appodel_native_ad_content_stream);
                    adView.setNativeAd(loadedNativeAds.get(0));
                    adView.setVisibility(View.VISIBLE);
                } else {
                    NativeAdViewAppWall adView = adContainer.findViewById(R.id.appodel_native_ad_app_wall);
                    adView.setNativeAd(loadedNativeAds.get(0));
                    adView.setVisibility(View.VISIBLE);
                }
            }
        } else {
            AdContainerObj obj = new AdContainerObj(adContainer, isContentStream);
            mAdsContainers.add(obj);
        }
    }


    @Override
    public void showRewardVideoAds() {
        Log.d(TAG, "showRewardVideoAds Show");
        if (Appodeal.isInitialized(Appodeal.REWARDED_VIDEO)) {
            Log.d(TAG, "showRewardVideoAds isShow");
            Appodeal.show(mActivity, Appodeal.REWARDED_VIDEO);
            isRewardVideoAdasShowReq = false;
            return;
        }
        isRewardVideoAdasShowReq = true;
    }


public class AdContainerObj {
    View adContainer;
    boolean isContentStream;

    public AdContainerObj(View adContainer, boolean isContentStream) {
        this.adContainer = adContainer;
        this.isContentStream = isContentStream;
    }
}
}
