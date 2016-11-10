package com.gii.maxflow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Timur on 17-Nov-15.
 * The list of operations (codename Whatsapp)
 */
public class OperationListWindow {


    public GII gii;
    public OperationListWindow(GII gii) {
        this.gii = gii;
        //this.gii.graphics = gii.gii.graphics;
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
    float dy = 0; //vertical velocity
    float lastY0 = 0f;
    float lastY1 = 0f;
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
    //private Graphics gii.graphics;

    private float scaleFactor;
    public PointF backgroundPosition = new PointF(0, 0);
    private PointF lastBackgroundPosition = new PointF(0,0);
    private PointF canvasMovingStartingPoint = new PointF(0,0);

    private int moving; // 1 - Up/Down, 2 - Left/Right
    private String selectedId = "";
    private String currentOperation = "none";
    public ArrayList<Circle> circle;
    private ArrayList<Operation> operation;
    private ArrayList<Operation> displayedOperation;
    public String[] monthName;

    Map<String, Float> statIncome = new HashMap<String, Float>();
    Map<String, Float> statExpense = new HashMap<String, Float>();
    float statIncomeTotal = 0;
    float statExpenseTotal = 0;

    Bitmap pieExpense;
    Bitmap pieIncome;

    Paint fabPaint = new Paint();

    Context context;

    Circle abstractCircleForTotals = new Circle();

    public void init(Graphics graphics, Context context, ArrayList<Circle> circle, ArrayList<Operation> operation,
                     String selectedId,int pageNo, String[] monthName, boolean keepBackground) {

        fabPaint.setColor(gii.fab.getBackgroundTintList().getDefaultColor());
        fabPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        this.context = context;

        showIncomeNotExpense = false;

        deleteOperation = ContextCompat.getDrawable(context, R.drawable._ic_delete_black_24dp);
        editOperation = ContextCompat.getDrawable(context, R.drawable._ic_mode_edit_black_24dp);

        textPaint.setColor(gii.graphics.mainFont.getColor());
        textPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        bigTextPaint.setColor(textPaint.getColor());
        bigTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        veryBigTextPaint.setColor(textPaint.getColor());
        veryBigTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        billTextPaint.setColor(textPaint.getColor());
        billTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);

        this.monthName = monthName;

        moving = 0;
        if (!keepBackground)
            backgroundPosition = new PointF(0, 0);
        pressedHere = false;

        choosing = Choosing.icon;
        //this.gii.graphics = gii.graphics;
        scaleFactor = 1;
        this.circle = circle;
        this.operation = operation;

        this.selectedId = selectedId;

        displayedOperation = new ArrayList<>();
        statIncome = new HashMap<String, Float>();
        statExpense = new HashMap<String, Float>();
        statIncomeTotal = 0;
        statExpenseTotal = 0;

        for (Operation _operation: operation) {
            //Operation _operation = this.operation.get(i);
            if ((selectedId.equals("none") ||
                    _operation.circlesWayIn.contains(selectedId) ||
                    _operation.circlesWayOut.contains(selectedId)) &&
                _operation.inFilter && !_operation.deleted) {
                boolean added = false;
                /*for (int u = 0; u < displayedOperation.size() && !added; u++) {
                    if (displayedOperation.get(u).date.before(_operation.date)) {
                        displayedOperation.add(u, _operation);
                        added = true;
                    }
                }
                if (!added)*/
                    displayedOperation.add(_operation);

                if (!_operation.fromCircle.equals(selectedId)) {
                    statIncome.put(_operation.fromCircle, (statIncome.get(_operation.fromCircle) == null) ? _operation.amount : statIncome.get(_operation.fromCircle) + _operation.amount);
                    statIncomeTotal += _operation.amount;
                }
                if (!_operation.toCircle.equals(selectedId)) {
                    statExpense.put(_operation.toCircle, (statExpense.get(_operation.toCircle) == null) ? _operation.amount : statExpense.get(_operation.toCircle) + _operation.amount);
                    statExpenseTotal += _operation.amount;
                }
            }
         }
        Collections.sort(displayedOperation);
        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        final int size = Math.min((int)graphics.canvasHeight,(int)graphics.canvasWidth)/2;
        //if (gi123i.prefs.getBoolean("pref_low_res",false) == true)
        //    size = size / 2;
        if (size > 0 && !selectedId.equals("none")) {
            pieExpense = Bitmap.createBitmap(size, size, conf); // this creates a MUTABLE bitmap
            pieIncome = Bitmap.createBitmap(size, size, conf); // this creates a MUTABLE bitmap
            final Canvas canvas = new Canvas(pieExpense);
            final Canvas canvas2 = new Canvas(pieIncome);
            final Graphics graphicsInstance = graphics;
            graphicsInstance.buildPie(statExpenseTotal, statExpense, new Rect(0, 0, size, size), canvas);
            graphicsInstance.buildPie(statIncomeTotal, statIncome, new Rect(0, 0, size, size), canvas2);
        }
        if (!selectedId.equals("none")) {
            Circle _circle =gii.circleById(selectedId);

            float totalIncome = 0;
            float totalOutcome = 0;
            float totalAmount = 0;
            int totalOperations = 0;

            for (Operation _operation : operation) {
                if (_operation.circlesWayOut.contains(selectedId) && !_operation.deleted) {
                    totalOutcome += _operation.amount;
                    totalOperations++;
                }
                if (_operation.circlesWayIn.contains(selectedId) && !_operation.deleted) {
                    totalIncome += _operation.amount;
                    totalOperations++;
                }
            }
            totalAmount = totalIncome - totalOutcome;

            billTxt = new String[1];
            billTxt[0] =gii.circleById(selectedId).name;
            //billTxt[1] = "+ " + gii.currency(totalIncome,"") +
              //      " - " + gii.currency(totalOutcome,"");

            //billTxt[2] = gii.getContext().getString(R.string.total_grand) + " " + gii.currency(totalAmount,"");
            //billTxt[4] = "Operations: " + totalOperations;
        }

        int n = displayedOperation.size();
        origRect = new Rect[n];
        drawRect = new Rect[n];
        for (int i = 0; i < n; i++) {
            origRect[i] = new Rect(0,0,0,0);
            drawRect[i] = new Rect(0,0,0,0);
        }
        buttonDrawable = new Drawable[] {
                ContextCompat.getDrawable(context, R.drawable._ic_mode_edit_black_24dp),
                ContextCompat.getDrawable(context, R.drawable._ic_exchange_money_black_24dp),
                ContextCompat.getDrawable(context, R.drawable._ic_delete_black_24dp),
                ContextCompat.getDrawable(context, R.drawable._ic_photo_size_select_actual_black_24dp),
                ContextCompat.getDrawable(context, R.drawable._ic_format_color_fill_black_24dp),
                ContextCompat.getDrawable(context, R.drawable._ic_attach_money_black_24dp)

        };
        buttonIcon =  new Rect[buttonDrawable.length];
        for (int i = 0; i < buttonIcon.length; i++) {
            buttonIcon[i] = new Rect(0, 0, 0, 0);
            buttonDrawable[i].setBounds(buttonIcon[i]);
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
                dy = 0;
                lastY0 = event.getY();
                lastY1 = event.getY();
                lastY0Time = Calendar.getInstance();
                lastY1Time = Calendar.getInstance();
                needToUpdate = false;
                swiping = 0;
                break;
            case MotionEvent.ACTION_MOVE:
                if (pressedHere && moving == 0 &&
                        Math.abs(canvasMovingStartingPoint.y - event.getY())  > 10)
                    moving = 1;
                if (pressedHere && moving == 0 &&
                        Math.abs(event.getX() - canvasMovingStartingPoint.x) > 10 &&
                        getOperation(event.getX(),event.getY()))
                    moving = 2;
                if (pressedHere && moving == 1) {
                    backgroundPosition = new PointF(0, lastBackgroundPosition.y + (canvasMovingStartingPoint.y - event.getY()));
                    checkBackground();
                }
                if (pressedHere && moving == 2) {
                    backgroundPosition = new PointF(
                             lastBackgroundPosition.x - (canvasMovingStartingPoint.x - event.getX()), backgroundPosition.y);
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
                if (pressedHere && moving != 1) {
                    click(event.getX(), event.getY());
                }
                if (pressedHere && moving == 1) {
                    dy = (lastY1-lastY0) / (lastY1Time.getTimeInMillis() - lastY0Time.getTimeInMillis()) * 100;
                    needToUpdate = true;
                }
                if (pressedHere && moving == 2 && swipeActivated)
                    doTheSwipe(context);
                moving = 0;
                backgroundPosition = new PointF(0, backgroundPosition.y);
                swiping = 0;
                break;
            default:
                return false;
        }
        return true;
    }

    private boolean checkButtons(PointF pointF) {
        int click = -1;
        for (int i = 0; i < buttonIcon.length; i++)
            if (buttonIcon[i].contains((int)pointF.x,(int)pointF.y))
                click = i;
        if (click == 0)
            renameCircle();
        if (click == 1) {
            //gii.reportWindow.init();
            //gii.appState = GII.AppState.reporting;
            exchange();
        }
        if (click == 2)
            deleteCircle();
        if (click == 3) {
            gii.iconWindow.init(gii.graphics);
            gii.iconWindow.color =gii.circleById(selectedId).color;
            gii.iconWindow.returnAppState = GII.AppState.showOperations;
            gii.appState = GII.AppState.iconChoose;
        }
        if (click == 4) {
            gii.iconWindow.init(gii.graphics);
            gii.iconWindow.choosing = Icons.Choosing.color;
            gii.iconWindow.pictureNo =gii.circleById(selectedId).picture;
            gii.iconWindow.returnAppState = GII.AppState.showOperations;
            gii.appState = GII.AppState.iconChoose;
        }
        if (click == 5) {
            boolean lastValue =gii.circleById(selectedId).myMoney;
           gii.circleById(selectedId).setMyMoney(!lastValue);
            gii.updateFile(true);
        }
        return (click >= 0);
    }

    static String TAG = "OperationListWindow";
    private void exchange() {
        //here we should open the convert program
        GIIApplication.gii.exchangeRates.readRatesFromInternet();
        Log.w(TAG, "convert: read complete" );

        String[] currentCurrencies = GIIApplication.gii.exchangeRates.currencyArray();

        if (currentCurrencies.length < 2) {
            new AlertDialog.Builder(context)
                    //.setTitle(gii.getContext().getString(R.string.delete_circle))
                    .setMessage(gii.getContext().getString(R.string.you_need_to_select_currencies))
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            GIIApplication.gii.activity.chooseCurrencies();
                        }
                    }).setNegativeButton(gii.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                }
            }).show();
            return;
        }

        final ArrayList<String> fromCurrency = new ArrayList<>();
        final ArrayList<String> toCurrency = new ArrayList<>();
        ArrayList<String> showCurrencyExchangeOption = new ArrayList<>();

        for (int i = 0; i < currentCurrencies.length; i++) {
            for (int j = 0; j < currentCurrencies.length; j++) {
                if (i != j && abstractCircleForTotals.displayAmount.get(currentCurrencies[i]) != null) {
                    fromCurrency.add(currentCurrencies[i]);
                    toCurrency.add(currentCurrencies[j]);
                    showCurrencyExchangeOption.add(currentCurrencies[i] + " -> " + currentCurrencies[j]);
                }
            }
        }

        String[] showOptions = new String[showCurrencyExchangeOption.size()];
        for (int i = 0; i < showCurrencyExchangeOption.size(); i++)
            showOptions[i] = showCurrencyExchangeOption.get(i);

        new AlertDialog.Builder(GIIApplication.gii.activity)
                .setTitle(GIIApplication.gii.activity.getString(R.string.title_choose_direction))
                .setSingleChoiceItems(showOptions, 0, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case Dialog.BUTTON_NEGATIVE: // Cancel button selected, do nothing
                                dialog.cancel();
                                break;

                            case Dialog.BUTTON_POSITIVE: // OK button selected, send the data back
                                dialog.dismiss();
                                break;

                            default:
                                dialog.dismiss();
                                exchange(fromCurrency.get(which),toCurrency.get(which));
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

    }

    private void exchange(final String fromCurrency, final String toCurrency) {
        final EditText newAmountFrom = new EditText(context);
        final TextView newAmountFromTB = new TextView(context);
        final EditText newAmountTo = new EditText(context);
        final TextView newAmountToTB = new TextView(context);
        final EditText descriptionBox = new EditText(context);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        final AlertDialog dialog = builder.create();

        descriptionBox.setText(context.getString(R.string.hint_description_exchange) + " " + fromCurrency + " -> " + toCurrency);

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        final LinearLayout layout1 = new LinearLayout(context);
        layout1.setOrientation(LinearLayout.HORIZONTAL);

        final LinearLayout layout2 = new LinearLayout(context);
        layout2.setOrientation(LinearLayout.HORIZONTAL);

        newAmountFrom.setMinimumWidth(100);
        newAmountTo.setMinimumWidth(100);
        newAmountFrom.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        newAmountFrom.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        newAmountFrom.setFocusable(true);
        newAmountFrom.setFocusableInTouchMode(true);
        newAmountTo.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        newAmountTo.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        newAmountTo.setFocusable(true);
        newAmountTo.setFocusableInTouchMode(true);

        final Circle firstEditTextIsFree = new Circle();
        firstEditTextIsFree.deleted = true;
        final Circle secondEditTextIsFree = new Circle();
        secondEditTextIsFree.deleted = true;

        newAmountFrom.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (newAmountFrom.hasFocus()) {
                    firstEditTextIsFree.deleted = newAmountFrom.getText().toString().equals("");
                    if (secondEditTextIsFree.deleted) {
                        float convertAmount = GII.parseFloatFromString(newAmountFrom.getText().toString());
                        if (convertAmount != -7.3024f) {
                            newAmountTo.setText(GII.df.format(GIIApplication.gii.exchangeRates.convert(convertAmount, fromCurrency, toCurrency)));
                        }
                    }
                }
            }
        });


        newAmountTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (newAmountTo.hasFocus()) {
                    secondEditTextIsFree.deleted = newAmountTo.getText().toString().equals("");
                    if (firstEditTextIsFree.deleted) {
                        float convertAmount = GII.parseFloatFromString(newAmountTo.getText().toString());
                        if (convertAmount != -7.3024f) {
                            newAmountFrom.setText(GII.df.format(GIIApplication.gii.exchangeRates.convert(convertAmount, toCurrency, fromCurrency)));
                        }
                    }
                }
            }
        });

        newAmountFromTB.setText(fromCurrency);
        newAmountToTB.setText(toCurrency);

        layout1.addView(newAmountFrom);
        layout1.addView(newAmountFromTB);

        layout2.addView(newAmountTo);
        layout2.addView(newAmountToTB);

        layout.addView(layout1);
        layout.addView(layout2);
        layout.addView(descriptionBox);

        builder.setTitle(gii.getContext().getString(R.string.hint_description_exchange))
                .setMessage(gii.getContext().getString(R.string.enter_amount))
                .setView(layout)
                .setPositiveButton(gii.getContext().getString(R.string.convert), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        float amount1 = GII.parseFloatFromString(newAmountFrom.getText().toString());
                        float amount2 = GII.parseFloatFromString(newAmountTo.getText().toString());
                        if (amount1 != -7.3024f && amount2 != -7.3024f) {

                            gii.calendarTo = Calendar.getInstance();

                            Random rnd = new Random();

                            int transactionId = rnd.nextInt(Integer.MAX_VALUE);
                            String last = "Exchange";
                            gii.addOperation(new Operation(gii.generateNewId(), selectedId, last, amount1, fromCurrency, 0, gii.calendarTo.getTime(), transactionId + 1, gii.properties.currentPageNo, false, false, "", descriptionBox.getText().toString(), false));
                            gii.addOperation(new Operation(gii.generateNewId(), last, selectedId, amount2, toCurrency, 0, gii.calendarTo.getTime(), transactionId + 1, gii.properties.currentPageNo, false, false, "", descriptionBox.getText().toString(), false));

                            Log.w(TAG, "converting from " + amount1 + fromCurrency + " to " + amount2 + toCurrency);
                            needToUpdateFile = true;
                        }
                        layout.removeAllViews();
                    }
                }).setNegativeButton(gii.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                layout.removeAllViews();
            }
        }).show();


}

    private void deleteCircle() {
        new AlertDialog.Builder(context)
                .setTitle(gii.getContext().getString(R.string.delete_circle))
                .setMessage(gii.getContext().getString(R.string.are_you_sure))
                .setPositiveButton(gii.getContext().getString(R.string.delete), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        for (Circle _circle: circle) {
                            if (_circle.id.equals(selectedId)) {
                                _circle.setDeleted(true, circle);
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

    private void renameCircle() {
        final EditText renameName = new EditText(context);

        final String[] currency = gii.exchangeRates.currencyArray();
        final EditText[] currentAmountEditText = new EditText[currency.length];
        final TextView[] currentAmountHint = new TextView[currency.length];
        for (int i = 0; i < currency.length; i++) {
            currentAmountEditText[i] = new EditText(context);
            currentAmountHint[i] = new TextView(context);
        }

        final EditText goalAmountEditText = new EditText(context);
        final TextView goalAmountHint = new TextView(context);
        final EditText limitAmountEditText = new EditText(context);
        final TextView limitAmountHint = new TextView(context);

        final Button button_goal = new Button(context);
        final Button button_limit = new Button(context);
        final Button button_photo = new Button(context);
        final Button button_remove_photo = new Button(context);

        limitAmountHint.setText(gii.getContext().getString(R.string.hint_limit));
        goalAmountHint.setText(gii.getContext().getString(R.string.hint_goal));



        goalAmountEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        goalAmountEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        limitAmountEditText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        limitAmountEditText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        for (int i = 0; i < currency.length; i++) {
            currentAmountEditText[i].setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
            currentAmountEditText[i].setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            currentAmountHint[i].setText(gii.getContext().getString(R.string.hint_current_amount) + " " + currency[i]);
        }

        renameName.setText(gii.circleById(selectedId).name);
        goalAmountEditText.setText(gii.df.format(gii.circleById(selectedId).goalAmount));
        limitAmountEditText.setText(gii.df.format(gii.circleById(selectedId).goalAmount));

        float[] amount = new float[currency.length];
        float[] amountTotal = new float[currency.length];
        for (int i = 0; i < currency.length; i++) {
            amount[i] = 0;
            amountTotal[i] = 0;
        }
        for (Operation _operation : gii.operations) {
            int i = -1;
            if (!_operation.deleted) {
                String opCurrency = _operation.currency;
                if (opCurrency.equals(""))
                    opCurrency = gii.properties.defaultCurrency;
                for (int j = 0; j < currency.length; j++) {
                    if (currency[j].equals(opCurrency))
                        i = j;
                }
                if (i >= 0) {
                    //for this page
                    if (_operation.pageNo == gii.properties.currentPageNo) {
                        if (_operation.fromCircle.equals(gii.selectedId))
                            amount[i] -= _operation.amount;
                        if (_operation.toCircle.equals(gii.selectedId))
                            amount[i] += _operation.amount;
                    }
                    //for all pages up to current
                    if (_operation.pageNo <= gii.properties.currentPageNo) {
                        if (_operation.fromCircle.equals(gii.selectedId))
                            amountTotal[i] -= _operation.amount;
                        if (_operation.toCircle.equals(gii.selectedId))
                            amountTotal[i] += _operation.amount;
                    }
                }
            }
        }


        final float[] currentAmount = new float[currency.length];
        for (int i = 0; i < currency.length; i++)
                currentAmount[i] = (gii.circleById(gii.selectedId).myMoney ? amountTotal[i] : amount[i]);

        for (int i = 0; i < currency.length; i++)
            currentAmountEditText[i].setText(gii.df.format(currentAmount[i]));

        renameName.setSingleLine(true);

        final LinearLayout layout = new LinearLayout(context);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(renameName);



        final LinearLayout layoutGoal = new LinearLayout(context);
        layoutGoal.setOrientation(LinearLayout.HORIZONTAL);
        layoutGoal.setLayoutParams
                (new ViewGroup.MarginLayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final LinearLayout layoutLimit = new LinearLayout(context);
        layoutLimit.setOrientation(LinearLayout.HORIZONTAL);
        layoutLimit.setLayoutParams
                (new ViewGroup.MarginLayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout layoutButtons = new LinearLayout(context);
        layoutButtons.setOrientation(LinearLayout.HORIZONTAL);
        layoutButtons.setLayoutParams
                (new ViewGroup.MarginLayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        goalAmountEditText.setLayoutParams
                (new ViewGroup.MarginLayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        limitAmountEditText.setLayoutParams
                (new ViewGroup.MarginLayoutParams
                        (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        //layoutButtons.addView(button_goal);
        layoutButtons.addView(button_limit);
        layoutButtons.addView(button_photo);
        if (GIIApplication.gii.circleById(GIIApplication.gii.selectedId).photoString != null &&
                !GIIApplication.gii.circleById(GIIApplication.gii.selectedId).photoString.equals("") )
            layoutButtons.addView(button_remove_photo);


        for (int i = 0; i < currency.length; i++) {
            LinearLayout layout1 = new LinearLayout(context);
            layout1.setOrientation(LinearLayout.HORIZONTAL);
            layout1.setLayoutParams
                    (new ViewGroup.MarginLayoutParams
                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            currentAmountEditText[i].setLayoutParams
                    (new ViewGroup.MarginLayoutParams
                            (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            if (currency[i].equals(GIIApplication.gii.properties.defaultCurrency))
                currentAmountHint[i].setTypeface(null, Typeface.BOLD);
            layout1.addView(currentAmountHint[i]);
            layout1.addView(currentAmountEditText[i]);
            layout.addView(layout1);
        }

        layoutGoal.addView(goalAmountHint);
        layoutGoal.addView(goalAmountEditText);
        layoutLimit.addView(limitAmountHint);
        layoutLimit.addView(limitAmountEditText);

        layout.addView(layoutButtons);
        layout.addView(layoutGoal);
        layout.addView(layoutLimit);

        button_goal.setText(context.getString(R.string.hint_goal_button));
        button_limit.setText(context.getString(R.string.hint_limit_button));
        button_photo.setText(context.getString(R.string.hint_photo_button));
        button_remove_photo.setText(context.getString(R.string.hint_remove_photo_button));

        button_goal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutLimit.setVisibility(View.GONE);
                layoutGoal.setVisibility(View.VISIBLE);
            }
        });

        button_limit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                layoutLimit.setVisibility(View.VISIBLE);
                layoutGoal.setVisibility(View.GONE);
            }
        });

        button_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        button_remove_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GIIApplication.gii.circleById(GIIApplication.gii.selectedId).photoString = "";
                GIIApplication.gii.circleById(GIIApplication.gii.selectedId).theIconParams = "";
                button_remove_photo.setVisibility(View.GONE);
            }
        });

        layoutLimit.setVisibility(View.GONE);
        layoutGoal.setVisibility(View.GONE);

        if (gii.circleById(selectedId).limitGoal) {
            if (gii.circleById(selectedId).goalAmount != 0)
                layoutGoal.setVisibility(View.VISIBLE);
        } else {
            if (gii.circleById(selectedId).goalAmount != 0)
                layoutLimit.setVisibility(View.VISIBLE);
        }

        new AlertDialog.Builder(context)
                .setTitle(gii.getContext().getString(R.string.edit_circle))
                .setView(layout)
                .setPositiveButton(gii.getContext().getString(R.string.save), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String url = renameName.getText().toString();
                       gii.circleById(selectedId).setName(url);

                        gii.circleById(selectedId).setGoalAmount(0);
                        if (layoutGoal.getVisibility() == View.VISIBLE) {
                            float amount = gii.parseFloatFromString(goalAmountEditText.getText().toString());
                            if (amount != -7.3024f) {
                                gii.circleById(selectedId).setGoalAmount(amount);
                                gii.circleById(selectedId).limitGoal = true;
                            }
                        }

                        if (layoutLimit.getVisibility() == View.VISIBLE) {
                            float amount = gii.parseFloatFromString(limitAmountEditText.getText().toString());
                            if (amount != -7.3024f) {
                                gii.circleById(selectedId).setGoalAmount(amount);
                                gii.circleById(selectedId).limitGoal = false;
                            }
                        }

                        for (int i = 0; i < currency.length; i++) {
                            float newCurrentAmount = gii.parseFloatFromString(currentAmountEditText[i].getText().toString());
                            if (newCurrentAmount != -7.3024f) {
                                float difference = newCurrentAmount - currentAmount[i];
                                if (difference != 0) {
                                    int transactionId = 0;
                                    String first = "Correction";
                                    String last = selectedId;
                                    if (difference < 0) {
                                        last = "Correction";
                                        first = selectedId;
                                        difference = -difference;
                                    }
                                    gii.calendarTo = Calendar.getInstance();
                                    gii.addOperation(new Operation(gii.generateNewId(), first, last, difference, currency[i], 0, gii.calendarTo.getTime(), transactionId + 1, gii.properties.currentPageNo, false, false, "", gii.activity.getString(R.string.hint_correction_amount), false));
                                }
                            }
                        }

                        needToUpdateFile = true;
                        needToUpdate = true;

                    }
                }).setNegativeButton(gii.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();
    }

    private void takePhoto() {
        if (GIIApplication.gii.ref.getAuth() != null)
            dispatchTakePictureIntent();
        else
            GIIApplication.gii.showMessage(GIIApplication.gii.activity.getString(R.string.icons_Message));
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(GIIApplication.gii.activity.getPackageManager()) != null) {
            GIIApplication.gii.activity.startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    private void doTheSwipe(Context context) {
        if (swipeMode == SwipeMode.left) {
        for (Operation _operation: operation)
            if (_operation.id.equals(currentOperation)) {
                final Operation operationToDelete = _operation;
                new AlertDialog.Builder(context)
                        .setTitle(gii.getContext().getString(R.string.delete_operation))
                        .setMessage(gii.getContext().getString(R.string.are_you_sure))
                        .setPositiveButton(gii.getContext().getString(R.string.delete), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                deleteOperation(operationToDelete);
                                needToUpdateFile = true;
                            }
                        }).setNegativeButton(gii.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                    }
                }).show();

                Log.w("swipe","deleted operation ".concat(_operation.id));
                //needToUpdateFile = true;
            }
        }
        if (swipeMode == SwipeMode.right) {
            for (final Operation _operation: operation)
                if (_operation.id.equals(currentOperation)) {

                    /*
                    final EditText newAmount = new EditText(context);
                    final EditText descriptionBox = new EditText(context);
                    //final EditText datePickerTo = new EditText(context);

                    newAmount.setText("" + _operation.amount);
                    newAmount.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    newAmount.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    newAmount.setFocusable(true);
                    newAmount.setFocusableInTouchMode(true);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    final AlertDialog dialog = builder.create();

                    newAmount.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                        @Override
                        public void onFocusChange(View v, boolean hasFocus) {
                            if (hasFocus) {
                                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                            }
                        }
                    });

                    descriptionBox.setText(_operation.description);
                    //gi123i.datePickerTo.setHint("Today");
                    gii.calendarTo.setTime(_operation.date);
                    gii.datePickerTo.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
                    gii.datePickerTo.setRawInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
                    gii.datePickerTo.setText(GII.dateText(gii.calendarTo.getTime()));
                    gii.datePickerTo.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if(event.getAction() == MotionEvent.ACTION_DOWN) {
                                new DatePickerDialog(gii.getContext(), gii.dateTo, gii.calendarTo
                                        .get(Calendar.YEAR), gii.calendarTo.get(Calendar.MONTH),
                                        gii.calendarTo.get(Calendar.DAY_OF_MONTH)).show();
                            }
                            return true;
                        }
                    });
                    // http://stackoverflow.com/questions/17902694/how-to-create-a-textedit-for-date-only-programatically-in-android

                    final LinearLayout layout = new LinearLayout(context);
                    layout.setOrientation(LinearLayout.VERTICAL);

                    ViewGroup parent = (ViewGroup) gii.datePickerTo.getParent();

                    if (parent != null)
                        parent.removeAllViews();

                    layout.addView(newAmount);
                    layout.addView(descriptionBox);
                    layout.addView(gii.datePickerTo);

                    builder.setTitle(gii.getContext().getString(R.string.edit_operation))
                            .setMessage(gii.getContext().getString(R.string.enter_amount))
                            .setView(layout)
                            .setPositiveButton(gii.getContext().getString(R.string.save), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    float amount = GII.parseFloatFromString(newAmount.getText().toString());
                                    if (amount != -7.3024f) {
                                        _operation.setAmount(amount);
                                        _operation.setDescription(descriptionBox.getText().toString());
                                        _operation.setDate(gii.calendarTo.getTime());
                                        _operation.setSyncedWithCloud(false);
                                        Log.e("swipe", "edited operation ".concat(_operation.id));
                                        needToUpdateFile = true;
                                    }
                                    layout.removeAllViews();
                                }
                            }).setNegativeButton(gii.getContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            layout.removeAllViews();
                        }
                    }).show();
                    */
                    GIIApplication.gii.calcWindow.init(GIIApplication.gii.graphics,_operation);
                    GIIApplication.gii.appState = GII.AppState.calculator;
                }
        }
    }

    private void deleteOperation(Operation operationToDelete) {
        operationToDelete.setSyncedWithCloud(false);
        operationToDelete.delete();
        for (Operation _operation : operation)
            if (_operation.transactionId > 1 && _operation.transactionId == operationToDelete.transactionId)
                _operation.delete();
    }

    int currentTransaction = 0;
    private boolean getOperation(float x, float y) {
        for (int i = 0; i < displayedOperation.size(); i++) {
            if (drawRect[i].contains((int) x, (int) y)) {
                currentOperation = displayedOperation.get(i).id;
                currentTransaction = displayedOperation.get(i).transactionId;
                return true;
            }
        }
        return false;
    }

    private void checkBackground() {
        int n = displayedOperation.size();
        if (n == 0) {
            backgroundPosition.set(0,0);
            return;
        }
        Rect border = new Rect(origRect[0]);
        for (int i = 0; i < n; i++) {
            if (origRect[i].right > border.right)
                border.set(border.left,border.top, origRect[i].right,border.bottom);
            if (origRect[i].bottom > border.bottom)
                border.set(border.left,border.top,border.right, origRect[i].bottom);
        }
        if (n > 0)
            border.set(border.left,border.top,border.right,border.bottom + (int)(origRect[0].height()*2.5f) + (int)(bigTextPaint.getTextSize() * (abstractCircleForTotals.displayAmount.size())));
        //(int)(bigTextPaint.getTextSize() * (lineNo + 2))
        if (border.bottom - backgroundPosition.y < gii.graphics.canvasHeight)
            backgroundPosition.y = border.bottom - gii.graphics.canvasHeight;
        if (backgroundPosition.y < 0)
            backgroundPosition.y = 0;
        if (backgroundPosition.x < 0) {
            swipeMode = SwipeMode.left;
        }
        if (backgroundPosition.x > 0) {
            swipeMode = SwipeMode.right;
        }
        swiping = 0;

        if (Math.abs(backgroundPosition.x) > gii.graphics.canvasWidth / 3)
            swipeActivated = true;
        else
            swipeActivated = false;


        backgroundPosition.x = Math.max(Math.min(backgroundPosition.x, gii.graphics.canvasWidth / 3),-gii.graphics.canvasWidth/3);

        swiping = (int)(Math.abs(backgroundPosition.x) / (gii.graphics.canvasWidth / 3) * 150);

    }

    private void click(float x, float y) {
    }

    public boolean onScale(ScaleGestureDetector detector, GII.AppState appState) {
        scaleFactor *= detector.getScaleFactor();
        scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 4));
        if (scaleFactor <= 0.1f)
            appState = GII.AppState.idle;
        moving = 0;
        backgroundPosition.x = 0;
        checkBackground();
        return true;
    }

    protected void onDraw(Canvas canvas, Context context) {
        canvasWidth = canvas.getWidth();
        canvasHeight = canvas.getHeight();
        int diameter = (int)(Math.min(canvasWidth,canvasHeight)/6 * scaleFactor);
        int fixedDiameter = (int)(Math.min(canvasWidth,canvasHeight)/5);
        int y = 10;
        veryBigTextPaint.setTextSize(fixedDiameter/2);
        veryBigTextPaint.setFakeBoldText(true);
        billTextPaint.setTextSize(fixedDiameter/4);
        billTextPaint.setFakeBoldText(true);
        Circle selectedCircle =gii.circleById(selectedId);
        mainIconRect = new Rect((int) (canvasWidth / 2 - fixedDiameter*2), y - (int) backgroundPosition.y + (int)(fixedDiameter/1.7f), (int) (canvasWidth / 2), (int) (y + fixedDiameter * 2 - (int) backgroundPosition.y + fixedDiameter/1.7f));
        for (int i = 0; i < buttonIcon.length; i++)
            buttonIcon[i].set(0,0,0,0);

        if (!selectedCircle.name.equals("none") && mainIconRect.bottom+veryBigTextPaint.getTextSize() > 0) {
            //gii.graphics.drawIcon(selectedCircle.picture, selectedCircle.color,selectedCircle.color, mainIconRect, canvas);
            //gii.graphics.buildPie(statOutputTotal,statOutput,statInputTotal,statInput,mainIconRect,canvas);
            rect0.set(0, 0, pieExpense.getWidth(), pieExpense.getHeight());
            if (!showIncomeNotExpense)
                canvas.drawBitmap(pieExpense, rect0, mainIconRect, null);
            else
                canvas.drawBitmap(pieIncome, rect0, mainIconRect, null);
            //canvas.drawText(selectedCircle.name,Math.max(mainIconRect.centerX() - veryBigTextPaint.measureText(selectedCircle.name)/2,10),mainIconRect.bottom+veryBigTextPaint.getTextSize(),veryBigTextPaint);

            //bill
            canvas.drawText(billTxt[0],canvasWidth/2 - veryBigTextPaint.measureText(billTxt[0])/2, - (int) backgroundPosition.y + 20 + veryBigTextPaint.getTextSize(),veryBigTextPaint);
            for (int i = 1; i < billTxt.length; i++)
                canvas.drawText(billTxt[i],canvasWidth/2 + fixedDiameter * 2.3f - billTextPaint.measureText(billTxt[i]), - (int) backgroundPosition.y + 20 + fixedDiameter - (billTxt.length * billTextPaint.getTextSize())/2 + billTextPaint.getTextSize() * (i+1),billTextPaint);

            for (int i = 0; i < buttonIcon.length; i++) {
                float buttonWidth = 0.8f;
                int _x = (int)(canvasWidth/2 + fixedDiameter*0.2f + (i % 3)*fixedDiameter*buttonWidth);
                int _y = (int)(- (int) backgroundPosition.y + 20 + fixedDiameter*0.8 + (i / 3)*fixedDiameter*buttonWidth);
                buttonIcon[i].set(_x, _y, (int)(_x + fixedDiameter*(buttonWidth*0.9)), (int)(_y + fixedDiameter*(buttonWidth*0.9)));
                if (!selectedCircle.myMoney || i != 5)
                    canvas.drawRect(buttonIcon[i],fabPaint);
                else
                    canvas.drawRect(buttonIcon[i], gii.graphics.green);
                buttonDrawable[i].setBounds(buttonIcon[i]);
                buttonDrawable[i].draw(canvas);
            }
        }
        if (!selectedCircle.name.equals("none"))
            y = (int)(fixedDiameter * 2 + veryBigTextPaint.getTextSize() * 2 + 20);
        else
            y = 10;

        int offset = (int)(diameter * 0.15);
        float totalOnPage = 0;
        abstractCircleForTotals.resetDisplayAmount();

        for (int i = 0; i < displayedOperation.size(); i++) {
            Operation _operation = displayedOperation.get(i);

            if (!selectedId.equals("none")) {
                if (_operation.circlesWayIn.contains(selectedId))
                    abstractCircleForTotals.addDisplayAmount(_operation.currency,_operation.amount);
                    //totalOnPage += _operation.amount;
                if (_operation.circlesWayOut.contains(selectedId))
                    abstractCircleForTotals.addDisplayAmount(_operation.currency,-_operation.amount);
                    //totalOnPage -= _operation.amount;
            } else
                abstractCircleForTotals.addDisplayAmount(_operation.currency,_operation.amount);
                //totalOnPage += _operation.amount;

            origRect[i].set(0, y, canvas.getWidth(), y + diameter);
            drawRect[i].set(0,0,0,0);
            int standingHeight = (int)(textPaint.getTextSize() * 1.25f);
            if (origRect[i].bottom - (int) backgroundPosition.y > 0 && origRect[i].top - (int) backgroundPosition.y < canvas.getHeight()) {
                //draw swiping
                if (!(currentOperation.equals(_operation.id) || ((int) backgroundPosition.x < 0 && currentTransaction > 1 && _operation.transactionId == currentTransaction))) {
                    drawRect[i].set(0, y - (int) backgroundPosition.y, canvasWidth, y + diameter - (int) backgroundPosition.y);
                } else {
                    drawRect[i].set((int) backgroundPosition.x, y - (int) backgroundPosition.y, canvasWidth + (int) backgroundPosition.x, y + diameter - (int) backgroundPosition.y);
                    drawUnderOperation(drawRect[i], canvas);
                }
                //draw main rectangle
                drawOperation(drawRect[i], _operation, canvas);
                //draw the standings
                canvas.drawText(GII.df.format(_operation.newStandingFrom) + " " + _operation.currency,10,y + diameter - (int) backgroundPosition.y + standingHeight - textPaint.getTextSize()*0.25f, textPaint);
                String rightText = GII.df.format(_operation.newStandingTo) + " " + _operation.currency;
                canvas.drawText(rightText, canvas.getWidth() - 10 - textPaint.measureText(rightText), y + diameter - (int) backgroundPosition.y + standingHeight - textPaint.getTextSize()*0.25f, textPaint);

                //draw delimiter
                canvas.drawLine(0, y + diameter - (int) backgroundPosition.y + standingHeight, canvasWidth,
                        y + diameter - (int) backgroundPosition.y + standingHeight, gii.graphics.gray);
                canvas.drawLine(0, y - (int) backgroundPosition.y, canvasWidth,
                        y - (int) backgroundPosition.y, gii.graphics.gray);
            }
            y += diameter + standingHeight;
        }

        String totalOnPageTxt = gii.getContext().getString(R.string.total_on_page);
        String totalOnPageTxtConverted = gii.getContext().getString(R.string.total_on_page_converted);
        canvas.drawText(totalOnPageTxt, canvas.getWidth() - bigTextPaint.measureText(totalOnPageTxt + "  "), y - (int) backgroundPosition.y + (int) (bigTextPaint.getTextSize() * 1.5), bigTextPaint);

        int lineNo = 0;

        float sum = 0;
        for (Map.Entry<String, Float> entry : abstractCircleForTotals.displayAmount.entrySet()) {
            lineNo++;
            totalOnPageTxt = entry.getKey();
            canvas.drawText(totalOnPageTxt, canvas.getWidth() - bigTextPaint.measureText(totalOnPageTxt + "  "), y - (int) backgroundPosition.y + (int) (bigTextPaint.getTextSize() * 1.5) + (int)(bigTextPaint.getTextSize() * lineNo), bigTextPaint);
            totalOnPageTxt = gii.df.format(entry.getValue());
            canvas.drawText(totalOnPageTxt, canvas.getWidth() - bigTextPaint.measureText(totalOnPageTxt + "   WWW  "), y - (int) backgroundPosition.y + (int) (bigTextPaint.getTextSize() * 1.5) + (int)(bigTextPaint.getTextSize() * lineNo), bigTextPaint);
            if (!gii.properties.defaultCurrency.equals("")) {
                sum += gii.exchangeRates.convert(entry.getValue(), entry.getKey(),gii.properties.defaultCurrency);
            }
        }

        sum = (float)(Math.floor(sum * 100) / 100);

        //null point reference
        if (!gii.properties.defaultCurrency.equals("") && (abstractCircleForTotals.displayAmount.get(gii.properties.defaultCurrency) == null ||
                sum != abstractCircleForTotals.displayAmount.get(gii.properties.defaultCurrency))) {
            //Log.e(TAG, "onDraw: " + sum + " != " +  abstractCircleForTotals.displayAmount.get(gii.properties.defaultCurrency));
            lineNo++;
            canvas.drawText(totalOnPageTxtConverted, canvas.getWidth() - bigTextPaint.measureText(totalOnPageTxtConverted + "  "), y - (int) backgroundPosition.y + (int) (bigTextPaint.getTextSize() * 1.5 + (int)(bigTextPaint.getTextSize() * lineNo )), bigTextPaint);
            lineNo++;
            totalOnPageTxt = gii.properties.defaultCurrency;
            canvas.drawText(totalOnPageTxt, canvas.getWidth() - bigTextPaint.measureText(totalOnPageTxt + "  "), y - (int) backgroundPosition.y + (int) (bigTextPaint.getTextSize() * 1.5) + (int)(bigTextPaint.getTextSize() * lineNo), bigTextPaint);
            totalOnPageTxt = gii.df.format(sum);
            canvas.drawText(totalOnPageTxt, canvas.getWidth() - bigTextPaint.measureText(totalOnPageTxt + "   WWW  "), y - (int) backgroundPosition.y + (int) (bigTextPaint.getTextSize() * 1.5) + (int)(bigTextPaint.getTextSize() * lineNo), bigTextPaint);
        }
    }


    
    public void drawUnderOperation(Rect bounds, Canvas canvas) {
        if (moving == 2) {
            underPaint.setStyle(Paint.Style.FILL_AND_STROKE);
            if (swipeMode == SwipeMode.right) {
                underPaint.setColor(Color.rgb(100, swiping + 50, 50));
                canvas.drawRect(new Rect(0, bounds.top, bounds.left, bounds.bottom), underPaint);
                if (swipeActivated) {
                    rect1.set(bounds.left/2-bounds.height()/2,bounds.top,bounds.left/2 + bounds.height()/2,bounds.bottom);
                    editOperation.setBounds(rect1);
                    editOperation.draw(canvas);
                }
            }
            else {
                underPaint.setColor(Color.rgb(swiping + 100, 50, 50));
                canvas.drawRect(new Rect(bounds.right, bounds.top, (int) gii.graphics.canvasWidth, bounds.bottom), underPaint);
                if (swipeActivated) {
                    int middle = (bounds.right + (int) gii.graphics.canvasWidth)/2;
                    rect1.set(middle-bounds.height()/2,bounds.top,middle + bounds.height()/2,bounds.bottom);
                    deleteOperation.setBounds(rect1);
                    deleteOperation.draw(canvas);
                }
            }
        }
    }

    public void drawOperation(Rect bounds, Operation _operation, Canvas canvas) {
        int alpha = 255;
        if (bounds.left < 0)
            alpha = Math.max(
                    Math.min(
                            (int)(((gii.graphics.canvasWidth / 3 + bounds.left)/(gii.graphics.canvasWidth / 3))*200 + 50),
                            250),0
            );

        textPaint.setAlpha(alpha);
        bigTextPaint.setAlpha(alpha);

        int oneTenth = (int)bounds.height()/10;
        textPaint.setTextSize(bounds.height()/5);
        bigTextPaint.setTextSize(bounds.height()/3);
        Circle cFrom = gii.circleById(_operation.fromCircle);
        Circle cTo = gii.circleById(_operation.toCircle);
        String dateText = GII.dateText(_operation.date);
        String timeText = GII.timeText(_operation.date);

        Rect cFromRect = new Rect(bounds.left,bounds.top + oneTenth/2,bounds.left + bounds.height(), bounds.bottom - oneTenth/2);
        Rect cToRect = new Rect(bounds.right - bounds.height(),bounds.top + oneTenth/2,bounds.right, bounds.bottom - oneTenth/2);

        int textOffset = oneTenth * 2;
        textPaint.setFakeBoldText(false);
        //canvas.drawText(_operation.description, cFromRect.right + oneTenth, bounds.top + 3 * oneTenth + textPaint.getTextSize(), textPaint);
        canvas.drawText(dateText.concat("  ").concat(_operation.description), cFromRect.right + oneTenth + textOffset, bounds.centerY() + textPaint.getTextSize() * 1, textPaint);
        canvas.drawText(timeText, cFromRect.right + oneTenth + textOffset, bounds.centerY() + textPaint.getTextSize() * 2, textPaint);
        canvas.drawText(gii.currency(_operation.amount,_operation.currency), cFromRect.right + oneTenth + textOffset, bounds.centerY() - bigTextPaint.getTextSize()/5, bigTextPaint);

        String viaCircles = "";
        if (_operation.circlesWayOut.size() > 0) {
            for (int i = 0; i < _operation.circlesWayOut.size(); i++) {
                if (!gii.circleById(_operation.circlesWayOut.get(i)).name.equals("none"))
                    viaCircles = viaCircles.concat(gii.circleById(_operation.circlesWayOut.get(i)).name);
                if (i != _operation.circlesWayOut.size() - 1)
                    viaCircles = viaCircles.concat("->");
            }
        }
        viaCircles = viaCircles.concat("->");
        if (_operation.circlesWayIn.size() > 0) {
            for (int i = _operation.circlesWayIn.size()-1; i >= 0 ; i--) {
                if (!gii.circleById(_operation.circlesWayIn.get(i)).name.equals("none"))
                    viaCircles = viaCircles.concat(gii.circleById(_operation.circlesWayIn.get(i)).name);
                if (i != 0)
                    viaCircles = viaCircles.concat("->");
            }
        }

        canvas.drawText(timeText, cFromRect.right + oneTenth + textOffset, bounds.centerY() + textPaint.getTextSize() * 2, textPaint);
        canvas.drawText(viaCircles, cToRect.left - oneTenth - textOffset - textPaint.measureText(viaCircles), bounds.centerY() + textPaint.getTextSize() * 2, textPaint);

        textPaint.setAlpha(255);
        bigTextPaint.setAlpha(255);

        //canvas.drawText("viaCircles", cFromRect.right + oneTenth, bounds.bottom - textPaint.getTextSize() * 2, textPaint);

        if (!cFrom.name.equals("none"))
            drawChevron(cFrom.picture, cFrom.color, cFromRect, canvas,0);
        if (!cTo.name.equals("none"))
            drawChevron(cTo.picture, cTo.color, cToRect, canvas, 2);
        if (_operation.fromCircle.equals("Exchange"))
            drawChevron(-1, cTo.color, cFromRect, canvas,0);
        if (_operation.toCircle.equals("Exchange"))
            drawChevron(-1, cFrom.color, cToRect, canvas,2);
    }

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

    PointF moved = new PointF();
    Rect rect0 = new Rect(0,0,0,0);
    Rect rect1 = new Rect(0,0,0,0);
    Path chevron = new Path();
    public void drawChevron(int iconBitmapNo, int iconColorNo, Rect bounds, Canvas canvas, int leftMiddleRight) {
        moved.set(bounds.centerX(), bounds.centerY());
        float radius = bounds.width()/4;
        int chevronEdge = bounds.width()/5;
        //canvas.drawCircle(moved.x, moved.y, (float) (radius * 1.1), circleColor[iconColorNo % circleColor.length]);
        //rect0.set(0, 0, gii.graphics.circleBitmap[iconBitmapNo % gii.graphics.circleBitmap.length].getWidth(), gii.graphics.circleBitmap[iconBitmapNo % gii.graphics.circleBitmap.length].getHeight());
        //if (leftMiddleRight == 0)
            rect1.set((int) (moved.x - (radius)), (int) (moved.y - radius),
                (int) (moved.x + radius), (int) (moved.y + radius));
        //else
        //    rect1.set((int) (moved.x - (radius) + chevronEdge), (int) (moved.y - radius),
        //            (int) (moved.x + radius + chevronEdge), (int) (moved.y + radius));
        //canvas.drawBitmap(colorIcon[iconColorNo % gii.graphics.colorIcon.length], rect0,rect1, null);

        chevron.reset();
        chevron.moveTo(bounds.left, bounds.top);
        if (leftMiddleRight > 0)
            chevron.lineTo(bounds.left + chevronEdge,bounds.centerY());
        chevron.lineTo(bounds.left,bounds.bottom);
        chevron.lineTo(bounds.right,bounds.bottom);
        if (leftMiddleRight < 2)
            chevron.lineTo(bounds.right + chevronEdge, bounds.centerY());
        chevron.lineTo(bounds.right,bounds.top);
        chevron.lineTo(bounds.left,bounds.top);
        canvas.drawPath(chevron, gii.graphics.circleColor[iconColorNo % gii.graphics.circleColor.length]);

        //canvas.drawBitmap(gii.graphics.circleBitmap[iconBitmapNo % gii.graphics.circleBitmap.length], rect0, rect1, null);
        if (iconBitmapNo >= 0) {
            gii.graphics.drawableIcon[iconBitmapNo % gii.graphics.drawableIcon.length].setBounds(rect1);
            gii.graphics.drawableIcon[iconBitmapNo % gii.graphics.drawableIcon.length].draw(canvas);
        }
        if (iconBitmapNo == -1) {
            gii.graphics.exchangeIcon.setBounds(rect1);
            gii.graphics.exchangeIcon.draw(canvas);
        }
    }

}
//TODO: next version: Show multi line if needed