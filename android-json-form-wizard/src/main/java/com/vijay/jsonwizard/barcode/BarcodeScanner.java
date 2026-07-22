package com.vijay.jsonwizard.barcode;

import android.content.Intent;

public interface BarcodeScanner {

    void start();

    void stop();

    void release();

    boolean onActivityResult(int requestCode, int resultCode, Intent data);

    boolean isUsingGoogleScanner();

    interface Callback {
        void onBarcodeScanned(BarcodeScanResult barcodeScanResult);

        void onScanCancelled();

        void onScannerError(boolean canFallbackToAosp);
    }
}
