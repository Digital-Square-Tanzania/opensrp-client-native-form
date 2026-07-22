package com.vijay.jsonwizard.barcode;

import android.content.Intent;

import com.vijay.jsonwizard.BaseTest;
import com.vijay.jsonwizard.constants.JsonFormConstants;

import org.junit.Assert;
import org.junit.Test;

public class BarcodeScanResultTest extends BaseTest {

    @Test
    public void testWritesAndReadsIntentExtras() {
        Intent intent = new Intent();
        BarcodeScanResult barcodeScanResult = new BarcodeScanResult("12345", "QR_CODE");

        barcodeScanResult.writeToIntent(intent);
        BarcodeScanResult restoredBarcodeScanResult = BarcodeScanResult.fromIntent(intent);

        Assert.assertNotNull(restoredBarcodeScanResult);
        Assert.assertEquals("12345", restoredBarcodeScanResult.getValue());
        Assert.assertEquals("QR_CODE", restoredBarcodeScanResult.getFormat());
        Assert.assertEquals("12345", intent.getStringExtra(JsonFormConstants.BARCODE_CONSTANTS.BARCODE_VALUE_KEY));
    }

    @Test
    public void testMissingValueReturnsNullResult() {
        Assert.assertNull(BarcodeScanResult.fromIntent(new Intent()));
    }
}
