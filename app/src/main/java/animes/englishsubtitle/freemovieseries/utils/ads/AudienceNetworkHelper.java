package animes.englishsubtitle.freemovieseries.utils.ads;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdOptionsView;
import com.facebook.ads.AdSettings;
import com.facebook.ads.AudienceNetworkAds;
import com.facebook.ads.CacheFlag;
import com.facebook.ads.InterstitialAd;
import com.facebook.ads.InterstitialAdExtendedListener;
import com.facebook.ads.MediaView;
import com.facebook.ads.NativeAd;
import com.facebook.ads.NativeAdLayout;
import com.facebook.ads.NativeAdListener;
import com.facebook.ads.NativeAdScrollView;
import com.facebook.ads.NativeAdView;
import com.facebook.ads.NativeAdViewAttributes;
import com.facebook.ads.NativeAdsManager;
import com.facebook.ads.NativeBannerAd;
import com.facebook.ads.NativeBannerAdView;
import com.facebook.ads.RewardData;
import com.facebook.ads.RewardedVideoAd;
import com.facebook.ads.S2SRewardedVideoAdListener;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import animes.englishsubtitle.freemovieseries.DetailsActivity;
import animes.englishsubtitle.freemovieseries.ItemMovieActivity;
import animes.englishsubtitle.freemovieseries.R;
import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.network.model.config.AdsConfig;
import animes.englishsubtitle.freemovieseries.utils.PreferenceUtils;

import static android.content.Context.MODE_PRIVATE;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static animes.englishsubtitle.freemovieseries.BuildConfig.DEBUG;

public class AudienceNetworkHelper
        implements AudienceNetworkAds.InitListener, AdsHelper {
    private final String TAG = "FANHelper";
    private NativeAd nativeAd;
    private NativeAdLayout nativeAdLayout;
    private LinearLayout adView;
    private final int NATIVE_AD_VIEW_HEIGHT_DP = 300;
    private final int NATIVE_AD_VIEW_HEIGHT_DP_500 = 400;
    InterstitialAd interstitialAd;
    private RewardedVideoAd rewardedVideoAd;
    private boolean isInterstitialAdasShowReq = false;
    private boolean isRewardVideoAdasShowReq = false;
    private Activity mActivity;
    private boolean mIsDark;
    NativeAdViewAttributes mNativeAttributes;


    static AudienceNetworkHelper instance;

    public AudienceNetworkHelper() {
        instance = this;
    }

    public AudienceNetworkHelper(Activity activity) {
        mActivity = activity;
    }

    public static AudienceNetworkHelper getInstance(Activity activity) {
        if (instance == null) {
            instance = new AudienceNetworkHelper(activity);
        }
        instance.setActivity(activity);
        return instance;
    }



    /**
     * It's recommended to call this method from Application.onCreate().
     * Otherwise you can call it from all mActivity.onCreate()
     * methods for Activities that contain ads.
     *
     * @param context Application or mActivity.
     */
    public static void initialize(Context context) {
        if (!AudienceNetworkAds.isInitialized(context)) {
            if (DEBUG) {
                AdSettings.turnOnSDKDebugger(context);
            }

            AudienceNetworkAds
                    .buildInitSettings(context)
                    .withInitListener(new AudienceNetworkHelper())
                    .initialize();
        }


    }

    @Override
    public void onInitialized(AudienceNetworkAds.InitResult result) {
        Log.d(AudienceNetworkAds.TAG, result.getMessage());
    }


    public void onDestroy() {
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy();
            rewardedVideoAd = null;
        }
    }

    @Override
    public void initRewardVideoAds() {
        if (rewardedVideoAd != null) {
            rewardedVideoAd.destroy();
            rewardedVideoAd = null;
        }

        String NATIVE_REWARD_PLACEMENT_ID = new DatabaseHelper(mActivity).getConfigurationData().getAdsConfig().getFanRewardedAdsPlacementId();
        rewardedVideoAd =
                new RewardedVideoAd(mActivity, NATIVE_REWARD_PLACEMENT_ID);
        RewardedVideoAd.RewardedVideoLoadAdConfig loadAdConfig =
                rewardedVideoAd
                        .buildLoadAdConfig()
                        .withAdListener(new S2SRewardedVideoAdListener() {
                            @Override
                            public void onRewardServerFailed() {
                                Log.e(TAG, "RewardVideo onRewardServerFailed");
                            }

                            @Override
                            public void onRewardServerSuccess() {
                                Log.e(TAG, "RewardVideo onRewardServerSuccess");
                            }

                            @Override
                            public void onRewardedVideoCompleted() {
                                Log.e(TAG, "RewardVideo onRewardedVideoCompleted");
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {
                                Log.e(TAG, "RewardVideo onLoggingImpression");
                                if ((mActivity instanceof DetailsActivity)) {
                                    ((DetailsActivity) mActivity).onAdStart();
                                }
                            }

                            @Override
                            public void onRewardedVideoClosed() {
                                Log.e(TAG, "RewardVideo onRewardedVideoClosed");
                                if ((mActivity instanceof DetailsActivity)) {
                                    ((DetailsActivity) mActivity).onAdFinish();
                                }
                                initRewardVideoAds();
                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                                Log.e(TAG, "RewardVideo onError");
                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                Log.e(TAG, "RewardVideo onAdLoaded");
                                if (isRewardVideoAdasShowReq) {
                                    showRewardVideoAds();
                                }
                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                                Log.e(TAG, "RewardVideo onAdClicked");
                            }
                        })
                        .withFailOnCacheFailureEnabled(true)
                        .withRewardData(new RewardData(PreferenceUtils.getUserId(mActivity), "points", 10))
                        .build();
        rewardedVideoAd.loadAd(loadAdConfig);
    }

    @Override
    public void initInterstitialAds() {
        // Instantiate a Interstitials object.
        if (interstitialAd != null) {
            interstitialAd.destroy();
            interstitialAd = null;
        }
        String NATIVE_INTERSTITIAL_PLACEMENT_ID = new DatabaseHelper(mActivity).getConfigurationData().getAdsConfig().getFanInterstitialAdsPlacementId();


        interstitialAd = new InterstitialAd(mActivity, NATIVE_INTERSTITIAL_PLACEMENT_ID);
        // Load a new interstitial.
        InterstitialAd.InterstitialLoadAdConfig loadAdConfig =
                interstitialAd
                        .buildLoadAdConfig()
                        // Set a listener to get notified on changes
                        // or when the user interact with the ad.
                        .withAdListener(new InterstitialAdExtendedListener() {
                            @Override
                            public void onRewardedAdCompleted() {
                                Log.e(TAG, "InterstitialAd onRewardedAdCompleted");
                            }

                            @Override
                            public void onRewardedAdServerSucceeded() {
                                Log.e(TAG, "InterstitialAd onRewardedAdServerSucceeded");
                            }

                            @Override
                            public void onRewardedAdServerFailed() {
                                Log.e(TAG, "InterstitialAd onRewardedAdServerFailed");
                            }

                            @Override
                            public void onError(Ad ad, AdError adError) {
                                if (ad == interstitialAd) {
                                    Log.e(TAG, "Interstitial ad failed to load: " + adError.getErrorMessage());
                                }
                            }

                            @Override
                            public void onAdLoaded(Ad ad) {
                                if (ad == interstitialAd) {
                                    Log.e(TAG, "InterstitialAd onAdLoaded");
                                    // Ad was loaded, show it!
                                    if (isInterstitialAdasShowReq) {
                                        showInterstitialAds();
                                    }
                                }
                            }

                            @Override
                            public void onAdClicked(Ad ad) {
                                Log.e(TAG, "InterstitialAd onAdClicked");
                            }

                            @Override
                            public void onLoggingImpression(Ad ad) {
                                Log.e(TAG, "InterstitialAd onLoggingImpression");
                            }

                            @Override
                            public void onInterstitialDisplayed(Ad ad) {
                                Log.e(TAG, "InterstitialAdonInterstitialDisplayed");
                                if ((mActivity instanceof DetailsActivity)) {
                                    ((DetailsActivity) mActivity).onAdStart();
                                }
                            }

                            @Override
                            public void onInterstitialDismissed(Ad ad) {
                                Log.e(TAG, "InterstitialAd onInterstitialDismissed");
                                if ((mActivity instanceof DetailsActivity)) {
                                    ((DetailsActivity) mActivity).onAdFinish();
                                }
                                initInterstitialAds();
                            }

                            @Override
                            public void onInterstitialActivityDestroyed() {
                                Log.e(TAG, "InterstitialAd onInterstitialmActivityDestroyed");
                            }
                        })
                        .withCacheFlags(EnumSet.of(CacheFlag.VIDEO))
                        .withRewardData(new RewardData(PreferenceUtils.getUserId(mActivity), "points", 10))
                        .build();
        interstitialAd.loadAd(loadAdConfig);
    }

    @Override
    public void initNativeAds() {

    }

    @Override
    public void showRewardVideoAds() {
        if (rewardedVideoAd == null
                || !rewardedVideoAd.isAdLoaded()
                || rewardedVideoAd.isAdInvalidated()) {
            isRewardVideoAdasShowReq = true;
        } else {
            isRewardVideoAdasShowReq = false;
            rewardedVideoAd.show();
        }

    }

    @Override
    public void showInterstitialAds() {
        if (interstitialAd == null
                || !interstitialAd.isAdLoaded()
                || interstitialAd.isAdInvalidated()) {
            isInterstitialAdasShowReq = true;
        } else {
            isInterstitialAdasShowReq = false;
            interstitialAd.show();
        }
    }

    @Override
    public void showNativeAds(View adContainer, boolean isTemplateNativeAds) {
        // Instantiate a NativeAd object.
        if (!isTemplateNativeAds) {
            loadScrollNativeAd(adContainer,NATIVE_AD_VIEW_HEIGHT_DP);
        }else{
            loadScrollNativeAd(adContainer,NATIVE_AD_VIEW_HEIGHT_DP_500);
        }

    }

    @Override
    public void setActivity(Activity activity) {
        mActivity = activity;
        SharedPreferences sharedPreferences = activity.getSharedPreferences("push", MODE_PRIVATE);
        mIsDark = sharedPreferences.getBoolean("dark", false);
        mNativeAttributes =
                new NativeAdViewAttributes(activity)
                        .setBackgroundColor(mIsDark ? Color.BLACK : Color.WHITE)
                        .setTitleTextColor(mIsDark ? Color.WHITE : Color.BLACK)
                        .setDescriptionTextColor(mIsDark ? Color.WHITE : Color.BLACK)
                        .setButtonBorderColor(mIsDark ? Color.WHITE : Color.BLACK)
                        .setButtonTextColor(mIsDark ? Color.WHITE : Color.BLACK)
                        .setButtonColor(activity.getResources().getColor(R.color.com_facebook_button_background_color))
                        .setButtonBorderColor(activity.getResources().getColor(R.color.com_facebook_button_background_color));
    }

    public void showFANNativeBannerAd(RelativeLayout container) {
        if (!PreferenceUtils.isActivePlan(mActivity)) {
            String nativeAdId = new DatabaseHelper(mActivity).getConfigurationData().getAdsConfig().getFanNativeAdsPlacementId();
            NativeBannerAd nativeBannerAd =
                    new NativeBannerAd(mActivity, nativeAdId);
            NativeAdListener listener = new NativeAdListener() {
                @Override
                public void onMediaDownloaded(Ad ad) {

                }

                @Override
                public void onError(Ad ad, AdError adError) {
                    Log.e(TAG, "FAN Native ad failed to load: " + adError.getErrorMessage());
                }

                @Override
                public void onAdLoaded(Ad ad) {
                    View adView = NativeBannerAdView.render(mActivity, nativeBannerAd,
                            NativeBannerAdView.Type.HEIGHT_100);
                    container.addView(adView);
                }

                @Override
                public void onAdClicked(Ad ad) {

                }

                @Override
                public void onLoggingImpression(Ad ad) {

                }
            };

            //initiate a request to load an ad
            nativeBannerAd.loadAd(
                    nativeBannerAd.buildLoadAdConfig()
                            .withAdListener(listener)
                            .build());
        }
    }

    private void loadCustomNativeAds(View adContainer, boolean isNothing){
        String NATIVE_DETAIL_PLACEMENT_ID = new DatabaseHelper(mActivity).getConfigurationData().getAdsConfig().getFanNativeAdsPlacementId1();

        nativeAd = new NativeAd(mActivity, NATIVE_DETAIL_PLACEMENT_ID);

        NativeAdListener nativeAdListener = new NativeAdListener() {
            @Override
            public void onMediaDownloaded(Ad ad) {
                // Native ad finished downloading all assets
                Log.e(TAG, "Native ad finished downloading all assets.");
            }

            @Override
            public void onError(Ad ad, AdError adError) {
                // Native ad failed to load
                Log.e(TAG, "Native ad failed to load: " + adError.getErrorMessage());
            }

            @Override
            public void onAdLoaded(Ad ad) {
                // Native ad is loaded and ready to be displayed
                Log.d(TAG, "Native ad is loaded and ready to be displayed!");
                // Race condition, load() called again before last ad was displayed
                if (nativeAd == null || nativeAd != ad) {
                    return;
                }
                // Inflate Native Ad into Container
                inflateAd(mActivity, nativeAd, adContainer);
            }

            @Override
            public void onAdClicked(Ad ad) {
                // Native ad clicked
                Log.d(TAG, "Native ad clicked!");
            }

            @Override
            public void onLoggingImpression(Ad ad) {
                // Native ad impression
                Log.d(TAG, "Native ad impression logged!");
            }
        };

        // Request an ad
        nativeAd.loadAd(
                nativeAd.buildLoadAdConfig()
                        .withAdListener(nativeAdListener)
                        .build());
    }

    private void inflateAd(Context context, NativeAd nativeAd, View adContainer) {


        nativeAd.unregisterView();

        // Add the Ad view into the ad container.
        nativeAdLayout = adContainer.findViewById(R.id.native_ad_container);
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the Ad view.  The layout referenced should be the one you created in the last step.
        adView = (LinearLayout) inflater.inflate(R.layout.native_ad_layout, nativeAdLayout, false);
        nativeAdLayout.removeAllViews();
        nativeAdLayout.addView(adView);

        // Add the AdOptionsView
        LinearLayout adChoicesContainer = adContainer.findViewById(R.id.ad_choices_container);
        AdOptionsView adOptionsView = new AdOptionsView(context, nativeAd, nativeAdLayout);
        adChoicesContainer.removeAllViews();
        adChoicesContainer.addView(adOptionsView, 0);

        // Create native UI using the ad metadata.
        MediaView nativeAdIcon = adView.findViewById(R.id.native_ad_icon);
        TextView nativeAdTitle = adView.findViewById(R.id.native_ad_title);
        MediaView nativeAdMedia = adView.findViewById(R.id.native_ad_media);
        TextView nativeAdSocialContext = adView.findViewById(R.id.native_ad_social_context);
        TextView nativeAdBody = adView.findViewById(R.id.native_ad_body);
        TextView sponsoredLabel = adView.findViewById(R.id.native_ad_sponsored_label);
        Button nativeAdCallToAction = adView.findViewById(R.id.native_ad_call_to_action);
        //nativeAdMedia.setLayoutParams(new LinearLayout.LayoutParams(MATCH_PARENT,  (int) (Resources.getSystem().getDisplayMetrics().density * 150)));
        // Set the Text.
        nativeAdTitle.setText(nativeAd.getAdvertiserName());
        nativeAdTitle.setTextColor(mIsDark ? Color.WHITE : Color.BLACK);
        nativeAdBody.setText(nativeAd.getAdBodyText());
        nativeAdBody.setTextColor(mIsDark ? Color.WHITE : Color.BLACK);
        nativeAdSocialContext.setText(nativeAd.getAdSocialContext());
        nativeAdSocialContext.setTextColor(mIsDark ? Color.WHITE : Color.BLACK);
        nativeAdCallToAction.setVisibility(nativeAd.hasCallToAction() ? View.VISIBLE : View.INVISIBLE);
        nativeAdCallToAction.setText(nativeAd.getAdCallToAction());
        nativeAdCallToAction.setTextColor(mIsDark ? Color.WHITE : Color.BLACK);
        sponsoredLabel.setText(nativeAd.getSponsoredTranslation());
        sponsoredLabel.setTextColor(mIsDark ? Color.WHITE : Color.BLACK);
        // Create a list of clickable views
        List<View> clickableViews = new ArrayList<>();
        clickableViews.add(nativeAdTitle);
        clickableViews.add(nativeAdCallToAction);

        // Register the Title and CTA button to listen for clicks.
        nativeAd.registerViewForInteraction(
                adView, nativeAdMedia, nativeAdIcon, clickableViews);
    }


    public void loadScrollNativeAd(View AdsContainer, int AdsHeight) {
        // Instantiate a NativeAd object.
        NativeAdsManager manager;
        String NATIVE_HOME_PLACEMENT_ID = new DatabaseHelper(mActivity).getConfigurationData().getAdsConfig().getFanNativeAdsPlacementId();

        LinearLayout fanAdsScrollViewContainer = (LinearLayout) AdsContainer.findViewById(R.id.hscroll_container);
        manager = new NativeAdsManager(mActivity, NATIVE_HOME_PLACEMENT_ID, 5);
        manager.setListener(new NativeAdsManager.Listener() {
            @Override
            public void onAdsLoaded() {
                if (mActivity == null) {
                    return;
                }
                Log.d(TAG, "Scroll Native ad onAdsLoaded");
                fanAdsScrollViewContainer.removeAllViews();
                NativeAdScrollView fanAdsScrollView = new NativeAdScrollView(mActivity, manager, AdsHeight, mNativeAttributes);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 0);
                fanAdsScrollView.setLayoutParams(params);
                fanAdsScrollViewContainer.addView(fanAdsScrollView);
            }

            @Override
            public void onAdError(AdError adError) {
                Log.d(TAG, "Scroll Native ad onAdError");
            }
        });
        manager.loadAds(NativeAd.MediaCacheFlag.ALL);
    }
}