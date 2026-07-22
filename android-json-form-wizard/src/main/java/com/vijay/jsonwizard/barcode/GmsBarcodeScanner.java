package com.vijay.jsonwizard.barcode;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import com.vijay.jsonwizard.utils.barcode.JsonFormCameraSourcePreview;

import java.io.IOException;

import timber.log.Timber;

public class GmsBarcodeScanner implements BarcodeScanner, Detector.Processor<Barcode> {

    private final Activity activity;
    private final JsonFormCameraSourcePreview preview;
    private final Callback callback;

    private BarcodeDetector barcodeDetector;
    private CameraSource cameraSource;
    private boolean resultDispatched;

    public GmsBarcodeScanner(Activity activity, JsonFormCameraSourcePreview preview, Callback callback) {
        this.activity = activity;
        this.preview = preview;
        this.callback = callback;
        this.resultDispatched = false;
        createCameraSource();
    }

    @Override
    public void start() {
        if (cameraSource == null) {
            createCameraSource();
        }

        if (cameraSource != null) {
            try {
                preview.start(cameraSource);
            } catch (IOException exception) {
                Timber.e(exception, "Unable to start Google barcode scanner");
                callback.onScannerError(true);
            } catch (SecurityException exception) {
                Timber.e(exception, "Missing camera permission for Google barcode scanner");
                callback.onScannerError(true);
            }
        }
    }

    @Override
    public void stop() {
        preview.stop();
    }

    @Override
    public void release() {
        preview.release();
        if (barcodeDetector != null) {
            barcodeDetector.release();
            barcodeDetector = null;
        }
        cameraSource = null;
    }

    @Override
    public boolean onActivityResult(int requestCode, int resultCode, Intent data) {
        return false;
    }

    @Override
    public boolean isUsingGoogleScanner() {
        return true;
    }

    @Override
    public void receiveDetections(Detector.Detections<Barcode> detections) {
        if (resultDispatched) {
            return;
        }

        SparseArray<Barcode> barcodeSparseArray = detections.getDetectedItems();
        if (barcodeSparseArray.size() > 0) {
            Barcode barcode = barcodeSparseArray.valueAt(0);
            resultDispatched = true;
            callback.onBarcodeScanned(new BarcodeScanResult(
                    barcode.displayValue != null ? barcode.displayValue : barcode.rawValue,
                    String.valueOf(barcode.format)
            ));
        }
    }

    @SuppressLint("InlinedApi")
    private void createCameraSource() {
        Context context = activity.getApplicationContext();
        barcodeDetector = new BarcodeDetector.Builder(context).build();
        barcodeDetector.setProcessor(this);

        cameraSource = new CameraSource.Builder(context, barcodeDetector)
                .setFacing(CameraSource.CAMERA_FACING_BACK)
                .setRequestedPreviewSize(1600, 1024)
                .setAutoFocusEnabled(true)
                .setRequestedFps(45.0f)
                .build();
    }
}
