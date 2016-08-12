package com.gii.maxflow;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;

/**
 * Created by onion on 03.01.16.
 */

public class GraphBundleType {

    CircleNode selectedCircle;
    CircleNode meCircle;

    ArrayList<CircleNode> allNodes = new ArrayList<CircleNode>();
    ArrayList<CircleNode> rootNodes = new ArrayList<CircleNode>();
    ArrayList<CircleNode> moneyNodes = new ArrayList<CircleNode>();


    public ArrayList<Circle> allCircles = new ArrayList<Circle>();

    public ArrayList<CircleNode> hiddenCircleNodes = new ArrayList<CircleNode>();

    final long dayMS = (1000*60*60*24);
    final long secondMS = 1000;
    final long tenMinMS = 10 * 60 * 1000;
    final long weekMS=dayMS*7;

    GraphBundleType() {
        allNodes = new ArrayList<CircleNode>();
        rootNodes = new ArrayList<CircleNode>();
        moneyNodes = new ArrayList<CircleNode>();
        hiddenCircleNodes = new ArrayList<CircleNode>();

        allCircles  = new ArrayList<Circle>();

    }

    public void fillTheGraph(ArrayList<Circle> circles, ArrayList<Operation> operations, Properties properties, Circle slctCircle,
                             int leftPage, int rightPage) {

        allCircles= new ArrayList<Circle>();

        if(allNodes==null)
            allNodes = new ArrayList<CircleNode>();

        if(moneyNodes==null)
            moneyNodes = new ArrayList<CircleNode>();

        //find all circles responsible for money in one or another form
        for (CircleNode circleNode:allNodes)
            if (circleNode.node.isMyMoney() && !moneyNodes.contains(circleNode))
                moneyNodes.add(circleNode);

        //find all the CircleNodes and fill the ArrayList "allNodes"
        for(Circle crc:circles){
            boolean alreadyInAllNodes = false;
            for(CircleNode circleNode:allNodes)
                if(circleNode.node.id.equals(crc.id))
                    alreadyInAllNodes = true;
            if (!alreadyInAllNodes)
                allNodes.add(new CircleNode(crc) );
        }

        for(CircleNode circleNode:allNodes) {
            circleNode.specifyNode(circles, allNodes, operations,moneyNodes);
            /*
            if(circleNode.node.name.equals("Mensa")) {
                if (circleNode.parent == null)
                    Log.e("error", "Mensa has no parent ");
                else
                    Log.e("error", "Mensa has parent with id " + circleNode.parent.name+" and name " + circleNode.parent.name);
            }
            */
        }
        //find all circles responsible for money in one or another form
        for (CircleNode circleNode:allNodes)
            if (circleNode.node.isMyMoney() && !moneyNodes.contains(circleNode))
                moneyNodes.add(circleNode);

        for(CircleNode circleNode:allNodes)
            circleNode.specifyNode(circles, allNodes, operations,moneyNodes);


        //initialize selected circle
        if(selectedCircle==null)
        {
            for(CircleNode circleNode:allNodes)
                circleNode.isHidden=false;
        }
        else {
            if (!selectedCircle.node.id.equals(slctCircle.id))
                for (CircleNode circleNode : allNodes)
                    circleNode.isHidden = false;
        }


        for(CircleNode circleNode:allNodes) {
            if (circleNode.node.id.equals(slctCircle.id))
                selectedCircle=circleNode;
            hiddenCircleNodes = new ArrayList<CircleNode>();
        }



        //find circle comprising all the assets
        for (CircleNode circleNode:allNodes)
            if (circleNode.node.name.equals("Me"))
                meCircle = circleNode;


        //remove nodes if circle has been deleted.  Here we have to use iterators
        for(Iterator<CircleNode> circleNodeIterator = allNodes.iterator(); circleNodeIterator.hasNext();) {
            CircleNode currentCircleNode = circleNodeIterator.next();
            if(currentCircleNode.node.deleted)
                circleNodeIterator.remove();
        }

    }

    public void plotLinearChart(Canvas canvas, LinearChart linearChart, ArrayList<Operation> operations,
                                Date dateFrom, Date dateTo, Graphics graphics, PlotChartMenu plotChartMenu,
                                ListOfElements listOfElements) {
        dateFrom = adoptDate(dateFrom,linearChart.buttonSelected,false);

        dateTo = adoptDate(dateTo,linearChart.buttonSelected,true);


        //generate colors and names
        ArrayList<CircleNode> elements = new ArrayList<CircleNode>();
        if(!moneyNodes.contains(selectedCircle)) {
            elements = new ArrayList<CircleNode>();
            if (!selectedCircle.isHidden)
                elements.add(selectedCircle);
            for (CircleNode circleNode : allNodes)
                if (circleNode.parentNode != null) {
                    boolean allParentsAreMaximized = true;
                    CircleNode currCircleNode = circleNode;
                    while (currCircleNode.parentNode != null) {
                        currCircleNode = currCircleNode.parentNode;
                        if (!currCircleNode.showChildren)
                            allParentsAreMaximized = false;
                    }
                    if (circleNode.hasAncestor(selectedCircle, allNodes) && !circleNode.isHidden && allParentsAreMaximized)
                        elements.add(circleNode);
                } else {
                    if (circleNode.hasAncestor(selectedCircle, allNodes) && !circleNode.isHidden)
                        elements.add(circleNode);
                }
        }
        if(moneyNodes.contains(selectedCircle)) {
            elements = new ArrayList<CircleNode>();
            for (CircleNode circleNode : allNodes) {
                boolean allParentsAreMaximized = true;
                CircleNode currCircleNode = circleNode;
                while(currCircleNode.parentNode!=null){
                    currCircleNode=currCircleNode.parentNode;
                    if(!currCircleNode.showChildren)
                        allParentsAreMaximized=false;
                }
                if (!circleNode.isHidden && allParentsAreMaximized && !moneyNodes.contains(circleNode))
                    elements.add(circleNode);
            }
        }

        int n = elements.size();
        int[] colors = new int[n];
        String[] names = new String[n];
        int k = 0;

        for (CircleNode circleNode : elements){
            colors[k] = (graphics.circleColor[circleNode.node.color]).getColor();
            names[k] = circleNode.node.name;
            k = k + 1;
        }

        Date minDate = new Date(dateFrom.getTime());
        Date maxDate = new Date(dateTo.getTime());

        for (Operation opr : operations) {
            if (opr.getDate().before(minDate))
                minDate=opr.getDate();
            if (opr.getDate().after(maxDate))
                maxDate=opr.getDate();
        }

        Date leftDate = new Date();
        Date rightDate = new Date();
        ArrayList<Date> verticalDashes = new ArrayList<Date>();

        int m=0;
        leftDate = adoptDate(dateFrom,linearChart.buttonSelected,false);
        while(leftDate.getTime()<dateTo.getTime()){
            rightDate = adoptDate(leftDate,linearChart.buttonSelected,true);
            leftDate.setTime(rightDate.getTime()+1000*60);
            leftDate = adoptDate(leftDate,linearChart.buttonSelected,false);

            m+=1;
        }
        float[][] M = new float[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++)
                M[i][j] = 0;
        }

        int indx=0;

        //leftDate.setTime(dateFrom.getTime());
        leftDate = adoptDate(dateFrom,linearChart.buttonSelected,false);


        while(leftDate.getTime()<dateTo.getTime()){
            Date newDate = new Date();
            newDate.setTime(leftDate.getTime());
            verticalDashes.add(newDate);
            rightDate.setTime(adoptDate(leftDate, linearChart.buttonSelected, true).getTime());
            for (CircleNode circleNode : elements) {
                Float val=0f;
                if(plotChartMenu.inRectActivated){
                    val = circleNode.getValueOfOutcomingOperations(leftDate.getTime(), rightDate.getTime());
                }
                if(plotChartMenu.outRectActivated){
                    val = circleNode.getValueOfIncomingOperations(leftDate.getTime(), rightDate.getTime());
                }
                if(plotChartMenu.generalModeActivated){
                    val = circleNode.getValueOfOutcomingOperations(leftDate.getTime(), rightDate.getTime())
                            - circleNode.getValueOfIncomingOperations(leftDate.getTime(), rightDate.getTime());
                }
                M[elements.indexOf(circleNode)][indx] =
                        val;

            }

            leftDate.setTime(rightDate.getTime() + 1000 * 2000);
            leftDate = adoptDate(leftDate,linearChart.buttonSelected,false);
            indx+=1;
        }
        //verticalDashes.add(rightDate);


        int[][] M_ = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                M_[i][j] = (int) M[i][j];


        linearChart.plotLinearChart(canvas, M_, colors, dateFrom.getTime(), dateTo.getTime(), names, verticalDashes, "");
    }


    public void plotHistChart(Canvas canvas, HistChart histChart, ArrayList<Operation> operations,
                              Date dateFrom, Date dateTo, Graphics graphics, PlotChartMenu plotChartMenu,
                              ListOfElements listOfElements) {


        //Log.d("message","Previous dateFrom = " + (new SimpleDateFormat().format(dateFrom)));
        dateFrom = adoptDate(dateFrom,histChart.buttonSelected,false);
        //Log.d("message","New dateFrom = " + (new SimpleDateFormat().format(dateFrom)));

        //Log.d("message","Previous dateTo = " + (new SimpleDateFormat().format(dateTo)));
        dateTo = adoptDate(dateTo,histChart.buttonSelected,true);
        //Log.d("message","New dateTo = " + (new SimpleDateFormat().format(dateTo)));


        //generate colors and names
        ArrayList<CircleNode> elements = new ArrayList<CircleNode>();
        if(!moneyNodes.contains(selectedCircle)) {
            elements = new ArrayList<CircleNode>();
            if (!selectedCircle.isHidden)
                elements.add(selectedCircle);
            for (CircleNode circleNode : allNodes)
                if (circleNode.parentNode != null) {
                    boolean allParentsAreMaximized = true;
                    CircleNode currCircleNode = circleNode;
                    while (currCircleNode.parentNode != null) {
                        currCircleNode = currCircleNode.parentNode;
                        if (!currCircleNode.showChildren)
                            allParentsAreMaximized = false;
                    }
                    if (circleNode.hasAncestor(selectedCircle, allNodes) && !circleNode.isHidden && allParentsAreMaximized)
                        elements.add(circleNode);
                } else {
                    if (circleNode.hasAncestor(selectedCircle, allNodes) && !circleNode.isHidden)
                        elements.add(circleNode);
                }
        }
        if(moneyNodes.contains(selectedCircle)) {
            elements = new ArrayList<CircleNode>();
            for (CircleNode circleNode : allNodes) {
                boolean allParentsAreMaximized = true;
                CircleNode currCircleNode = circleNode;
                while(currCircleNode.parentNode!=null){
                    currCircleNode=currCircleNode.parentNode;
                    if(!currCircleNode.showChildren)
                        allParentsAreMaximized=false;
                }
                if (!circleNode.isHidden && allParentsAreMaximized && !moneyNodes.contains(circleNode))
                    elements.add(circleNode);
            }
        }

        int n = elements.size();
        int[] colors = new int[n];
        String[] names = new String[n];
        int k = 0;

        for (CircleNode circleNode : elements){
            colors[k] = (graphics.circleColor[circleNode.node.color]).getColor();
            names[k] = circleNode.node.name;
            k = k + 1;
        }

        Date minDate = new Date(dateFrom.getTime());
        Date maxDate = new Date(dateTo.getTime());




        for (Operation opr : operations) {
            if (opr.getDate().before(minDate))
                minDate=opr.getDate();
            if (opr.getDate().after(maxDate))
                maxDate=opr.getDate();
        }


        Date leftDate = new Date();
        Date rightDate = new Date();
        ArrayList<Date> verticalDashes = new ArrayList<Date>();

        int m=0;
        leftDate = adoptDate(dateFrom,histChart.buttonSelected,false);
        while(leftDate.getTime()<dateTo.getTime()){
            rightDate = adoptDate(leftDate,histChart.buttonSelected,true);
            leftDate.setTime(rightDate.getTime()+1000*60);
            leftDate = adoptDate(leftDate,histChart.buttonSelected,false);

            m+=1;
        }
        float[][] M = new float[n][m];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++)
                M[i][j] = 0;
        }


        int indx=0;

        //leftDate.setTime(dateFrom.getTime());
        leftDate = adoptDate(dateFrom,histChart.buttonSelected,false);


        while(leftDate.getTime()<dateTo.getTime()){
            Date newDate = new Date();
            newDate.setTime(leftDate.getTime());
            verticalDashes.add(newDate);
            rightDate.setTime(adoptDate(leftDate, histChart.buttonSelected, true).getTime());

            /*
            Log.d("message", "leftDate = " + (new SimpleDateFormat().format(leftDate)));
            Log.d("message", "rightDate = " + (new SimpleDateFormat().format(rightDate)));
            Log.d("message", "but the last dash has time " + (new SimpleDateFormat().format(verticalDashes.get(verticalDashes.size() - 1))));
            */
            for (CircleNode circleNode : elements) {
                Float val=0f;
                if(plotChartMenu.inRectActivated){
                    val = circleNode.getValueOfOutcomingOperations(leftDate.getTime(), rightDate.getTime());
                }
                if(plotChartMenu.outRectActivated){
                    val = circleNode.getValueOfIncomingOperations(leftDate.getTime(), rightDate.getTime());
                }
                if(plotChartMenu.generalModeActivated){
                    val = circleNode.getValueOfOutcomingOperations(leftDate.getTime(), rightDate.getTime())
                            - circleNode.getValueOfIncomingOperations(leftDate.getTime(), rightDate.getTime());
                }
                M[elements.indexOf(circleNode)][indx] =
                        val;

            }

            leftDate.setTime(rightDate.getTime() + 1000 * 2000);
            leftDate = adoptDate(leftDate,histChart.buttonSelected,false);
            indx+=1;
        }
        //verticalDashes.add(rightDate);
        /*
        for(Date dt:verticalDashes){
            Log.d("message","dt = " +(new SimpleDateFormat().format(dt)));
        }
        */
        /*
        for(long currTime = dateFrom.getTime(); currTime<dateTo.getTime(); currTime+=dayMS){
            for (CircleNode circleNode : elements) {
                Float val=0f;
                if(plotChartMenu.inRectActivated){
                    val = circleNode.getValueOfOutcomingOperations(currTime, currTime+dayMS);
                }
                if(plotChartMenu.outRectActivated){
                    val = circleNode.getValueOfIncomingOperations(currTime, currTime+dayMS);
                }
                if(plotChartMenu.generalModeActivated){
                    val = circleNode.getValueOfOutcomingOperations(currTime, currTime+dayMS)
                            - circleNode.getValueOfIncomingOperations(currTime, currTime+dayMS);;
                }
                M[elements.indexOf(circleNode)][indx] =
                        val;

            }
            indx+=1;
        }
        */

        int[][] M_ = new int[n][m];
        for (int i = 0; i < n; i++)
            for (int j = 0; j < m; j++)
                M_[i][j] = (int) M[i][j];


        histChart.plotHistChart(canvas, M_, colors, dateFrom.getTime(), dateTo.getTime(), names, verticalDashes, "");
    }


    public void plotPieChart(Canvas canvas, PieChart pieChart, ArrayList<Operation> operations,
                             Date dateFrom, Date dateTo, Graphics graphics, PlotChartMenu plotChartMenu,
                             ListOfElements listOfElements) {

        //generate colors and names
        ArrayList<CircleNode> elements = new ArrayList<CircleNode>();
        if(!moneyNodes.contains(selectedCircle)) {
            elements = new ArrayList<CircleNode>();
            if (!selectedCircle.isHidden)
                elements.add(selectedCircle);
            for (CircleNode circleNode : allNodes)
                if (circleNode.parentNode != null) {
                    boolean allParentsAreMaximized = true;
                    CircleNode currCircleNode = circleNode;
                    while (currCircleNode.parentNode != null) {
                        currCircleNode = currCircleNode.parentNode;
                        if (!currCircleNode.showChildren)
                            allParentsAreMaximized = false;
                    }
                    if (circleNode.hasAncestor(selectedCircle, allNodes) && !circleNode.isHidden && allParentsAreMaximized)
                        elements.add(circleNode);
                } else {
                    if (circleNode.hasAncestor(selectedCircle, allNodes) && !circleNode.isHidden)
                        elements.add(circleNode);
                }
        }
        if(moneyNodes.contains(selectedCircle)) {
            elements = new ArrayList<CircleNode>();
            for (CircleNode circleNode : allNodes) {
                boolean allParentsAreMaximized = true;
                CircleNode currCircleNode = circleNode;
                while(currCircleNode.parentNode!=null){
                    currCircleNode=currCircleNode.parentNode;
                    if(!currCircleNode.showChildren)
                        allParentsAreMaximized=false;
                }
                if (!circleNode.isHidden && allParentsAreMaximized && !moneyNodes.contains(circleNode))
                    elements.add(circleNode);
            }
        }

        int n = elements.size();
        int[] colors = new int[n];
        String[] names = new String[n];
        int[] values = new int[n];

        int k = 0;

        for (CircleNode circleNode : elements){
            colors[k] = (graphics.circleColor[circleNode.node.color]).getColor();
            names[k] = circleNode.node.name;
            k = k + 1;
        }



        for (CircleNode circleNode : elements) {
            Float val=0f;
            if(plotChartMenu.inRectActivated){
                val = circleNode.getValueOfOutcomingOperations(dateFrom.getTime(),dateTo.getTime());
            }
            if(plotChartMenu.outRectActivated){
                val = circleNode.getValueOfIncomingOperations(dateFrom.getTime(),dateTo.getTime());
            }
            if(plotChartMenu.generalModeActivated){
                val = circleNode.getValueOfOutcomingOperations(dateFrom.getTime(),dateTo.getTime())
                        - circleNode.getValueOfIncomingOperations(dateFrom.getTime(),dateTo.getTime());;
            }
            values[elements.indexOf(circleNode)] = val.intValue();
        }

        //values = new int[]{10, 20, 40};
        //colors = new int[]{Color.RED,Color.GREEN,Color.BLUE};
        //names = new String[]{"Food","Car","Appartment" };

        pieChart.mainLabel=selectedCircle.node.name;
        if(selectedCircle.node.isMyMoney()) {
            if(plotChartMenu.inRectActivated){
                pieChart.mainLabel="Income";
            }
            if(plotChartMenu.outRectActivated){
                pieChart.mainLabel="Expenses";
            }
            if(plotChartMenu.generalModeActivated){
                pieChart.mainLabel="Balance";
            }
        }
        pieChart.plotPieChart(canvas, values, colors, names);

    }

    public void plotListOfElements(ListOfElements listOfElements, Canvas canvas, PlotChartMenu plotChartMenu,
                                   Graphics graphics,Date dateFrom, Date dateTo){

        if (plotChartMenu.inRectActivated)
            listOfElements.plotListStart(canvas, selectedCircle, moneyNodes, allNodes, graphics, 10, "out",dateFrom,dateTo);

        if (plotChartMenu.outRectActivated)
            listOfElements.plotListStart(canvas, selectedCircle, moneyNodes, allNodes, graphics, 10, "in",dateFrom,dateTo);

        if (plotChartMenu.generalModeActivated)
            listOfElements.plotListStart(canvas, selectedCircle, moneyNodes, allNodes, graphics, 10, "both",dateFrom,dateTo);

    }

    Date adoptDate(Date dateArg, String mode, boolean direction){
        //parameter direction = true accounts for shifting the date in the future, and false - to the past
        //now set date to the very beginning of the day/week/month/3month
        Date date = new Date();
        date.setTime(dateArg.getTime());
        if(!direction){
            if(mode.equals("D")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 00);
                calendar.set(Calendar.MINUTE, 00);
                calendar.set(Calendar.SECOND, 00);
                calendar.set(Calendar.MILLISECOND, 001);
                //Log.d("message", "D before calendar.getTime");
                date.setTime(calendar.getTime().getTime());
                //Log.d("message", "D after calendar.getTime");
            }
            if(mode.equals("W")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 00);
                calendar.set(Calendar.MINUTE, 00);
                calendar.set(Calendar.SECOND, 00);
                calendar.set(Calendar.MILLISECOND, 001);
                //calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
                //calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMinimum(Calendar.DAY_OF_WEEK));
                while(calendar.get(Calendar.DAY_OF_WEEK)>1)
                    calendar.setTime(new Date(calendar.getTime().getTime()-dayMS));

                //Log.d("message", "W before calendar.getTime");
                //Log.d("message", "CUrrent day of week = " +calendar.get(Calendar.DAY_OF_WEEK));
                date.setTime(calendar.getTime().getTime());
                //Log.d("message", "W after calendar.getTime");
            }
            if(mode.equals("M") || mode.equals("3M")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 00);
                calendar.set(Calendar.MINUTE, 00);
                calendar.set(Calendar.SECOND, 00);
                calendar.set(Calendar.MILLISECOND, 001);
                calendar.set(Calendar.DAY_OF_MONTH, 01);
                //Log.d("message", "M before calendar.getTime");
                date.setTime(calendar.getTime().getTime());
                //Log.d("message", "M after calendar.getTime");
            }

            if(mode.equals("Y")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 00);
                calendar.set(Calendar.MINUTE, 00);
                calendar.set(Calendar.SECOND, 00);
                calendar.set(Calendar.MILLISECOND, 001);
                calendar.set(Calendar.DAY_OF_MONTH,01);
                calendar.set(Calendar.MONTH, Calendar.JANUARY);
                //Log.d("message", "M before calendar.getTime");
                date.setTime(calendar.getTime().getTime());
                //Log.d("message", "M after calendar.getTime");
            }

        }

        if(direction){
            if(mode.equals("D")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                //Log.d("message", "D before calendar.getTime");
                date.setTime(calendar.getTime().getTime());
                //Log.d("message", "D after calendar.getTime");
            }
            if(mode.equals("W")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                while(calendar.get(Calendar.DAY_OF_WEEK)<7)
                    calendar.setTime(new Date(calendar.getTime().getTime()+dayMS));


                //calendar.set(Calendar.DAY_OF_WEEK, calendar.getActualMaximum(Calendar.DAY_OF_WEEK));
                //calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
                //Log.d("message", "W before calendar.getTime");
                //Log.d("message", "CUrrent day of week = " +calendar.get(Calendar.DAY_OF_WEEK));
                date.setTime(calendar.getTime().getTime());
                //Log.d("message", "W after calendar.getTime");
            }
            if(mode.equals("M") || mode.equals("3M")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                //Log.d("message", "M before calendar.getTime");
                date.setTime(calendar.getTime().getTime());
                //Log.d("message", "M after calendar.getTime");
            }
            if(mode.equals("Y")) {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                calendar.set(Calendar.MILLISECOND, 999);
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
                calendar.set(Calendar.MONTH, Calendar.DECEMBER);
                //Log.d("message", "M before calendar.getTime");
                date.setTime(calendar.getTime().getTime());
                //Log.d("message", "M after calendar.getTime");
            }


        }

        return date;
    }
}