package com.vijay.jsonwizard.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.widget.Toast;

import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.barcode.BarcodeScanResult;
import com.vijay.jsonwizard.barcode.BarcodeScanner;
import com.vijay.jsonwizard.barcode.BarcodeScannerFactory;
import com.vijay.jsonwizard.utils.barcode.JsonFormCameraSourcePreview;

public class JsonFormBarcodeScanActivity extends Activity implements BarcodeScanner.Callback {

    private BarcodeScanner barcodeScanner;
    private JsonFormCameraSourcePreview jsonFormCameraSourcePreview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.json_form_activity_scan_barcode);
        jsonFormCameraSourcePreview = findViewById(R.id.preview);
        barcodeScanner = getBarcodeScannerFactory().create(this, jsonFormCameraSourcePreview, this);
    }

    protected BarcodeScannerFactory getBarcodeScannerFactory() {
        return new BarcodeScannerFactory();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (barcodeScanner != null) {
            barcodeScanner.start();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (barcodeScanner != null) {
            barcodeScanner.stop();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (barcodeScanner != null) {
            barcodeScanner.release();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (barcodeScanner != null && barcodeScanner.onActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBarcodeScanned(BarcodeScanResult barcodeScanResult) {
        vibrateOnScan();
        closeBarcodeActivity(barcodeScanResult);
    }

    @Override
    public void onScanCancelled() {
        setResult(RESULT_CANCELED);
        finish();
    }

    @Override
    public void onScannerError(boolean canFallbackToAosp) {
        if (canFallbackToAosp && barcodeScanner != null && barcodeScanner.isUsingGoogleScanner()) {
            switchToAospScanner();
            return;
        }

        Toast.makeText(this, R.string.barcode_scanner_unavailable, Toast.LENGTH_LONG).show();
        setResult(RESULT_CANCELED);
        finish();
    }

    public void closeBarcodeActivity(BarcodeScanResult barcodeScanResult) {
        Intent intent = new Intent();
        if (barcodeScanResult != null) {
            barcodeScanResult.writeToIntent(intent);
        }
        setResult(RESULT_OK, intent);
        finish();
    }

    private void switchToAospScanner() {
        if (barcodeScanner != null) {
            barcodeScanner.stop();
            barcodeScanner.release();
        }

        barcodeScanner = getBarcodeScannerFactory().createFallbackScanner(this, this);
        barcodeScanner.start();
    }

    private void vibrateOnScan() {
        Vibrator vibrator = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(100);
        }
    }
}
