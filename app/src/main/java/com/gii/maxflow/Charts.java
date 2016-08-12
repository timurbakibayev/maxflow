package com.gii.maxflow;

import android.graphics.Canvas;
import android.graphics.PointF;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

/**
 * Created by Timur on 17-Nov-15.
 */
public class Charts {
    ScaleGestureDetector _scaleDetector;

    public enum ChartType {
        pie, bars
    }

    public ChartType chartType;

    private float scaleFactor;
    private PointF backgroundPosition = new PointF(0, 0);
    private PointF lastBackgroundPosition = new PointF(0,0);
    private PointF canvasMovingStartingPoint = new PointF(0,0);

    public boolean onTouchEventChartMode(@NonNull MotionEvent event, GII.AppState appState) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                canvasMovingStartingPoint = new PointF((int) event.getX(), (int) event.getY());
                lastBackgroundPosition = backgroundPosition;
                break;
            case MotionEvent.ACTION_MOVE:
                backgroundPosition = new PointF((int) (lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX())),
                        (int) (lastBackgroundPosition.y + (canvasMovingStartingPoint.y - event.getY())));
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                return false;
        }
        return true;
    }

    public boolean onScale(ScaleGestureDetector detector, GII.AppState appState) {
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 4));
        if (scaleFactor <= 0.1f)
            appState = GII.AppState.idle;
        return true;
    }
    protected void onDraw(Canvas canvas) {

    }

}