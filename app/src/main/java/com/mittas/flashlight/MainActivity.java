package com.mittas.flashlight;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

import com.mittas.flashlight.billing.BillingManager;
import com.mittas.flashlight.billing.BillingManagerHolder;
import com.mittas.flashlight.billing.UpdateListener;

public class MainActivity extends AppCompatActivity {
    private BillingManager billingManager;
    private UpdateListener updateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        updateListener = new UpdateListener(this);
        billingManager = new BillingManager(this, updateListener);
    }

    public void onBillingManagerSetupFinished() {
        BillingManagerHolder.getInstance().setBillingManager(this.billingManager);
    }

    @Override
    protected void onDestroy() {
        if (billingManager != null) {
            billingManager.destroy();
        }
        super.onDestroy();
    }
}
