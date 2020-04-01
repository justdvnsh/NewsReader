package com.divyansh.newsreader;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.divyansh.newsreader.handlers.MyNotificationOpenHandler;
import com.divyansh.newsreader.ui.WebViewActivity;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationAction;
import com.onesignal.OSNotificationOpenResult;
import com.onesignal.OneSignal;

import org.json.JSONObject;

public class MyApplication extends Application {

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

    }

    public static Context getInstance() {
        return mInstance;
    }
}
