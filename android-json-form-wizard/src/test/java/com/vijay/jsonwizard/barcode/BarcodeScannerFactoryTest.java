package com.vijay.jsonwizard.barcode;

import android.app.Activity;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.utils.barcode.JsonFormCameraSourcePreview;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

public class BarcodeScannerFactoryTest extends BaseTest {

    @Mock
    private Activity activity;

    @Mock
    private JsonFormCameraSourcePreview preview;

    @Mock
    private BarcodeScanner.Callback callback;

    @Mock
    private BarcodeScanner gmsScanner;

    @Mock
    private BarcodeScanner aospScanner;

    @Test
    public void testFactoryReturnsGmsScannerWhenAvailable() {
        BarcodeScannerFactory factory = Mockito.spy(new BarcodeScannerFactory());
        Mockito.doReturn(true).when(factory).shouldUseGoogleScanner(activity);
        Mockito.doReturn(gmsScanner).when(factory).createGmsScanner(activity, preview, callback);

        Assert.assertSame(gmsScanner, factory.create(activity, preview, callback));
    }

    @Test
    public void testFactoryReturnsAospScannerWhenGoogleScannerIsUnavailable() {
        BarcodeScannerFactory factory = Mockito.spy(new BarcodeScannerFactory());
        Mockito.doReturn(false).when(factory).shouldUseGoogleScanner(activity);
        Mockito.doReturn(aospScanner).when(factory).createAospScanner(activity, callback);

        Assert.assertSame(aospScanner, factory.create(activity, preview, callback));
    }

    @Test
    public void testFactoryReturnsExplicitFallbackScanner() {
        BarcodeScannerFactory factory = Mockito.spy(new BarcodeScannerFactory());
        Mockito.doReturn(aospScanner).when(factory).createAospScanner(activity, callback);

        Assert.assertSame(aospScanner, factory.createFallbackScanner(activity, callback));
    }
}
