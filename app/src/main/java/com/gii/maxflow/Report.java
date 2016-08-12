package com.gii.maxflow;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Timur on 17-Nov-15.
 * The list of operations (codename Whatsapp)
 */
public class Report {


    public GII gii;
    public Report(GII gii) {
        this.gii = gii;
    }

    public boolean needToUpdateFile = false;

    public boolean showIncomeNotExpense = false;

    private Rect[] origRect = new Rect[500];
    private Rect[] drawRect = new Rect[500];
    private Rect mainIconRect = new Rect(0,0,0,0);

    private Rect[] buttonIcon;
    private Drawable[] buttonDrawable;


    private String[] billTxt;

    boolean needToUpdate = false;
    PointF dy = new PointF(0,0); //vertical velocity
    PointF lastY0 = new PointF(0,0);
    PointF lastY1 = new PointF(0,0);
    Calendar lastY0Time = Calendar.getInstance();
    Calendar lastY1Time = Calendar.getInstance();


    private Paint textPaint = new Paint();
    private Paint bigTextPaint = new Paint();
    private Paint veryBigTextPaint = new Paint();
    private Paint billTextPaint = new Paint();
    private Paint underPaint = new Paint();

    private Drawable deleteOperation;
    private Drawable editOperation;

    int canvasWidth = 0;
    int canvasHeight = 0;
    public boolean pressedHere = false;

    public enum Choosing {
        icon, color
    }
    public enum SwipeMode {
        left,right
    }

    public int swiping = 0;
    public SwipeMode swipeMode = SwipeMode.left;
    public boolean swipeActivated = false;

    public Choosing choosing = Choosing.icon;

    private float scaleFactor;
    public int maxY = 0;
    public int maxX = 0;
    public PointF backgroundPosition = new PointF(0, 0);
    private PointF lastBackgroundPosition = new PointF(0,0);
    private PointF canvasMovingStartingPoint = new PointF(0,0);

    private int moving; // 1 - Up/Down, 2 - Left/Right 
    private String currentOperation = "none";
    private ArrayList<Operation> graphOperation; //only for selected circles

    Map<String, Float> statInput = new HashMap<String, Float>();
    Map<String, Float> statOutput = new HashMap<String, Float>();
    float statInputTotal = 0;
    float statOutputTotal = 0;

    Bitmap pieInput;
    Bitmap pieOutput;
    Bitmap dinaPieInput;
    Bitmap dinaPieOutput;
    Bitmap lineGraph;
    Bitmap bubbleGraph;
    Bitmap lineBarInput;
    Bitmap lineBarOutput;

    Paint fabPaint = new Paint();

    Context context;

    public int canvasUnit = 0;

    public void init() {

        context = gii.getContext();

        Log.e("Report", "init: (" + gii.selectedId + ")" + gii.selectedCircle.name);

        canvasUnit = (int)Math.min(gii.graphics.canvasHeight,gii.graphics.canvasWidth);

        statInput = new HashMap<String, Float>();
        statOutput = new HashMap<String, Float>();
        //this is a floating button (hence titleBar color, keep it)
        fabPaint.setColor(gii.fab.getBackgroundTintList().getDefaultColor());
        fabPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.context = context;

        textPaint.setColor(gii.graphics.nameFont.getColor());
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bigTextPaint.setColor(textPaint.getColor());
        bigTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        veryBigTextPaint.setColor(textPaint.getColor());
        veryBigTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        veryBigTextPaint.setFakeBoldText(true);
        billTextPaint.setFakeBoldText(true);

        billTextPaint.setColor(textPaint.getColor());
        billTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        moving = 0;
        backgroundPosition = new PointF(0, 0);
        pressedHere = false;

        choosing = Choosing.icon;
        scaleFactor = 1;

        graphOperation = new ArrayList<>();
        statInputTotal = 0;
        statOutputTotal = 0;

        float currentExchangeRate = GIIApplication.gii.exchangeRates.getRate(GIIApplication.gii.properties.defaultCurrency);

        for (Operation _operation: gii.operation) {
            //Operation _operation = this.operation.get(i);
            if ((gii.selectedId.equals("none") ||
                    _operation.circlesWayIn.contains(gii.selectedId) ||
                    _operation.circlesWayOut.contains(gii.selectedId)) &&
                _operation.inFilter && !_operation.deleted) {

                float _operationAmount = _operation.amount;

                if (GIIApplication.gii.exchangeRates.alreadyRead) {
                    if (_operation.exchangeRate != 0)
                        _operationAmount = _operation.amount / _operation.exchangeRate * currentExchangeRate;
                    else
                        _operationAmount = _operation.amount / GIIApplication.gii.exchangeRates.getRate(_operation.currency) * currentExchangeRate;
                }


                graphOperation.add(_operation);

                _operation.isIncome = false;
                _operation.isExpense = false;
                if (!gii.selectedId.equals("none")) {
                    if (!(_operation.fromCircle.equals(gii.selectedId))) {
                        statInput.put(_operation.fromCircle, (statInput.get(_operation.fromCircle) == null) ? _operationAmount : statInput.get(_operation.fromCircle) + _operationAmount);
                        statInputTotal += _operationAmount;
                        _operation.isIncome = true;
                    }
                    if (!(_operation.toCircle.equals(gii.selectedId))) {
                        statOutput.put(_operation.toCircle, (statOutput.get(_operation.toCircle) == null) ? _operationAmount : statOutput.get(_operation.toCircle) + _operationAmount);
                        statOutputTotal += _operationAmount;
                        _operation.isExpense = true;
                    }
                }

                if (gii.selectedId.equals("none")) {
                    Circle fromCircle = gii.circleById(_operation.fromCircle);
                    Circle toCircle = gii.circleById(_operation.toCircle);
                    if (!fromCircle.myMoney && toCircle.myMoney) {
                        statInput.put(_operation.fromCircle, (statInput.get(_operation.fromCircle) == null) ? _operationAmount : statInput.get(_operation.fromCircle) + _operationAmount);
                        statInputTotal += _operationAmount;
                        _operation.isIncome = true;
                    }
                    if (fromCircle.myMoney && !toCircle.myMoney) {
                        statOutput.put(_operation.toCircle, (statOutput.get(_operation.toCircle) == null) ? _operationAmount : statOutput.get(_operation.toCircle) + _operationAmount);
                        statOutputTotal += _operationAmount;
                        _operation.isExpense = true;
                    }
                    if (!_operation.isExpense && !_operation.isIncome)
                        graphOperation.remove(graphOperation.size()-1);
                }

            }
         }

        Collections.sort(graphOperation);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        final int size = Math.min((int)gii.graphics.canvasHeight,(int)gii.graphics.canvasWidth)/2;
        //if (gi123i.prefs.getBoolean("pref_low_res",false) == true)
        //    size = size / 2;
        if (canvasUnit > 0) {
            rect1.set(0, 0,
                    (int) (canvasUnit * 13 / 15 * 2) ,
                    (int) (canvasUnit * 13 / 15)) ;
            lineBarInput = Bitmap.createBitmap(rect1.width(), rect1.height(), conf); // this creates a MUTABLE bitmap
            lineBarOutput = Bitmap.createBitmap(rect1.width(), rect1.height(), conf); // this creates a MUTABLE bitmap
            pieOutput = Bitmap.createBitmap(rect1.width(), rect1.height(), conf); // this creates a MUTABLE bitmap
            pieInput = Bitmap.createBitmap(rect1.width(), rect1.height(), conf); // this creates a MUTABLE bitmap
            dinaPieOutput = Bitmap.createBitmap(rect1.width(), rect1.height(), conf); // this creates a MUTABLE bitmap
            dinaPieInput = Bitmap.createBitmap(rect1.width(), rect1.height(), conf); // this creates a MUTABLE bitmap
            lineGraph = Bitmap.createBitmap(rect1.width(), rect1.height(), conf); // this creates a MUTABLE bitmap
            bubbleGraph = Bitmap.createBitmap(rect1.width(), rect1.height(), conf); // this creates a MUTABLE bitmap
            final Canvas canvasBarInput = new Canvas(lineBarInput);
            final Canvas canvasBarOutput = new Canvas(lineBarOutput);
            final Canvas canvasPieInput = new Canvas(pieInput);
            final Canvas canvasPieOutput = new Canvas(pieOutput);
            final Canvas canvasDinaPieInput = new Canvas(dinaPieInput);
            final Canvas canvasDinaPieOutput = new Canvas(dinaPieOutput);
            final Canvas canvasLineGraph = new Canvas(lineGraph);
            final Canvas canvasBubbleGraph = new Canvas(bubbleGraph);
            final Graphics graphicsInstance = gii.graphics;
            if (statInput.size() > 0)
                graphicsInstance.buildBarGraph(statInputTotal, statInput, rect1, canvasBarInput);
            if (statOutput.size() > 0)
                graphicsInstance.buildBarGraph(statOutputTotal, statOutput, rect1, canvasBarOutput);
            if (statInput.size() > 0) {
                graphicsInstance.buildPie(statInputTotal, statInput, rect1, canvasPieInput);
                graphicsInstance.buildDinaPie(statInputTotal, statInput, rect1, canvasDinaPieInput);
            }
            if (statOutput.size() > 0) {
                graphicsInstance.buildPie(statOutputTotal, statOutput, rect1, canvasPieOutput);
                graphicsInstance.buildDinaPie(statOutputTotal, statOutput, rect1, canvasDinaPieOutput);
            }
            if (graphOperation.size() > 0) {
                graphicsInstance.buildLineGraph(gii.selectedId, graphOperation, rect1, canvasLineGraph);
                graphicsInstance.buildBubbleGraph(gii.selectedId, graphOperation, rect1, canvasBubbleGraph);
            }
        }

    }

    public boolean onTouchEvent(@NonNull MotionEvent event, GII.AppState appState, Context context) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                //if (gi123i.checkButtons(new PointF(event.getX(), event.getY())))
                //    break;
                if (checkButtons(new PointF(event.getX(), event.getY()))) {
                    pressedHere = false;
                    break;
                }

                if (mainIconRect.contains((int)event.getX(),(int)event.getY())) {
                    showIncomeNotExpense = !showIncomeNotExpense;
                }

                pressedHere = true;
                canvasMovingStartingPoint.set((int) event.getX(), (int) event.getY());
                lastBackgroundPosition = backgroundPosition;
                dy = new PointF(0,0);
                lastY0.set(event.getX(),event.getY());
                lastY1.set(event.getX(),event.getY());
                lastY0Time = Calendar.getInstance();
                lastY1Time = Calendar.getInstance();
                needToUpdate = false;
                swiping = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedHere && moving == 0 &&
                        (Math.abs(canvasMovingStartingPoint.y - event.getY())  > 10 ||
                                Math.abs(canvasMovingStartingPoint.x - event.getX())  > 10))
                    moving = 1;
                if (pressedHere && moving == 1) {
                    backgroundPosition = new PointF(lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX()),
                             lastBackgroundPosition.y + (canvasMovingStartingPoint.y - event.getY()));
                    checkBackground();
                }

                dy = new PointF(0,0);;
                lastY0.set(lastY1);
                lastY0Time.setTime(lastY1Time.getTime());
                lastY1.set(event.getX(),event.getY());
                lastY1Time = Calendar.getInstance();
                needToUpdate = false;


                break;
            case MotionEvent.ACTION_UP:
                if (pressedHere && moving != 1) {
                    click(event.getX(), event.getY());
                }
                if (pressedHere && moving == 1) {
                    dy = new PointF((lastY1.x-lastY0.x) / (lastY1Time.getTimeInMillis() - lastY0Time.getTimeInMillis()) * 70,
                            (lastY1.y-lastY0.y) / (lastY1Time.getTimeInMillis() - lastY0Time.getTimeInMillis()) * 70);;
                    needToUpdate = true;
                }
                moving = 0;
                //backgroundPosition = new PointF(0, backgroundPosition.y);
                swiping = 0;
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean checkButtons(PointF pointF) {
        return (false);
    }

    private void deleteCircle() {
        new AlertDialog.Builder(context)
                .setTitle(gii.getContext().getString(R.string.delete_circle))
                .setMessage(gii.getContext().getString(R.string.are_you_sure))
                .setPositiveButton(gii.getContext().getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //TODO: check this part!
                        dialog.dismiss();
                        for (Circle _circle: gii.circle) {
                            if (_circle.id.equals(gii.selectedId)) {
                                _circle.setDeleted(true, gii.circle);
                                _circle.setSyncedWithCloud(false);
                            }
                        }
                        gii.updateFile(true);
                        gii.appState = GII.AppState.idle;
                    }
                }).setNegativeButton(gii.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }


    private void checkBackground() {

        if (backgroundPosition.y < 0)
            backgroundPosition.y = 0;
        if (backgroundPosition.x < 0)
            backgroundPosition.x = 0;

        backgroundPosition.y = Math.max(Math.min(backgroundPosition.y,maxY),0);
        backgroundPosition.x = Math.max(Math.min(backgroundPosition.x,maxX),0);
        swiping = 0;

    }

    private void click(float x, float y) {
    }

    public PointF screenToMap(PointF moved) {
        return (new PointF(((backgroundPosition.x - gii.graphics.canvasCenter.x + moved.x) / scaleFactor),
                ((backgroundPosition.y - gii.graphics.canvasCenter.y + moved.y) / scaleFactor)));
    }
    public void mapToScreen(PointF original, PointF moved) {
        moved.set((original.x * scaleFactor + gii.graphics.canvasCenter.x - backgroundPosition.x),
                (original.y * scaleFactor + gii.graphics.canvasCenter.y - backgroundPosition.y));
    }


    PointF point0 = new PointF();
    PointF point1 = new PointF();
    public boolean onScale(ScaleGestureDetector detector, GII.AppState appState) {
        PointF movedBackPoint = screenToMap(gii.graphics.canvasCenter);

        scaleFactor *= detector.getScaleFactor();
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 4));

        mapToScreen(movedBackPoint, point0);

        point1.set(point0.x - gii.graphics.canvasCenter.x,
                point0.y - gii.graphics.canvasCenter.y);
        backgroundPosition.set(backgroundPosition.x + point1.x,
                backgroundPosition.y + point1.y);


        moving = 0;
        //backgroundPosition.x = 0;
        checkBackground();
        return true;
    }

    int print(String s, int y, Paint paint, Canvas canvas) {
        canvas.drawText(s, canvasUnit/15 - backgroundPosition.x + canvas.getWidth()/2 * column,
                y + paint.getTextSize() / 2 - (int) backgroundPosition.y, paint);
        y += paint.getTextSize();
        return y;
    }

    int column = 0;
    int lastY = 0;

    protected void onDraw(Canvas canvas, PdfDocument document, Storage storage) {
        if (document != null)
            backgroundPosition = new PointF(0,0);

        if (document == null)
            canvas.drawRect(0,0,canvas.getWidth(),canvas.getHeight(),GIIApplication.gii.graphics.white);

        column = 0;

        veryBigTextPaint.setTextSize(canvasUnit/15 * scaleFactor);
        bigTextPaint.setTextSize(canvasUnit/18 * scaleFactor);
        textPaint.setTextSize(canvasUnit/20 * scaleFactor);
        if (document != null) {
            veryBigTextPaint.setTextSize(canvasUnit/15 * scaleFactor / 2);
            bigTextPaint.setTextSize(canvasUnit/18 * scaleFactor / 2);
            textPaint.setTextSize(canvasUnit/20 * scaleFactor / 2);
        }

        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        int y = 30;

        int baseLength = canvas.getWidth()-canvasUnit/15*2;
        Rect widgetRect = new Rect(0,0,baseLength,
                lineGraph.getHeight()/lineGraph.getWidth() * baseLength);

        if (document != null)
            widgetRect = new Rect(0,0,(int)(baseLength / 2.2f),
                    (int)(lineGraph.getHeight()/lineGraph.getWidth() * (int)(baseLength / 2.2f)));

        y = print("", y, bigTextPaint, canvas);
        if (!gii.selectedId.equals("none"))
            y = print(context.getString(R.string.report) + " (" + gii.selectedCircle.name + ")", y, veryBigTextPaint, canvas);
        else
            y = print(context.getString(R.string.report), y, veryBigTextPaint, canvas);
        if (gii.properties.filtered)
            y = print(GII.dateText(gii.properties.filterFrom) + " - " + GII.dateText(gii.properties.filterTo), y, veryBigTextPaint, canvas);
        else
            y = print(GII.dateText(gii.pageDateFrom) + " - " + GII.dateText(gii.pageDateTo), y, veryBigTextPaint, canvas);
        if (gii.properties.filtered && !gii.properties.filterText.equals(""))
            y = print(context.getString(R.string.report_text_filter) + gii.properties.filterText, y, veryBigTextPaint, canvas);
        y = print("", y, bigTextPaint, canvas);

        if (graphOperation.size() > 0) {

            lastY = y;

            y = print(context.getString(R.string.report_trend), y, bigTextPaint, canvas);

            rect1.set((int) (canvasUnit / 15) - (int)backgroundPosition.x, y - (int) backgroundPosition.y ,
                    (int) (canvasUnit / 15 + widgetRect.width() * scaleFactor - (int)backgroundPosition.x),
                    (int) ((y - (int) backgroundPosition.y) +
                            (widgetRect.width() / 2 * scaleFactor)));
            rect0.set(0, 0, lineGraph.getWidth(), lineGraph.getHeight());
            canvas.drawBitmap(lineGraph, rect0, rect1, null);
            y += (int) (rect1.height() + canvasUnit / 15);


            if (document != null) {
                column = 1;
                y = lastY;
            }

            y = print(context.getString(R.string.report_operations), y, bigTextPaint, canvas);

            rect1.set((int) (canvasUnit / 15) - (int)backgroundPosition.x + canvas.getWidth()/2 * column, y - (int) backgroundPosition.y ,
                    (int) (canvasUnit / 15 + widgetRect.width() * scaleFactor - (int)backgroundPosition.x + canvas.getWidth()/2 * column),
                    (int) ((y - (int) backgroundPosition.y) +
                            (widgetRect.width() / 2 * scaleFactor)));
            rect0.set(0, 0, bubbleGraph.getWidth(), bubbleGraph.getHeight());
            canvas.drawBitmap(bubbleGraph, rect0, rect1, null);
            y += (int) (rect1.height() + canvasUnit / 15);

        }

        column = 0;

        lastY = y;

        if (statInput.size() == 1) {
            for (Map.Entry<String, Float> entry : statInput.entrySet()) {
                if (!gii.selectedId.equals("none")) {
                    y = print(context.getString(R.string.report_circle_only_source) + " " + gii.circleById(entry.getKey()).name, y, textPaint, canvas);
                    y = print(context.getString(R.string.report_total_income) + " " + gii.currency(entry.getValue(), ""), y, textPaint, canvas);
                } else {
                    y = print(context.getString(R.string.report_only_source) + " " + gii.circleById(entry.getKey()).name, y, textPaint, canvas);
                    y = print(context.getString(R.string.report_total_income) + " " + gii.currency(entry.getValue(), ""), y, textPaint, canvas);
                }
            }
        }

        if (statInput.size() > 1) {
            if (!gii.selectedId.equals("none"))
                y = print(context.getString(R.string.report_income), y, bigTextPaint, canvas);
            else
                y = print(context.getString(R.string.report_income), y, bigTextPaint, canvas);

            rect1.set((int) (canvasUnit / 15) - (int)backgroundPosition.x, y - (int) backgroundPosition.y ,
                    (int) (canvasUnit / 15 + widgetRect.width() * scaleFactor - (int)backgroundPosition.x),
                    (int) ((y - (int) backgroundPosition.y) +
                            (widgetRect.width() / 2 * scaleFactor)));
            rect0.set(0, 0, pieInput.getWidth(), pieInput.getHeight());
            canvas.drawBitmap(pieInput, rect0, rect1, null);

            y += (int) (rect1.height() + canvasUnit / 15);

            if (document == null) {
                rect1.set((int) (canvasUnit / 15) - (int) backgroundPosition.x, y - (int) backgroundPosition.y,
                        (int) (canvasUnit / 15 + widgetRect.width() * scaleFactor - (int) backgroundPosition.x),
                        (int) ((y - (int) backgroundPosition.y) +
                                (widgetRect.width() / 2 * scaleFactor)));
                rect0.set(0, 0, dinaPieInput.getWidth(), dinaPieInput.getHeight());
                canvas.drawBitmap(dinaPieInput, rect0, rect1, null);

                y += (int) (rect1.height() + canvasUnit / 15);
            }

            rect1.set((int) (canvasUnit / 15) - (int)backgroundPosition.x, y - (int) backgroundPosition.y ,
                    (int) (canvasUnit / 15 + widgetRect.width() * scaleFactor - (int)backgroundPosition.x),
                    (int) ((y - (int) backgroundPosition.y) +
                            (widgetRect.width() / 2 * scaleFactor)));
            rect0.set(0, 0, lineBarInput.getWidth(), lineBarInput.getHeight());
            canvas.drawBitmap(lineBarInput, rect0, rect1, null);

            y += (int) (rect1.height() + canvasUnit / 15);

            y = print(context.getString(R.string.report_total_income) + " " + gii.currency(statInputTotal, ""), y, textPaint, canvas);
        }


        y = print("", y, bigTextPaint, canvas);

        if (statOutput.size() == 1) {


            if (document != null) {
                column = 1;
                y = lastY;
            }


            for (Map.Entry<String, Float> entry : statOutput.entrySet()) {
                if (!gii.selectedId.equals("none")) {
                    y = print(context.getString(R.string.report_circle_only_expense) + " "  + gii.circleById(entry.getKey()).name, y, textPaint, canvas);
                    y = print(context.getString(R.string.report_total_expense) + " " + gii.currency(entry.getValue(), ""), y, textPaint, canvas);
                } else {
                    y = print(context.getString(R.string.report_only_expense) + " " + gii.circleById(entry.getKey()).name, y, textPaint, canvas);
                    y = print(context.getString(R.string.report_total_expense) + " "  + gii.currency(entry.getValue(), ""), y, textPaint, canvas);
                }
            }
        }

        if (statOutput.size() > 1 ) {

            if (document != null) {
                column = 1;
                y = lastY;
            }

            if (!gii.selectedId.equals("none"))
                y = print(context.getString(R.string.report_expenses), y, bigTextPaint, canvas);
            else
                y = print(context.getString(R.string.report_expenses), y, bigTextPaint, canvas);

            rect1.set((int) (canvasUnit / 15) - (int)backgroundPosition.x + canvas.getWidth()/2 * column, y - (int) backgroundPosition.y ,
                    (int) (canvasUnit / 15 + widgetRect.width() * scaleFactor - (int)backgroundPosition.x + canvas.getWidth()/2 * column),
                    (int) ((y - (int) backgroundPosition.y) +
                            (widgetRect.width() / 2 * scaleFactor)));
            rect0.set(0, 0, pieOutput.getWidth(), pieOutput.getHeight());
            canvas.drawBitmap(pieOutput, rect0, rect1, null);
            y += (int) (rect1.height() + canvasUnit / 15);

            if (document == null) {
                rect1.set((int) (canvasUnit / 15) - (int) backgroundPosition.x + canvas.getWidth() / 2 * column, y - (int) backgroundPosition.y,
                        (int) (canvasUnit / 15 + widgetRect.width() * scaleFactor - (int) backgroundPosition.x + canvas.getWidth() / 2 * column),
                        (int) ((y - (int) backgroundPosition.y) +
                                (widgetRect.width() / 2 * scaleFactor)));
                rect0.set(0, 0, dinaPieOutput.getWidth(), dinaPieOutput.getHeight());
                canvas.drawBitmap(dinaPieOutput, rect0, rect1, null);
                y += (int) (rect1.height() + canvasUnit / 15);
            }
            /*if (document != null && (y + widgetRect.height() > canvasHeight * 0.9f)) {
                storage.startNewPDFPage();
                canvas = storage.page.getCanvas();
                y = 30;
            }*/


            rect1.set((int) (canvasUnit / 15) - (int)backgroundPosition.x + canvas.getWidth()/2 * column, y - (int) backgroundPosition.y ,
                    (int) (canvasUnit / 15 + widgetRect.width() * scaleFactor - (int)backgroundPosition.x + canvas.getWidth()/2 * column),
                    (int) ((y - (int) backgroundPosition.y) +
                            (widgetRect.width() / 2 * scaleFactor)));
            rect0.set(0, 0, lineBarOutput.getWidth(), lineBarOutput.getHeight());
            canvas.drawBitmap(lineBarOutput, rect0, rect1, null);

            y += (int) (rect1.height() + canvasUnit / 15);

            y = print(context.getString(R.string.report_total_expense) + " " + gii.currency(statOutputTotal, ""), y, textPaint, canvas);
        }

        column = 0;

        y = print(" ", y, bigTextPaint, canvas);

        if (graphOperation.size() == 0) {
            y = print(context.getString(R.string.report_no_operations), y, bigTextPaint, canvas);
            boolean thereIsMyMoney = false;
            for (Circle _circle: gii.circle)
                if (_circle.myMoney) {
                    thereIsMyMoney = true;
                    break;
                }
            if (!thereIsMyMoney) {
                y = print("", y, textPaint, canvas);
                y = print(context.getString(R.string.report_no_my_money_selected1), y, textPaint, canvas);
                y = print(context.getString(R.string.report_no_my_money_selected2), y, textPaint, canvas);
                y = print(context.getString(R.string.report_no_my_money_selected3), y, textPaint, canvas);
                y = print(context.getString(R.string.report_no_my_money_selected4), y, textPaint, canvas);
            }
        }
        else {
            y = print(context.getString(R.string.report_total_operations) + " " + graphOperation.size(), y, bigTextPaint, canvas);
            //y = print(context.getString(R.string.report_total_amount) + " " + gii.currency(statInputTotal - statOutputTotal,""), y, bigTextPaint, canvas);
        }

        maxY = Math.max(y + canvasUnit/5 - canvas.getHeight(),0);
        maxX = Math.max((canvasUnit / 15) + rect1.width() - canvas.getWidth() + canvasUnit / 15,0);
        //On orientation change, refresh

    }



    public void update() {
        //return;
        if (needToUpdate) {
            backgroundPosition.set(backgroundPosition.x - dy.x,
                    backgroundPosition.y - dy.y);
            checkBackground();
            dy.set ( (float) dy.x / 1.2f,(float) dy.y / 1.2f);
            if (Math.abs(dy.length()) < 1) {
                needToUpdate = false;
            }
        }
    }

    Rect rect0 = new Rect(0,0,0,0);
    Rect rect1 = new Rect(0,0,0,0);

}