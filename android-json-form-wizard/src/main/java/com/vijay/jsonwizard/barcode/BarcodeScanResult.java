package com.vijay.jsonwizard.barcode;

import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.vijay.jsonwizard.constants.JsonFormConstants;

public class BarcodeScanResult {

    private final String value;
    private final String format;

    public BarcodeScanResult(String value, @Nullable String format) {
        this.value = value;
        this.format = format;
    }

    public String getValue() {
        return value;
    }

    @Nullable
    public String getFormat() {
        return format;
    }

    public void writeToIntent(Intent intent) {
        if (intent == null) {
            return;
        }

        intent.putExtra(JsonFormConstants.BARCODE_CONSTANTS.BARCODE_VALUE_KEY, value);
        intent.putExtra(JsonFormConstants.BARCODE_CONSTANTS.BARCODE_FORMAT_KEY, format);
    }

    @Nullable
    public static BarcodeScanResult fromIntent(Intent intent) {
        if (intent == null) {
            return null;
        }

        String barcodeValue = intent.getStringExtra(JsonFormConstants.BARCODE_CONSTANTS.BARCODE_VALUE_KEY);
        if (TextUtils.isEmpty(barcodeValue)) {
            return null;
        }

        return new BarcodeScanResult(
                barcodeValue,
                intent.getStringExtra(JsonFormConstants.BARCODE_CONSTANTS.BARCODE_FORMAT_KEY)
        );
    }
}
