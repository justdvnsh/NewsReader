package com.divyansh.newsreader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.multidex.MultiDex;
import androidx.multidex.MultiDexApplication;

import com.divyansh.newsreader.handlers.MyNotificationOpenHandler;
import com.google.firebase.FirebaseApp;
import com.onesignal.OneSignal;

public class MyApplication extends MultiDexApplication {

    private static Context mInstance;

    public MyApplication() {
        mInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;

        // OneSignal Initialization
        OneSignal.startInit(this)
                .setNotificationOpenedHandler(new MyNotificationOpenHandler(mInstance))
                .init();

        FirebaseApp.initializeApp(this);

    }

    public static Context getInstance() {
        return mInstance;
    }
}
