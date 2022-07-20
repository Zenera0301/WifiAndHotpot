package com.dj.hotpotopenandkownwhoconnectme;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    Handler mHandler;
    private static final int TIME = 1000;
    TextView mTextView;
    Button mButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mTextView = findViewById(R.id.textview);
        mButton = findViewById(R.id.button);

        // 每隔1秒执行一次
        mHandler = new Handler();//初始化mHandler
        mHandler.postDelayed(runnable, TIME); // 在初始化方法里

        // 去系统页激活热点
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //   第一个参为包名，第二个各个设置的类名(可以参考下面，包名不用改变)
                ComponentName cm = new ComponentName("com.android.settings",
                        "com.android.settings.TetherSettings");
                Intent intent = new Intent();
                intent.setComponent(cm);
                intent.setAction("android.intent.action.VIEW");
                startActivity(intent);
            }
        });
    }


    /**
     * 看看是谁连了我的热点，把他们的IP地址打印出来
     * 下面3个函数都是这个功能
     * 区别是：
     * 1和2只能在Android10以下版本使用
     * 3可以在任意版本使用
     * @return
     */
    private ArrayList<String> getConnectedIP1() {
        ArrayList<String> connectedIP = new ArrayList<String>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(
                    "/proc/net/arp"));
            String line;
            while ((line = br.readLine()) != null) {
                String[] splitted = line.split(" +");
                if (splitted != null && splitted.length >= 4) {
                    String ip = splitted[0];
                    connectedIP.add(ip);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connectedIP;
    }

    private ArrayList<String> getConnectedIP2(){
        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        DhcpInfo dhcpInfo = wifiManager.getDhcpInfo();
//        int ip = dhcpInfo.serverAddress;
//        int ipc = dhcpInfo.ipAddress;
        int ipc = wifiManager.getConnectionInfo().getIpAddress();
        //此处获取ip为整数类型，需要进行转换
        String strIp = intToIp(ipc);
        ArrayList<String> aLists = new ArrayList<>();
        aLists.add(strIp);
        return aLists;
    }

    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }


    private ArrayList<String> getConnectedIP3() {
        ArrayList<String> aList = new ArrayList<>();
        String IPADDRESS_PATTERN ="(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        // 获取 result
        ShellUtils.CommandResult result = ShellUtils.execCommand("ip neigh",false);
        Log.i(TAG,"result1.result = " + result.result);
        Log.i(TAG,"result1.successMsg = " + result.successMsg);
        Log.i(TAG,"result1.errorMsg = " + result.errorMsg);

        String[] strList = result.successMsg.split(" "); // 以空格为分隔符
        for (int i = 0; i < strList.length; i++) {
            Matcher matcher = pattern.matcher(strList[i]);
            if (matcher.find()) {
                aList.add(matcher.group());
            }
        }
        return aList;
    }



    private HashMap<String, String> getConnectedIP4() {
        HashMap<String, String> map = new HashMap<>();
        String IPADDRESS_PATTERN ="(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        // 获取 result
        ShellUtils.CommandResult result = ShellUtils.execCommand("ip neigh",false);
        Log.i(TAG,"result1.result = " + result.result);
        Log.i(TAG,"result1.successMsg = " + result.successMsg);
        Log.i(TAG,"result1.errorMsg = " + result.errorMsg);

        String SPLIT_PATTERN = "[A-Z]+|\\d+|-|\\[|\\]|[\\u4e00-\\u9fa5]+";
        Pattern p = Pattern.compile(SPLIT_PATTERN);
        String[] strList = result.successMsg.split(" "); // 以空格为分隔符
        String ip = "";
        int index = -1;
        for (int i = 0; i < strList.length; i++) {
            if(i == index + 5) {
                Matcher m = p.matcher(strList[i]);
                if (m.find()) {
                    map.put(ip, m.group());
                }
            }

            Matcher matcher = pattern.matcher(strList[i]);
            if (matcher.find()) {
                ip = matcher.group(); // 先记录ip
                index = i;
            }
        }
        return map;
    }

    /**
     * 每隔1s刷新一次
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                ArrayList<String> ipList = getConnectedIP3();
                HashMap<String, String> ipMap = getConnectedIP4();
                mHandler.postDelayed(this, TIME);
                //mTextView.setText(ipList.toString()); // 给用户看到，哪些IP在连手机
                mTextView.setText(ipMap.toString()); // 给用户看到，哪些IP在连手机
                Log.i(TAG, ipList.toString());
                Log.i(TAG, ipMap.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

}