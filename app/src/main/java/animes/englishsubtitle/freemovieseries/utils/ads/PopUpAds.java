package animes.englishsubtitle.freemovieseries.utils.ads;

import android.app.Activity;
import android.content.Context;

import com.appodeal.ads.Appodeal;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import animes.englishsubtitle.freemovieseries.database.DatabaseHelper;
import animes.englishsubtitle.freemovieseries.network.model.config.AdsConfig;

public class PopUpAds {
    public static int count = 0;

    public static void ShowAdmobInterstitialAds(Activity activity) {
        if(count == 3){
            count = 0;
            VideoRewardAds.showRewardedVideo(activity);
        }else{
            count++;
            AdsConfig adsConfig = new DatabaseHelper(activity.getApplicationContext()).getConfigurationData().getAdsConfig();
            final InterstitialAd mInterstitialAd = new InterstitialAd(activity.getApplicationContext());
            mInterstitialAd.setAdUnitId(adsConfig.getAdmobInterstitialAdsId());
            mInterstitialAd.loadAd(new AdRequest.Builder().build());

            mInterstitialAd.setAdListener(new AdListener(){
                @Override
                public void onAdLoaded() {
                    super.onAdLoaded();

                    mInterstitialAd.show();

                /*Random rand = new Random();
                int i = rand.nextInt(10)+1;

                Log.e("INTER AD:", String.valueOf(i));

                if (i%2==0){
                    mInterstitialAd.show();
                }*/
                }

                @Override
                public void onAdFailedToLoad(int i) {
                    super.onAdFailedToLoad(i);

                }
            });
        }

    }

    public static void showFANInterstitialAds(Context context){
        DatabaseHelper db = new DatabaseHelper(context);
        String placementId = db.getConfigurationData().getAdsConfig().getFanInterstitialAdsPlacementId();

        final com.facebook.ads.InterstitialAd interstitialAd = new com.facebook.ads.InterstitialAd(context, placementId);
//        interstitialAd.setAdListener(new InterstitialAdListener() {
//            @Override
//            public void onInterstitialDisplayed(Ad ad) {
//
//            }
//
//            @Override
//            public void onInterstitialDismissed(Ad ad) {
//
//            }
//
//            @Override
//            public void onError(Ad ad, AdError adError) {
//
//            }
//
//            @Override
//            public void onAdLoaded(Ad ad) {
//                interstitialAd.show();
//            }
//
//            @Override
//            public void onAdClicked(Ad ad) {
//
//            }
//
//            @Override
//            public void onLoggingImpression(Ad ad) {
//
//            }
//        });

        interstitialAd.loadAd();
    }

    public static void showStartappInterstitialAds(Context context){
        //startapp
       // StartAppAd startAppAd = new StartAppAd(context);
        //startAppAd.showAd(); // show the ad
    }

    public static void showAppodealInterstitialAds(Activity activity){
        //startapp
        Appodeal.show(activity,Appodeal.INTERSTITIAL); // show the ad
    }

}
