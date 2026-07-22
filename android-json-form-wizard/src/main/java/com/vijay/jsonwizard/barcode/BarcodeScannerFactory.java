package com.vijay.jsonwizard.barcode;

import android.app.Activity;
import android.content.Context;

import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.vijay.jsonwizard.utils.GooglePlayServicesHelper;
import com.vijay.jsonwizard.utils.barcode.JsonFormCameraSourcePreview;

import timber.log.Timber;

public class BarcodeScannerFactory {

    public BarcodeScanner create(Activity activity, JsonFormCameraSourcePreview preview, BarcodeScanner.Callback callback) {
        if (shouldUseGoogleScanner(activity)) {
            return createGmsScanner(activity, preview, callback);
        }

        return createAospScanner(activity, callback);
    }

    public BarcodeScanner createFallbackScanner(Activity activity, BarcodeScanner.Callback callback) {
        return createAospScanner(activity, callback);
    }

    protected boolean shouldUseGoogleScanner(Context context) {
        if (!isGooglePlayServicesAvailable(context)) {
            return false;
        }

        BarcodeDetector barcodeDetector = null;
        try {
            barcodeDetector = new BarcodeDetector.Builder(context).build();
            return barcodeDetector.isOperational();
        } catch (RuntimeException exception) {
            Timber.w(exception, "Unable to initialize Google barcode detector");
            return false;
        } finally {
            if (barcodeDetector != null) {
                barcodeDetector.release();
            }
        }
    }

    protected boolean isGooglePlayServicesAvailable(Context context) {
        return GooglePlayServicesHelper.isAvailable(context);
    }

    protected BarcodeScanner createGmsScanner(Activity activity, JsonFormCameraSourcePreview preview, BarcodeScanner.Callback callback) {
        return new GmsBarcodeScanner(activity, preview, callback);
    }

    protected BarcodeScanner createAospScanner(Activity activity, BarcodeScanner.Callback callback) {
        return new AospBarcodeScanner(activity, callback);
    }
}
