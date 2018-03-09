package com.mittas.flashlight.billing;

import com.android.billingclient.api.BillingClient.BillingResponse;
import com.android.billingclient.api.BillingClient.SkuType;

public class BillingUtility {

    public static boolean hasAds() {
        boolean hasAdvertisements;

        BillingManager billingManager = BillingManagerHolder.getInstance().getBillingManager();
        if (billingManager != null) {
            hasAdvertisements = !(billingManager.getUpdateListener().doRemoveAds());
        } else {
            hasAdvertisements = true;
        }

        return hasAdvertisements;
    }

    public static void payToRemoveAds() {
        BillingManager billingManager = BillingManagerHolder.getInstance().getBillingManager();
        if (billingManager != null) {
            final String paidSkuID = billingManager.getUpdateListener().REMOVE_ADS_SKU_ID;
            billingManager.initiatePurchaseFlow(paidSkuID, SkuType.INAPP);
        }
    }

    public static void queryPurchases() {
        BillingManager billingManager = BillingManagerHolder.getInstance().getBillingManager();
        if (billingManager != null) {
            if (billingManager.getBillingClientResponseCode() == BillingResponse.OK) {
                billingManager.queryPurchases();
            }
        }
    }
}
