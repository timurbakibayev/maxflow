package com.gii.maxflow;

import android.graphics.Rect;

import java.util.ArrayList;

/**
 * Created by onion on 02.01.16.
 */
public class GraphKnot {
    Circle knot = null;
    ArrayList<Circle> predecessor = new ArrayList<Circle>();
    ArrayList<Circle> successor = new ArrayList<Circle>();
    GraphKnot rootedAtKnot;  //defines a list, at which current knot is rooted in the List

    int toPlot;    //plot on chart, if equal 0 - not defined yet, 1 stays for plotting and -1 for concealed element
    int alreadyIndexedInList;
    int toDisplayInList; //to display in the List or to leave transparent
    int toExpand; //to expand adjoined elements in the List
    Rect toPlotRect; //Rectangle that's filled with color and indicates whether current knot is displayed on a chart
    Rect toExpandRect; //Rectangle that "contains" text written in the List
    float toPlotValue;
    int colorIndex;


    public GraphKnot(GraphKnot knt){            //copy constuctor
        //this.knot = new Circle(knt.knot);
        if(knt!=null) {
            if (knt.knot != null)
                this.knot = new Circle(knt.knot);

            this.rootedAtKnot = new GraphKnot(knt.rootedAtKnot);

            this.toPlot = knt.toPlot;
            this.alreadyIndexedInList = knt.alreadyIndexedInList;
            this.toDisplayInList = knt.toDisplayInList;
            this.toExpand = knt.toExpand;
            this.toPlotRect = knt.toPlotRect;
            this.toExpandRect = knt.toExpandRect;
            this.toPlotValue = knt.toPlotValue;
            this.colorIndex = knt.colorIndex;
        }
        else
            ;

    }


    public GraphKnot(Circle crc) {
        this.knot = crc;
        this.toPlot=1;
        this.alreadyIndexedInList=0;
        this.toExpand=1;
        this.toDisplayInList=1;
        //this.toPlotValue=0;
        this.colorIndex=-1;
    }

    public boolean hasPredecessor(String crc){
        for (Circle acc:predecessor)
            if(acc.id == crc)
                return true;
        return false;
    }
    public boolean hasPredecessor(Circle crc){
        for (Circle acc:predecessor)
            if(acc.id == crc.id)
                return true;
        return false;
    }
    public boolean hasPredecessor(GraphKnot crc){
        for (Circle acc:predecessor)
            if(acc.id == crc.knot.id)
                return true;
        return false;
    }
    public boolean hasSuccessor(String crc){
        for (Circle acc:successor)
            if(acc.id == crc)
                return true;
        return false;
    }
    public boolean hasSuccessor(Circle crc){
        for (Circle acc:successor)
            if(acc.id == crc.id)
                return true;
        return false;
    }
    public boolean hasSuccessor(GraphKnot crc){
        for (Circle acc:successor)
            if(acc.id == crc.knot.id)
                return true;
        return false;
    }
}