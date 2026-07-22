package com.vijay.jsonwizard.location;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

import com.vijay.jsonwizard.BaseTest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.powermock.reflect.Whitebox;

public class GmsGpsLocationProviderTest extends BaseTest {

    @Mock
    private Context context;

    @Mock
    private GpsLocationProvider.Callback callback;

    @Test
    public void testRejectsLocationOlderThanCaptureSession() {
        GmsGpsLocationProvider gmsGpsLocationProvider = new GmsGpsLocationProvider(context, callback);
        Whitebox.setInternalState(gmsGpsLocationProvider, "sessionStartElapsedRealtimeNanos", 1000L);
        Whitebox.setInternalState(gmsGpsLocationProvider, "sessionStartWallClockMillis", 1000L);

        Location staleLocation = new Location("fused");
        staleLocation.setElapsedRealtimeNanos(1L);
        staleLocation.setTime(1L);

        Assert.assertFalse(gmsGpsLocationProvider.isLocationFromCurrentSession(staleLocation));
    }

    @Test
    public void testAcceptsLocationFromCurrentSession() {
        GmsGpsLocationProvider gmsGpsLocationProvider = new GmsGpsLocationProvider(context, callback);
        long sessionStartElapsedRealtimeNanos = SystemClock.elapsedRealtimeNanos();
        long sessionStartWallClockMillis = System.currentTimeMillis();
        Whitebox.setInternalState(gmsGpsLocationProvider, "sessionStartElapsedRealtimeNanos", sessionStartElapsedRealtimeNanos);
        Whitebox.setInternalState(gmsGpsLocationProvider, "sessionStartWallClockMillis", sessionStartWallClockMillis);

        Location freshLocation = new Location("fused");
        freshLocation.setElapsedRealtimeNanos(sessionStartElapsedRealtimeNanos);
        freshLocation.setTime(sessionStartWallClockMillis);

        Assert.assertTrue(gmsGpsLocationProvider.isLocationFromCurrentSession(freshLocation));
    }
}
