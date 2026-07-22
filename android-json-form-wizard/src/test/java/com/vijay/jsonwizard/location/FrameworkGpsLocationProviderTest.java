package com.vijay.jsonwizard.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.os.SystemClock;

import com.vijay.jsonwizard.BaseTest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class FrameworkGpsLocationProviderTest extends BaseTest {

    @Mock
    private Context context;

    @Mock
    private LocationManager locationManager;

    @Mock
    private GpsLocationProvider.Callback callback;

    @Test
    public void testStartAlwaysRequestsFreshLocation() {
        Mockito.doReturn(context).when(context).getApplicationContext();
        Mockito.doReturn(true).when(locationManager).isProviderEnabled(LocationManager.GPS_PROVIDER);
        Mockito.doReturn(false).when(locationManager).isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Mockito.doReturn(false).when(locationManager).isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

        FrameworkGpsLocationProvider frameworkGpsLocationProvider =
                new FrameworkGpsLocationProvider(context, callback, locationManager);

        frameworkGpsLocationProvider.start();

        Mockito.verify(locationManager).requestLocationUpdates(
                Mockito.eq(LocationManager.GPS_PROVIDER),
                Mockito.eq(1000L),
                Mockito.eq(0f),
                Mockito.eq(frameworkGpsLocationProvider),
                Mockito.eq(Looper.getMainLooper())
        );
        Mockito.verify(locationManager, Mockito.never()).requestSingleUpdate(
                Mockito.anyString(),
                Mockito.eq(frameworkGpsLocationProvider),
                Mockito.eq(Looper.getMainLooper())
        );
        Mockito.verify(locationManager, Mockito.never()).getLastKnownLocation(Mockito.anyString());
    }

    @Test
    public void testIgnoresLocationFromBeforeCurrentSession() {
        Mockito.doReturn(context).when(context).getApplicationContext();
        Mockito.doReturn(true).when(locationManager).isProviderEnabled(LocationManager.GPS_PROVIDER);
        Mockito.doReturn(false).when(locationManager).isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Mockito.doReturn(false).when(locationManager).isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
        FrameworkGpsLocationProvider frameworkGpsLocationProvider =
                new FrameworkGpsLocationProvider(context, callback, locationManager);

        frameworkGpsLocationProvider.start();

        Location staleLocation = new Location(LocationManager.GPS_PROVIDER);
        staleLocation.setElapsedRealtimeNanos(1L);
        staleLocation.setTime(1L);

        frameworkGpsLocationProvider.onLocationChanged(staleLocation);

        Mockito.verify(callback, Mockito.never()).onLocationUpdate(Mockito.any(Location.class));
        Assert.assertFalse(frameworkGpsLocationProvider.isLocationFromCurrentSession(staleLocation));
    }

    @Test
    public void testAcceptsLocationFromCurrentSession() {
        Mockito.doReturn(context).when(context).getApplicationContext();
        Mockito.doReturn(true).when(locationManager).isProviderEnabled(LocationManager.GPS_PROVIDER);
        Mockito.doReturn(false).when(locationManager).isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Mockito.doReturn(false).when(locationManager).isProviderEnabled(LocationManager.PASSIVE_PROVIDER);
        FrameworkGpsLocationProvider frameworkGpsLocationProvider =
                new FrameworkGpsLocationProvider(context, callback, locationManager);

        frameworkGpsLocationProvider.start();

        Location freshLocation = new Location(LocationManager.GPS_PROVIDER);
        freshLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        freshLocation.setTime(System.currentTimeMillis());

        frameworkGpsLocationProvider.onLocationChanged(freshLocation);

        Mockito.verify(callback).onLocationUpdate(freshLocation);
        Assert.assertTrue(frameworkGpsLocationProvider.isLocationFromCurrentSession(freshLocation));
    }

    @Test
    public void testStartRequestsUpdatesFromEnabledNetworkProvider() {
        Mockito.doReturn(context).when(context).getApplicationContext();
        Mockito.doReturn(true).when(locationManager).isProviderEnabled(LocationManager.GPS_PROVIDER);
        Mockito.doReturn(true).when(locationManager).isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        Mockito.doReturn(false).when(locationManager).isProviderEnabled(LocationManager.PASSIVE_PROVIDER);

        FrameworkGpsLocationProvider frameworkGpsLocationProvider =
                new FrameworkGpsLocationProvider(context, callback, locationManager);

        frameworkGpsLocationProvider.start();

        Mockito.verify(locationManager).requestLocationUpdates(
                Mockito.eq(LocationManager.NETWORK_PROVIDER),
                Mockito.eq(1000L),
                Mockito.eq(0f),
                Mockito.eq(frameworkGpsLocationProvider),
                Mockito.eq(Looper.getMainLooper())
        );
    }
}
