package com.dj.hotpotopenandkownwhoinmytouch;

import androidx.appcompat.app.AppCompatActivity;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
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

        // 去系统页连接指定wifi
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
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

    /**
     * 将int类型的IP转换成String类型的IP
     * @param i 数字类型的IP
     * @return  String类型的IP
     */
    private String intToIp(int i) {
        return (i & 0xFF) + "." + ((i >> 8) & 0xFF) + "." + ((i >> 16) & 0xFF) + "." + ((i >> 24) & 0xFF);
    }


    private ArrayList<String> getConnectedIP3() {
        ArrayList<String> aList = new ArrayList<>();
        String IPADDRESS_PATTERN ="(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
        Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
        // 获取result
        ShellUtils.CommandResult result = ShellUtils.execCommand("ip neigh",false);
        Log.i(TAG,"result1.result = " + result.result);
        Log.i(TAG,"result1.successMsg = " + result.successMsg); // 真正有用的
        Log.i(TAG,"result1.errorMsg = " + result.errorMsg);
        String[] strList = result.successMsg.split(" "); // 以空格为分隔符
        for (int i = 0; i < strList.length; i++) {
            if("lladdr".equals(strList[i])) {
                Matcher matcher = pattern.matcher(strList[i-3]);
                if (matcher.find()) {
                    aList.add(matcher.group());
                }
            }
        }
        return aList;
    }


    /**
     * 每隔1s刷新一次
     */
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            try {
                // 首先向所处网段的所有设备发送一遍数据包
                sendDataToLoacl();

                // 展示arp文件中的数据
                ArrayList<String> ipList = getConnectedIP3();
                mHandler.postDelayed(this, TIME);
                mTextView.setText(ipList.toString()); // 给用户看到该局域网中设备的IP
                Log.i(TAG, ipList.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };


    /**
     * 向局域网内的其他IP广播
     */
    private void sendDataToLoacl() {
        // 局域网内存在的ip集合
        final List<String> ipList = new ArrayList<>();
        final Map<String, String> map = new HashMap<>();

        // 获取本机所在的局域网地址
        String hostIP = getHostIP();
        int lastIndexOf = hostIP.lastIndexOf(".");
        final String substring = hostIP.substring(0, lastIndexOf + 1);

        //创建线程池
        //        final ExecutorService fixedThreadPool = Executors.newFixedThreadPool(20);
        new Thread(new Runnable() {
            @Override
            public void run() {
                DatagramPacket dp = new DatagramPacket(new byte[0], 0, 0);
                DatagramSocket socket;
                try {
                    socket = new DatagramSocket();
                    int position = 2;
                    while (position < 255) {
                        dp.setAddress(InetAddress.getByName(substring + String.valueOf(position)));
                        socket.send(dp);
                        position++;
                        if (position == 125) { // 分两段掉包，一次性发的话，达到236左右，会耗时3秒左右再往下发
                            socket.close();
                            socket = new DatagramSocket();
                        }
                    }
                    Log.e("Scanner ", "run: udp:" + substring +"2 ~ "+substring +"254");
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    /**
     * 获取本机 ip地址
     *
     * @return
     */
    private String getHostIP() {
        String hostIp = null;
        try {
            Enumeration nis = NetworkInterface.getNetworkInterfaces();
            InetAddress ia;
            while (nis.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) nis.nextElement();
                Enumeration<InetAddress> ias = ni.getInetAddresses();
                while (ias.hasMoreElements()) {
                    ia = ias.nextElement();
                    if (ia instanceof Inet6Address) {
                        continue; // skip ipv6
                    }
                    String ip = ia.getHostAddress();
                    if (!"127.0.0.1".equals(ip)) {
                        hostIp = ia.getHostAddress();
                        Log.i(TAG, "hostIp="+hostIp);
                        break;
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
        }
        return hostIp;
    }
}