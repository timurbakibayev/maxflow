package com.gii.maxflow;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Created by onion on 29.12.15.
 */

/*This version works with the field Date, so it may come into use in the next version of the programm


public class TimeLine {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Canvas ourCanvas;
    Date leftDate;
    Date rightDate;
    Date minDate;
    Date maxDate;
    int barWidthBy2=20;
    int barHeightBy2=10;
    Rect leftRect;
    Rect rightRect;
    boolean leftRectPressed = false;
    boolean rightRectPressed = false;

    private PointF backgroundPosition = new PointF(0, 0);
    private PointF lastBackgroundPosition = new PointF(0,0);
    private PointF canvasMovingStartingPoint = new PointF(0,0);


    public TimeLine(Canvas canvas){//,ArrayList<Operation> displayedOperation){
        super();
        ourCanvas=canvas;
        leftRect =  new Rect(20-barWidthBy2,20-barHeightBy2,20+barWidthBy2,20+barHeightBy2);
        rightRect = new Rect(ourCanvas.getWidth()-20-barWidthBy2,20-barHeightBy2,
                             ourCanvas.getWidth()-20+barWidthBy2,20+barHeightBy2);
    };

    public void toPlotTimeLine(Canvas canvas, ArrayList<Operation> displayedOperation) {
        ourCanvas = canvas;
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        ourCanvas.drawLine(20, 20, ourCanvas.getWidth() - 20, 20, paint);

        ourCanvas.drawRect(leftRect, paint);
        ourCanvas.drawRect(rightRect, paint);
        minDate=displayedOperation.get(0).date;
        maxDate=displayedOperation.get(0).date;

        for(Operation acc:displayedOperation) {
            if (maxDate.before(acc.date))
                maxDate = acc.date;
            if (minDate.after(acc.date))
                minDate = acc.date;
        }

        SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM", Locale.getDefault());
        SimpleDateFormat yearFormatter = new SimpleDateFormat("yy", Locale.getDefault());
        SimpleDateFormat dayFormatter = new SimpleDateFormat("dd", Locale.getDefault());

        paint.setColor(Color.BLACK);
        paint.setTextSize(25);
        String minDateString=   dayFormatter.format(minDate) + "/" +
                                monthFormatter.format(minDate) + "/" +
                                yearFormatter.format(minDate);

        String maxDateString=   dayFormatter.format(maxDate) + "/" +
                                monthFormatter.format(maxDate) + "/" +
                                yearFormatter.format(maxDate);
        ourCanvas.drawText(minDateString,
                            20-barWidthBy2,20+barHeightBy2*2,paint);
        ourCanvas.drawText(maxDateString,
                            ourCanvas.getWidth()-20-barWidthBy2*3,20+barHeightBy2*2,paint);

        leftDate=new Date(minDate.getTime());
        rightDate=new Date(maxDate.getTime());
        float tm;

        tm = (float)minDate.getTime();
        tm = tm + (float)(maxDate.getTime()-minDate.getTime()) * (float)(leftRect.centerX()-20)/(float)(ourCanvas.getWidth()-40);
        leftDate.setTime((long)tm);

        tm = (float)minDate.getTime();
        tm = tm + (float)(maxDate.getTime()-minDate.getTime()) * (float)(rightRect.centerX()-20)/(float)(ourCanvas.getWidth()-40);
        rightDate.setTime((long)tm);

        String leftDateString=  dayFormatter.format(leftDate) + "/" +
                                monthFormatter.format(leftDate) + "/" +
                                yearFormatter.format(leftDate);

        String rightDateString= dayFormatter.format(rightDate) + "/" +
                                monthFormatter.format(rightDate) + "/" +
                                yearFormatter.format(rightDate);
        paint.setTextSize(30);
        ourCanvas.drawText("from: " + leftDateString, 20 - barWidthBy2, 20 + barHeightBy2 * 4, paint); //leftDate.toString()
        ourCanvas.drawText("to:   "+ rightDateString, 20-barWidthBy2,20+barHeightBy2*7,paint);


    };


    public boolean onTouchEventTimeLine(@NonNull MotionEvent event) {
        //Log.d("message","TimeLine event holder");
        canvasMovingStartingPoint = new PointF((int) event.getX(), (int) event.getY());
        lastBackgroundPosition = backgroundPosition;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if( leftRect.contains((int)event.getX(),(int)event.getY())){
                    leftRectPressed=true;
                    Log.d("message","left rectangle clicked, click has coordinates ("+(int)event.getX()+
                                            ","+(int)event.getY()+")");
                }

                if( rightRect.contains((int)event.getX(),(int)event.getY())){
                    rightRectPressed=true;
                    Log.d("message","right rectangle clicked, click has coordinates ("+(int)event.getX()+
                            ","+(int)event.getY()+")");
                }
                break;
            case MotionEvent.ACTION_MOVE:
                backgroundPosition = new PointF((int) (lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX())),
                        (int) (lastBackgroundPosition.y + (canvasMovingStartingPoint.y - event.getY())));
                if (leftRectPressed) {
                    //leftRect.left = Math.max(Math.min((int)event.getX(),ourCanvas.getWidth()-20-barWidthBy2),20-barWidthBy2);
                    leftRect.right = Math.max(Math.min((int) event.getX()+barWidthBy2, ourCanvas.getWidth() - 20 + barWidthBy2), 20 + barWidthBy2);
                    leftRect.right = Math.min(rightRect.left, leftRect.right);
                    leftRect.left = leftRect.right-barWidthBy2*2;
                        //leftRect.left + (int)(lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX()));
                }
                if (rightRectPressed) {
                    rightRect.left = Math.max(Math.min((int)event.getX()-barWidthBy2,ourCanvas.getWidth()-20-barWidthBy2),20-barWidthBy2);
                    //rightRect.right = Math.max(Math.min((int) event.getX(), ourCanvas.getWidth() - 20 + barWidthBy2), 20 + barWidthBy2);
                    rightRect.left = Math.max(rightRect.left, leftRect.right);
                    rightRect.right = rightRect.left+barWidthBy2*2;
                    //leftRect.left + (int)(lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX()));
                }


                break;
            case MotionEvent.ACTION_UP:
                leftRectPressed=false;
                rightRectPressed=false;
                break;
            default:
                return false;
        }
        return true;
    }



}
*/


public class TimeLine {
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Canvas ourCanvas;
    int leftDate=0;
    int rightDate=100500;
    int minDate;
    int maxDate;
    int barWidthBy2=10;
    int barHeightBy2=20;
    Rect leftRect;
    Rect rightRect;
    boolean leftRectPressed = false;
    boolean rightRectPressed = false;

    private PointF backgroundPosition = new PointF(0, 0);
    private PointF lastBackgroundPosition = new PointF(0,0);
    private PointF canvasMovingStartingPoint = new PointF(0,0);


    public TimeLine(Canvas canvas){//,ArrayList<Operation> displayedOperation){
        super();
        ourCanvas=canvas;
        leftRect =  new Rect(20-barWidthBy2,20-barHeightBy2,20+barWidthBy2,20+barHeightBy2);
        rightRect = new Rect(ourCanvas.getWidth()-20-barWidthBy2,20-barHeightBy2,
                ourCanvas.getWidth()-20+barWidthBy2,20+barHeightBy2);
    };

    public void toPlotTimeLine(Canvas canvas, ArrayList<Operation> displayedOperation) {
        ourCanvas = canvas;
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        ourCanvas.drawLine(20, 20, ourCanvas.getWidth() - 20, 20, paint);

        ourCanvas.drawRect(leftRect, paint);
        ourCanvas.drawRect(rightRect, paint);
        minDate=1;//displayedOperation.get(0).pageNo;
        maxDate=1;//displayedOperation.get(0).pageNo;

        for(Operation acc:displayedOperation) {
            if (maxDate<=(acc.pageNo))
                maxDate = acc.pageNo;
            if (minDate>=(acc.pageNo))
                minDate = acc.pageNo;
        }

        SimpleDateFormat monthFormatter = new SimpleDateFormat("MMM", Locale.getDefault());
        SimpleDateFormat yearFormatter = new SimpleDateFormat("yy", Locale.getDefault());
        SimpleDateFormat dayFormatter = new SimpleDateFormat("dd", Locale.getDefault());

        paint.setColor(Color.BLACK);
        paint.setTextSize(25);
        ourCanvas.drawText(""+minDate, 20 - barWidthBy2, 20 + barHeightBy2, paint);
        ourCanvas.drawText(""+maxDate, ourCanvas.getWidth()-20-barWidthBy2*3,20+barHeightBy2,paint);

        leftDate=minDate;
        rightDate=maxDate;

        leftDate = minDate + (int)((float)(maxDate-minDate) * (float)(leftRect.centerX()-20-barWidthBy2)/(float)(ourCanvas.getWidth()-40));

        rightDate = minDate + (int)((float)(maxDate-minDate) * (float)(rightRect.centerX()-20+barWidthBy2)/(float)(ourCanvas.getWidth()-40));
        paint.setTextSize(30);
        ourCanvas.drawText("from: " + leftDate, 20 - barWidthBy2, 20 + barHeightBy2 * 2, paint); //leftDate.toString()
        ourCanvas.drawText("to:   "+ rightDate, 20-barWidthBy2,20+barHeightBy2*4,paint);


    };


    public boolean onTouchEventTimeLine(@NonNull MotionEvent event) {
        //Log.d("message","TimeLine event holder");
        canvasMovingStartingPoint = new PointF((int) event.getX(), (int) event.getY());
        lastBackgroundPosition = backgroundPosition;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if( leftRect.contains((int)event.getX(),(int)event.getY())){
                    leftRectPressed=true;
                    Log.d("message","left rectangle clicked, click has coordinates ("+(int)event.getX()+
                            ","+(int)event.getY()+")");
                }

                if( rightRect.contains((int)event.getX(),(int)event.getY())){
                    rightRectPressed=true;
                    Log.d("message","right rectangle clicked, click has coordinates ("+(int)event.getX()+
                            ","+(int)event.getY()+")");
                }
                break;
            case MotionEvent.ACTION_MOVE:
                backgroundPosition = new PointF((int) (lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX())),
                        (int) (lastBackgroundPosition.y + (canvasMovingStartingPoint.y - event.getY())));
                if (leftRectPressed) {
                    //leftRect.left = Math.max(Math.min((int)event.getX(),ourCanvas.getWidth()-20-barWidthBy2),20-barWidthBy2);
                    leftRect.right = Math.max(Math.min((int) event.getX()+barWidthBy2, ourCanvas.getWidth() - 20 + barWidthBy2), 20 + barWidthBy2);
                    leftRect.right = Math.min(rightRect.left, leftRect.right);
                    leftRect.left = leftRect.right-barWidthBy2*2;
                    //leftRect.left + (int)(lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX()));
                }
                if (rightRectPressed) {
                    rightRect.left = Math.max(Math.min((int)event.getX()-barWidthBy2,ourCanvas.getWidth()-20-barWidthBy2),20-barWidthBy2);
                    //rightRect.right = Math.max(Math.min((int) event.getX(), ourCanvas.getWidth() - 20 + barWidthBy2), 20 + barWidthBy2);
                    rightRect.left = Math.max(rightRect.left, leftRect.right);
                    rightRect.right = rightRect.left+barWidthBy2*2;
                    //leftRect.left + (int)(lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX()));
                }


                break;
            case MotionEvent.ACTION_UP:
                leftRectPressed=false;
                rightRectPressed=false;
                break;
            default:
                return false;
        }
        return true;
    }



}