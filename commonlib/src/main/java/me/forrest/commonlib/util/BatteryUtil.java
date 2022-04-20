package me.forrest.commonlib.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.util.Log;

public class BatteryUtil {
    public interface BatteryListener {
        //电量的百分比
        void batterInfo(int percent, boolean isCharging);
    }

    private BatteryListener listener;
    private boolean mRegister;

    /* 创建BroadcastReceiver */
    private BroadcastReceiver mBatInfoReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
                int level = intent.getIntExtra("level", 0);
                int scale = intent.getIntExtra("scale", 100);
                int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
                boolean isCharging = (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL);
                int percent = level * 100 / scale;
                if (listener != null) listener.batterInfo(percent, isCharging);
            }
        }
    };

    public void registerReceiver(Context context) {
        if (!mRegister) {
            mRegister = true;
            context.registerReceiver(mBatInfoReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        }
    }

    public void unregisterReceiver(Context context) {
        if (mRegister) {
            context.unregisterReceiver(mBatInfoReceiver);
            mRegister = false;
        }
    }

    public void setBatteryListener(BatteryListener l) {
        this.listener = l;
    }

    //相当于注册一个空的BroadcastReceiver,可以调用多次，在activity onDestroy时不需要进行unRegister操作
//    Intent batteryInfoIntent = getApplicationContext()
//            .registerReceiver( null ,
//                    new IntentFilter( Intent.ACTION_BATTERY_CHANGED ) ) ;
//
//    int level = batteryInfoIntent.getIntExtra( "level" , 0 );//电量（0-100）
//    int status = batteryInfoIntent.getIntExtra( "status" , 0 );
//    int health = batteryInfoIntent.getIntExtra( "health" , 1 );
//    boolean present = batteryInfoIntent.getBooleanExtra( "present" , false );
//    int scale = batteryInfoIntent.getIntExtra( "scale" , 0 );
//    int plugged = batteryInfoIntent.getIntExtra( "plugged" , 0 );//
//    int voltage = batteryInfoIntent.getIntExtra( "voltage" , 0 );//电压
//    int temperature = batteryInfoIntent.getIntExtra( "temperature" , 0 ); // 温度的单位是10℃
//    String technology = batteryInfoIntent.getStringExtra( "technology" );
}
