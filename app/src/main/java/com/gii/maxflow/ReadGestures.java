package com.gii.maxflow;

import android.graphics.Point;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by Timur on 21-Aug-15.
 */
public class ReadGestures {
    ArrayList<Point> gesture;
    public String gestureType = "";
    public Point center = new Point(0,0);
    public float radius = 0;
    public ReadGestures(ArrayList<Point> gesture) {
        this.gesture = gesture;
        checkGesture();
    }

    private void checkGesture() {
        gestureType = "";

        //find center
        Point minPoint = gesture.get(0);
        Point maxPoint = gesture.get(0);
        int minX45 = gesture.get(0).x+gesture.get(0).y;
        int maxX45 = gesture.get(0).x+gesture.get(0).y;
        int minX135 = gesture.get(0).x-gesture.get(0).y;
        int maxX135 = gesture.get(0).x-gesture.get(0).y;

        for (Iterator<Point> i = gesture.iterator(); i.hasNext(); ) {
            Point point = i.next();

            //0 and 90 degrees
            if (minPoint.x > point.x)
                minPoint = new Point(point.x,minPoint.y);
            if (minPoint.y > point.y)
                minPoint = new Point(minPoint.x,point.y);
            if (maxPoint.x < point.x)
                maxPoint = new Point(point.x,maxPoint.y);
            if (maxPoint.y < point.y)
                maxPoint = new Point(maxPoint.x,point.y);

            //45 and 135 degrees
            if (minX45 > point.x + point.y)
                minX45 = point.x + point.y;
            if (maxX45 < point.x + point.y)
                maxX45 = point.x + point.y;
            if (minX135 > point.x - point.y)
                minX135 = point.x - point.y;
            if (maxX135 < point.x - point.y)
                maxX135 = point.x - point.y;
        }
        if (minPoint.y == maxPoint.y)
            maxPoint = new Point(maxPoint.x,maxPoint.y + 1);
        float ratio = (float)(maxPoint.x - minPoint.x)/(maxPoint.y - minPoint.y);
        float diagonalRatio = (float)(maxX45 - minX45)/(maxX135 - minX135);
        if ((Math.abs(ratio-1) < 0.3) && (Math.abs(diagonalRatio-1) < 0.3))
        {
            center = new Point((maxPoint.x + minPoint.x)/2,(maxPoint.y + minPoint.y)/2);
            radius = maxPoint.x - center.x;
            if (radius > 10)
                gestureType = "circle";
        }

    }

}
