package com.vijay.jsonwizard.activities;

import android.app.Activity;
import android.content.Intent;

import com.vijay.jsonwizard.barcode.BarcodeScanResult;
import com.vijay.jsonwizard.barcode.BarcodeScanner;
import com.vijay.jsonwizard.barcode.BarcodeScannerFactory;
import com.vijay.jsonwizard.utils.barcode.JsonFormCameraSourcePreview;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;

public class JsonFormBarcodeScanActivityTest extends BaseActivityTest {

    public static class TestJsonFormBarcodeScanActivity extends JsonFormBarcodeScanActivity {
        static BarcodeScannerFactory barcodeScannerFactory;

        @Override
        protected BarcodeScannerFactory getBarcodeScannerFactory() {
            return barcodeScannerFactory;
        }
    }

    private ActivityController<TestJsonFormBarcodeScanActivity> controller;
    private TestJsonFormBarcodeScanActivity barcodeScanActivity;

    @Mock
    private BarcodeScannerFactory barcodeScannerFactory;

    @Mock
    private BarcodeScanner barcodeScanner;

    @Mock
    private BarcodeScanner fallbackBarcodeScanner;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        TestJsonFormBarcodeScanActivity.barcodeScannerFactory = barcodeScannerFactory;
        Mockito.doReturn(barcodeScanner).when(barcodeScannerFactory)
                .create(ArgumentMatchers.any(Activity.class), ArgumentMatchers.any(JsonFormCameraSourcePreview.class),
                        ArgumentMatchers.any(BarcodeScanner.Callback.class));
        Mockito.doReturn(fallbackBarcodeScanner).when(barcodeScannerFactory)
                .createFallbackScanner(ArgumentMatchers.any(Activity.class), ArgumentMatchers.any(BarcodeScanner.Callback.class));
        controller = Robolectric.buildActivity(TestJsonFormBarcodeScanActivity.class).create().start().resume();
        barcodeScanActivity = controller.get();
    }

    @After
    public void tearDown() {
        TestJsonFormBarcodeScanActivity.barcodeScannerFactory = null;
        controller.pause().stop().destroy();
    }

    @Test
    public void testCloseActivityWritesProviderNeutralExtras() {
        barcodeScanActivity.closeBarcodeActivity(new BarcodeScanResult("test-value", "QR_CODE"));

        Assert.assertTrue(barcodeScanActivity.isFinishing());
        Intent resultIntent = Shadows.shadowOf(barcodeScanActivity).getResultIntent();
        Assert.assertEquals(Activity.RESULT_OK, Shadows.shadowOf(barcodeScanActivity).getResultCode());
        Assert.assertEquals("test-value", resultIntent.getStringExtra("barcode_value"));
        Assert.assertEquals("QR_CODE", resultIntent.getStringExtra("barcode_format"));
    }

    @Test
    public void testCancelledScanReturnsCancelledResult() {
        barcodeScanActivity.onScanCancelled();

        Assert.assertTrue(barcodeScanActivity.isFinishing());
        Assert.assertEquals(Activity.RESULT_CANCELED, Shadows.shadowOf(barcodeScanActivity).getResultCode());
    }

    @Test
    public void testScannerErrorSwitchesToAospFallback() {
        Mockito.doReturn(true).when(barcodeScanner).isUsingGoogleScanner();

        barcodeScanActivity.onScannerError(true);

        Mockito.verify(barcodeScanner).stop();
        Mockito.verify(barcodeScanner).release();
        Mockito.verify(barcodeScannerFactory).createFallbackScanner(barcodeScanActivity, barcodeScanActivity);
        Mockito.verify(fallbackBarcodeScanner).start();
    }
}
