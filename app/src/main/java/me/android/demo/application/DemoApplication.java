package me.android.demo.application;

import android.app.Application;

import me.android.demo.util.JavaCrashHandler;

public class DemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DemoApplication mApplication = this;
        JavaCrashHandler.init(mApplication);
    }
}
