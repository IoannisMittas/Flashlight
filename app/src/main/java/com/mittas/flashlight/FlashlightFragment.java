package com.mittas.flashlight;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ToggleButton;

import com.mittas.flashlight.billing.BillingUtility;
import com.mittas.flashlight.service.BlinkService;
import com.shawnlin.numberpicker.NumberPicker;

public class FlashlightFragment extends Fragment {
    private static final String LOG_TAG = FlashlightFragment.class.getSimpleName();
    private static final String IS_ON_KEY = "isOn";
    private static final String BLINK_FREQ_KEY = "blinkFrequency";
    private BlinkService blinkService;
    private AdsManager adsManager;
    private int blinkingFrequency;
    private boolean isBlinkServiceBound = false;
    private boolean isOn;
    private boolean hasDeprecatedCameraObject = false;
    private boolean launchedSettings = false;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            if (savedInstanceState.containsKey(IS_ON_KEY)) {
                this.isOn = savedInstanceState.getBoolean("isOn");
            }

            if (savedInstanceState.containsKey(BLINK_FREQ_KEY)) {
                this.blinkingFrequency = savedInstanceState.getInt("blinkingFrequency");
            }
        } else {
            // Default values
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
            String startupLightKey = getContext().getString(R.string.pref_startup_light_key);
            boolean hasStartupLight = sharedPref.getBoolean(startupLightKey,
                    Boolean.parseBoolean(getContext().getString(R.string.pref_startup_light_default)));
            this.isOn = hasStartupLight;

            String blinkFreqDefault = getContext().getString(R.string.blink_frequency_default);
            this.blinkingFrequency = Integer.parseInt(blinkFreqDefault);

        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            hasDeprecatedCameraObject = true;
        }

        BillingUtility.queryPurchases();

        if (BillingUtility.hasAds()) {
            adsManager = new AdsManager(getActivity());
            adsManager.createAd();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        NumberPicker numberPicker = rootView.findViewById(R.id.numberpicker);
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(10);
        numberPicker.setValue(this.blinkingFrequency);
        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                blinkingFrequency = newVal;
                chooseOnOffOrBlink();
            }
        });

        ToggleButton toggle = rootView.findViewById(R.id.toggleButton);
        toggle.setChecked(this.isOn);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isOn = isChecked;
                chooseOnOffOrBlink();

                // Notify that flashlight turned off
                if(!isOn && BillingUtility.hasAds() && (adsManager != null)) {
                    adsManager.onFlashlightTurnedOff();
                }
            }
        });

        ImageButton settingsButton = rootView.findViewById(R.id.settingsButton);
        settingsButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                launchedSettings = true;
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();

        if (hasDeprecatedCameraObject) {
            CameraUtility.initCamera();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        BillingUtility.queryPurchases();

        if (BillingUtility.hasAds()) {
            if (launchedSettings && (adsManager != null)) { // Show ad when back from settings
                adsManager.showAd();
                launchedSettings = false;
            }
        }

        chooseOnOffOrBlink();
    }


    /**
     * The logic is to bind when it's time to start blinking, and unbind when it's time to
     * stop blinking. Bind == blinking, unbind == stop blinking.
     */
    private void chooseOnOffOrBlink() {
        if (isOn && (blinkingFrequency != 0)) { // Start blinking or change blink frequency
            if (!isBlinkServiceBound) {
                // Start blinking
                doBindBlinkService();
            } else {
                blinkService.onFrequencyChanged(blinkingFrequency);
            }
        } else { // Stop blinking or turn torch on/off
            if (isBlinkServiceBound) {
                // Stop blinking
                doUnbindBlinkService();
                getActivity().stopService(new Intent(getContext(), BlinkService.class));
            }

            // Possible synchronization problem, but working okay
            CameraUtility.turnOnOff(getActivity(), isOn);
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(IS_ON_KEY, this.isOn);
        savedInstanceState.putInt(BLINK_FREQ_KEY, this.blinkingFrequency);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onStop() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getContext());
        String backgroundModeKey = getContext().getString(R.string.pref_background_mode_key);
        boolean runInBackground = sharedPref.getBoolean(backgroundModeKey,
                Boolean.parseBoolean(getContext().getString(R.string.pref_background_mode_default)));

        if (!runInBackground) {
            if (isBlinkServiceBound) {
                // Stop blinking
                doUnbindBlinkService();
                getActivity().stopService(new Intent(getContext(), BlinkService.class));
            }

            // Possible synchronization problem, but working okay
            CameraUtility.turnOnOff(getActivity(), false);

            if (hasDeprecatedCameraObject) {
                CameraUtility.releaseCamera();
            }
        } else {
            // if the app is set to run in the background, but the flashlight is turned off
            // and doesn't blinking, we need to release the camera object
            if (hasDeprecatedCameraObject && !isOn && !isBlinkServiceBound) {
                CameraUtility.releaseCamera();
            }
        }

        super.onStop();
    }

    void doBindBlinkService() {
        Intent intent = new Intent(getContext(), BlinkService.class);
        getActivity().bindService(intent, blinkServiceConnection, Context.BIND_AUTO_CREATE);
    }

    void doUnbindBlinkService() {
        if (isBlinkServiceBound) {
            getActivity().unbindService(blinkServiceConnection);
            isBlinkServiceBound = false;
        }
    }

    void doStartBlinkService() {
        Intent intent = new Intent(getContext(), BlinkService.class);
        intent.putExtra(blinkService.BLINKING_FREQUENCY, blinkingFrequency);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getActivity().startForegroundService(intent);
        } else {
            getActivity().startService(intent);
        }
    }

    private ServiceConnection blinkServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to BlinkService, cast the IBinder and get BlinkService instance
            BlinkService.LocalBinder binder = (BlinkService.LocalBinder) service;
            blinkService = binder.getService();
            isBlinkServiceBound = true;
            doStartBlinkService();
        }

        //The Android system calls this when the blinkServiceConnection to the service is unexpectedly lost,
        // such as when the service has crashed or has been killed.
        // This is not called when the client unbinds.
        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            isBlinkServiceBound = false;
        }
    };

}
