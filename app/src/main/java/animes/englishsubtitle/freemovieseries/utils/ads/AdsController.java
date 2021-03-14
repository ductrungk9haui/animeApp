package animes.englishsubtitle.freemovieseries.utils.ads;

import android.app.Activity;
import android.view.View;

import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.network.model.config.AdsConfig;
import animes.englishsubtitle.freemovieseries.utils.Constants;
import animes.englishsubtitle.freemovieseries.utils.PreferenceUtils;

public class AdsController implements AdsHelper {
    private AdsHelper mAdsHelper;
    private boolean mIsAdsEnable = false;
    private static AdsController instance;
    public static int count = 0;

    public AdsController(Activity activity) {
        init(activity);
    }

    public static AdsController getInstance(Activity activity) {
        if (instance == null) {
            instance = new AdsController(activity);
        }
        instance.setActivity(activity);
        return instance;
    }

    private void init(Activity activity) {
        DatabaseHelper dbHelper = new DatabaseHelper(activity);
        AdsConfig adsConfig = dbHelper.getConfigurationData().getAdsConfig();
        if (adsConfig.getAdsEnable().equals("1")) {
            if (PreferenceUtils.isLoggedIn(activity)) {
                if (!PreferenceUtils.isActivePlan(activity)) {
                    mIsAdsEnable = true;
                    if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.START_APP)) {
                        mAdsHelper = new AppodealHelper(activity);
                    } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.ADMOB)) {
                        mAdsHelper = new AdMobHelper(activity);
                    } else if (adsConfig.getMobileAdsNetwork().equalsIgnoreCase(Constants.NETWORK_AUDIENCE)) {
                        mAdsHelper = AudienceNetworkHelper.getInstance(activity);
                    }
                }
            } else {
                mIsAdsEnable = false;
            }
        }

    }

    public boolean isAdsEnable() {
        return mIsAdsEnable;
    }

    @Override
    public void initRewardVideoAds() {
        if (!isAdsEnable()) return;
        mAdsHelper.initRewardVideoAds();
    }

    public void initInterstitialAds() {
        if (!isAdsEnable()) return;
        mAdsHelper.initInterstitialAds();
    }

    @Override
    public void initNativeAds() {
        if (!isAdsEnable()) return;
        mAdsHelper.initNativeAds();
    }

    @Override
    public void showRewardVideoAds() {
        if (!isAdsEnable()) return;
        mAdsHelper.showRewardVideoAds();
    }

    @Override
    public void showInterstitialAds() {
        if (!isAdsEnable()) return;
        if (count == 3) {
            count = 0;
            showRewardVideoAds();
        } else {
            count++;
            mAdsHelper.showInterstitialAds();
        }
    }

    @Override
    public void showNativeAds(View adContainer, boolean isContentStream) {
        if (!isAdsEnable()) return;
        mAdsHelper.showNativeAds(adContainer, isContentStream);
    }

    @Override
    public void setActivity(Activity activity) {
        if (!isAdsEnable()) return;
        mAdsHelper.setActivity(activity);
    }

    @Override
    public void onDestroy() {
        if (!isAdsEnable()) return;
        mAdsHelper.onDestroy();
    }
}
