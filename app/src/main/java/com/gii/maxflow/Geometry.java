package com.gii.maxflow;

import android.graphics.PointF;

/**
 * Created by Timur on 03-Sep-15.
 * Geometrical functions
 */
public class Geometry {
    public Geometry() {
    }
    public float distance(PointF point1, PointF point2) {
        return  ((float)(Math.sqrt((point1.x - point2.x)*(point1.x - point2.x)+
                (point1.y - point2.y)*(point1.y - point2.y))));
    }
    public static void moveToAngle(PointF inPoint, float AngleInDegree, float Distance, PointF point)
    {
        float angleInRadian = toRadian(AngleInDegree);
        point.set((float) (inPoint.x + Math.cos(angleInRadian) * Distance), (float) (inPoint.y + Math.sin(angleInRadian) * Distance));
    }
    public static float toRadian(float angleInDegree) {
        return ((float)(Math.PI / 180 * (angleInDegree)));
    }
    public static float calculateAngle(PointF startPoint, PointF endPoint)
    {
        float Angle = (float)(Math.atan((endPoint.y - startPoint.y) / (endPoint.x - startPoint.x)) / Math.PI * 180);
        if (endPoint.x < startPoint.x)
            Angle += 180;
        else if ((endPoint.x > startPoint.x) && (endPoint.y < startPoint.y))
            Angle += 360;
        if (Angle < 0)
            Angle += 360;
        return (Angle);
    }
    static PointF a1 = new PointF();
    static PointF a2 = new PointF();
    public static void centerRightOffset(PointF point1, PointF point2, float offset, PointF point)
    {
        moveToAngle(point1,calculateAngle(point1,point2) + 90 ,offset,a1);
        moveToAngle(point2,calculateAngle(point1,point2) + 90 ,offset,a2);
        point.set((a1.x + a2.x)/2,(a1.y + a2.y)/2);
    }
}
