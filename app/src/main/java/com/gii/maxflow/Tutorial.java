package com.gii.maxflow;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import java.util.HashMap;
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
    private Circle circleMiddle = new Circle();
    private Rect rectangleLeft = new Rect(0,0,100,100);
    private Rect rectangleRight = new Rect(0,0,100,100);
    private Rect rectangleMiddle = new Rect(0,0,100,100);
    private Point fingerPosition = new Point(0,0);
    private Point dragFrom;
    private boolean fingerPressed = false;

    Properties properties = new Properties();

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
        if (step < 4)
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        update();
                        postInvalidate();
                    } catch (Exception e) {}

                }
            }, 0, 50);
        else
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        updateNewCircle();
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
                if (approach(fingerPosition, fingerInitPosition, new Point(rectangleLeft.centerX(), rectangleLeft.centerY()))) {
                    animationStep++;
                    counter = 0;
                    dragFrom = new Point(fingerPosition);
                }
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
                        new Point(rectangleRight.centerX(), rectangleRight.centerY()))) {
                    animationStep++;
                    counter = 0;
                }
                break;
            case 3:
                counter ++;
                if (counter > 10) {
                    circleLeft.addDisplayAmount("USD",-50);
                    circleRight.addDisplayAmount("USD",50);
                    circleLeft.theIconParams = "redraw";
                    circleRight.theIconParams = "redraw";
                    fingerPressed = false;
                }
                if (counter > 15)
                    animationStep++;
                break;
            case 4:
                if (approach(fingerPosition, new Point(rectangleRight.centerX(), rectangleRight.centerY()),
                        new Point(fingerInitPosition.x, fingerInitPosition.y))) {
                    animationStep++;
                    counter = 0;
                }
                break;
            case 5:
                counter ++;
                if (counter > 20) {
                    animationStep = 0;
                }
                break;
        }
    }

    private void updateNewCircle() {
        switch (animationStep) {
            case 0: //go to middle
                if (approach(fingerPosition, fingerInitPosition, new Point(rectangleMiddle.centerX(), rectangleMiddle.centerY()))) {
                    animationStep++;
                    counter = 0;
                }
                break;
            case 1: //wait and press button
                counter ++;
                if (counter > 10) {
                    fingerPressed = true;
                    counter = 0;
                    animationStep++;
                }
                break;
            case 2: //animate new circle
                counter ++;
                if (counter >= 20) {
                    animationStep++;
                    counter = 0;
                    fingerPressed = false;
                }
                break;
            case 3: //show new circle
                counter ++;
                circleMiddle.visible = true;
                if (counter > 20) {
                    animationStep++;
                    counter = 0;
                }
                break;
            case 4: //middle -> left
                if (approach(fingerPosition, new Point(rectangleMiddle.centerX(), rectangleMiddle.centerY()),
                        new Point(rectangleLeft.centerX(), rectangleLeft.centerY()))) {
                    animationStep++;
                    dragFrom = new Point(fingerPosition);
                    counter = 0;
                }
                break;
            case 5: //wait and press
                counter ++;
                if (counter > 10)
                    fingerPressed = true;
                if (counter > 15)
                    animationStep++;
                break;
            case 6: //drag to middle
                if (approach(fingerPosition, new Point(rectangleLeft.centerX(), rectangleLeft.centerY()),
                        new Point(rectangleMiddle.centerX(), rectangleMiddle.centerY()))) {
                    animationStep++;
                    counter = 0;
                }
                break;
            case 7: //wait
                counter ++;
                if (counter > 10) { //this will fire 6 times for 10 <= counter <= 15
                    fingerPressed = false;
                    circleLeft.addDisplayAmount("USD",-50);
                    circleMiddle.addDisplayAmount("USD",50);
                    circleLeft.theIconParams = "redraw";
                    circleMiddle.theIconParams = "redraw";
                }
                if (counter > 15)
                    animationStep++;
                break;
            case 8: //middle -> init
                if (approach(fingerPosition, new Point(rectangleMiddle.centerX(), rectangleMiddle.centerY()),
                        fingerInitPosition)) {
                    animationStep++;
                    counter = 0;
                }
                break;
            case 9:
                counter ++;
                if (counter > 20) {
                    animationStep = 0;
                    circleLeft.displayAmount = new HashMap<>();
                    circleLeft.addDisplayAmount("USD",1000);
                    circleMiddle.displayAmount = new HashMap<>();
                    circleLeft.theIconParams = "redraw";
                    circleMiddle.theIconParams = "redraw";
                    circleMiddle.visible = false;
                }
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

        gii.graphics.drawBackground(canvas);

        //circles:
        gii.graphics.mainFont.setTextSize(40);
        if (circleLeft != null && !circleLeft.name.equals(""))
            gii.graphics.drawIcon(circleLeft,0,3,3,rectangleLeft,0,false,canvas);
        gii.graphics.mainFont.setTextSize(40);
        if (circleRight != null && !circleRight.name.equals(""))
            gii.graphics.drawIcon(circleRight,0,3,3,rectangleRight,0,false,canvas);

        if (circleMiddle != null && !circleMiddle.name.equals("") && circleMiddle.visible)
            gii.graphics.drawIcon(circleMiddle,0,3,3,rectangleMiddle,0,false,canvas);
        //finger
        int fingerSize = canvas.getHeight() / 10;

        if (!fingerPressed) {
            fingerSize = canvas.getHeight() / 7;
            gii.graphics.fingerIcon.setBounds(fingerPosition.x - fingerSize, fingerPosition.y - fingerSize,
                    fingerPosition.x + fingerSize, fingerPosition.y + fingerSize * 3 - fingerSize);
            gii.graphics.fingerIcon.draw(canvas);
        }
        else {
            if (step < 4 || animationStep != 2) {
                gii.graphics.drawConnection(dragFrom.x, dragFrom.y, fingerPosition.x, fingerPosition.y,
                        gii.graphics.circleColor[circleLeft.color % gii.graphics.circleColor.length], properties, canvas);
                gii.graphics.dollarCoinIcon.setBounds(fingerPosition.x - fingerSize, fingerPosition.y - fingerSize,
                        fingerPosition.x + fingerSize, fingerPosition.y + fingerSize);
                gii.graphics.dollarCoinIcon.draw(canvas);
            } else if (animationStep == 2) {
                canvas.save();
                gii.graphics.tmpPath.reset();
                gii.graphics.tmpPath.moveTo(fingerPosition.x, fingerPosition.y);
                for (float j = 0 + (float) Math.PI; j < (float) counter / 20 * Math.PI * 2 ; j = j + 0.1f)
                    gii.graphics.tmpPath.lineTo((float) (fingerPosition.x + Math.sin(j) * 100),
                            (float) (fingerPosition.y + Math.cos(j) * 100));
                gii.graphics.tmpPath.lineTo(fingerPosition.x, fingerPosition.y);
                for (float j = 0 + (float) Math.PI; j < (float) counter / 20 * Math.PI * 2 ; j = j + 0.1f)
                    gii.graphics.tmpPath.lineTo((float) (fingerPosition.x + Math.sin(j + Math.PI) * 100),
                            (float) (fingerPosition.y + Math.cos(j + Math.PI) * 100));
                canvas.clipPath(gii.graphics.tmpPath);
                gii.graphics.drawIcon(null,GIIApplication.gii.abstractCorrectionCircle.picture,1,1,
                        new Rect((int)(fingerPosition.x - 100),(int)(fingerPosition.y - 100),
                                (int)(fingerPosition.x + 100), (int)(fingerPosition.y + 100)),0,false,canvas
                );
                canvas.restore();
            }
            gii.graphics.fingerIcon.setBounds(fingerPosition.x - fingerSize, fingerPosition.y - fingerSize,
                    fingerPosition.x + fingerSize, fingerPosition.y + fingerSize * 3 - fingerSize);
            gii.graphics.fingerIcon.draw(canvas);
        }
    }

    Point fingerInitPosition;
    void prepareStep(Canvas canvas) {
        int oneThird = canvas.getHeight() / 3;
        int offset = (canvas.getWidth() - canvas.getHeight()) / 2;
        rectangleLeft.set(0 + offset,oneThird - oneThird/2,oneThird + offset,2 * oneThird - oneThird/2);
        rectangleRight.set(2 * oneThird + offset, oneThird - oneThird/2, 3 * oneThird + offset, 2 * oneThird - oneThird/2);
        rectangleMiddle.set((int)(oneThird * 1.5) + offset - oneThird/2, oneThird*2 - oneThird/2,
                (int)(oneThird * 1.5) + offset + oneThird/2, oneThird*2 + oneThird/2);
        //dragFrom = new Point((int)(oneThird * 1.5) + offset, oneThird*2);
        fingerInitPosition = new Point((int)(oneThird * 1.5) + offset, oneThird * 3);
        fingerPosition.set(fingerInitPosition.x,fingerInitPosition.y);
        properties.scaleFactor = 1;
        if (step == 0) { //Getting a salary
            circleLeft.name = context.getString(R.string.circle_job);
            circleLeft.color = 5;
            circleLeft.nameTextWidth = 0;

            circleRight.name = context.getString(R.string.circle_card);
            circleRight.color = 12;
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
        if (step == 4) {
            circleLeft.name = context.getString(R.string.circle_cash);
            circleLeft.nameTextWidth = 0;
            circleRight.name = context.getString(R.string.circle_card);
            circleRight.nameTextWidth = 0;
            circleMiddle.name = context.getString(R.string.circle_food);
            circleMiddle.visible = false;
            circleMiddle.nameTextWidth = 0;
        }
        circleLeft.addDisplayAmount("USD",1000);
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
