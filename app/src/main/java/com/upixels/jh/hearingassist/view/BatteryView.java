package com.upixels.jh.hearingassist.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;


import com.upixels.jh.hearingassist.R;

import androidx.annotation.Nullable;
import me.forrest.commonlib.util.DensityUtil;

public class BatteryView extends View {

    private Bitmap bitmapBatNormal;
    private Bitmap bitmapBatDisconnected;
    private Bitmap bitmap;
    private Rect rect;
    private Paint paint;
    private Rect rectBat;
    private int W;
    private int dw;
    private int space;
    private float bat;

    public BatteryView(Context context) {
        super(context);
        init();
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public BatteryView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

//    @Override
//    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
////        setMeasuredDimension();
//    }

    private void init() {
        bitmapBatNormal = BitmapFactory.decodeResource(getResources(), R.drawable.bg_battery_normal);
        bitmapBatDisconnected = BitmapFactory.decodeResource(getResources(), R.drawable.bg_barrery_dis);
        bitmap = bitmapBatNormal;

        rect = new Rect();
        rectBat = new Rect();

        paint = new Paint();
        paint.setColor(0xFF16DC8F);
//        paint.setStrokeWidth(30f); // 描边宽度
        paint.setAntiAlias(true);
//        Paint.Style.FILL：填充内部
//        Paint.Style.FILL_AND_STROKE  ：填充内部和描边
//        Paint.Style.STROKE  ：描边
        paint.setStyle(Paint.Style.FILL);

//        if volta >= 0.3 {
//            vVolta.backgroundColor = UIColor(hex6: 0x16DC8F)
//        } else if volta < 0.3, volta >= 0.1 {
//            vVolta.backgroundColor = UIColor(hex6: 0xf06d06)
//        } else {
//            vVolta.backgroundColor = UIColor.JH_RedColor()
//        }

        // 电量左上角与View的间距
        space = DensityUtil.dip2px(getContext(), 3); // 3dp ==> 9px
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (W == 0 ) { W = getWidth() - 3 * space; }
        dw = (int)(bat * W);
        // 绘制背景
        rect.set(0, 0, getWidth(), getHeight()); // 99 54
        canvas.drawBitmap(bitmap, null, rect, null);

        // 绘制电量
        rectBat.set(space, space, space + dw, getHeight() - space);
        canvas.drawRect(rectBat, paint);
    }

    // bat = [0.0, 1.0]
    public void setBattery(float bat) {
        this.bat = bat;
        if (bat >= 0.3f) {
            paint.setColor(0xFF16DC8F);
            bitmap = bitmapBatNormal;
        } else if (bat < 0.3f && bat >= 0.1f) {
            paint.setColor(0xFFF06D06);
            bitmap = bitmapBatNormal;
        } else if (bat < 0.1f && bat > 0.00001f) {
            paint.setColor(0xFFE22732);
            bitmap = bitmapBatNormal;
        } else { // == 0.0f
            bitmap = bitmapBatDisconnected;
        }
        postInvalidate();
    }
}
