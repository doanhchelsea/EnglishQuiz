package com.example.doanhchelsea.english;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class MyApplications extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
