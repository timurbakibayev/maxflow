package com.gii.maxflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.EditText;

import org.apache.http.params.HttpParams;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by onion on 22.11.15.
 */
public class PlotChart{

    public static class LayoutProp{
        int screenW = 800;
        int screenH = 1020;
        int c=30; //this constant shows how much space we leave between different canvases
        Rect plotChartMenuCanvas = new Rect(0,0,screenW,screenH/15);
        Rect LinearChartCanvas = new Rect(screenW/c,        plotChartMenuCanvas.bottom+screenH/c,
                screenW-screenW/c,(int)plotChartMenuCanvas.bottom+(int)(screenH/2.5f)-screenH/c);
        Rect PieChartCanvas = new Rect(screenW/c,        plotChartMenuCanvas.bottom+screenH/c,
                screenW-screenW/c,(int)plotChartMenuCanvas.bottom+(int)(screenH/2.5f)-screenH/c);
        Rect HistChartCanvas = new Rect(screenW/c,        plotChartMenuCanvas.bottom+screenH/c,
                screenW-screenW/c,(int)plotChartMenuCanvas.bottom+(int)(screenH/2.5f)-screenH/c);

        Rect timeLineCanvas = new Rect(screenW/c,         PieChartCanvas.bottom+screenH/c,
                screenW-screenW/c, PieChartCanvas.bottom + screenH/8 - screenH/c);

        Rect listOfElmCanvas = new Rect(screenW/c,         timeLineCanvas.bottom+screenH/c,
                screenW-screenW/c, screenH-screenH/c);



        public int listColRectSize = 30;
        public int fontSize = 30;

        LayoutProp(){
            super();
        }

        public void recalculateLayout(){
            c=30; //this constant shows how much space we leave between different canvases
            LinearChartCanvas = new Rect(screenW/c,        0+screenH/c,
                    screenW-screenW/c,(int)(screenH/2.5f)-screenH/c);
            PieChartCanvas = new Rect(screenW/c,        0+screenH/c,
                    screenW-screenW/c,(int)(screenH/2.5f)-screenH/c);
            HistChartCanvas = new Rect(screenW/c,        0 +screenH/c,
                    screenW-screenW/c,(int)(screenH/2.5f)-screenH/c);
            plotChartMenuCanvas = new Rect(0,PieChartCanvas.bottom+screenH/c,screenW,PieChartCanvas.bottom+screenH/15+screenH/c);

            timeLineCanvas = new Rect(screenW/c,         plotChartMenuCanvas.bottom,
                    screenW-screenW/c, plotChartMenuCanvas.bottom + 1);

            listOfElmCanvas = new Rect(screenW/c,         timeLineCanvas.bottom+1,
                    screenW-screenW/c, screenH-screenH/c);

            listColRectSize = 30;
            fontSize = 30;


        }
    }

    Date prevDateFrom = new Date();
    Date prevDateTo = new Date();

    boolean newPlotChartCall=true;
    boolean recalcLinearChart = true;
    boolean recalcHistChart = true;
    boolean recalcPieChart = true;
    boolean recalcPlotChartMenu = true;
    boolean recalcListOfElements = true;
    boolean scrollListOfElements = true;


    Canvas canvas;
    LayoutProp currLayoutProp= new LayoutProp();

    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);



    Bitmap.Config toBlurBitmapConf = Bitmap.Config.ARGB_8888;

    Graph graph=new Graph();
    GraphBundleType graphBundleType=new GraphBundleType();
    PlotChartMenu plotChartMenu;
    PieChart pieChart;
    HistChart histChart;
    LinearChart linearChart;
    TimeLine timeLine;
    ListOfElements listOfElements=new ListOfElements();

    int toDrawPortion=0; //redraw parameter, responsible for smoothness of drawing diagramms. It shows
    // what a percentage of our chart we want to draw right now. It varies from 0 to maxDrawPortion
    int toDrawPortionPrev=0; //previousValue of toDrawPortion, we need this field to recognize that
    // we need to redraw chart before the previous charts was drawn to the end
    private static final int maxDrawPortion=20;
    private static int       drawPortionSpeed=1;
    private static final int dayMS= 1000*60*60*24;

    private float scaleFactor;
    private int scrolledX = 0;
    private int lastScrolledX = 0;
    private int scrolledStartingPointX = 0;

    Bitmap plotChartMenuBitmap;
    Canvas plotChartMenuCanvas;

    Bitmap linearChartBitmap;
    Canvas linearChartCanvas;
    Bitmap pieChartBitmap;
    Canvas pieChartCanvas;
    Bitmap histChartBitmap;
    Canvas histChartCanvas;

    Bitmap chartBitmap;
    Canvas chartCanvas;
    Bitmap listBitmap;
    Canvas listCanvas;
    Bitmap timeLineBitmap;
    Canvas timeLineCanvas;

    public boolean onTouchEventChartMode(@NonNull MotionEvent event, GII.AppState appState) {


        //check whether user's trying to switch chart type by scrolling left or right
        if(event.getY()>currLayoutProp.PieChartCanvas.top && event.getY()<currLayoutProp.PieChartCanvas.bottom){
            if(event.getAction()==MotionEvent.ACTION_DOWN) {

                scrolledStartingPointX = (int) event.getX();
                lastScrolledX = scrolledX;
            }
            if(event.getAction()==MotionEvent.ACTION_MOVE) {
                //scrolledStartingPointX = (int) event.getX();
                scrolledX = lastScrolledX-(scrolledStartingPointX-(int)event.getX());
                scrolledX=Math.max(-currLayoutProp.screenW,scrolledX);
                scrolledX=Math.min(currLayoutProp.screenW,scrolledX);

            }
            if(event.getAction()==MotionEvent.ACTION_UP || event.getAction()!=MotionEvent.ACTION_MOVE) {
                if(scrolledX<-currLayoutProp.screenW/2)
                    scrolledX = -currLayoutProp.screenW;
                else if(scrolledX>currLayoutProp.screenW/2)
                    scrolledX = currLayoutProp.screenW;
                else if(scrolledX>-currLayoutProp.screenW/2 && scrolledX<currLayoutProp.screenW/2)
                    scrolledX = 0;
            }

        }

        //call plotChartMenu event holder
        if(currLayoutProp.plotChartMenuCanvas.contains((int)event.getX(),(int)event.getY())) {
            MotionEvent plotChartMenuEvent = MotionEvent.obtain(event);
            plotChartMenuEvent.setLocation(event.getX() - currLayoutProp.plotChartMenuCanvas.left,
                    event.getY() - currLayoutProp.plotChartMenuCanvas.top);
            plotChartMenu.onTouchEventPlotChartMenu(plotChartMenuEvent, this);
        }

        //call linearChart event holder
        if(currLayoutProp.LinearChartCanvas.contains((int)event.getX(),(int)event.getY())) {
            MotionEvent linearChartEvent = MotionEvent.obtain(event);

            linearChartEvent.setLocation(event.getX() - currLayoutProp.LinearChartCanvas.left,//- currLayoutProp.LinearChartCanvas.left-scrolledX,
                    event.getY() - currLayoutProp.LinearChartCanvas.top);
            if(linearChart!=null)
                linearChart.onTouchEventLinearChart(linearChartEvent, this);
        }

        //call PieChart event holder
        if( currLayoutProp.PieChartCanvas.contains((int)event.getX(),(int)event.getY()) ) {
            MotionEvent PieChartCanvasEvent = MotionEvent.obtain(event);
            PieChartCanvasEvent.setLocation(event.getX() - currLayoutProp.PieChartCanvas.left - scrolledX,
                    event.getY() - currLayoutProp.PieChartCanvas.top);
            if(pieChart!=null)
                pieChart.onTouchEventPieChart(PieChartCanvasEvent);
        }

        if(currLayoutProp.HistChartCanvas.contains((int)event.getX(),(int)event.getY())) {
            MotionEvent histChartEvent = MotionEvent.obtain(event);

            histChartEvent.setLocation(event.getX() - currLayoutProp.HistChartCanvas.left,//- currLayoutProp.LinearChartCanvas.left-scrolledX,
                    event.getY() - currLayoutProp.HistChartCanvas.top);
            if(histChart!=null)
                histChart.onTouchEventHistChart(histChartEvent, this);
        }

        //call TimeLine event holder
        if( currLayoutProp.timeLineCanvas.contains((int)event.getX(),(int)event.getY()) ) {
            MotionEvent timeLineEvent = MotionEvent.obtain(event);
            timeLineEvent.setLocation(event.getX() - currLayoutProp.timeLineCanvas.left,
                    event.getY() - currLayoutProp.timeLineCanvas.top);
            if(timeLine!=null)
                timeLine.onTouchEventTimeLine(timeLineEvent);
            toDrawPortion=0;
        }
        else {
            timeLine.leftRectPressed = false;
            timeLine.rightRectPressed = false;
        }

        //check ListOfElements event holder
        if( currLayoutProp.listOfElmCanvas.contains((int)event.getX(),(int)event.getY()) ) {
            MotionEvent listOfElementsEvent = MotionEvent.obtain(event);
            listOfElementsEvent.setLocation(event.getX() - currLayoutProp.listOfElmCanvas.left,
                    event.getY() - currLayoutProp.listOfElmCanvas.top);
            if(plotChartMenu!=null)
                listOfElements.onTouchEventListOfElements(listOfElementsEvent,currLayoutProp.listOfElmCanvas,
                        timeLine,toDrawPortion,graphBundleType.allNodes,this);
            toDrawPortion=0;
        }

        return true;
    }

    public  PlotChart() {

        //Bitmap and canvas for the first pieOutput chart
        chartBitmap = Bitmap.createBitmap(currLayoutProp.PieChartCanvas.width(),currLayoutProp.PieChartCanvas.height(),toBlurBitmapConf);
        chartCanvas= new Canvas(chartBitmap);

        //Bitmap and canvas for the time Line
        timeLineBitmap = Bitmap.createBitmap(currLayoutProp.timeLineCanvas.width(),currLayoutProp.timeLineCanvas.height(),toBlurBitmapConf);
        timeLineCanvas = new Canvas(timeLineBitmap);

        //plotting list with names of all the circles and corresponding colors
        listBitmap = Bitmap.createBitmap(currLayoutProp.listOfElmCanvas.width(),currLayoutProp.listOfElmCanvas.height(),toBlurBitmapConf);
        listCanvas = new Canvas(listBitmap);

        if (timeLine == null)
            timeLine = new TimeLine(timeLineCanvas);


        paint.setStyle(Paint.Style.FILL);
        paint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
    }

    public void loadResouces(Context context){

    }


    public void plot(Canvas canvas_orig, GII.AppState appState, Properties properties, ArrayList<Circle> circle,
                     ArrayList<Operation> displayedOperation, Circle selectedCircle, Graphics graphics) {
        Date dateFrom = new Date();
        Date dateTo =   new Date();
        dateFrom.setTime(properties.filterFrom.getTime());
        dateTo.setTime(properties.filterTo.getTime());


        if(dateTo.getTime()-dateFrom.getTime()<=dayMS*1.5) {
            //Log.d("message", "Old starting and finishing dates:");
            //Log.d("message", "dateFrom is " + DateFormat.getDateTimeInstance().format(dateFrom));
            //Log.d("message","dateTo is " + DateFormat.getDateTimeInstance().format(dateTo));
            Date minDate = new Date(1709434461729l);
            Date maxDate = new Date(1);
            for(Operation opr:displayedOperation){
                //Log.d("message", "operation date is " + DateFormat.getDateTimeInstance().format(opr.date)
                //        + ". And has number: " + opr.date.getTime());
                if(opr.date.after(maxDate) && opr.inFilter)
                    maxDate.setTime(opr.date.getTime());
                if(opr.date.before(minDate) && opr.inFilter)
                    minDate.setTime(opr.date.getTime());
            }
            dateFrom.setTime(minDate.getTime());
            dateTo.setTime(maxDate.getTime());
            //Log.d("message", "New starting and finishing dates:");
            //Log.d("message", "dateFrom is " + DateFormat.getDateTimeInstance().format(dateFrom));
            //Log.d("message","dateTo is " + DateFormat.getDateTimeInstance().format(dateTo));
        }


        if(prevDateFrom.getTime()!=dateFrom.getTime() || prevDateTo.getTime()!=dateTo.getTime()
                || prevDateFrom==null || prevDateTo==null) {
            prevDateFrom.setTime(dateFrom.getTime());
            prevDateTo.setTime(dateTo.getTime());
            newPlotChartCall=true;
        }
        /*
        long hour_ms = 1000*60*60;
        long week_ms = hour_ms*24*7;
        Date dateFromPlusWeek=new Date(dateFrom.getTime()+week_ms);
        */
        //DateTime dateFromPlusWeek =
        //dateFromPlusWeek.setDate(dateFrom.getDay()+1);
        //Log.d("message","dateFromPlusWeek is " + DateFormat.getDateTimeInstance().format(dateFromPlusWeek));

        if(newPlotChartCall) {
            graphBundleType=new GraphBundleType();
            currLayoutProp.recalculateLayout();
            graph.fillTheGraph(circle, displayedOperation, properties);
            graphBundleType.fillTheGraph(circle, displayedOperation, properties, selectedCircle, timeLine.leftDate, timeLine.rightDate);
            plotChartMenuBitmap = Bitmap.createBitmap(currLayoutProp.plotChartMenuCanvas.width(),
                    currLayoutProp.plotChartMenuCanvas.height(),toBlurBitmapConf);
            plotChartMenuCanvas = new Canvas(plotChartMenuBitmap);

            linearChartBitmap = Bitmap.createBitmap(currLayoutProp.LinearChartCanvas.width(),
                    currLayoutProp.LinearChartCanvas.height(),toBlurBitmapConf);
            linearChartCanvas = new Canvas(linearChartBitmap);

            pieChartBitmap = Bitmap.createBitmap(currLayoutProp.PieChartCanvas.width(),
                    currLayoutProp.PieChartCanvas.height(),toBlurBitmapConf);
            pieChartCanvas = new Canvas(pieChartBitmap);

            histChartBitmap = Bitmap.createBitmap(currLayoutProp.HistChartCanvas.width(),
                    currLayoutProp.HistChartCanvas.height(),toBlurBitmapConf);
            histChartCanvas = new Canvas(histChartBitmap);


            chartBitmap = Bitmap.createBitmap(currLayoutProp.PieChartCanvas.width(),
                    currLayoutProp.PieChartCanvas.height(),toBlurBitmapConf);
            chartCanvas = new Canvas(chartBitmap);


            timeLineBitmap = Bitmap.createBitmap(currLayoutProp.timeLineCanvas.width(),
                    currLayoutProp.timeLineCanvas.height(),toBlurBitmapConf);
            timeLineCanvas = new Canvas(timeLineBitmap);


            listBitmap = Bitmap.createBitmap(currLayoutProp.listOfElmCanvas.width(),
                    currLayoutProp.listOfElmCanvas.height(),toBlurBitmapConf);
            listCanvas = new Canvas(listBitmap);

            //initialize selection menu
            if( plotChartMenu == null)
                plotChartMenu = new PlotChartMenu();

            //initialize linear chart
            if(linearChart == null)
                linearChart = new LinearChart();
            //here we initialize pieOutput chart
            if (pieChart==null)
                pieChart = new PieChart();
            //and histogram chart
            if (histChart == null)
                histChart = new HistChart();



            plotChartMenu.plotChartMenu(plotChartMenuCanvas, selectedCircle);

            graphBundleType.plotListOfElements(listOfElements, listCanvas, plotChartMenu, graphics,dateFrom,dateTo);
            graphBundleType.plotLinearChart(linearChartCanvas, linearChart, displayedOperation, dateFrom,dateTo,
                    graphics, plotChartMenu, listOfElements);
            graphBundleType.plotHistChart(histChartCanvas, histChart, displayedOperation, dateFrom, dateTo,
                    graphics, plotChartMenu, listOfElements);
            graphBundleType.plotPieChart(pieChartCanvas, pieChart, displayedOperation, dateFrom, dateTo,
                    graphics, plotChartMenu, listOfElements);


            newPlotChartCall=false;
            recalcLinearChart = false;
            recalcHistChart = false;
            recalcPieChart = false;
            recalcPlotChartMenu = false;
            recalcListOfElements = false;
            scrollListOfElements=false;
        }


        if(recalcPlotChartMenu || recalcListOfElements) {
            linearChartCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            histChartCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            pieChartCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            plotChartMenuCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            listCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

            plotChartMenu.plotChartMenu(plotChartMenuCanvas, selectedCircle);
            graphBundleType.plotListOfElements(listOfElements, listCanvas, plotChartMenu, graphics,dateFrom,dateTo);
            graphBundleType.plotLinearChart(linearChartCanvas, linearChart, displayedOperation, dateFrom,dateTo,
                    graphics, plotChartMenu, listOfElements);
            graphBundleType.plotHistChart(histChartCanvas, histChart, displayedOperation, dateFrom, dateTo ,
                    graphics, plotChartMenu, listOfElements);
            graphBundleType.plotPieChart(pieChartCanvas, pieChart, displayedOperation, dateFrom, dateTo,
                    graphics, plotChartMenu, listOfElements);
            recalcPlotChartMenu=false;
            recalcListOfElements = false;
        }

        if(scrollListOfElements){
            listCanvas.drawColor(0, PorterDuff.Mode.CLEAR);

            graphBundleType.plotListOfElements(listOfElements, listCanvas, plotChartMenu, graphics,dateFrom,dateTo);
            scrollListOfElements=false;
        }

        if(pieChart.timeLeftToPlot<pieChart.msToPlot+255){
            pieChartCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            graphBundleType.plotPieChart(pieChartCanvas, pieChart, displayedOperation, dateFrom,dateTo,
                    graphics, plotChartMenu, listOfElements);
        }


        if(recalcLinearChart){
            linearChartCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            graphBundleType.plotLinearChart(linearChartCanvas, linearChart, displayedOperation, dateFrom,dateTo,
                    graphics, plotChartMenu, listOfElements);
            recalcLinearChart=false;
        }

        if(recalcHistChart){
            histChartCanvas.drawColor(0, PorterDuff.Mode.CLEAR);
            graphBundleType.plotHistChart(histChartCanvas, histChart, displayedOperation, dateFrom, dateTo,
                    graphics, plotChartMenu, listOfElements);
            recalcHistChart=false;
        }





        //finally plotting of ready canvases
        canvas_orig.drawBitmap(linearChartBitmap,currLayoutProp.LinearChartCanvas.left +scrolledX - currLayoutProp.screenW,
                currLayoutProp.LinearChartCanvas.top,paint);
        canvas_orig.drawBitmap(pieChartBitmap, currLayoutProp.PieChartCanvas.left + scrolledX,
                currLayoutProp.PieChartCanvas.top, paint);
        canvas_orig.drawBitmap(histChartBitmap, currLayoutProp.HistChartCanvas.left + scrolledX + currLayoutProp.screenW,
                currLayoutProp.HistChartCanvas.top, paint);

        canvas_orig.drawBitmap(plotChartMenuBitmap, currLayoutProp.plotChartMenuCanvas.left, currLayoutProp.plotChartMenuCanvas.top, paint);
        canvas_orig.drawBitmap(listBitmap, currLayoutProp.listOfElmCanvas.left, currLayoutProp.listOfElmCanvas.top, paint);
        canvas_orig.drawBitmap(timeLineBitmap, currLayoutProp.timeLineCanvas.left, currLayoutProp.timeLineCanvas.top, paint);


    }


}