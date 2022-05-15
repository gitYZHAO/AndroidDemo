package me.android.demo.application;

import android.app.Application;

import me.android.demo.util.JavaCrashHandler;

public class DemoApplication extends Application {
    static DemoApplication mApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        mApplication = this;
        JavaCrashHandler.init(mApplication);
    }

    public static Application getApplication() {
        return mApplication;
    }
}
