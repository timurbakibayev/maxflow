package com.gii.maxflow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Map;

/**
 * Created by Timur on 15-Nov-15.
 * Moved all drawings here
 */
public class Graphics {

    boolean popupNeedsToUpdate = false;
    int popupPosition = 0;
    int popupPositionStartShow = 0;
    public Rect popupRectangle = new Rect(0,0,0,0);
    public Operation popupOperation;
    Circle popupCircleFrom;
    Circle popupCircleTo;
    OperationListWindow operationListWindow;
    GII gii;
    public Graphics(GII gii) {
        this.gii = gii;
        operationListWindow = new OperationListWindow(gii);
    }
    public String[] monthName;

    Rect bgRect0 = new Rect(150,150,150,150);
    Rect bgRect1 = new Rect(150,150,150,150);
    Rect rect0 = new Rect(0,0,0,0);
    Rect rect1 = new Rect(0,0,0,0);
    Rect rect2 = new Rect(0,0,0,0);
    PointF moved = new PointF(0,0);
    PointF moved1 = new PointF(0,0);
    PointF point0 = new PointF(0,0);
    PointF point1 = new PointF(0,0);
    PointF point11 = new PointF(0,0);
    PointF point2 = new PointF(0,0);
    PointF point3 = new PointF(0,0);
    Path tmpPath = new Path();
    Path tmpPath1 = new Path();

    Rect pageButtonsRectangle; //change month icon position

    //static Bitmap[] circleBitmap = new Bitmap[3]; //circle icons
    Bitmap[] colorIcon; //color circles (color0 to color15)
    Paint[] circleColor = new Paint[16];
    int[] circleColorBrightness = new int[20];

    Bitmap backgroundBitmap;


    Paint bgColor = new Paint();
    Paint gray = new Paint();

    Paint nameFont = new Paint();
    Paint dkBlueCircle = new Paint();
    Paint dkBlue = new Paint();
    Paint dkBlue1 = new Paint();
    Paint chain = new Paint();
    Paint darkTransparent = new Paint();
    Paint circlePaint = new Paint();
    Paint circleSelected = new Paint();
    Paint bottomButtonTextPaint;
    Paint selectorPaint;

    Paint white = new Paint();
    Paint whiteUnder = new Paint();
    Paint whiteTransp = new Paint();
    Paint blueTransp = new Paint();
    Paint greenTransp = new Paint();
    Paint greenToRedTransp = new Paint();

    Paint arrowPaint = new Paint();
    Paint fatArrowPaint = new Paint();


    ArrayList<CanvasButton> bottomButton;

    public PointF canvasCenter = new PointF(0, 0); //center of canvas, screen coordinates

    public float canvasHeight = 0,canvasWidth=0; //canvas height and width,
    //for performance reasons

    public static DecimalFormat df = new DecimalFormat("#.##");

    float menuPosition = 0; //the position of the bottom menu. When equals to canvas.bottom it is hidden
    PointF menuSize = new PointF(50, 50); //the size of the bottom menu

    float editModeShakeOffset = 0;
    boolean editModeShakeRight = true;

    //private int drawCounter = 0;

    boolean pdfMode = false;
    public void drawTheStuff(Canvas canvas, GII.AppState appState, Properties properties, ArrayList<Circle> circle, ArrayList<Operation> displayedOperation,
                             String selectedId, String moveIntoId, PointF moveXY) {

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();

        pdfMode = false;
        float pdfScale = 1;
        PointF pdfOffSet = new PointF(0,0);
        float lastScaleFactor = properties.scaleFactor;
        PointF lastBackgroundPosition = properties.backgroundPosition;

        if (selectedId.equals("-1")) {
            pdfMode = true;
            Rect bounds = new Rect(-1,-1,1,1);

            //detect bounds to rescale;
            for (Circle _circle : circle) {
                if (!_circle.deleted && _circle.visible) {
                    float radius = _circle.radius;
                    mapToScreen(_circle.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && _circle.id.equals(selectedId), moveXY), properties, moved);
                    rect0.set((int) (moved.x - (radius * properties.scaleFactor)), (int) (moved.y - radius * properties.scaleFactor),
                            (int) (moved.x + radius * properties.scaleFactor), (int) (moved.y + radius * properties.scaleFactor));
                    if (bounds.left == -1) {
                        bounds = new Rect(rect0);
                    } else {
                        if (bounds.left > rect0.left)
                            bounds.left = rect0.left;
                        if (bounds.right < rect0.right)
                            bounds.right = rect0.right;
                        if (bounds.top > rect0.top)
                            bounds.top = rect0.top;
                        if (bounds.bottom < rect0.bottom)
                            bounds.bottom = rect0.bottom;
                    }
                }
            }
            //now rescale and detect bounds again
            properties.scaleFactor *= (float)canvas.getWidth()/bounds.width() * 0.8f;
            //if bounds.height() > (float)canvas.getWidth() .... scale down...

            bounds = new Rect(-1,-1,1,1);
            for (Circle _circle : circle) {
                if (!_circle.deleted && _circle.visible) {
                    float radius = _circle.radius;
                    mapToScreen(_circle.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && _circle.id.equals(selectedId), moveXY), properties, moved);
                    rect0.set((int) (moved.x - (radius * properties.scaleFactor)), (int) (moved.y - radius * properties.scaleFactor),
                            (int) (moved.x + radius * properties.scaleFactor), (int) (moved.y + radius * properties.scaleFactor));
                    if (bounds.left == -1) {
                        bounds = new Rect(rect0);
                    } else {
                        if (bounds.left > rect0.left)
                            bounds.left = rect0.left;
                        if (bounds.right < rect0.right)
                            bounds.right = rect0.right;
                        if (bounds.top > rect0.top)
                            bounds.top = rect0.top;
                        if (bounds.bottom < rect0.bottom)
                            bounds.bottom = rect0.bottom;
                    }
                }
            }
            properties.backgroundPosition.set(
                    properties.backgroundPosition.x + bounds.left - (float)canvas.getWidth()*0.1f,
                    properties.backgroundPosition.y + bounds.top - (float)canvas.getHeight()*0.1f
            );

        }



        //for (int i = 0; i < 100; i++) {
        //    drawable.setBounds(10 + i*10, 10 + i*10, 70 + i*10, 70 + i*10);
        //    drawable.draw(canvas);
        //}

        dkBlue.setStrokeWidth(2 * properties.scaleFactor); //4 * properties.scaleFactor
        dkBlue1.setStrokeWidth(2 * properties.scaleFactor); //4 * properties.scaleFactor
        dkBlue.setTextSize(20 * properties.scaleFactor);
        dkBlue1.setTextSize(15 * properties.scaleFactor);
        dkBlueCircle.setStrokeWidth(4 * properties.scaleFactor);
        arrowPaint.setStrokeWidth(0.5f);
        fatArrowPaint.setStrokeWidth(3);

        selectorPaint.setStrokeWidth(8 * properties.scaleFactor);

        //white.setStrokeWidth(0.8f * properties.scaleFactor);
        white.setStrokeWidth(properties.scaleFactor * 10);
        white.setTextSize(25 * properties.scaleFactor);

        whiteUnder.setStrokeWidth(7f * properties.scaleFactor);
        whiteUnder.setTextSize(20 * properties.scaleFactor);
        whiteUnder.setStrokeCap(Paint.Cap.ROUND);
        white.setFilterBitmap(false);
        //arrowPaint.setStrokeWidth(properties.scaleFactor*7);//change in drawArrow method, paint

        if (editModeShakeRight)
            editModeShakeOffset++;
        else
            editModeShakeOffset--;
        if (editModeShakeOffset < -1)
            editModeShakeRight = true;
        if (editModeShakeOffset > 1)
            editModeShakeRight = false;

        /*int backgroundZoom = 4;
        PointF zeroPoint = mapToScreen(new PointF(0,0),properties);
        PointF backgroundAtZero = mapToScreen(new PointF(backgroundBitmap.getWidth(),backgroundBitmap.getHeight()),properties);
        Point backgroundSize = new Point((int)(backgroundAtZero.x - zeroPoint.x)*backgroundZoom,(int)(backgroundAtZero.y - zeroPoint.y)*backgroundZoom);
        Point stepsBack = new Point((int)(zeroPoint.x - (((int)zeroPoint.x / backgroundSize.x)+1)*backgroundSize.x),
                (int)(zeroPoint.y - (((int)zeroPoint.y / backgroundSize.y)+1)*backgroundSize.y));

        for (int x = stepsBack.x;x < canvasWidth; x = x + backgroundSize.x)
            for (int y = stepsBack.y ; y < canvasHeight; y = y + backgroundSize.y)
                canvas.drawBitmap(backgroundBitmap, new Rect(0, 0, backgroundBitmap.getWidth(), backgroundBitmap.getHeight()),
                    new Rect(x,y,x + backgroundSize.x,y + backgroundSize.y), null);
                    */ //This was a bitmap background :(

        //canvas.drawRect(0,0,canvasWidth,canvasHeight,bgColor);

        //Draw connections to parents
        for (Circle acc : circle)
            if (acc.visible && !acc.parentId.equals("") && !acc.deleted) {
                chain.setStrokeWidth(properties.scaleFactor * 7);
                mapToScreen(acc.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && acc.id.equals(selectedId), moveXY), properties, moved);
                mapToScreen(acc.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && acc.id.equals(selectedId), moveXY), properties, moved1);
                for (Circle acc1 : circle)
                    if (acc1.id.equals(acc.parentId)) {
                        mapToScreen(acc1.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && acc1.id.equals(selectedId), moveXY), properties, moved1);
                        chain.setColor(circleColor[acc1.getColor()%circleColor.length].getColor());
                    }
                drawASolidLine(moved.x, moved.y, moved1.x, moved1.y, chain, properties, canvas);
            }


        //draw the arrows for each displayedOperation

        if (gii.prefs.getBoolean("show_operation_arrow",true)) {
            for (int i = Math.max(0, displayedOperation.size() - 50); i < displayedOperation.size(); i++) { //Shows only last 10 Arrows! xxxxxxxxxxxxxx
                Operation myOperation = displayedOperation.get(i);
                Circle fromCircle = gii.circleById(myOperation.fromCircle, circle);
                Circle toCircle =gii.circleById(myOperation.toCircle, circle);

                if (fromCircle.visible && toCircle.visible)
                    //if (!fromCircle.parentId.equals(toCircle.id) && !toCircle.parentId.equals(fromCircle.id))
                    drawArrow(canvas, fromCircle, toCircle, myOperation, arrowPaint, properties, appState, selectedId, moveXY);
            }
        }



        //Draw a circle bitmap, draw circles, show circles
        for (Circle _circle : circle) {
            if (!_circle.deleted && _circle.visible) {
                float radius = _circle.radius;
                if ((appState == GII.AppState.editMode ||
                        appState == GII.AppState.circleTouched) && _circle.id.equals(moveIntoId))
                    radius *= 2;

                mapToScreen(_circle.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && _circle.id.equals(selectedId), moveXY), properties, moved);


                if (moved.x + radius * properties.scaleFactor < 0 || moved.x - radius * properties.scaleFactor > canvas.getWidth() ||
                        moved.y - radius * properties.scaleFactor > canvas.getHeight() || moved.y + radius * properties.scaleFactor < 0)
                    continue;


                /*if (_circle.childrenId.size() > 0 && !_circle.showChildren) {
                    for (int k = 2; k > 0; k--) {
                        double offsetFolder = 7 * k * properties.scaleFactor;
                        rect0.set((int) (moved.x - (radius * properties.scaleFactor) + offsetFolder), (int) (moved.y - radius * properties.scaleFactor + offsetFolder),
                                (int) (moved.x + radius * properties.scaleFactor + offsetFolder), (int) (moved.y + radius * properties.scaleFactor + offsetFolder));
                        drawIcon(_circle.picture, k*3, rect0, canvas);
                    }
                }*/
                if (_circle.childrenId.size() > 0 && !_circle.showChildren) {
                    canvas.drawCircle(moved.x, moved.y, (int) (radius* properties.scaleFactor * 1.2), gray);
                    for (int i = 0; i < Math.min(3, _circle.childrenId.size()); i++) {
                        Circle subCircle =gii.circleById(_circle.childrenId.get(i),circle);
                        mapToScreen(_circle.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && _circle.id.equals(selectedId), moveXY), properties, moved);
                        moved1.set(moved.x + (int) (1.13 * radius * properties.scaleFactor * Math.sin(Math.PI / 5 + i * 0.3)),
                                moved.y + (int) (1.13 * radius * properties.scaleFactor * Math.cos(Math.PI / 5 + i * 0.3)));
                        //rect0.set((int) (moved1.x - (0.15*radius * properties.scaleFactor)), (int) (moved1.y - 0.15*radius * properties.scaleFactor),
                        //        (int) (moved1.x + 0.15*radius * properties.scaleFactor), (int) (moved1.y + 0.15*radius * properties.scaleFactor));
                        //drawIcon(subCircle.picture, subCircle.color,subCircle.color, rect0, canvas);
                        canvas.drawCircle(moved1.x,moved1.y,(int)(0.05*radius*properties.scaleFactor),white);
                    }
                }

                    mapToScreen(_circle.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && _circle.id.equals(selectedId), moveXY), properties, moved);
                canvas.save();
                    if (appState == GII.AppState.editMode)
                        canvas.rotate(editModeShakeOffset*5,moved.x,moved.y);
                        //moved.set(moved.x + editModeShakeOffset, moved.y);


                    rect0.set((int) (moved.x - (radius * properties.scaleFactor)), (int) (moved.y - radius * properties.scaleFactor),
                            (int) (moved.x + radius * properties.scaleFactor), (int) (moved.y + radius * properties.scaleFactor));

                    if (_circle.id.equals(selectedId)) {
                        selectorPaint.setStyle(Paint.Style.FILL_AND_STROKE);
                        selectorPaint.setShader(new RadialGradient(moved.x,moved.y,(float)(radius * properties.scaleFactor*1.63),
                                circleColor[_circle.color % circleColor.length].getColor(),Color.TRANSPARENT,
                                Shader.TileMode.CLAMP));
                        canvas.drawCircle(moved.x, moved.y, (float)(radius * properties.scaleFactor * 1.5), selectorPaint);
                        //canvas.drawCircle(moved.x + 15 * properties.scaleFactor, moved.y + 15 * properties.scaleFactor, (float)(radius * properties.scaleFactor * 1.1), gray);
                        //canvas.drawRect(moved.x-(float)(radius * properties.scaleFactor * 1.2),moved.y - (float)(radius * properties.scaleFactor * 1.2),
                        //        moved.x + (float)(radius * properties.scaleFactor * 1.2), moved.y + (float)(radius * properties.scaleFactor * 1.2),
                        //        selectorPaint);
                    }

                    int parentColor = _circle.color;
                    if (!_circle.parentId.equals(""))
                        parentColor = gii.circleById(_circle.parentId,circle).getColor();
                float fraction = 0;
                if (_circle.goalAmount != 0) {
                    fraction = Math.min(Math.abs(
                            //((!_circle.myMoney || properties.filtered) ? _circle.amount : _circle.amountTotal)
                            (_circle.amount)
                    ) / Math.abs(_circle.goalAmount), 1);
                }
                drawIcon(_circle, _circle.picture, _circle.color, parentColor, rect0, fraction, _circle.limitGoal, canvas);
                canvas.restore();

                //Show the name inside the circle
                //redefine text size depending on the circle radius and the length of the amount of the circle
                if (gii.prefs.getBoolean("show_circle_name",true)) { //was < 5
                    float nameTextWidth = _circle.nameTextWidth * properties.scaleFactor;
                    //float textSizeChange = (_circle.radius * 1.5f * properties.scaleFactor / nameTextWidth);
                    //nameFont.setTextSize(25 * properties.scaleFactor * textSizeChange);
                    nameFont.setTextSize(40 * properties.scaleFactor);
                    canvas.drawText(_circle.name, moved.x - nameTextWidth / 2, moved.y + nameFont.getTextSize() + _circle.radius * properties.scaleFactor, nameFont);
                }
            }
        }


        //draw amount, show amount
        //white.setAlpha(130);
        for (Circle acc : circle) {
            if (!acc.deleted && acc.visible) {
                mapToScreen(acc.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && acc.id.equals(selectedId), moveXY), properties,moved);
                if (!gii.doNotMove && appState == GII.AppState.editMode && acc.id.equals(selectedId)) {
                    mapToScreen(moveXY,properties,moved);
                }


                if (moved.x + acc.radius * properties.scaleFactor< 0 || moved.x - acc.radius * properties.scaleFactor > canvas.getWidth() ||
                        moved.y - acc.radius * properties.scaleFactor > canvas.getHeight() || moved.y + acc.radius * properties.scaleFactor < 0)
                    continue;

                if (acc.displayAmount.size() != 0) {
                    String amountText = "";
                    //if (!acc.myMoney || properties.filtered)
                    //    amountText = gii.df.format(acc.amount); //- dkBlue.measureText(amountText) / 2 below
                    //else
                    //    amountText = gii.df.format(acc.amountTotal);
                //if (acc.showAmount != 0) {
                //    String amountText = gii.df.format(acc.showAmount);
                    //if (!amountText.contains("."))
                    //    amountText = amountText + ".";
                    //while (amountText.length()-amountText.indexOf(".") < 3)
                    //    amountText = amountText + "0";
                    //if (acc.dxShowAmount != 0)
                    //    acc.amountTextWidth = dkBlue.measureText(amountText);
                    //float realWidth = dkBlue.measureText(amountText);

                    float movedY = (moved.y - (acc.radius * properties.scaleFactor));
                    float txtWidth = acc.amountTextWidth * properties.scaleFactor;
                    float movedX = (moved.x + (acc.radius * properties.scaleFactor * 1.1f)) - txtWidth;
                    float amWidth = txtWidth + 10 * properties.scaleFactor;
                    float amHeight = dkBlue.getTextSize()*1.2f;
                    float verticalMiddle = movedY + amHeight/2;
                    //canvas.drawRect(moved.x - 4 * properties.scaleFactor,movedY - 4 * properties.scaleFactor,moved.x + txtWidth+4* properties.scaleFactor,movedY + dkBlue.getTextSize()+6* properties.scaleFactor,dkBlue);
                    //canvas.drawRect(movedX, movedY, movedX + txtWidth, movedY + dkBlue.getTextSize() + 2 * properties.scaleFactor, whiteTransp);

                    tmpPath.reset();
                    tmpPath.moveTo(movedX - 7 * properties.scaleFactor,movedY);
                    tmpPath.lineTo(movedX, verticalMiddle);
                    tmpPath.lineTo(movedX - 7 * properties.scaleFactor, movedY + amHeight);
                    tmpPath.lineTo(movedX + amWidth, movedY + amHeight);
                    tmpPath.lineTo(movedX + amWidth + 7 * properties.scaleFactor, verticalMiddle);
                    tmpPath.lineTo(movedX + amWidth, movedY);
                    tmpPath.lineTo(movedX - 7 * properties.scaleFactor, movedY);
                    float fraction = 0;

                    if (gii.prefs.getBoolean("show_circle_amount",true)) {
                        dkBlue.setStyle(Paint.Style.STROKE);

                        /*if (gii.prefs.getBoolean("show_circle_amount_rect",false)) {
                            if (!acc.myMoney || properties.filtered) {
                                canvas.drawRect((int) movedX, (int) movedY, (int) (movedX + amWidth), (int) (movedY + amHeight), whiteTransp);
                                if (acc.goalAmount != 0 && !properties.filtered) {
                                    canvas.drawRect((int) movedX, (int) movedY, (int) (movedX + amWidth), (int) (movedY + amHeight), blueTransp);
                                    canvas.drawRect((int) movedX, (int) movedY, (int) (movedX + amWidth * fraction), (int) (movedY + amHeight), greenToRedTransp);
                                }
                                canvas.drawRect((int) movedX, (int) movedY, (int) (movedX + amWidth), (int) (movedY + amHeight), dkBlue);
                            } else {
                                canvas.drawPath(tmpPath, whiteTransp);
                                canvas.drawPath(tmpPath, dkBlue);
                                if (acc.goalAmount != 0) {
                                    canvas.drawPath(tmpPath, blueTransp);
                                    canvas.drawPath(tmpPath1, greenToRedTransp);
                                }
                            }
                        }


                        dkBlue.setStyle(Paint.Style.FILL_AND_STROKE);
                        canvas.drawText(amountText, movedX, movedY + dkBlue.getTextSize(), whiteUnder);
                        canvas.drawText(amountText, movedX, movedY + dkBlue.getTextSize(), dkBlue);*/

                        dkBlue.setStyle(Paint.Style.FILL_AND_STROKE);
                        dkBlue1.setStyle(Paint.Style.FILL_AND_STROKE);
                        int movedYOffset = 0;
                        //movedY =
                        for (Map.Entry<String, Float> entry : acc.displayAmount.entrySet()) {
                            txtWidth = acc.displayAmountTextWidth.get(entry.getKey()) * properties.scaleFactor;
                            movedX = (moved.x + (acc.radius * properties.scaleFactor * 0.7f)) - txtWidth;
                            //movedX = moved.x  - txtWidth;
                            movedYOffset++;
                            canvas.drawText(gii.df.format(entry.getValue()) , movedX, movedY + dkBlue.getTextSize() * movedYOffset, whiteUnder);
                            canvas.drawText(gii.df.format(entry.getValue()) , movedX, movedY + dkBlue.getTextSize() * movedYOffset, dkBlue);
                            //movedX = (moved.x + (acc.radius * properties.scaleFactor * 0.7f));
                            movedX = movedX + txtWidth;
                            //canvas.drawText(entry.getKey(), movedX, movedY + dkBlue.getTextSize() * movedYOffset, whiteUnder);
                            canvas.drawText(entry.getKey(), movedX, movedY + dkBlue.getTextSize() * movedYOffset, dkBlue1);
                        }
                    }
                }
            }
        }
        //canvas.drawText(gii.prefs.getString("pin",""), 70, 70, dkBlue);
        //drawCurrentUsername and file:
        //dkBlue.setTextSize(20);
        //if (!properties.firebaseUserEmail.equals(""))
            //canvas.drawText(properties.firebaseUserEmail + "/" + properties.fileName, 20, 50, dkBlue);
        //else
            //canvas.drawText("offline/" + properties.fileName, 20, 50, dkBlue);
        //drawCounter++;
        //canvas.drawText("Recalculations: " + numberOfRecalculcations, 20, 70, dkBlue);

        if (popupNeedsToUpdate)
            drawPopup(canvas);

        if (pdfMode) {
            properties.scaleFactor = lastScaleFactor;
            properties.backgroundPosition = lastBackgroundPosition;

        } else {

            dkBlue.setTextSize(25);
            whiteUnder.setTextSize(20);
            Circle acc = GIIApplication.gii.abstractTotalCircle;
            int i = 0;
            float max = 0;
            float[] measure = new float[acc.displayAmount.size()];
            for (Map.Entry<String, Float> entry : acc.displayAmount.entrySet()) {
                String textAmount = gii.df.format(entry.getValue()) + " ";
                measure[i] = dkBlue.measureText(textAmount);
                if (measure[i] > max)
                    max = measure[i];
                i++;
            }
            i = 0;
            dkBlue.setStrokeWidth(1);
            canvas.drawRect(0,0,canvas.getWidth(),30 + dkBlue.getTextSize(),whiteUnder);
            String toDisplayTotal = gii.activity.getString(R.string.you_have) + " ";
            for (Map.Entry<String, Float> entry : acc.displayAmount.entrySet()) {
                toDisplayTotal = toDisplayTotal + gii.df.format(entry.getValue()) + " " + entry.getKey();
                if (i <= acc.displayAmount.entrySet().size() - 2)
                    toDisplayTotal = toDisplayTotal + ", ";
                i++;
            }
            if (acc.displayAmount.size() > 0)
                canvas.drawText(toDisplayTotal, 10, dkBlue.getTextSize()*1.5f, dkBlue);
        }
    }

    public Paint brighter(Paint paint, int delta, int alpha) {
        int red = Math.max(Math.min(Color.red(paint.getColor())+delta,255),0);
        int green = Math.max(Math.min(Color.green(paint.getColor())+delta,255),0);
        int blue = Math.max(Math.min(Color.blue(paint.getColor())+delta,255),0);
        Paint toReturn = new Paint();
        toReturn.setColor(Color.rgb(red,green,blue));
        toReturn.setStrokeWidth(paint.getStrokeWidth());
        toReturn.setStyle(paint.getStyle());
        toReturn.setAlpha(alpha);
        return toReturn;
    }

    private String TAG = "Graphics";
    Rect srcRect = new Rect(0,0,0,0);
    Rect destRect = new Rect(0,0,0,0);
    public void drawIcon(Circle _circle, int iconBitmapNo, int iconColorNo, int parentIconColorNo, Rect bounds, float fillPercent, boolean limitGoal, Canvas canvas) {

        if (_circle == null || pdfMode) {
            moved.set(bounds.centerX(), bounds.centerY());
            if (bounds.right < 0 || bounds.left > canvas.getWidth() ||
                    bounds.top > canvas.getHeight() || bounds.bottom < 0)
                return;
            float radius = bounds.width() / 2.8f;
            canvas.drawCircle(moved.x, moved.y, (float) (radius * 1.40), circleColor[parentIconColorNo % circleColor.length]);
            canvas.drawCircle(moved.x, moved.y, (float) (radius * 1.2), white);
            canvas.drawCircle(moved.x, moved.y, (float) (radius * 1.1), circleColor[iconColorNo % circleColor.length]);
            rect1.set((int) (moved.x - (radius * 0.65)), (int) (moved.y - radius * 0.65),
                    (int) (moved.x + radius * 0.65), (int) (moved.y + radius * 0.65));
            drawableIcon[iconBitmapNo % drawableIcon.length].setBounds(rect1);
            drawableIcon[iconBitmapNo % drawableIcon.length].draw(canvas);
            return;
        }

        //TODO: make intervals for bounds.width() (increase quality by steps)
        String newParams = _circle.picture + "," + _circle.color  + "," + fillPercent;

        if (bounds.right < 0 || bounds.left > canvas.getWidth() ||
                bounds.top > canvas.getHeight() || bounds.bottom < 0)
            return;

        Rect newBounds = new Rect(0,0,bounds.width(),bounds.height());
        float difference = 0;
        if (_circle.theIcon != null)
            difference = Math.abs(bounds.width() - _circle.theIcon.getWidth()) + Math.abs(bounds.height() - _circle.theIcon.getHeight());

        if (_circle.theIcon == null || !gii.scaling && !(_circle.theIconParams.equals(newParams) && difference < 5)) {
            /*Log.e(TAG, "drawIcon: yet another icon");
            Log.e(TAG, "drawIcon: null:" + (_circle.theIcon == null));
            Log.e(TAG, "drawIcon: params:" + (_circle.theIconParams));
            Log.e(TAG, "drawIcon: newParams:" + (newParams));*/

            _circle.theIconParams = newParams;
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            if (bounds.width() < 1 || bounds.height() < 1)
                bounds.set(0,0,10,10);
            _circle.theIcon = Bitmap.createBitmap(bounds.width(), bounds.height(), conf).copy(conf,true);
            Canvas littleCanvas = new Canvas(_circle.theIcon);
            moved.set(newBounds.centerX(), newBounds.centerY());
            float radius = newBounds.width() / 2.8f;

            littleCanvas.drawCircle(moved.x, moved.y, (float) (radius * 1.40), circleColor[parentIconColorNo % circleColor.length]);
            white.setAlpha(255);
            littleCanvas.drawCircle(moved.x, moved.y, (float) (radius * 1.2), white);


            //littleCanvas.clipPath(null);

            if (fillPercent > 0) {
                //srcRect.set(0, fill_with_red.getHeight() - (int) (fill_with_red.getHeight() * fillPercent), fill_with_red.getWidth(), fill_with_red.getHeight());
                //destRect.set(newBounds.left, newBounds.bottom - (int) (newBounds.height() * fillPercent), newBounds.right, newBounds.bottom);
                //littleCanvas.drawBitmap(fill_with_red, srcRect, destRect, null);
            }

            littleCanvas.drawCircle(moved.x, moved.y, (float) (radius * 1.1), circleColor[iconColorNo % circleColor.length]);
            //rect0.set(0, 0, circleBitmap[iconBitmapNo % circleBitmap.length].getWidth(), circleBitmap[iconBitmapNo % circleBitmap.length].getHeight());
            rect1.set((int) (moved.x - (radius * 0.65)), (int) (moved.y - radius * 0.65),
                    (int) (moved.x + radius * 0.65), (int) (moved.y + radius * 0.65));

            //canvas.drawBitmap(circleBitmap[iconBitmapNo % circleBitmap.length], rect0, rect1, null);
            drawableIcon[iconBitmapNo % drawableIcon.length].setBounds(rect1);
            drawableIcon[iconBitmapNo % drawableIcon.length].draw(littleCanvas);

            if (_circle.photoString != null &&
                    !_circle.photoString.equals("")) {
                Path circlePath = new Path();
                circlePath.moveTo(moved.x + (float) Math.sin(0) * (radius * 1.2f),
                        moved.y + (float) Math.cos(0) * (radius * 1.2f));

                    for (float k = 0; k < Math.PI * 2; k = k + 0.2f) {
                        circlePath.lineTo(moved.x + (float) Math.sin(k) * (radius * 1.2f),
                                moved.y + (float) Math.cos(k) * (radius * 1.2f));
                    }
                circlePath.lineTo(moved.x + (float) Math.sin(0) * (radius * 1.2f),
                        moved.y + (float) Math.cos(0) * (radius * 1.2f));
                //littleCanvas.drawPath(percentPath,dkBlue);
                littleCanvas.clipPath(circlePath);
                Bitmap tmpBitmap = GIIApplication.decodeBase64(_circle.photoString);
                Rect cutRect = new Rect(0,0,tmpBitmap.getWidth(),tmpBitmap.getHeight());
                if (tmpBitmap.getHeight() > tmpBitmap.getWidth())
                    cutRect.set(0,tmpBitmap.getHeight()/2 - tmpBitmap.getWidth()/2,
                            tmpBitmap.getWidth(),tmpBitmap.getHeight()/2 + tmpBitmap.getWidth()/2);
                if (tmpBitmap.getHeight() < tmpBitmap.getWidth())
                    cutRect.set(tmpBitmap.getWidth()/2 - tmpBitmap.getHeight()/2,0,
                            tmpBitmap.getWidth()/2 + tmpBitmap.getHeight()/2,tmpBitmap.getHeight());
                littleCanvas.drawBitmap(tmpBitmap, cutRect,
                        new Rect((int)(moved.x - radius * 1.2f), (int)(moved.y - radius * 1.2f),
                                (int)(moved.x + radius * 1.2f), (int)(moved.y + radius * 1.2f)),null);
            }

            if (fillPercent > 0) {
                white.setAlpha(130);
                Path percentPath = new Path();
                percentPath.moveTo(moved.x,moved.y);
                if (fillPercent < 1) {
                    for (float k = (float)Math.PI/8; k < (float)Math.PI/8 + (1 - fillPercent) * Math.PI * 2; k = k + 0.2f) {
                        percentPath.lineTo(moved.x + (float) Math.sin(k) * (radius * 1.2f),
                                moved.y + (float) Math.cos(k) * (radius * 1.2f));
                        //littleCanvas.drawCircle(moved.x + (float) Math.sin(k) * (radius * 1.2f),
                        //        moved.y + (float) Math.cos(k) * (radius * 1.2f), (float) (radius * 0.1), dkBlue);
                    }
                }
                percentPath.lineTo(moved.x, moved.y);
                //littleCanvas.drawPath(percentPath,dkBlue);
                if (fillPercent < 1) {
                    littleCanvas.clipPath(percentPath);
                    littleCanvas.drawCircle(moved.x, moved.y, (float) (radius * 1.2), white);
                }
                //littleCanvas.clipRect(0, 0, newBounds.width(), moved.y + (radius * 1.2f) - (int) (radius * 1.2f * 2 * fillPercent));
                //littleCanvas = new Canvas(_circle.theIcon);
                white.setAlpha(255);
            } else {
                //littleCanvas.drawCircle(moved.x, moved.y, (float) (radius * 1.2), white);
            }
        }
        canvas.drawBitmap(_circle.theIcon, new Rect(0,0,_circle.theIcon.getWidth(),_circle.theIcon.getHeight()), bounds, null );
        moved.set(bounds.centerX(), bounds.centerY());
    }


    private void drawConnection(float x, float y, float x1, float y1, Paint chain, Properties properties, Canvas canvas) {
        //canvas.drawLine(x,y,x1,y1,chain);

        int numberOfCircles = (int)(Math.sqrt((y1-y)*(y1-y) + (x1-x)*(x1-x)) / properties.scaleFactor / 25);
        float dx = (float)((x1-x)/numberOfCircles);
        float dy = (float)((y1-y)/numberOfCircles);

        for (int i = 0; i < numberOfCircles; i++) {
            canvas.drawCircle(x + dx*i, y + dy * i, 10*properties.scaleFactor,chain);
        }
    }

    private void drawASolidLine(float x, float y, float x1, float y1, Paint chain, Properties properties, Canvas canvas) {
        canvas.drawLine(x,y,x1,y1,chain);
    }

    /**
     *
     * @param original map coordinates
     *
     */
    public void mapToScreen(PointF original, Properties properties, PointF moved) {
        moved.set((original.x * properties.scaleFactor + canvasCenter.x - properties.backgroundPosition.x),
                (original.y * properties.scaleFactor + canvasCenter.y - properties.backgroundPosition.y));
    }
    static public Drawable[] drawableIcon;
    static public Drawable backspaceIcon;
    static public Drawable calendarIcon;
    static public Drawable divideIcon;
    static public Drawable exchangeIcon;
    static Drawable noteIcon;
    static Bitmap checkMark;
    static Bitmap fill_with_red;
    static int[] drawableIconCategory;
    public void loadResources(Context context) {
        backspaceIcon = ContextCompat.getDrawable(context, R.drawable._ic_backspace_black_24dp);
        noteIcon = ContextCompat.getDrawable(context, R.drawable._ic_note_black_24dp1);
        calendarIcon = ContextCompat.getDrawable(context, R.drawable._ic_calendar_black_24);
        divideIcon = ContextCompat.getDrawable(context, R.drawable._ic_divide);
        exchangeIcon = ContextCompat.getDrawable(context, R.drawable._ic_exchange_money_black_24dp);
        GIIApplication.gii.abstractExchangeCircle.name = context.getString(R.string.circle_exchange);
        GIIApplication.gii.abstractExchangeCircle.id = "-2";
        GIIApplication.gii.abstractCorrectionCircle.id = "-2";
        GIIApplication.gii.abstractTotalCircle.id = "-2";
        GIIApplication.gii.abstractCorrectionCircle.name = context.getString(R.string.circle_correction);

        checkMark = BitmapFactory.decodeResource(context.getResources(),R.drawable.checkmark_circled_my);
        fill_with_red = BitmapFactory.decodeResource( GIIApplication.gii.activity.getResources(),R.drawable.fill_with_red);
        //Typeface moneyTypeFace = Typeface.defaultFromStyle()
        drawableIcon = new Drawable[58+14+46+2]; //0 - 71 normal, 72 - 117 - colour, 118 - normal
        drawableIconCategory = new int[drawableIcon.length];
        int iconNo = 0;
        drawableIconCategory[iconNo] = 1; drawableIcon[iconNo] = new Drawable() {
            @Override
            public void draw(Canvas canvas) {

            }

            @Override
            public void setAlpha(int alpha) {

            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {

            }

            @Override
            public int getOpacity() {
                return 0;
            }
        }; iconNo++;
        drawableIconCategory[iconNo] = 1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_account_balance_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_airplanemode_active_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_alarm_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_assistant_photo_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_attachment_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_attach_file_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_attach_money_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_build_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_camera_alt_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_casino_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 4; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_child_friendly_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_color_lens_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_content_cut_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_create_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_credit_card_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_directions_bike_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_directions_bus_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_directions_car_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_directions_subway_black_24dp); iconNo++;

        GIIApplication.gii.abstractCorrectionCircle.picture = iconNo;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_done_black_24dp); iconNo++;

        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_favorite_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 4; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_fitness_center_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_flash_on_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_format_paint_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_grade_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_home_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_important_devices_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_language_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 2; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_bar_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 2; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_cafe_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_car_wash_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 2; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_dining_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_florist_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_gas_station_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_hospital_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_hotel_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_laundry_service_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 4; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_mall_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_movies_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_parking_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_shipping_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_taxi_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_mail_outline_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_motorcycle_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_music_note_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_notifications_black_24dp2); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_pets_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_phone_android_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_power_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_school_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_search_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_settings_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 4; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_shopping_cart_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 4; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_smoking_rooms_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_traffic_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_weekend_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_work_black_24dp); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_beach_access_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_child_care_white_24px); iconNo++;
        //drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_child_friendly_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_filter_vintage_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_folder_open_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_folder_shared_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_headset_white_24px); iconNo++;
        //drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_gas_station_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_library_white_24px); iconNo++;
        //drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_local_mall_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_pool_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_security_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_speaker_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_toys_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_videogame_asset_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_watch_white_24px); iconNo++;
        drawableIconCategory[iconNo] = 5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_wb_incandescent_white_24px); iconNo++;
        //Colorful:
        drawableIconCategory[iconNo] = -1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.money_bag); iconNo++;
        drawableIconCategory[iconNo] = -3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_aeroplane); iconNo++;
        drawableIconCategory[iconNo] = -1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_bank); iconNo++;
        drawableIconCategory[iconNo] = -4; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_basket); iconNo++;
        drawableIconCategory[iconNo] = -3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_bus); iconNo++;
        drawableIconCategory[iconNo] = -1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_business_man); iconNo++;
        drawableIconCategory[iconNo] = -1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_business_woman); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_cat); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_chair); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_cigar); iconNo++;
        drawableIconCategory[iconNo] = -2; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_coffee); iconNo++;
        drawableIconCategory[iconNo] = -1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_credit_card); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_cut); iconNo++;
        drawableIconCategory[iconNo] = -2; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_dine_out); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_dog); iconNo++;
        drawableIconCategory[iconNo] = -5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_electricity); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_family); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_flower); iconNo++;
        drawableIconCategory[iconNo] = -2; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_fruits); iconNo++;
        drawableIconCategory[iconNo] = -3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_garage); iconNo++;
        drawableIconCategory[iconNo] = -3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_gasoline); iconNo++;
        drawableIconCategory[iconNo] = -2; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_hamburger); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_headphones); iconNo++;
        drawableIconCategory[iconNo] = -3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_helicopter); iconNo++;
        drawableIconCategory[iconNo] = -5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_hotel); iconNo++;
        drawableIconCategory[iconNo] = -5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_house); iconNo++;
        drawableIconCategory[iconNo] = -5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_light); iconNo++;
        drawableIconCategory[iconNo] = -1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_maestro); iconNo++;
        drawableIconCategory[iconNo] = -1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_money); iconNo++;
        drawableIconCategory[iconNo] = -5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_paint); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_phone); iconNo++;
        drawableIconCategory[iconNo] = -1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_piggy); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_pill); iconNo++;
        drawableIconCategory[iconNo] = -5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_rent); iconNo++;
        drawableIconCategory[iconNo] = -4; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_sale); iconNo++;
        drawableIconCategory[iconNo] = -4; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_shopping); iconNo++;
        drawableIconCategory[iconNo] = -4; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_store); iconNo++;
        drawableIconCategory[iconNo] = -0; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_study); iconNo++;
        drawableIconCategory[iconNo] = -4; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_sweater); iconNo++;
        drawableIconCategory[iconNo] = -3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_taxi); iconNo++;
        drawableIconCategory[iconNo] = -5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_television); iconNo++;
        drawableIconCategory[iconNo] = -3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_town_car); iconNo++;
        drawableIconCategory[iconNo] = -3; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_train); iconNo++;
        drawableIconCategory[iconNo] = -1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_visa); iconNo++;
        drawableIconCategory[iconNo] = -5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_water); iconNo++;
        drawableIconCategory[iconNo] = -5; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.p_wifi); iconNo++;
        //this came from beginning of the list
        drawableIconCategory[iconNo] = 1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable.ic_accessibility_black_24dp); iconNo++;

        GIIApplication.gii.abstractExchangeCircle.picture = iconNo;
        drawableIconCategory[iconNo] = 1; drawableIcon[iconNo] = ContextCompat.getDrawable(context, R.drawable._ic_exchange_money_black_24dp); iconNo++;


        bgColor = new Paint();
        bgColor.setColor(Color.rgb(220,250,220));
        bgColor.setStyle(Paint.Style.FILL_AND_STROKE);

        gray = new Paint();
        gray.setColor(Color.LTGRAY);
        gray.setStyle(Paint.Style.FILL_AND_STROKE);

        dkBlue = new Paint();
        dkBlue.setColor(Color.rgb(47, 96, 96));
        dkBlue.setStyle(Paint.Style.FILL);

        dkBlue1 = new Paint();
        dkBlue1.setColor(Color.rgb(47, 96, 96));
        dkBlue1.setStyle(Paint.Style.FILL);

        dkBlueCircle = new Paint();
        dkBlueCircle.setColor(Color.rgb(19, 21, 81));
        dkBlueCircle.setStyle(Paint.Style.STROKE);

        green.setColor(Color.GREEN);
        green.setStyle(Paint.Style.FILL_AND_STROKE);
        green.setStrokeWidth(3);

        nameFont = new Paint();
        nameFont.setColor(Color.rgb(47, 96, 96));
        nameFont.setStyle(Paint.Style.FILL);

        chain = new Paint();
        chain.setColor(Color.rgb (21,124,194));
        chain.setStyle(Paint.Style.FILL);
        chain.setAlpha(160);

        selectorPaint = new Paint();
        selectorPaint.setColor(Color.RED);
        selectorPaint.setStyle(Paint.Style.STROKE);
        selectorPaint.setAlpha(150);

        darkTransparent.setColor(Color.rgb(200,100,70));

        circlePaint = new Paint();
        circlePaint.setColor(Color.rgb(34, 139, 34));
        circlePaint.setStyle(Paint.Style.FILL_AND_STROKE);

        circleSelected = new Paint();
        circleSelected.setColor(Color.rgb(34, 139, 34));
        circleSelected.setStyle(Paint.Style.FILL_AND_STROKE);

        whiteTransp = new Paint(); //for amounts
        whiteTransp.setColor(Color.WHITE);
        whiteTransp.setStyle(Paint.Style.FILL_AND_STROKE);
        if (gii.prefs.getBoolean("show_circle_amount_transparent",true))
            whiteTransp.setAlpha(200);
        else
            whiteTransp.setAlpha(255);

        blueTransp = new Paint(); //for amounts
        blueTransp.setColor(Color.WHITE);
        blueTransp.setStyle(Paint.Style.FILL_AND_STROKE);
        if (gii.prefs.getBoolean("show_circle_amount_transparent",true))
            blueTransp.setAlpha(200);
        else
            blueTransp.setAlpha(255);

        greenTransp = new Paint(); //for amounts
        greenTransp.setColor(Color.GREEN);
        greenTransp.setStyle(Paint.Style.FILL_AND_STROKE);
        if (gii.prefs.getBoolean("show_circle_amount_transparent",true))
            greenTransp.setAlpha(200);
        else
            greenTransp.setAlpha(255);

        greenToRedTransp = new Paint(); //for amounts
        greenToRedTransp.setColor(Color.GREEN);
        greenToRedTransp.setStyle(Paint.Style.FILL_AND_STROKE);
        if (gii.prefs.getBoolean("show_circle_amount_transparent",true))
            greenToRedTransp.setAlpha(120);
        else
            greenToRedTransp.setAlpha(255);

        white = new Paint();
        whiteUnder = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL_AND_STROKE);
        whiteUnder.setColor(Color.WHITE);
        whiteUnder.setStyle(Paint.Style.FILL_AND_STROKE);
        whiteUnder.setAlpha(220);

        //white.setAlpha(255);

        arrowPaint = new Paint();
        //arrowPaint.setColor(Color.rgb(47, 96, 96));
        arrowPaint.setColor(Color.rgb(16, 16, 156));
        arrowPaint.setStyle(Paint.Style.STROKE);
        arrowPaint.setAlpha(120);

        //canvas.drawPath(arrowPath, paint);

        fatArrowPaint = new Paint();
        fatArrowPaint.setColor(Color.rgb(190, 210, 220));
        fatArrowPaint.setStyle(Paint.Style.STROKE);
        fatArrowPaint.setAlpha(200);

        bottomButtonTextPaint = new Paint();
        bottomButtonTextPaint.setColor(Color.GRAY);
        bottomButtonTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bottomButtonTextPaint.setTextSize(30);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = 2;
        if (gii.prefs.getBoolean("pref_low_res",false) == true)
            options.inSampleSize = 8;

        int[] manyColors = new int[] { Color.rgb(229,115,115), Color.rgb(244,67,54), Color.rgb(211,47,47),
                Color.rgb(240,98,146), Color.rgb(233,30,99), Color.rgb(194,24,91),
                Color.rgb(186,104,200), Color.rgb(156,39,176), Color.rgb(123,31,162),
                Color.rgb(149,117,205), Color.rgb(103,58,183), Color.rgb(81,145,168),
                Color.rgb(121,134,203), Color.rgb(63,81,181), Color.rgb(48,63,159),
                Color.rgb(100,181,246), Color.rgb(33,150,243), Color.rgb(25,118,210),
                Color.rgb(79,195,247), Color.rgb(3,169,244), Color.rgb(2,136,209),
                Color.rgb(77,208,225), Color.rgb(0,188,212), Color.rgb(0,151,167),
                Color.rgb(77,182,172), Color.rgb(0,150,136), Color.rgb(0,121,107),
                Color.rgb(129,199,132), Color.rgb(76,175,80), Color.rgb(56,142,60),
                Color.rgb(174,213,129), Color.rgb(139,195,74), Color.rgb(104,159,56),
                Color.rgb(220,231,117), Color.rgb(205,220,57), Color.rgb(175,180,43),
                Color.rgb(255,241,118), Color.rgb(255,235,59), Color.rgb(251,192,45),
                Color.rgb(255,213,79), Color.rgb(255,193,7), Color.rgb(255,160,0),
                Color.rgb(255,183,77), Color.rgb(255,152,0), Color.rgb(245,124,0),
                Color.rgb(255,138,101), Color.rgb(255,87,34), Color.rgb(230,74,25),
                Color.rgb(161,136,127), Color.rgb(121,85,72), Color.rgb(93,64,55),
                Color.rgb(224,224,224), Color.rgb(158,158,158), Color.rgb(97,97,97),
                Color.rgb(144,164,174), Color.rgb(96,125,139), Color.rgb(69,90,100)};

        circleColor = new Paint[manyColors.length];
        for (int i = 0; i < circleColor.length; i++) {
            circleColor[i] = new Paint();
            circleColor[i].setStyle(Paint.Style.FILL_AND_STROKE);
            circleColor[i].setColor(manyColors[i]);
            //circleColor[i].setAlpha(150);
        }

            //Bottom buttons (when some circle is selected)
        //Here we define how many different buttons we have and what are the icons.
        bottomButton = new ArrayList<>();
        bottomButton.add(new CanvasButton(ContextCompat.getDrawable(context, R.drawable._ic_mode_edit_black_24dp), context.getResources().getString(R.string.edit), CanvasButton.ButtonType.edit, bottomButtonTextPaint));
        bottomButton.get(0).addSubButton(new CanvasButton(ContextCompat.getDrawable(context, R.drawable._ic_all_out_black_24dp), context.getResources().getString(R.string.size), CanvasButton.ButtonType.plus, bottomButtonTextPaint));
        bottomButton.get(0).addSubButton(new CanvasButton(ContextCompat.getDrawable(context, R.drawable._ic_photo_size_select_actual_black_24dp), context.getResources().getString(R.string.icon), CanvasButton.ButtonType.color, bottomButtonTextPaint));
        bottomButton.get(0).addSubButton(new CanvasButton(ContextCompat.getDrawable(context, R.drawable._ic_mode_edit_black_24dp), context.getResources().getString(R.string.rename), CanvasButton.ButtonType.rename, bottomButtonTextPaint));
        bottomButton.get(0).addSubButton(new CanvasButton(ContextCompat.getDrawable(context, R.drawable._ic_delete_black_24dp), context.getResources().getString(R.string.delete), CanvasButton.ButtonType.delete, bottomButtonTextPaint));

        bottomButton.add(new CanvasButton(ContextCompat.getDrawable(context, R.drawable._ic_data_usage_black_24dp), context.getResources().getString(R.string.statistics), CanvasButton.ButtonType.branch, bottomButtonTextPaint));

        bottomButton.add(new CanvasButton(ContextCompat.getDrawable(context, R.drawable._ic_view_list_black_24dp), context.getResources().getString(R.string.operations) ,
                CanvasButton.ButtonType.operations, bottomButtonTextPaint));

    }

    public void animateBottomMenu(boolean showMenu) {
        menuSize.set(canvasWidth, Math.max(canvasWidth, canvasHeight) / 10);
        if (!showMenu)
            menuPosition = Math.min(menuPosition + (canvasHeight - menuPosition) / 2, canvasHeight);
        else
            menuPosition = Math.max(menuPosition - (menuPosition - (canvasHeight - menuSize.y)) / 2, canvasHeight - menuSize.y);
    }

    Path myPath = new Path();
    PointF centerText = new PointF();
    PointF center = new PointF();
    /**
     * Draws an arrow from one circle to another
     * @param operation the operation (may be a displayedOperation). Needed for amount.
     * @param paint color of an arrow. Used for making older operations more transparent.
     */
    private void drawArrow(Canvas canvas, Circle fromCircle, Circle toCircle, Operation operation, Paint paint, Properties properties, GII.AppState appState, String selectedId, PointF moveXY) {
        if (fromCircle.name.equals("none") ||
                toCircle.name.equals("none"))
            return;

        if (gii.prefs.getBoolean("show_arrow_only_related", true))
            if (!gii.selectedId.equals(fromCircle.id) &&
                    !gii.selectedId.equals(toCircle.id))
                return;
        
        paint.setStyle(Paint.Style.STROKE);
        float strokeWidth = 4 * properties.scaleFactor +
                properties.scaleFactor * 30 / GIIApplication.gii.maxDisplayedAmount * operation.absAmountInLocalCurrency;
        paint.setStrokeWidth(strokeWidth); //4*properties.scaleFactor

        //Path myPath = new Path();

        int offset = 20;

        mapToScreen(fromCircle.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && fromCircle.id.equals(selectedId), moveXY),properties,point0);
        mapToScreen(toCircle.getCoordinates(!gii.doNotMove && appState == GII.AppState.editMode && toCircle.id.equals(selectedId), moveXY),properties,point1);

        Geometry.moveToAngle(point0, Geometry.calculateAngle(point0, point1), fromCircle.radius * 1.1f * properties.scaleFactor, point0);
        Geometry.moveToAngle(point1, Geometry.calculateAngle(point1, point0), toCircle.radius * 1.1f * properties.scaleFactor, point1);
        Geometry.moveToAngle(point1, Geometry.calculateAngle(point1, point0), 5 * properties.scaleFactor, point11);
        Geometry.moveToAngle(point1, Geometry.calculateAngle(point1, point0) - 15, 25 * properties.scaleFactor, point2);
        Geometry.moveToAngle(point1, Geometry.calculateAngle(point1, point0) + 15, 25 * properties.scaleFactor, point3);
        Geometry.centerRightOffset(point0, point1, (offset) * properties.scaleFactor, centerText);
        Geometry.centerRightOffset(point0, point1, (offset) * properties.scaleFactor, center);
        //myPath.moveTo(pointA.x, pointA.y);
        //myPath.quadTo(center.x, center.y, pointB.x, pointB.y);
        //myPath.lineTo(pointB.x, pointB.y);
        paint.setShader(new LinearGradient(point0.x, point0.y, centerText.x, centerText.y, Color.TRANSPARENT, Color.rgb(16, 16, 196), Shader.TileMode.CLAMP));
        myPath.reset();
        myPath.moveTo(point0.x,point0.y);
        myPath.lineTo(point11.x,point11.y);
        myPath.lineTo(point2.x,point2.y);
        myPath.lineTo(point1.x,point1.y);
        myPath.lineTo(point3.x,point3.y);
        myPath.lineTo(point11.x,point11.y);

        //canvas.drawLine(point0.x,point0.y,point1.x,point1.y, paint);
        //paint.setStyle(Paint.Style.FILL_AND_STROKE);
        //paint.setStrokeJoin(Paint.Join.ROUND);

        //paint.setStrokeCap(Paint.Cap.ROUND);
        canvas.drawPath(myPath,paint);

        //canvas.drawLine(point1.x, point1.y, point2.x, point2.y, paint);
        //canvas.drawLine(point1.x, point1.y, point3.x, point3.y, paint);

        if (gii.prefs.getBoolean("show_operation_arrow_amount", false)) {
            canvas.save();
            canvas.rotate(Geometry.calculateAngle(point0, point1), centerText.x, centerText.y);
            paint.setTextSize(20 * properties.scaleFactor);
            paint.setStrokeWidth(1);//2 * properties.scaleFactor
            paint.setStyle(Paint.Style.FILL_AND_STROKE);
            String operationAmount = operation.amountText;
            if (operation.twoWay)
                operationAmount = operation.amountText + " -->";
            if (point1.x < point0.x) {
                if (operation.twoWay)
                    operationAmount = "<-- " + operation.amountText;
                canvas.rotate(180, centerText.x, centerText.y - paint.getTextSize()/2.5f );
            }

            canvas.drawText(operationAmount, centerText.x - operation.amountTextWidth * properties.scaleFactor / 2, centerText.y + strokeWidth / 2, paint);
            canvas.restore();
        }
    }

    Paint green = new Paint();
    PointF lastPoint = new PointF();
    float x = 0,y = 0;
    /**
     * Simply draws the green gestures on screen. They represent "gesture" list.
     */
    public void drawGestures(Canvas canvas, GII.AppState appState, ArrayList<Circle> circle, Properties properties,
                             String firstGestureCircleId, ArrayList<PointF> gesture,
                             long counter, int timeToCreate) {



        if (appState == GII.AppState.circleTouched) {
            Circle acc =gii.circleById(firstGestureCircleId, circle);

            if (gesture.size() == 0)
                return;


            lastPoint = gesture.get(0);
            mapToScreen(lastPoint, properties,moved1);

            lastPoint = gesture.get(gesture.size() - 1);
            mapToScreen(lastPoint, properties,moved);
            x = moved.x;
            y = moved.y;
            //rect0.set(0, 0, circleBitmap[acc.picture % circleBitmap.length].getWidth(), circleBitmap[acc.picture % circleBitmap.length].getHeight());
            rect1.set((int) (x - (acc.radius * properties.scaleFactor * 0.7f)), (int) (y - acc.radius * properties.scaleFactor*0.7f),
                    (int) (x + acc.radius * properties.scaleFactor * 0.7f), (int) (y + acc.radius * properties.scaleFactor*0.7f));
            drawConnection(moved.x, moved.y, moved1.x, moved1.y, circleColor[acc.color % circleColor.length], properties, canvas);
            canvas.drawCircle(x,y,acc.radius * properties.scaleFactor * 0.7f, circleColor[acc.color % circleColor.length]);

            drawableIcon[acc.picture % drawableIcon.length].setBounds(rect1);
            drawableIcon[acc.picture % drawableIcon.length].draw(canvas);
        }

        if (appState == GII.AppState.creating) {
            Calendar timerCal = Calendar.getInstance();
            Long interval = timerCal.getTimeInMillis() - counter;
            tmpPath.reset();
            if (interval < timeToCreate)
                if ((gesture.size() == 1)) {
                    if (!gii.prefs.getBoolean("prevent_create",false)) {
                        mapToScreen(gesture.get(0), properties, moved);
                        tmpPath.moveTo(moved.x, moved.y);
                        for (float j = 0 + (float) Math.PI; j < (float) interval / timeToCreate * Math.PI * 2 ; j = j + 0.1f)
                            tmpPath.lineTo((float) (moved.x + Math.sin(j) * 100),
                                    (float) (moved.y + Math.cos(j) * 100));
                        tmpPath.lineTo(moved.x, moved.y);
                        for (float j = 0 + (float) Math.PI; j < (float) interval / timeToCreate * Math.PI * 2 ; j = j + 0.1f)
                            tmpPath.lineTo((float) (moved.x + Math.sin(j + Math.PI) * 100),
                                    (float) (moved.y + Math.cos(j + Math.PI) * 100));
                        canvas.clipPath(tmpPath);
                        drawIcon(null,GIIApplication.gii.abstractCorrectionCircle.picture,1,1,
                                new Rect((int)(moved.x - 100),(int)(moved.y - 100),
                                        (int)(moved.x + 100), (int)(moved.y + 100)),0,false,canvas
                                );
                    }
                }
        }

    }

    Paint menuText = new Paint();
    Paint menuText1 = new Paint();
    /**
     * Define the position of a bottom menu.
     * This function is sophisticated only because the menu
     * slowly appears on screen (moves up when needed).
     * In general, all code for "selectedId == -1" is not necessary here,
     * because this function is called from "onDraw" only if appState = circleSelected,
     * and this happens only when selectedId != -1
     */
    public void drawBottomMenu(Canvas canvas, GII.AppState appState) {

        //menuText.setAlpha(255);
        menuText.setColor(Color.GREEN);
        menuText.setStrokeWidth(2);
        menuText.setTextSize(canvasHeight / (5 * 4));

        //menuText1.setAlpha(255);
        menuText1.setColor(Color.GREEN);
        menuText1.setStrokeWidth(2);
        menuText1.setTextSize(canvasHeight / (8 * 4));

        //if (menuPosition < canvasHeight - 10)
        //    drawButtons(canvas,appState);
    }

    /** draw bottom buttons
     * Here we can define where these bottom buttons are.
     * I was thinking of moving them to the right bottom corner
     * and place vertically, not horizontally as now.
     */
    private void drawButtons(Canvas canvas, GII.AppState appState) {
        int buttonsCount = bottomButton.size();
        int offSet = 30;//(((int) canvasWidth - ((int) menuSize.y) * (buttonsCount))) / 2 - 10;
        for (int i = 0; i < buttonsCount; i++) {
            bottomButton.get(i).setToRectangle(
                    offSet + (int) menuSize.y * i, (int) menuPosition, offSet + (i + 1) * (int) menuSize.y, (int) menuPosition + (int) menuSize.y);
            bottomButton.get(i).transformAndMove();
            if (bottomButton.get(i).selected) {
                for (int j = 0; j < bottomButton.get(i).subButton.size(); j++) {
                    bottomButton.get(i).subButton.get(j).setToRectangle(
                            bottomButton.get(i).rectangle.left, bottomButton.get(i).rectangle.top - (int) ((j + 1) * menuSize.y),
                                    bottomButton.get(i).rectangle.right, bottomButton.get(i).rectangle.bottom - (int) ((j + 1) * menuSize.y)
                    );
                    bottomButton.get(i).subButton.get(j).transformAndMove();
                }
            }
            if (appState == GII.AppState.showOperations && i==buttonsCount-2)
                i++;
        }

        for (CanvasButton aBottomButton : bottomButton) {
            drawButton(aBottomButton,canvas);
            if (aBottomButton.selected) {
                for (int i = 0; i < aBottomButton.subButton.size(); i++) {
                    drawButton(aBottomButton.subButton.get(i),canvas);
                }
            }
        }
    }
    public void drawButton(CanvasButton button, Canvas canvas) {
        int offSet = button.rectangle.width()/5;
        button.bitmap.setBounds(button.rectangle.left+offSet,button.rectangle.top+offSet,button.rectangle.right-offSet,button.rectangle.bottom-offSet);
        canvas.drawCircle(button.rectangle.centerX(),button.rectangle.centerY(),button.rectangle.width()/2,dkBlue);
        button.bitmap.draw(canvas);
        moved.set(button.rectangle.centerX(), button.rectangle.top);
        canvas.drawText(button.description, moved.x - button.halfMeasuredDescription, moved.y + button.paint.getTextSize() / 2.5f, button.paint);
    }



    public void drawBackground(Canvas canvas) {
        //float ratio = backgroundBitmap.getHeight() / backgroundBitmap.getWidth();
        //bgRect0.set(0, 0, backgroundBitmap.getWidth(), backgroundBitmap.getHeight());
        //bgRect1.set(0, 0, canvas.getWidth(),Math.max(canvas.getHeight(),(int)(canvas.getWidth()*ratio)));
        //canvas.drawBitmap(backgroundBitmap,bgRect0,bgRect1,null);
        //canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),bgColor);

    }

    public void showPopUpOperation(Operation operation,  ArrayList<Circle> circle) {
        popupNeedsToUpdate = true;
        popupPosition = 0;
        popupPositionStartShow = 0;
        popupOperation = operation;
        popupCircleFrom =gii.circleById(operation.fromCircle, circle);
        popupCircleTo =gii.circleById(operation.toCircle, circle);
        operationListWindow.circle = circle;
        operationListWindow.monthName = monthName;
    }

    public void updatePopUpOperation() {
        popupPositionStartShow++;
        if (popupPositionStartShow < 30)
            return;
        int diameter = (int)(Math.min(canvasWidth,canvasHeight)/6);
        popupPosition++;
        if (popupPosition <= 5) {
            popupRectangle.set(0,(int)canvasHeight - diameter*popupPosition/5,(int)canvasWidth,
                    (int)canvasHeight - diameter*popupPosition/5 + diameter);
        }

        if (popupPosition == 5)
            if (gii.prefs.getBoolean("sounds",false))
                MainActivity.beep();

        if (popupPosition > 90) {
            popupRectangle.set(0,(int)canvasHeight - diameter*(95-popupPosition)/5,(int)canvasWidth,
                    (int)canvasHeight - diameter*(95 - popupPosition)/5 + diameter);
        }

        if (popupPosition >= 95)
            popupNeedsToUpdate = false;
    }
    private void drawPopup(Canvas canvas) {
        canvas.drawRect(popupRectangle,whiteTransp);
        operationListWindow.drawOperation(popupRectangle, popupOperation, canvas);
        animateNewOperation(popupOperation, canvas);
    }

    private void animateNewOperation(Operation popupOperation, Canvas canvas) {
        if (popupPosition > 50)
            return;
        if (!gii.prefs.getBoolean("show_operation_animation",true))
            return;
        Circle acc1 =gii.circleById(popupOperation.fromCircle, gii.circle);
        Circle acc2 =gii.circleById(popupOperation.toCircle, gii.circle);
        mapToScreen(acc1.coordinates,gii.properties, moved);
        mapToScreen(acc2.coordinates,gii.properties, moved1);
        chain.setColor(circleColor[acc1.getColor()%circleColor.length].getColor());
        //drawASolidLine(moved.x, moved.y, moved1.x, moved1.y, chain, gii.properties, canvas);
        for (int i = 2; i >= 0; i--) {
            float progress = (popupPosition - 10*i);
            if (progress >=0 && progress <= 30) {
                float radius = (20 + 25 - (Math.abs(progress - 15))) * gii.properties.scaleFactor;
                moved.set(moved.x + (moved1.x - moved.x) * progress / 30,
                        moved.y + (moved1.y - moved.y) * progress / 30);
                rect1 = new Rect((int) (moved.x - (radius)), (int) (moved.y - radius),
                        (int) (moved.x + radius), (int) (moved.y + radius));
                canvas.save();
                canvas.rotate(progress * 20, moved.x, moved.y);
                drawIcon(null, acc1.picture, acc1.color, acc1.color, rect1, 0, false, canvas);
                canvas.restore();
            }
        }
    }


    public void buildPie(float statExpenseTotal, Map<String, Float> statExpense, Rect rect, Canvas canvas) {

        //drawIcon(gii.selectedCircle.picture, gii.selectedCircle.color,gii.selectedCircle.color,
        //        new Rect (rect.left + rect.width() / 4,rect.top + rect.height() / 4,
        //                rect.right - rect.width() / 4, rect.bottom - rect.width() / 4), canvas);

        boolean wideMode = false;

        Rect mainRect = rect;
        //Log.e("Report","Main dimensions: " + mainRect.width() + "," + mainRect.height());

        if (rect.width() > rect.height()) {
            int centerX = rect.centerX();
            rect = new Rect(centerX - rect.height()/2 + rect.height()/10, rect.top + rect.height()/10,
                    centerX + rect.height()/2 - rect.height()/10, rect.bottom - rect.height()/10);
            //Log.e("Report","center: " + rect.centerX() + "," + rect.centerY());
            wideMode = true;
        }

        if (wideMode) {
            canvas.drawRect(mainRect,white);
        }

        Log.e("Report","Drawing pie, wideMode = " + wideMode);

        float fraction = 0;
        if (gii.selectedCircle.goalAmount != 0) {
            fraction = Math.min(Math.abs(
                    //((!_circle.myMoney || properties.filtered) ? _circle.amount : _circle.amountTotal)
                    (gii.selectedCircle.amount)
            ) / Math.abs(gii.selectedCircle.goalAmount), 1);
        }

        drawIcon(gii.selectedCircle, gii.selectedCircle.picture, gii.selectedCircle.color,gii.selectedCircle.color,
                rect, fraction, false, canvas);

        Paint piePaint = new Paint();
        Paint pieInnerPaint = new Paint();
        PointF pieStartPoint = new PointF(0,0);
        PointF pieInnerStartPoint = new PointF(0,0);
        Path piePath = new Path();
        Path pieInnerPath = new Path();
        piePaint.setColor(Color.GREEN);
        piePaint.setStrokeWidth(1);
        piePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pieInnerPaint.setColor(Color.GREEN);
        pieInnerPaint.setStrokeWidth(1);
        pieInnerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        float startAngle = (float)Math.PI/4;
        ArrayList<String> rightAgenda = new ArrayList<>();
        ArrayList<PointF> rightAgendaPoints = new ArrayList<>();
        ArrayList<String> leftAgenda = new ArrayList<>();
        ArrayList<PointF> leftAgendaPoints = new ArrayList<>();
        for (Map.Entry<String, Float> entry : statExpense.entrySet()) {
            piePaint.setColor(circleColor[gii.circleById(entry.getKey(),gii.circle).getColor() % circleColor.length].getColor());

            pieInnerPaint.setColor(Color.rgb(Math.min(Color.red(piePaint.getColor())+70,255),
                    Math.min(Color.green(piePaint.getColor())+70,255),
                    Math.min(Color.blue(piePaint.getColor())+70,255)));
            float length =  (float)Math.PI * 2f * entry.getValue()/statExpenseTotal;
            pieStartPoint.set((float)(rect.centerX() + Math.sin(startAngle)*rect.width()/2),
                    (float)(rect.centerY() + Math.cos(startAngle)*rect.height()/2));
            pieInnerStartPoint.set((float)(rect.centerX() + Math.sin(startAngle)*rect.width()/2.6),
                    (float)(rect.centerY() + Math.cos(startAngle)*rect.height()/2.6));
            moved.set((float)(rect.centerX() + Math.sin(startAngle + length/2)*rect.width()/2.3),
                    (float)(rect.centerY() + Math.cos(startAngle + length/2)*rect.height()/2.3));
            piePath.reset();
            pieInnerPath.reset();
            piePath.moveTo(pieStartPoint.x,pieStartPoint.y);
            pieInnerPath.moveTo(pieInnerStartPoint.x,pieInnerStartPoint.y);
            boolean once = false;
            for (float i = startAngle; i <= startAngle + length; i = i + 0.1f) {
                piePath.lineTo((float)(rect.centerX() + Math.sin(i) * rect.width()/2),
                        (float)(rect.centerY() + Math.cos(i) * rect.height()/2));
                pieInnerPath.lineTo((float)(rect.centerX() + Math.sin(i)*rect.width()/2.6),
                        (float)(rect.centerY() + Math.cos(i) * rect.height()/2.6));
                if (i + 0.1f > startAngle + length && !once) {
                    i = startAngle + length - 0.1f;
                    once = true;
                }
            }
            once = false;
            for (float i = startAngle + length; i >= startAngle; i = i - 0.1f) {
                piePath.lineTo((float)(rect.centerX() + Math.sin(i)*rect.width()/2.6),
                        (float)(rect.centerY() + Math.cos(i)*rect.height()/2.6));
                pieInnerPath.lineTo((float)(rect.centerX() + Math.sin(i)*rect.width()/2.8),
                        (float)(rect.centerY() + Math.cos(i)*rect.height()/2.8));
                if (i - 0.1f < startAngle && !once) {
                    i = startAngle + 0.1f;
                    once = true;
                }
            }
            piePath.lineTo((float)(rect.centerX() + Math.sin(startAngle)*rect.width()/2.6),
                    (float)(rect.centerY() + Math.cos(startAngle)*rect.height()/2.6));
            pieInnerPath.lineTo((float)(rect.centerX() + Math.sin(startAngle)*rect.width()/2.8),
                    (float)(rect.centerY() + Math.cos(startAngle)*rect.height()/2.8));
            piePath.moveTo(pieStartPoint.x,pieStartPoint.y);
            pieInnerPath.moveTo(pieInnerStartPoint.x,pieInnerStartPoint.y);

            canvas.drawPath(piePath,piePaint);
            canvas.drawPath(pieInnerPath,pieInnerPaint);

            if (length > 0.15) {
                int iconBitmapNo = gii.circleById(entry.getKey(), gii.circle).picture;
                float radius = (rect.width() / 2f - (float) rect.width() / 2.3f) * 0.65f;
                //rect0 = new Rect(0, 0, circleBitmap[iconBitmapNo % circleBitmap.length].getWidth(), circleBitmap[iconBitmapNo % circleBitmap.length].getHeight());
                rect1 = new Rect((int) (moved.x - (radius)), (int) (moved.y - radius),
                        (int) (moved.x + radius), (int) (moved.y + radius));
                //canvas.drawBitmap(circleBitmap[iconBitmapNo % circleBitmap.length], rect0, rect1, null);
                drawableIcon[iconBitmapNo % drawableIcon.length].setBounds(rect1);
                drawableIcon[iconBitmapNo % drawableIcon.length].draw(canvas);

            }
            moved.set((float)(rect.centerX() + Math.sin(startAngle + length/2)*rect.width()/2.05),
                    (float)(rect.centerY() + Math.cos(startAngle + length/2)*rect.height()/2.05));
            PointF longerLine = new PointF((float)(rect.centerX() + Math.sin(startAngle + length/2)*rect.width()/1.8f),
                    (float)(rect.centerY() + Math.cos(startAngle + length/2)*rect.height()/1.8f));
            Point sidePoint = new Point(rect.centerX() + (int)(rect.width()/2*1.1f),(int)longerLine.y);
            if (moved.x < rect.centerX())
                sidePoint = new Point(rect.centerX() - (int)(rect.width()/2*1.1f),(int)longerLine.y);
            if (moved.x < rect.centerX()) {
                leftAgenda.add(gii.circleById(entry.getKey(), gii.circle).name + " " + GII.df.format(entry.getValue()/statExpenseTotal*100) + "%");
                leftAgendaPoints.add(new PointF(sidePoint));
            } else {
                rightAgenda.add(gii.circleById(entry.getKey(), gii.circle).name + " " + GII.df.format(entry.getValue()/statExpenseTotal*100) + "%");
                rightAgendaPoints.add(new PointF(sidePoint));
            }
            Path twoLines = new Path();
            twoLines.moveTo((int)moved.x, (int)moved.y);
            twoLines.lineTo((int)longerLine.x,(int)longerLine.y);
            twoLines.lineTo((int)sidePoint.x,(int)sidePoint.y);
            piePaint.setStyle(Paint.Style.STROKE);
            piePaint.setStrokeWidth(rect.height()/120);
            if (wideMode)
                canvas.drawPath(twoLines,piePaint);
            piePaint.setStyle(Paint.Style.FILL_AND_STROKE);
            piePaint.setStrokeWidth(1);
            startAngle += length;
        }
        Paint textPaint = new Paint();
        textPaint.setTextSize(rect.height()/20);
        textPaint.setStrokeWidth(1);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        textPaint.setColor(Color.BLUE);
        for (int i = 0; i < rightAgenda.size(); i++) {
            String text = rightAgenda.get(i);
            PointF textPoint = rightAgendaPoints.get(i);
            if (wideMode)
                canvas.drawText(text,textPoint.x + textPaint.getTextSize()/2,textPoint.y + textPaint.getTextSize()/2,textPaint);
        }
        for (int i = 0; i < leftAgenda.size(); i++) {
            String text = leftAgenda.get(i);
            PointF textPoint = new PointF(leftAgendaPoints.get(i).x - textPaint.measureText(text), leftAgendaPoints.get(i).y);
            if (wideMode)
                canvas.drawText(text,textPoint.x - textPaint.getTextSize()/2,textPoint.y + textPaint.getTextSize()/2,textPaint);
        }

    }
    public void buildDinaPie(float statExpenseTotal, Map<String, Float> statExpense, Rect rect, Canvas canvas) {

        //drawIcon(gii.selectedCircle.picture, gii.selectedCircle.color,gii.selectedCircle.color,
        //        new Rect (rect.left + rect.width() / 4,rect.top + rect.height() / 4,
        //                rect.right - rect.width() / 4, rect.bottom - rect.width() / 4), canvas);

        boolean wideMode = false;

        Rect mainRect = rect;
        //Log.e("Report","Main dimensions: " + mainRect.width() + "," + mainRect.height());


        float startRaius = 0.3f/2f;
        float endRadius = 0.9f/2f;
        float stepRadius = (endRadius - startRaius)/statExpense.size();

        if (rect.width() > rect.height()) {
            int centerX = rect.centerX();
            rect = new Rect(centerX - rect.height()/2, rect.top,
                    centerX + rect.height()/2, rect.bottom);
            //Log.e("Report","center: " + rect.centerX() + "," + rect.centerY());
            wideMode = true;
        }

        if (wideMode) {
            canvas.drawRect(mainRect,white);
        }

        Log.e("Report","Drawing pie, wideMode = " + wideMode);



        //drawIcon(gii.selectedCircle.picture, gii.selectedCircle.color,gii.selectedCircle.color,
        //        rect, canvas);

        Paint piePaint = new Paint();
        Paint pieInnerPaint = new Paint();
        PointF pieStartPoint = new PointF(0,0);
        PointF pieInnerStartPoint = new PointF(0,0);
        Path piePath = new Path();
        Path pieInnerPath = new Path();
        piePaint.setColor(Color.GREEN);
        piePaint.setStrokeWidth(1);
        piePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pieInnerPaint.setColor(Color.GREEN);
        pieInnerPaint.setStrokeWidth(1);
        pieInnerPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        float startAngle = 0;
        int i = -1;

        float coefficient = 1;
        float max = 0;
        String[] circleId = new String[statExpense.size()];
        float[] circleAmount = new float[statExpense.size()];

        for (Map.Entry<String, Float> entry : statExpense.entrySet()) {
            if (max < entry.getValue())
                max = entry.getValue();
            i++;
            circleId[i] = entry.getKey();
            circleAmount[i] = entry.getValue();
            Log.e("Report","Dinara Pie: " + circleAmount[i]);
        }
        for (int k1 = 0; k1 < statExpense.size(); k1++)
            for (int k2 = k1; k2 < statExpense.size(); k2++)
                if (circleAmount[k1] > circleAmount[k2]) {
                    float cA = circleAmount[k1];
                    circleAmount[k1] = circleAmount[k2];
                    circleAmount[k2] = cA;
                    String cId = circleId[k1];
                    circleId[k1] = circleId[k2];
                    circleId[k2] = cId;
                }


        for (int j = 0; j < statExpense.size(); j++) {
            i = j;
            Circle currentCircle = gii.circleById(circleId[i],gii.circle);
            startAngle = - (float)Math.PI/2;
            piePaint.setColor(circleColor[currentCircle.getColor() % circleColor.length].getColor());
            pieInnerPaint.setColor(Color.rgb(Math.min(Color.red(piePaint.getColor()) + 70,255),
                    Math.min(Color.green(piePaint.getColor())+70,255),
                    Math.min(Color.blue(piePaint.getColor())+70,255)));
            float length =  ((float)Math.PI * 2f * circleAmount[i] / max)*0.75f + (float)Math.PI * 0.2f;
            //Log.e("Report","Dinara Pie, drawing length " + length + " i = " + i + ", " + circleAmount[i]);
            pieStartPoint.set((float)(rect.centerX() + Math.sin(-startAngle)*rect.width() * (startRaius + (i+1) * stepRadius)),
                    (float)(rect.centerY() + Math.cos(-startAngle)*rect.height() * (startRaius + (i+1) * stepRadius)));
            pieInnerStartPoint.set((float)(rect.centerX() + Math.sin(-startAngle)*rect.width() * (startRaius + (i) * stepRadius)),
                    (float)(rect.centerY() + Math.cos(- startAngle)*rect.height() * (startRaius + (i) * stepRadius)));
            moved.set((float)(rect.centerX() + Math.sin(- (startAngle + Math.PI * 0.1f)) * rect.width() * (startRaius + (i + 0.5) * stepRadius)),
                    (float)(rect.centerY() + Math.cos(- (startAngle + Math.PI * 0.1f)) * rect.height() * (startRaius + (i + 0.5) * stepRadius)));
            piePath.reset();
            pieInnerPath.reset();
            piePath.moveTo(pieStartPoint.x,pieStartPoint.y);
            pieInnerPath.moveTo(pieInnerStartPoint.x,pieInnerStartPoint.y);
            boolean once = false;
            for (float a = startAngle; a <= startAngle + length; a = a + 0.1f) {
                piePath.lineTo((float)(rect.centerX() + Math.sin(-a) * rect.width() * (startRaius + (i+1) * stepRadius)),
                        (float)(rect.centerY() + Math.cos(-a) * rect.height() * (startRaius + (i+1) * stepRadius)));
                if (a + 0.1f > startAngle + length && !once) {
                    a = startAngle + length - 0.1f;
                    once = true;
                }
            }
            {
                float a = startAngle + length + 0.2f/(float)statExpense.size();
                piePath.lineTo((float) (rect.centerX() + Math.sin(-a) * rect.width() * (startRaius + (i + 0.5) * stepRadius)),
                        (float) (rect.centerY() + Math.cos(-a) * rect.height() * (startRaius + (i + 0.5) * stepRadius)));
            }

            once = false;
            for (float a = startAngle + length; a >= startAngle; a = a - 0.1f) {
                piePath.lineTo((float)(rect.centerX() + Math.sin(-a) * rect.width() * (startRaius + (i) * stepRadius)),
                        (float)(rect.centerY() + Math.cos(-a) * rect.height() * (startRaius + (i) * stepRadius)));
                if (a - 0.1f < startAngle && !once) {
                    a = startAngle + 0.1f;
                    once = true;
                }
            }
            piePath.moveTo(pieStartPoint.x,pieStartPoint.y);
            canvas.drawPath(piePath,piePaint);
            float radius = stepRadius * canvas.getWidth() / 2 /2 * 0.8f;
            //Log.e("Report","Dinara Pie stepRadius = " + radius);
            //canvas.drawCircle(moved.x,moved.y,radius,circleColor[currentCircle.color]);
            int iconBitmapNo = currentCircle.picture;
            rect1 = new Rect((int) (moved.x - (radius)), (int) (moved.y - radius),
                    (int) (moved.x + radius), (int) (moved.y + radius));
            drawableIcon[iconBitmapNo % drawableIcon.length].setBounds(rect1);
            drawableIcon[iconBitmapNo % drawableIcon.length].draw(canvas);
        }
        String s = gii.selectedCircle.name;
        Paint textPaint = new Paint(dkBlue);
        textPaint.setTextSize(canvas.getHeight()/5);
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        PointF textPoint = new PointF((float)(rect.centerX() + Math.sin(Math.PI/2 + 0.2f) * rect.width() * (startRaius + endRadius)/2)
                - textPaint.measureText(s)/2,
                (float)(rect.centerY() + Math.cos(Math.PI/2 + 0.2f) * rect.height() * (startRaius + endRadius)/2)
                );
        if (!s.equals("none"))
            canvas.drawText(s,textPoint.x,textPoint.y,textPaint);
    }

    public void buildLineGraph(String selectedId, ArrayList<Operation> graphOperation, Rect rect, Canvas canvas) {
        int unit = rect.width()/30;
        Paint white = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL_AND_STROKE);
        white.setStrokeWidth(unit/15);

        Paint gray = new Paint();
        gray.setColor(Color.GRAY);
        gray.setTextSize(unit/1.5f);
        gray.setStyle(Paint.Style.FILL_AND_STROKE);
        gray.setStrokeWidth(unit/15);

        Paint circlePaint = new Paint(circleColor[gii.selectedCircle.color % circleColor.length]);
        Paint circlePaintTransparent = new Paint();
        circlePaint.setStrokeWidth(unit / 4);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaintTransparent = new Paint(circleColor[gii.selectedCircle.color % circleColor.length]);
        circlePaintTransparent.setStrokeWidth(unit / 4);
        circlePaintTransparent.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaintTransparent.setAlpha(90);



        //canvas.drawRect(rect,piePaint);
        //canvas.drawRect(new Rect(rect.left + 10, rect.top + 10, rect.right - 10, rect.bottom - 10),pieInnerPaint);

        ArrayList<Float> segments = new ArrayList<>();
        ArrayList<Integer> scaledSegments = new ArrayList<>();
        ArrayList<Integer> scaledSums = new ArrayList<>();

        segments.clear();
        scaledSegments.clear();

        int n = 21;
        float[] data = new float[n];
        for (int i = 0; i < n; i++)
            data[i] = 0;

        Calendar cal = Calendar.getInstance();
        cal.setTime(Collections.max(graphOperation).date);
        long startingMillis = cal.getTimeInMillis();
        cal.setTime(Collections.min(graphOperation).date);
        long endingMillis = cal.getTimeInMillis();
        Log.e("Report","starting: " + startingMillis + ", ending: " + endingMillis);
        endingMillis -= startingMillis;
        //float

        long currentMillis = 0;
        long intervalMillis = endingMillis / n;
        for (Operation operation : graphOperation) {
            float amount = operation.amount;
            Log.e("Report","Circles in: " + operation.circlesWayIn);
            if (!gii.selectedId.equals("none")) {
                if (!operation.circlesWayIn.contains(selectedId))
                    amount *= -1;
            } else {
                if (operation.isExpense)
                    amount *= -1;
            }
            cal.setTime(operation.date);
            currentMillis = cal.getTimeInMillis() - startingMillis;
            int position = (int)(((float)currentMillis / (float)endingMillis) * (n-1));
            data[position] += amount;
            Log.e("Report","Line Graph: adding " + amount + " to " + position + ", id=" + operation.id + " Income/Expense: " + operation.isIncome + "/" + operation.isExpense);
        }

        for (int i = 0; i < n; i++)
            segments.add(data[i]);

        float min = segments.get(0);
        float max = segments.get(0);
        int sum = 0;

        canvas.drawRect(rect,white);

        Rect chart = new Rect(rect.left + unit * 5,rect.top + unit,rect.right - unit,rect.bottom - unit * 6);
        //canvas.drawRect(chart,gray);

        for (Float segment : segments) {
            sum += segment;
            if (min > segment)
                min = segment;
            if (min > sum)
                min = sum;
            if (max < segment)
                max = segment;
            if (max < sum)
                max = sum;
        }
        if (max == min)
            max = min + 1;
        float scale = (float)chart.height()/(float)(max - min); //50/1000? 1000 * (50/1000) = 50; seems correct

        canvas.drawLine(chart.left,chart.bottom,chart.left,chart.top,gray);
        canvas.drawLine(chart.left,chart.bottom,chart.right,chart.bottom,gray);
        float whereIsZero = -700;
        if (min * max <= 0) {
            canvas.drawLine(chart.left, chart.bottom - (0 - min) * scale, chart.right + 2, chart.bottom - (0 - min) * scale, gray);
            canvas.drawText("0  ", chart.left - gray.measureText("0  "),chart.bottom - (0 - min) * scale + gray.getTextSize() / 2,gray);
            whereIsZero = (0 - min) * scale;
        }
        String nextText = gii.currency(min,"");
        if (whereIsZero > gray.getTextSize())
            canvas.drawText(nextText, chart.left - gray.measureText(nextText + "  "),chart.bottom + gray.getTextSize() / 2,gray);
        nextText = gii.currency(max,"");
        if (whereIsZero < chart.height() - gray.getTextSize())
            canvas.drawText(nextText, chart.left - gray.measureText(nextText + "  "),chart.top + gray.getTextSize() / 2,gray);

        sum = 0;
        for (Float segment : segments) {
            scaledSegments.add((int) ((segment - min) * scale));
            sum += segment;
            scaledSums.add((int)((sum - min) * scale));
            //Log.e("Report","adding scaled segment " + (int) ((segment - min) * scale));
        }

        Path linePath = new Path();
        Path totalPath = new Path();
        linePath.moveTo(chart.left + 2,chart.bottom - scaledSegments.get(0) - 2);
        totalPath.moveTo(chart.left + 2,chart.bottom - 2);
        int lastValue = scaledSegments.get(0);
        int i = 0; sum = 0;
        int verticalLine = 0;
        int lastVerticalLine = chart.left + 2;
        Point via = new Point (0,0);
        Point viaTotal = new Point(0,0);
        for (Integer scaledSegment : scaledSegments) {
            verticalLine = chart.left + 2 + (int)((chart.width() - 4)/(scaledSegments.size()-1)) * i;
            long verticalMillis = intervalMillis * i + startingMillis;
            if (i == n - 1)
                verticalMillis = intervalMillis * n + startingMillis;
            cal = Calendar.getInstance();
            cal.setTimeInMillis(verticalMillis);
            String dateStr = GII.dateText(cal.getTime());
            Point textCoordinates = new Point(verticalLine, chart.bottom + unit/30);

            if (i % 2 == 0 || i == n - 1) {
                canvas.save();
                canvas.rotate(270 + 30, textCoordinates.x, textCoordinates.y);
                canvas.drawText(dateStr, textCoordinates.x - gray.measureText(dateStr + "   "), textCoordinates.y + gray.getTextSize() / 2, gray);
                canvas.restore();
            }

            via.set((lastVerticalLine + verticalLine) / 2, lastValue);
            linePath.quadTo(via.x,chart.bottom - via.y,verticalLine,chart.bottom - scaledSegment - 2);
            totalPath.lineTo(verticalLine,chart.bottom - scaledSums.get(i) - 2);
            i++;
            canvas.drawLine(verticalLine,chart.bottom,
                    verticalLine,chart.top,gray);
            lastValue = scaledSegment;
            lastVerticalLine = verticalLine;
        }
        totalPath.lineTo(verticalLine, chart.bottom - 2);
        canvas.drawPath(totalPath,circlePaintTransparent);
        canvas.drawPath(linePath,circlePaint);
    }

    public void buildBubbleGraph(String selectedId, ArrayList<Operation> graphOperation, Rect rect, Canvas canvas) {
        int unit = rect.width()/30;
        Paint white = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL_AND_STROKE);
        white.setStrokeWidth(unit/15);

        Paint gray = new Paint();
        gray.setColor(Color.GRAY);
        gray.setTextSize(unit/1.5f);
        gray.setStyle(Paint.Style.FILL_AND_STROKE);
        gray.setStrokeWidth(unit/15);


        Paint circlePaint = new Paint(circleColor[gii.selectedCircle.color % circleColor.length]);
        Paint circlePaintTransparent = new Paint();
        circlePaint.setStrokeWidth(unit / 4);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaintTransparent = new Paint(circleColor[gii.selectedCircle.color % circleColor.length]);
        circlePaintTransparent.setStrokeWidth(unit / 4);
        circlePaintTransparent.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaintTransparent.setAlpha(90);

        if (gii.selectedId.equals("none")) {
            for (Circle _circle : gii.circle)
                if (_circle.myMoney) {
                    circlePaint = new Paint(circleColor[_circle.color % circleColor.length]);
                    circlePaintTransparent = new Paint();
                    circlePaint.setStrokeWidth(unit / 4);
                    circlePaint.setStyle(Paint.Style.STROKE);
                    circlePaintTransparent = new Paint(circleColor[_circle.color % circleColor.length]);
                    circlePaintTransparent.setStrokeWidth(unit / 4);
                    circlePaintTransparent.setStyle(Paint.Style.FILL_AND_STROKE);
                    circlePaintTransparent.setAlpha(90);
                    break;
                }
        }


        //canvas.drawRect(rect,piePaint);
        //canvas.drawRect(new Rect(rect.left + 10, rect.top + 10, rect.right - 10, rect.bottom - 10),pieInnerPaint);

        ArrayList<Float> segments = new ArrayList<>();
        ArrayList<Integer> scaledSegments = new ArrayList<>();
        ArrayList<Operation> scaledOperations = new ArrayList<>();
        ArrayList<Integer> scaledSums = new ArrayList<>();

        segments.clear();
        scaledSegments.clear();

        int n = 21;
        float[] data = new float[n];
        for (int i = 0; i < n; i++)
            data[i] = 0;

        Calendar cal = Calendar.getInstance();
        cal.setTime(Collections.max(graphOperation).date);
        long startingMillis = cal.getTimeInMillis();
        cal.setTime(Collections.min(graphOperation).date);
        long endingMillis = cal.getTimeInMillis();
        Log.e("Report","bubble starting: " + startingMillis + ", ending: " + endingMillis + " operations: " + graphOperation.size());
        endingMillis -= startingMillis;
        //float

        long currentMillis = 0;
        long intervalMillis = endingMillis / n;
        float min = graphOperation.get(0).amount;
        float max = graphOperation.get(0).amount;
        for (Operation operation : graphOperation) {

            float amount = operation.amount;
            Log.e("Report", "Bubble Circles in: " + operation.circlesWayIn);
            //if (!operation.circlesWayIn.contains(selectedId) && !selectedId.equals("none"))
            //    amount *= -1;

            cal.setTime(operation.date);
            currentMillis = cal.getTimeInMillis() - startingMillis;
            int position = (int)(((float)currentMillis / (float)endingMillis) * (n-1));
            data[position] += amount;
            if (min > operation.amount)
                min = operation.amount;
            if (max < operation.amount)
                max = operation.amount;
            Log.e("Report","adding " + amount + " to " + position);
        }

        for (int i = 0; i < n; i++)
            segments.add(data[i]);

        canvas.drawRect(rect,white);

        Rect chart = new Rect(rect.left + unit * 5,rect.top + unit,rect.right - unit,rect.bottom - unit * 6);
        //canvas.drawRect(chart,gray);

        if (max == min)
            max = min + 1;
        //max += (max - min)/10;
        //min -= (max - min)/10;

        float scale = (float)chart.height()/(float)(max - min); //50/1000? 1000 * (50/1000) = 50; seems correct

        canvas.drawLine(chart.left,chart.bottom,chart.left,chart.top,gray);
        canvas.drawLine(chart.left,chart.bottom,chart.right,chart.bottom,gray);
        float whereIsZero = -700;
        if (min * max <= 0) {
            canvas.drawLine(chart.left, chart.bottom - (0 - min) * scale, chart.right + 2, chart.bottom - (0 - min) * scale, gray);
            canvas.drawText("0  ", chart.left - gray.measureText("0  "),chart.bottom - (0 - min) * scale + gray.getTextSize() / 2,gray);
            whereIsZero = (0 - min) * scale;
        }
        String nextText = gii.currency(min,"");
        if (whereIsZero > gray.getTextSize() || min * max > 0)
            canvas.drawText(nextText, chart.left - gray.measureText(nextText + "  "),chart.bottom + gray.getTextSize() / 2,gray);
        nextText = gii.currency(max,"");
        if (whereIsZero < chart.height() - gray.getTextSize())
            canvas.drawText(nextText, chart.left - gray.measureText(nextText + "  "),chart.top + gray.getTextSize() / 2,gray);

        for (Float segment : segments) {
            scaledSegments.add((int) ((segment - min) * scale));
            //Log.e("Report","adding scaled segment " + (int) ((segment - min) * scale));
        }

        for (Operation operation : graphOperation) {
            Operation t = new Operation();
            t.amount = operation.amount;
            t.date = operation.date;
            t.fromCircle = operation.fromCircle;
            t.toCircle = operation.toCircle;
            t.id = operation.id;
            t.isExpense = operation.isExpense;
            t.isIncome = operation.isIncome;
            scaledOperations.add(t);
        }
        for (Operation operation : scaledOperations)
            operation.setAmount((operation.amount - min) * scale);


        int lastValue = scaledSegments.get(0);
        int i = 0;
        int verticalLine = 0;
        int lastVerticalLine = chart.left + 2;
        Point via = new Point (0,0);
        Point viaTotal = new Point(0,0);
        for (Integer scaledSegment : scaledSegments) {
            verticalLine = chart.left + 2 + (int)((chart.width() - 4)/(scaledSegments.size()-1)) * i;
            long verticalMillis = intervalMillis * i + startingMillis;
            if (i == n - 1)
                verticalMillis = intervalMillis * n + startingMillis;
            cal = Calendar.getInstance();
            cal.setTimeInMillis(verticalMillis);
            String dateStr = GII.dateText(cal.getTime());
            Point textCoordinates = new Point(verticalLine, chart.bottom + unit/30);

            if (i % 2 == 0 || i == n - 1) {
                canvas.save();
                canvas.rotate(270 + 30, textCoordinates.x, textCoordinates.y);
                canvas.drawText(dateStr, textCoordinates.x - gray.measureText(dateStr + "   "), textCoordinates.y + gray.getTextSize() / 2, gray);
                canvas.restore();
            }

            via.set((lastVerticalLine + verticalLine) / 2, lastValue);
            i++;
            canvas.drawLine(verticalLine,chart.bottom,
                    verticalLine,chart.top,gray);
            lastValue = scaledSegment;
            lastVerticalLine = verticalLine;
        }

        for (Operation operation : scaledOperations) {
            Log.e("Report","Bubble going to draw " + operation.amount +
                    " from "+ operation.fromCircle + " to " + operation.toCircle);
            Log.e("Report","Bubble income/expense: " + operation.isIncome + "/" + operation.isExpense);
            Circle currentCircle;
            if (operation.isIncome)
                currentCircle = gii.circleById(operation.fromCircle);
            else
                currentCircle = gii.circleById(operation.toCircle);
            if (!currentCircle.id.equals(gii.selectedId)) {
                cal.setTime(operation.date);
                currentMillis = cal.getTimeInMillis() - startingMillis;
                int position = (int)(((float)currentMillis / (float)endingMillis) * chart.width());
                int diameter = chart.width()/25;
                Log.e("Report","Bubble drawing " + position + "," + operation.amount);
                rect1.set(chart.left + position - diameter, chart.bottom - (int)operation.amount - diameter,
                        chart.left + position + diameter, chart.bottom - (int)operation.amount + diameter);
                //drawIcon(currentCircle.picture, currentCircle.color, currentCircle.color, rect1 , canvas);
                circlePaint = new Paint(circleColor[currentCircle.color % circleColor.length]);
                circlePaint.setAlpha(100);
                canvas.drawCircle(chart.left + position, chart.bottom - (int)operation.amount,diameter,circlePaint);
            }
        }

    }

    public void buildBarGraph(float statExpenseTotal, Map<String, Float> statExpense, Rect rect, Canvas canvas) {
        int unit = rect.width()/30;

        Paint piePaint = new Paint();
        Paint pieInnerPaint = new Paint();
        PointF pieStartPoint = new PointF(0,0);
        PointF pieInnerStartPoint = new PointF(0,0);
        Path piePath = new Path();
        Path pieInnerPath = new Path();
        piePaint.setColor(Color.GREEN);
        piePaint.setStrokeWidth(1);
        piePaint.setStyle(Paint.Style.FILL_AND_STROKE);
        pieInnerPaint.setColor(Color.GREEN);
        pieInnerPaint.setStrokeWidth(1);
        pieInnerPaint.setStyle(Paint.Style.FILL_AND_STROKE);


        Paint white = new Paint();
        white.setColor(Color.WHITE);
        white.setStyle(Paint.Style.FILL_AND_STROKE);
        white.setStrokeWidth(unit/15);

        Paint gray = new Paint();
        gray.setColor(Color.GRAY);
        gray.setTextSize(unit/1.5f);
        gray.setStyle(Paint.Style.FILL_AND_STROKE);
        gray.setStrokeWidth(unit/15);

        Paint circlePaint = new Paint(circleColor[gii.selectedCircle.color % circleColor.length]);
        Paint circlePaintTransparent = new Paint();
        circlePaint.setStrokeWidth(unit / 4);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaintTransparent = new Paint(circleColor[gii.selectedCircle.color % circleColor.length]);
        circlePaintTransparent.setStrokeWidth(unit / 4);
        circlePaintTransparent.setStyle(Paint.Style.FILL_AND_STROKE);
        circlePaintTransparent.setAlpha(90);

        //canvas.drawRect(rect,piePaint);
        //canvas.drawRect(new Rect(rect.left + 10, rect.top + 10, rect.right - 10, rect.bottom - 10),pieInnerPaint);

        ArrayList<Float> segments = new ArrayList<>();
        ArrayList<Integer> scaledSegments = new ArrayList<>();

        segments.clear();
        scaledSegments.clear();

        int n = statExpense.size();
        float[] data = new float[n];
        String[] name = new String[n];
        String[] id = new String[n];
        Paint[] cPaint = new Paint[n];
        Paint[] iPaint = new Paint[n];
        for (int i = 0; i < n; i++)
            data[i] = 0;

        int k = 0;
        for (Map.Entry<String, Float> entry : statExpense.entrySet()) {
            piePaint.setColor(circleColor[gii.circleById(entry.getKey(), gii.circle).getColor() % circleColor.length].getColor());
            cPaint[k] = new Paint(piePaint);
            pieInnerPaint.setColor(Color.rgb(Math.min(Color.red(piePaint.getColor()) + 70, 255),
                    Math.min(Color.green(piePaint.getColor()) + 70, 255),
                    Math.min(Color.blue(piePaint.getColor()) + 70, 255)));
            iPaint[k] = new Paint(pieInnerPaint);
            float length = entry.getValue();
            data[k] = length;
            name[k] = gii.circleById(entry.getKey()).name;
            id[k] = entry.getKey();
            k++;
        }

        for (int i = 0; i < n; i++)
            segments.add(data[i]);

        float min = segments.get(0);
        float max = segments.get(0);
        int sum = 0;

        canvas.drawRect(rect,white);

        Rect chart = new Rect(rect.left + unit * 5,rect.top + unit,rect.right - unit,rect.bottom - unit * 6);
        //canvas.drawRect(chart,gray);

        for (Float segment : segments) {
            sum += segment;
            if (min > segment)
                min = segment;
            if (max < segment)
                max = segment;
        }
        //min -= sum/10; //some free space for icons
        //max += sum/10;
        if (Math.abs(min) <= (max - min)/20)
            min = - (max - min)/20;
        //This is actually better, but may be we should beautify it later:
        min = - (max - min)/20;
        float scale = (float)chart.height()/(float)(max - min); //50/1000? 1000 * (50/1000) = 50; seems correct

        canvas.drawLine(chart.left,chart.bottom,chart.left,chart.top,gray);
        canvas.drawLine(chart.left,chart.bottom,chart.right,chart.bottom,gray);
        float whereIsZero = -700;
        if (min * max <= 0) {
            canvas.drawLine(chart.left, chart.bottom - (0 - min) * scale, chart.right + 2, chart.bottom - (0 - min) * scale, gray);
            canvas.drawText("0  ", chart.left - gray.measureText("0  "),chart.bottom - (0 - min) * scale + gray.getTextSize() / 2,gray);
            whereIsZero = (0 - min) * scale;
        }
        String nextText = gii.currency(min,"");
        if (whereIsZero > gray.getTextSize())
            canvas.drawText(nextText, chart.left - gray.measureText(nextText + "  "),chart.bottom + gray.getTextSize() / 2,gray);
        nextText = gii.currency(max,"");
        if (whereIsZero < chart.height() - gray.getTextSize())
            canvas.drawText(nextText, chart.left - gray.measureText(nextText + "  "),chart.top + gray.getTextSize() / 2,gray);

        sum = 0;
        for (Float segment : segments) {
            scaledSegments.add((int) ((segment - min) * scale));
            sum += segment;
            Log.e("Report","adding scaled segment " + (int) ((segment - min) * scale));
        }

        Path linePath = new Path();
        Path totalPath = new Path();
        linePath.moveTo(chart.left + 2,chart.bottom - scaledSegments.get(0) - 2);
        totalPath.moveTo(chart.left + 2,chart.bottom - 2);
        int lastValue = scaledSegments.get(0);
        int i = 0; sum = 0;
        int verticalLine = 0;
        int nextVerticalLine = 0;
        int lastVerticalLine = chart.left + 2;
        Point via = new Point (0,0);
        Point viaTotal = new Point(0,0);
        for (Integer scaledSegment : scaledSegments) {
            verticalLine = chart.left + 2 + (int)((chart.width() - 4)/(scaledSegments.size())) * i;
            nextVerticalLine = chart.left + 2 + (int)((chart.width() - 4)/(scaledSegments.size())) * (i + 1);

            String barName = name[i];

            Point textCoordinates = new Point((verticalLine + nextVerticalLine)/2 - (int)gray.getTextSize()/2, chart.bottom + unit/30);


                canvas.save();
                canvas.rotate(270 + 30, textCoordinates.x, textCoordinates.y);
                canvas.drawText(barName, textCoordinates.x - gray.measureText(barName + "   "), textCoordinates.y + gray.getTextSize() / 2, gray);
                canvas.drawText(gii.currency(data[i],""), textCoordinates.x - gray.measureText(gii.currency(data[i],"")), textCoordinates.y + gray.getTextSize() / 2 + gray.getTextSize(), gray);
                canvas.restore();


            via.set((lastVerticalLine + verticalLine) / 2, lastValue);
            linePath.quadTo(via.x,chart.bottom - via.y,verticalLine,chart.bottom - scaledSegment - 2);
            cPaint[i].setShader(new LinearGradient(0, chart.bottom, 0,chart.bottom - scaledSegment , cPaint[i].getColor(), Color.WHITE, Shader.TileMode.MIRROR));
            canvas.drawRect(verticalLine,chart.bottom,
                    nextVerticalLine,chart.bottom - scaledSegment,cPaint[i]);

            //if (scaledSegment > chart.height()/3)
            //    canvas.drawRect(verticalLine,chart.bottom,
            //        nextVerticalLine,chart.bottom - chart.height()/3,iPaint[i]);

            //float circleHeight = Math.min(chart.bottom - scaledSegment);
            Point circleCenter = new Point((verticalLine + nextVerticalLine) / 2,
                    chart.bottom - scaledSegment - (nextVerticalLine -  verticalLine) / 3);

            if (scaledSegment > chart.height() - (nextVerticalLine -  verticalLine) / 3
                    - (nextVerticalLine - verticalLine) / 4) {
                circleCenter = new Point((verticalLine + nextVerticalLine) / 2,
                        Math.min(chart.bottom - (nextVerticalLine -  verticalLine) / 4,
                        chart.bottom - scaledSegment + (nextVerticalLine -  verticalLine) / 2));
            }

            if (min * max <= 0)
                canvas.drawLine(verticalLine, chart.bottom - (0 - min) * scale, nextVerticalLine, chart.bottom - (0 - min) * scale, gray);

            Circle findCircle = gii.circleById(id[i]);
            int radius = (nextVerticalLine - verticalLine)/4;
            drawIcon(null, findCircle.picture,findCircle.color,findCircle.color,
                    new Rect(circleCenter.x - radius, circleCenter.y - radius,
                            circleCenter.x + radius, circleCenter.y + radius),
                    0, false, canvas);

            i++;
            lastValue = scaledSegment;
            lastVerticalLine = verticalLine;
        }
        //canvas.drawPath(linePath,circlePaint);
    }

    float k = 0;
    public void showLoading(Canvas canvas) {
        //Loading...
        dkBlue.setStyle(Paint.Style.FILL_AND_STROKE);
        k = (k + 0.3f) % ((float)Math.PI * 2);
        int radius = (int)(Math.min(canvas.getHeight(),canvas.getWidth())/2.5f);
        int circleRadius = radius/10;
        PointF center = new PointF(canvas.getWidth()/2, canvas.getHeight()/2);
        for (int i = 1; i <5; i++) {
            double x = center.x + radius*Math.sin(k + i * 0.2f);
            double y = center.y + radius*Math.cos(k + i * 0.2f);
            canvas.drawCircle((int)x,(int)y,circleRadius/(5-i), dkBlue);
        }
    }
}
