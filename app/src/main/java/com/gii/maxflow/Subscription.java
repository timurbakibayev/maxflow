package com.gii.maxflow;

import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by Timur on 03-Feb-16.
 */
public class Subscription {
    public Date getEndingDate() {
        return endingDate;
    }

    public String getUserId() {
        return userId;
    }

    public String getSignature() {
        return signature;
    }

    Date endingDate;
    String userId = "";
    String signature = "";
    public Subscription() {

    }

    public boolean validate() {
        return (hash().equals(signature));
    };

    public void sign() {
        signature = hash();
    }

    public String hash() {
        Calendar c = Calendar.getInstance();
        c.setTime(endingDate);
        String dom = c.get(Calendar.DAY_OF_MONTH) + "";
        if (dom.length()<2)
            dom = "0" + dom;
        String month = c.get(Calendar.MONTH) + "";
        if (month.length()<2)
            month = "0" + month;
        int checkSum = 0;
        for (int i = 0; i < userId.length(); i++) {
            try {
                int k = Integer.parseInt(userId.substring(i, i + 1));
                checkSum += k;
            } catch (NumberFormatException e) {

            } catch (ArrayIndexOutOfBoundsException e) {

            }
        }
        String result = dom + "-" + month + "-" + c.get(Calendar.YEAR) + userId + (c.get(Calendar.MONTH)*c.get(Calendar.YEAR)+c.get(Calendar.DAY_OF_MONTH) + checkSum);

        Log.w("Subscription","code: " + result);
        Log.w("Subscription","try decode: " + unHash(result));
        return (result);
    }

    public static Date unHash(String hased) {
        Calendar c = Calendar.getInstance();
        try {
            c.set(Calendar.DAY_OF_MONTH, Integer.parseInt(hased.substring(0, 2)));
            c.set(Calendar.MONTH, Integer.parseInt(hased.substring(3, 5)));
            c.set(Calendar.YEAR, Integer.parseInt(hased.substring(6, 10)));
        } catch (NumberFormatException e) {
            c.set(Calendar.DAY_OF_MONTH, 01);
            c.set(Calendar.MONTH, 01);
            c.set(Calendar.YEAR, 2017);
        }

        Log.w("Subscription",hased);
        Log.w("Subscription","day" + c.get(Calendar.DAY_OF_MONTH));
        Log.w("Subscription","month" + c.get(Calendar.MONTH));
        Log.w("Subscription","year" + c.get(Calendar.YEAR));
        return c.getTime();
    }

    public String makeSHA1Hash(String input)
            throws NoSuchAlgorithmException, UnsupportedEncodingException
    {
        MessageDigest md = MessageDigest.getInstance("SHA1");
        md.reset();
        byte[] buffer = input.getBytes("UTF-8");
        md.update(buffer);
        byte[] digest = md.digest();

        String hexStr = "";
        for (int i = 0; i < digest.length; i++) {
            hexStr +=  Integer.toString( ( digest[i] & 0xff ) + 0x100, 16).substring( 1 );
        }
        return hexStr;
    }
}
