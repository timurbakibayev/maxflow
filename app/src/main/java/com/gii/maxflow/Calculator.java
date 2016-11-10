package com.gii.maxflow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;

import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Stack;
import java.util.TreeMap;

/**
 * Created by Timur on 17-Nov-15.
 */
public class Calculator {
    ScaleGestureDetector _scaleDetector;

    public Rect[][] origRects = new Rect[10][10];
    public Rect[][] drawRects = new Rect[10][10];
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


    DatePickerDialog.OnDateSetListener dateTo = new
            DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    GIIApplication.gii.calendarTo.set(Calendar.YEAR, year);
                    GIIApplication.gii.calendarTo.set(Calendar.MONTH, monthOfYear);
                    GIIApplication.gii.calendarTo.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    if (GIIApplication.gii.calendarFrom.after(GIIApplication.gii.calendarTo))
                        GIIApplication.gii.calendarFrom.setTime(GIIApplication.gii.calendarTo.getTime());
                    if (GIIApplication.gii.prefs.getBoolean("askTime",false))
                        new TimePickerDialog(GIIApplication.gii.activity, timeTo, GIIApplication.gii.calendarTo
                                .get(Calendar.HOUR_OF_DAY), GIIApplication.gii.calendarTo.get(Calendar.MINUTE),true).show();
                }

            };

    TimePickerDialog.OnTimeSetListener timeTo = new
            TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    GIIApplication.gii.calendarTo.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    GIIApplication.gii.calendarTo.set(Calendar.MINUTE, minute);
                    if (GIIApplication.gii.calendarFrom.after(GIIApplication.gii.calendarTo))
                        GIIApplication.gii.calendarFrom.setTime(GIIApplication.gii.calendarTo.getTime());
                    //datePickerTo.setText(dateText(calendarTo.getTime()) + " " + timeText(calendarTo.getTime()));
                    //datePickerFrom.setText(dateText(calendarFrom.getTime()) + " " + timeText(calendarFrom.getTime()));
                }

            };

    public Circle fromCircle = new Circle();
    public Circle toCircle = new Circle();

    public Choosing choosing = Choosing.icon;
    private Graphics graphics;
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
    private Paint paintFont = new Paint();

    public String calcDisplay = "";
    public String calcDescription = "";
    public String calcResult = "";
    public String calcCurrency = "";

    public static int mSelectedIndex = 0;

    public Operation editOperation = null;

    public void init(Graphics graphics, Operation editOperation) {
        if (GIIApplication.gii.properties.defaultCurrency.equals(""))
                GIIApplication.gii.mainActivity.chooseCurrencies();

        this.editOperation = editOperation;
        if (editOperation == null) {
            this.calcCurrency = GIIApplication.gii.properties.defaultCurrency;
            this.calcResult = "0";
            this.calcDisplay = "0";
            this.calcDescription = "";
        } else {
            calcDescription = editOperation.description;
            DecimalFormat dfSimple = new DecimalFormat("#.##");
            calcDisplay = dfSimple.format(editOperation.amount);
            int checkInt = (int)editOperation.amount;
            if (checkInt == editOperation.amount)
                calcDisplay = "" + checkInt;
            calcResult = calcDisplay;
            calcCurrency = editOperation.currency;
            GIIApplication.gii.calendarTo.setTime(editOperation.date);
        }
        paintGray.setColor(Color.rgb(240,240,240));
        paintLevel1.setColor(Color.rgb(250,250,250));
        paintLevel2.setColor(Color.rgb(242,244,244));
        paintLevel3.setColor(Color.rgb(223,227,228));
        paintFont.setColor(Color.rgb(50,98,107));
        paintGray.setStrokeWidth(7);
        paintLevel1.setStrokeWidth(3);
        paintLevel2.setStrokeWidth(3);
        paintLevel3.setStrokeWidth(3);
        paintFont.setStrokeWidth(3);
        paintGray.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLevel1.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLevel2.setStyle(Paint.Style.FILL_AND_STROKE);
        paintLevel3.setStyle(Paint.Style.FILL_AND_STROKE);
        paintFont.setStyle(Paint.Style.FILL_AND_STROKE);
        paintGray.setAlpha(90);
        //paintFont.setTextSize();
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
        for (int i = 0; i < buttons.length; i++) {
            for (int j = 0; j < buttons[0].length; j++) {
                origRects[i][j] = new Rect(0, 0, 0, 0);
                drawRects[i][j] = new Rect(0, 0, 0, 0);
            }
        }
        PointF moved = new PointF(0,0);
        fromCircle = new Circle(GIIApplication.gii.circleById(GIIApplication.gii.newOperationFromCircle));
        graphics.mapToScreen(fromCircle.getCoordinates(false, moved), GIIApplication.gii.properties, moved);
        fromCircle.coordinates.set(moved);
        fromCircle.radius *= GIIApplication.gii.properties.scaleFactor;
        toCircle = new Circle(GIIApplication.gii.circleById(GIIApplication.gii.newOperationToCircle));
        graphics.mapToScreen(toCircle.getCoordinates(false, moved), GIIApplication.gii.properties, moved);
        toCircle.coordinates.set(moved);
        toCircle.radius *= GIIApplication.gii.properties.scaleFactor;
    }


    public String evaluate(String expr) {

        if (expr.equals("0") || expr.length() == 0)
            return "0.0";

        String args = "";
        int opens = 0;
        int closes = 0;
        for (int i = 0; i < expr.length(); i++) {
            if ("()*+-/".contains(expr.substring(i,i+1)))
                args = args + " ";
            args = args + expr.substring(i,i+1);
            if ("()*+-/".contains(expr.substring(i,i+1)))
                args = args + " ";
            if (expr.substring(i,i+1).equals("("))
                opens++;
            if (expr.substring(i,i+1).equals(")"))
                closes++;
        }
        for (int i = 0; i < opens - closes; i++)
            args = args + " )";
        while (args.contains("  "))
            args = args.replace("  "," ");
        args = args.trim();
        args = args + " + 0";
        try {
            return dijkstra(args.split(" "));
        } catch (Exception e) {

        }
        return "0";

        //return args;
    }

    public static String eval(String op, String val1s, String val2s) {
        //Double.parseDouble
        float val1 = GII.parseFloatFromString(val1s);
        float val2 = GII.parseFloatFromString(val2s);
        boolean percents = false;
        if (val2s.substring(val2s.length() - 1, val2s.length()).equals("%")) {
            val2 = GII.parseFloatFromString(val2s.substring(0,val2s.length()-1));
            val2 = val1 * val2 / 100;
            percents = true;
        }
        if (val1 == -7.3024f)
            throw new RuntimeException("Invalid number");

        if (val2 == -7.3024f)
            throw new RuntimeException("Invalid number");


        if (op.equals("+")) return val1 + val2 + "";
        if (op.equals("-")) return val1 - val2 + "";
        if (op.equals("/")) return val1 / val2 + "";
        if (op.equals("*")) return (percents? (val2 + "") : val1 * val2 + "");
        throw new RuntimeException("Invalid operator");
    }

    public static String dijkstra(String[] args) {

        // precedence order of operators
        TreeMap<String, Integer> precedence = new TreeMap<String, Integer>();
        precedence.put("(", 0);   // for convenience with algorithm
        precedence.put(")", 0);
        precedence.put("+", 1);   // + and - have lower precedence than * and /
        precedence.put("-", 1);
        precedence.put("*", 2);
        precedence.put("/", 2);

        Stack<String> ops  = new Stack<>();
        Stack<String> vals = new Stack<>();

        for (String s:args) {
            // token is a value
            if (!precedence.containsKey(s)) {
                vals.push(s);
                continue;
            }

            // token is an operator
            while (true) {

                // the last condition ensures that the operator with higher precedence is evaluated first
                if (ops.isEmpty() || s.equals("(") || (precedence.get(s) > precedence.get(ops.peek()))) {
                    ops.push(s);
                    break;
                }

                // evaluate expression
                String op = ops.pop();

                // but ignore left parentheses
                if (op.equals("(")) {
                    assert s.equals(")");
                    break;
                }

                // evaluate operator and two operands and push result onto value stack
                else {
                    String val2 = vals.pop();
                    String val1 = vals.pop();
                    vals.push(eval(op, val1, val2));
                }
            }
        }

        while (!ops.isEmpty()) {
            String op = ops.pop();
            String val2 = vals.pop();
            String val1 = vals.pop();
            vals.push(eval(op, val1, val2));
        }

        String result = vals.pop();
        return ((result.replace(",",".").equals("-7.3024"))?"0":result);

        //Copyright © 2002–2010, Robert Sedgewick and Kevin Wayne.
        //adapted from here: http://algs4.cs.princeton.edu/13stacks/EvaluateDeluxe.java.html
    }


    boolean needToUpdate = false;
    float dy = 0; //vertical velocity
    float lastY0 = 0f;
    float lastY1 = 0f;
    Calendar lastY0Time = Calendar.getInstance();
    Calendar lastY1Time = Calendar.getInstance();

    PointF firstGoesTo = new PointF(0,0);
    PointF secondGoesTo = new PointF(0,0);
    float radiusGoesTo = 1;
    public void update() {
        //return;
        if (needToUpdate) {
            //backgroundPosition.set(backgroundPosition.x,backgroundPosition.y - dy);
            //checkBackground();
            //dy = (float)dy / 1.2f;
            fromCircle.coordinates.set((fromCircle.coordinates.x + firstGoesTo.x)/2,
                    (fromCircle.coordinates.y + firstGoesTo.y)/2);
            toCircle.coordinates.set((toCircle.coordinates.x + secondGoesTo.x)/2,
                    (toCircle.coordinates.y + secondGoesTo.y)/2);
            needToUpdate = false;
            if (Math.max(Math.abs(fromCircle.coordinates.x - firstGoesTo.x),
                    Math.abs(fromCircle.coordinates.y - firstGoesTo.y)) > 2)
                needToUpdate = false;
            if (Math.max(Math.abs(toCircle.coordinates.x - secondGoesTo.x),
                    Math.abs(toCircle.coordinates.y - secondGoesTo.y)) > 2)
                needToUpdate = false;
            fromCircle.radius = (fromCircle.radius + radiusGoesTo)/2;
            toCircle.radius = (toCircle.radius + radiusGoesTo)/2;
            if (Math.max(Math.abs(radiusGoesTo - fromCircle.radius),
                    Math.abs(radiusGoesTo - toCircle.radius)) > 2)
                needToUpdate = false;
        }
    }


    public boolean onTouchEvent(@NonNull MotionEvent event, GII.AppState appState) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                click(event.getX(), event.getY());
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
                    //click(event.getX(), event.getY());
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
        /*int n = 0;
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
            backgroundPosition.y = 0;*/
    }

    private void click(float x, float y) {
            for (int i = 0; i < buttons.length; i++) {
                for (int j = 0; j < buttons[0].length; j++) {
                    if (drawRects[i][j].contains((int) x, (int) y)) {
                        press(buttons[i][j]);
                        calcResult = evaluate(calcDisplay);
                        return;
                    }
                }
            }
    }

    private void calculateNow() {
        //evaluate expression here
        if (calcDescription.equals(""))
            calcDescription = calcDisplay;
        //evaluate expression here
        calcResult = evaluate(calcDisplay);
        //evaluate expression here
        //calcDisplay = calcResult;
    }

    private void press(String operand) {
        if (operand.equals("<-")) {
            if (calcDisplay.length() > 0) {
                calcDisplay = calcDisplay.substring(0,calcDisplay.length() - 1);
            }
            if (calcDisplay.length() == 0) {
                calcDisplay = "0";
            }
            return;
        }

        if (operand.equals("=")) {
            calculateNow();
            return;
        }

        if (operand.equals("OK")) {
            calculateNow();
            ready = true;
            return;
        }

        if (operand.equals("C")) {
            calcDisplay = "0";
            return;
        }

        if (operand.equals("X")) {
            if (calcDisplay.length() > 0 && "/*+-".contains(calcDisplay.substring(calcDisplay.length() - 1,calcDisplay.length())))
                calcDisplay = calcDisplay.substring(0, calcDisplay.length() - 1);
            if (lastIsDigit())
                calcDisplay = calcDisplay + "*";
            return;
        }

        if (operand.equals("/")) {
            if (calcDisplay.length() > 0 && "/*+-".contains(calcDisplay.substring(calcDisplay.length() - 1,calcDisplay.length())))
                calcDisplay = calcDisplay.substring(0, calcDisplay.length() - 1);
            if (lastIsDigit())
                calcDisplay = calcDisplay + "/";
            //Log.e("calc", "/*=-".contains("-") + " contains -");
            return;
        }

        if (operand.equals("-")) {
            if (calcDisplay.length() > 0 && "/*+-".contains(calcDisplay.substring(calcDisplay.length() - 1,calcDisplay.length())))
                calcDisplay = calcDisplay.substring(0, calcDisplay.length() - 1);
            if (lastIsDigit())
                calcDisplay = calcDisplay + "-";
            return;
        }

        if (operand.equals("+")) {
            if (calcDisplay.length() > 0 && "/*+-".contains(calcDisplay.substring(calcDisplay.length() - 1,calcDisplay.length())))
                calcDisplay = calcDisplay.substring(0, calcDisplay.length() - 1);
            if (lastIsDigit())
                calcDisplay = calcDisplay + "+";
            return;
        }

        if (operand.equals("11/02/82")) {
            new DatePickerDialog(GIIApplication.gii.activity, dateTo, GIIApplication.gii.calendarTo
                    .get(Calendar.YEAR), GIIApplication.gii.calendarTo.get(Calendar.MONTH),
                    GIIApplication.gii.calendarTo.get(Calendar.DAY_OF_MONTH)).show();
            return;
        }

        if (operand.equals("...")) {
            final EditText pin = new EditText(GIIApplication.gii.activity);

            pin.setSingleLine(true);

            pin.setHint(GIIApplication.gii.activity.getString(R.string.hint_description));
            if (calcDisplay.contains("+") || calcDisplay.contains("-") || calcDisplay.contains("*") || calcDisplay.contains("/"))
                pin.setText("("+calcDisplay+") ");
            pin.setSingleLine(false);
            pin.setFocusable(true);
            pin.setFocusableInTouchMode(true);

            new AlertDialog.Builder(GIIApplication.gii.activity)
                    .setTitle(GIIApplication.gii.activity.getString(R.string.hint_description))
                    .setView(pin)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String url = pin.getText().toString();
                            calcDescription = url;
                        }
                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //return;
                }
            }).
                    show();
            return;
        }


        if (operand.equals("USD")) {
            //final EditText pin = new EditText(GIIApplication.gii.activity);
            final String[] currencyArray = GIIApplication.gii.properties.currency.replace(" ",",").split(",");

            if (currencyArray.length <= 1) {
                return;
            }

            int defaultCurIndex = 0;
            for (int i = 0; i < currencyArray.length; i++) {
                if (currencyArray[i].equals(calcCurrency))
                    defaultCurIndex = i;
            }

            new AlertDialog.Builder(GIIApplication.gii.activity)
                    //.setTitle(GIIApplication.gii.activity.getString(R.string.hint_description))
                    //.setView(pin)
                    .setSingleChoiceItems(currencyArray, defaultCurIndex, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case Dialog.BUTTON_NEGATIVE: // Cancel button selected, do nothing
                                    dialog.cancel();
                                    break;

                                case Dialog.BUTTON_POSITIVE: // OK button selected, send the data back
                                    dialog.dismiss();
                                    // message selected value to registered callbacks with the
                                    // selected value.
                                    //calcCurrency = GIIApplication.gii.properties.currency.replace(" ",",").split(",")[mSelectedIndex];
                                    //mDialogSelectorCallback.onSelectedOption(mSelectedIndex);
                                    break;

                                default: // choice item selected
                                    // store the new selected value in the static variable
                                    calcCurrency = GIIApplication.gii.properties.currency.replace(" ",",").split(",")[which];
                                    dialog.dismiss();
                                    //mSelectedIndex = which;
                                    break;
                            }
                        }
                    })
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    //return;
                }
            }).
                    show();
            return;
        }



        if (operand.equals("( )")) {
            if (lastIsDigit()) {
                int opens = 0;
                int closes = 0;
                for (int i = 0; i < calcDisplay.length(); i++) {
                    if (calcDisplay.substring(i,i+1).equals("("))
                        opens++;
                    if (calcDisplay.substring(i,i+1).equals(")"))
                        closes++;
                }
                if (opens > closes)
                    calcDisplay = calcDisplay + ")";
            }
            else
                calcDisplay = calcDisplay + "(";
            if (calcDisplay.length() > 1 &&
                    calcDisplay.substring(0,1).equals("0"))
                calcDisplay = calcDisplay.substring(1,calcDisplay.length());
            return;
        }

        calcDisplay = calcDisplay + operand;
        if (calcDisplay.length() > 1 &&
                calcDisplay.substring(0,1).equals("0"))
            calcDisplay = calcDisplay.substring(1,calcDisplay.length());

    }

    public boolean lastIsDigit() {
        return (calcDisplay.length() > 0 && !calcDisplay.equals("0") &&
                ")0123456789%".contains(calcDisplay.substring(calcDisplay.length()-1,calcDisplay.length())));
    }

    public boolean onScale(ScaleGestureDetector detector, GII.AppState appState) {
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 4));
        if (scaleFactor <= 0.1f)
            appState = GII.AppState.idle;
        return true;
    }

    int[] catOrder = {1,2,4,3,5,0};
    String[][] buttons = {
            {"C", "/", "X", "<-", "USD"},
            {"1", "2", "3", "( )", ""},
            {"4", "5", "6", "+", "11/02/82"},
            {"7", "8", "9", "-", "..."},
            {"0", "00", ".", "%", "OK"}
    };

    protected void onDraw(Canvas canvas) {
        graphics.canvasWidth = canvas.getWidth();
        graphics.canvasHeight = canvas.getHeight();
        int diameter = Math.min((int) canvas.getWidth(), (int) canvas.getHeight()) / (buttons[0].length);
        if ((int) canvas.getWidth() > (int) canvas.getHeight()) {
            diameter *= 0.7;
            firstGoesTo.set(radiusGoesTo * 2, radiusGoesTo * 2);
            secondGoesTo.set(canvas.getWidth() - radiusGoesTo * 2, radiusGoesTo * 2);
            needToUpdate = true;
            graphics.drawIcon(null, fromCircle.picture, fromCircle.color, fromCircle.color,
                    new Rect((int) fromCircle.coordinates.x - (int) fromCircle.radius, (int) fromCircle.coordinates.y - (int) fromCircle.radius,
                            (int) fromCircle.coordinates.x + (int) fromCircle.radius, (int) fromCircle.coordinates.y + (int) fromCircle.radius), 0, false, canvas);
            graphics.drawIcon(null, toCircle.picture, toCircle.color, toCircle.color,
                    new Rect((int) toCircle.coordinates.x - (int) toCircle.radius, (int) toCircle.coordinates.y - (int) toCircle.radius,
                            (int) toCircle.coordinates.x + (int) toCircle.radius, (int) toCircle.coordinates.y + (int) toCircle.radius), 0, false, canvas);
        } else {
            //graphics.drawIcon();
            radiusGoesTo = diameter / 3;
            firstGoesTo.set(radiusGoesTo*2,radiusGoesTo*2);
            secondGoesTo.set(canvas.getWidth() - radiusGoesTo*2,radiusGoesTo*2);
            needToUpdate = true;
            graphics.drawIcon(null, fromCircle.picture, fromCircle.color, fromCircle.color,
                    new Rect((int)fromCircle.coordinates.x - (int)fromCircle.radius,(int)fromCircle.coordinates.y - (int)fromCircle.radius,
                            (int)fromCircle.coordinates.x + (int)fromCircle.radius,(int)fromCircle.coordinates.y + (int)fromCircle.radius), 0, false, canvas);
            graphics.drawIcon(null, toCircle.picture, toCircle.color, toCircle.color,
                    new Rect((int)toCircle.coordinates.x - (int)toCircle.radius,(int)toCircle.coordinates.y - (int)toCircle.radius,
                            (int)toCircle.coordinates.x + (int)toCircle.radius,(int)toCircle.coordinates.y + (int)toCircle.radius), 0, false, canvas);
        }
        paintFont.setTextSize(diameter / 2);
        int x = 10;
        int y = 10;
        int offset = (int)(canvas.getWidth()/2 - diameter * buttons[0].length / 2);
        //if (choosing == Choosing.color) {
            for (int i = 0; i < buttons.length; i++) {
                for (int j = 0; j < buttons[0].length; j++) {
                    x = j * diameter + offset;
                    y = i * diameter + canvas.getHeight() - diameter * buttons.length;
                    origRects[i][j].set(x, y, x + diameter, y + diameter);
                    drawRects[i][j].set(x + diameter/15, y + diameter/15, x + diameter - diameter/15, y + diameter - diameter/15);
                    //graphics.drawIcon(pictureNo, i, i, drawRects[i][j], 0.3f, canvas);
                    if (i == 0 || j >= buttons[0].length - 2)
                        canvas.drawRect(drawRects[i][j],paintLevel2);
                    else
                        canvas.drawRect(drawRects[i][j],paintLevel1);

                    if (buttons[i][j].equals("( )")) {
                        if (lastIsDigit())
                            canvas.drawText(")", drawRects[i][j].centerX() - paintFont.measureText(")") / 2,
                                    drawRects[i][j].centerY() + paintFont.getTextSize() / 3, paintFont);
                        else
                            canvas.drawText("(", drawRects[i][j].centerX() - paintFont.measureText("(") / 2,
                                    drawRects[i][j].centerY() + paintFont.getTextSize() / 3, paintFont);

                    } else
                    if (buttons[i][j].equals("<-")) {
                        graphics.backspaceIcon.setBounds(drawRects[i][j].left + diameter / 7, drawRects[i][j].top + diameter / 7,
                                drawRects[i][j].right - diameter / 7, drawRects[i][j].bottom - diameter / 7);
                        graphics.backspaceIcon.draw(canvas);
                    } else
                    if (buttons[i][j].equals("/")) {
                        graphics.divideIcon.setBounds(drawRects[i][j].left + diameter / 7, drawRects[i][j].top + diameter / 7,
                                drawRects[i][j].right - diameter / 7, drawRects[i][j].bottom - diameter / 7);
                        graphics.divideIcon.draw(canvas);
                    } else
                    if (buttons[i][j].equals("...")) {
                        graphics.noteIcon.setBounds(drawRects[i][j].left + diameter / 7, drawRects[i][j].top + diameter / 7,
                                drawRects[i][j].right - diameter / 7, drawRects[i][j].bottom - diameter / 7);
                        graphics.noteIcon.draw(canvas);
                    } else
                    if (buttons[i][j].equals("USD")) {
                        if (calcCurrency.equals(""))
                            calcCurrency = GIIApplication.gii.properties.currency.replace(" ",",").split(",")[0];
                        canvas.drawText(calcCurrency, drawRects[i][j].centerX() - paintFont.measureText(calcCurrency) / 2,
                                drawRects[i][j].centerY() + paintFont.getTextSize() / 3, paintFont);
                    } else
                    if (buttons[i][j].equals("11/02/82")) {
                        graphics.calendarIcon.setBounds(drawRects[i][j].left + diameter / 7, drawRects[i][j].top + diameter / 7,
                                drawRects[i][j].right - diameter / 7, drawRects[i][j].bottom - diameter / 7);
                        graphics.calendarIcon.draw(canvas);
                    } else
                    {
                        canvas.drawText(buttons[i][j], drawRects[i][j].centerX() - paintFont.measureText(buttons[i][j]) / 2,
                                drawRects[i][j].centerY() + paintFont.getTextSize() / 3, paintFont);
                    }
                }
            }
        int displayLevel = (canvas.getHeight() - diameter * buttons.length) / 2;

        canvas.drawText("= " + calcResult + " " + calcCurrency,canvas.getWidth()/2 - paintFont.measureText("= " + calcResult + " " + calcCurrency)/2,
                displayLevel + paintFont.getTextSize(),paintFont);

        float textSize = (int)paintFont.measureText(calcDisplay);
        if (textSize > canvas.getWidth() * 0.9f) {
            paintFont.setTextSize(diameter/2/(textSize/(canvas.getWidth()*0.9f)));
        }
        canvas.drawText(calcDisplay,canvas.getWidth()/2 - paintFont.measureText(calcDisplay)/2,
                displayLevel,paintFont);
        //}
    }

}
