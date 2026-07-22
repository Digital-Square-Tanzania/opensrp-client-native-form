package com.vijay.jsonwizard.location;

import android.content.Context;

import com.vijay.jsonwizard.utils.GooglePlayServicesHelper;

import timber.log.Timber;

public class GpsLocationProviderFactory {

    public GpsLocationProvider create(Context context, GpsLocationProvider.Callback callback, boolean preferGooglePlayServices) {
        boolean googlePlayServicesAvailable = isGooglePlayServicesAvailable(context);
        Timber.i("GPS provider factory: preferGooglePlayServices=%s, googlePlayServicesAvailable=%s",
                preferGooglePlayServices, googlePlayServicesAvailable);
        if (preferGooglePlayServices && googlePlayServicesAvailable) {
            Timber.i("GPS provider factory: selecting GMS provider");
            return createGmsLocationProvider(context, callback);
        }

        Timber.i("GPS provider factory: selecting framework provider");
        return createFrameworkLocationProvider(context, callback);
    }

    protected boolean isGooglePlayServicesAvailable(Context context) {
        return GooglePlayServicesHelper.isAvailable(context);
    }

    protected GpsLocationProvider createGmsLocationProvider(Context context, GpsLocationProvider.Callback callback) {
        return new GmsGpsLocationProvider(context, callback);
    }

    protected GpsLocationProvider createFrameworkLocationProvider(Context context, GpsLocationProvider.Callback callback) {
        return new FrameworkGpsLocationProvider(context, callback);
    }
}
