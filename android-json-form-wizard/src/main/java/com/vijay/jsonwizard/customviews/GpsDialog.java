package com.vijay.jsonwizard.customviews;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.rey.material.widget.TextView;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.location.GpsLocationProvider;
import com.vijay.jsonwizard.location.GpsLocationProviderFactory;
import com.vijay.jsonwizard.widgets.GpsFactory;

import timber.log.Timber;

/**
 * Created by Jason Rogena - jrogena@ona.io on 11/24/17.
 */
public class GpsDialog extends Dialog {
    private static final double MIN_ACCURACY = 5d;
    private static final long LOCATION_FIX_TIMEOUT_MILLIS = 60 * 1000L;

    private final View dataView;
    private final TextView latitudeTV;
    private final TextView longitudeTV;
    private final TextView altitudeTV;
    private final TextView accuracyTV;
    private final Context context;
    private final GpsLocationProviderFactory locationProviderFactory;
    private final Handler handler;
    private final Runnable locationTimeoutRunnable;

    private TextView dialogAccuracyTV;
    private GpsLocationProvider locationProvider;
    private Location lastLocation;
    private boolean usingPlatformFallback;

    public GpsDialog(Context context, View dataView, TextView latitudeTV, TextView longitudeTV, TextView altitudeTV, TextView accuracyTV) {
        this(context, dataView, latitudeTV, longitudeTV, altitudeTV, accuracyTV, new GpsLocationProviderFactory());
    }

    GpsDialog(Context context, View dataView, TextView latitudeTV, TextView longitudeTV,
              TextView altitudeTV, TextView accuracyTV, GpsLocationProviderFactory locationProviderFactory) {
        super(context);
        this.context = context;
        this.dataView = dataView;
        this.latitudeTV = latitudeTV;
        this.longitudeTV = longitudeTV;
        this.altitudeTV = altitudeTV;
        this.accuracyTV = accuracyTV;
        this.locationProviderFactory = locationProviderFactory;
        this.handler = new Handler(Looper.getMainLooper());
        this.locationTimeoutRunnable = new Runnable() {
            @Override
            public void run() {
                handleLocationTimeout();
            }
        };
        init();
    }

    protected void init() {
        Timber.i("GPS dialog: initializing");
        setContentView(R.layout.dialog_gps);
        setTitle(R.string.loading_location);
        setCancelable(false);
        lastLocation = null;
        setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialogInterface) {
                Timber.i("GPS dialog: dismissed");
                cancelLocationTimeout();
                stopLocationUpdates();
            }
        });

        Button okButton = findViewById(R.id.ok_button);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAndDismiss();
            }
        });

        Button cancelButton = findViewById(R.id.cancel_button);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GpsDialog.this.dismiss();
            }
        });

        dialogAccuracyTV = findViewById(R.id.accuracy);

        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Timber.i("GPS dialog: shown, starting location updates");
                startLocationUpdates(true);
            }
        });
    }

    protected void saveAndDismiss() {
        Timber.i("GPS dialog: saving and dismissing with location=%s", describeLocation(lastLocation));
        updateLocationViews(lastLocation);
        dismiss();
    }

    protected void startLocationUpdates(boolean preferGooglePlayServices) {
        Timber.i("GPS dialog: startLocationUpdates preferGooglePlayServices=%s", preferGooglePlayServices);
        cancelLocationTimeout();
        stopLocationUpdates();
        locationProvider = locationProviderFactory.create(context, getLocationCallback(), preferGooglePlayServices);
        usingPlatformFallback = !locationProvider.isUsingGooglePlayServices();
        Timber.i("GPS dialog: using provider=%s, usingPlatformFallback=%s",
                locationProvider.getClass().getSimpleName(), usingPlatformFallback);
        locationProvider.start();
        scheduleLocationTimeout();
    }

    private void switchToPlatformFallback() {
        Timber.w("GPS dialog: switching to platform fallback provider");
        startLocationUpdates(false);
    }

    private void stopLocationUpdates() {
        if (locationProvider != null) {
            Timber.i("GPS dialog: stopping provider=%s", locationProvider.getClass().getSimpleName());
            locationProvider.stop();
            locationProvider = null;
        }
    }

    private void scheduleLocationTimeout() {
        Timber.i("GPS dialog: scheduling timeout in %s ms", LOCATION_FIX_TIMEOUT_MILLIS);
        handler.postDelayed(locationTimeoutRunnable, LOCATION_FIX_TIMEOUT_MILLIS);
    }

    private void cancelLocationTimeout() {
        Timber.i("GPS dialog: cancelling timeout");
        handler.removeCallbacks(locationTimeoutRunnable);
    }

    private void handleLocationTimeout() {
        Timber.w("GPS dialog: timeout fired, lastLocation=%s, usingPlatformFallback=%s, provider=%s",
                describeLocation(lastLocation), usingPlatformFallback,
                locationProvider != null ? locationProvider.getClass().getSimpleName() : "null");
        if (lastLocation != null) {
            return;
        }

        if (locationProvider != null && locationProvider.isUsingGooglePlayServices() && !usingPlatformFallback) {
            switchToPlatformFallback();
            return;
        }

        stopLocationUpdates();
        Toast.makeText(context, R.string.could_not_get_your_location, Toast.LENGTH_LONG).show();
        dismiss();
    }

    private GpsLocationProvider.Callback getLocationCallback() {
        return new GpsLocationProvider.Callback() {
            @Override
            public void onLocationUpdate(Location location) {
                onLocationReceived(location);
            }

            @Override
            public void onLocationError(int errorResId, boolean canFallbackToPlatform) {
                onLocationProviderError(errorResId, canFallbackToPlatform);
            }
        };
    }

    private void onLocationReceived(Location location) {
        if (location == null) {
            Timber.w("GPS dialog: received null location");
            return;
        }

        Timber.i("GPS dialog: received location=%s", describeLocation(location));
        cancelLocationTimeout();
        lastLocation = location;
        updateDialogAccuracy(location);
        if (lastLocation.getAccuracy() <= MIN_ACCURACY) {
            Timber.i("GPS dialog: accuracy threshold met (%s <= %s), auto-saving",
                    lastLocation.getAccuracy(), MIN_ACCURACY);
            saveAndDismiss();
        } else {
            Timber.i("GPS dialog: accuracy threshold not met yet (%s > %s), waiting for better fix",
                    lastLocation.getAccuracy(), MIN_ACCURACY);
        }
    }

    private void onLocationProviderError(int errorResId, boolean canFallbackToPlatform) {
        Timber.w("GPS dialog: provider error errorResId=%s canFallbackToPlatform=%s provider=%s",
                errorResId, canFallbackToPlatform,
                locationProvider != null ? locationProvider.getClass().getSimpleName() : "null");
        if (canFallbackToPlatform && locationProvider != null
                && locationProvider.isUsingGooglePlayServices() && !usingPlatformFallback) {
            switchToPlatformFallback();
            return;
        }

        cancelLocationTimeout();
        stopLocationUpdates();
        Toast.makeText(context, errorResId, Toast.LENGTH_LONG).show();
        if (lastLocation == null) {
            dismiss();
        }
    }

    private void updateDialogAccuracy(Location location) {
        if (location != null && dialogAccuracyTV != null) {
            Timber.i("GPS dialog: updating dialog accuracy text accuracy=%s", location.getAccuracy());
            dialogAccuracyTV.setText(String.format(context.getString(R.string.accuracy),
                    String.valueOf(location.getAccuracy()) + " m"));
        }
    }

    private void updateLocationViews(Location location) {
        if (location != null) {
            Timber.i("GPS dialog: writing location to views location=%s", describeLocation(location));
            latitudeTV.setText(String.format(context.getString(R.string.latitude), String.valueOf(location.getLatitude())));
            longitudeTV.setText(String.format(context.getString(R.string.longitude), String.valueOf(location.getLongitude())));
            altitudeTV.setText(String.format(context.getString(R.string.altitude), String.valueOf(location.getAltitude()) + " m"));
            accuracyTV.setText(String.format(context.getString(R.string.accuracy), String.valueOf(location.getAccuracy()) + " m"));
            dataView.setTag(R.id.raw_value, GpsFactory.constructString(location));
            GpsFactory.clearValidationError(dataView);
        }
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

    public View getDataView() {
        return dataView;
    }

    public TextView getLatitudeTV() {
        return latitudeTV;
    }

    public TextView getLongitudeTV() {
        return longitudeTV;
    }

    public TextView getAltitudeTV() {
        return altitudeTV;
    }

    public TextView getAccuracyTV() {
        return accuracyTV;
    }

    public TextView getDialogAccuracyTV() {
        return dialogAccuracyTV;
    }

    public void setDialogAccuracyTV(TextView dialogAccuracyTV) {
        this.dialogAccuracyTV = dialogAccuracyTV;
    }
}
