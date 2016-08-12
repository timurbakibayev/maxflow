package com.gii.maxflow;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.Calendar;

/**
 * Created by Timur on 17-Nov-15.
 */
public class Icons {
    private Canvas canvas;
    private float scaleFactor;

    private PointF backgroundPosition = new PointF(0, 0);
    private PointF lastBackgroundPosition = new PointF(0,0);
    private PointF canvasMovingStartingPoint = new PointF(0,0);
    private boolean moving;

    private Paint paintLevel1 = new Paint();

    private Paint paintLevel2 = new Paint();
    private Paint paintLevel3 = new Paint();
    private Paint paintGray = new Paint();
    public void init(Graphics graphics) {
        paintGray.setColor(Color.GRAY);
        paintLevel1.setColor(Color.rgb(100,100,250));
        paintLevel2.setColor(Color.rgb(80,80,150));
        paintLevel3.setColor(Color.rgb(30,30,100));
        paintGray.setStrokeWidth(7);
        paintLevel1.setStrokeWidth(3);
        paintLevel2.setStrokeWidth(3);
        paintLevel3.setStrokeWidth(3);
        paintGray.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLevel1.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLevel2.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLevel3.setStyle(Paint.Style.FILL_AND_STROKE);
        paintGray.setAlpha(90);
        color = -1;
        moving = false;
        backgroundPosition = new PointF(0, 0);
        pressedHere = false;
        ready = false;
        choosing = Choosing.icon;
        this.graphics = graphics;
        scaleFactor = 1;
        rectsNumber = graphics.drawableIcon.length;
        colorsNumber = graphics.circleColor.length;
        int n = Math.max(rectsNumber,colorsNumber);
        for (int i = 0; i < n; i++) {
            origRects[i] = new Rect(0,0,0,0);
            drawRects[i] = new Rect(0,0,0,0);
        }
    }

    boolean needToUpdate = false;

    float dy = 0; //vertical velocity
    float lastY0 = 0f;
    float lastY1 = 0f;
    Calendar lastY0Time = Calendar.getInstance();
    Calendar lastY1Time = Calendar.getInstance();
    public void update() {
        //return;
        if (needToUpdate) {
            backgroundPosition.set(backgroundPosition.x,backgroundPosition.y - dy);
            checkBackground();
            dy = (float)dy / 1.2f;
            if (Math.abs(dy) < 1) {
                needToUpdate = false;
            }
        }
    }

    public boolean onTouchEvent(@NonNull MotionEvent event, GII.AppState appState) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressedHere = true;
                canvasMovingStartingPoint.set((int) event.getX(), (int) event.getY());
                lastBackgroundPosition = backgroundPosition;
                dy = 0;
                lastY0 = event.getY();
                lastY1 = event.getY();
                lastY0Time = Calendar.getInstance();
                lastY1Time = Calendar.getInstance();
                needToUpdate = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedHere &&
                        Math.sqrt((canvasMovingStartingPoint.x - event.getX()) * (canvasMovingStartingPoint.x - event.getX()) +
                                (canvasMovingStartingPoint.y - event.getY()) * (canvasMovingStartingPoint.y - event.getY())) > 10)
                    moving = true;
                if (pressedHere && moving) {
                    backgroundPosition.set(lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX()), lastBackgroundPosition.y + (canvasMovingStartingPoint.y - event.getY()));
                    backgroundPosition = new PointF(lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX()), lastBackgroundPosition.y + (canvasMovingStartingPoint.y - event.getY()));
                    checkBackground();
                }
                dy = 0;
                lastY0 = lastY1;
                lastY0Time.setTime(lastY1Time.getTime());
                lastY1 = event.getY();
                lastY1Time = Calendar.getInstance();
                needToUpdate = false;
                break;
            case MotionEvent.ACTION_UP:
                if (pressedHere && !moving) {
                    click(event.getX(), event.getY());
                }
                if (pressedHere && moving) {
                    dy = (lastY1-lastY0) / (lastY1Time.getTimeInMillis() - lastY0Time.getTimeInMillis()) * 100;
                    needToUpdate = true;
                }
                moving = false;
                break;
            default:
                return false;
        }
        return true;
    }


    private void checkBackground() {
        int n = 0;
        if (choosing == Choosing.icon)
            n = rectsNumber;
        if (choosing == Choosing.color)
            n = colorsNumber;
        Rect border = new Rect(origRects[0]);
        for (int i = 0; i < n; i++) {
            if (origRects[i].right > border.right)
                border.set(border.left,border.top, origRects[i].right,border.bottom);
            if (origRects[i].bottom > border.bottom)
                border.set(border.left,border.top,border.right, origRects[i].bottom);
        }
        if (border.bottom - backgroundPosition.y < graphics.canvasHeight)
            backgroundPosition.y = border.bottom - graphics.canvasHeight;
        if (backgroundPosition.y < 0)
            backgroundPosition.y = 0;
    }

    private void click(float x, float y) {
        if (choosing == Choosing.icon) {
            for (int i = 0; i < rectsNumber; i++) {
                if (drawRects[i].contains((int) x, (int) y)) {

                    if (GIIApplication.gii.ref.getAuth() == null &&
                            !GIIApplication.gii.prefs.getBoolean("idkfa",false) &&
                            GIIApplication.gii.graphics.drawableIconCategory[i] < 0) {
                        final AlertDialog.Builder builder = new AlertDialog.Builder(GIIApplication.gii.activity);
                        //final AlertDialog dialog = builder.create();

                        builder//setTitle(GIIApplication.gii.activity.getString(R.string.myAppFree_Title))
                                .setMessage(GIIApplication.gii.activity.getString(R.string.icons_Message))
                                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                        //nothing here, just a button
                                    }
                                }).show();
                    } else {
                        pictureNo = i;
                        choosing = Choosing.color;
                        backgroundPosition = new PointF(0, 0);
                        if (color != -1) {
                            ready = true;
                            pressedHere = false;
                        }
                        return;
                    }
                }
            }
        }
        if (choosing == Choosing.color) {
            for (int i = 0; i < colorsNumber; i++) {
                if (drawRects[i].contains((int) x, (int) y)) {
                    color = i;
                    ready = true;
                    pressedHere = false;
                    return;
                }
            }
        }
    }

    public boolean onScale(ScaleGestureDetector detector, GII.AppState appState) {
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 4));
        if (scaleFactor <= 0.1f)
            appState = GII.AppState.idle;
        return true;
    }

    int[] catOrder = {1,2,4,3,5,0};

    protected void onDraw(Canvas canvas) {
        graphics.canvasWidth = canvas.getWidth();
        graphics.canvasHeight = canvas.getHeight();
        int diameter = Math.min((int)canvas.getWidth(),(int)canvas.getHeight())/5;
        int x = 10;
        int y = 10;
        int offset = (int)(diameter * 0.15);
        if (choosing == Choosing.icon) {
            for (int cat = 0; cat < 6; cat++) {
                for (int i = 0; i < rectsNumber; i++) {
                    if (!(GIIApplication.gii.prefs.getBoolean("prevent_color_icons",false) && ((GIIApplication.gii.graphics.drawableIconCategory[i] < 0) || (i >= 72 && i <= 117)))) {
                        if (Math.abs(GIIApplication.gii.graphics.drawableIconCategory[i]) == catOrder[cat]) {
                            if (x + diameter > graphics.canvasWidth) {
                                x = 10;
                                y += diameter;
                            }
                            origRects[i].set(x, y, x + diameter, y + diameter);
                            drawRects[i].set(x + offset, y + offset - (int) backgroundPosition.y, x + diameter - offset, y + diameter - offset - (int) backgroundPosition.y);
                            graphics.drawIcon(null, i, (i * 3) % graphics.circleColor.length, (i * 3) % graphics.circleColor.length, drawRects[i], 0, false, canvas);
                            if (GIIApplication.gii.ref.getAuth() == null &&
                                    !GIIApplication.gii.prefs.getBoolean("idkfa", false) &&
                                    GIIApplication.gii.graphics.drawableIconCategory[i] < 0) {
                                canvas.drawLine(drawRects[i].left, drawRects[i].top,
                                        drawRects[i].right, drawRects[i].bottom, paintGray);
                            }
                            x += diameter;
                        }
                    }
                }
                x = 10;
                canvas.drawLine(0,y + diameter  - (int) backgroundPosition.y,graphics.canvasWidth,y + diameter - (int) backgroundPosition.y,
                        paintLevel1);
                canvas.drawLine(0,y - 3 + diameter  - (int) backgroundPosition.y,graphics.canvasWidth,y + diameter  - (int) backgroundPosition.y - 3,
                        paintLevel2);
                canvas.drawLine(0,y + 3 + diameter - (int) backgroundPosition.y,graphics.canvasWidth,y + diameter  - (int) backgroundPosition.y + 3,
                        paintLevel3);
                y += diameter;
                //y += diameter;
            }
        }
        if (choosing == Choosing.color) {
            for (int i = 0; i < colorsNumber; i++) {
                origRects[i].set(x, y, x + diameter, y + diameter);
                drawRects[i].set(x + offset, y + offset - (int) backgroundPosition.y, x + diameter - offset, y + diameter - offset - (int) backgroundPosition.y);
                graphics.drawIcon(null, pictureNo, i,i, drawRects[i], 0.3f, false, canvas);
                x += diameter;
                if (x + diameter > graphics.canvasWidth) {
                    x = 10;
                    y += diameter;
                }
            }
        }
    }
    ScaleGestureDetector _scaleDetector;

    public Rect[] origRects = new Rect[500];
    public Rect[] drawRects = new Rect[500];
    public int rectsNumber = 0;
    public int colorsNumber = 0;
    public boolean ready = false;
    public int pictureNo = 0;
    public int color = -1;
    public GII.AppState returnAppState = GII.AppState.idle;

    public boolean pressedHere = false;

    public enum Choosing {
        icon, color
    }

    public Choosing choosing = Choosing.icon;
    private Graphics graphics;

}
