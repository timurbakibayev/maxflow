package com.gii.maxflow;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by Timur on 26-May-16.
 */
public class ExchangeRates {

    public String dateRef = "";

    public ExchangeRates(Context context) {
        Calendar c = Calendar.getInstance();
        dateRef = c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR);
        Log.e(TAG, "ExchangeRates: init");
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.getString("LastExchangeRates", "").equals(dateRef)) {
            rates = prefs.getString("rates", "");
            alreadyRead = true;
            readRatesFromInternet();
            return;
        }

        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    Log.e(TAG, "run: reading data from internet");
                    readRatesFromInternet();
                    Log.e(TAG, "run: finished reading");
                } catch (Exception e) {
                    Log.e(TAG, "problem: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };

        thread.start();

    }

    Map<String, Float> currentRate = new HashMap<String, Float>();

    public String getJSON(String url, int timeout) {
        HttpsURLConnection c = null;
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                .permitAll().build();
        StrictMode.setThreadPolicy(policy);
        try {
            URL u = new URL(url);
            c = (HttpsURLConnection) u.openConnection();
            c.setRequestMethod("GET");
            c.setRequestProperty("Content-length", "0");
            c.setUseCaches(false);
            c.setAllowUserInteraction(false);
            c.setConnectTimeout(timeout);
            c.setReadTimeout(timeout);
            c.connect();
            int status = c.getResponseCode();

            switch (status) {
                case 200:
                case 201:
                    BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        sb.append(line+"\n");
                    }
                    br.close();
                    return sb.toString();
            }

        } catch (MalformedURLException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (c != null) {
                try {
                    c.disconnect();
                } catch (Exception ex) {
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return null;
    }

    static String TAG = "ExchangeRates";
    public static boolean alreadyRead = false;
    String rates = "";
    public SharedPreferences prefs;

    public void readRatesFromInternet() {
        if (!alreadyRead) {
            rates = getJSON("https://openexchangerates.org/api/latest.json?app_id=c8275a08520641bbb78f53fea64c2a49", 5000);
            if (rates == null) {
                Log.e(TAG, "readRatesFromInternet: i understand that there's no data");
                Log.e(TAG, "readRatesFromInternet: getting previous data, if possible");
                rates = prefs.getString("rates", "");
            } else {
                alreadyRead = true;
                rates = rates.substring(rates.indexOf(": {") + 3);
                rates = rates.substring(0, rates.indexOf("}"));
                SharedPreferences.Editor edit = prefs.edit();
                edit.putString("rates", rates);
                edit.putString("LastExchangeRates", dateRef);
                edit.commit();
            }
        } else {
            Log.e(TAG, "readRatesFromInternet: using previous data");
            //return;
        }
        //AuthMsg msg = new Gson().fromJson(rates, AuthMsg.class);
        String[] g = rates.split(",");
        Log.e(TAG, "readRatesFromInternet:");

        for (int i = 0; i < g.length; i++) {
            g[i] = g[i].trim();
            Log.e(TAG, "--" + g[i] + "--");
            if (g[i].length() >= 7)
            if ("0123456789".contains(g[i].substring(7, 8))) {
                try {
                    currentRate.put(g[i].substring(1,4),Float.parseFloat(g[i].substring(7)));
                } catch (Exception s) {

                }
            }
        }
        Log.e(TAG, "readRatesFromInternet: parse complete");
        for (Map.Entry<String, Float> entry : currentRate.entrySet()) {
            Log.e(TAG, "readRatesFromInternet: currency: -" + entry.getKey() + "- rate: " + entry.getValue() );
        }
        Log.e(TAG, "readRatesFromInternet: KZT: " + currentRate.get("KZT"));
        Log.e(TAG, "readRatesFromInternet: USD: " + currentRate.get("USD"));
        Log.e(TAG, "readRatesFromInternet: RUB: " + currentRate.get("RUB"));
    }

    public float getRate(String currency) {
        if (currency.equals(""))
            currency = GIIApplication.gii.properties.defaultCurrency;
        if (currentRate.get(currency) == null)
            return 1;
        return currentRate.get(currency);
    }

    final String[] currencyArray() {
        return GIIApplication.gii.properties.currency.replace(" ", ",").split(",");
    }

    public float convert(float convertAmount, String fromCurrency, String toCurrency) {
        if (toCurrency == null || toCurrency.equals(""))
            return convertAmount;
        float rate1 = currentRate.get(fromCurrency);
        float rate2 = currentRate.get(toCurrency);
        if (rate1 != 0) {
            return convertAmount * rate2 / rate1;
        }
        return 0;
    }
}
