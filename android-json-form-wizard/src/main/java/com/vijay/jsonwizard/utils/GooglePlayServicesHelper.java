package com.vijay.jsonwizard.utils;

import android.content.Context;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import timber.log.Timber;

public final class GooglePlayServicesHelper {

    private GooglePlayServicesHelper() {
        // Utility class
    }

    public static boolean isAvailable(Context context) {
        try {
            return GoogleApiAvailability.getInstance()
                    .isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
        } catch (RuntimeException exception) {
            Timber.w(exception, "Unable to determine Google Play Services availability");
            return false;
        }
    }
}
