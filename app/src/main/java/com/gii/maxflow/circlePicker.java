package com.gii.maxflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by Timur on 19-Jun-16.
 */
public class circlePicker extends View {
    Paint defaultPaint = new Paint();
    public circlePicker(Context context) {
        super(context);
        defaultPaint.setColor(Color.RED);
        defaultPaint.setStyle(Paint.Style.FILL_AND_STROKE);
    }

    public circlePicker(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
    }

    public circlePicker(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawCircle(canvas.getWidth()/2,canvas.getHeight()/2,canvas.getHeight()/2,defaultPaint);
    }

}