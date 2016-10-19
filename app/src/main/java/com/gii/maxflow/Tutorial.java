package com.gii.maxflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Timur_hnimdvi on 18-Oct-16.
 */
public class Tutorial extends View {

    Context context;
    GII gii;
    public int animationStep = 0;
    public Timer timer = new Timer();
    public int step;
    private Circle circleLeft = new Circle();
    private Circle circleRight = new Circle();
    private Rect rectangleLeft = new Rect(0,0,100,100);
    private Rect rectangleRight = new Rect(0,0,100,100);
    private Point fingerPosition = new Point(0,0);
    private boolean fingerPressed = false;

    public static String[] stepTitles;

    private boolean stepReady = false;

    public Tutorial(Context context) {
        super(context);
        this.context = context;
    }

    public Tutorial(Context context, GII gii, int step) {
        super(context);
        this.step = step;
        this.context = context;
        this.gii = gii;
        stepTitles = new String[]{context.getString(R.string.step_salary),
                context.getString(R.string.step_atm),
                context.getString(R.string.step_rent),
                context.getString(R.string.step_other_spending),
                context.getString(R.string.step_new_circle)
        };
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    update();
                    postInvalidate();
                } catch (Exception e) {}

            }
        }, 0, 50);
        if (step == 1)
            x = 300;
    }

    private boolean approach(Point fingerPosition, Point fingerInitPosition, Point goalPoint) {
        fingerPosition.set(fingerPosition.x + (goalPoint.x - fingerInitPosition.x)/20,
                fingerPosition.y + (goalPoint.y - fingerInitPosition.y) / 20);
        return ((fingerPosition.x - goalPoint.x) * (fingerPosition.x - goalPoint.x) +
                (fingerPosition.y - goalPoint.y) * (fingerPosition.y - goalPoint.y) < 100);
    }

    int x = 10;
    int counter = 0;
    private void update() {
        switch (animationStep) {
            case 0:
                if (approach(fingerPosition, fingerInitPosition, new Point(rectangleLeft.centerX(), rectangleLeft.centerY())))
                    animationStep++;
                    counter = 0;
                break;
            case 1:
                counter ++;
                if (counter > 10)
                    fingerPressed = true;
                if (counter > 15)
                    animationStep++;
                break;
            case 2:
                if (approach(fingerPosition, new Point(rectangleLeft.centerX(), rectangleLeft.centerY()),
                        new Point(rectangleRight.centerX(), rectangleRight.centerY())))
                    animationStep++;
                break;
        }
    }

    public Tutorial(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public Tutorial(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.context = context;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!stepReady)
            prepareStep(canvas);
        stepReady = true;

        //circles:
        gii.graphics.nameFont.setTextSize(40);
        if (circleLeft != null && !circleLeft.name.equals(""))
            gii.graphics.drawIcon(circleLeft,0,3,3,rectangleLeft,0,false,canvas);
        gii.graphics.nameFont.setTextSize(40);
        if (circleRight != null && !circleRight.name.equals(""))
            gii.graphics.drawIcon(circleRight,0,3,3,rectangleRight,0,false,canvas);
        //finger
        int fingerSize = canvas.getHeight() / 10;

        if (!fingerPressed) {
            gii.graphics.fingerIcon.setBounds(fingerPosition.x - fingerSize, fingerPosition.y - fingerSize/7,
                    fingerPosition.x + fingerSize, fingerPosition.y + fingerSize * 3 - fingerSize/7);
            gii.graphics.fingerIcon.draw(canvas);
        }
        else {
            gii.graphics.fingerHoldIcon.setBounds(fingerPosition.x - fingerSize, fingerPosition.y - fingerSize/7,
                    fingerPosition.x + fingerSize, fingerPosition.y + fingerSize * 3 - fingerSize/7);
            gii.graphics.fingerHoldIcon.draw(canvas);
        }
    }

    Point fingerInitPosition;
    void prepareStep(Canvas canvas) {
        int oneThird = canvas.getHeight() / 3;
        int offset = (canvas.getWidth() - canvas.getHeight()) / 2;
        rectangleLeft.set(0 + offset,oneThird - oneThird/2,oneThird + offset,2 * oneThird - oneThird/2);
        rectangleRight.set(2 * oneThird + offset, oneThird - oneThird/2, 3 * oneThird + offset, 2 * oneThird - oneThird/2);
        fingerInitPosition = new Point((int)(oneThird * 1.5) + offset, oneThird * 2);
        fingerPosition.set(fingerInitPosition.x,fingerInitPosition.y);
        if (step == 0) { //Getting salary
            circleLeft.name = context.getString(R.string.circle_job);
            circleLeft.nameTextWidth = 0;
            circleRight.name = context.getString(R.string.circle_card);
            circleRight.nameTextWidth = 0;
        }
        if (step == 1) { //ATM
            circleLeft.name = context.getString(R.string.circle_card);
            circleLeft.nameTextWidth = 0;
            circleRight.name = context.getString(R.string.circle_cash);
            circleRight.nameTextWidth = 0;
        }
        if (step == 2) {
            circleLeft.name = context.getString(R.string.circle_card);
            circleLeft.nameTextWidth = 0;
            circleRight.name = context.getString(R.string.circle_house);
            circleRight.nameTextWidth = 0;
        }
        if (step == 3) {
            circleLeft.name = context.getString(R.string.circle_cash);
            circleLeft.nameTextWidth = 0;
            circleRight.name = context.getString(R.string.circle_cinema);
            circleRight.nameTextWidth = 0;
        }
    }


    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //if (gi123i.checkButtons(new PointF(event.getX(), event.getY())))
                //    break;
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
