package com.example.ezhaome.getwifiinfo;

import android.os.Bundle;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.widget.TextView;

public class MainActivity extends Activity {
    /** Called when the activity is first created. */
    private static final int REQUEST_ENABLE_BT = 3;
    private WifiManager mWifi;
    private BluetoothAdapter bAdapt;
    private String btMac;
    private String WifiMac;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mWifi = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!mWifi.isWifiEnabled()) {
            mWifi.setWifiEnabled(true);
        }

        WifiInfo wifiInfo = mWifi.getConnectionInfo();

        if ((WifiMac = wifiInfo.getMacAddress()) == null) {
            WifiMac = "No Wifi Device";
        }

        TextView mac = (TextView) findViewById(R.id.macView);
        mac.setTextSize(16);

//      查看已经连接上的WIFI信息，在Android的SDK中为我们提供了一个叫做WifiInfo的对象，这个对象可以通过WifiManager.getConnectionInfo()来获取。WifiInfo中包含了当前连接中的相关信息。
//      getBSSID()  获取BSSID属性
//      getDetailedStateOf()  获取客户端的连通性
//      getHiddenSSID()  获取SSID 是否被隐藏
//      getIpAddress()  获取IP 地址
//      getLinkSpeed()  获取连接的速度
//      getMacAddress()  获取Mac 地址
//      getRssi()  获取802.11n 网络的信号
//      getSSID()  获取SSID
//      getSupplicanState()  获取具体客户端状态的信息
        StringBuffer sb = new StringBuffer();
        sb.append("\n获取BSSID属性（所连接的WIFI设备的MAC地址）：" + wifiInfo.getBSSID());
//      sb.append("getDetailedStateOf()  获取客户端的连通性：");
        sb.append("\n\n获取SSID 是否被隐藏："+ wifiInfo.getHiddenSSID());
        sb.append("\n\n获取IP 地址：" + wifiInfo.getIpAddress());
        sb.append("\n\n获取连接的速度：" + wifiInfo.getLinkSpeed());
        sb.append("\n\n获取Mac 地址（手机本身网卡的MAC地址）：" + WifiMac);
        sb.append("\n\n获取802.11n 网络的信号：" + wifiInfo.getRssi());
        sb.append("\n\n获取SSID（所连接的WIFI的网络名称）：" + wifiInfo.getSSID());
        sb.append("\n\n获取具体客户端状态的信息：" + wifiInfo.getSupplicantState());
        mac.setText("WIFI网络信息:  " + sb.toString());
    }
}

