package animes.englishsubtitle.freemovieseries.utils.ads;

import android.app.Activity;
import android.view.View;

public interface AdsHelper {
    void initRewardVideoAds();
    void initInterstitialAds();
    void initNativeAds();

    void showRewardVideoAds();
    void showInterstitialAds();
    void showNativeAds(View adView, boolean isSpecialAd);

    void setActivity(Activity activity);

    void onDestroy();
}
