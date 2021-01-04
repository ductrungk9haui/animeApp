package tvseries.koreandramaengsub.freemovieapp.utils.ads;

import android.content.Context;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.startapp.android.publish.adsCommon.StartAppAd;

import tvseries.koreandramaengsub.freemovieapp.database.DatabaseHelper;
import tvseries.koreandramaengsub.freemovieapp.network.model.config.AdsConfig;

public class PopUpAds {

    public static void ShowAdmobInterstitialAds(Context context) {
        AdsConfig adsConfig = new DatabaseHelper(context).getConfigurationData().getAdsConfig();
        final InterstitialAd mInterstitialAd = new InterstitialAd(context);
        mInterstitialAd.setAdUnitId("ca-app-pub-3940256099942544/1033173712");
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
        StartAppAd startAppAd = new StartAppAd(context);
        startAppAd.showAd(); // show the ad
    }

}
