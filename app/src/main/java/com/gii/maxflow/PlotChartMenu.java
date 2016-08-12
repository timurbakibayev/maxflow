package com.gii.maxflow;

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
 * Created by onion on 16.01.16.
 */
public class PlotChartMenu {
    Paint paint = new Paint();
    Circle selectedCircle;
    String s;
    Rect inRectButton = new Rect();
    Rect outRectButton = new Rect();
    Rect generalModeButton = new Rect();
    boolean inRectActivated = false;
    boolean outRectActivated = false;
    boolean generalModeActivated = true;
    int textSize;

    PlotChartMenu(){super();}

    public void plotChartMenu(Canvas org_canvas,Circle selCrc){
        selectedCircle=selCrc;
        inRectButton = new Rect(15,0, org_canvas.getWidth()/20*7,org_canvas.getHeight());
        generalModeButton = new Rect(org_canvas.getWidth()/20*7,0, org_canvas.getWidth()/20*13,org_canvas.getHeight());
        outRectButton = new Rect(org_canvas.getWidth()/20*13,0, org_canvas.getWidth()-15,org_canvas.getHeight());
        //textSize = Math.min(org_canvas.getWidth() / 20, 30);
        //at first we draw buttons
        /*
        paint.setColor(Color.rgb(0, 138, 184));//blue
        org_canvas.drawRect(inRectButton, paint);
        org_canvas.drawRect(outRectButton, paint);
        org_canvas.drawRect(generalModeButton, paint);
        */

        //now we draw delimiters
        /*
        paint.setColor(Color.BLACK);
        org_canvas.drawRect(inRectButton.right, 0, inRectButton.right + 1, inRectButton.bottom, paint);
        org_canvas.drawRect(outRectButton.right, 0, outRectButton.right + 2, outRectButton.bottom, paint);
        */

        //we also need to underline selected buttons
        if(inRectActivated) {
            paint.setColor(Color.WHITE);
            paint.setColor(Color.rgb(230, 230, 230));
            org_canvas.drawRoundRect(new RectF(inRectButton), 5f, 5f, paint);
            paint.setColor(Color.rgb(51, 255, 102));//green
            org_canvas.drawRect(inRectButton.left, inRectButton.bottom - 4, inRectButton.right, inRectButton.bottom, paint);
        }
        if(outRectActivated) {
            paint.setColor(Color.WHITE);
            paint.setColor(Color.rgb(230, 230, 230));
            org_canvas.drawRoundRect(new RectF(outRectButton), 5f, 5f, paint);
            paint.setColor(Color.rgb(255, 102, 51));//orange
            org_canvas.drawRect(outRectButton.left, outRectButton.bottom - 4, outRectButton.right, outRectButton.bottom, paint);
        }

        if(generalModeActivated) {
            paint.setColor(Color.WHITE);
            paint.setColor(Color.rgb(230,230,230));
            org_canvas.drawRoundRect(new RectF(generalModeButton), 5f, 5f, paint);
            paint.setColor(Color.BLACK);
            org_canvas.drawRect(generalModeButton.left, generalModeButton.bottom - 4, generalModeButton.right, generalModeButton.bottom, paint);
        }
        //at last write labels
        paint.setTextSize(30);
        paint.setColor(Color.rgb(108, 209, 25));//green
        if(!selectedCircle.isMyMoney() && false) {  //deactivate this option
            paint.setTextSize(10);
            float l = Math.max(paint.measureText("Income from"),paint.measureText(selectedCircle.name));
            l = Math.max(10 * l / inRectButton.width(), 25);
            //l = Math.min(l,inRectButton.height()/3);
            paint.setTextSize(l);

            s = "Income from ";
            org_canvas.drawText(s, inRectButton.centerX() - paint.measureText(s) / 2, inRectButton.centerY(), paint);
            s = selectedCircle.name;
            org_canvas.drawText(s, inRectButton.centerX() - paint.measureText(s) / 2, generalModeButton.centerY() + paint.getTextSize(), paint);
        }
        else{
            paint.setTextSize(10);
            float l = Math.max(paint.measureText("Income"), paint.measureText(selectedCircle.name));
            l = Math.max(20 * l / inRectButton.width(), 30);
            //l = Math.min(l,inRectButton.height()/3);
            paint.setTextSize(l);

            s = "Income";
            s = "Доходы";
            org_canvas.drawText(s, inRectButton.centerX() - paint.measureText(s) / 2, inRectButton.centerY(), paint);

        }

        paint.setColor(Color.rgb(209, 34, 25));//orange
        if(!selectedCircle.isMyMoney() && false) {  //deactivate this option
            paint.setTextSize(10);
            float l = Math.max(paint.measureText("Expenses of"),paint.measureText(selectedCircle.name));
            l = Math.max(10 * l / outRectButton.width(), 25);
            //l = Math.min(l,outRectButton.height()/3);
            paint.setTextSize(l);

            s = "Expenses of";
            org_canvas.drawText(s, outRectButton.centerX() - paint.measureText(s) / 2, outRectButton.centerY(), paint);
            s = selectedCircle.name;
            org_canvas.drawText(s, outRectButton.centerX() - paint.measureText(s) / 2, generalModeButton.centerY() + paint.getTextSize(), paint);
        }
        else{
            paint.setTextSize(10);
            float l = paint.measureText("Expenses");
            l = Math.max(20 * l / outRectButton.width(), 30);
            //l = Math.min(l,outRectButton.height()/3);
            paint.setTextSize(l);

            s = "Expenses";
            s = "Расходы";
            org_canvas.drawText(s, outRectButton.centerX() - paint.measureText(s) / 2, outRectButton.centerY(), paint);

        }

        paint.setColor(Color.rgb(25, 200, 209));//blue
        paint.setColor(Color.BLACK);//black, obviously
        s = "Together";
        s = "Баланс";
        org_canvas.drawText(s, generalModeButton.centerX() - paint.measureText(s) / 2, generalModeButton.centerY(), paint);

        paint.setColor(Color.rgb(191,191,191));
        org_canvas.drawRect(0, org_canvas.getHeight() - 1, org_canvas.getWidth(), org_canvas.getHeight(), paint);
    }

    public boolean onTouchEventPlotChartMenu(@NonNull MotionEvent event,PlotChart plotChart) {
        Log.d("message","PlotChartMenu event holder");

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                if(inRectButton.contains((int)event.getX(),(int)event.getY())) {
                    inRectActivated = true;
                    outRectActivated = false;
                    generalModeActivated=false;
                    Log.d("message","inRectButton contains click");
                    plotChart.recalcPlotChartMenu=true;
                }
                if(outRectButton.contains((int)event.getX(),(int)event.getY())) {
                    inRectActivated = false;
                    outRectActivated = true;
                    generalModeActivated=false;
                    plotChart.recalcPlotChartMenu=true;
                }

                if(generalModeButton.contains((int)event.getX(),(int)event.getY())) {
                    inRectActivated = false;
                    outRectActivated = false;
                    generalModeActivated = true;
                    plotChart.recalcPlotChartMenu=true;
                }
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


}