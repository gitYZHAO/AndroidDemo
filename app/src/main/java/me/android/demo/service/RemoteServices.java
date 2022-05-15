package me.android.demo.service;

import static java.lang.Thread.sleep;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import me.android.demo.util.Logger;

public class RemoteServices extends Service {
    final String TAG = "RemoteServices";

    @Override
    public void onCreate() {
        Logger.d(TAG,"onCreate.");
        super.onCreate();
    }

    @Override
    public void onDestroy() {
        Logger.d(TAG,"onDestroy.");
        super.onDestroy();
    }

    @Nullable
    @android.support.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    //实现binder接口
    //auto gen:build/generated/aidl_source_output_dir/debug/out/me/android/demo/service/IRemoteService.java
    private IRemoteService.Stub mBinder = new IRemoteService.Stub() {
        String phoneName = null;

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public void addPhone(String name) throws RemoteException {
            Logger.d(TAG,"addPhone:" + name);
            try {
                sleep(3299);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Logger.d(TAG,"sleep done.");
            phoneName = name + " has eat.";
        }

        @Override
        public String getPhone() throws RemoteException {
            return phoneName;
        }
    };
}
