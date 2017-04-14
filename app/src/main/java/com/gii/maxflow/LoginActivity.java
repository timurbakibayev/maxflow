package com.gii.maxflow;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ComponentName;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.vending.billing.IInAppBillingService;
import com.firebase.client.AuthData;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.gii.maxflow.util.IabBroadcastReceiver;
import com.gii.maxflow.util.IabHelper;
import com.gii.maxflow.util.IabResult;
import com.gii.maxflow.util.Inventory;
import com.gii.maxflow.util.Purchase;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements IabBroadcastReceiver.IabBroadcastListener, LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */

    public static String base64EncodedPublicKey = "MIIBIjANB".concat("gkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAqBw").
            concat("IqCFY2LqGe4Vsdd5IRHAWiSaP4ZQvJg0RznnU1zytB4YXgDePtPwsKX93ZOqgel311SO77B8flngZlcEermMDkhuKOPSAx7whvLBZ8I/Qo9riq96g/9Ksa7DQn0IHZHsdAng5rBSXKbyht56V25OcrKOHkq5F3Tco3Ze1EEBq18QRKnrfFmmu+WXsWoLp7sYWeVS20um8UOHsOEwaFTj3J/2liduweyqmJG7JamlZSSxQADX600h+fkRMcbn+qhpa1sAMwBQVCbCG7+yquoPdBy54lr1qKRqXIFzUwAX6PcWC86vq6zvTex3koTInvNO4KgJEmFh6doLLei1bPQIDAQAB");

    IInAppBillingService mService;
    IabHelper mHelper;


    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };


    private static final int REQUEST_READ_CONTACTS = 0;

    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    IabBroadcastReceiver mBroadcastReceiver;


    @Override
    public void receivedBroadcast() {
        // Received a broadcast notification that the inventory of items has changed
        Log.d("purchase", "Received broadcast notification. Querying inventory.");
        mHelper.queryInventoryAsync(mGotInventoryListener);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        Intent serviceIntent =
                new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);

        mHelper = new IabHelper(this, base64EncodedPublicKey);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
                public void onIabSetupFinished(IabResult result) {
                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        Log.d("Purchase", "Problem setting up In-app Billing: " + result);
                    } else {
                        // Hooray, IAB is fully set up!

                        if (mHelper != null) {
                            List skuList = new ArrayList<>();
                            skuList.add("cloud1m");
                            skuList.add("cloud1y");
                            Bundle querySkus = new Bundle();

                            // Important: Dynamically register for broadcast messages about updated purchases.
                            // We register the receiver here instead of as a <receiver> in the Manifest
                            // because we always call getPurchases() at startup, so therefore we can ignore
                            // any broadcasts sent while the app isn't running.
                            // Note: registering this listener in an Activity is a bad idea, but is done here
                            // because this is a SAMPLE. Regardless, the receiver must be registered after
                            // IabHelper is setup, but before first call to getPurchases().

                            //mBroadcastReceiver = new IabBroadcastReceiver(LoginActivity.this);
                            //IntentFilter broadcastFilter = new IntentFilter(IabBroadcastReceiver.ACTION);
                            //registerReceiver(mBroadcastReceiver, broadcastFilter);

                            mHelper.queryInventoryAsync(true, skuList,
                                    mQueryFinishedListener);
                        }
                    }
                }
            });


        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        final Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        final Button mResetPasswordButton = (Button) findViewById(R.id.reset_password_button);
        final Button buy_1mButton = (Button) findViewById(R.id.buy_1m);
        final Button buy_1yButton = (Button) findViewById(R.id.buy_1y);
        //TODO: consider removing next two lines to make paid.
        buy_1mButton.setVisibility(View.GONE);
        buy_1yButton.setVisibility(View.GONE);
        final TextView licenseTextView = (TextView) findViewById(R.id.licenseTextView);
        //TODO: consider removing next line to make paid.
        licenseTextView.setVisibility(View.GONE);
        final Button mChangePasswordButton = (Button) findViewById(R.id.change_password_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        mResetPasswordButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                resetPassword();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);


        final Button signOutButton = (Button) findViewById(R.id.sign_out_button);
        if (GII.ref.getAuth() != null) {
            mEmailView.setVisibility(View.GONE);
            mPasswordView.setVisibility(View.GONE);
            mEmailSignInButton.setVisibility(View.GONE);
            mResetPasswordButton.setVisibility(View.GONE);
            mChangePasswordButton.setEnabled(true);
            signOutButton.setEnabled(true);
            signOutButton.setText(getBaseContext().getString(R.string.action_sign_out) + " (" + GII.ref.getAuth().getProviderData().get("email").toString() + ")");
            signOutButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    GII.ref.unauth();
                    mEmailView.setVisibility(View.VISIBLE);
                    mPasswordView.setVisibility(View.VISIBLE);
                    signOutButton.setEnabled(false);
                    buy_1mButton.setEnabled(false);
                    buy_1yButton.setEnabled(false);
                    mChangePasswordButton.setEnabled(false);
                    mEmailSignInButton.setVisibility(View.VISIBLE);
                    mResetPasswordButton.setVisibility(View.VISIBLE);
                }
            });
            mChangePasswordButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    changePassword();
                }
            });
        }


        if (GII.ref.getAuth() != null) {
            //TODO: Enable?
//            buy_1mButton.setEnabled(true);
//            buy_1yButton.setEnabled(true);
            Calendar c = Calendar.getInstance();
            if (GIIApplication.gii.prefs.getBoolean("myAppFree1",false)) {

                if (MainActivity.subscription.endingDate != null) {
                    c.setTime(MainActivity.subscription.endingDate);
                    licenseTextView.setText(getBaseContext().getString(R.string.licence_expires) + " " + GII.dateText(c.getTime()));
                }
                //buy_1mButton.setText(getBaseContext().getString(R.string.claim_3m));
                //TODO: Remove next line to enable 1mButton
                buy_1mButton.setVisibility(View.GONE);
                buy_1yButton.setVisibility(View.GONE);
                buy_1mButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        freeLicense("1m");
                        finish();
                    }
                });
            } else if (MainActivity.subscription.endingDate == null) {
                buy_1mButton.setText(getBaseContext().getString(R.string.claim_1m));
                if (GIIApplication.gii.prefs.getBoolean("myAppFree1",false))
                    buy_1mButton.setText(getBaseContext().getString(R.string.claim_3m));
                buy_1yButton.setVisibility(View.GONE);
                buy_1mButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        freeLicense("1m");
                        finish();
                    }
                });
            } else {
                c.setTime(MainActivity.subscription.endingDate);
                licenseTextView.setText(getBaseContext().getString(R.string.licence_expires) + " " + GII.dateText(c.getTime()));
                buy_1mButton.setText(getBaseContext().getString(R.string.buy_1m) + " " + cloud1mPrice);
                buy_1yButton.setText(getBaseContext().getString(R.string.buy_1y) + " " + cloud1yPrice);
                buy_1mButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        buy("1m");
                    }
                });
                buy_1yButton.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        buy("1y");
                    }
                });
            }
        }

        loadPassword();
    }

    String cloud1mPrice = "";
    String cloud1yPrice = "";

    private Timer tim = new Timer();

    IabHelper.QueryInventoryFinishedListener
            mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory)
        {
            if (result.isFailure()) {
                // handle error
                return;
            }

            if (inventory != null && inventory.getSkuDetails("cloud1m").getPrice() != null) {

                cloud1mPrice =
                        inventory.getSkuDetails("cloud1m").getPrice();
                cloud1yPrice =
                        inventory.getSkuDetails("cloud1y").getPrice();

            } else {
                cloud1mPrice = "no prices error";
                cloud1yPrice = "no prices error";
                if (inventory == null) {
                    cloud1yPrice = "no inventory error";
                    cloud1mPrice = "no inventory error";
                }
            }

            // update the UI

            Button buy_1mButton = (Button) findViewById(R.id.buy_1m);
            Button buy_1yButton = (Button) findViewById(R.id.buy_1y);
            if (!buy_1mButton.getText().toString().equals(getBaseContext().getString(R.string.claim_1m)) &&
                    !buy_1mButton.getText().toString().equals(getBaseContext().getString(R.string.claim_3m)))
                buy_1mButton.setText(getBaseContext().getString(R.string.buy_1m) + " " + cloud1mPrice);
            buy_1yButton.setText(getBaseContext().getString(R.string.buy_1y) + " " + cloud1yPrice);

            mHelper.queryInventoryAsync(mGotInventoryListener);
        }
    };

    private void buy(String param) {
        if (param.equals("1m")) {
            Calendar c = Calendar.getInstance();
            if (MainActivity.subscription.endingDate != null &&
                    MainActivity.subscription.endingDate.after(c.getTime()))
                c.setTime(MainActivity.subscription.endingDate);
            c.add(Calendar.MONTH, 1);

            Subscription newSubscription = new Subscription();
            newSubscription.endingDate = c.getTime();
            newSubscription.userId = GII.ref.getAuth().getUid();
            newSubscription.sign();
            initiatePurchase(newSubscription, "cloud1m");
        }
        if (param.equals("1y")) {
            Calendar c = Calendar.getInstance();
            if (MainActivity.subscription.endingDate != null &&
                    MainActivity.subscription.endingDate.after(c.getTime()))
                c.setTime(MainActivity.subscription.endingDate);
            c.add(Calendar.YEAR, 1);

            Subscription newSubscription = new Subscription();
            newSubscription.endingDate = c.getTime();
            newSubscription.userId = GII.ref.getAuth().getUid();
            newSubscription.sign();
            initiatePurchase(newSubscription, "cloud1y");
        }

    }

    Subscription intentSubscription = null;
    private void initiatePurchase(Subscription newSubscription, String skuName) {
        intentSubscription = newSubscription;
        //GII.ref.child("subscriptions/" + GII.ref.getAuth().getUid() + "/").
        //        push().setValue(newSubscription);
        // <------------ This is needed if paid!!! xxxxxxxxx
        Log.w("purchase","initiating purchase");

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            GII.ref.child("subscriptions/" + GII.ref.getAuth().getUid() + "/").
                    push().setValue(newSubscription);
            finish();
            startActivity(getIntent());
            return;
        }

        if (cloud1mPrice.equals("") || cloud1yPrice.equals("")) {
            Log.w("purchase","no prices aquired :(");
            return;
        }

        mHelper.launchPurchaseFlow(this, skuName, 31,
                mPurchaseFinishedListener, intentSubscription.signature);

        /*try {
            Bundle skuDetails = mService.getSkuDetails(3,getPackageName(),"inapp", querySkus);
            int response = skuDetails.getInt("RESPONSE_CODE");
            if (response == 0) {
                Log.e("purchase","response is zero! good!");
                ArrayList<String> responseList = skuDetails.getStringArrayList("DETAILS_LIST");
                for (String thisResponse : responseList) {
                    JSONObject object = new JSONObject(thisResponse);
                    String sku = object.getString("productId");
                    String price = object.getString("price");
                    if (sku.equals(skuName)) {
                        Log.e("purchase", "found sku " + sku + "price " + price);
                        Bundle buyIntentBundle = mService.getBuyIntent(3, getPackageName(),
                                sku,"inapp",intentSubscription.signature);
                        PendingIntent pendingIntent = buyIntentBundle.getParcelable("BUY_INTENT");
                        if (pendingIntent != null) {
                            Log.e("purchase", "starting intent for " + intentSubscription.signature);
                            startIntentSenderForResult(pendingIntent.getIntentSender(), 31, new Intent(),
                                    Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0));
                        }
                    }
                }
            }
            Log.e("purchase","end first try successfully");
        } catch (RemoteException e) {
            e.printStackTrace();
            Log.e("purchase","error " + e.toString());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e("purchase","error " + e.toString());
        } catch (IntentSender.SendIntentException e) {
            e.printStackTrace();
            Log.e("purchase","intent error " + e.toString());
        }*/
    }



    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase)
        {
            if (result.isFailure()) {
                Log.d("purchase", "Error purchasing: " + result);
                return;
            }
            else if (purchase.getSku().equals("cloud1m") || purchase.getSku().equals("cloud1y")) {
                // give user access to premium content and update the UI
                //mHelper.queryInventoryAsync(mGotInventoryListener);
                /*tim.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        LoginActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mHelper.queryInventoryAsync(mGotInventoryListener);
                                tim.cancel();
                            }
                        });
                    }
                }, 0, 3000);*/
                Log.w("purchase","trying to consume " + purchase.getDeveloperPayload());
                mHelper.consumeAsync(purchase,
                        mConsumeFinishedListener);
                String hashed = purchase.getDeveloperPayload();
                Log.w("purchase","finishing: " + hashed);
                Calendar c = Calendar.getInstance();
                c.setTime(Subscription.unHash(hashed));
                intentSubscription = new Subscription();
                intentSubscription.endingDate = c.getTime();
                intentSubscription.userId = GII.ref.getAuth().getUid();
                intentSubscription.sign();
                GII.ref.child("subscriptions/" + GII.ref.getAuth().getUid() + "/").
                        push().setValue(intentSubscription);
                Log.w("purchase","Pushing the intentSubscription");
                try {
                    Toast.makeText(getApplicationContext(),getBaseContext().getString(R.string.thank_you)
                            , Toast.LENGTH_LONG).show();
                } catch (Exception e) {

                }
                final TextView licenseTextView = (TextView) findViewById(R.id.licenseTextView);
                licenseTextView.setText(getBaseContext().getString(R.string.licence_expires) + " " + GII.dateText(c.getTime()));
            }
        }
    };

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result,
                                             Inventory inventory) {

            Log.w("purchase","mGotInventoryListener");
            if (result.isFailure()) {
                // handle error here
                Log.w("purchase","mGotInventoryListener - result failed");
            }
            else {
                // does the user have the premium upgrade?
                //mIsPremium = inventory.hasPurchase(SKU_PREMIUM);
                if (inventory.hasPurchase("cloud1m"))
                    mHelper.consumeAsync(inventory.getPurchase("cloud1m"),
                            mConsumeFinishedListener);

                if (inventory.hasPurchase("cloud1y"))
                    mHelper.consumeAsync(inventory.getPurchase("cloud1y"),
                            mConsumeFinishedListener);

                // update UI accordingly
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase, IabResult result) {
                    if (result.isSuccess()) {
                        String hashed = purchase.getDeveloperPayload();
                        Log.w("purchase","finishing: " + hashed);
                        Calendar c = Calendar.getInstance();
                        c.setTime(Subscription.unHash(hashed));
                        intentSubscription = new Subscription();
                        intentSubscription.endingDate = c.getTime();
                        intentSubscription.userId = GII.ref.getAuth().getUid();
                        intentSubscription.sign();
                        GII.ref.child("subscriptions/" + GII.ref.getAuth().getUid() + "/").
                                push().setValue(intentSubscription);
                        Log.w("purchase","Pushing the intentSubscription");
                        final TextView licenseTextView = (TextView) findViewById(R.id.licenseTextView);
                        licenseTextView.setText(getBaseContext().getString(R.string.licence_expires) + " " + GII.dateText(c.getTime()));
                    }
                    else {
                        // handle error
                        Log.e("purchase","mConsumeFinishedListener ERROR ");
                    }
                }
            };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.w("purchase", "onActivityResult(" + requestCode + "," + resultCode + "," + data);
        finish();
        startActivity(getIntent());
        /*
        // Pass on the activity result to the helper for handling
        if (!inappBillingHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
        else {
            Log.i("purchase", "onActivityResult handled by IABUtil.");
        }*/
        /*Log.e("purchase","onActivityResult " + requestCode);
        Calendar c = Calendar.getInstance();
        if (requestCode == 31) {
            int responseCode = data.getIntExtra("RESPONSE_CODE",0);
            String purchaseData = data.getStringExtra("INAPP_PURCHASE_DATA");
            String dataSignature = data.getStringExtra("INAPP_DATA_SIGNATURE");

            if (resultCode == RESULT_OK) {
                try {
                    JSONObject jo = new JSONObject(purchaseData);
                    String sku = jo.getString("productId");
                    GII.ref.child("subscriptions/" + GII.ref.getAuth().getUid() + "/").
                                push().setValue(intentSubscription);
                    int response = mService.consumePurchase(3, getPackageName(), intentSubscription.signature);
                    //mHelper.consumeAsync(inventory.getPurchase(SKU_GAS),
                    //        mConsumeFinishedListener);
                    Toast.makeText(getApplicationContext(),getBaseContext().getString(R.string.thank_you) ,
                            Toast.LENGTH_LONG).show();
                    c.setTime(intentSubscription.endingDate);
                    Button buy_1mButton = (Button) findViewById(R.id.buy_1m);
                    Button buy_1yButton = (Button) findViewById(R.id.buy_1y);
                    //update license message
                    //buy_1mButton.setText(getBaseContext().getString(R.string.licence_expires) + " " + GII.dateText(c.getTime()) + "\n" + getBaseContext().getString(R.string.buy_1m));
                    //buy_1yButton.setText(getBaseContext().getString(R.string.licence_expires) + " " + GII.dateText(c.getTime()) + "\n" + getBaseContext().getString(R.string.buy_1y));

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("purchase","JSON error " + e.toString());
                } catch (RemoteException e) {
                    e.printStackTrace();
                    Log.e("purchase","Could not consume the product :( " + e.toString());
                }
            } else {
                Log.e("purchase","Purchase result not ok");
            }
        }*/
    }

    public static void freeLicense(String param) {
        if (param.equals("1m")) {
            Calendar c = Calendar.getInstance();
            if (MainActivity.subscription.endingDate != null &&
                    MainActivity.subscription.endingDate.after(c.getTime()))
                c.setTime(MainActivity.subscription.endingDate);
            c.add(Calendar.MONTH,1);
            if (GIIApplication.gii.prefs.getBoolean("myAppFree1",false))
                c.add(Calendar.MONTH,2); //two more
            Subscription newSubscription = new Subscription();
            newSubscription.endingDate = c.getTime();
            newSubscription.userId = GII.ref.getAuth().getUid();
            newSubscription.sign();
            GII.ref.child("subscriptions/" + GII.ref.getAuth().getUid() + "/").
                    push().setValue(newSubscription);

            SharedPreferences.Editor edit= GIIApplication.gii.prefs.edit();
            edit.putBoolean("myAppFree1", false);
            edit.commit();

        }
    }

    private void savePassword(String email, String password) {
        SharedPreferences preferences = getSharedPreferences("emailPasswordRecircle", MODE_PRIVATE);
        SharedPreferences.Editor edit= preferences.edit();
        edit.putString("email", email);
        edit.putString("password", password);
        edit.commit();
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        //this will always fire, so no contacts are requested ever!
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return false;
        }

        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    String email = "";
    String password = "";

    private void changePassword() {
        if (GII.ref.getAuth() == null)
            return;
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final AlertDialog dialog = builder.create();
        final EditText newPassword1 = new EditText(this);
        final EditText newPassword2 = new EditText(this);
        final LinearLayout layout = new LinearLayout(this);
        newPassword1.setHint(getBaseContext().getString(R.string.change_password_new1));
        newPassword2.setHint(getBaseContext().getString(R.string.change_password_new2));
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.addView(newPassword1);
        layout.addView(newPassword2);

        final SharedPreferences preferences = getSharedPreferences("emailPasswordRecircle", MODE_PRIVATE);
        Toast.makeText(getApplicationContext(), preferences.getString("password", ""),
                Toast.LENGTH_LONG).show();

        builder.setTitle(getBaseContext().getString(R.string.change_password))
                //.setMessage(getContext().getString(R.string.subscription_expired))
                .setView(layout)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        if (newPassword1.getText().toString().equals(newPassword2.getText().toString()) &&
                                isPasswordValid(newPassword1.getText().toString())) {
                            GII.ref.changePassword(GII.ref.getAuth().getProviderData().get("email").toString(),
                                    preferences.getString("password", ""), newPassword1.getText().toString(),
                                    new Firebase.ResultHandler() {
                                        @Override
                                        public void onSuccess() {
                                            Toast.makeText(getApplicationContext(), getString(R.string.successfully_changed_password),
                                                    Toast.LENGTH_LONG).show();
                                        }

                                        @Override
                                        public void onError(FirebaseError firebaseError) {
                                            Toast.makeText(getApplicationContext(), firebaseError.getMessage(),
                                                    Toast.LENGTH_LONG).show();
                                        }
                                    });
                        } else
                            if (!isPasswordValid(newPassword1.getText().toString()))
                                Toast.makeText(getApplicationContext(), getString(R.string.error_invalid_password),
                                        Toast.LENGTH_LONG).show();
                        else
                                Toast.makeText(getApplicationContext(), getString(R.string.change_password_not_match),
                                        Toast.LENGTH_LONG).show();
                    }
                }).setNegativeButton(getBaseContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {

            }
        }).show();
    }

    private void resetPassword() {
        mEmailView.setError(null);
        email = mEmailView.getText().toString();
        boolean cancel = false;
        View focusView = null;
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }
        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            showProgress(true);
            GII.ref.resetPassword(email, new Firebase.ResultHandler() {
                @Override
                public void onSuccess() {
                    showProgress(false);
                    Toast.makeText(getApplicationContext(), getString(R.string.successfully_reset_password),
                            Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(FirebaseError firebaseError) {
                    showProgress(false);
                    Toast.makeText(getApplicationContext(), getString(R.string.reset_password_failed),
                            Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        email = mEmailView.getText().toString();
        password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            //mAuthTask = new UserLoginTask(email, password);
            //mAuthTask.execute((Void) null);
            /*
            ParseUser.logInInBackground(email, password,
                    new LogInCallback() {
                        public void done(ParseUser user, ParseException e) {
                            if (user != null) {
                                Toast.makeText(getApplicationContext(),
                                        "Successfully Logged in",
                                        Toast.LENGTH_LONG).show();
                                showProgress(false);
                                savePassword(email, password);
                                finish();
                            } else {
                                if (e.getCode() == e.OBJECT_NOT_FOUND) {
                                    signUp(email, password);
                                }

                            }
                        }
                    });
                    */
            loginFirebase(email,password);
        }
    }

    boolean signedUp = false;
    Firebase.AuthResultHandler authResultHandler = new Firebase.AuthResultHandler() {
        @Override
        public void onAuthenticated(AuthData authData) {
            savePassword(email, password);
            Map<String, String> map = new HashMap<String, String>();
            map.put("provider", authData.getProvider());
            if(authData.getProviderData().containsKey("displayName")) {
                map.put("displayName", authData.getProviderData().get("displayName").toString());
            }
            if(authData.getProviderData().containsKey("email")) {
                map.put("email", authData.getProviderData().get("email").toString());
            }
            GIIApplication.gii.ref.child("users").child(authData.getUid()).setValue(map);
            finish();
        }
        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            // Authenticated failed with error firebaseError
            //Try to signup, if possible
            switch (firebaseError.getCode()) {
                case FirebaseError.USER_DOES_NOT_EXIST:
                    //Toast.makeText(getApplicationContext(),
                    //        "No such user", Toast.LENGTH_LONG)
                    //        .show();
                    if (!signedUp)
                        signUp(email,password);
                    signedUp = true;
                    break;
                case FirebaseError.INVALID_PASSWORD:
                    Toast.makeText(getApplicationContext(),
                            getString(R.string.wrong_password), Toast.LENGTH_LONG)
                            .show();
                    break;
                default:
                    // handle other errors
                    break;
            }

            showProgress(false);
        }
    };

    private void loginFirebase(String email, String password) {
        //savePassword(email, password);
        //finish();
        GII.ref.authWithPassword(email,password,authResultHandler);
    }

    private void loadPassword() {
        SharedPreferences preferences = getSharedPreferences("emailPasswordRecircle", MODE_PRIVATE);
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
        mPasswordView = (EditText) findViewById(R.id.password);
        mEmailView.setText(preferences.getString("email", ""));
        mPasswordView.setText(preferences.getString("password", ""));
    }

    private void signUp(final String email, final String password) {
        Log.w("Firebase","Registered user:" + " trying to sign up");
        GII.ref.createUser(email,password, new Firebase.ValueResultHandler<Map<String, Object>>() {
                    @Override
                    public void onSuccess(Map<String, Object> result) {
                        Toast.makeText(getApplicationContext(), getString(R.string.successfully_registered),
                                Toast.LENGTH_LONG).show();
                        Log.w("Firebase","Registered user:" + result.get("uid") + ", giving free license");
                    }

                    @Override
                    public void onError(FirebaseError firebaseError) {
                        // there was an error]
                        Toast.makeText(getApplicationContext(), getString(R.string.register_failed),
                                Toast.LENGTH_LONG).show();
                        Log.w("Firebase","Registering user ERROR: " + firebaseError.toString());
                    }
                }
            );

        GII.ref.authWithPassword(email,password,authResultHandler);

        /*
        ParseUser user = new ParseUser();
        user.setUsername(email);
        user.setPassword(password);
        user.signUpInBackground(new SignUpCallback() {
            public void done(ParseException e) {
                if (e == null) {
                    // Show a simple Toast message upon successful registration
                    Toast.makeText(getApplicationContext(),
                            "Successfully Signed up, please log in.",
                            Toast.LENGTH_LONG).show();
                    showProgress(false);
                } else {
                    Toast.makeText(getApplicationContext(),
                            "Wrong username or password (" + e.getCode() + ")", Toast.LENGTH_LONG)
                            .show();
                    showProgress(false);
                }
                showProgress(false);
            }
        });
        */
    }
    private boolean isEmailValid(String email) {
        return email.contains("@") && email.contains(".");
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
        if (mHelper != null) mHelper.dispose();
        mHelper = null;
    }
}

