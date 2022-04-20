package me.forrest.commonlib.util;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

/*
 *   Spanned.SPAN_EXCLUSIVE_EXCLUSIVE(前后都不包括)、
 *   Spanned.SPAN_INCLUSIVE_EXCLUSIVE(前面包括，后面不包括)、
 *   Spanned.SPAN_EXCLUSIVE_INCLUSIVE(前面不包括，后面包括)、
 *   Spanned.SPAN_INCLUSIVE_INCLUSIVE(前后都包括)。
 */

/**
 * 富文本 工具类
 */
public class SpannableStringUtil {
    /**
     * 设置String 显示字体的类型
     * @param style 字体类型 Typeface.NORMAL / Typeface.BOLD
     * @param content 内容
     */
    public static SpannableString setTypeface(int style, String content) {
        SpannableString spanString = new SpannableString(content);
        StyleSpan styleSpan = new StyleSpan(style);
        spanString.setSpan(styleSpan, 0, spanString.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanString;
    }

    /**
     * 设置String 显示字体颜色
     * @param color 颜色值
     * @param content 内容
     */
    public static SpannableString setTextColor(int color, int start, int end, String content) {
        SpannableString spanString = new SpannableString(content);
        ForegroundColorSpan span = new ForegroundColorSpan(color);
        spanString.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanString;
    }

    // 1. SpannableString spanString = new SpannableString(content);
    // 2. 设置字体颜色
    public static SpannableString setTextColor(SpannableString spanString, int color, int start, int end) {
        ForegroundColorSpan span = new ForegroundColorSpan(color);
        spanString.setSpan(span, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanString;
    }

    public static SpannableString setTextClick(SpannableString spanString, ClickableSpan clickableSpan, int start, int end) {
        spanString.setSpan(clickableSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spanString;
    }
}
