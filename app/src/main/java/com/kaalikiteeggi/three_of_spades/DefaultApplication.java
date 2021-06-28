package com.kaalikiteeggi.three_of_spades;

import android.app.Application;

public class DefaultApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SoundManager.Companion.initialize(getApplicationContext());
    }
}
