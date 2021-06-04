package com.haven.test.wifi;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class WifiListAdapter extends RecyclerView.Adapter<WifiListAdapter.ViewHolder> {

    private List<ScanResult> mWifiList = new ArrayList<>();
    private Context mContext;

    public WifiListAdapter(Context context) {
        mContext = context;
    }

    public void refreshData(List<ScanResult> wifiList) {
        if (wifiList == null || wifiList.size() == 0) {
            return;
        }
        mWifiList = wifiList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view =  LayoutInflater.from(mContext).inflate(R.layout.item_wifi_info, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        ScanResult scanResult = mWifiList.get(position);
        if (scanResult == null) {
            return;
        }

        holder.tvWifiSSID.setText(scanResult.SSID);
        holder.tvWifiBSSID.setText(scanResult.BSSID);
        // 信号强度为负数，数值越大信号越好
        holder.tvWifiLevel.setText("" + scanResult.level);
    }

    @Override
    public int getItemCount() {
        return mWifiList == null ? 0 : mWifiList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvWifiSSID;
        private TextView tvWifiBSSID;
        private TextView tvWifiLevel;

        public ViewHolder(View view) {
            super(view);
            tvWifiSSID = view.findViewById(R.id.tvWifiSSID);
            tvWifiBSSID = view.findViewById(R.id.tvWifiBSSID);
            tvWifiLevel = view.findViewById(R.id.tvWifiLevel);
        }
    }
}
