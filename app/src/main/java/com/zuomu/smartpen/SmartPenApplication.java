package com.zuomu.smartpen;

import android.app.Application;
import com.zuomu.smartpen.connection.SmartPenConnection;

public class SmartPenApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        SmartPenConnection.init(this);
    }
} 