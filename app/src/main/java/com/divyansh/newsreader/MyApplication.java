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

    private static MyApplication mInstance;

    public MyApplication() {
        mInstance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;


        // load onesignal sdk
        // OneSignal Initialization
        OneSignal.startInit(this)
                .setNotificationReceivedHandler(new ExampleNotificationReceivedHandler())
                .setNotificationOpenedHandler(new MyNotificationOpenHandler())
                .init();

    }

    class ExampleNotificationReceivedHandler implements OneSignal.NotificationReceivedHandler {
        @Override
        public void notificationReceived(OSNotification notification) {
            JSONObject data = notification.payload.additionalData;
            String customKey;

            if (data != null) {
                customKey = data.optString("customkey", null);
                if (customKey != null)
                    Log.i("OneSignalExample", "customkey set with value: " + customKey);
            }
        }
    }

    public static MyApplication getInstance() {
        return mInstance;
    }
}
