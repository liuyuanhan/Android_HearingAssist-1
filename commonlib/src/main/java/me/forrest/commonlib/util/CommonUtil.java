package me.forrest.commonlib.util;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Vibrator;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.GregorianCalendar;
import java.util.Locale;

import me.forrest.commonlib.log.LogTool;

public class CommonUtil {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static Toast toast;

    // Toast文字不居中解决方案 二种解决方案
    // 1. 不要设置Activity theme的fitSystemWindows 为true
    // 2. toast创建时，上下文如果传如Activity，就会发生偏移，此时把上下文改为getApplicationContext()即可
    public static void showToast(Context context, String msg) {
        if (toast == null) {
            toast = Toast.makeText(context.getApplicationContext(), msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
            toast.setDuration(Toast.LENGTH_SHORT);
        }
        toast.show();
    }

    public static void showToastShort(final Activity activity, final String msg) {
        activity.runOnUiThread(() -> {
            if (toast == null) {
                toast = Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_SHORT);
            } else {
                toast.setText(msg);
                toast.setDuration(Toast.LENGTH_SHORT);
            }
            toast.show();
        });
    }

    public static void showToastShort(final Activity activity, final int resId) {
        if (activity == null) { return; }
        activity.runOnUiThread(() -> {
            if (toast == null) {
                toast = Toast.makeText(activity.getApplicationContext(), resId, Toast.LENGTH_SHORT);
            } else {
                toast.setText(activity.getString(resId));
                toast.setDuration(Toast.LENGTH_SHORT);
            }
            toast.show();
        });
    }

    public static void showToastLong(final Activity activity, String msg) {
        if (activity == null) { return; }
        activity.runOnUiThread(() -> {
            if (toast == null) {
                toast = Toast.makeText(activity.getApplicationContext(), msg, Toast.LENGTH_LONG);
            } else {
                toast.setText(msg);
                toast.setDuration(Toast.LENGTH_LONG);
            }
            toast.show();
        });
    }

    public static void showToastLong(final Activity activity, final int resId) {
        activity.runOnUiThread(() -> {
            if (toast == null) {
                toast = Toast.makeText(activity.getApplicationContext(), resId, Toast.LENGTH_LONG);
            } else {
                toast.setText(activity.getString(resId));
                toast.setDuration(Toast.LENGTH_LONG);
            }
            toast.show();
        });
    }

    public static void setFullScreen(final Window window) {
        int uiOptions = window.getDecorView().getSystemUiVisibility();
        uiOptions |= (View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN);
        window.getDecorView().setSystemUiVisibility(uiOptions);
        final int finalUiOptions = uiOptions;
        window.getDecorView().setOnSystemUiVisibilityChangeListener(visibility -> {
            if (visibility == View.VISIBLE) {
                window.getDecorView().setSystemUiVisibility(finalUiOptions);
            }
        });
    }

    /**
     * 获取当前本地apk的版本
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        try {
            //获取软件版本号，对应AndroidManifest.xml下android:versionCode
            versionCode = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionCode;
            LogTool.debug("versionCode: " + versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取版本号名称
     * @param context 上下文
     */
    public static String getVersionName(Context context) {
        String verName = "";
        try {
            verName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return verName;
    }

    public static String getUniqueID(Context context) {
        //在设备首次启动时，系统会随机生成一个64位的数字，并把这个数字以16进制字符串的形式保存下来。
        String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        return ANDROID_ID;
    }

    public static String toMD5(String text) {
        try {
            //获取摘要器 MessageDigest
            MessageDigest messageDigest = null;
            messageDigest = MessageDigest.getInstance("MD5");
            //通过摘要器对字符串的二进制字节数组进行hash计算
            byte[] digest = messageDigest.digest(text.getBytes());

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < digest.length; i++) {
                //循环每个字符 将计算结果转化为正整数;
                int digestInt = digest[i] & 0xff;
                //将10进制转化为较短的16进制
                String hexString = Integer.toHexString(digestInt);
                //转化结果如果是个位数会省略0,因此判断并补0
                if (hexString.length() < 2) {
                    sb.append(0);
                }
                //将循环结果添加到缓冲区
                sb.append(hexString);
            }
            //返回整个结果
            return sb.toString().toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取手机品牌
     */
    public static String getBrand() {
        return Build.BRAND;
    }

    /**
     * 获取SDK版本号
     */
    public static int getSDK() {
        return Build.VERSION.SDK_INT;
    }

    /**
     * 判断APP是否存在
     */
    public static boolean isApkExist(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return false;
        try {
            ApplicationInfo info = context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    public static boolean isWechatExist(Context context) {
        return isApkExist(context, "com.tencent.mm");
    }

    public static boolean isWeiboExist(Context context) {
        return isApkExist(context, "com.sina.weibo");
    }

    public static boolean isDouyinExist(Context context) {
        return isApkExist(context, "com.ss.android.ugc.aweme");
    }

    // android:name="android.permission.VIBRATE"


    public static boolean isFirstLaunchToday(Context context) {
        GregorianCalendar now = new GregorianCalendar();
        String date = new SimpleDateFormat("yyyyMMdd", Locale.US).format(now.getTime());
        Log.d("XXX", "date " + date);
        String lastLaunchDay = SPUtil.getInstance(context).getString("yyyyMMdd");
        boolean isFirstLaunchOfToday = !date.equals(lastLaunchDay);
        if (isFirstLaunchOfToday) {
            SPUtil.getInstance(context).putString("yyyyMMdd", date);
        }
        return isFirstLaunchOfToday;
    }

}
