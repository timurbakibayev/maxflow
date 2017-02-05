package com.gii.maxflow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.batch.android.Batch;
import com.batch.android.BatchURLListener;
import com.batch.android.BatchUnlockListener;
import com.batch.android.CodeErrorInfo;
import com.batch.android.FailReason;
import com.batch.android.Offer;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements BatchUnlockListener, BatchURLListener {

    private static final String TAG = "MainActivity";

    public int backgroundColorBar = 2;
    public int backgroundColorTop = 2;
    public int backgroundColorBottom = 2;
    public int backgroundColorAccent = 2;
    public int backgroundColorArrow = 2;
    public int backgroundColorFont = 2;

    ImageButton bottom_filter;
    ImageButton bottom_map;
    ImageButton bottom_list;
    ImageButton bottom_pie;

    FloatingActionButton fab;
    //com.getbase.floatingactionbutton.FloatingActionsMenu fabMenu;
    //com.getbase.floatingactionbutton.FloatingActionButton fabReport;
    //com.getbase.floatingactionbutton.FloatingActionButton fabCharts;
    //com.getbase.floatingactionbutton.FloatingActionButton fabOperations;

    ArrayList<String> owner = new ArrayList<String>();
    ArrayList<AccessRight> accessRightses  = new ArrayList<AccessRight>();

    public SharedPreferences prefs;

    //static GII GIIApplication.gii;
    static Subscription subscription = new Subscription();
    private Timer tim = new Timer();
    private Timer tim1 = new Timer();

    public static java.util.ArrayList<Circle> circle = new ArrayList<>(); //the circles, loaded and saved to XML


    RelativeLayout canvas;
    Boolean canvasLoaded = false;
    Storage storage;
    Cloud cloud;
    Boolean active = true;
    MenuItem monthDescription;

    @Override
    protected void onResume() {
        super.onResume();
        updateColors();
        Log.w("RC","Resume");
        active = true;
        GIIApplication.gii.updateTitle();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.w("RC", "Pause");
        active = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Batch.onStart(this);
        Batch.Unlock.setUnlockListener(this);
        Batch.Unlock.setURLListener(this);
    }

    @Override
    protected void onStop()
    {
        Batch.onStop(this);

        super.onStop();
    }

    @Override
    protected void onDestroy()
    {
        Batch.onDestroy(this);

        super.onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent)
    {
        Batch.onNewIntent(this, intent);

        super.onNewIntent(intent);
    }

    static int n9;
    static int n10;
    static int n11;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        GIIApplication.gii.menu = menu;
        final MenuItem open_file_menu = menu.findItem(R.id.action_OpenFile);
        getMenuInflater().inflate(R.menu.open_file, open_file_menu.getSubMenu());
        final SubMenu submenu = open_file_menu.getSubMenu();
        final MenuItem copy_file_offline = menu.findItem(R.id.action_CopyFileOfflie);
        getMenuInflater().inflate(R.menu.copy_file_offline, copy_file_offline.getSubMenu());
        final SubMenu submenuCopyOffline = copy_file_offline.getSubMenu();
        final MenuItem open_file_menu_shared = menu.findItem(R.id.action_OpenFileShared);
        getMenuInflater().inflate(R.menu.open_file_shared, open_file_menu_shared.getSubMenu());
        final SubMenu submenuShared = open_file_menu_shared.getSubMenu();

        ArrayList<String> fileName;

        open_file_menu_shared.setVisible(true);
        copy_file_offline.setVisible(true);
        if (GIIApplication.gii.ref.getAuth() == null) {
            open_file_menu_shared.setVisible(false);
            copy_file_offline.setVisible(false);
        }
            if (storage == null)
                storage = new Storage(GIIApplication.gii);
            fileName = storage.fileListOffline("xml", GIIApplication.gii.properties);
            n9 = 0;
            for (int i = 0; i < fileName.size(); i++) {
                n9++;
                if (GIIApplication.gii.ref.getAuth() == null)
                    submenu.add(10,n9,n9,Properties.withoutXML(fileName.get(i)));
                else
                    submenuCopyOffline.add(12,n9,n9,Properties.withoutXML(fileName.get(i)));
            }

        if (GIIApplication.gii.ref.getAuth() != null) {
            n10 = 0;
            n11 = 0;
            owner.clear();
            accessRightses.clear();
            submenu.clear();
            //TODO: shallow query (get only keys, no values)
            GII.ref.child("maxflow/" + GII.ref.getAuth().getUid()).
                    addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            submenu.clear();
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                submenu.add(10,n10,n10,postSnapshot.getKey());
                                n10++;
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Log.e("Firebase", "The read failed 0: " + firebaseError.getMessage());
                        }
                    });
            submenuShared.clear();
            //TODO: shallow query (get only keys, no values)
            GII.ref.child("shared/" + GII.ref.getAuth().getUid()).
                    addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot snapshot) {
                            owner.clear();
                            accessRightses.clear();
                            submenuShared.clear();
                            for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                for (DataSnapshot inPostSnapshot : postSnapshot.getChildren()) {
                                    AccessRight accessRight = inPostSnapshot.getValue(AccessRight.class);
                                    String email = inPostSnapshot.child("ownerEmail").getValue().toString();
                                    if (email.length() > 10)
                                        email = email.substring(0, 8) + "..";
                                    submenuShared.add(11, n11, n11, email + "/" + inPostSnapshot.getKey());
                                    owner.add(postSnapshot.getKey());
                                    accessRightses.add(accessRight);
                                    n11++;
                                }
                            }
                        }

                        @Override
                        public void onCancelled(FirebaseError firebaseError) {
                            Log.e("Firebase", "The read failed 1: " + firebaseError.getMessage());
                        }
                    });
        }
        monthDescription = menu.findItem(R.id.action_monthClick);
        //monthDescription.setActionView(R.layout.datemenu);
        MenuItemCompat.setActionView(monthDescription,R.layout.datemenu);
        TextView tv = (TextView)(monthDescription.getActionView().findViewById(R.id.action_bar_date_text));
        tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GIIApplication.gii.filterDialog();
            }
        });
        GIIApplication.gii.actionBarDateText = tv;
        if (GIIApplication.gii != null)
            GIIApplication.gii.recalculateAll();

        toolbarIsVisibile = prefs.getBoolean("toolbar",false);
        saveToolbar();
        return true;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().requestFeature(Window.FEATURE_ACTION_BAR_OVERLAY);


        Log.w("RC","CREATE");
        prefs = PreferenceManager.getDefaultSharedPreferences(this.getBaseContext());

        //DONE: REMOVE!
        //unlockIDKFA();

        prepareBeep();

        setContentView(R.layout.activity_main);

        updateColors();

        final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        ((ImageButton)findViewById(R.id.bottom_menu)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switchtoolbar();
            }
        });

        ((ImageButton)findViewById(R.id.bottom_filter)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GIIApplication.gii.filterDialog();
            }
        });

        setSupportActionBar(toolbar);
        toolbar.setTitle("Anyway");
        toolbar.setSubtitle("def_file");

        fab = (FloatingActionButton) findViewById(R.id.fab);

        bottom_filter = (ImageButton) findViewById(R.id.bottom_filter);
        bottom_list = (ImageButton) findViewById(R.id.bottom_list);
        bottom_map = (ImageButton) findViewById(R.id.bottom_map);
        bottom_pie = (ImageButton) findViewById(R.id.bottom_pie);

        bottom_pie.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GIIApplication.gii.pressFloatingButton("report");
            }
        });

        bottom_list.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GIIApplication.gii.pressFloatingButton("operations");
            }
        });

        bottom_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GIIApplication.gii.appState = GII.AppState.idle;
            }
        });

        /*
        fabMenu = (com.getbase.floatingactionbutton.FloatingActionsMenu) findViewById(R.id.right_labels);
        fabReport = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_report);
        fabCharts = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_charts);
        fabOperations = (com.getbase.floatingactionbutton.FloatingActionButton) findViewById(R.id.fab_operations);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //GIIApplication.gii.pressFloatingButton("");
                onBackPressed();
            }
        });

        fabReport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GIIApplication.gii.pressFloatingButton("report");
                fabMenu.collapse();
                fabMenu.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
            }
        });

        fabCharts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GIIApplication.gii.pressFloatingButton("charts");
                fabMenu.collapse();
                fabMenu.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
            }
        });

        fabOperations.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                GIIApplication.gii.pressFloatingButton("operations");
                fabMenu.collapse();
                fabMenu.setVisibility(View.GONE);
                fab.setVisibility(View.VISIBLE);
            }
        });


        */

        //cloud = new Cloud(this);


        if (GIIApplication.gii == null) {
            Log.w("RC", "Creating GIIApplication.gii");
            GIIApplication.gii = new GII(this);
            GIIApplication.gii.fab = fab;
            //GIIApplication.gii.fabMenu = fabMenu;
            storage = new Storage(GIIApplication.gii);
            GIIApplication.gii.cloud = cloud;
            GIIApplication.gii.graphics.loadResources(this.getApplicationContext(),true, false);
        }
        canvas = (RelativeLayout) findViewById(R.id.canvas);
        GIIApplication.gii.bindActivity(this);
        GIIApplication.gii.fab = fab;
        GIIApplication.gii.lastAppState = GII.AppState.unreal;

        if (GIIApplication.gii.ref.getAuth() == null) {
            GIIApplication.gii.refreshUser();
            toolbar.setSubtitle(GIIApplication.gii.emailOrOffline);
        }
        else {
            GIIApplication.gii.refreshUser();
            toolbar.setSubtitle(GIIApplication.gii.ref.getAuth().getProviderData().get("email").toString());
        }

        tim.schedule(new TimerTask() {
            @Override
            public void run() {
                MainActivity.this.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (active)
                            GIIApplication.gii.timerTick();
                    }
                });
            }
        }, 0, 50);

        if (monthDescription != null)
            GIIApplication.gii.recalculateAll();

        ViewGroup parent = (ViewGroup)GIIApplication.gii.getParent();
        if (parent != null) {
           parent.removeAllViews();
        }
        canvas.addView(GIIApplication.gii);
        grantCameraPermission();

        showTutorial(false,0);
        showMyAppFree();
    }

    private void showTutorial(final boolean forced, final int step) {
        final Tutorial tutorial = new Tutorial(this,GIIApplication.gii, step);
        //TODO: if tutorial is not shown or forced
        if (!forced && prefs.getBoolean("tutorial_shown",false))
            return;

        SharedPreferences.Editor edit= prefs.edit();
        edit.putBoolean("tutorial_shown", true);
        edit.commit();


        AlertDialog ad =
            new AlertDialog.Builder(this)
                .setView(tutorial)
                .setTitle(Tutorial.stepTitles[step])
                //.setView(pin)
                .setPositiveButton(GIIApplication.gii.activity.getString(R.string.next), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (step < Tutorial.stepTitles.length - 1) {
                            showTutorial(true, step + 1);
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                tutorial.timer.cancel();
            }
        }).
                create();
        ad.show();
        Point screenSize = new Point();
        getWindowManager().getDefaultDisplay().getSize(screenSize);
        int t = Math.min(screenSize.x, screenSize.y);
        t = (int)(t * 0.95);
        ad.getWindow().setLayout(t,t);
    }

    private void showMyAppFree() {
        return;
        /*
        if (!prefs.getBoolean("myAppFree031116", false)) {
            Calendar cal = Calendar.getInstance();
            if (cal.get(Calendar.DAY_OF_MONTH) >= 2 &&
                    cal.get(Calendar.DAY_OF_MONTH) <= 4 &&
                    cal.get(Calendar.MONTH) == 10 && //november
                cal.get(Calendar.YEAR) == 2016 ) {

                SharedPreferences.Editor edit = prefs.edit();
                edit.putBoolean("myAppFree1", true);
                edit.putBoolean("myAppFree031116", true);
                edit.commit();

                Log.e(TAG, "showMyAppFree: :)");
                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(this.getString(R.string.myAppFree_Title))
                        .setMessage(this.getString(R.string.myAppFree_Message))
                        .setPositiveButton(this.getString(R.string.myAppFree_ThankYou), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                //nothing here, just a button
                            }
                        }).show();
            }
        }*/
    }

    private void updateColors() {
        backgroundColorBar = prefs.getInt("backColorBar",Color.parseColor("#00B9FF"));
        backgroundColorTop = prefs.getInt("backColorTop",Color.parseColor("#00B9FF"));
        backgroundColorBottom = prefs.getInt("backColorBottom",Color.parseColor("#CB429D"));
        backgroundColorAccent = prefs.getInt("backColorAccent",Color.parseColor("#CB429D"));
        backgroundColorArrow = prefs.getInt("backColorArrow",Color.parseColor("#0066FF"));
        backgroundColorFont = prefs.getInt("backColorFont",Color.parseColor("#FFFFFF"));

        if (GIIApplication.gii != null) {
            GIIApplication.gii.graphics.mainFont.setColor(backgroundColorFont);
            GIIApplication.gii.graphics.arrowPaint.setColor(backgroundColorArrow);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

            getWindow().setStatusBarColor(backgroundColorBar);
        }
        ((Toolbar) findViewById(R.id.toolbar)).setBackgroundColor(backgroundColorBar);
        ((RelativeLayout)findViewById(R.id.RLBar)).setBackgroundColor(backgroundColorBar);

        if (GIIApplication.gii != null &&
                GIIApplication.gii.graphics.bgColor != null) {
            GIIApplication.gii.graphics.bgColor.setShader(
                    new RadialGradient(200, 0, 1000, backgroundColorTop, backgroundColorBottom, Shader.TileMode.CLAMP));
            GIIApplication.gii.lastAppState = GII.AppState.calculator;
        }
    }

    boolean toolbarIsVisibile = false;

    private void switchtoolbar() {
        toolbarIsVisibile = prefs.getBoolean("toolbar",false);
        toolbarIsVisibile = !toolbarIsVisibile;
        saveToolbar();
    }

    private void saveToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        View decorView = getWindow().getDecorView();

        if (toolbarIsVisibile) {
            toolbar.setVisibility(View.VISIBLE);
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            ((ImageButton)findViewById(R.id.bottom_menu)).setBackgroundColor(backgroundColorAccent);
        }
        else {
            toolbar.setVisibility(View.GONE);
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            ((ImageButton)findViewById(R.id.bottom_menu)).setBackgroundColor(Color.TRANSPARENT);
        }
        SharedPreferences.Editor edit= prefs.edit();
        edit.putBoolean("toolbar", toolbarIsVisibile);
        edit.commit();
    }

    public void grantCameraPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (this.checkSelfPermission(android.Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted");
                return;
            } else {
                //Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.CAMERA}, 1);
                return ;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            //Log.v(TAG,"Permission is granted");
            return ;
        }
    }

    public void showSnackBar() {
        Snackbar.make(findViewById(R.id.fab), "Replace with your own action", Snackbar.LENGTH_LONG)
                .setAction("Action", null).show();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onBackPressed() {
        if (GIIApplication.gii.appState == GII.AppState.calculator &&
                GIIApplication.gii.calcWindow.editOperation != null) {
            GIIApplication.gii.appState = GII.AppState.showOperations;
            return;
        }

        if (GIIApplication.gii.appState == GII.AppState.reporting ||
                GIIApplication.gii.appState == GII.AppState.chartPlotting ||
                GIIApplication.gii.appState == GII.AppState.showOperations ||
                GIIApplication.gii.appState == GII.AppState.editMode ||
                GIIApplication.gii.appState == GII.AppState.calculator) {
            GIIApplication.gii.appState = GII.AppState.idle;
            //fab.setVisibility(View.GONE);
            //fabMenu.setVisibility(View.VISIBLE);
            return;
        }

        if (GIIApplication.gii.appState == GII.AppState.iconChoose) {
            GIIApplication.gii.appState = GIIApplication.gii.iconWindow.returnAppState;
            if (GIIApplication.gii.appState == GII.AppState.idle) {
                //fab.setVisibility(View.GONE);
                //fabMenu.setVisibility(View.VISIBLE);
            }
            return;
        }
        super.onBackPressed();
    }

    public void showMessage(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.

        if (!GIIApplication.gii.pinCodeEntered)
            return true;

        int id = item.getItemId();
        String caption = item.getTitle().toString();

        /*if (caption.substring(0, 2).equals("  ")) {
            String filename = caption.substring(2,caption.length()).concat(".xml");
            GIIApplication.gii.loadFile(filename);
            if (monthDescription != null)
                GIIApplication.gii.recalculateAll();
        }*/
        if (item.getGroupId() == 10) {
            String filename = caption;
            if (GIIApplication.gii.ref.getAuth() == null)
                filename = filename.concat(".xml");
            GIIApplication.gii.loadFile(filename);
            if (monthDescription != null)
                GIIApplication.gii.recalculateAll();
        }

        if (item.getGroupId() == 11) {
            String filename = caption.split("/")[1];
            GIIApplication.gii.loadFileShared(filename,owner.get(item.getItemId()));
            GIIApplication.gii.properties.accessRight = accessRightses.get(item.getItemId());
            if (monthDescription != null)
                GIIApplication.gii.recalculateAll();
        }

        if (item.getGroupId() == 12) {
            if (GIIApplication.gii.ref.getAuth() != null) {
                String filename = caption;
                String filenameXML = caption.concat(".xml");
                //GIIApplication.gii.loadFile(filenameXML);
                try {
                    GIIApplication.gii.properties = new Properties();
                    Log.w("properties","new properties");
                    GIIApplication.gii.circle = new ArrayList<>();
                    GIIApplication.gii.operations = new ArrayList<>();
                    GIIApplication.gii.properties.fileName = filenameXML;
                    storage.loadXML(GIIApplication.gii.properties, GIIApplication.gii.circle, GIIApplication.gii.operations);
                    GIIApplication.gii.properties.fileName = filename;
                    GIIApplication.gii.properties.owner = "";
                    GIIApplication.gii.updateFile(true);
                    GIIApplication.gii.loadFile(filename);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                if (monthDescription != null)
                    GIIApplication.gii.recalculateAll();
            }
        }

        if (caption.equals(getString(R.string.getFilesFromCloud))) {
            GIIApplication.gii.syncFiles();
        }

        if (id == R.id.action_NewFile) {
            createNewFile();
        }

        if (id == R.id.action_CopyFile) {
            copyFile();
        }

        if (id == R.id.action_rate) {
            rateApp();
        }

        if (id == R.id.action_ShareFileXML) {
            storage.sendFile(GIIApplication.gii.properties);
        }

        if (id == R.id.action_ShareFile) {
            //storage.shareFile(GIIApplication.gii.properties,this);
            Intent shareFileActivity = new Intent(this, ShareFile.class);
            startActivity(shareFileActivity);

        }

        if (id == R.id.action_DeleteFile) {
            deleteFile();
        }

        if (id == R.id.action_nextMonth) {
            GIIApplication.gii.switchPage(1);
            GIIApplication.gii.updateFile(true);
        }

        if (id == R.id.action_prevMonth) {
            GIIApplication.gii.switchPage(-1);
            GIIApplication.gii.updateFile(true);
        }

        if (id == R.id.action_cloud_login) {
            login();
        }

        if (id == R.id.action_settings) {
            settings();
        }

        if (id == R.id.action_help) {
            help();
        }

        if (id == R.id.action_tutorial) {
            showTutorial(true,0);
        }

        if (id == R.id.action_pdf) {
            exportPDF();
        }

        if (id == R.id.action_currency) {
            chooseCurrencies();
        }

        return super.onOptionsItemSelected(item);
    }


    private boolean MyStartActivity(Intent aIntent) {
        try
        {
            startActivity(aIntent);
            return true;
        }
        catch (ActivityNotFoundException e)
        {
            return false;
        }
    }


    //On click event for rate this app button
    public void rateApp() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        //Try Google play
        intent.setData(Uri.parse("market://details?id=com.gii.maxflow"));
        if (!MyStartActivity(intent)) {
            //Market (Google play) app seems not installed, let's try to open a webbrowser
            intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.gii.maxflow"));
            if (!MyStartActivity(intent)) {
                //Well if this also fails, we have run out of options, inform the user.
                Toast.makeText(this, "Could not open Android market, please install the market app.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void chooseCurrencies() {
        final String[] currencyArray = GIIApplication.gii.properties.currency.replace(" ",",").split(",");
        final String[] allCurrencies = this.getResources().getStringArray(R.array.currency_array);
        final boolean[] checked = new boolean[allCurrencies.length];
        for (int i = 0; i < allCurrencies.length; i++) {
            checked[i] = false;
            for (int j = 0; j < currencyArray.length; j++)
                if (currencyArray[j].toUpperCase().equals(allCurrencies[i].toUpperCase()))
                    checked[i] = true;
        }

        new AlertDialog.Builder(GIIApplication.gii.activity)
                .setTitle(GIIApplication.gii.activity.getString(R.string.title_choose_currencies))
                //.setView(pin)
                .setMultiChoiceItems(allCurrencies,checked,new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog,int which,boolean ischecked) {
                        switch (which) {
                            case Dialog.BUTTON_NEGATIVE: // Cancel button selected, do nothing
                                dialog.cancel();
                                break;

                            default: // choice item selected
                                // store the new selected value in the static variable
                                //mSelectedIndex = which;
                                checked[which] = ischecked;
                                break;
                        }
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newCurrencies = "";
                        boolean first = true;
                        for (int i = 0; i < allCurrencies.length; i++) {
                            if (checked[i]) {
                                if (!first)
                                    newCurrencies = newCurrencies + ",";
                                newCurrencies = newCurrencies + allCurrencies[i];
                                first = false;
                            }
                        }
                        //TODO: curencies are not saved :( fie: ggggg
                        GIIApplication.gii.properties.currency = newCurrencies;
                        GIIApplication.gii.properties.syncedWithCloud = false;
                        GIIApplication.gii.updateFile(true);
                        chooseDefaultCurrency();
                    }
                }).setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                //return;
            }
        }).
                show();
    }

    private void chooseDefaultCurrency() {
        final String[] currencyArray = GIIApplication.gii.properties.currency.replace(" ",",").split(",");
        if (GIIApplication.gii.properties.currency.equals("")) {
            Log.w(TAG, "chooseDefaultCurrency: no currencies");
            GIIApplication.gii.properties.defaultCurrency = "";
            GIIApplication.gii.properties.syncedWithCloud = false;
            GIIApplication.gii.updateFile(true);
            return;
        }
        if (currencyArray.length == 1) {
            Log.w(TAG, "chooseDefaultCurrency: only one currency");
            GIIApplication.gii.properties.defaultCurrency = GIIApplication.gii.properties.currency.toUpperCase();
            GIIApplication.gii.properties.syncedWithCloud = false;
            GIIApplication.gii.updateFile(true);
            checkIfThereAreOperationsWithMissingCurrency();
            return;
        }

        int defaultCurIndex = 0;
        for (int i = 0; i < currencyArray.length; i++) {
            if (currencyArray[i].equals(GIIApplication.gii.properties.defaultCurrency))
                defaultCurIndex = i;
        }
        new AlertDialog.Builder(GIIApplication.gii.activity)
                .setTitle(GIIApplication.gii.activity.getString(R.string.title_choose_default_currency))
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
                                //mSelectedIndex = which;
                                GIIApplication.gii.properties.defaultCurrency = GIIApplication.gii.properties.currency.replace(" ",",").split(",")[which];;
                                GIIApplication.gii.properties.syncedWithCloud = false;
                                GIIApplication.gii.updateFile(true);
                                dialog.dismiss();
                                checkIfThereAreOperationsWithMissingCurrency();
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

    private void checkIfThereAreOperationsWithMissingCurrency() {

        boolean thereAreNonCurrencyOperations = false;

        for (Operation _operation : GIIApplication.gii.operations) {
            if (_operation.currency.equals(""))
                thereAreNonCurrencyOperations = true;
        }

        if (!thereAreNonCurrencyOperations)
            return;

        new AlertDialog.Builder(GIIApplication.gii.activity)
                //.setTitle(GIIApplication.gii.activity.getString(R.string.title_choose_default_currency))
                .setMessage(GIIApplication.gii.activity.getString(R.string.title_choose_default_currency_fix))
                //.setView(pin)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        //return;
                    }
                })
                .setPositiveButton(GIIApplication.gii.activity.getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                fixOperationsCurrencies();
                }
            })
            .setNegativeButton(GIIApplication.gii.activity.getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).
                show();
    }

    private void fixOperationsCurrencies() {
        showMessage(GIIApplication.gii.activity.getString(R.string.fixing_operations));
        for (Operation _opeation : GIIApplication.gii.operations) {
            if (_opeation.currency.equals("")) {
                _opeation.currency = GIIApplication.gii.properties.defaultCurrency;
                _opeation.exchangeRate = GIIApplication.gii.exchangeRates.getRate(GIIApplication.gii.properties.defaultCurrency);
                _opeation.setSyncedWithCloud(false);
            }
        }
        GIIApplication.gii.updateFile(true);
        for (Circle circle1 : GIIApplication.gii.circle) {
            circle1.theIconParams = "fix";
        }
        showMessage(GIIApplication.gii.activity.getString(R.string.done));
    }

    private void exportPDF() {
        storage.exportToPDF(GIIApplication.gii.properties);
    }

    public void login() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, 1);
    }

    private void settings() {
        circle = GIIApplication.gii.circle;
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivityForResult(intent, 1);
    }

    private void help() {
        Intent intent = new Intent(this, ScrollingHelp.class);
        startActivityForResult(intent, 1);
    }

    static final int REQUEST_IMAGE_CAPTURE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            //mImageView.setImageBitmap(imageBitmap);
            if (GIIApplication.gii.appState == GII.AppState.showOperations) {
                GIIApplication.gii.circleById(GIIApplication.gii.selectedId).photoString =
                        GIIApplication.encodeToBase64(imageBitmap, Bitmap.CompressFormat.JPEG, 100);
                GIIApplication.gii.circleById(GIIApplication.gii.selectedId).setSyncedWithCloud(false);
                showMessage(getString(R.string.saved));
                GIIApplication.gii.circleById(GIIApplication.gii.selectedId).theIconParams = "";
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (GIIApplication.gii.ref.getAuth() == null) {
            GIIApplication.gii.refreshUser();
            toolbar.setSubtitle(GIIApplication.gii.emailOrOffline);
        }
        else {
            GIIApplication.gii.refreshUser();
            toolbar.setSubtitle(GIIApplication.gii.ref.getAuth().getProviderData().get("email").toString());
        }
        GIIApplication.gii.graphics.loadResources(GIIApplication.gii.getContext(),true, false);
        prepareBeep();
        invalidateOptionsMenu();
        if (monthDescription != null)
            GIIApplication.gii.updateFile(true);
    }

    private void createNewFile() {
        final EditText newFilename = new EditText(MainActivity.this);
        final CheckBox useTemplate = new CheckBox(MainActivity.this);

        useTemplate.setText(this.getString(R.string.use_template));

        newFilename.setFocusable(true);
        newFilename.setFocusableInTouchMode(true);
        newFilename.setSingleLine(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog dialog = builder.create();

        newFilename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        final LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        layout.addView(newFilename);
        layout.addView(useTemplate);

        builder.setTitle("New file")
                .setMessage("Enter filename")
                .setView(layout)
                .setPositiveButton("Create", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = sanitize(newFilename.getText().toString());
                        if (!newName.equals("error")) {
                            GIIApplication.gii.loadFile(newName);
                            if (useTemplate.isChecked()) {
                                storage.fromTemplate(GIIApplication.gii.circle);
                                GIIApplication.gii.updateFile(true);
                            }
                            invalidateOptionsMenu();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();

    }
    private void copyFile() {
        final EditText newFilename = new EditText(MainActivity.this);

        newFilename.setFocusable(true);
        newFilename.setFocusableInTouchMode(true);
        newFilename.setSingleLine(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog dialog = builder.create();

        newFilename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });
        builder.setTitle("Copy file")
                .setMessage("Enter filename")
                .setView(newFilename)
                .setPositiveButton("Copy", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String newName = sanitize(newFilename.getText().toString());
                        if (!newName.equals("error")) {
                            GIIApplication.gii.properties.fileName = newName;
                            GIIApplication.gii.properties.owner = "";
                            GIIApplication.gii.updateFile(true);
                            GIIApplication.gii.loadFile(newName);
                            //setTitle(newName);
                            invalidateOptionsMenu();
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();

    }

    private void deleteFile() {
        final EditText deleteFilename = new EditText(MainActivity.this);

        deleteFilename.setFocusable(true);
        deleteFilename.setFocusableInTouchMode(true);
        deleteFilename.setSingleLine(true);

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        final AlertDialog dialog = builder.create();

        deleteFilename.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        builder.setTitle("DELETE")
                .setMessage("Enter filename")
                .setView(deleteFilename)
                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String deleteName = sanitize(deleteFilename.getText().toString());

                        if (GIIApplication.gii.ref.getAuth() != null) {
                                GII.ref.child("maxflow/" + GII.ref.getAuth().getUid()+"/"+Properties.withoutXML(deleteName)).setValue(null);
                        }

                        if (!deleteName.equals("error")) {
                            storage.deleteFile(deleteName, GIIApplication.gii.properties);
                            invalidateOptionsMenu();
                            if (GIIApplication.gii.properties.fileName.equals(deleteName)) {
                                GIIApplication.gii.loadFile("any");
                                //setTitle(GIIApplication.gii.properties.fileName);
                            }
                        }
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();

    }


    private String sanitize(String s) {
        s = s.trim();
        if (s.length()>=4)
            if (s.substring(s.length()-4).toUpperCase().equals(".XML"))
                s = s.substring(0,s.length()-4);
        s = s + ".xml";
        if (s.length() == 4)
            s = "error";
        return s;
    }
    static Ringtone r;
    public void prepareBeep() {
        try {
            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            String soundUri = prefs.getString("notifications_new_message_ringtone",RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION).toString());
            r = RingtoneManager.getRingtone(getApplicationContext(), Uri.parse(soundUri));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void beep() {
        try {
                r.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onURLWithCodeFound(String code)
    {
        // Function called when your app is opened from a link that contains a Batch code. You may want to display a load UI to wait for success or fail callback.
    }

    @Override
    public void onURLCodeSuccess(String code, Offer offer)
    {
        // Hide wait UI.

        // Give features & resources contained in the campaign to the user.

        // Show success UI.
    }

    @Override
    public void onURLCodeFailed(String code, FailReason reason, CodeErrorInfo info)
    {
        // Hide wait UI.

        // Show a message error to the user using the reason code and error info.
    }

    public void unlockIDKFA() {
        SharedPreferences.Editor edit= prefs.edit();
        edit.putBoolean("idkfa", true);
        edit.commit();
    }

    public void unlockIDDQD() {
        SharedPreferences.Editor edit= prefs.edit();
        edit.putBoolean("iddqd", true);
        edit.commit();
    }

    @Override
    public void onRedeemAutomaticOffer(Offer offer)
    {
        SharedPreferences.Editor edit= prefs.edit();
        edit.putBoolean("myAppFree1", true);
        edit.commit();

        Log.e(TAG, "onRedeemAutomaticOffer: BATCH! BAM! " + offer.getItems().size() + " item(s)");

        showMyAppFree();
    }

}