package me.android.demo;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import me.android.demo.receiver.NetWorkStateReceiver;
import me.android.demo.util.NetworkUtils;

public class MainActivity extends AppCompatActivity {
    private Handler handler;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // HandlerThread thread1 = new HandlerThread("thread1");
        // HandlerThread thread2 = new HandlerThread("thread2");
        //
        // thread1.start();
        // thread2.start();
        //
        // Handler h1 = new Handler(thread1.getLooper());
        // Handler h2 = new Handler(thread2.getLooper());

        button = findViewById(R.id.THbutton);
        button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // testLooper();

                // testHandler();

                // NetworkUtils.iConnected(getApplicationContext());
                // NetworkUtils.isWifiConnected(getApplicationContext());

                // 发送广播
                // 方式一：
                // 在 SDK 26，为限制后台过多应用启动，接受广播等情况，将静态注册的广播接收器失效。
                // 在 发送隐式Intent 的时候，接收器只能通过动态注册广播接收器解决
                sendBroadcast(new Intent("net.android.MY_BROADCAST"));

                // 方式二：
                //参数1-包名 参数2-广播接收者所在的路径名
                // ComponentName componentName = new ComponentName(getApplicationContext(),
                //         "me.android.demo.receiver.NetWorkStateReceiver");
                // Intent intent = new Intent();
                // intent.setComponent(componentName);
            }
        });

        HandlerThread testhandler = new HandlerThread("testhandler");
        testhandler.start();
        handler = new MyHandler(testhandler.getLooper());

        //动态注册
        register();

    }

    @Override
    protected void onPause() {
        unregister();
        super.onPause();
    }

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            System.out.println("Get Message:" + msg.what);
        }
    }

    private void testHandler() {
        System.out.println("start send msg...");
        handler.sendEmptyMessage(1);
        handler.sendEmptyMessage(10);
        handler.sendEmptyMessage(100);
        System.out.println("end send msg...");
    }

    public void testLooper() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread1, LOOPER:" + Looper.myLooper());
            }
        }, "Thread1").start();

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                System.out.println("Thread2, LOOPER:" + Looper.myLooper());
            }
        }, "Thread2").start();

    }

    NetWorkStateReceiver netWorkStateReceiver;

    private void register() {
        if (netWorkStateReceiver == null) {
            netWorkStateReceiver = new NetWorkStateReceiver();
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        filter.addAction("net.android.MY_BROADCAST");
        Intent intent = registerReceiver(netWorkStateReceiver, filter);
        if (intent == null) {
            Log.e("register", "register fail");
        }
    }

    private void unregister() {
        unregisterReceiver(netWorkStateReceiver);
    }

}
