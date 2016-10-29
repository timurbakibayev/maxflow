package com.gii.maxflow;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.firebase.client.Firebase;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Currency;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Timur on 20-Aug-15.
 * Draft version of a Money Flow application
 *
 * Gesture: a set of points that were touched starting from
 * clicking, including movement, and till release of the mouse/touch
 *
 * There are two types of coordinates:
 * Screen coordinates - the coordinates of an object relative to canvas, needed
 *                  for displaying or for input from a touch screen
 * Map coordinates - the coordinates of a point relative to the working map.
 * Say, you have changed the zoom, or scrolled. Then for a circle map coordinate is
 * not changed, but screen coordinate changes.
 *
 */
public class GII extends View {


    private static final String TAG = "GII" ;
    final public SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());

    public ExchangeRates exchangeRates = new ExchangeRates(getContext());

    public int loaded = 0;

    public float maxDisplayedAmount = 0;

    public enum AppState {
        idle, circleTouched, creating, circleSelected, editMode, canvasMoving, reporting, iconChoose, showOperations, chartPlotting, calculator, unreal
    }
    public AppState appState = AppState.idle;

    public static boolean needToRecalculate = false;

    public static Firebase ref = new Firebase("https://maxflow.firebaseio.com/");

    public boolean needToRedraw = true;

    public ArrayList<String> filesInCloud = new ArrayList<>();

    Calendar calendarTo = Calendar.getInstance();
    Calendar calendarFrom = Calendar.getInstance();
    final EditText datePickerTo = new EditText(this.getContext());;
    final EditText datePickerFrom = new EditText(this.getContext());;

    public Date pageDateFrom = new Date();
    public Date pageDateTo = new Date();

    public Cloud cloud;

    public Properties properties = new Properties();

    public Graphics graphics = new Graphics(this);

    public PlotChart chart1=new PlotChart(); //added by Dima
    public Icons iconWindow =new Icons();
    public Calculator calcWindow = new Calculator();

    public Charts charts = new Charts();
    public OperationListWindow operationListWindow = new OperationListWindow(this);
    public Report reportWindow = new Report(this);

    Storage storage = new Storage(this); //a class that saves and loads XML

    Circle abstractTotalCircle = new Circle();
    //Circle abstractTotalCircleWidget = new Circle();

    public java.util.ArrayList<Circle> circle = new ArrayList<>(); //the circles, loaded and saved to XML
    public java.util.ArrayList<Operation> operations = new ArrayList<>(); //the operations, loaded and saved to XML
    //public java.util.ArrayList<Circle> displayedCircle = new ArrayList<>(); //circles, always changing,
                    //used only for displaying circles on screen! never saved to XML!
    public java.util.ArrayList<Operation> displayedOperation = new ArrayList<>(); //operations, always changing,
                    //used only for displaying operations on screen! never saved to XML!

    public Geometry geometry = new Geometry(); //a class with geometry functions

    int timeToCreate = 1000; //how many timer ticks needed to create a new circle when you
                            // touch the screen


    ScaleGestureDetector _scaleDetector; //detects the "scale gesture" (two fingers)

    public PointF lastBackgroundPosition = new PointF(0, 0); //last scroll position
    public PointF canvasMovingStartingPoint = new PointF(0, 0); //where was the canvas before scrolling

    ArrayList<PointF> gesture = new ArrayList<>(); //list of all points in Map Coordinates
                            // when we move a finger on screen
    String firstGestureCircleId = "none";
    boolean doNotMove = false;

    String selectedId = "none"; //an Id of the currently selected circle
    Circle selectedCircle = new Circle("none"); //the currently selected circle
    String moveIntoId = "none"; //an Id to group into while edit mode

    PointF moveXY = new PointF(0,0);

    PointF inCircleDiff = new PointF(0,0);


    Vibrator vibe = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE); //Vibrator :)

    long counter = 0; //how many timer ticks passed after a click.
    long lastRecalculateInitiative = 0; //Last "2023" initiative
            //needed for creating new circles of going to "edit Mode"


    PointF point0 = new PointF();
    PointF point1 = new PointF();

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (!pinCodeEntered)
            return;
        graphics.drawBackground(canvas);

        if (appState == AppState.reporting) {
            reportWindow.onDraw(canvas, null, null);
            return;
        }
        if (appState == AppState.chartPlotting) {
            //chart1.plot(canvas, appState, properties, circle, operation, selectedCircle, graphics);   //added by Dima
            chart1.currLayoutProp.screenW=(int)graphics.canvasWidth;                //added by Dima
            chart1.currLayoutProp.screenH=(int)graphics.canvasHeight;               //added by Dima
            chart1.plot(canvas, appState, properties, circle, operations, selectedCircle, graphics);
            //chart1.plot(canvas, appState, properties, circle, operation,selectedCircle,graphics,properties.filterFrom,properties.filterTo); //added by Dima
            return;
        }

        if(appState!=AppState.chartPlotting)
            chart1.newPlotChartCall=true; //Added by Dima

        if (appState == AppState.iconChoose) {
            iconWindow.onDraw(canvas);
            return;
        }

        if (appState == AppState.calculator) {
            calcWindow.onDraw(canvas);
            return;
        }

        if (appState == AppState.showOperations) {
            operationListWindow.onDraw(canvas, getContext());
            return;
        }

        graphics.canvasHeight = canvas.getHeight();
        graphics.canvasWidth = canvas.getWidth();
        if (graphics.canvasCenter.x == 0) {
            graphics.canvasCenter.set(graphics.canvasWidth / 2, graphics.canvasHeight / 2);
        }

        //float rememberScale = properties.scaleFactor;
        //properties.scaleFactor = 1;
        //canvas.save();
        //canvas.translate(properties.backgroundPosition.x,properties.backgroundPosition.y);
        //canvas.scale(rememberScale,rememberScale);
        graphics.drawTheStuff(canvas, appState, properties, circle, displayedOperation, selectedId, moveIntoId, moveXY);
        //properties.scaleFactor = rememberScale;
        //canvas.restore();

        graphics.animateBottomMenu(appState == AppState.circleSelected);
        if (!selectedId.equals("none"))
            graphics.drawBottomMenu(canvas,appState);
        if (appState == AppState.circleTouched ||
                appState == AppState.creating)
            graphics.drawGestures(canvas, appState, circle, properties, firstGestureCircleId, gesture,
                    counter, timeToCreate);

        if (loaded < 4) {
            graphics.showLoading(canvas);
        }

    }

    public void switchPage(int i) {
        boolean wasEmpty = displayedOperation.size() == 0;
        chart1.toDrawPortion=0; //added by Dima
        properties.currentPageNo += i;
        //updateFile();
        if (operations.size() > 0) {
            int minPage = 100000;
            int maxPage = -100000;
            for (Operation _operation : operations) {
                if (!_operation.deleted) {
                    if (_operation.pageNo < minPage)
                        minPage = _operation.pageNo;
                    if (_operation.pageNo > maxPage)
                        maxPage = _operation.pageNo;
                }
            }
            if (properties.currentPageNo < minPage)
                properties.currentPageNo = minPage - 1;
            if (properties.currentPageNo > maxPage)
                properties.currentPageNo = maxPage + 1;
        } else
            properties.currentPageNo = 0;
        properties.syncedWithCloud = false;
        //recalculateAll();
        Log.e("RecalculateAll","initiate gii 235");
        if (appState == AppState.reporting) {
            reportWindow.init();
            reportWindow.needToUpdate = true;
        }
        postInvalidate();
    }

    String lastEmailLogin = "";
    public void refreshUser() {
        String newName = "offline";
        if (ref.getAuth() != null)
            newName = ref.getAuth().getProviderData().get("email").toString();
        if (!newName.equals(lastEmailLogin))
            loadFile("last");
        lastEmailLogin = newName;
        needToRedraw = true;
        postInvalidate();
    }

    /**
     * Check if the coordinates are over some button (bottom menu).
     * @param touchPoint screen coordinates touched
     */
    public boolean checkButtons(PointF touchPoint) {
        for (CanvasButton _button : graphics.bottomButton) {
            if (_button.rectangle.contains((int) touchPoint.x, (int) touchPoint.y)) {
                pressMenuButton(_button);
                return true;
            }
            if (_button.selected) {
                for (CanvasButton _subButton : _button.subButton)
                    if (_subButton.rectangle.contains((int) touchPoint.x, (int) touchPoint.y)) {
                        pressMenuButton(_subButton);
                        return true;
                    }
            }
        }
        return false;
    }



    /**
     * What happens if we press a button of bottom menu (for a selected circle)
     * @param button - the button pressed
     */
    private void pressMenuButton(CanvasButton button) {

        if (button.type == CanvasButton.ButtonType.delete) {
            //deleteCircle(selectedId);
        }

        if (button.type == CanvasButton.ButtonType.rename) {
            //renameCircle(selectedId);
        }

        if (button.type == CanvasButton.ButtonType.color) {
            iconWindow.init(graphics);
            appState = AppState.iconChoose;
            return;
        }

        if (button.type == CanvasButton.ButtonType.branch) {
            if (!selectedId.equals("none")) {
                showMessage("Total of selected: " + currency(selectedCircle.amountTotal,""));
                reportWindow.init();
                appState=AppState.reporting; //added by Dima
            }
        }

        if (button.type == CanvasButton.ButtonType.operations) {
            //if (!selectedId.equals("none")) {
            //    showMessage("Total of selected: " + df.format(circleById(selectedId).amountTotal));
                appState = AppState.showOperations;
                operationListWindow.init(graphics,this.getContext(),circle, operations,selectedId,properties.currentPageNo,monthName,false);
            //}
        }

        if (button.selected)
            button.openSubMenu(false);
        else {
            for (CanvasButton _button : graphics.bottomButton)
                _button.openSubMenu(false);

            if (button.type == CanvasButton.ButtonType.edit)
                button.openSubMenu(true);
        }
    }


    /**
     *
     * @param moved screen coordinates
     * @return map coordinates
     *
     */
    public PointF screenToMap(PointF moved) {
        return (new PointF(((properties.backgroundPosition.x - graphics.canvasCenter.x + moved.x) / properties.scaleFactor),
                ((properties.backgroundPosition.y - graphics.canvasCenter.y + moved.y) / properties.scaleFactor)));
    }



    public void syncFiles() {
        Log.e("syncFiles","downloading...");
        //cloud.downloadFiles(filesInCloud, storage, properties);
    }

    public String emailOrOffline = getContext().getString(R.string.offline);
    //static DecimalFormat df = new DecimalFormat("#.##");
    static DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.US);

    public String currency(float amount, String currency) {
        Locale currentLocale = Locale.getDefault();
        Currency localCurrency = Currency.getInstance(currentLocale);
        String localCurrencyCode = localCurrency.getSymbol();
        //if (currency.equals(""))
        //    currency = properties.currency;

        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(Locale.getDefault());
        String currencyOut = currencyFormatter.format(amount);

        if (currency.equals(""))
            currency = properties.defaultCurrency;

        return (df.format(amount) + " " + currency);
    }

    public void loadFile(String s) {
        loaded = 0;
        operationListWindow.monthName = monthName;
        graphics.monthName = monthName;
        if (appState == AppState.showOperations)
            graphics.operationListWindow.init(graphics,this.getContext(),circle, operations,selectedId,0,monthName,true);
        //String rememberParseUser = properties.firebaseUserEmail;
        properties = new Properties();
        Log.e("properties","new properties");
        circle = new ArrayList<>();
        operations = new ArrayList<>();

        properties.fileName = s;
        //properties.firebaseUserEmail = rememberParseUser;
        if (properties.fileName.equals("last")) {
            properties.fileName = storage.getLastFile(properties);
        }
        if (properties.fileName.equals("any")) {
            properties.fileName = storage.getAnyFile(properties);
        }
        storage.loadFile(properties, circle, operations);

        showMessage(properties.fileName);
        emailOrOffline = getContext().getString(R.string.offline);
        if (ref.getAuth() != null) {
            emailOrOffline = ref.getAuth().getProviderData().get("email").toString();
            if (!properties.owner.equals(""))
                emailOrOffline = properties.owner;
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            toolbar.setSubtitle(emailOrOffline);
        updateTitle();

        updateFile(true);
        appState = AppState.idle;
        selectedId = "none";
        selectedCircle = new Circle("none");
    }

    public void loadFileShared(String s, String owner) {
        loaded = 0;
        operationListWindow.monthName = monthName;
        graphics.monthName = monthName;
        graphics.operationListWindow.init(graphics,this.getContext(),circle, operations,selectedId,0,monthName,true);
        //String rememberParseUser = properties.firebaseUserEmail;
        properties = new Properties();
        circle = new ArrayList<>();
        operations = new ArrayList<>();

        properties.fileName = s;
        properties.owner = owner;
        //properties.firebaseUserEmail = rememberParseUser;
        //if (properties.fileName.equals("last")) {
        //    properties.fileName = storage.getLastFile(properties);
        //}
        //if (properties.fileName.equals("any")) {
        //    properties.fileName = storage.getAnyFile(properties);
        //}
        storage.loadFile(properties, circle, operations);

        showMessage(properties.fileName);
        emailOrOffline = getContext().getString(R.string.offline);
        if (ref.getAuth() != null)
            emailOrOffline = owner;
        updateTitle();
        updateFile(true);
        selectedId = "none";
        selectedCircle = new Circle("none");
        appState = AppState.idle;
    }

    public MainActivity activity = (MainActivity)getContext();

    public EditText editText;
    public void bindActivity(MainActivity activity) {
        this.activity = activity;
        editText = new EditText(activity);
        //editText.setText("hi");
        //activity.canvas.addView(editText);

        //datePickerTo = new EditText(activity);
    }
    public void updateTitle() {
        activity.setTitle(properties.computeFileNameWithoutXML());
    }


    /**
     * monthNames can have more that three letters, no problem. The problem is only with displaying.
     * I was also thinking of omitting year, if it coincides with current.
     */
    static String[] monthName;

    public static String dateText(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String year = "" + cal.get(Calendar.YEAR);
        int monthNo = cal.get(Calendar.MONTH);
        String day = "" + cal.get(Calendar.DAY_OF_MONTH);
        return (day + " " + monthName[monthNo] + " " + year);
    }

    public static String timeText(Date date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String hours = "" + cal.get(Calendar.HOUR_OF_DAY);
        String minutes = "" + cal.get(Calendar.MINUTE);
        if (minutes.length() < 2)
            minutes = "0" + minutes;
        return (hours.concat(":").concat(minutes));
    }


    /**
     * This is for improving performance. Without this function we would have to
     * recalculate all numbers and operations during reDraw, which is slow.
     *
     * Recalculating of numbers we see in the circles (amount and amountTotal)
     * and the operations we see on the screen depending on page number, month etc.
     * Aggregate several operations with the same circles by summing up.
     *
     * displayedOperation - list of operations that will be visible on screen
     */
    int numberOfRecalculations = 0;
    String actionBarDates = "";
    TextView actionBarDateText = (TextView)findViewById(R.id.action_bar_date_text);


            /*setOnClickListener(new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            GIIApplication.gii.pressFloatingButton();
        }
    });(*/
    String textStandings;

    public void recalculateAll() {
        Log.e("RecalculateAll","Calculation started...");
        abstractTotalCircle.resetDisplayAmount();
        abstractTotalCircle.resetDisplayAmountWidget();

        graphics.dkBlue.setTextSize(20);
        graphics.nameFont.setTextSize(40);

        numberOfRecalculations++;

        for (com.gii.maxflow.Operation _operation : operations) {
            _operation.calculatePath(circle);

        }

        Collections.sort(operations, new Comparator<Operation>() {
            @Override
            public int compare(Operation lhs, Operation rhs) {
                return (lhs.date.before(rhs.date)?-1:rhs.date.after(lhs.date)?1:0);
            }
        });

        Log.e("RecalculateAll","Step 1");
        String newTextStangings = "";
        int maxStandingsLength = 0;
        int maxStandingsWordLength = 0;
        for (Circle _circle : circle) {
            _circle.recalculateChildren(circle);
            _circle.resetDisplayAmount();
            _circle.resetDisplayAmountWidget();
            float amount = 0;
            Calendar tempCalendar = Calendar.getInstance();
            if (!_circle.deleted) {
                for (Operation _operation : operations) {
                    if (properties.filtered) {
                        _operation.inFilter = (_operation.date.compareTo(properties.filterFrom) >= 0 &&
                                _operation.date.compareTo(properties.filterTo) <= 0);
                        if (_operation.inFilter && !properties.filterText.trim().equals("")) {

                            for (String nextTxt : properties.filterText.trim().split(" ")) {
                                if (!(nextTxt.equals("") ||
                                        _operation.forSearch.contains(nextTxt.toUpperCase().trim()) ||
                                        nextTxt.trim().equals(GII.df.format(_operation.amount).trim()) ||
                                        lessOrGreater(nextTxt.trim(), _operation.amount)
                                ))
                                    _operation.inFilter = false;
                            }
                        }
                    } else {
                        _operation.inFilter = (_operation.pageNo == properties.currentPageNo);
                    }

                    if (!_operation.deleted) {
                        //for this page
                        if (((!_circle.myMoney || properties.filtered) && _operation.inFilter) ||
                                (!(!_circle.myMoney || properties.filtered) && _operation.pageNo <= properties.currentPageNo)) {
                            if (_operation.fromCircle.equals(_circle.id)) {
                                _circle.addDisplayAmount(_operation.currency.equals("") ? properties.defaultCurrency : _operation.currency, -_operation.amount);
                                _operation.newStandingFrom = _circle.displayAmount.get(_operation.currency.equals("") ? properties.defaultCurrency : _operation.currency);
                            }
                            if (_operation.toCircle.equals(_circle.id)) {
                                _circle.addDisplayAmount(_operation.currency.equals("") ? properties.defaultCurrency : _operation.currency, +_operation.amount);
                                _operation.newStandingTo = _circle.displayAmount.get(_operation.currency.equals("") ? properties.defaultCurrency : _operation.currency);
                            }
                        }
                        if (_circle.myMoney) {
                            if (_operation.fromCircle.equals(_circle.id)) {
                                _circle.addDisplayAmountWidget(_operation.currency.equals("") ? properties.defaultCurrency : _operation.currency, -_operation.amount);
                            }
                            if (_operation.toCircle.equals(_circle.id)) {
                                _circle.addDisplayAmountWidget(_operation.currency.equals("") ? properties.defaultCurrency : _operation.currency, +_operation.amount);
                            }
                        }
                    }
                }
            }
            for (Map.Entry<String,Float> entry : _circle.displayAmount.entrySet())
                amount += exchangeRates.convert(entry.getValue(),entry.getKey(),properties.defaultCurrency);
            //amountTotal = amount;
            _circle.setAmount(amount);
            //_circle.setAmountTotal(amountTotal);
            //if (!_circle.myMoney || properties.filtered)
            _circle.setAmountText(df.format(_circle.amount));
            // else
            //    _circle.setAmountText(df.format(_circle.amountTotal));

            _circle.setDisplayAmountTextWidth(graphics.dkBlue);
            _circle.setNameTextWidth(graphics.nameFont.measureText(_circle.name));
            _circle.setVisible(true);
            if (_circle.coordinates.x != _circle.coordinates.x ||
                    _circle.coordinates.y != _circle.coordinates.y) {
                _circle.coordinates.set(0,0);
                _circle.setSyncedWithCloud(false);
            }
            if (_circle.myMoney) {
                for (Map.Entry<String,Float> entry : _circle.displayAmount.entrySet()) {
                    abstractTotalCircle.addDisplayAmount(entry.getKey(), entry.getValue());
                    abstractTotalCircle.setDisplayAmountTextWidth(graphics.dkBlue);
                }
                if (maxStandingsWordLength < _circle.name.length())
                    maxStandingsWordLength = _circle.name.length();

                for (Map.Entry<String,Float> entry : _circle.displayAmountWidget.entrySet()) {
                    if (Math.abs(entry.getValue()) > 0.01) {
                        int len = (df.format(entry.getValue()) + " " + entry.getKey()).length();
                        if (maxStandingsLength < len)
                            maxStandingsLength = len;

                    }
                    abstractTotalCircle.addDisplayAmountWidget(entry.getKey(), entry.getValue());
                    //abstractTotalCircle.setDisplayAmountTextWidth(graphics.dkBlue);
                }

            }
        }

        Log.e(TAG, "recalculateAll: Widget word length " + maxStandingsLength + " w" + maxStandingsWordLength);
        if (maxStandingsWordLength < 5 ) // 5 is the length of "Total"
            maxStandingsLength += (5 - maxStandingsWordLength);
        Log.e(TAG, "recalculateAll: Widget word length " + maxStandingsLength + " w" + maxStandingsWordLength);

        for (Circle _circle : circle) {
            if (_circle.myMoney) {
                String nTS = "";
                if (_circle.displayAmountWidget.size() > 0)
                    nTS = nTS + _circle.name + " ";
                int i = 0;
                for (Map.Entry<String,Float> entry : _circle.displayAmountWidget.entrySet()) {
                    if (Math.abs(entry.getValue()) > 0.01) {
                        i++;
                        int len = (df.format(entry.getValue()) + " " + entry.getKey()).length();
                        if (i == 1) {
                            for (int j = 0; j < maxStandingsLength + 1 - len; j++)
                                nTS = nTS + " ";
                        }
                        nTS = nTS + df.format(entry.getValue()) + " " + entry.getKey() +"\n";
                    }
                }
                if (i > 0) {
                    newTextStangings = newTextStangings + nTS;
                }
            }
        }
        newTextStangings = newTextStangings + "----------------\nTotal";
        int i = 0;
        for (Map.Entry<String,Float> entry : abstractTotalCircle.displayAmountWidget.entrySet()) {
            if (Math.abs(entry.getValue()) > 0.01) {
                i++;
                int len = (df.format(entry.getValue()) + " " + entry.getKey()).length();
                if (i == 1) {
                    for (int j = 0; j < maxStandingsLength + 2 - len; j++)
                        newTextStangings = newTextStangings + " ";
                }
                newTextStangings = newTextStangings + df.format(entry.getValue()) + " " + entry.getKey() +"\n";
            }
        }
        if (!textStandings.equals(newTextStangings)) {
            SharedPreferences.Editor edit= prefs.edit();
            edit.putString("widgetText", newTextStangings);
            edit.commit();
            textStandings = newTextStangings;

            int[] ids = AppWidgetManager.getInstance(activity).getAppWidgetIds(new ComponentName(activity, StandingsWidgetProvider.class));
            StandingsWidgetProvider myWidget = new StandingsWidgetProvider();
            myWidget.onUpdate(activity, AppWidgetManager.getInstance(activity),ids);
        }
        Log.e("RecalculateAll","Step 2");
        for (Circle _circle : circle)
            if (_circle.childrenId.size() > 0 && (!_circle.showChildren)) {
                //Log.e("Hiding",_circle.name + " has children");
                for (Circle _circle1 : circle) {
                    if (_circle.isAChild(_circle1.id)) {
                        //_circle.setAmount(_circle.amount + _circle1.amount);

                        /*if (!_circle.myMoney || properties.filtered) {
                            if (!_circle1.myMoney || properties.filtered)
                                _circle.setAmount(_circle.amount + _circle1.amount);
                            else
                                _circle.setAmount(_circle.amount + _circle1.amountTotal);
                        } else {
                            if (!_circle1.myMoney || properties.filtered)
                                _circle.setAmountTotal(_circle.amountTotal + _circle1.amount);
                            else
                                _circle.setAmountTotal(_circle.amountTotal + _circle1.amountTotal);
                        }*/
                        //_circle.displayAmount = _circle.displayAmount + _circle1.displayAmount;
                        //_circle.addDisplayAmount(_operation.currency.equals("")?properties.defaultCurrency:_operation.currency, -_operation.amount);
                        _circle.addDisplayAmountsFromCircle(_circle1);

                        /*Log.e(TAG, "recalculateAll: added amounts from " + _circle1.name + " to " + _circle.name);
                        for (Map.Entry<String,Float> entry : _circle1.displayAmount.entrySet())
                            Log.e(TAG, "recalculateAll: " + entry.getKey() + " " + entry.getValue());
                        Log.e(TAG, "recalculateAll: result in " + _circle.name);
                        for (Map.Entry<String,Float> entry : _circle.displayAmount.entrySet())
                            Log.e(TAG, "recalculateAll: " + entry.getKey() + " " + entry.getValue());*/

                        //_circle.setAmountTotal(_circle.amountTotal + _circle1.amountTotal);
                        _circle1.setVisible(false);
                        //Log.e("Hiding",_circle1.name);
                    }
                }

                /*if (!_circle.myMoney || properties.filtered) {
                    _circle.setAmountText(df.format(_circle.amount));
                } else {
                    _circle.setAmountText(df.format(_circle.amountTotal));
                }

                _circle.setAmountTextWidth(graphics.dkBlue.measureText(_circle.amountText));
                _circle.setNameTextWidth(graphics.nameFont.measureText(_circle.name));*/
                _circle.setDisplayAmountTextWidth(graphics.dkBlue);
                //_circle.setNameTextWidth(graphics.nameFont.measureText(_circle.name));
            }

        if (prefs.getBoolean("circle_size_auto", false)) {
            float maxAbsAmount = 1;
            for (Circle _circle : circle)
                if (maxAbsAmount < _circle.amount)
                    maxAbsAmount = _circle.amount;

            for (Circle _circle : circle) {
                if (_circle.radius != Math.max(_circle.amount/maxAbsAmount * 120 + 120, 120))
                _circle.setRadius(Math.max(_circle.amount/maxAbsAmount * 120 + 120,120));
                if (_circle.radius <= 0)
                    Log.e("Achtung!!!","Radius is zero! " + _circle.name + ", " + _circle.amount + ", total " + _circle.amountTotal);
            }
        }

        graphics.dkBlue.setTextSize(20); //for operations

        displayedOperation = new ArrayList<>();


        Log.e("RecalculateAll","Step 3");
        for (Operation _operation : operations) {
            if (!_operation.deleted) {
                Circle fromCircle = circleById(_operation.circlesWayOut.get(_operation.circlesWayOut.size()-1));
                Circle toCircle = circleById(_operation.circlesWayIn.get(_operation.circlesWayIn.size()-1));
                //Circle toCircle = circleById(_operation.toCircle);
                if (!fromCircle.deleted && !toCircle.deleted && _operation.inFilter) {
                    //now decide whether we want a new displayOperation or there is one already
                    boolean thereIsAnOperationDisplayed = false;
                    boolean twoWay = false;
                    for (int j = 0; !thereIsAnOperationDisplayed && j < displayedOperation.size(); j++) {
                        if (displayedOperation.get(j).fromCircle.equals(fromCircle.id) &&
                                displayedOperation.get(j).toCircle.equals(toCircle.id)) {
                            thereIsAnOperationDisplayed = true;
                            Operation oper = displayedOperation.get(j);
                            oper.amount += _operation.amount;
                            oper.abstractCircleForMultipleOperations.addDisplayAmount(_operation.currency, _operation.amount);
                            oper.amountText = "";
                            for (Map.Entry<String, Float> entry : oper.abstractCircleForMultipleOperations.displayAmount.entrySet())
                                oper.amountText += df.format(entry.getValue()) + " " + entry.getKey() + " ";
                            oper.amountTextWidth = graphics.dkBlue.measureText(oper.amountText);
                            Collections.swap(displayedOperation, j, displayedOperation.size() - 1); //Bring this operation to end
                        }
                        if (displayedOperation.get(j).toCircle.equals(fromCircle.id) &&
                                displayedOperation.get(j).fromCircle.equals(toCircle.id)) {
                            displayedOperation.get(j).twoWay = true;
                            twoWay = true;
                        }
                    }
                    if (!thereIsAnOperationDisplayed) {
                        com.gii.maxflow.Operation oper = new Operation("does not matter", fromCircle.id, toCircle.id, _operation.amount, _operation.currency, _operation.exchangeRate, _operation.date, _operation.transactionId, _operation.pageNo, false, false, "", _operation.description, false);
                        oper.abstractCircleForMultipleOperations.addDisplayAmount(_operation.currency, _operation.amount);
                        oper.amountText = GII.df.format(_operation.amount) + " " + _operation.currency;
                        oper.amountTextWidth = graphics.dkBlue.measureText(oper.amountText);
                        displayedOperation.add(oper);
                        displayedOperation.get(displayedOperation.size()-1).twoWay = twoWay;
                    }
                }
            }
        }

        Log.e("RecalculateAll","Step 4");
        maxDisplayedAmount = 0;
        if (displayedOperation.size() > 0) {
            Date date0 = displayedOperation.get(0).date;
            Date date1 = displayedOperation.get(0).date;
            if (!properties.filtered) {
                for (Operation _operation : operations) {
                    if (_operation.pageNo == properties.currentPageNo) {
                        if (_operation.date.before(date0))
                            date0 = _operation.date;
                        if (_operation.date.after(date1))
                            date1 = _operation.date;
                    }
                }
            }

            for (Operation _displayedOperation : displayedOperation) {
                float amount = 0;
                for (Map.Entry<String,Float> entry : _displayedOperation.abstractCircleForMultipleOperations.displayAmount.entrySet())
                    amount += exchangeRates.convert(entry.getValue(),entry.getKey(),properties.defaultCurrency);

                _displayedOperation.setAbsAmountInLocalCurrency(Math.abs(
                        amount
                ));
                if (maxDisplayedAmount < _displayedOperation.absAmountInLocalCurrency)
                    maxDisplayedAmount = _displayedOperation.absAmountInLocalCurrency;
            }
            pageDateFrom = date0;
            pageDateTo = date1;
            //return ("Page " + properties.currentPageNo);
            actionBarDates = (dateText(date0).concat("\n").concat(dateText(date1)));
        }
        if (displayedOperation.size() == 0 &&
                !properties.filtered)
            actionBarDates = (getContext().getString(R.string.new_page));

        if (!properties.filtered)
            actionBarDates = actionBarDates.concat("\n" + getContext().getString(R.string.page) +" " + properties.currentPageNo);
        //actionBarDateText = (TextView)findViewById(R.id.action_bar_date_text);

        if (properties.filtered) {
            Date date0 = properties.filterFrom;
            Date date1 = properties.filterTo;
            actionBarDates = (dateText(date0).concat("\n").concat(dateText(date1)));
            if (properties.filterText != null && !properties.filterText.equals(""))
                actionBarDates = actionBarDates.concat("\n" + properties.filterText);
        }

        if (actionBarDateText != null)
            actionBarDateText.setText(actionBarDates);

        needToRedraw = true;

        Log.e("RecalculateAll","Step 5");
        if (menu != null) {
            final MenuItem leftArrow = menu.findItem(R.id.action_prevMonth);
            final MenuItem rightArrow = menu.findItem(R.id.action_nextMonth);
            if (leftArrow != null)
                leftArrow.setVisible(!properties.filtered);
            if (rightArrow != null)
                rightArrow.setVisible(!properties.filtered);
        }
        Log.e("RecalculateAll","Step 6");
        if (!properties.syncedWithCloud && properties.loaded) {
            properties.lastChangeId = prefs.getString("AndroidID","");
            properties.syncedWithCloud = true;
            if (ref.getAuth() != null)
                ref.child("maxflow/" + findOwner() + "/" + properties.computeFileNameWithoutXML() + "/properties/0").
                        setValue(properties);
            Log.e("properties","pushing to cloud:" +properties.fileName + "," + properties.currentPageNo);
        }
        Log.e("RecalculateAll","Calculation end");
    }

    private boolean lessOrGreater(String text, float amount) {
        if (!(text.substring(0,1).equals(">") ||
                text.substring(0,1).equals("<") ||
            text.substring(0,1).equals("=")))
                return false;
        float u = 0;
        try {
            if (text.substring(1,2).equals("="))
                u = Float.parseFloat(text.substring(2));
            else
                u = Float.parseFloat(text.substring(1));
        } catch (Exception e) {
            return false;
        }
        //activity.showMessage("Checking amount " + u);
        if (text.substring(0,1).equals(">"))
            return (amount >= u);
        if (text.substring(0,1).equals("<"))
            return (amount <= u);
        return (amount == u);
    }

    Properties lastProperties = new Properties();

    Circle abstractExchangeCircle = new Circle();
    Circle abstractCorrectionCircle = new Circle();
    public Circle circleById(String id, ArrayList<Circle> circle) {
        if (id.equals("Exchange"))
            return abstractExchangeCircle;
        if (id.equals("Correction"))
            return abstractCorrectionCircle;
        if (selectedCircle.id.equals(id))
            return selectedCircle;
        for (Circle _circle : circle) {
            if (_circle.id.equals(id))
                return (_circle);
        }
        return (new Circle("none"));
    }


    public Circle circleById(String id) {
        if (id.equals("Exchange"))
            return abstractExchangeCircle;
        if (selectedCircle.id.equals(id))
            return selectedCircle;
        if (id.equals("Correction")) {
            return abstractCorrectionCircle;
        }

        for (Circle _circle : circle) {
            if (_circle.id.equals(id))
                return (_circle);
        }
        return (new Circle("none"));
    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            if (appState == AppState.reporting)
                return reportWindow.onScale(detector, appState);
            if (appState == AppState.chartPlotting)
                return charts.onScale(detector, appState);
            if (appState == AppState.showOperations)
                return operationListWindow.onScale(detector, appState);
            if (appState != AppState.editMode) {
                PointF movedBackPoint = screenToMap(graphics.canvasCenter);
                properties.scaleFactor *= detector.getScaleFactor();
                properties.scaleFactor = Math.max(0.05f, Math.min(properties.scaleFactor, 4));
                graphics.mapToScreen(movedBackPoint, properties, point0);
                point1.set(point0.x - graphics.canvasCenter.x,
                        point0.y - graphics.canvasCenter.y);
                properties.backgroundPosition.set(properties.backgroundPosition.x + point1.x,
                        properties.backgroundPosition.y + point1.y);
                checkBackroundPosition();
                //properties.syncedWithCloud = false;
            } else {
                if (!selectedId.equals("none")) {
                    float radius = circleById(selectedId).radius;
                    circleById(selectedId).setRadius(Math.max(50f, Math.min(detector.getScaleFactor() * radius, 250)));
                }
            }
            return true;
        }
    }

    Context context;
    MainActivity mainActivity;

    public GII(MainActivity context) {
        super(context);
        this.context = context;
        this.mainActivity = context;

        _scaleDetector = new ScaleGestureDetector(this.getContext(), new ScaleListener());
            monthName = new String[] {getContext().getString(R.string.january), getContext().getString(R.string.february), getContext().getString(R.string.march), getContext().getString(R.string.april), getContext().getString(R.string.may), getContext().getString(R.string.june), getContext().getString(R.string.july), getContext().getString(R.string.august), getContext().getString(R.string.september), getContext().getString(R.string.october), getContext().getString(R.string.november), getContext().getString(R.string.december)};
        if (prefs.getString("AndroidID","").equals("")) {
            SharedPreferences.Editor edit= prefs.edit();
            edit.putString("AndroidID", generateNewId());
            edit.commit();
        }
        textStandings =  prefs.getString("widgetText","0 USD\n-----------\nTotal: 0 USD");
        DecimalFormatSymbols symbols = df.getDecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        df.setDecimalFormatSymbols(symbols);
    }

    public GII(Context context, AttributeSet attributeSet) {
        super(context,attributeSet);
    }

    public GII(Context context, AttributeSet attributeSet, int defStyle) {
        super(context, attributeSet, defStyle);
    }

    /**
     * This is what happens when you interact with the display.
     * Main variable here is appState:
     * default = nothing is happening, nothing is selected, just show the circles
     * scaling = we are now scaling the map
     * circleSelected = some circle is selected, show the bottom menu
     * circleTouched = when we click the screen, the first point is in the circle
     * creating = we have clicked not in the circle, so if we wait a bit, we will create a new circle
     * editMode = everything is vibrating, we can move any circle
     * canvasMoving = scrolling
     *
     * When we move the mouse, collect all points into "gestures", so when we release we
     * will be able to process them.
     *
     */
    public boolean onTouchEventNormalMode(MotionEvent event) {
        PointF movedBackPoint = screenToMap(new PointF((int) event.getX(), (int) event.getY()));
        float touchX = movedBackPoint.x;
        float touchY = movedBackPoint.y;

        //if (event.getAction() == MotionEvent.ACTION_DOWN)
        //    return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //editText.requestFocus();
                moveIntoId = "none";
                //No more artificial buttons (Sat, 27 Feb, 2016),
                //but if you delete this part, resize of icons will fail :(
                if (appState == AppState.circleSelected) {
                    if (fling || !checkButtons(new PointF(event.getX(), event.getY())))
                        appState = AppState.idle;
                }
                //appState = AppState.idle;

                if (appState == AppState.idle) {

                    if (graphics.popupNeedsToUpdate)
                        if (graphics.popupRectangle.contains((int)event.getX(),(int)event.getY())) {
                            selectedId = graphics.popupOperation.toCircle;
                            selectedCircle = circleById(selectedId);
                            appState = AppState.showOperations;
                            operationListWindow.init(graphics,this.getContext(),circle, operations,selectedId,properties.currentPageNo,monthName,false);
                            return true;
                        }
                    gesture = new ArrayList<>();
                    gesture.add(new PointF((int) touchX, (int) touchY));
                    checkInCircle(new PointF((int) touchX, (int) touchY));
                    if (!selectedId.equals("")) {
                        PointF accCoordinates = circleById(selectedId).coordinates;
                        inCircleDiff = new PointF(accCoordinates.x - touchX, accCoordinates.y - touchY);
                    }
                    firstGestureCircleId = selectedId;
                    if (fling)
                        firstGestureCircleId = "none";
                    Calendar cal = Calendar.getInstance();
                    counter = cal.getTimeInMillis(); //animation start

                    if (!selectedId.equals("none"))
                        appState = AppState.circleTouched;

                    if (selectedId.equals("none")) {
                        canvasMovingStartingPoint = new PointF((int) event.getX(), (int) event.getY());
                        lastBackgroundPosition = properties.backgroundPosition;
                        appState = AppState.creating;
                    }
                }


                if (appState == AppState.editMode) {
                    String lastSelectedId = selectedId;
                    doNotMove = false;
                    checkInCircle(new PointF((int) touchX, (int) touchY));
                    if (!selectedId.equals("")) {
                        PointF accCoordinates = circleById(selectedId).coordinates;
                        inCircleDiff = new PointF(accCoordinates.x - touchX, accCoordinates.y - touchY);
                    }
                    if (fling)
                        selectedId = "";
                    if (selectedId.equals("none")) {
                        selectedId = lastSelectedId;
                        doNotMove = true;
                        canvasMovingStartingPoint = new PointF((int) event.getX(), (int) event.getY());
                        lastBackgroundPosition = properties.backgroundPosition;
                    }
                    selectedCircle = circleById(selectedId);
                    //if (selectedId.equals("none"))
                    //    appState = AppState.idle;
                }
                moveXY = new PointF((int) touchX + inCircleDiff.x, (int) touchY + inCircleDiff.y);

                velocity = new PointF(0,0);
                lastY0 = new PointF(event.getX(),event.getY());
                lastY1 = new PointF(event.getX(),event.getY());
                lastY0Time = Calendar.getInstance();
                lastY1Time = Calendar.getInstance();
                fling = false;

                break;
            case MotionEvent.ACTION_MOVE:
                if (appState == AppState.editMode && !doNotMove) {
                    moveXY.set((int) touchX + inCircleDiff.x, (int) touchY + inCircleDiff.y);
                    String inCircleId = pointInCircle(new PointF(touchX, touchY));
                    moveIntoId = "none";
                    if (!(inCircleId.equals("none") || inCircleId.equals(selectedId))) {
                        if (prefs.getBoolean("vibrate",true))
                            vibe.vibrate(300);
                        moveIntoId = inCircleId;
                    }
                    checkBorderFinger(new PointF(event.getX(), event.getY()));
                    checkBackroundPosition();
                    //moveCircle(selectedId, new PointF(touchX, touchY));
                }
                if (appState == AppState.circleTouched) {
                    gesture.add(new PointF(touchX, touchY));
                    String prevSelectedId = selectedId;
                    checkInCircle(new PointF(touchX, touchY));
                    if (!selectedId.equals(prevSelectedId)) {
                        Circle selectedCircle = circleById(selectedId);
                        selectedCircle.setShowChildren(1, circle, prefs.getString("AndroidID",""));
                        moveIntoId = selectedId;
                        if (selectedCircle.childrenId.size() > 0) {
                            needToRecalculate = true;
                            Log.e("RecalculateAll","initiate gii 904");
                        }
                    }
                    checkBorderFinger(new PointF(event.getX(), event.getY()));
                    checkBackroundPosition();
                }
                if (appState == AppState.creating) {
                    if (geometry.distance(canvasMovingStartingPoint, new PointF(event.getX(), event.getY())) > 50)
                        appState = AppState.canvasMoving;
                }
                if (appState == AppState.canvasMoving ||
                        (appState == AppState.editMode && doNotMove)) {
                    properties.backgroundPosition = new PointF((int) (lastBackgroundPosition.x + (canvasMovingStartingPoint.x - event.getX())),
                            (int) (lastBackgroundPosition.y + (canvasMovingStartingPoint.y - event.getY())));
                    //properties.syncedWithCloud = false;

                    checkBackroundPosition();
                }

                velocity = new PointF(0,0);;
                lastY0.set(lastY1);
                lastY0Time.setTime(lastY1Time.getTime());
                lastY1.set(event.getX(),event.getY());
                lastY1Time = Calendar.getInstance();
                fling = false;

                break;
            case MotionEvent.ACTION_UP:
                checkMouseUp();
                gesture.clear();
                if (appState == AppState.canvasMoving) {
                    velocity = new PointF((lastY1.x-lastY0.x) / (lastY1Time.getTimeInMillis() - lastY0Time.getTimeInMillis()) * 10,
                            (lastY1.y-lastY0.y) / (lastY1Time.getTimeInMillis() - lastY0Time.getTimeInMillis()) * 10);
                    fling = true;
                }

                if (appState == AppState.canvasMoving ||
                        appState == AppState.circleTouched ||
                        appState == AppState.creating ){
                    appState = AppState.idle;
                }
                if (appState == AppState.editMode && !selectedId.equals("none") && !doNotMove)
                    moveCircle(selectedId,moveXY);

                if (appState == AppState.editMode && doNotMove &&
                    geometry.distance(canvasMovingStartingPoint, new PointF(event.getX(), event.getY())) < 20)
                    appState = AppState.idle;

                if (!fling)
                    updateFile(false);
                break;
            default:
                return false;
        }
        return true;
    }
    public FloatingActionButton fab;
    //public com.getbase.floatingactionbutton.FloatingActionsMenu fabMenu;
    public void pressFloatingButton(String menuName) {
        if (!pinCodeEntered)
            return;

        if (menuName.equals("operations")) {
            //if (appState == AppState.editMode || appState == AppState.showOperations || appState == AppState.reporting) {
            //    appState = AppState.idle;
            //    return;
            //}
            //if (appState == AppState.idle || appState == AppState.circleSelected) {
                appState = AppState.showOperations;
                operationListWindow.init(graphics, activity, circle, operations, selectedId, properties.currentPageNo, monthName, false);
            //    return;
            //}
            //if (appState == AppState.iconChoose) {
            //    appState = iconWindow.returnAppState;
            //    return;
            //}
        }
        if (menuName.equals("charts")) {
            //reportWindow.init();
            appState = AppState.chartPlotting;
        }
        if (menuName.equals("report")) {
            reportWindow.init();
            appState = GII.AppState.reporting;
        }


    }

    public boolean pinCodeEntered = false;

    public Menu menu;
    //---------------------------------------------FILTER DIALOG
    public void filterDialog() {
        final MenuItem leftArrow = menu.findItem(R.id.action_prevMonth);
        final MenuItem rightArrow = menu.findItem(R.id.action_nextMonth);
        final EditText descriptionBox = new EditText(activity);
        final ScrollView filterScrollView = new ScrollView(activity);
        final String[] periodText = {getContext().getString(R.string.today),
                getContext().getString(R.string.last_week),
                getContext().getString(R.string.last_month),
                getContext().getString(R.string.last_year),
                getContext().getString(R.string.filter_all)};
        final Button[] filterPeriodButton = new Button[periodText.length];
        for (int i = 0; i < periodText.length; i++) {
            final int _i = i;
            filterPeriodButton[i] = new Button(activity);
            filterPeriodButton[i].setText(periodText[i]);
            filterPeriodButton[i].setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    calendarTo = Calendar.getInstance();
                    if (_i == 4)
                        calendarTo.add(Calendar.YEAR,50);
                    if (_i == 4 && operations.size() > 0)
                        calendarTo.setTime(Collections.min(operations).date);
                    calendarTo.set(Calendar.HOUR_OF_DAY, 23);
                    calendarTo.set(Calendar.MINUTE, 59);
                    calendarTo.set(Calendar.SECOND, 59);
                    calendarTo.set(Calendar.MILLISECOND, 999);
                    datePickerTo.setText(dateText(calendarTo.getTime()) + " " + timeText(calendarTo.getTime()));

                    calendarFrom.setTime(calendarTo.getTime());
                    if (_i == 1)
                        calendarFrom.add(Calendar.DAY_OF_MONTH,-7);
                    if (_i == 2)
                        calendarFrom.add(Calendar.MONTH,-1);
                    if (_i == 3)
                        calendarFrom.add(Calendar.YEAR,-1);
                    if (_i == 4)
                        calendarFrom.add(Calendar.YEAR,-100);
                    if (_i == 4 && operations.size() > 0)
                        calendarFrom.setTime(Collections.max(operations).date);
                    if (_i > 0 && _i != 4)
                        calendarFrom.add(Calendar.DAY_OF_MONTH,1);
                    calendarFrom.set(Calendar.HOUR_OF_DAY, 0);
                    calendarFrom.set(Calendar.MINUTE, 0);
                    calendarFrom.set(Calendar.SECOND, 0);
                    calendarFrom.set(Calendar.MILLISECOND, 0);
                    datePickerFrom.setText(dateText(calendarFrom.getTime()) + " " + timeText(calendarFrom.getTime()));
                }
            });
        }
        if (properties.filtered) {
            calendarTo.setTime(properties.filterTo);
            calendarFrom.setTime(properties.filterFrom);
        } else {
            calendarTo.setTime(pageDateTo); //the dateTo from last visited page
            calendarTo.set(Calendar.HOUR_OF_DAY, 23);
            calendarTo.set(Calendar.MINUTE, 59);
            calendarTo.set(Calendar.SECOND, 59);
            calendarTo.set(Calendar.MILLISECOND, 999);
            calendarFrom.setTime(pageDateFrom); //the dateFrom from last visited page
            calendarFrom.set(Calendar.HOUR_OF_DAY, 0);
            calendarFrom.set(Calendar.MINUTE, 0);
            calendarFrom.set(Calendar.SECOND, 0);
            calendarFrom.set(Calendar.MILLISECOND, 0);
            for (Operation _oper : operations) {
                if (!_oper.deleted && _oper.pageNo == properties.currentPageNo) {
                    if (calendarFrom.after(_oper.date))
                        calendarFrom.setTime(_oper.date);
                    if (calendarTo.before(_oper.date))
                        calendarTo.setTime(_oper.date);
                }
            }
        }

        //actionBarDateText.setText("16 Fev 2013\n24 Feb 2015\nFilter!!!");

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.create();

        descriptionBox.setHint(getContext().getString(R.string.filter_text));
        if (properties.filtered) {
            descriptionBox.setText(properties.filterText);
        } else
            descriptionBox.setText("");

        datePickerTo.setText(dateText(calendarTo.getTime()) + " " + timeText(calendarTo.getTime()));
        datePickerTo.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
        datePickerTo.setRawInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
        datePickerTo.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    //InputMethodManager im = (InputMethodManager)Context.get(Context.INPUT_METHOD_SERVICE);
                    //im.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    InputMethodManager im = (InputMethodManager)builder.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(descriptionBox.getWindowToken(), 0);
                    new DatePickerDialog(getContext(), dateTo, calendarTo
                            .get(Calendar.YEAR), calendarTo.get(Calendar.MONTH),
                            calendarTo.get(Calendar.DAY_OF_MONTH)).show();
                }
                return true;
            }
        });

        datePickerFrom.setText(dateText(calendarFrom.getTime()) + " " + timeText(calendarFrom.getTime()));
        datePickerFrom.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
        datePickerFrom.setRawInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
        datePickerFrom.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                    //InputMethodManager im = (InputMethodManager)Context.get(Context.INPUT_METHOD_SERVICE);
                    //im.hideSoftInputFromWindow(input.getWindowToken(), 0);
                    InputMethodManager im = (InputMethodManager)builder.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    im.hideSoftInputFromWindow(descriptionBox.getWindowToken(), 0);
                    new DatePickerDialog(getContext(), dateFrom, calendarFrom
                            .get(Calendar.YEAR), calendarFrom.get(Calendar.MONTH),
                            calendarFrom.get(Calendar.DAY_OF_MONTH)).show();
                }
                return true;
            }
        });
        // http://stackoverflow.com/questions/17902694/how-to-create-a-textedit-for-date-only-programatically-in-android

        final LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(descriptionBox);

        ViewGroup parent = (ViewGroup) datePickerTo.getParent();

        if (parent != null)
            parent.removeAllViews();

        ViewGroup parent2 = (ViewGroup) datePickerFrom.getParent();

        if (parent2 != null)
            parent2.removeAllViews();

        layout.addView(datePickerFrom);
        layout.addView(datePickerTo);

        for (int i = 0; i  < periodText.length; i++) {
            layout.addView(filterPeriodButton[i]);
        }

        filterScrollView.addView(layout);

        String isGodMode = (prefs.getBoolean("iddqd",false)?" (God Mode ON)":"");

        builder.setTitle(getContext().getString(R.string.filter) + isGodMode)
                //.setMessage(getContext().getString(R.string.enter_amount))
                .setView(filterScrollView)
                .setPositiveButton(getContext().getString(R.string.filter), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        properties.filterText = descriptionBox.getText().toString();
                        properties.filterFrom = calendarFrom.getTime();
                        properties.filterTo = calendarTo.getTime();
                        properties.filtered = true;
                        //updateFile();
                        leftArrow.setVisible(false);
                        rightArrow.setVisible(false);
                        updateFile(true);
                        if (appState.equals(AppState.showOperations))
                            operationListWindow.init(graphics,GIIApplication.gii.getContext(),circle, operations,selectedId,properties.currentPageNo,monthName,false);
                        if (appState.equals(AppState.reporting))
                            reportWindow.init();
                        postInvalidate();
                        layout.removeAllViews();

                        if (descriptionBox.getText().toString().toUpperCase().contains("IDKFA")) {
                            activity.unlockIDKFA();
                            activity.showMessage("Offline icons unlocked!");
                        }

                        if (descriptionBox.getText().toString().toUpperCase().contains("IDDQD")) {
                            activity.unlockIDDQD();
                            activity.showMessage("God mode unlocked!");
                        }

                        properties.syncedWithCloud = false;
                    }
                }).setNegativeButton(getContext().getString(R.string.filter_reset), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                        properties.filtered = false;
                        leftArrow.setVisible(true);
                        rightArrow.setVisible(true);
                        updateFile(true);
                        if (appState.equals(AppState.showOperations))
                            operationListWindow.init(graphics,GIIApplication.gii.getContext(),circle, operations,selectedId,properties.currentPageNo,monthName,false);
                        postInvalidate();
                        properties.syncedWithCloud = false;
                        layout.removeAllViews();
            }
        }).show();

    }


    private void checkBorderFinger(PointF touchPoint) {
        if (touchPoint.x < graphics.canvasWidth*0.1f)
            properties.backgroundPosition = new PointF(properties.backgroundPosition.x - 20, properties.backgroundPosition.y);
        if (touchPoint.x > graphics.canvasWidth*0.9f)
            properties.backgroundPosition = new PointF(properties.backgroundPosition.x + 20, properties.backgroundPosition.y);
        if (touchPoint.y < graphics.canvasHeight*0.1f)
            properties.backgroundPosition = new PointF(properties.backgroundPosition.x, properties.backgroundPosition.y - 20);
        if (touchPoint.y > graphics.canvasHeight*0.9f)
            properties.backgroundPosition = new PointF(properties.backgroundPosition.x, properties.backgroundPosition.y + 20);
    }

    private void checkBackroundPosition() {
        if (circle.size() < 1)
            return;
        Float minX = circle.get(0).coordinates.x;
        Float minY = circle.get(0).coordinates.y;
        Float maxX = circle.get(0).coordinates.x;
        Float maxY = circle.get(0).coordinates.y;

        for (Circle _circle : circle) {
            if (!_circle.deleted && _circle.visible) {
                if (_circle.coordinates.x + _circle.radius < minX) minX = _circle.coordinates.x + _circle.radius;
                if (_circle.coordinates.y + _circle.radius < minY) minY = _circle.coordinates.y + _circle.radius;
                if (_circle.coordinates.x - _circle.radius > maxX) maxX = _circle.coordinates.x - _circle.radius;
                if (_circle.coordinates.y - _circle.radius > maxY) maxY = _circle.coordinates.y - _circle.radius;
            }
        }
        graphics.mapToScreen(new PointF(minX, minY), properties,point0);
        graphics.mapToScreen(new PointF(maxX, maxY), properties,point1);
        if (point0.x > graphics.canvasWidth)
            properties.backgroundPosition = new PointF(properties.backgroundPosition.x + (point0.x - graphics.canvasWidth),
                    properties.backgroundPosition.y);
        if (point1.x < 0)
            properties.backgroundPosition = new PointF(properties.backgroundPosition.x + point1.x,
                    properties.backgroundPosition.y);
        if (point0.y > graphics.canvasHeight)
            properties.backgroundPosition = new PointF(properties.backgroundPosition.x,
                    properties.backgroundPosition.y + (point0.y - graphics.canvasHeight));
        if (point1.y <0)
            properties.backgroundPosition = new PointF(properties.backgroundPosition.x,
                    properties.backgroundPosition.y + point1.y);
    }

    public boolean scaling = false;

    @Override
    public boolean onTouchEvent(@NonNull MotionEvent event) {
        needToRedraw = true;
        _scaleDetector.onTouchEvent(event);
        if (_scaleDetector.isInProgress()) {
            scaling = true;
            if (appState != AppState.reporting && appState != AppState.chartPlotting && appState != AppState.iconChoose && appState != AppState.showOperations
                    && appState != AppState.editMode)
                appState = AppState.idle;
            postInvalidate();
            return true;
        }

        else if (!scaling) {
                if (appState == AppState.showOperations) {
                    if (operationListWindow.onTouchEvent(event, appState, activity)) {
                        //checkIconWindow();
                        postInvalidate();
                        return (true);
                    }
                }
                if (appState == AppState.chartPlotting) {
                    if (chart1.onTouchEventChartMode(event, appState)) {
                        //checkIconWindow();
                        postInvalidate();
                        return (true);
                    }
                }
                if (appState == AppState.iconChoose) {
                    if (iconWindow.onTouchEvent(event, appState)) {
                        checkIconWindow();
                        postInvalidate();
                        return (true);
                    }
                }
                if (appState == AppState.calculator) {
                    if (calcWindow.onTouchEvent(event, appState)) {
                        checkCalcWindow();
                        postInvalidate();
                        return (true);
                    }
                }
                if (appState == AppState.reporting) {
                    if (reportWindow.onTouchEvent(event, appState, activity)) {
                        postInvalidate();
                        return (true);
                    }
                }
                if (onTouchEventNormalMode(event)) {
                    postInvalidate();
                    return(true);
                }
            }
        else if (event.getAction() == MotionEvent.ACTION_UP) {
            scaling = false;
            properties.syncedWithCloud = false;
            updateFile(false);
            postInvalidate();
            return true;
        }
        return false;
    }

    private void checkIconWindow() {
        if (iconWindow.ready) {
            circleById(selectedId).setPicture(iconWindow.pictureNo);
            circleById(selectedId).setColor(iconWindow.color);
            circleById(selectedId).setSyncedWithCloud(false);
            updateFile(true);
            operationListWindow.init(graphics,activity,circle, operations,selectedId,properties.currentPageNo,monthName,false);
            appState = iconWindow.returnAppState;
        }
    }


    public void updateFile(boolean recalculate) {
        //cloud.uploadCircles(circle, properties.fileName, properties);
        //cloud.uploadOperations(operation, properties.fileName, properties);
        //Skip the push :( May be in the next version
        //cloud.downloadCircles(circle, properties);
        //cloud.downloadOperations(operation, properties);
        //Log.e("Dir problem","GII.updateFile()");

        storage.saveFile(properties, circle, operations);
        if (recalculate) {
            recalculateAll();
            Log.e("RecalculateAll", "initiate gii 1301");
        }
    }

    //this function is called when we release a mouse/touch
    private void checkMouseUp() {
        //if nothing is written to gestures
        if (gesture.size() <= 0)
            return;

        //a list of touched circles in a gesture. Including "-1" between circles
        ArrayList<String> touchCircle = new ArrayList<>();
        String current = "nope"; //none!=nope intentionally
        int differentCircles = 0;
        for (PointF _gesture : gesture) {
            String nextPoint = pointInCircle(_gesture);
            if (!nextPoint.equals(current)) {
                current = nextPoint;
                boolean newCircle = true;
                for (String _touchCircle : touchCircle)
                    if (_touchCircle.equals(current))
                        newCircle = false;
                if (newCircle && !current.equals("none"))
                    differentCircles++;
                if (!current.equals("none"))
                    touchCircle.add(current);
            }
        }

        //if first and last circles are different, create a new operation
        if (touchCircle.size() > 1  && //!touchCircle.get(0).equals(touchCircle.get(touchCircle.size()-1))
                !current.equals("none"))
            newOperation(touchCircle);

        //if first and last circles are the same, show its operations
        if (touchCircle.size() > 1 && touchCircle.get(0).equals(touchCircle.get(touchCircle.size()-1))) {
            selectedId = touchCircle.get(0);
            pressMenuButton(new CanvasButton(CanvasButton.ButtonType.operations));
            return;
        }


        //we release mouse, so deselect everything
        selectedId = "none";
        selectedCircle = new Circle("none");

        //but if we touched only one circle, then select this circle and show the menu
        if (touchCircle.size() == 1) {
            selectedId = touchCircle.get(0);
            selectedCircle = circleById(selectedId);
            Calendar rightNow = Calendar.getInstance();
            if (lastUpCircle.equals(selectedId) &&
                    (rightNow.getTimeInMillis() - lastUpTime.getTimeInMillis()) < 500) {
                appState = AppState.showOperations;
                operationListWindow.init(graphics,activity,circle, operations,selectedId,properties.currentPageNo,monthName,false);
                return;
            }
            lastUpCircle = selectedId;
            lastUpTime = Calendar.getInstance();
            Circle selectedCircle = circleById(selectedId);
            if (appState != AppState.editMode) {
                selectedCircle.setShowChildren(0, circle, prefs.getString("AndroidID", ""));
                if (selectedCircle.childrenId.size() > 0) {
                    //recalculateAll();
                    needToRecalculate = true;
                    Log.e("RecalculateAll","initiate gii 1366");
                }
            }
            if (appState != AppState.editMode) {
                appState = AppState.circleSelected;
                //for (CanvasButton _button : graphics.bottomButton)
                //    _button.openSubMenu(false);
            }
        }

        if (touchCircle.size() == 0) {
            Calendar rightNow = Calendar.getInstance();
            if (lastUpCircle.equals(selectedId) &&
                    (rightNow.getTimeInMillis() - lastUpTime.getTimeInMillis()) < 500) {
                appState = AppState.showOperations;
                operationListWindow.init(graphics,activity,circle, operations,selectedId,properties.currentPageNo,monthName,false);
                return;
            }
            lastUpCircle = selectedId;
            lastUpTime = Calendar.getInstance();
        }

    }
    String lastUpCircle = "";
    Calendar lastUpTime = Calendar.getInstance();


    DatePickerDialog.OnDateSetListener dateTo = new
            DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    calendarTo.set(Calendar.YEAR, year);
                    calendarTo.set(Calendar.MONTH, monthOfYear);
                    calendarTo.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    if (calendarFrom.after(calendarTo))
                        calendarFrom.setTime(calendarTo.getTime());
                    datePickerTo.setText(dateText(calendarTo.getTime()) + " " + timeText(calendarTo.getTime()));
                    datePickerFrom.setText(dateText(calendarFrom.getTime()) + " " + timeText(calendarFrom.getTime()));
                    if (prefs.getBoolean("askTime",false))
                        new TimePickerDialog(activity, timeTo, calendarTo
                            .get(Calendar.HOUR_OF_DAY), calendarTo.get(Calendar.MINUTE),true).show();
                }

            };

    DatePickerDialog.OnDateSetListener dateFrom = new
            DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    calendarFrom.set(Calendar.YEAR, year);
                    calendarFrom.set(Calendar.MONTH, monthOfYear);
                    calendarFrom.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    if (calendarFrom.after(calendarTo))
                        calendarTo.setTime(calendarFrom.getTime());
                    datePickerTo.setText(dateText(calendarTo.getTime()) + " " + timeText(calendarTo.getTime()));
                    datePickerFrom.setText(dateText(calendarFrom.getTime()) + " " + timeText(calendarFrom.getTime()));
                    if (prefs.getBoolean("askTime",false))
                        new TimePickerDialog(activity, timeFrom, calendarFrom
                            .get(Calendar.HOUR_OF_DAY), calendarFrom.get(Calendar.MINUTE),true).show();
                }

            };

    TimePickerDialog.OnTimeSetListener timeTo = new
            TimePickerDialog.OnTimeSetListener() {

                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    calendarTo.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendarTo.set(Calendar.MINUTE, minute);
                    if (calendarFrom.after(calendarTo))
                        calendarFrom.setTime(calendarTo.getTime());
                    datePickerTo.setText(dateText(calendarTo.getTime()) + " " + timeText(calendarTo.getTime()));
                    datePickerFrom.setText(dateText(calendarFrom.getTime()) + " " + timeText(calendarFrom.getTime()));
                }

            };

    TimePickerDialog.OnTimeSetListener timeFrom = new
            TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    calendarFrom.set(Calendar.HOUR_OF_DAY, hourOfDay);
                    calendarFrom.set(Calendar.MINUTE, minute);
                    if (calendarFrom.after(calendarTo))
                        calendarTo.setTime(calendarFrom.getTime());
                    datePickerTo.setText(dateText(calendarTo.getTime()) + " " + timeText(calendarTo.getTime()));
                    datePickerFrom.setText(dateText(calendarFrom.getTime()) + " " + timeText(calendarFrom.getTime()));
                }
            };

    private boolean checkIfPaid() {
        if (prefs.getBoolean("iddqd",false))
            return true;
        Date today = new Date();
        if (ref.getAuth() != null) {
            if (MainActivity.subscription.endingDate == null)
                return (false);
            return (MainActivity.subscription.endingDate.after(today));
        }
        return (true);
    }

    private void showNotPaidWindow() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final AlertDialog dialog = builder.create();

        builder.setTitle(getContext().getString(R.string.subscription_expired))
                //.setMessage(getContext().getString(R.string.subscription_expired))
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        ((MainActivity)activity).login();
                    }
                }).setNegativeButton(getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        }).show();


    }


    private void checkCalcWindow() {
        if (calcWindow.ready) {
            if (calcWindow.editOperation == null ) { //add a new operation
                appState = AppState.idle;
                newOperation(newOperationFromCircle, newOperationToCircle, calcWindow.calcDescription, calcWindow.calcResult, calcWindow.calcCurrency);
            } else { //eidting operation
                for (Operation operation1 : operations) {
                    if (operation1.id.equals(calcWindow.editOperation.id)) {
                        operation1.setDate(calendarTo.getTime());
                        operation1.setCurrency(calcWindow.calcCurrency);
                        operation1.setAmount(parseFloatFromString(calcWindow.calcResult.toString()));
                        operation1.setDescription(calcWindow.calcDescription);
                        operation1.setSyncedWithCloud(false);
                        operationListWindow.needToUpdateFile = true;
                        appState = AppState.showOperations;
                    }
                }
            }
        }
    }


    String newOperationFromCircle = "";
    String newOperationToCircle = "";
    private void newOperation(final ArrayList<String> touchCircle) {
        calendarTo = Calendar.getInstance();
        newOperationFromCircle = touchCircle.get(0);
        newOperationToCircle = touchCircle.get(touchCircle.size() - 1);
        calcWindow.init(graphics, null);

        if (calcWindow.calcCurrency.equals("") && exchangeRates.currencyArray().length > 0) {
            calcWindow.calcCurrency = exchangeRates.currencyArray()[0];
        }

        //if (prefs.getBoolean("autofill_amount", false) && operation.size() > 0) {
        Date maxDate = new Date(0L);
        for (Operation _operation : operations)
            if (_operation.toCircle.equals(newOperationToCircle) &&
                    (maxDate == null || _operation.date.after(maxDate))) {
                //calcWindow.calcDisplay = (GII.df.format(_operation.amount));
                //calcWindow.calcDescription = (_operation.description);
                if (!_operation.currency.equals("")) {
                    calcWindow.calcCurrency = _operation.currency;
                    maxDate = _operation.date;
                }
            }

        maxDate = new Date(0L);
        for (Operation _operation : operations)
            if (_operation.fromCircle.equals(newOperationFromCircle) &&
                    _operation.toCircle.equals(newOperationToCircle) &&
                    (maxDate == null || _operation.date.after(maxDate))) {
                if (!_operation.currency.equals("")) {
                    calcWindow.calcCurrency = _operation.currency;
                    maxDate = _operation.date;
                }
            }
        //}

        appState = AppState.calculator;
    }

    //This dialog opens when we touch more than one circle:
    //Asks for an amount and creates a new operation
    private void newOperation(final String fromCircle, final String toCircle, final String defDescription, final String defAmount, final String defCurrency) {

        if (!checkIfPaid()) {
            showNotPaidWindow();
            return;
        }

        try { //anything may happen, including showing dialog when some other dialog is open,
                //so, by now, simply ignore problems
            final EditText newAmount = new EditText(activity);
            final EditText descriptionBox = new EditText(activity);

            final Spinner currency = new Spinner(activity);
            ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(activity,
                    R.array.currency_array, android.R.layout.simple_spinner_item);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            currency.setAdapter(adapter);
            int i = 0;
            for (String s : activity.getResources().getStringArray(R.array.currency_array)) {
                if (s.equals(defCurrency))
                    currency.setSelection(i);
                i++;
            }

            ArrayList<Button> descriptionHints = new ArrayList<>();

            newAmount.setHint(getContext().getString(R.string.hint_amount));
            newAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            newAmount.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            newAmount.setFocusable(true);
            newAmount.setFocusableInTouchMode(true);

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            final AlertDialog dialog = builder.create();

            newAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                    }
                }
            });

            descriptionBox.setHint(getContext().getString(R.string.description));
            //calendarTo = Calendar.getInstance();
            datePickerTo.setText(dateText(calendarTo.getTime()) + " " + timeText(calendarTo.getTime()));
            datePickerTo.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
            datePickerTo.setRawInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
            datePickerTo.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {
                        //dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
                        //InputMethodManager im = (InputMethodManager)Context.get(Context.INPUT_METHOD_SERVICE);
                        //im.hideSoftInputFromWindow(input.getWindowToken(), 0);
                        InputMethodManager im = (InputMethodManager) builder.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                        im.hideSoftInputFromWindow(descriptionBox.getWindowToken(), 0);
                        im.hideSoftInputFromWindow(newAmount.getWindowToken(), 0);
                        new DatePickerDialog(getContext(), dateTo, calendarTo
                                .get(Calendar.YEAR), calendarTo.get(Calendar.MONTH),
                                calendarTo.get(Calendar.DAY_OF_MONTH)).show();
                    }
                    return true;
                }
            });
            // http://stackoverflow.com/questions/17902694/how-to-create-a-textedit-for-date-only-programatically-in-android

            final LinearLayout layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);

            final LinearLayout layoutAmount = new LinearLayout(activity);
            layoutAmount.setOrientation(LinearLayout.HORIZONTAL);

            layoutAmount.addView(newAmount);
            layoutAmount.addView(currency);

            layout.addView(layoutAmount);
            layout.addView(descriptionBox);

            ViewGroup parent = (ViewGroup) datePickerTo.getParent();

            if (parent != null) {
                parent.removeAllViews();
                Log.e("Yahoo!", "Avoided crashing!");
            }

            layout.addView(datePickerTo);

            String prepFirst = fromCircle;
            final String last = toCircle;
            Map<String, Integer> mFromCircle = new HashMap<String, Integer>();

            // Initialize frequency table from command line
            if (prepFirst.equals(last)) {
                for (Operation _oper : operations) {
                    if (_oper.toCircle.equals(last)) {
                        //Integer freq = m.get(_oper.fromCircle);
                        mFromCircle.put(_oper.fromCircle, (mFromCircle.get(_oper.fromCircle) == null) ? 1 : mFromCircle.get(_oper.fromCircle) + 1);
                    }
                }
                Map.Entry<String, Integer> maxEntry = null;

                for (Map.Entry<String, Integer> entry : mFromCircle.entrySet()) {
                    if (maxEntry == null || entry.getValue().compareTo(maxEntry.getValue()) >= 0) {
                        maxEntry = entry;
                    }
                }

                if (maxEntry == null)
                    return;
                prepFirst = maxEntry.getKey();
            }
            ;
            /*if (prefs.getBoolean("autofill_amount", false) && operation.size() > 0) {
                Date maxDate = new Date(0L);
                for (Operation _operation : operation)
                    if (_operation.fromCircle.equals(prepFirst) &&
                            _operation.toCircle.equals(last) &&
                            (maxDate == null || _operation.date.after(maxDate))) {
                        newAmount.setText(GII.df.format(_operation.amount));
                        descriptionBox.setText(_operation.description);
                        maxDate = _operation.date;
                    }
            }*/
            newAmount.setText(defAmount);
            descriptionBox.setText(defDescription);
            final String first = prepFirst;
            //final String firstCircleName = circleById(first).name;
            //final String lastCircleName = circleById(last).name;
            final MenuItem leftArrow = menu.findItem(R.id.action_prevMonth);
            final MenuItem rightArrow = menu.findItem(R.id.action_nextMonth);


            //Now we do not need any additional dialog window
            int transactionId = 0;
            float amount = parseFloatFromString(defAmount.toString());
            addOperation(new com.gii.maxflow.Operation(generateNewId(), first, last, amount, defCurrency, GIIApplication.gii.exchangeRates.getRate(defCurrency), calendarTo.getTime(), transactionId + 1, properties.currentPageNo, false, false, "", descriptionBox.getText().toString(), false));
            properties.filtered = false;
            leftArrow.setVisible(true);
            rightArrow.setVisible(true);
            updateFile(true);
            postInvalidate();

        /*builder.setTitle(firstCircleName.concat("->").concat(lastCircleName))
                    //.setMessage(getContext().getString(R.string.enter_amount))
                    .setView(layout)
                    .setPositiveButton(getContext().getString(R.string.transfer), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            float amount = parseFloatFromString(newAmount.getText().toString());
                            if (amount != -7.3024f) {
                                int transactionId = 0;
                                addOperation(new com.gii.maxflow.Operation(generateNewId(), first, last, amount, defCurrency, 0, calendarTo.getTime(), transactionId + 1, properties.currentPageNo, false, false, "", descriptionBox.getText().toString(), false));
                                properties.filtered = false;
                                leftArrow.setVisible(true);
                                rightArrow.setVisible(true);
                                updateFile();
                                invalidate();
                            }
                            layout.removeAllViews();
                        }
                    }).setNegativeButton(getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    layout.removeAllViews();
                }
            }).show();*/
        } catch (Exception e) {
            //nothing, but would be good to implement some log mechanism
        }
    }


    //Given a string, find float. Needed when we create a new circle from a dialog
    public static float parseFloatFromString(String s) {
        float result = 0;
        if ((s != null) && !s.equals("")) {
            try {
                result = Float.parseFloat(s.replace(",", "."));
            } catch (Throwable ignored) {
                result = -7.3024f;
            }


        }
        return (result);
    }

    public static String generateNewId() {
        SecureRandom random = new SecureRandom();
        return(new BigInteger(64, random).toString(32));
    }

    //moves a circle with given id to the new point
    private void moveCircle(String id, PointF point) {
        for (Circle _circle : circle) {
            if (_circle.id.equals(id)) {
                if (moveIntoId.equals("none")) {
                    _circle.setCoordinates(new PointF(point.x,point.y),circle,prefs.getString("AndroidID",""));
                    _circle.setSyncedWithCloud(false);
                } else {
                    if (!_circle.childrenId.contains(moveIntoId)) {
                        _circle.setParentId(moveIntoId);
                        _circle.setSyncedWithCloud(false);
                        moveIntoId = "";
                        selectedId = "none";
                        selectedCircle = new Circle("none");
                    }
                    else {
                        moveIntoId = "";
                        selectedId = "none";
                        selectedCircle = new Circle("none");
                        Log.e(TAG, "moveCircle: cannot have a cycle!");
                    }
                }
            }
        }
    }

    //check whether some point is in the circle or not
    //coordinate system = map, not screen
    private String pointInCircle(PointF point) {
        ArrayList<String> inCircles = new ArrayList<>();
        for (Circle acc : circle) {
            if (!acc.deleted && acc.visible)
                if (geometry.distance(point, acc.coordinates) < acc.radius)
                    return (acc.id);
        }
        return ("none");
    }

    //check whether some point is in the circle or not
    //coordinate system = map, not screen
    private void checkInCircle(PointF point) {
        String pCircle = pointInCircle(point);
        if (!selectedId.equals(pCircle)) {
            selectedId = pCircle;
            if (!selectedId.equals("none"))
                if (prefs.getBoolean("vibrate",true))
                    vibe.vibrate(100);
        }
        selectedCircle = circleById(selectedId);
    }

    //simply show a toast message
    public void showMessage(String s) {
        try {
            Toast.makeText(this.getContext(), s, Toast.LENGTH_SHORT).show();
        } catch (Exception e) {

        }
    }

    //Add a circle by user interface
    public void newCircle(final PointF point, final float radius) {

        try {
            final EditText newName = new EditText(activity);
            final EditText newAmount = new EditText(activity);

            newName.setHint(getContext().getString(R.string.hint_cash));
            newName.setSingleLine(true);

            newAmount.setHint(getContext().getString(R.string.hint_start_amount));
            newAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            newAmount.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            newAmount.setFocusable(true);
            newAmount.setFocusableInTouchMode(true);

            final LinearLayout layout = new LinearLayout(activity);
            layout.setOrientation(LinearLayout.VERTICAL);

            layout.addView(newName);

            layout.addView(newAmount);

            new AlertDialog.Builder(activity)
                    .setTitle(activity.getString(R.string.new_circle))
                    .setMessage(activity.getString(R.string.new_circle_description))
                    .setView(layout)
                    .setPositiveButton(getContext().getString(R.string.create), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            String url = newName.getText().toString();
                            String newId = generateNewId();
                            boolean isMyMoney = false; //xxxxxxxxxxxxxxxxx
                            addCircle(new Circle(newId, "", point, radius, url, false, 0, 0, false, false, "", isMyMoney, false, 0));

                            float amount = parseFloatFromString(newAmount.getText().toString());
                            if (amount != -7.3024f && amount != 0) {
                                int transactionId = 0;
                                String first = "Correction";
                                String last = newId;
                                calendarTo = Calendar.getInstance();
                                addOperation(new Operation(generateNewId(), first, last, amount, GIIApplication.gii.properties.defaultCurrency, 0, calendarTo.getTime(), transactionId + 1, properties.currentPageNo, false, false, "", getContext().getString(R.string.hint_start_amount), false));
                                updateFile(true);
                                postInvalidate();
                            }

                            selectedId = newId;
                            selectedCircle = circleById(selectedId);
                            iconWindow.init(graphics);
                            iconWindow.returnAppState = AppState.idle;
                            appState = AppState.iconChoose;
                            updateFile(true);
                        }
                    }).setNegativeButton(getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).show();
        } catch (Exception e) {
            //ignore for a while
        }
    }
    //Ask for pin code pinCode
    public void askPinCode() {

        final EditText pin = new EditText(activity);

        pin.setSingleLine(true);

        pin.setHint(getContext().getString(R.string.hint_pin));
        pin.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        pin.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        pin.setFocusable(true);
        pin.setFocusableInTouchMode(true);

        new AlertDialog.Builder(activity)
                .setTitle(getContext().getString(R.string.hint_pin))
                .setView(pin)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String url = pin.getText().toString();
                        if (url.equals(prefs.getString("pin", ""))) {
                            pinCodeEntered = true;
                        }
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                askingForPin = false;
                return;
            }
        }).
                show();
    }

    public String findOwner() {
        if (!properties.owner.equals(""))
            return properties.owner;
        return ref.getAuth().getUid();
    }

    public void addCircle(Circle _circle) {
        if (ref.getAuth() != null) {
            GII.ref.child("maxflow/" + findOwner() + "/" + properties.computeFileNameWithoutXML() + "/circles/" + _circle.id).
                    setValue(_circle);
            circle.add(_circle);
        } else
            circle.add(_circle);
        if (prefs.getBoolean("sounds",false))
            MainActivity.beep();
    }

    public int correspondingPage(Date date, int defaultPage) {
        Log.e("GII", "correspondingPage: start with " + operations.size() + " operations");
        int t = defaultPage;
        //check if we need to go to the right
        //and find the borders of the pages
        Map<Integer, Date> dateFrom = new HashMap<Integer, Date>();
        for (Operation _operation : operations) {
            if (!_operation.deleted) {
                if (_operation.pageNo > t &&
                        _operation.date.compareTo(date) <= 0)
                    t = _operation.pageNo;
                dateFrom.put(_operation.pageNo, (dateFrom.get(_operation.pageNo) == null) ? _operation.date :
                        dateFrom.get(_operation.pageNo).after(_operation.date) ? _operation.date : dateFrom.get(_operation.pageNo));
            }
        }

        //now we need to check if we have to move to the left
        for (Map.Entry<Integer, Date> entry : dateFrom.entrySet())
        {
            //Log.e("GII", "correspondingPage: page " + entry.getKey() + "date " + dateText(entry.getValue()));
            if (entry.getValue().after(date) &&
                    entry.getKey() <= t)
                t = entry.getKey();
        }

        return t;
    }

    boolean fling = false;
    PointF velocity = new PointF(0,0); //vertical velocity
    PointF lastY0 = new PointF(0,0);
    PointF lastY1 = new PointF(0,0);
    Calendar lastY0Time = Calendar.getInstance();
    Calendar lastY1Time = Calendar.getInstance();
    public void updateFling() {
        //return;
        if (fling) {
            properties.backgroundPosition.set(properties.backgroundPosition.x - velocity.x,
                    properties.backgroundPosition.y - velocity.y);
            //properties.syncedWithCloud = false;
            checkBackroundPosition();
            velocity.set ( (float) velocity.x / 1.2f,(float) velocity.y / 1.2f);
            if (Math.abs(velocity.length()) < 1) {
                fling = false;
                properties.syncedWithCloud = false;
                updateFile(false);
            }
        }
    }


    public void addOperation(Operation _operation) {

        if (_operation.transactionId == 0) {
            if (operations.size() == 0) {
                _operation.transactionId = 1;
            } else {
                _operation.transactionId = operations.get(operations.size()-1).transactionId + 1;
            }
        }
        String exchange = "Exchange";
            if (!_operation.fromCircle.equals("Correction") &&
                    !_operation.toCircle.equals("Correction")) {
                _operation.setPageNo(correspondingPage(_operation.date, properties.currentPageNo));
                properties.currentPageNo = _operation.pageNo;
                properties.syncedWithCloud = false;
            }

        if (ref.getAuth() != null) {
            ref.child("maxflow/" + findOwner() + "/" + properties.computeFileNameWithoutXML() + "/operations/" + _operation.id).
                    setValue(_operation);
            operations.add(_operation); //to fetch it faster, it will not be duplicated
        } else
            operations.add(_operation);
        if (!_operation.fromCircle.equals(exchange) &&
                !_operation.toCircle.equals(exchange))
            graphics.showPopUpOperation(_operation, circle);

        //convert currency, if needed
        Circle circleFrom = circleById(_operation.fromCircle);
        //Circle circleTo = circleById(_operation.toCircle);
        if (!_operation.fromCircle.equals(exchange) &&
                !_operation.toCircle.equals(exchange))
            if (!_operation.currency.equals("") && (
                    circleFrom.displayAmount.get(_operation.currency) == null ||
                    circleFrom.displayAmount.get(_operation.currency) == 0)) {
                int k = 0;
                String currency = "";
                for (Map.Entry<String, Float> entry : circleFrom.displayAmount.entrySet()) {
                    if (entry.getValue() > 0) {
                        k++;
                        currency = entry.getKey();
                    }
                }
                if (k == 1) {
                    float amount2 = exchangeRates.convert(_operation.amount, _operation.currency, currency);
                    Date littleEarlier = new Date(_operation.date.getTime() - 10000);
                    addOperation(new Operation(generateNewId(), _operation.fromCircle, exchange, amount2, currency, 0,littleEarlier, _operation.transactionId, _operation.pageNo, false, false, "", "Auto-Currency-Exchange", false));
                    addOperation(new Operation(generateNewId(), exchange, _operation.fromCircle, _operation.amount, _operation.currency, 0, littleEarlier, _operation.transactionId, _operation.pageNo, false, false, "", "Auto-Currency-Exchange", false));
                }
            }
        operationListWindow.needToUpdate = true;
        operationListWindow.needToUpdateFile = true;
    }

    //long lastTime = System.currentTimeMillis();
    //Date lastCircleUpdate = new Date(0);
    //Date lastOperationsUpdate = new Date(0);
    //This function is called from the MainActivity.java every 50ms.
    public AppState lastAppState = appState.idle;
    int lastPageNo = -1;
    boolean askingForPin = false;
    boolean firstTimerTick = true;
    boolean lastFiltered = false;
    Calendar timerCal = Calendar.getInstance();
    public void timerTick() {
        if (!pinCodeEntered && !prefs.getString("pin", "").equals("") && !askingForPin) {
            askingForPin = true;
            askPinCode();
        }

        if (prefs.getString("pin","").equals(""))
            pinCodeEntered = true;

        if (!pinCodeEntered)
            return;


        if (firstTimerTick || lastFiltered != properties.filtered) {
            if (properties.filtered)
                activity.bottom_filter.setBackgroundColor(activity.backgroundColorAccent);
            else
                activity.bottom_filter.setBackgroundColor(Color.TRANSPARENT);
            lastFiltered = properties.filtered;
            firstTimerTick = false;
        }

        if (properties.backgroundPosition.x != properties.backgroundPosition.x ||
                properties.backgroundPosition.y != properties.backgroundPosition.y) {
            Log.e("Properties NaN","Got properties error: " + properties.backgroundPosition.x + "/" + properties.backgroundPosition.y);
            Log.e("Properties NaN","Fixing properties");
            properties.backgroundPosition.set(0,0);
            properties.syncedWithCloud = false;
            updateFile(false);
            //properties.syncedWithCloud = false;
            Log.e("Properties NaN","After Fixing: " + properties.backgroundPosition.x + "/" + properties.backgroundPosition.y);
        }


        if (lastPageNo != properties.currentPageNo && appState.equals(AppState.showOperations))
            operationListWindow.init(graphics,activity,circle, operations,selectedId,properties.currentPageNo,monthName,false);

        //if we have touched the screen in empty space, not some circle,

        if (appState == AppState.creating) {
            needToRedraw = true;
            timerCal = Calendar.getInstance();
            long interval = timerCal.getTimeInMillis() - counter;
            //and waited long enough to create a new circle
            if (interval >= timeToCreate) {
                if ((gesture.size() == 1)) {
                    if (!prefs.getBoolean("prevent_create",false)) {
                        newCircle(gesture.get(0), 120);
                        appState = AppState.idle;
                    }
                }
            }
        }

        //if we touched a circle
        if (appState == AppState.circleTouched) {
            timerCal = Calendar.getInstance();
            long interval = timerCal.getTimeInMillis() - counter;
            //find the circle Number
            String circleNo = pointInCircle(gesture.get(0));
            if (interval >= timeToCreate) {
                if (!circleNo.equals("none")) {
                    boolean allPointsAreCloseToEachOther = true;
                    //if all touches of current gesture are in the same circle
                    for (PointF _gesture: gesture)
                        if (geometry.distance(gesture.get(0),_gesture) > 20)
                            allPointsAreCloseToEachOther = false;
                    if (allPointsAreCloseToEachOther) {
                        //then start the "edit mode", where we can move circles
                        if (!prefs.getBoolean("prevent_move",false)) {
                            appState = AppState.editMode;
                            doNotMove = false;
                        }
                    }
                }
            }
        }
        //if (counter % 5 == 0)

        //for (Circle _circle : circle)
        //    _circle.move();

        if (needToRedraw)
            postInvalidate();

        needToRedraw = false;

        /*for (CanvasButton cb : graphics.bottomButton) {
            if (cb.needToTransform) {
                needToRedraw = true;
                cb.transformAndMove();
            }
            for (CanvasButton scb : cb.subButton)
                if (scb.needToTransform) {
                    needToRedraw = true;
                    scb.transformAndMove();
                }
        }*/

        if (appState == AppState.canvasMoving)
            needToRedraw = true;

        if (appState == AppState.reporting &&
                reportWindow.needToUpdate) {
            reportWindow.update();
            needToRedraw = true;
        }

        if (appState == AppState.calculator &&
                calcWindow.needToUpdate) {
            calcWindow.update();
            needToRedraw = true;
        }

        if (appState == AppState.chartPlotting) { //&& charts.needToUpdate)
            //needToRedraw = true;
        }

        if (!lastAppState.equals(appState)) {
            needToRedraw = true;
            if (lastAppState == AppState.editMode)
                updateFile(true);

            activity.bottom_list.setBackgroundColor(Color.TRANSPARENT);
            activity.bottom_map.setBackgroundColor(Color.TRANSPARENT);
            activity.bottom_pie.setBackgroundColor(Color.TRANSPARENT);

            activity.bottom_filter.setBackgroundColor(Color.TRANSPARENT);
            if (properties.filtered)
                activity.bottom_filter.setBackgroundColor(activity.backgroundColorAccent);


            if (appState == AppState.showOperations) {
                activity.bottom_list.setBackgroundColor(activity.backgroundColorAccent);
                //fab.setVisibility(View.GONE);
                //fabMenu.setVisibility(View.VISIBLE);
            }
            else if (appState == AppState.reporting) {
                activity.bottom_pie.setBackgroundColor(activity.backgroundColorAccent);
                //fab.setVisibility(View.VISIBLE);
                //fabMenu.setVisibility(View.GONE);
            } else if (appState == AppState.calculator || appState == AppState.iconChoose) {
                //fab.setVisibility(View.GONE);
                //fabMenu.setVisibility(View.GONE);
            } else {
                activity.bottom_map.setBackgroundColor(activity.backgroundColorAccent);
            }
        }

        if (appState == appState.showOperations) {
            if (operationListWindow.needToUpdate) {
                operationListWindow.update();
                needToRedraw = true;
            }
            if (operationListWindow.needToUpdateFile) {
                operationListWindow.needToUpdateFile = false;
                updateFile(true);
                operationListWindow.init(graphics,activity,circle, operations,selectedId,properties.currentPageNo,monthName,true);
            }
        }

        if (appState == appState.iconChoose) {
            if (iconWindow.needToUpdate) {
                iconWindow.update();
                needToRedraw = true;
            }
        }

        if (appState == AppState.editMode)
            needToRedraw = true;

        if (graphics.popupNeedsToUpdate) {
            needToRedraw = true;
            graphics.updatePopUpOperation();
        }

        if (needToRecalculate || lastPageNo != properties.currentPageNo) {
            timerCal = Calendar.getInstance();
            long interval = timerCal.getTimeInMillis() - lastRecalculateInitiative;

            if (interval > 1000 || lastPageNo != properties.currentPageNo) {
                recalculateAll();
                lastPageNo = properties.currentPageNo;
                Log.e("RecalculateAll", "initiate gii 2023");
                needToRecalculate = false;
                lastRecalculateInitiative = timerCal.getTimeInMillis();
            }
        }

        lastAppState = appState;
        lastPageNo = properties.currentPageNo;

        for (Circle _circle: circle)
            if (_circle.moving)
                needToRedraw = true;

        if (loaded < 4) {
            needToRedraw = true;
        };

        if (fling) {
            updateFling();
            needToRedraw = true;
        }

        /*for (Circle _circle : circle) {
            float amount = _circle.myMoney?_circle.amountTotal:_circle.amount;
            if (properties.filtered) amount = _circle.amount;
            if ((_circle.dxShowAmount == 0 &&
                    _circle.showAmount != amount) ||
                    (_circle.showAmount > amount &&
                            _circle.dxShowAmount > 0) ||
                    (_circle.showAmount < amount &&
                            _circle.dxShowAmount < 0)
                            ) {
                //needToRedraw = true;
                _circle.dxShowAmount = (amount - _circle.showAmount)/30f;
            }
            if (_circle.dxShowAmount != 0) {
                if (Math.abs(_circle.showAmount - amount) <= 0.01) {
                    _circle.showAmount = amount;
                    _circle.dxShowAmount = 0;
                } else
                    needToRedraw = true;
                _circle.showAmount += _circle.dxShowAmount;
            }
        }*/
    }
}

//TODO: next version; write whatever you want (reminders? notes?)

//TODO: next version; export to XML only filtered operations

//TODO: Try attaching pictures to transactions

//TODO: show goal amount in Calculator, if any

//TODO: a parent of b, make b parent of a and app crashes :(


//Changes: Backup for offline files, new create circle, performance/bugs, faster saving file, doubleClick on free space,
    //      defaultCurrencyProblem in Calculator, fixed some English words in Russian version