package com.mittas.flashlight.billing;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.Purchase;
import com.mittas.flashlight.MainActivity;
import com.mittas.flashlight.billing.BillingManager.BillingUpdatesListener;

import java.util.List;

/**
 * Handler to billing updates
 */
public class UpdateListener implements BillingUpdatesListener {
    public static final String REMOVE_ADS_SKU_ID = "block_ads";
    private static final String REMOVE_ADS_LABEL = "paidToRemoveAds";
    private MainActivity mActivity;

    public UpdateListener(MainActivity activity) {
        this.mActivity = activity;
    }

    @Override
    public void onBillingClientSetupFinished() {
        mActivity.onBillingManagerSetupFinished();
    }

    @Override
    public void onConsumeFinished(String token, @BillingResponse int result) {

    }

    @Override
    public void onPurchasesUpdated(List<Purchase> purchaseList) {
        for (Purchase purchase : purchaseList) {
            switch (purchase.getSku()) {
                case REMOVE_ADS_SKU_ID:
                    setRemoveAds();
                    break;
            }
        }
    }

    public void setRemoveAds() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        prefs.edit().putBoolean(REMOVE_ADS_LABEL, true).apply();
    }

    public boolean doRemoveAds() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mActivity);
        return prefs.getBoolean(REMOVE_ADS_LABEL, false);
    }

}