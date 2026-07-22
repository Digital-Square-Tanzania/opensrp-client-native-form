package com.vijay.jsonwizard.location;

import android.content.Context;

import com.vijay.jsonwizard.BaseTest;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class GpsLocationProviderFactoryTest extends BaseTest {

    @Mock
    private Context context;

    @Mock
    private GpsLocationProvider.Callback callback;

    @Mock
    private GpsLocationProvider gmsLocationProvider;

    @Mock
    private GpsLocationProvider frameworkLocationProvider;

    @Test
    public void testFactoryReturnsGmsLocationProviderWhenPreferredAndAvailable() {
        GpsLocationProviderFactory factory = Mockito.spy(new GpsLocationProviderFactory());
        Mockito.doReturn(true).when(factory).isGooglePlayServicesAvailable(context);
        Mockito.doReturn(gmsLocationProvider).when(factory).createGmsLocationProvider(context, callback);

        Assert.assertSame(gmsLocationProvider, factory.create(context, callback, true));
    }

    @Test
    public void testFactoryReturnsFrameworkProviderWhenGooglePlayServicesIsUnavailable() {
        GpsLocationProviderFactory factory = Mockito.spy(new GpsLocationProviderFactory());
        Mockito.doReturn(false).when(factory).isGooglePlayServicesAvailable(context);
        Mockito.doReturn(frameworkLocationProvider).when(factory).createFrameworkLocationProvider(context, callback);

        Assert.assertSame(frameworkLocationProvider, factory.create(context, callback, true));
    }

    @Test
    public void testFactoryReturnsFrameworkProviderWhenGooglePlayServicesIsNotPreferred() {
        GpsLocationProviderFactory factory = Mockito.spy(new GpsLocationProviderFactory());
        Mockito.doReturn(frameworkLocationProvider).when(factory).createFrameworkLocationProvider(context, callback);

        Assert.assertSame(frameworkLocationProvider, factory.create(context, callback, false));
    }
}
