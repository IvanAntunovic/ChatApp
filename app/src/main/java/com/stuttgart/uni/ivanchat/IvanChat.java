package com.stuttgart.uni.ivanchat;

import android.app.Application;

import com.google.firebase.database.FirebaseDatabase;

public class IvanChat extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        FirebaseDatabase.getInstance().setPersistenceEnabled(true);

    }
}
