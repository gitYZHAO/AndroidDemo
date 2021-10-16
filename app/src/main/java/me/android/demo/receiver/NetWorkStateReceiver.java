package me.android.demo.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.util.Log;
import android.widget.Toast;

public class NetWorkStateReceiver extends BroadcastReceiver {
    final static private String TAG = "NetWorkStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {

        if ("net.android.MY_BROADCAST".equals(intent.getAction())) {
            // Do some test ...
            // for (int i = 0; i < 1000; i++) {
            //     Log.d(TAG, "SLEEP...");
            //     try {
            //         Thread.sleep(10*1000);
            //     } catch (InterruptedException e) {
            //         e.printStackTrace();
            //     }
            // }

        }

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.LOLLIPOP) {

            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();
            //用于存放网络连接信息
            StringBuilder sb = new StringBuilder();
            //通过循环将网络信息逐个取出来
            for (int i=0; i < networks.length; i++){
                //获取ConnectivityManager对象对应的NetworkInfo对象
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
            }
            Toast.makeText(context, sb.toString(),Toast.LENGTH_SHORT).show();
        }
    }
}
