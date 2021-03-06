package animes.englishsubtitle.freemovieseries.utils.ads;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.network.model.config.AdsConfig;

public class VideoRewardAds {
    static RewardedAd mRewardedAd;
    static AdsConfig adsConfig;
    public static void prepareAd(Context context){
        if (mRewardedAd == null || !mRewardedAd.isLoaded()) {
            adsConfig = new DatabaseHelper(context).getConfigurationData().getAdsConfig();
            mRewardedAd = new RewardedAd(context, adsConfig.getAdmobRewardedVideoAdsId());
            mRewardedAd.loadAd(
                    new AdRequest.Builder().build(),
                    new RewardedAdLoadCallback() {
                        @Override
                        public void onRewardedAdLoaded() {
                            // Ad successfully loaded.
                        }
                        @Override
                        public void onRewardedAdFailedToLoad(LoadAdError loadAdError) {
                            // Ad failed to load.
                        }
                    });
        }
    }

    public static void showRewardedVideo(Activity activity) {
        if(mRewardedAd == null){
            prepareAd(activity);
            return;
        }
        if(!mRewardedAd.isLoaded())return;
        RewardedAdCallback adCallback =
                new RewardedAdCallback() {
                    @Override
                    public void onRewardedAdOpened() {
                        // Ad opened.
                        prepareAd(activity);
                    }

                    @Override
                    public void onRewardedAdClosed() {
                        // Ad closed.
                        // Preload the next video ad.
                    }

                    @Override
                    public void onUserEarnedReward(RewardItem rewardItem) {
                        // User earned reward.
                    }

                    @Override
                    public void onRewardedAdFailedToShow(AdError adError) {
                        // Ad failed to display
                    }
                };
        mRewardedAd.show(activity, adCallback);
    }

}
