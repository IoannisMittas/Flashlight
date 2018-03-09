package com.mittas.flashlight.billing;

/**
 * Singleton
 */
public final class BillingManagerHolder {
    private static final BillingManagerHolder HOLDER = new BillingManagerHolder();
    private  BillingManager billingManager;

    private BillingManagerHolder(){}

    public static BillingManagerHolder getInstance() {
        return HOLDER;
    }

    public  void setBillingManager(BillingManager billingManager) {
        this.billingManager = billingManager;
    }
    public BillingManager getBillingManager() {
        return billingManager;
    }



}
