package com.gii.maxflow;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by onion on 14.01.16.
 */

/**
 * Created by onion on 01.01.16.
 */
public class LinearChart {
    class crcl{
        float x;
        float y;
        float r;
        public  crcl ()
        {x =0; y =0; r = 0;}
        public  crcl (float x1, float y1, float r1)
        {x = x1; y = y1; r = r1;}
        public  crcl (int x1, int y1, int r1)
        {x = x1; y = y1; r = r1;}


    }
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Canvas ourCanvas;
    crcl[][] allCircles;
    Rect logRect,physRect;
    //2 indices of selected circles
    int selected_i=-1;
    int selected_j=-1;
    int textSize = 20;

    private PointF backgroundPosition = new PointF(0, 0);
    private PointF lastBackgroundPosition = new PointF(0,0);
    private PointF canvasMovingStartingPoint = new PointF(0,0);

    Rect buttonD = new Rect();
    Rect buttonW = new Rect();
    Rect buttonM = new Rect();
    Rect buttonY = new Rect();
    String buttonSelected = "D";

    final long dayMS = (1000*60*60*24);
    final long secondMS = 1000;
    final long tenMinMS = 10 * 60 * 1000;


    public LinearChart(){//,ArrayList<Operation> displayedOperation){
        super();
    };

    public void toPlotLinearChart(Canvas canvas, ArrayList<Operation> operation, ArrayList<Circle> circle) {
        ourCanvas = canvas;
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.FILL);

    };


    public void plotLinearChart(Canvas org_canvas, int[][] M, int[] colors, long dateFrom, long dateTo,
                                String[] names, ArrayList<Date> verticalDashes, String mode) {
        int buttonOffset = org_canvas.getHeight() / 21;
        textSize = Math.min(org_canvas.getWidth() / 25, 30);
        //Draw background rectangle
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        org_canvas.drawRoundRect(new RectF(0, 0, org_canvas.getWidth(), org_canvas.getHeight()), 6, 6, paint);
        paint.setStrokeWidth(1);
        if (M != null && M.length != 0)
            if (M[0] != null && M[0].length != 0) {

                paint.setStyle(Paint.Style.FILL);
                paint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
                int n = M.length;    //number of elements
                int m = M[0].length; //number of days
                allCircles = new crcl[n][m];

                float maxValue = -(10 ^ 10), minValue = 10 ^ 10;
                for (int i = 0; i < n; i++)
                    for (int j = 0; j < m; j++) {
                        if (M[i][j] > maxValue)
                            maxValue = M[i][j];
                        if (M[i][j] < minValue)
                            minValue = M[i][j];

                    }

                //in such a way we leave a little space both left and right
                maxValue = maxValue + (maxValue - minValue) * 0.15f;
                minValue = minValue - (maxValue - minValue) * 0.15f;

                if (maxValue - minValue < 100) {
                    maxValue = maxValue + 90;
                    minValue = minValue - 20;
                }
                if (minValue > 0)
                    minValue = -10;
                if (maxValue < 0)
                    maxValue = 10;
                //rect is of following form Rect(left top right bottom)
                //an area of plotting chart on a physical screen with respect to org_canvas
                physRect = new Rect((int) (org_canvas.getWidth()  / 18f), (int) (org_canvas.getHeight() / 20f),
                        org_canvas.getWidth() - buttonOffset * 5, (int) (org_canvas.getHeight() * 19f / 20f));

                //logRect = new Rect((int)minValue,dateFrom, dateTo,(int)maxValue);
                logRect = new Rect(0, (int) maxValue, Math.round((dateTo - dateFrom) / 1000), (int) minValue);

                double width = physRect.width() / (n * m);

                paint.setStrokeWidth(1);
                plotAxis(org_canvas, maxValue, minValue, dateFrom, dateTo, mode);


                paint.setStrokeWidth(2);
                //draw Rectangles
                for (int j = 0; j < M[0].length; j++) {
                    float xCoord,xCoordNext;
                    xCoord = (float) (trnsfmX((verticalDashes.get(j).getTime() - dateFrom) / 1000 + logRect.width()/M[0].length/2) );
                    if(j<M[0].length-1)
                        xCoordNext = (float) (trnsfmX((verticalDashes.get(j+1).getTime() - dateFrom) / 1000 + logRect.width()/M[0].length/2) );
                    else
                        xCoordNext = (float) (trnsfmX((verticalDashes.get(j).getTime() - dateFrom) / 1000 + 3f*logRect.width()/M[0].length/2) );
                    for (int i = 0; i < M.length; i++) {
                        paint.setStrokeWidth(2);
                        paint.setColor(colors[i]);
                        //first draw line
                        if(j<M[0].length-1)
                            org_canvas.drawLine(xCoord, trnsfmY(M[i][j]),xCoordNext,trnsfmY(M[i][j+1]),paint);

                        //now circle
                        if(M[i][j] != 0 || j==0 || j==M[0].length-1) {
                            paint.setColor(colors[i]);
                            org_canvas.drawCircle(xCoord, trnsfmY(M[i][j]), 9f, paint);
                            paint.setColor(Color.WHITE);
                            org_canvas.drawCircle(xCoord, trnsfmY(M[i][j]), 7, paint);
                        }
                        allCircles[i][j] = new crcl(xCoord, trnsfmY(M[i][j]), 9);

                        //if current circle's been selected, we draw a smaller circle inside and a small label beside
                        if (selected_i == i && selected_j == j) {
                            paint.setColor(colors[i]);
                            org_canvas.drawCircle(xCoord, trnsfmY(M[i][j]), 5, paint);
                            paint.setTextSize(textSize);
                            paint.setStrokeWidth(1);
                            String s = names[i] + " " + M[i][j];
                            int x_coord = (int) Math.min(xCoord + 10f, physRect.right - paint.measureText(s));
                            int y_coord = (int) Math.max(trnsfmY(M[i][j]) - 10f, physRect.top + paint.getTextSize());

                            //org_canvas.drawText(s,trnsfmX(j)+10, trnsfmY(M[i][j])-10,paint);
                            paint.setStyle(Paint.Style.FILL);

                            paint.setColor(Color.DKGRAY);
                            org_canvas.drawText(s, x_coord, y_coord, paint);
                        }
                    }
                }


                //Now draw buttons
                buttonD = new Rect(org_canvas.getWidth() - buttonOffset * 5, buttonOffset * 1,
                        org_canvas.getWidth() - buttonOffset, buttonOffset * 5);
                buttonW = new Rect(org_canvas.getWidth() - buttonOffset * 5, buttonD.bottom + buttonOffset,
                        org_canvas.getWidth() - buttonOffset, buttonD.bottom + buttonOffset * 5);
                buttonM = new Rect(org_canvas.getWidth() - buttonOffset * 5, buttonW.bottom + buttonOffset,
                        org_canvas.getWidth() - buttonOffset, buttonW.bottom + buttonOffset * 5);
                buttonY = new Rect(org_canvas.getWidth() - buttonOffset * 5, buttonM.bottom + buttonOffset,
                        org_canvas.getWidth() - buttonOffset, buttonM.bottom + buttonOffset * 5);
                paint.setColor(Color.rgb(25, 200, 209));//blue
                org_canvas.drawCircle(buttonD.centerX(), buttonD.centerY(), (int) (buttonOffset * 2), paint);
                org_canvas.drawCircle(buttonW.centerX(), buttonW.centerY(), (int) (buttonOffset * 2), paint);
                org_canvas.drawCircle(buttonM.centerX(), buttonM.centerY(), (int) (buttonOffset * 2), paint);
                org_canvas.drawCircle(buttonY.centerX(), buttonY.centerY(), (int) (buttonOffset * 2), paint);
                paint.setColor(Color.GRAY);
                paint.setTextSize((int) (buttonOffset * 1.8));
                org_canvas.drawText("D", buttonD.centerX() - (paint.measureText("D") / 2) + 2, buttonD.centerY() + (int) paint.getTextSize() / 2.4f, paint);
                org_canvas.drawText("W", buttonW.centerX() - (paint.measureText("W") / 2), buttonW.centerY() + (int) paint.getTextSize() / 2.4f, paint);
                org_canvas.drawText("M", buttonM.centerX() - (paint.measureText("M") / 2), buttonM.centerY() + (int) paint.getTextSize() / 2.4f, paint);
                org_canvas.drawText("Y", buttonY.centerX() - (paint.measureText("Y") / 2), buttonY.centerY() + (int) paint.getTextSize() / 2.4f, paint);

                paint.setTextSize((int) (buttonOffset * 1.8));
                paint.setColor(Color.WHITE);
                if (buttonSelected.equals("D"))
                    org_canvas.drawText("D", buttonD.centerX() - (paint.measureText("D") / 2) + 2, buttonD.centerY() + (int) paint.getTextSize() / 2.4f, paint);
                if (buttonSelected.equals("W"))
                    org_canvas.drawText("W", buttonW.centerX() - (paint.measureText("W") / 2), buttonW.centerY() + (int) paint.getTextSize() / 2.4f, paint);
                if (buttonSelected.equals("M"))
                    org_canvas.drawText("M", buttonM.centerX() - (paint.measureText("M") / 2), buttonM.centerY() + (int) paint.getTextSize() / 2.4f, paint);
                if (buttonSelected.equals("Y"))
                    org_canvas.drawText("Y", buttonY.centerX() - (paint.measureText("Y") / 2), buttonY.centerY() + (int) paint.getTextSize() / 2.4f, paint);


            }

        paint.setTextSize(textSize);
        //draw all the dashes
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(1);

        int dashesNumber = Math.min(3 * 5 * 7, verticalDashes.size());
        for (int i = 0; i < dashesNumber; i++) {
            Date date = verticalDashes.get((int) Math.floor((double) i / dashesNumber * verticalDashes.size()));
            //String dateString = new SimpleDateFormat("ddMMM").format(date);
            //org_canvas.drawText(dateString, trnsfmX((date.getTime() - dateFrom) / secondMS) - paint.measureText(dateString) / 2, trnsfmY(0) + 20, paint);
            org_canvas.drawLine(trnsfmX((date.getTime() - dateFrom) / secondMS), trnsfmY(0) - 3,
                    trnsfmX((date.getTime() - dateFrom) / secondMS), trnsfmY(0) + 3, paint);
        }
        //attach labels to the X-axis
        int marksNumber = Math.min(6, verticalDashes.size());
        for (int i = 0; i < marksNumber; i++) {
            Date date = verticalDashes.get((int) Math.floor((double) i / marksNumber * verticalDashes.size()));
            String dateString = new SimpleDateFormat("d/MM").format(date);
            org_canvas.drawLine(trnsfmX((date.getTime() - dateFrom) / secondMS), trnsfmY(0) - 3,
                    trnsfmX((date.getTime() - dateFrom) / secondMS), trnsfmY(0) + 3, paint);
            org_canvas.drawText(dateString, trnsfmX((date.getTime() - dateFrom) / secondMS) - paint.measureText(dateString) / 2,
                                trnsfmY(0) + textSize, paint);
        }

        /*
        //draw lines
        for (int i = 0; i < M.length; i++)
            for (int j = 0; j < M[i].length - 1; j++) {
                paint.setColor(colors[i]);
                org_canvas.drawLine(trnsfmX(j), trnsfmY(M[i][j]), trnsfmX(j + 1), trnsfmY(M[i][j + 1]), paint);
            }

        //draw circles
        for (int i = 0; i < M.length; i++)
            for (int j = 0; j < M[i].length; j++) {
                paint.setColor(colors[i]);
                //paint.setStyle(Paint.Style.STROKE);
                //paint.setStrokeWidth(3);
                org_canvas.drawCircle(trnsfmX(j), trnsfmY(M[i][j]), 9, paint);
                allCircles[i][j] = new crcl(trnsfmX(j), trnsfmY(M[i][j]), 9);
                paint.setStyle(Paint.Style.FILL_AND_STROKE);
                paint.setColor(Color.WHITE);
                org_canvas.drawCircle(trnsfmX(j), trnsfmY(M[i][j]), 6, paint);

                //if current circle's been selected, we draw a smaller circle inside and a small label beside
                if (selected_i == i && selected_j == j) {
                    paint.setColor(colors[i]);
                    org_canvas.drawCircle(trnsfmX(j), trnsfmY(M[i][j]), 4, paint);
                    paint.setTextSize(30);
                    String s = names[i] + " " + M[i][j];
                    int x_coord = (int) Math.min(trnsfmX(j) + 10f, physRect.right - paint.measureText(s));
                    int y_coord = (int) Math.max(trnsfmY(M[i][j]) - 10f, physRect.top + paint.getTextSize());

                    //org_canvas.drawText(s,trnsfmX(j)+10, trnsfmY(M[i][j])-10,paint);
                    paint.setStyle(Paint.Style.FILL_AND_STROKE);
                    paint.setStrokeWidth(1);
                    paint.setColor(Color.DKGRAY);
                    org_canvas.drawText(s, x_coord, y_coord, paint);
                }
            }

        paint.setStrokeWidth(1);
        */
        selected_i=-1;
        selected_j=-1;
    }


    private void plotAxis(Canvas org_canvas, float maxValue, float minValue, long dateFrom, long dateTo, String mode) {
        paint.setColor(Color.DKGRAY);
        paint.setStrokeWidth(1);

        float horStep = (float)Math.pow(10,(int)Math.floor(Math.log10((maxValue - minValue))));

        if  (Math.log10((maxValue-minValue)) - Math.floor(Math.log10((maxValue-minValue))) < 0.2) {
            horStep = horStep / 4;
        }
        else {
            if (Math.log10((maxValue - minValue)) - Math.floor(Math.log10((maxValue - minValue))) < 0.4)
                horStep = horStep / 2;
        }

        //value of a lower horizontal line
        float lowerValue=(float)(Math.ceil(minValue/horStep)*horStep);
        while(lowerValue<maxValue-20){
            paint.setTextSize(textSize);
            //y coordinate of lowerValue
            int yCoord = (int)(physRect.height()*(1-(lowerValue-minValue)/(maxValue-minValue)));
            org_canvas.drawText(String.valueOf((int) lowerValue),
                    Math.max(6, physRect.left - paint.measureText(String.valueOf(lowerValue)) / 1),
                    trnsfmY((int)lowerValue) - 3, paint);
            org_canvas.drawLine(Math.max(12, physRect.left - paint.measureText(String.valueOf(lowerValue)) / 1),//physRect.left,
                    trnsfmY((int)lowerValue),
                    physRect.width()*59/60+physRect.left,trnsfmY((int)lowerValue),paint);
            lowerValue=lowerValue+horStep;
        }
    };

    private int trnsfmX(int date){
        if(logRect.width()>0)
            return Math.round((float)(date)/(float)logRect.width()*(float)physRect.width()+(float)physRect.left);
        else
            return Math.round((float)physRect.width()*0.5f+(float)physRect.left);
        //we add +1 to the varaible date, since this variable may take value 0
    }

    private int trnsfmY(int value) {
        return Math.round((float) (value - logRect.bottom)/(float)logRect.height()*(float)physRect.height()+(float)physRect.bottom);
    }

    public boolean onTouchEventLinearChart(@NonNull MotionEvent event,PlotChart plotChart) {
        canvasMovingStartingPoint = new PointF((int) event.getX(), (int) event.getY());
        lastBackgroundPosition = backgroundPosition;
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                if(allCircles!=null)
                    for(int i = 0; i<allCircles.length; i++)
                        for(int j = 0; j<allCircles[i].length; j++){

                            float dist = (float)Math.sqrt(Math.pow(allCircles[i][j].x-event.getX(),2) +
                                    Math.pow(allCircles[i][j].y-event.getY(),2));
                            if (dist<allCircles[i][j].r*2.5) {
                                selected_i = i;
                                selected_j = j;
                                plotChart.recalcLinearChart=true;
                                break;
                            }
                        }

                if(buttonD.contains((int)event.getX(),(int)event.getY()))
                    buttonSelected="D";
                if(buttonW.contains((int)event.getX(),(int)event.getY()))
                    buttonSelected="W";
                if(buttonM.contains((int)event.getX(),(int)event.getY()))
                    buttonSelected="M";
                if(buttonY.contains((int)event.getX(),(int)event.getY()))
                    buttonSelected="Y";

                plotChart.recalcLinearChart=true;

                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                return false;
        }
        return true;
    }


    private int trnsfmX(long date){
        return (int)((double)(date)*(double)physRect.width()/(double)logRect.width()+(double)physRect.left);
    }
    private int trnsfmX(double date){
        return (int)((double)(date)*(double)physRect.width()/(double)logRect.width()+(double)physRect.left);
    }

    private int trnsfmY(long value){
        return (int)((float)(value-logRect.bottom)/(float)logRect.height()*(float)physRect.height()+(float)physRect.bottom);
    }

    private int trnsfmY(double value){
        return (int)((float)(value-logRect.bottom)/(float)logRect.height()*(float)physRect.height()+(float)physRect.bottom);
    }


}