package org.smartregister.nativeform;

import android.app.Application;

import timber.log.Timber;

public class NativeFormsApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
