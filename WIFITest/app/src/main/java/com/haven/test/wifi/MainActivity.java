package com.haven.test.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView rvWifiList;

    private WifiListAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
         * 注意：代码里未做动态权限检测，需手动打开 "定位权限"
         */
        setContentView(R.layout.activity_main);

        mAdapter = new WifiListAdapter(this);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);

        rvWifiList = findViewById(R.id.rvWifiList);
        rvWifiList.setLayoutManager(manager);
        rvWifiList.setAdapter(mAdapter);
        mAdapter.refreshData(getWifiList());
        mHandler.sendEmptyMessageDelayed(1, 3000);
    }

    private Handler mHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message message) {
            switch (message.what) {
                case 1:
                    mAdapter.refreshData(getWifiList());
                    mHandler.sendEmptyMessageDelayed(1, 3000);
                    break;
            }
            return true;
        }
    });

    private List<ScanResult> getWifiList() {
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        List<ScanResult> scanResults = wm.getScanResults();
        if (scanResults == null || scanResults.size() == 0)
            scanResults = new ArrayList<>();
        return  wm.getScanResults();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }
}
