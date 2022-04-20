package me.forrest.commonlib.util;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;
import android.util.DisplayMetrics;
import android.util.Log;

import java.util.Locale;

/**
 * 多语言切换的帮助类
 */
public class MultiLanguageUtil {

    private static final String TAG = "MultiLanguageUtil";
    private static MultiLanguageUtil instance;
    private Context context;
    public static final String SAVE_LANGUAGE = "save_language";

    public static void init(Context mContext) {
        if (instance == null) {
            synchronized (MultiLanguageUtil.class) {
                if (instance == null) {
                    instance = new MultiLanguageUtil(mContext);
                }
            }
        }
    }

    public static MultiLanguageUtil getInstance() {
        if (instance == null) {
            throw new IllegalStateException("You must be init MultiLanguageUtil first");
        }
        return instance;
    }

    private MultiLanguageUtil(Context context) {
        this.context = context;
    }

    /**
     * 设置语言
     */
    public void setConfiguration() {
        Locale targetLocale = getLanguageLocale();
        Log.d(TAG, "targetLocale =" + targetLocale);
        Configuration configuration = context.getResources().getConfiguration();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            configuration.setLocale(targetLocale);
        } else {
            configuration.locale = targetLocale;
        }
        Resources resources = context.getResources();
        DisplayMetrics dm = resources.getDisplayMetrics();
        resources.updateConfiguration(configuration, dm);   //语言更换生效的代码!
    }

    /**
     * 如果不是英文、简体中文、繁体中文，默认返回简体中文
     */
    public Locale getLanguageLocale() {
        int languageType = SPUtil.getInstance(context).getInt(MultiLanguageUtil.SAVE_LANGUAGE, 0);
        Locale locale = Locale.ENGLISH;
        if (languageType == LanguageType.LANGUAGE_FOLLOW_SYSTEM) {
            locale = getSysLocale();
        } else if (languageType == LanguageType.LANGUAGE_EN) {
            locale = Locale.ENGLISH;
        } else if (languageType == LanguageType.LANGUAGE_CHINESE_SIMPLIFIED) {
            locale = Locale.SIMPLIFIED_CHINESE;
        } else if (languageType == LanguageType.LANGUAGE_CHINESE_TRADITIONAL) {
            locale = Locale.SIMPLIFIED_CHINESE; //Locale.TRADITIONAL_CHINESE;
        }
//        Log.d(TAG, "locale =" + locale);
        return locale;
    }

    private String getSystemLanguage(Locale locale) {
        return locale.getLanguage() + "_" + locale.getCountry();
    }

    //以上获取方式需要特殊处理一下
    public Locale getSysLocale() {
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = LocaleList.getDefault().get(0);
        } else {
            locale = Locale.getDefault();
        }
        // 同一简体和繁体中文
        if (locale.getLanguage().equals(Locale.SIMPLIFIED_CHINESE.getLanguage())) {
            locale = Locale.SIMPLIFIED_CHINESE;
        }
        return locale;
    }

    /**
     * 更新语言
     * @param languageType
     */
    public void updateLanguage(int languageType) {
        Log.d(TAG, "updateLanguage languageType = " + languageType);
        SPUtil.getInstance(context).putInt(MultiLanguageUtil.SAVE_LANGUAGE, languageType);
        setConfiguration();
    }

    public String getLanguageName(Context context) {
//        int languageType = CommSharedUtil.getInstance(context).getInt(MultiLanguageUtil.SAVE_LANGUAGE, LanguageType.LANGUAGE_FOLLOW_SYSTEM);
//        if (languageType == LanguageType.LANGUAGE_EN) {
//            return mContext.getString(R.string.setting_language_english);
//        } else if (languageType == LanguageType.LANGUAGE_CHINESE_SIMPLIFIED) {
//            return mContext.getString(R.string.setting_simplified_chinese);
//        } else if (languageType == LanguageType.LANGUAGE_CHINESE_TRADITIONAL) {
//            return mContext.getString(R.string.setting_traditional_chinese);
//        }
//        return mContext.getString(R.string.setting_language_auto);
        return "";
    }

    /**
     * 获取到用户保存的语言类型
     * @return
     */
    public int getLanguageType() {
        int languageType = SPUtil.getInstance(context).getInt(MultiLanguageUtil.SAVE_LANGUAGE, LanguageType.LANGUAGE_FOLLOW_SYSTEM);
         if (languageType == LanguageType.LANGUAGE_CHINESE_SIMPLIFIED) {
            return LanguageType.LANGUAGE_CHINESE_SIMPLIFIED;
        } else if (languageType == LanguageType.LANGUAGE_CHINESE_TRADITIONAL) {
            return LanguageType.LANGUAGE_CHINESE_TRADITIONAL;
        } else if (languageType == LanguageType.LANGUAGE_FOLLOW_SYSTEM) {
           return LanguageType.LANGUAGE_FOLLOW_SYSTEM;
        }
        return languageType;
    }

    public static Context attachBaseContext(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return createConfigurationResources(context);
        } else {
            MultiLanguageUtil.getInstance().setConfiguration();
            return context;
        }
    }

    @TargetApi(Build.VERSION_CODES.N)
    private static Context createConfigurationResources(Context context) {
        Resources resources = context.getResources();
        Configuration configuration = resources.getConfiguration();
        Locale locale = getInstance().getLanguageLocale();
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    public boolean needUpdateLocale(Context context, int type) {
        int languageType = SPUtil.getInstance(context).getInt(MultiLanguageUtil.SAVE_LANGUAGE, LanguageType.LANGUAGE_FOLLOW_SYSTEM);
        if (languageType == LanguageType.LANGUAGE_FOLLOW_SYSTEM) {
            return true;
        } else {
            return languageType != type;
        }
    }

//    protected void attachBaseContext(Context newBase) {
//
//        Context context = attachBaseContext(newBase);
//        final Configuration configuration = context.getResources().getConfiguration();
//        // 此处的ContextThemeWrapper是androidx.appcompat.view包下的
//        // 你也可以使用android.view.ContextThemeWrapper，但是使用该对象最低只兼容到API 17
//        // 所以使用 androidx.appcompat.view.ContextThemeWrapper省心
//        final ContextThemeWrapper wrappedContext = new ContextThemeWrapper(context,
//                R.style.Theme_AppCompat_Empty) {
//            @Override
//            public void applyOverrideConfiguration(Configuration overrideConfiguration) {
//                if (overrideConfiguration != null) {
//                    overrideConfiguration.setTo(configuration);
//                }
//                super.applyOverrideConfiguration(overrideConfiguration);
//            }
//        };
//        super.attachBaseContext(wrappedContext);
//    }

}
