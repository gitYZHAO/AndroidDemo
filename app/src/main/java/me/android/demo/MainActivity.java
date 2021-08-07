package me.android.demo;

import android.app.ActivityManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

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

                testHandler();
            }
        });

        HandlerThread testhandler = new HandlerThread("testhandler");
        testhandler.start();
        handler = new MyHandler(testhandler.getLooper());

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
                System.out.println("Thread1, LOOPER:"+ Looper.myLooper());
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
                System.out.println("Thread2, LOOPER:"+ Looper.myLooper());
            }
        }, "Thread2").start();

    }


}
