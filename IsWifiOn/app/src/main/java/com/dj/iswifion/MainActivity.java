package com.dj.iswifion;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    Context context;
    Thread myThread = new Thread(new Runnable() {
        @Override
        public void run() {
            while(true) {
                try {
                    Thread.sleep(1000);
                    String result = getIpAddressForBroadcast(context);
                    if(!"".equals(result)) {
                        Log.i("djtest", "wifi on " + result);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText("wifi on " + result);
                            }
                        });

                    } else {
                        Log.i("djtest", "wifi off");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                textView.setText("wifi off");
                            }
                        });
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    break;
                }
            }
        }
    });
    private TextView textView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = findViewById(R.id.textView);
        context = getApplicationContext();
        myThread.start();
    }

    @Override
    protected void onStop() {
        super.onStop();
        myThread.interrupt();
    }

    /***
     * 获得广播的地址 xxx.xxx.xxx.255
     * 手机连接路由器wifi，获得手机IP，组装出广播地址
     * @param context 上下文
     * @return   192.168.31.255  或者 空
     */
    public String getIpAddressForBroadcast(Context context) {
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        int ipAddress = wifiInfo == null ? 0 : wifiInfo.getIpAddress();
        if (mWifiManager.isWifiEnabled() || ipAddress != 0) {
            @SuppressLint("DefaultLocale") String result = String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff), (ipAddress >> 16 & 0xff), (0xff));
            if("0.0.0.255".equals(result)){
                return "";
            }
            return result;
        } else {
            return "";
        }
    }

}