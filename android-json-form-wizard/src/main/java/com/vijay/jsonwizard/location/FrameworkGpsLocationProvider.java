package com.vijay.jsonwizard.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.os.SystemClock;
import android.text.TextUtils;

import com.vijay.jsonwizard.R;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FrameworkGpsLocationProvider implements GpsLocationProvider, LocationListener {

    private final Callback callback;
    private final LocationManager locationManager;
    private long sessionStartElapsedRealtimeNanos;
    private long sessionStartWallClockMillis;

    public FrameworkGpsLocationProvider(Context context, Callback callback) {
        this(context, callback, (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE));
    }

    FrameworkGpsLocationProvider(Context context, Callback callback, LocationManager locationManager) {
        this.callback = callback;
        this.locationManager = locationManager;
    }

    @Override
    @SuppressLint("MissingPermission")
    public void start() {
        if (locationManager == null) {
            Timber.w("Framework GPS provider: LocationManager is null");
            callback.onLocationError(R.string.could_not_get_your_location, false);
            return;
        }

        List<String> providers = getEnabledProviders();
        Timber.i("Framework GPS provider: enabled providers=%s", providers);
        if (providers.isEmpty()) {
            Timber.w("Framework GPS provider: no enabled providers available");
            callback.onLocationError(R.string.could_not_get_your_location, false);
            return;
        }

        markSessionStart();
        Timber.i("Framework GPS provider: session started, startWallClockMillis=%s, startElapsedRealtimeNanos=%s",
                sessionStartWallClockMillis, sessionStartElapsedRealtimeNanos);
        for (String provider : providers) {
            try {
                Timber.i("Framework GPS provider: requesting continuous updates from provider=%s", provider);
                locationManager.requestLocationUpdates(provider, 1000L, 0f, this, Looper.getMainLooper());
            } catch (IllegalArgumentException exception) {
                Timber.w(exception, "Framework GPS provider: invalid provider=%s while requesting updates", provider);
                // Ignore invalid providers reported by the system.
            } catch (SecurityException exception) {
                Timber.e(exception, "Framework GPS provider: security exception while requesting provider=%s", provider);
                callback.onLocationError(R.string.could_not_get_your_location, false);
                return;
            }
        }
    }

    @Override
    public void stop() {
        if (locationManager != null) {
            Timber.i("Framework GPS provider: removing location updates");
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public boolean isUsingGooglePlayServices() {
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        boolean accepted = isLocationFromCurrentSession(location);
        Timber.i("Framework GPS provider: onLocationChanged accepted=%s, location=%s", accepted, describeLocation(location));
        if (accepted) {
            callback.onLocationUpdate(location);
        }
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Timber.i("Framework GPS provider: onStatusChanged provider=%s status=%s extras=%s", provider, status, extras);
        // Deprecated callback retained for older Android framework location listener API.
    }

    @Override
    public void onProviderEnabled(String provider) {
        Timber.i("Framework GPS provider: provider enabled=%s", provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        Timber.w("Framework GPS provider: provider disabled=%s", provider);
        if (getEnabledProviders().isEmpty()) {
            Timber.w("Framework GPS provider: all providers disabled");
            callback.onLocationError(R.string.could_not_get_your_location, false);
        }
    }

    private void markSessionStart() {
        sessionStartElapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
        sessionStartWallClockMillis = System.currentTimeMillis();
    }

    private List<String> getEnabledProviders() {
        List<String> providers = new ArrayList<>();
        addProviderIfEnabled(providers, LocationManager.GPS_PROVIDER);
        addProviderIfEnabled(providers, LocationManager.NETWORK_PROVIDER);
        // Passive provider only replays locations produced for other apps, so it does not
        // satisfy the strict current-capture requirement on its own.
        return providers;
    }

    private void addProviderIfEnabled(List<String> providers, String provider) {
        if (!TextUtils.isEmpty(provider) && locationManager != null) {
            try {
                if (locationManager.isProviderEnabled(provider)) {
                    providers.add(provider);
                    Timber.i("Framework GPS provider: provider=%s is enabled", provider);
                } else {
                    Timber.i("Framework GPS provider: provider=%s is disabled", provider);
                }
            } catch (Exception exception) {
                Timber.w(exception, "Framework GPS provider: failed checking provider=%s", provider);
                // Ignore provider introspection failures and continue with the remaining providers.
            }
        }
    }

    boolean isLocationFromCurrentSession(Location location) {
        if (location == null) {
            return false;
        }

        long locationElapsedRealtimeNanos = location.getElapsedRealtimeNanos();
        if (locationElapsedRealtimeNanos > 0L) {
            boolean accepted = locationElapsedRealtimeNanos >= sessionStartElapsedRealtimeNanos;
            if (!accepted) {
                Timber.w("Framework GPS provider: rejecting stale location by elapsed realtime. locationElapsedRealtimeNanos=%s, sessionStartElapsedRealtimeNanos=%s, location=%s",
                        locationElapsedRealtimeNanos, sessionStartElapsedRealtimeNanos, describeLocation(location));
            }
            return accepted;
        }

        boolean accepted = location.getTime() >= sessionStartWallClockMillis;
        if (!accepted) {
            Timber.w("Framework GPS provider: rejecting stale location by wall clock. locationTime=%s, sessionStartWallClockMillis=%s, location=%s",
                    location.getTime(), sessionStartWallClockMillis, describeLocation(location));
        }
        return accepted;
    }

    private String describeLocation(Location location) {
        if (location == null) {
            return "null";
        }

        return "provider=" + location.getProvider()
                + ", lat=" + location.getLatitude()
                + ", lon=" + location.getLongitude()
                + ", accuracy=" + location.getAccuracy()
                + ", time=" + location.getTime()
                + ", elapsedRealtimeNanos=" + location.getElapsedRealtimeNanos();
    }
}
