package com.gii.maxflow;

import android.app.Application;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.batch.android.Batch;
import com.batch.android.Config;
import com.firebase.client.Firebase;

import java.io.ByteArrayOutputStream;

/**
 * Created by Timur on 24-Jan-16.
 */
public class GIIApplication extends Application  {
    static GII gii;
    public static String widgetText = "";

    @Override
    public void onCreate() {
        super.onCreate();
        Firebase.setAndroidContext(this);
        Firebase.getDefaultConfig().setPersistenceEnabled(true);

        //Batch.Push.setGCMSenderId("696759372546");
        Batch.Push.setGCMSenderId("696759372546");
        //Batch.Push.setGCMSenderId("276787617041");

        //
        //Batch.setConfig(new Config("DEV570225D7B338472CD5D48455441"));
        //Batch.setConfig(new Config("570225D7B1ED30DAE43639B9922AC7"));
        //Batch.setConfig(new Config("DEV58188E798F39FD4C35861D25991")); //<- new dev
        Batch.setConfig(new Config("58188E798EFC85FFADAD506D7A9149"));

    }

    public static String encodeToBase64(Bitmap image, Bitmap.CompressFormat compressFormat, int quality)
    {
        ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
        image.compress(compressFormat, quality, byteArrayOS);
        return Base64.encodeToString(byteArrayOS.toByteArray(), Base64.DEFAULT);
    }

    public static Bitmap decodeBase64(String input)
    {
        byte[] decodedBytes = Base64.decode(input, 0);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

}
