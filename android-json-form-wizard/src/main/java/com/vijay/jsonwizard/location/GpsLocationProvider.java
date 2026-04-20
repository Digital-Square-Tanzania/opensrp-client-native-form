package com.vijay.jsonwizard.location;

import android.location.Location;

import androidx.annotation.StringRes;

public interface GpsLocationProvider {

    void start();

    void stop();

    boolean isUsingGooglePlayServices();

    interface Callback {
        void onLocationUpdate(Location location);

        void onLocationError(@StringRes int errorResId, boolean canFallbackToPlatform);
    }
}
