package com.gii.maxflow;

import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;

/**
 * Created by onion on 03.01.16.
 */

public class PieChart {
    boolean CheckDrawArcMine = true;
    Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    Canvas canvas;
    ArrayList<Float> strtAngle = new ArrayList<Float>();
    ArrayList<Float> finAngle  = new ArrayList<Float>();
    ArrayList<Float> value     = new ArrayList<Float>();
    ArrayList<String> sectorName = new ArrayList<String>();
    String sectorNameClicked="";
    float sectorValueClicked=0f;
    long toDrawTime = 0;
    long timeLeftToPlot = 0;
    final int msToPlot=3000; //ms to plot name
    int trnsp=0;
    String mainLabel = "Ля-Ля-Тополя";
    float mainValue=666f;


    float radiusFrom,radiusTo;
    int pieCenterX,pieCenterY;


    private PointF backgroundPosition = new PointF(0, 0);
    private PointF lastBackgroundPosition = new PointF(0,0);
    private PointF canvasMovingStartingPoint = new PointF(0,0);


    public PieChart(){//,ArrayList<Operation> displayedOperation){
        super();
    };


    public void plotPieChartGeneral(Canvas org_canvas, Graph graph, ArrayList<Circle> circle,
                                    ArrayList<Operation> displayedOperation,int leftDate, int rightDate,
                                    int toDrawPortion, int maxDrawPortion) {
        strtAngle = new ArrayList<Float>();
        finAngle  = new ArrayList<Float>();
        value     = new ArrayList<Float>();
        sectorName = new ArrayList<String>();
        canvas=org_canvas;

        paint.setStyle(Paint.Style.FILL);
        paint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
        float totalSum = 0f;
        int amountNonZeroCircles=0;
        //divide by a larger number to get more compact chart
        int pieRadius = (int)(Math.min(org_canvas.getHeight(), org_canvas.getWidth())/2.0);

        pieCenterX = org_canvas.getWidth()/2;
        pieCenterY = org_canvas.getHeight()/2;

        radiusTo=pieRadius;
        radiusFrom=pieRadius*0.85f;

        for (GraphKnot acc: graph.allKnots)
            if (!graph.toHideList.contains(acc.knot)) {
                acc.toPlotValue=0f;
                for (Operation opr:displayedOperation) {
                    if(opr.pageNo>=leftDate && opr.pageNo<=rightDate) {
                        if (opr.toCircle.equals(acc.knot.id)) {   //here we may add another condition to verify operation
                            /*
                            if(acc.knot.name.equals("meat"))
                                Log.d("message","found an operation to meat with pictureNo "+opr.amount);
                            if(acc.knot.name=="tires")
                                Log.d("message","found an operation to tires with pictureNo "+opr.amount);
                                */
                            // lies in a specific region of interest
                            acc.toPlotValue = acc.toPlotValue + opr.amount;
                        }
                        if (opr.fromCircle.equals(acc.knot.id)) { //here we may add another condition to verify operation
                            // lies in a specific region of interest
                            acc.toPlotValue = acc.toPlotValue - opr.amount;
                        }
                    }

                }
                //Log.d("message","circle " + acc.knot.name+" has pictureNo "+ acc.toPlotValue);
                totalSum += Math.abs(acc.toPlotValue);
                if(totalSum>0);
                amountNonZeroCircles=amountNonZeroCircles+1;
            }
        //generateColors(amountNonZeroCircles);

        float curAngle = 0f;
        float toAddAngle = 0f;
        int k = 0;

        //calculate coordinates of the pieOutput
        for (GraphKnot acc : graph.allKnots) {
            graph.currPieChartStrtAngle[graph.allKnots.indexOf(acc)]=curAngle;
            //new version
            toAddAngle = 0;
            if (!graph.toHideList.contains(acc.knot))
                toAddAngle = 360.0f * Math.abs(acc.toPlotValue) / (float)totalSum;

            curAngle = curAngle + toAddAngle;
            /*prev version
            toAddAngle = 360.0f * Math.abs(acc.toPlotValue) / (float)totalSum;
            if (!graph.toHideList.contains(acc.knot))
                curAngle = curAngle + toAddAngle;
            */
            graph.currPieChartFinAngle[graph.allKnots.indexOf(acc)] = curAngle;
        }

        float t = (float)toDrawPortion / (float)maxDrawPortion;
        t=(float)Math.sin(3.1415*((double)t-0.5))/2.0f+0.5f;
        t=t*t;
        for (GraphKnot acc : graph.allKnots) {
            paint.setColor(acc.colorIndex);
            int ind = graph.allKnots.indexOf(acc);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !CheckDrawArcMine){
                org_canvas.drawArc(pieCenterX - pieRadius, pieCenterY - pieRadius, pieCenterX + pieRadius, pieCenterY + pieRadius,
                        graph.prevPieChartStrtAngle[ind] * (1 - t) + graph.currPieChartStrtAngle[ind] * t,//30*t
                        graph.prevPieChartFinAngle[ind] * (1 - t) + graph.currPieChartFinAngle[ind] *
                                t - graph.prevPieChartStrtAngle[ind] * (1 - t) - graph.currPieChartStrtAngle[ind] * t, true, paint);}
            if(CheckDrawArcMine){
                drawArcMine(org_canvas, pieCenterX - pieRadius, pieCenterY - pieRadius, pieCenterX + pieRadius, pieCenterY + pieRadius,
                        graph.prevPieChartStrtAngle[ind] * (1 - t) + graph.currPieChartStrtAngle[ind] * t,//30*t
                        graph.prevPieChartFinAngle[ind] * (1 - t) + graph.currPieChartFinAngle[ind] *
                                t - graph.prevPieChartStrtAngle[ind] * (1 - t) - graph.currPieChartStrtAngle[ind] * t, true, paint);
            }
            strtAngle.add(graph.prevPieChartStrtAngle[ind] * (1 - t) + graph.currPieChartStrtAngle[ind] * t);
            finAngle.add(graph.prevPieChartFinAngle[ind] * (1 - t) + graph.currPieChartFinAngle[ind] * t);
            sectorName.add(graph.allKnots.get(ind).knot.name);
            int lastInd=strtAngle.size()-1;
            value.add(acc.toPlotValue);
            //Log.d("message","Circle " + acc.knot.name +" has toPlotValue " + value.get(lastInd));




        }
        paint.setColor(Color.rgb(240, 240, 240));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            org_canvas.drawArc(pieCenterX - pieRadius * 0.85f, pieCenterY - pieRadius * 0.85f,
                    pieCenterX + pieRadius * 0.85f, pieCenterY + pieRadius * 0.85f,
                    0, 360, true, paint);

        //if(sectorNameClicked!=null)
        plotLabel(sectorNameClicked,sectorValueClicked);
    };

    /*
    public void plotPieChartBundleType(Canvas org_canvas, GraphBundleType graph, ArrayList<Circle> circle,
                                       ArrayList<Operation> displayedOperation,int leftDate, int rightDate,
                                       int toDrawPortion, int maxDrawPortion, boolean incomeRectActivated) {
        canvas=org_canvas;
        if(!incomeRectActivated) {
            paint.setStyle(Paint.Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
            int totalSum = 0;
            //we don't actually need to divide by 1.5, just do this to smooth the performance
            int pieRadius = (int)(Math.min(org_canvas.getHeight(), org_canvas.getWidth())/2.0);

            pieCenterX = org_canvas.getWidth()/2;
            pieCenterY = org_canvas.getHeight()/2;

            for (GraphKnot acc : graph.outcomingKnots) {
                if (graph.outcomingKnotsValues[graph.outcomingKnots.indexOf(acc)] > 0 && !graph.toHideList.contains(acc.knot))
                    totalSum += graph.outcomingKnotsValues[graph.outcomingKnots.indexOf(acc)];
            }


            float curAngle = 0f;
            float toAddAngle = 0f;
            int k = 0;

            //calculate coordinates of the pieOutput
            for (GraphKnot acc : graph.outcomingKnots) {
                graph.currPieChartStrtAngle[graph.outcomingKnots.indexOf(acc)] = curAngle;
                toAddAngle = 0;
                if (!graph.toHideList.contains(acc.knot))
                    toAddAngle = 360.0f * graph.outcomingKnotsValues[graph.outcomingKnots.indexOf(acc)] / (float) totalSum;

                curAngle = curAngle + toAddAngle;
                graph.currPieChartFinAngle[graph.outcomingKnots.indexOf(acc)] = curAngle;
            }

            float t = (float) toDrawPortion / (float) maxDrawPortion;
            t = (float) Math.sin(3.1415 * ((double) t - 0.5)) / 2.0f + 0.5f;
            t = t * t;
            for (GraphKnot acc : graph.outcomingKnots) {

                paint.setColor(acc.colorIndex);
                //paint.setColor(Color.rgb(Math.random()*, 240, 240));
                int ind = graph.outcomingKnots.indexOf(acc);

                org_canvas.drawArc(pieCenterX-pieRadius, pieCenterY-pieRadius, pieCenterX+pieRadius, pieCenterY+pieRadius,
                        graph.prevPieChartStrtAngle[ind] *
                                (1 - t) + graph.currPieChartStrtAngle[ind] * t,//30*t
                        graph.prevPieChartFinAngle[ind] * (1 - t) + graph.currPieChartFinAngle[ind] *
                                t - graph.prevPieChartStrtAngle[ind] * (1 - t) - graph.currPieChartStrtAngle[ind] * t, true, paint);


            }

            paint.setColor(Color.rgb(240, 240, 240));

            org_canvas.drawArc(pieCenterX-pieRadius*0.85f, pieCenterY-pieRadius*0.85f,
                    pieCenterX+pieRadius*0.85f, pieCenterY+pieRadius*0.85f,
                    0, 360, true, paint);
        }


        if(incomeRectActivated) {
            paint.setStyle(Paint.Style.FILL);
            paint.setMaskFilter(new BlurMaskFilter(1, BlurMaskFilter.Blur.NORMAL));
            int totalSum = 0;
            int pieRadius = (int)(Math.min(org_canvas.getHeight(), org_canvas.getWidth())/2.0);

            pieCenterX = org_canvas.getWidth()/2;
            pieCenterY = org_canvas.getHeight()/2;

            for (GraphKnot acc : graph.incomingKnots) {
                if (graph.incomingKnotsValues[graph.incomingKnots.indexOf(acc)] > 0 && !graph.toHideList.contains(acc.knot))
                    totalSum += graph.incomingKnotsValues[graph.incomingKnots.indexOf(acc)];
            }


            float curAngle = 0f;
            float toAddAngle = 0f;
            int k = 0;

            //calculate coordinates of the pieOutput
            for (GraphKnot acc : graph.incomingKnots) {
                graph.currPieChartStrtAngle[graph.incomingKnots.indexOf(acc)] = curAngle;
                toAddAngle = 0;
                if (!graph.toHideList.contains(acc.knot))
                    toAddAngle = 360.0f * graph.incomingKnotsValues[graph.incomingKnots.indexOf(acc)] / (float) totalSum;

                curAngle = curAngle + toAddAngle;
                graph.currPieChartFinAngle[graph.incomingKnots.indexOf(acc)] = curAngle;
            }

            float t = (float) toDrawPortion / (float) maxDrawPortion;
            t = (float) Math.sin(3.1415 * ((double) t - 0.5)) / 2.0f + 0.5f;
            t = t * t;
            for (GraphKnot acc : graph.incomingKnots) {

                paint.setColor(acc.colorIndex);
                //paint.setColor(Color.rgb(Math.random()*, 240, 240));
                int ind = graph.incomingKnots.indexOf(acc);
                //Log.d("message",graph.incomingKnots.get(ind).knot.name+" has pictureNo " + graph.incomingKnotsValues[ind]+
                //        " and degrees "+ graph.currPieChartStrtAngle[ind]+" and " + graph.currPieChartFinAngle[ind]);

                //Prev Version of drawing arcs
                //org_canvas.drawArc(0, 0, pieDiameter, pieDiameter, graph.currPieChartStrtAngle[ind],//30*t
                //        graph.currPieChartFinAngle[ind]-graph.currPieChartStrtAngle[ind], true, paint);

                org_canvas.drawArc(pieCenterX-pieRadius, pieCenterY-pieRadius, pieCenterX+pieRadius, pieCenterY+pieRadius,
                        graph.prevPieChartStrtAngle[ind] * (1 - t) + graph.currPieChartStrtAngle[ind] * t,//30*t
                        graph.prevPieChartFinAngle[ind] * (1 - t) + graph.currPieChartFinAngle[ind] *
                                t - graph.prevPieChartStrtAngle[ind] * (1 - t) - graph.currPieChartStrtAngle[ind] * t, true, paint);


            }

            paint.setColor(Color.rgb(240, 240, 240));
            org_canvas.drawArc(pieCenterX-pieRadius*0.85f, pieCenterY-pieRadius*0.85f,
                    pieCenterX+pieRadius*0.85f, pieCenterY+pieRadius*0.85f,
                    0, 360, true, paint);
        }

    };

    */

    public void plotPieChart(Canvas org_canvas, int[] values_org, int[] colors, String[] names){
        int[] values = new int[values_org.length];
        strtAngle = new ArrayList<Float>();
        finAngle  = new ArrayList<Float>();
        value     = new ArrayList<Float>();
        sectorName = new ArrayList<String>();
        canvas = org_canvas;
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        int pieRadius = (int)(Math.min(canvas.getHeight(), canvas.getWidth())/2f);
        radiusTo=pieRadius;
        radiusFrom=pieRadius*0.85f;
        pieCenterX = canvas.getWidth()/2;
        pieCenterY = canvas.getHeight()/2;

        mainValue=0f;
        for(int i = 0; i<values_org.length; i++) {
            values[i] = Math.abs(values_org[i]);
            mainValue=mainValue+values_org[i];
        }

        int totalSum = 0;
        for(int i =0; i<values.length; i++) {
            totalSum=totalSum+values[i];
        }
        totalSum=Math.max(totalSum,1);

        int prevAngle = 0;

        for(int i=0; i<values.length; i++){
            paint.setColor(colors[i]);
            int addAngle = (int)((float)values[i]/(float)totalSum*360f);
            if(i==values.length-1)
                addAngle=360-prevAngle;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !CheckDrawArcMine) {
                canvas.drawArc(pieCenterX - pieRadius, pieCenterY - pieRadius, pieCenterX + pieRadius, pieCenterY + pieRadius,
                        prevAngle, addAngle, true, paint);
            }
            if(CheckDrawArcMine){
                drawArcMine(canvas, pieCenterX - pieRadius, pieCenterY - pieRadius, pieCenterX + pieRadius, pieCenterY + pieRadius,
                        prevAngle, addAngle, true, paint);
            }
            strtAngle.add((float) prevAngle);
            prevAngle = prevAngle+addAngle;
            finAngle.add((float)prevAngle);
            value.add((float) values_org[i]);
            sectorName.add(names[i]);
        }

        paint.setColor(Color.rgb(240, 240, 240));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && !CheckDrawArcMine)
            canvas.drawArc(pieCenterX-pieRadius*0.85f, pieCenterY-pieRadius*0.85f,
                    pieCenterX+pieRadius*0.85f, pieCenterY+pieRadius*0.85f,
                    0, 360, true, paint);
        if(CheckDrawArcMine){
            drawArcMine(canvas, pieCenterX - pieRadius * 0.85f, pieCenterY - pieRadius * 0.85f,
                    pieCenterX + pieRadius * 0.85f, pieCenterY + pieRadius * 0.85f,
                    0, 360, true, paint);
        }
        plotLabel(sectorNameClicked,sectorValueClicked);
    }
    public boolean onTouchEventPieChart(@NonNull MotionEvent event) {
        canvasMovingStartingPoint = new PointF((int) event.getX(), (int) event.getY());
        lastBackgroundPosition = backgroundPosition;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //check all sectors for pressing a button
                if(strtAngle.size()>0)
                    checkSectorPressed(event.getX()-pieCenterX,event.getY()-pieCenterY);

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

    public void checkSectorPressed(float x, float y) {
        float dist = (float)Math.sqrt(x*x + y*y);
        float angl = 0f;
        //calculate an angle
        if(x>0) {
            angl = (float) Math.atan(y/x) / 3.1415f * 180f;
            if(angl<0)
                angl=angl+360;
        }
        else {
            angl = (float) Math.atan(y / (-x)) / 3.1415f * 180f;
            angl = 180 - angl;
        }

        for (int i = 0; i < strtAngle.size(); i++) {

            if(dist>radiusFrom && dist<radiusTo) {
                if(angl>strtAngle.get(i) && angl<finAngle.get(i)) {

                    sectorNameClicked = sectorName.get(i);
                    sectorValueClicked = value.get(i);
                    toDrawTime = System.currentTimeMillis();
                    plotLabel(sectorNameClicked,sectorValueClicked);
                }
            }
        }



    }

    // plots a sector name on a click
    public void plotLabel(String s,float vl) {
        float t = System.currentTimeMillis()-toDrawTime;
        timeLeftToPlot=(int)t;


        if(t<msToPlot) {
            Paint pnt = new Paint();
            pnt.setColor(Color.BLACK);
            if(t<255) {pnt.setAlpha((int) t);}
            else {pnt.setAlpha(255);}
            pnt.setTextSize(50);

            //cut string so that it fits inside a circle
            while (pnt.measureText(s+"..")>2*radiusFrom){
                s=s.substring(0,s.length()-3);
                s=s+"..";
            }
            pnt.measureText(s);
            canvas.drawText(s, pieCenterX - pnt.measureText(s) / 2, pieCenterY, pnt);
            //canvas.drawText(Float.toString(sectorValueClicked) + "€",
            //        pieCenterX - pnt.measureText(Float.toString(sectorValueClicked) + "€") / 2, pieCenterY+50, pnt);
            canvas.drawText(Float.toString(sectorValueClicked) + " РУБ.",
                    pieCenterX - pnt.measureText(Float.toString(sectorValueClicked) + " РУБ.") / 2, pieCenterY+50, pnt);

        }
        else{
            s=mainLabel;
            if(mainLabel.equals("Expenses"))
                s = "Расходы";
            if(mainLabel.equals("Balance"))
                s = "Баланс";
            if(mainLabel.equals("Income"))
                s = "Доходы";
            Paint pnt = new Paint();
            pnt.setColor(Color.BLACK);
            if(t<255) {pnt.setAlpha((int) t);}
            else {pnt.setAlpha(255);}
            pnt.setTextSize(50);

            //cut string so that it fits inside a circle
            while (pnt.measureText(s+"..")>2*radiusFrom){
                s=s.substring(0,s.length()-3);
                s=s+"..";
            }
            pnt.measureText(s);
            canvas.drawText(s, pieCenterX - pnt.measureText(s) / 2, pieCenterY, pnt);
            //canvas.drawText(mainValue + "€",
            //        pieCenterX - pnt.measureText(Float.toString(mainValue) + "€") / 2, pieCenterY + 50, pnt);
            canvas.drawText(mainValue + " РУБ.",
                    pieCenterX - pnt.measureText(Float.toString(mainValue) + " РУБ.") / 2, pieCenterY + 50, pnt);
        }


    }

    void drawArcMine(Canvas cnvs,float left, float top, float right, float bottom, float strtAngl,float finAngl, boolean bln, Paint pnt) {
        float centerX=(right+left)/2f;
        float centerY=(top+bottom)/2f;
        float r = (right-left)/2f;
        Path wallpath = new Path();
        wallpath.reset(); // only needed when reusing this path for a new build
        wallpath.moveTo(centerX, centerY);
        for(float t =strtAngl; t<=strtAngl+finAngl; t++){


            wallpath.lineTo((float)(centerX + r * Math.cos(Math.toRadians(t))),
                            (float)(centerY + r * Math.sin(Math.toRadians(t))));


        }
        wallpath.close();//wallpath.lineTo(centerX, centerY);
        //pnt.setStyle(Paint.Style.STROKE);
        cnvs.drawPath(wallpath, pnt);
    }
}