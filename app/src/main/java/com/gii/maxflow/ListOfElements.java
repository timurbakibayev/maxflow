package com.gii.maxflow;


import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by onion on 03.01.16.
 */
public class ListOfElements {
    Canvas ourCanvas;
    int topOffset=20;
    private int toPlotListTopOffset=40;
    private int rectCircle = 45;
    float generalListScrolledPosition=0f;
    float scrolledY = 0f;


    ArrayList<Rect> Icons = new ArrayList<Rect>();
    ArrayList<Rect> toMaximizeArrow = new ArrayList<Rect>();
    ArrayList<CircleNode> Elements = new ArrayList<CircleNode>();

    private PointF backgroundPosition = new PointF(0, 0);
    private PointF lastBackgroundPosition = new PointF(0,0);
    private PointF canvasMovingStartingPoint = new PointF(0,0);

    public ListOfElements() {
        super();
    }


    public void plotListGraph(Canvas canvas, GII.AppState appState, ArrayList<Circle> circle, ArrayList<Operation> displayedOperation,
                              Graph graph, Paint paint){
        int xOffset = 40;
        int totalSum = 0;
        toPlotListTopOffset=60;
        int amountNonZeroCircles=0;
        for (Circle acc : circle )
            if (acc.amount != 0) {
                totalSum += Math.abs(acc.amount);
                amountNonZeroCircles=amountNonZeroCircles+1;
            }
        for (GraphKnot acc : graph.rootKnots)
            plotListRecursivelyGraph(canvas, acc.knot, xOffset, circle, displayedOperation, graph, paint);
    }

    private void plotListRecursivelyGraph(Canvas canvas, Circle currCircle,int horOffset, ArrayList<Circle> circle,
                                          ArrayList<Operation> displayedOperation, Graph graph, Paint paint){
        int k = 0;
        int currCircleIndex = 0;
        int yOffset = toPlotListTopOffset - (int)generalListScrolledPosition;

        if (graph.findKnotIndexById(currCircle.id)!=-1)
            currCircleIndex = graph.findKnotIndexById(currCircle.id);
        else
            return;
        graph.allKnots.get(currCircleIndex).alreadyIndexedInList=1;
        graph.allKnots.get(currCircleIndex).toPlotRect = new Rect(horOffset, yOffset - rectCircle,
                horOffset + rectCircle, yOffset);

        paint.setColor(graph.allKnots.get(currCircleIndex).colorIndex);
        paint.setAlpha(255);
        if (graph.toHideList.contains(graph.allKnots.get(currCircleIndex).knot))
            paint.setAlpha(30);
        canvas.drawRect(horOffset, yOffset - rectCircle, horOffset + rectCircle, yOffset, paint);
        paint.setTextSize((int) (rectCircle * 1.0));
        paint.setColor(Color.BLACK);
        canvas.drawText(currCircle.name, horOffset + (int) (rectCircle * 1.1), yOffset, paint);
        toPlotListTopOffset=toPlotListTopOffset+(int)(rectCircle*1.4);

        for (int j=0; j<graph.allKnots.size(); j++) {
            //now we find, so to say, main parent for an element j
            int maxIndexForJ=0;
            float maxWeightForJ=0f;
            for (int i = 0; i < graph.allKnots.size(); i++)
                if (graph.AdjMatrix[i][j] - graph.AdjMatrix[j][i] > maxWeightForJ ) {
                    maxWeightForJ = graph.AdjMatrix[i][j] - graph.AdjMatrix[j][i];
                    maxIndexForJ=i;
                }
            if (maxIndexForJ==currCircleIndex && maxWeightForJ>0f && graph.allKnots.get(j).alreadyIndexedInList==0) {
                plotListRecursivelyGraph(canvas, graph.allKnots.get(j).knot, horOffset + (int) (rectCircle * 0.7),
                        circle, displayedOperation, graph, paint);
            }
        }
    }


    public boolean onTouchEventListOfElements(@NonNull MotionEvent event,Rect canvasRect,
                                              TimeLine timeLine, int toDrawPortion, ArrayList<CircleNode> allNodes,
                                              PlotChart plotChart) {

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                canvasMovingStartingPoint = new PointF((int) event.getX(), (int) event.getY());
                lastBackgroundPosition = backgroundPosition;
                //check whether an icon was selected
                for (Rect rect : Icons) {
                    if (rect.contains((int) event.getX(), (int) event.getY())) {
                        int index=Icons.indexOf(rect);
                        Log.d("message", "clicked on " + Elements.get(index).node.name);
                        Elements.get(index).isHidden=!Elements.get(index).isHidden;
                        plotChart.recalcListOfElements=true;
                    }
                }

                for (Rect rect : toMaximizeArrow) {
                    if (rect.contains((int) event.getX(), (int) event.getY())) {
                        int index=toMaximizeArrow.indexOf(rect);
                        Log.d("message", "clicked on " + Elements.get(index).node.name+ ", its children are hidden now");
                        Elements.get(index).showChildren=!Elements.get(index).showChildren;
                        plotChart.recalcListOfElements=true;
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:

                if (canvasRect.contains((int) event.getX(), (int) event.getY()) || true) {
                    backgroundPosition = new PointF((int) (lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX())),
                            (int) (lastBackgroundPosition.y + (canvasMovingStartingPoint.y - event.getY())));
                    //backgroundPosition.y = Math.min(backgroundPosition.y, graph.allKnots.size() * rectCircle * 1.4f
                    //        - canvasRect.height() + 20);
                    backgroundPosition.y = Math.max(backgroundPosition.y, 0);
                    backgroundPosition.y = Math.min(backgroundPosition.y, Math.max(0,(Elements.size()+1)*2*rectCircle-canvasRect.height()));
                    generalListScrolledPosition = backgroundPosition.y;
                    Log.d("message", "generalListScrolledPosition = " + generalListScrolledPosition);
                    plotChart.scrollListOfElements = true;
                }

                break;
            case MotionEvent.ACTION_UP:
                break;
            default:
                return false;
        }
        return true;
    }


    public void plotListStart(Canvas canvas, CircleNode mainCircle, ArrayList<CircleNode> allCircleNodes,
                              Graphics graphics,int leftOffset,String mode, Date dateFrom, Date dateTo){
        Icons=new ArrayList<Rect>();
        Elements=new ArrayList<CircleNode>();
        toMaximizeArrow = new ArrayList<Rect>();
        topOffset=40;

        if(mainCircle.node.name.equals("Me")) {
            //plotList(canvas,mainCircle,graphics,leftOffset,mode);
            //topOffset = topOffset + rectCircle * 2;
            for(CircleNode circleNode:allCircleNodes){
                if(circleNode.parentNode==null && !circleNode.node.name.equals("Me")) {
                    plotList(canvas, circleNode, graphics, leftOffset, mode,dateFrom,dateTo);
                    topOffset = topOffset + rectCircle * 2;
                }
            }
        }
        else
            plotList(canvas,mainCircle,graphics,leftOffset,mode,dateFrom,dateTo);
    }

    public void plotListStart(Canvas canvas, CircleNode mainCircle, ArrayList<CircleNode> moneyNodes,
                              ArrayList<CircleNode> allCircleNodes, Graphics graphics,int leftOffset,String mode,
                              Date dateFrom, Date dateTo){
        Icons=new ArrayList<Rect>();
        Elements=new ArrayList<CircleNode>();
        toMaximizeArrow = new ArrayList<Rect>();
        topOffset=40;
        for (CircleNode circleNode: moneyNodes)
            Log.d("message",circleNode.node.name+" is a moneyNode");
        if(moneyNodes.contains(mainCircle)) {
            //plotList(canvas,mainCircle,graphics,leftOffset,mode);
            //topOffset = topOffset + rectCircle * 2;
            for(CircleNode circleNode:allCircleNodes){
                if(circleNode.parentNode==null && !moneyNodes.contains(circleNode)) {
                    plotList(canvas, circleNode, graphics, leftOffset, mode,dateFrom,dateTo);
                    topOffset = topOffset + rectCircle * 2;
                }
            }
        }
        else
            plotList(canvas,mainCircle,graphics,leftOffset,mode,dateFrom,dateTo);
    }

    public void plotList(Canvas canvas, CircleNode mainCircle, Graphics graphics,int leftOffset,String mode,
                         Date dateFrom, Date dateTo){

        Paint paint = new Paint();
        paint.setTextSize(rectCircle);
        paint.setColor(Color.GRAY);


        paint.setColor(graphics.circleColor[mainCircle.node.color].getColor());
        //replace 1.8 with 1.5 or 1.3 in the next line to make icons even bigger
        Rect currRect=new Rect(rectCircle-(int)(rectCircle/1.8f)+leftOffset,topOffset-(int)(rectCircle/1.8f) - (int)generalListScrolledPosition,
                rectCircle+(int)(rectCircle/1.8f)+leftOffset,topOffset+(int)(rectCircle/1.8f) - (int)generalListScrolledPosition);
        Icons.add(currRect);
        Elements.add(mainCircle);
        //Bitmap bitmap = graphics.circleBitmap[mainCircle.node.picture];
        //Rect rectSrc = new Rect(0,0,bitmap.getWidth(),bitmap.getHeight());
        canvas.drawCircle(currRect.centerX(), currRect.centerY(), currRect.width() / 1.5f, paint);
        //canvas.drawBitmap(graphics.circleBitmap[mainCircle.node.picture], rectSrc, currRect, paint);
        graphics.drawableIcon[mainCircle.node.picture % graphics.drawableIcon.length].setBounds(currRect);
        graphics.drawableIcon[mainCircle.node.picture % graphics.drawableIcon.length].draw(canvas);

        //separating line
        paint.setColor(Color.LTGRAY);
        canvas.drawLine(currRect.centerX()+rectCircle,currRect.bottom+rectCircle/3,
                        canvas.getWidth()-rectCircle/10,currRect.bottom+rectCircle/3,paint);

        //draw check-marks on those, that are not hidden
        Rect rectSrc = new Rect(0,0,graphics.checkMark.getWidth(),graphics.checkMark.getHeight());
        if(!mainCircle.isHidden){
            Rect rectDst = new Rect(currRect.centerX(),currRect.centerY(),
                    currRect.centerX() + currRect.width()/4*3,currRect.centerY() + currRect.height()/4*3);
            canvas.drawBitmap(graphics.checkMark,rectSrc,rectDst,paint);
        }

        String s = mainCircle.node.name;
        while (currRect.centerX() + rectCircle+paint.measureText(s+".."+" unclass.")>(canvas.getWidth()*5/4)){
            s=s.substring(0,s.length()-3);
            s=s+"..";
        }
        if(mainCircle.children.size()!=0 && mainCircle.showChildren)
            s=s+" unclass.";
        paint.setColor(Color.BLACK);
        canvas. drawText(s, currRect.centerX() + rectCircle, currRect.centerY() + rectCircle / 2, paint);


        //draw arrows that control revealing children
        Rect toMaximizeRect = new Rect(currRect.centerX()+rectCircle+(int)paint.measureText(s),currRect.centerY()-(int)(rectCircle*3/4f),
                currRect.centerX()+rectCircle+(int)paint.measureText(s)+rectCircle*2,currRect.centerY() + rectCircle );
        float prevStrokeWidth = paint.getStrokeWidth();
        if(mainCircle.children.size()!=0){
            float lngth = rectCircle/3;
            paint.setColor(Color.DKGRAY);
            paint.setStrokeWidth(3);
            if(!mainCircle.showChildren) {
                canvas.drawLine(toMaximizeRect.centerX() - lngth , toMaximizeRect.centerY() - lngth+lngth/2,
                        toMaximizeRect.centerX(), toMaximizeRect.centerY()+lngth/2, paint);
                canvas.drawLine(toMaximizeRect.centerX() + lngth, toMaximizeRect.centerY() - lngth+lngth/2,
                        toMaximizeRect.centerX(), toMaximizeRect.centerY()+lngth/2, paint);
            }
            if(mainCircle.showChildren) {
                canvas.drawLine(toMaximizeRect.centerX() - lngth, toMaximizeRect.centerY() + lngth-lngth/2,
                        toMaximizeRect.centerX(), toMaximizeRect.centerY()-lngth/2, paint);
                canvas.drawLine(toMaximizeRect.centerX() + lngth, toMaximizeRect.centerY() + lngth-lngth/2,
                        toMaximizeRect.centerX(), toMaximizeRect.centerY()-lngth/2, paint);
            }
        }
        paint.setStrokeWidth(prevStrokeWidth);

        toMaximizeArrow.add(toMaximizeRect);


        Float value=0f;
        if(mode.equals("in"))
            value= -mainCircle.getValueOfIncomingOperations(dateFrom.getTime(),dateTo.getTime());
        if(mode.equals("out"))
            value= mainCircle.getValueOfOutcomingOperations(dateFrom.getTime(),dateTo.getTime());
        if(mode.equals("both"))
            value= mainCircle.getValueOfOutcomingOperations(dateFrom.getTime(),dateTo.getTime())-
                    mainCircle.getValueOfIncomingOperations(dateFrom.getTime(),dateTo.getTime());

        String val = value.toString();
        /*Change color to red if its expense, green if its income, and grey otherwise
        if(value>0)
            paint.setColor(Color.GREEN);
        if(value<0)
            paint.setColor(Color.RED);
        if(value==0)
            paint.setColor(Color.GRAY);
            */


        canvas.drawText(val, canvas.getWidth() - paint.measureText(val), currRect.centerY() + rectCircle / 2, paint);
        if(mainCircle.showChildren) {
            for (CircleNode crc : mainCircle.children) {
                topOffset += rectCircle * 2;
                plotList(canvas, crc, graphics, leftOffset + rectCircle,mode,dateFrom,dateTo);
            }
        }
    }

}