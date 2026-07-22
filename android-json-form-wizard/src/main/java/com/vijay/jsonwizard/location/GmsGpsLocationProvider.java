package com.vijay.jsonwizard.location;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.vijay.jsonwizard.R;

import timber.log.Timber;

public class GmsGpsLocationProvider implements GpsLocationProvider, LocationListener,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private final Context context;
    private final Callback callback;
    private GoogleApiClient googleApiClient;
    private long sessionStartElapsedRealtimeNanos;
    private long sessionStartWallClockMillis;

    public GmsGpsLocationProvider(Context context, Callback callback) {
        this.context = context.getApplicationContext();
        this.callback = callback;
    }

    @Override
    public void start() {
        if (googleApiClient == null) {
            Timber.i("GMS GPS provider: building GoogleApiClient");
            googleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .build();
        }

        markSessionStart();
        Timber.i("GMS GPS provider: session started, startWallClockMillis=%s, startElapsedRealtimeNanos=%s",
                sessionStartWallClockMillis, sessionStartElapsedRealtimeNanos);
        Timber.i("GMS GPS provider: connecting GoogleApiClient");
        googleApiClient.connect();
    }

    @Override
    public void stop() {
        if (googleApiClient != null) {
            Timber.i("GMS GPS provider: stopping location updates, isConnected=%s", googleApiClient.isConnected());
            if (googleApiClient.isConnected()) {
                LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, this);
            }
            googleApiClient.disconnect();
        }
    }

    @Override
    public boolean isUsingGooglePlayServices() {
        return true;
    }

    @Override
    @SuppressLint("MissingPermission")
    public void onConnected(@Nullable Bundle bundle) {
        try {
            Timber.i("GMS GPS provider: GoogleApiClient connected, bundle=%s", bundle);
            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setInterval(5000);
            locationRequest.setFastestInterval(1000);
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            Timber.i("GMS GPS provider: requesting fused location updates interval=%s fastestInterval=%s priority=%s",
                    5000, 1000, LocationRequest.PRIORITY_HIGH_ACCURACY);
            LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        } catch (SecurityException exception) {
            Timber.e(exception, "GMS GPS provider: security exception while requesting fused updates");
            callback.onLocationError(R.string.could_not_get_your_location, true);
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        Timber.w("GMS GPS provider: connection suspended cause=%s", cause);
        callback.onLocationError(R.string.could_not_get_your_location, true);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Timber.w("GMS GPS provider: connection failed result=%s", connectionResult);
        callback.onLocationError(R.string.could_not_get_your_location, true);
    }

    @Override
    public void onLocationChanged(Location location) {
        boolean accepted = isLocationFromCurrentSession(location);
        Timber.i("GMS GPS provider: onLocationChanged accepted=%s, location=%s", accepted, describeLocation(location));
        if (accepted) {
            callback.onLocationUpdate(location);
        }
    }

    private void markSessionStart() {
        sessionStartElapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
        sessionStartWallClockMillis = System.currentTimeMillis();
    }

    boolean isLocationFromCurrentSession(Location location) {
        if (location == null) {
            return false;
        }

        long locationElapsedRealtimeNanos = location.getElapsedRealtimeNanos();
        if (locationElapsedRealtimeNanos > 0L) {
            boolean accepted = locationElapsedRealtimeNanos >= sessionStartElapsedRealtimeNanos;
            if (!accepted) {
                Timber.w("GMS GPS provider: rejecting stale location by elapsed realtime. locationElapsedRealtimeNanos=%s, sessionStartElapsedRealtimeNanos=%s, location=%s",
                        locationElapsedRealtimeNanos, sessionStartElapsedRealtimeNanos, describeLocation(location));
            }
            return accepted;
        }

        boolean accepted = location.getTime() >= sessionStartWallClockMillis;
        if (!accepted) {
            Timber.w("GMS GPS provider: rejecting stale location by wall clock. locationTime=%s, sessionStartWallClockMillis=%s, location=%s",
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
