package me.android.demo.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;

import androidx.annotation.Nullable;

import me.android.demo.util.Logger;

public class LocalClientService extends Service {
    final static String TAG = "LocalClient";
    Context mContext;
    private IRemoteService remoteService;

    @Nullable
    @android.support.annotation.Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Logger.d(TAG, "onCreate");
        mContext = getBaseContext();
        bindService();
        super.onCreate();
    }

    public IRemoteService getRemoteService() {
        synchronized (this) {
            return remoteService;
        }
    }

    public boolean bindService() {
        Intent intent = new Intent(mContext, RemoteServices.class);
        // intent.setComponent(new ComponentName("me.android.demo", "me.android.demo.service.RemoteServices"));
        boolean bindSuccessful = mContext.bindService(intent, connection, Context.BIND_AUTO_CREATE);
        Logger.d(TAG, "bindSuccessful:" + bindSuccessful);

        return bindSuccessful;
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Logger.d(TAG, "onServiceConnected");
            try {
                //获取代理
                synchronized (this) {
                    remoteService = IRemoteService.Stub.asInterface(service);
                }
                if (remoteService == null) {
                    Logger.e("remoteService is null!");
                    return;
                }

                remoteService.addPhone("apple");
                Logger.d(TAG, "getPhone:" + remoteService.getPhone());

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Logger.d(TAG, "onServiceDisconnected");
            synchronized (this) {
                remoteService = null;
            }
        }
    };


}
