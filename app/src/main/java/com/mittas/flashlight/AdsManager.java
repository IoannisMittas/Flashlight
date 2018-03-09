package com.mittas.flashlight;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

public class AdsManager {
    private static final String LOG_TAG = AdsManager.class.getSimpleName();
    private static final int SHOW_AD_UPPER_LIMIT = 2; // on the third time, show ad
    private Context context;
    private InterstitialAd interstitialAd;
    private int lightTurnedOffCounter;

    public AdsManager(Context context) {
        this.context = context;
        // Initialize counter
        lightTurnedOffCounter = 0;
    }

    public void createAd() {
        String appID = BuildConfig.ADMOB_APP_ID;
        String adUnitID = BuildConfig.ADMOB_AD_UNIT_ID;

        MobileAds.initialize(context, appID);

        interstitialAd = new InterstitialAd(context);
        interstitialAd.setAdUnitId(adUnitID);

        interstitialAd.loadAd(new AdRequest.Builder().build());
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                // Load the next interstitial.
                interstitialAd.loadAd(new AdRequest.Builder().build());
            }
        });
    }

    /**
     * When this method is called, it means that the flashlight turned off.
     * We count the times it turned off, and after a certain number we show an ad. That's because
     * we don't want the ads to be intrusive and to show an ad every time the user turn the light off.
     */
    public void onFlashlightTurnedOff() {
        lightTurnedOffCounter++;

        if (lightTurnedOffCounter > SHOW_AD_UPPER_LIMIT) {
            lightTurnedOffCounter = 0;
            showAd();
        }
    }

    public void showAd() {
        if (interstitialAd.isLoaded()) {
            interstitialAd.show();
        } else {
            Log.d(LOG_TAG, "The interstitial wasn't loaded yet.");
        }
    }
}
