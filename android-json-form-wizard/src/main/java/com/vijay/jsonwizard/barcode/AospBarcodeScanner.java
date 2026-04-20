package com.vijay.jsonwizard.barcode;

import android.app.Activity;
import android.content.Intent;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

public class AospBarcodeScanner implements BarcodeScanner {

    private final Activity activity;
    private final Callback callback;
    private boolean launched;

    public AospBarcodeScanner(Activity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
        this.launched = false;
    }

    @Override
    public void start() {
        if (launched) {
            return;
        }

        launched = true;
        IntentIntegrator intentIntegrator = new IntentIntegrator(activity);
        intentIntegrator.setBeepEnabled(false);
        intentIntegrator.setOrientationLocked(true);
        intentIntegrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        intentIntegrator.initiateScan();
    }

    @Override
    public void stop() {
        // The embedded ZXing capture activity manages its own camera lifecycle.
    }

    @Override
    public void release() {
        // No-op.
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult == null) {
            return false;
        }

        if (intentResult.getContents() != null) {
            callback.onBarcodeScanned(new BarcodeScanResult(intentResult.getContents(), intentResult.getFormatName()));
        } else {
            callback.onScanCancelled();
        }
        return true;
    }

    @Override
    public boolean isUsingGoogleScanner() {
        return false;
    }
}
