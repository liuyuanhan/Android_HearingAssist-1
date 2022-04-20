package me.forrest.commonlib.util;

//注意添加权限
//<uses-permission android:name="android.permission.ACCESS_WIFI_STATE"/>

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;

public class NetUtil {
    private static String TAG = "NetUtil";

    public interface RssiChangeListener {
        void rssiInfo(int rssi);
    }

    private RssiChangeListener listener;
    private boolean mRegister;

    // 获取网关ip地址
    public static String getServerAddress(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm == null) { return null;}
        DhcpInfo info = wm.getDhcpInfo();
        int ip = info.serverAddress;
        return iton(ip);
    }

    public static String getSSID(Context context) {
//        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
//        if (wm == null) { return null;}
//        WifiInfo wifiInfo = wm.getConnectionInfo();
//        return wifiInfo.getSSID();

//        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
//        if (cm == null) return null;
//        NetworkInfo info = cm.getActiveNetworkInfo();
//        Log.d("test", "ssid : " + info.getExtraInfo());
//        if (info.isConnected()) return info.getExtraInfo();
//        return null;

        String ssid="unknown id";

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O || Build.VERSION.SDK_INT == Build.VERSION_CODES.P) {
            WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            assert mWifiManager != null;
            @SuppressLint("MissingPermission") WifiInfo info = mWifiManager.getConnectionInfo();
            ssid = info.getSSID().replace("\"", "");

        } else if (Build.VERSION.SDK_INT== Build.VERSION_CODES.O_MR1){
            ConnectivityManager connManager = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
            assert connManager != null;
            NetworkInfo networkInfo = connManager.getActiveNetworkInfo();
            if (networkInfo.isConnected()) {
                if (networkInfo.getExtraInfo()!=null){
                    ssid = networkInfo.getExtraInfo().replace("\"","");
                }
            }
        }
        Log.d(TAG, "ssid : " + ssid);
        return ssid;
    }

    public static String getSSIDByNetWorkId(Context context) {
        String ssid = "";
        WifiManager mWifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        assert mWifiManager != null;
        @SuppressLint("MissingPermission") WifiInfo info = mWifiManager.getConnectionInfo();
        int networkId = info.getNetworkId();
        @SuppressLint("MissingPermission") List<WifiConfiguration> list = mWifiManager.getConfiguredNetworks(); // 获取所有连接过的wifi
        for (WifiConfiguration configuration : list) {
            if (configuration.networkId == networkId) {
                ssid = configuration.SSID;
                break;
            }
        }
        Log.d(TAG, "ssid = " + ssid);
        return ssid;
    }

    //获取wifi信号强度
    public static int getRSSI(Context context) {
        WifiManager wm = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wm == null) { return -200;}
        WifiInfo wifiInfo = wm.getConnectionInfo();
        return wifiInfo.getRssi();
    }

    //wifi信号强度检测回调
    private BroadcastReceiver mRssiInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (WifiManager.RSSI_CHANGED_ACTION.equals(action)) {
                if (listener != null) listener.rssiInfo(getRSSI(context));
            }
        }
    };

    //设置wifi信号强度变化接口
    public void setRssiChangeListener(RssiChangeListener l) {
        this.listener = l;
    }

    //注册wifi信号强度检测 <测试过程中，只回调了一次，不能连续回调>
    public void registerReceiver(Context context) {
        if (!mRegister) {
            mRegister = true;
            context.registerReceiver(mRssiInfoReceiver, new IntentFilter(WifiManager.RSSI_CHANGED_ACTION));
        }
    }

    public void unregisterReceiver(Context context) {
        if (mRegister) {
            mRegister = false;
            context.unregisterReceiver(mRssiInfoReceiver);
        }
    }

    //检测外网是否可通
    public static boolean isNetworkOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("ping -c 3 114.114.114.114");
            int exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    //通过socket检查外网的连通性
    public static boolean hasNetworkConnection(Context context){
        Socket s;
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        @SuppressLint("MissingPermission") NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean connected = (null != activeNetworkInfo) && activeNetworkInfo.isConnected();
        if(!connected) return false;
        boolean routeExists ;
        try {
            s = new Socket();
            InetAddress host = InetAddress.getByName("114.114.114.114");//国内使用114.114.114.114，如果全球通用google：8.8.8.8
            s.connect(new InetSocketAddress(host,80),2000);//google:53
            routeExists = true;
            s.close();
        } catch (IOException e) {
            routeExists = false ;
        }
        return routeExists ;
    }

    //Jellyfish判断网络是否联通外网
    public static boolean isOnline(Context context) {
        String ssid = getSSID(context);
        return ssid != null && !ssid.contains("Jellyfish");
    }

    private static String iton(int ip){
        StringBuilder sb = new StringBuilder();
        sb.append(String.valueOf((ip&0xff)));
        sb.append('.');
        sb.append(String.valueOf((ip>>8)&0xff));
        sb.append('.');
        sb.append(String.valueOf((ip>>16)&0xff));
        sb.append('.');
        sb.append(String.valueOf((ip>>24)&0xff));
        return sb.toString();
    }
}
