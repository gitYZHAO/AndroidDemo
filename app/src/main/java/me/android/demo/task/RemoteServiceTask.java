package me.android.demo.task;

import android.app.Application;
import android.content.ComponentName;
import android.content.Intent;
import android.os.RemoteException;

import me.android.demo.application.DemoApplication;
import me.android.demo.service.IRemoteService;
import me.android.demo.service.LocalClientService;
import me.android.demo.util.Logger;

public class RemoteServiceTask implements Runnable {

    @Override
    public void run() {
        Application application = DemoApplication.getApplication();

        Intent intent = new Intent(application , LocalClientService.class);
        application.startService(intent);

    }
}
