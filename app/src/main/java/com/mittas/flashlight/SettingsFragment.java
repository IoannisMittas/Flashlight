package com.mittas.flashlight;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;

import com.mittas.flashlight.billing.BillingUtility;

public class SettingsFragment extends PreferenceFragment {
    private boolean doRemoveAdsButton = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createUI();

        BillingUtility.queryPurchases();

        if (!BillingUtility.hasAds()) {
            removeAdsButton();
            doRemoveAdsButton = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        BillingUtility.queryPurchases();

        if(doRemoveAdsButton && !BillingUtility.hasAds()) {
            removeAdsButton();
            doRemoveAdsButton = false;
        }
    }

    private void createUI() {
        addPreferencesFromResource(R.xml.preferences);

        // "Remove ads" button
        Preference removeAdsButton = findPreference(getString(R.string.pref_remove_ads_key));
        removeAdsButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                BillingUtility.payToRemoveAds();
                doRemoveAdsButton = true;
                return true;
            }
        });

        // Rate button
        Preference rateButton = findPreference(getString(R.string.pref_rate_key));
        rateButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                rateApp();
                return true;
            }
        });

        // About button
        Preference aboutButton = findPreference(getString(R.string.pref_about_key));
        aboutButton.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(getActivity(), AboutActivity.class));
                return true;
            }
        });
    }

    private void removeAdsButton() {
            Preference button = findPreference(getString(R.string.pref_remove_ads_key));
            PreferenceScreen preferenceScreen = getPreferenceScreen();
            preferenceScreen.removePreference(button);
    }

    /**
     * Start with rating the app
     * Determine if the Play Store is installed on the device
     */
    public void rateApp() {
        try {
            Intent rateIntent = rateIntentForUrl("market://details");
            startActivity(rateIntent);
        } catch (ActivityNotFoundException e) {
            Intent rateIntent = rateIntentForUrl("https://play.google.com/store/apps/details");
            startActivity(rateIntent);
        }
    }

    private Intent rateIntentForUrl(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(String.format("%s?id=%s", url, BuildConfig.APPLICATION_ID)));
        int flags = Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_MULTIPLE_TASK;
        if (Build.VERSION.SDK_INT >= 21) {
            flags |= Intent.FLAG_ACTIVITY_NEW_DOCUMENT;
        } else {
            //noinspection deprecation
            flags |= Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET;
        }
        intent.addFlags(flags);
        return intent;
    }


}
