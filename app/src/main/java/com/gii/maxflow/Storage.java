package com.gii.maxflow;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.PointF;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import org.xmlpull.v1.XmlSerializer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.channels.FileChannel;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Created by Timur on 04-Sep-15.
 * A class for working with storage, in future with databases
 */
public class Storage {

    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US);
    SimpleDateFormat sdfNice = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
    GII gii;

    public Storage(GII gii) {
        this.gii = gii;
    }

    boolean permisionRequested = false;
    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (gii.activity.checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                //Log.v(TAG,"Permission is granted");
                return true;
            } else {
                //Log.v(TAG,"Permission is revoked");
                if (!permisionRequested) {
                    ActivityCompat.requestPermissions(gii.activity, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                    permisionRequested = true;
                }
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            //Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    public void sendFile(Properties properties) {
        Intent intentShareFile = new Intent(Intent.ACTION_SEND);
        //File fileWithinMyDir = new File(myFilePath);

        GIIApplication.gii.storage.saveNiceXML(GIIApplication.gii.properties,GIIApplication.gii.circle,GIIApplication.gii.operations);

        String myFilePath = xmlDirectory(properties,"nice") + "/export_" + properties.fileName;
        File fileWithinMyDir = new File(myFilePath);

        if(fileWithinMyDir.exists()) {
            intentShareFile.setType("application/pdf");
            intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+myFilePath));
            intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                    "Anyway Wallet: ".concat(properties.fileName));
            intentShareFile.putExtra(Intent.EXTRA_TEXT, gii.getContext().getString(R.string.emailBodyXML));
            gii.getContext().startActivity(Intent.createChooser(intentShareFile, "Share File"));
        }
    }

    public java.util.ArrayList<String> fileList(String extension, Properties properties) {
        java.util.ArrayList<String> forout = new java.util.ArrayList<>();
        File dir = new File(xmlDirectory(properties,""));
        if (dir.exists()) {
            File[] file = dir.listFiles();
            for (int i = 0; i < file.length; i++) {
                String k = file[i].getName();
                if (k.length() >= extension.length())
                    if (k.substring(k.length()-extension.length(),k.length()).equals(extension))
                        forout.add(file[i].getName());
            }
        }
        return(forout);
    }

    public java.util.ArrayList<String> fileListOffline(String extension, Properties properties) {
        java.util.ArrayList<String> forout = new java.util.ArrayList<>();
        String level1 = Environment.getExternalStorageDirectory().toString();
        File dir = new File(level1);
        createDir(dir);
        level1 = level1.concat( "/" + "MaxFlow");
        dir = new File(level1);
        createDir(dir);
        String userDir = "";
        dir = new File(level1 + userDir);
        if (dir.exists()) {
            File[] file = dir.listFiles();
            if (file != null)
                for (int i = 0; i < file.length; i++) {
                    String k = file[i].getName();
                    if (k.length() >= extension.length())
                        if (k.substring(k.length()-extension.length(),k.length()).equals(extension))
                            forout.add(file[i].getName());
                }
        }
        return(forout);
    }

    String lastPathToFile = "";
    public void loadFile(final Properties properties, final ArrayList<Circle> circle, final ArrayList<Operation> operation) {
        Log.w("RecalculateAll","loading file...");
        if (gii.ref.getAuth() != null) {
            String pathToFile = "maxflow/" + GII.ref.getAuth().getUid() + "/" + properties.computeFileNameWithoutXML();
            if (!properties.owner.equals(""))
                pathToFile = "maxflow/" + properties.owner + "/" + properties.computeFileNameWithoutXML();
            if (!pathToFile.equals(lastPathToFile)) {
                GII.ref.child(pathToFile + "/circles/").
                        addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    Circle post = postSnapshot.getValue(Circle.class);
                                    post.setSyncedWithCloud(true);
                                    boolean added = false;
                                    for (int i = 0; i < circle.size(); i++) {
                                        Circle _circle = circle.get(i);
                                        if (_circle.id.equals(post.id)) {
                                            if (!post.lastChangeId.equals(gii.prefs.getString("AndroidID", ""))) {
                                                circle.set(i, post);
                                                GII.needToRecalculate = true;
                                                Log.w("RecalculateAll", "initiate Storage 157");
                                            }
                                            added = true;
                                        }
                                    }
                                    if (!added) {
                                        circle.add(post);
                                        GII.needToRecalculate = true;
                                        Log.w("RecalculateAll", "initiate Storage 165");

                                    }
                                    //Log.e("Firebase change:", "circle " + post.name);
                                }
                                Log.w("Firebase change:", "got some changes (may be not)");
                                if (circle.size() == 0 &&
                                        properties.fileName.equals("default.xml"))
                                    fromTemplate(circle);
                                GIIApplication.gii.loaded++;
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                Log.w("Firebase", "The read failed 4: " + firebaseError.getMessage());
                            }
                        });
                GII.ref.child(pathToFile + "/operations/").
                        addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    Operation post = postSnapshot.getValue(Operation.class);
                                    post.setSyncedWithCloud(true);
                                    boolean added = false;
                                    for (int i = 0; i < operation.size(); i++) {
                                        Operation _operation = operation.get(i);
                                        if (_operation.id.equals(post.id)) {
                                            operation.set(i, post);
                                            added = true;
                                        }
                                    }
                                    if (!added)
                                        operation.add(post);
                                    //Log.e("Firebase change:", "operation " + post.amount);
                                    GII.needToRecalculate = true;
                                    //Log.e("RecalculateAll","initiate Storage 191");
                                }
                                GIIApplication.gii.loaded++;
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                Log.w("Firebase", "The read failed 5: " + firebaseError.getMessage());
                            }
                        });
                GII.ref.child(pathToFile + "/properties/").
                        addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                int i = 0;
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    Properties post = postSnapshot.getValue(Properties.class);
                                    if (!post.lastChangeId.equals(gii.prefs.getString("AndroidID", "")) ||
                                            !properties.loaded) { //|| true
                                        properties.set(post);
                                        Log.w("Storage.java", "onDataChange: got new properties, because " +
                                                !post.lastChangeId.equals(gii.prefs.getString("AndroidID", "")) + " || " +
                                                !properties.loaded);
                                        if (properties.loaded) {
                                            GII.needToRecalculate = true;
                                            Log.w("RecalculateAll", "initiate Storage 212");
                                        }
                                        properties.loaded = true;
                                    }
                                    i++;
                                }
                                if (i == 0) {
                                    properties.loaded = true;
                                }
                                GIIApplication.gii.loaded++;
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                Log.e("Firebase", "The read failed 6: " + firebaseError.getMessage());
                            }
                        });
                MainActivity.subscription = new Subscription();
                GII.ref.child("subscriptions/" + GII.ref.getAuth().getUid() + "/").
                        addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    Subscription post = postSnapshot.getValue(Subscription.class);
                                    if (MainActivity.subscription.endingDate == null ||
                                            post.endingDate.after(MainActivity.subscription.endingDate)) {
                                        MainActivity.subscription = post;
                                    }
                                }
                                GIIApplication.gii.loaded++;
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                Log.e("Firebase", "The read failed 7: " + firebaseError.getMessage());
                            }
                        });
                lastPathToFile = pathToFile;
            }
        } else {
            try {
                loadXML(properties, circle, operation);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            finally {
                GIIApplication.gii.loaded = 4;
            }
        }
        saveLastFile(properties);
    }

    private static final String TAG = "Storage.java";
    public void saveFile(Properties properties, java.util.ArrayList<Circle> circle, java.util.ArrayList<Operation> operation) {
        Log.w(TAG, "saveFile: started");
        if (gii.ref.getAuth() != null) {
            String pathToFile = "maxflow/" + GII.ref.getAuth().getUid() + "/" + properties.computeFileNameWithoutXML();
            if (!properties.owner.equals(""))
                pathToFile = "maxflow/" + properties.owner + "/" + properties.computeFileNameWithoutXML();

            for (Circle _circle: circle) {
                if (!_circle.syncedWithCloud) {
                    _circle.setSyncedWithCloud(true);
                    _circle.setLastChangeId(gii.prefs.getString("AndroidID",""));
                    GII.ref.child(pathToFile + "/circles/" + _circle.id).
                            setValue(_circle);
                }
            }

            for (Operation _operation: operation) {
                if (!_operation.syncedWithCloud) {
                    //TODO: check this part! xxxxx
                    _operation.setSyncedWithCloud(true);
                    GII.ref.child(pathToFile + "/operations/" + _operation.id).
                            setValue(_operation);
                }
            }

            Log.w(TAG, "saveFile: properties = null?:" + (properties == null));
            Log.w(TAG, "saveFile: properties loaded?:" + (properties.loaded));
            if (properties.loaded) {
                properties.lastChangeId = gii.prefs.getString("AndroidID", "");
                GII.ref.child(pathToFile + "/properties/0").
                        setValue(properties);
            }
        } else
            saveXML(properties, circle, operation);
        Log.w(TAG, "saveFile: complete!");
    }

    public void createDir(File dir) {
        if (!dir.exists()) {
            try {
                dir.mkdirs();
            }
            catch (IllegalStateException e) {  Log.w("storage",e.getMessage()) ;  }
        }
    }

    public String xmlDirectory(Properties properties,String prefix) {
        String level1 = Environment.getExternalStorageDirectory().toString();

        File dir = new File(level1);
        createDir(dir);
        level1 = level1.concat( "/" + "MaxFlow");
        dir = new File(level1);
        createDir(dir);
        if (!prefix.equals("")) {
            level1 = level1.concat( "/" + prefix);
            dir = new File(level1);
            createDir(dir);
        }


        String userDir = "";
        if (gii.ref.getAuth() != null) {
            userDir = "/" + gii.ref.getAuth().getProviderData().get("email").toString();
            if (!properties.owner.equals(""))
                userDir = "/" + properties.owner;
        }
        return (level1 + userDir);
    }

    public void saveNiceXML(Properties properties, java.util.ArrayList<Circle> circle, java.util.ArrayList<Operation> operation) {
        try {
            File dir = new File(xmlDirectory(properties,"nice"));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(xmlDirectory(properties,"nice") + "/export_" + properties.fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            else {
                file.delete();
                file.createNewFile();
            }

            FileOutputStream fileos = new FileOutputStream (file);

            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);

            xmlSerializer.startTag("", "Transactions");
            for (int i = 0; i < operation.size(); i++){
                Operation oper = operation.get(i);
                if (!oper.deleted) {
                    xmlSerializer.startTag("", "operation");
                    xmlSerializer.attribute("", "FROM", GIIApplication.gii.circleById(oper.fromCircle, circle).name);
                    xmlSerializer.attribute("", "TO", GIIApplication.gii.circleById(oper.toCircle, circle).name);
                    xmlSerializer.attribute("", "AMOUNT", Float.toString(oper.amount));
                    xmlSerializer.attribute("", "CURRENCY", oper.currency);
                    xmlSerializer.attribute("", "DATE", sdfNice.format(oper.date));
                    xmlSerializer.attribute("", "DATEWORDS", GII.dateText(oper.date));
                    if (oper.description != null)
                        xmlSerializer.attribute("", "DESCRIPTION", oper.description);

                    xmlSerializer.endTag("", "operation");
                }
            }
            xmlSerializer.endTag("","Transactions");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            String dataWrite = writer.toString();
            fileos.write(dataWrite.getBytes());
            fileos.close();
        }
        catch (FileNotFoundException e) {   Log.e("storage",e.getMessage());     }    catch (IllegalArgumentException e) {    Log.e("storage",e.getMessage()) ;  }    catch (IllegalStateException e) {  Log.e("storage",e.getMessage()) ;  }    catch (IOException e) { Log.e("storage",e.getMessage()); }

    }

    public SharedPreferences prefs;

    public void copy(File src, File dst, Properties properties) throws IOException {

        Calendar c = Calendar.getInstance();
        String dateRef = c.get(Calendar.DAY_OF_MONTH) + "." + c.get(Calendar.MONTH) + "." + c.get(Calendar.YEAR);
        Log.w(TAG, "copy: dateref" + dateRef);
        prefs = PreferenceManager.getDefaultSharedPreferences(GIIApplication.gii.activity);
        if (prefs.getString(properties.fileName + "/Backup","").equals(dateRef)) {
            Log.w(TAG, "Backup not needed");
            return;
        }
        SharedPreferences.Editor edit = prefs.edit();
        edit.putString(properties.fileName + "/Backup", dateRef);
        edit.commit();

        if (dst.exists())
            dst.delete();

        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    public void saveXML(Properties properties, java.util.ArrayList<Circle> circle, java.util.ArrayList<Operation> operation) {

        if (!isStoragePermissionGranted()) {

            return;
        }

        try {
            File dir = new File(xmlDirectory(properties,""));
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(xmlDirectory(properties,"") + "/" + properties.fileName);
            if (!file.exists()) {
                file.createNewFile();
            }
            else {
                //TODO: SAVE A COPY FIRST!
                File fileBackup = new File(xmlDirectory(properties,"") + "/" + properties.fileName + ".bak");
                try {
                    copy(file, fileBackup, properties);
                    file.delete();
                    file.createNewFile();
                } catch (Exception e) {
                    GIIApplication.gii.activity.showMessage("Problems with backup. Not saved!");
                    return;
                }
            }

            FileOutputStream fileos = new FileOutputStream (file);

            XmlSerializer xmlSerializer = Xml.newSerializer();
            StringWriter writer = new StringWriter();
            xmlSerializer.setOutput(writer);
            xmlSerializer.startDocument("UTF-8", true);

            xmlSerializer.startTag("", "GRAPH");
            xmlSerializer.startTag(null, "circles");
            for (int i = 0; i < circle.size(); i++){
                Circle acc = circle.get(i);
                xmlSerializer.startTag("", "circle");
                xmlSerializer.attribute("", "ID", acc.id);
                xmlSerializer.attribute("","PARENT",acc.parentId);
                xmlSerializer.attribute("", "NAME", acc.name);
                xmlSerializer.attribute("", "ISMYMONEY", booleanToString(acc.myMoney));
                xmlSerializer.attribute("", "ISGOAL", booleanToString(acc.limitGoal));
                xmlSerializer.attribute("","COORDINATES",pointfToString(acc.coordinates));
                xmlSerializer.attribute("","RADIUS",Integer.toString((int) acc.radius));
                xmlSerializer.attribute("","PICTURE",Integer.toString(acc.picture));
                xmlSerializer.attribute("","GOAL",gii.df.format(acc.goalAmount));
                xmlSerializer.attribute("","COLOR",Integer.toString((int) acc.color));
                xmlSerializer.attribute("","DELETED",booleanToString(acc.deleted));
                xmlSerializer.attribute("","SYNCEDCLOUD",booleanToString(acc.syncedWithCloud));
                xmlSerializer.attribute("","SENTCLOUD",booleanToString(acc.sentToCloud));
                xmlSerializer.attribute("","CLOUDID",acc.cloudId);
                xmlSerializer.endTag("","circle");
            }
            xmlSerializer.endTag(null, "circles");


            xmlSerializer.startTag(null, "operations");
            for (int i = 0; i < operation.size(); i++){
                Operation oper = operation.get(i);
                xmlSerializer.startTag("", "operation");
                xmlSerializer.attribute("", "ID", oper.id);
                xmlSerializer.attribute("", "AMOUNT", Float.toString(oper.amount));
                xmlSerializer.attribute("", "CURRENCY", oper.currency);
                xmlSerializer.attribute("", "RATE", Float.toString(oper.exchangeRate));
                xmlSerializer.attribute("","FROM",oper.fromCircle);
                xmlSerializer.attribute("","TO",oper.toCircle);
                xmlSerializer.attribute("","TRANSACTION",Integer.toString(oper.transactionId));
                xmlSerializer.attribute("","PAGENO",Integer.toString(oper.pageNo));
                xmlSerializer.attribute("","DATE",sdf.format(oper.date));
                xmlSerializer.attribute("","SYNCEDCLOUD",booleanToString(oper.syncedWithCloud));
                xmlSerializer.attribute("","DELETED",booleanToString(oper.deleted));
                //xmlSerializer.attribute("","SENTCLOUD",booleanToString(oper.sentToCloud));
                xmlSerializer.attribute("","CLOUDID",oper.cloudId);
                if (oper.description != null)
                    xmlSerializer.attribute("","DESCRIPTION", oper.description);
                xmlSerializer.endTag("","operation");
            }
            xmlSerializer.endTag(null, "operations");

            xmlSerializer.startTag(null, "properties");
            xmlSerializer.startTag("", "property");
            xmlSerializer.attribute("", "ZEROMONTH", Integer.toString(properties.zeroMonth));
            xmlSerializer.attribute("", "CURRENTMONTH", Integer.toString(properties.currentPageNo));
            xmlSerializer.attribute("", "SCALEFACTOR", Float.toString(properties.scaleFactor));
            xmlSerializer.attribute("", "BACKGROUNDX", Float.toString(properties.backgroundPosition.x));
            xmlSerializer.attribute("", "BACKGROUNDY", Float.toString(properties.backgroundPosition.y));
            xmlSerializer.attribute("", "CIRCLEUPDATE", sdf.format(properties.lastCloudUpdateCircle) );
            xmlSerializer.attribute("", "FILTERDATEFROM", sdf.format(properties.filterFrom) );
            xmlSerializer.attribute("", "FILTERDATETO", sdf.format(properties.filterTo) );
            xmlSerializer.attribute("", "FILTERTEXT", properties.filterText);
            xmlSerializer.attribute("", "CURRENCY", properties.currency);
            xmlSerializer.attribute("", "DEFCURRENCY", properties.defaultCurrency);
            xmlSerializer.attribute("", "FILTERED", booleanToString(properties.filtered));
            xmlSerializer.attribute("", "OPERATIONUPDATE", sdf.format(properties.lastCloudUpdateOperation) );
            xmlSerializer.endTag("", "property");
            xmlSerializer.endTag(null, "properties");

            /*
                            if (xpp.getName().equals("properties")) {
                    if (xpp.getAttributeValue("", "BACKGROUNDX") != null)
                        backgroundPosition = new PointF(myParseFloat(xpp.getAttributeValue("", "BACKGROUNDX")),
                                myParseFloat(xpp.getAttributeValue("", "BACKGROUNDY")));

                    if (xpp.getAttributeValue("", "SCALEFACTOR") != null)
                        scaleFactor = myParseFloat(xpp.getAttributeValue("", "SCALEFACTOR"));

                    if (xpp.getAttributeValue("", "ZEROMONTH") != null)
                        zeroMonth = Integer.parseInt(xpp.getAttributeValue("", "ZEROMONTH"));
                }

             */

            xmlSerializer.endTag("","GRAPH");
            xmlSerializer.endDocument();
            xmlSerializer.flush();
            String dataWrite = writer.toString();
            fileos.write(dataWrite.getBytes());
            fileos.close();
        }
        catch (FileNotFoundException e) {   Log.e("storage",e.getMessage());     }    catch (IllegalArgumentException e) {    Log.e("storage",e.getMessage()) ;  }    catch (IllegalStateException e) {  Log.e("storage",e.getMessage()) ;  }    catch (IOException e) { Log.e("storage",e.getMessage()); }
    }

    private String booleanToString(boolean b) {
        if (b)
            return "1";
        return "0";
    }

    private boolean stringToBoolean(String s) {
        if (s != null)
            return (s.equals("1"));
        return (false);
    }

    public String pointfToString(PointF inPoint) {
        String cx = Integer.toString(Math.round(inPoint.x));
        String cy = Integer.toString(Math.round(inPoint.y));
        return (cx + "," + cy);
    }

    public PointF stringToPointF(String inString) {
        String cx = inString.substring(0, inString.indexOf(","));
        String cy = inString.substring(inString.indexOf(",") + 1, inString.length());
        return (new PointF(myParseFloat(cx),myParseFloat(cy)));
    }

    public float myParseFloat(String s) {
        return Float.parseFloat(s.replace(",","."));
    }

    public void loadXML(Properties properties, ArrayList<Circle> circle, ArrayList<Operation> operation) throws ParseException {
        String data = "";
        if (properties.zeroMonth == 0) {
            Calendar c = Calendar.getInstance();
            properties.zeroMonth = c.get(Calendar.MONTH) + c.get(Calendar.YEAR)*12;
        }
        try {
            String level1 = Environment.getExternalStorageDirectory().toString();
            File dir = new File(level1);
            createDir(dir);
            level1 = level1.concat( "/" + "MaxFlow");
            dir = new File(level1);
            createDir(dir);
            String userDir = "";
            dir = new File(level1 + userDir);
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(level1 + userDir + "/" + properties.fileName);
            if (!file.exists()) {
                if (properties.fileName.equals("default.xml"))
                    fromTemplate(circle);
                return;
            }
            FileInputStream fileos = new FileInputStream(file);

            InputStreamReader isr = new InputStreamReader(fileos);

            char[] inputBuffer = new char[300]; //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
            int charRead;
            StringBuilder stringBuilder = new StringBuilder();
            while ((charRead = isr.read(inputBuffer))>0) {
                //---convert the chars to a String---
                String readString =
                        String.copyValueOf(inputBuffer, 0,
                                charRead);
                stringBuilder.append(readString);
            }
            data = stringBuilder.toString();

            isr.close();
            fileos.close();

        }
        catch (FileNotFoundException e) { return; }    catch (IllegalArgumentException e) { return; }    catch (IllegalStateException e) { return;}    catch (IOException e) {return;}

        ArrayList<String> userData = new ArrayList<String>();

        XmlPullParserFactory factory = null;
        try {
            factory = XmlPullParserFactory.newInstance();
        }catch (XmlPullParserException e2) {
            return;
        }
        factory.setNamespaceAware(true);
        XmlPullParser xpp = null;
        try {
            xpp = factory.newPullParser();
        }
        catch (XmlPullParserException e2) {
            return;
        }
        try {
            xpp.setInput(new StringReader(data));
        }
        catch (XmlPullParserException e1) {
            return;
        }
        int eventType = 0;
        try {
            eventType = xpp.getEventType();
        }
        catch (XmlPullParserException e1) {

            e1.printStackTrace();
        }
        properties.scaleFactor = 1;
        properties.backgroundPosition = new PointF(0,0);

        properties.lastCloudUpdateCircle = new Date(0);
        properties.lastCloudUpdateOperation = new Date(0);
        try {
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    if (xpp.getName().equals("circle")) {
                        String id = xpp.getAttributeValue("", "ID");
                        String name = xpp.getAttributeValue("", "NAME");
                        PointF newpoint = stringToPointF(xpp.getAttributeValue("", "COORDINATES"));
                        float newradius = myParseFloat(xpp.getAttributeValue("", "RADIUS"));
                        boolean deleted = stringToBoolean(xpp.getAttributeValue("", "DELETED"));
                        boolean syncedCloud = false;
                        boolean sentCloud = false;
                        String cloudId = "";
                        if (xpp.getAttributeValue("", "SYNCEDCLOUD") != null)
                            syncedCloud = stringToBoolean(xpp.getAttributeValue("", "SYNCEDCLOUD"));
                        if (xpp.getAttributeValue("", "SENTCLOUD") != null)
                            sentCloud = stringToBoolean(xpp.getAttributeValue("", "SENTCLOUD"));
                        if (xpp.getAttributeValue("", "CLOUDID") != null)
                            cloudId = xpp.getAttributeValue("", "CLOUDID");
                        int picture = 0;
                        if (xpp.getAttributeValue("", "PICTURE") != null)
                            picture = Integer.parseInt(xpp.getAttributeValue("", "PICTURE"));
                        int color = 0;
                        if (xpp.getAttributeValue("", "COLOR") != null &&
                                !xpp.getAttributeValue("", "COLOR").equals("MAGENTA"))
                            color = Integer.parseInt(xpp.getAttributeValue("", "COLOR"));
                        String parentId = "";
                        if (xpp.getAttributeValue("", "PARENT") != null)
                            parentId = xpp.getAttributeValue("", "PARENT");
                        boolean isMyMoney = false;
                        if (xpp.getAttributeValue("", "ISMYMONEY") != null)
                            isMyMoney = stringToBoolean(xpp.getAttributeValue("", "ISMYMONEY"));
                        boolean goal = false;
                        if (xpp.getAttributeValue("", "ISGOAL") != null)
                            goal = stringToBoolean(xpp.getAttributeValue("", "ISGOAL"));
                        float goalAmount = 0;
                        if (xpp.getAttributeValue("", "GOAL") != null)
                            goalAmount = myParseFloat(xpp.getAttributeValue("", "GOAL"));
                        circle.add(new Circle(id, parentId, newpoint, newradius, name, deleted, picture, color, sentCloud, syncedCloud, cloudId, isMyMoney, goal, goalAmount));
                    }
                    if (xpp.getName().equals("operation")) {
                        String id = xpp.getAttributeValue("", "ID");
                        int transactionId = Integer.parseInt(xpp.getAttributeValue("", "TRANSACTION"));
                        String fromCircle = xpp.getAttributeValue("", "FROM");
                        String toCircle = xpp.getAttributeValue("", "TO");
                        float amount = myParseFloat(xpp.getAttributeValue("", "AMOUNT"));
                        int pageNo = 0;
                        if (xpp.getAttributeValue("", "PAGENO") != null)
                            pageNo = Integer.parseInt(xpp.getAttributeValue("", "PAGENO"));
                        boolean syncedCloud = false;
                        boolean sentCloud = false;
                        boolean deleted = false;
                        String cloudId = "";
                        if (xpp.getAttributeValue("", "SYNCEDCLOUD") != null)
                            syncedCloud = stringToBoolean(xpp.getAttributeValue("", "SYNCEDCLOUD"));
                        if (xpp.getAttributeValue("", "SENTCLOUD") != null)
                            sentCloud = stringToBoolean(xpp.getAttributeValue("", "SENTCLOUD"));
                        if (xpp.getAttributeValue("", "DELETED") != null)
                            deleted = stringToBoolean(xpp.getAttributeValue("", "DELETED"));
                        if (xpp.getAttributeValue("", "CLOUDID") != null)
                            cloudId = xpp.getAttributeValue("", "CLOUDID");
                        Date operDate = new Date(0);
                        if (xpp.getAttributeValue("", "DATE") != null)
                            operDate = sdf.parse(xpp.getAttributeValue("", "DATE"));
                        String description = "";
                        if (xpp.getAttributeValue("", "DESCRIPTION") != null)
                            description = xpp.getAttributeValue("", "DESCRIPTION");
                        String currency = "";
                        if (xpp.getAttributeValue("", "CURRENCY") != null)
                            currency = xpp.getAttributeValue("", "CURRENCY");
                        float exchangeRate = 0;
                        if (xpp.getAttributeValue("", "RATE") != null)
                            exchangeRate = myParseFloat(xpp.getAttributeValue("", "RATE"));
                        operation.add(new Operation(id, fromCircle, toCircle, amount, currency, exchangeRate, operDate, transactionId, pageNo, sentCloud, syncedCloud, cloudId, description, deleted));
                    }
                    if (xpp.getName().equals("property")) {
                        if (xpp.getAttributeValue("", "BACKGROUNDX") != null)
                            properties.backgroundPosition = new PointF(myParseFloat(xpp.getAttributeValue("", "BACKGROUNDX")),
                                    myParseFloat(xpp.getAttributeValue("", "BACKGROUNDY")));

                        if (xpp.getAttributeValue("", "SCALEFACTOR") != null)
                            properties.scaleFactor = myParseFloat(xpp.getAttributeValue("", "SCALEFACTOR"));

                        if (xpp.getAttributeValue("", "ZEROMONTH") != null)
                            properties.zeroMonth = Integer.parseInt(xpp.getAttributeValue("", "ZEROMONTH"));

                        if (xpp.getAttributeValue("", "CURRENTMONTH") != null)
                            properties.currentPageNo = Integer.parseInt(xpp.getAttributeValue("", "CURRENTMONTH"));

                        if (xpp.getAttributeValue("", "CIRCLEUPDATE") != null)
                            properties.lastCloudUpdateCircle = sdf.parse(xpp.getAttributeValue("", "CIRCLEUPDATE"));

                        if (xpp.getAttributeValue("", "OPERATIONUPDATE") != null)
                            properties.lastCloudUpdateOperation = sdf.parse(xpp.getAttributeValue("", "OPERATIONUPDATE"));

                        if (xpp.getAttributeValue("", "FILTERDATEFROM") != null)
                            properties.filterFrom = sdf.parse(xpp.getAttributeValue("", "FILTERDATEFROM"));
                        if (xpp.getAttributeValue("", "FILTERDATETO") != null)
                            properties.filterTo = sdf.parse(xpp.getAttributeValue("", "FILTERDATETO"));
                        if (xpp.getAttributeValue("", "FILTERED") != null)
                            properties.filtered = stringToBoolean(xpp.getAttributeValue("", "FILTERED"));
                        if (xpp.getAttributeValue("", "FILTERTEXT") != null)
                            properties.filterText = xpp.getAttributeValue("", "FILTERTEXT");
                        if (xpp.getAttributeValue("", "CURRENCY") != null)
                            properties.currency = xpp.getAttributeValue("", "CURRENCY");
                        if (xpp.getAttributeValue("", "DEFCURRENCY") != null)
                            properties.defaultCurrency = xpp.getAttributeValue("", "DEFCURRENCY");
                    }

                    //System.out.println("Start tag "+xpp.getName());
                }
                try {
                    eventType = xpp.next();
                } catch (XmlPullParserException e) {
                    int u = 0;
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        catch (Exception e) {
            GIIApplication.gii.activity.showMessage("Failed to open file: \n" + e.getMessage());
        }

        if (circle.size() == 0)
            if (properties.fileName.equals("default.xml"))
                fromTemplate(circle);
    }

    public void fromTemplate(ArrayList<Circle> circle) {

        String[] id = {"cash","card","job","food","house","entertainment","sport","cinema","music"};
        String[] name = {
                GIIApplication.gii.getContext().getString(R.string.circle_cash),
                GIIApplication.gii.getContext().getString(R.string.circle_card),
                GIIApplication.gii.getContext().getString(R.string.circle_job),
                GIIApplication.gii.getContext().getString(R.string.circle_food),
                GIIApplication.gii.getContext().getString(R.string.circle_house),
                GIIApplication.gii.getContext().getString(R.string.circle_entertainment),
                GIIApplication.gii.getContext().getString(R.string.circle_sprot),
                GIIApplication.gii.getContext().getString(R.string.circle_cinema),
                GIIApplication.gii.getContext().getString(R.string.circle_music)
        };
        PointF[] newpoint = {
                new PointF(160,-270),
                new PointF(-100,-270),
                new PointF(40,-530),
                new PointF(30,80),
                new PointF(-240,-20),
                new PointF(320,-40),
                new PointF(425,250),
                new PointF(530,-190),
                new PointF(595,70)
        };
        float newradius = 120;
        boolean deleted = false;
        boolean syncedCloud = false;
        boolean sentCloud = false;
        String cloudId = "";
        int[] picture = {7,15,57,53,26,59,22,39,45};
        int[] color = {29,14,19,41,11,8,46,27,17};
        String[] parentId = {"","","","","","","entertainment","entertainment","entertainment"};
        boolean[] isMyMoney = {true,true,false,false,false,false,false,false,false};
        float goalAmount = 0;

        for (int i = 0; i < id.length; i++)
            circle.add(new Circle(id[i],parentId[i],newpoint[i],newradius,name[i],
                    deleted,picture[i],color[i],sentCloud,syncedCloud,cloudId,
                    isMyMoney[i],false,goalAmount));

    }

    public void saveLastFile(Properties properties) {
        if (!properties.owner.equals(""))
            return;
        try {
            File dir = new File(xmlDirectory(properties,""));
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(xmlDirectory(properties,"") + "/" + "last");
            if (!file.exists()) {
                file.createNewFile();
            }
            else {
                file.delete();
                file.createNewFile();
            }

            FileOutputStream fileos = new FileOutputStream (file);
            fileos.write(properties.fileName.getBytes());
            fileos.close();
        }
        catch (FileNotFoundException e) {        }    catch (IllegalArgumentException e) {       }    catch (IllegalStateException e) {     }    catch (IOException e) {}


    }


    PdfDocument.Page page;
    PdfDocument.PageInfo pageInfo;
    PdfDocument document;

    public void exportToPDF(Properties properties) {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            GIIApplication.gii.activity.showMessage("Android version not supported");
            return;
        }

        document = new PdfDocument();

        GIIApplication.gii.selectedId = "none";
        GIIApplication.gii.selectedCircle = GIIApplication.gii.circleById("none");

        //PdfDocument.PageInfo pageInfo = document.getPages().get(0);
        pageInfo = new PdfDocument.PageInfo.Builder
                (595, 842, 1).create();
        page = document.startPage(pageInfo);
        GIIApplication.gii.graphics.drawTheStuff(page.getCanvas(), GII.AppState.idle,
                gii.properties,gii.circle,gii.displayedOperation,"-1","-1",new PointF(0,0));
        document.finishPage(page);

        pageInfo = new PdfDocument.PageInfo.Builder(595,842,2).create();
        page = document.startPage(pageInfo);
        GIIApplication.gii.reportWindow.init();
        GIIApplication.gii.reportWindow.onDraw(page.getCanvas(), document, this);
        document.finishPage(page);

        //TODO: if this is a PDF, recycle the bitmaps!

        String pdfName = properties.getFileNameWithoutXML() + ".pdf";

        try {
            File dir = new File(xmlDirectory(properties,""));
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(xmlDirectory(properties,"") + "/" + pdfName);
            if (!file.exists()) {
                file.createNewFile();
            }
            else {
                file.delete();
                file.createNewFile();
            }

            FileOutputStream fileos = new FileOutputStream (file);
            document.writeTo(fileos);
            document.close();
            fileos.close();

            if(file.exists()) {
                Intent target = new Intent(Intent.ACTION_VIEW);
                target.setDataAndType(Uri.fromFile(file),"application/pdf");
                target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                Intent intent = Intent.createChooser(target, "Open File");
                try {
                    gii.activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Instruct the user to install a PDF reader here, or something
                    Intent intentShareFile = new Intent(Intent.ACTION_SEND);
                    intentShareFile.setType("application/pdf");
                    intentShareFile.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://"+xmlDirectory(properties,"") + "/" + pdfName));
                    intentShareFile.putExtra(Intent.EXTRA_SUBJECT,
                            "Anyway Wallet: ".concat(properties.getFileNameWithoutXML()));
                    //intentShareFile.putExtra(Intent.EXTRA_TEXT, gii.getContext().getString(R.string.emailBodyXML));
                    gii.activity.startActivity(Intent.createChooser(intentShareFile, "Share File"));
                }
            }


        }
        catch (FileNotFoundException e) {        }    catch (IllegalArgumentException e) {       }    catch (IllegalStateException e) {     }    catch (IOException e) {}

    }

    public String getLastFile(Properties properties) {
        try {
            File dir = new File(xmlDirectory(properties,""));
            if (!dir.exists())
                dir.mkdirs();
            File file = new File(xmlDirectory(properties,"") + "/last");
            if (!file.exists()) {
                return("default.xml");
            }
            FileInputStream fileos = new FileInputStream(file);

            InputStreamReader isr = new InputStreamReader(fileos);

            char[] inputBuffer = new char[3]; //xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
            int charRead;
            StringBuilder stringBuilder = new StringBuilder();
            while ((charRead = isr.read(inputBuffer))>0) {
                //---convert the chars to a String---
                String readString =
                        String.copyValueOf(inputBuffer, 0,
                                charRead);
                stringBuilder.append(readString);
            }
            String data = stringBuilder.toString();

            isr.close();
            fileos.close();

            return data;
        }
        catch (FileNotFoundException e) { return "default.xml"; }    catch (IllegalArgumentException e) { return "default.xml"; }    catch (IllegalStateException e) { return "default.xml";}    catch (IOException e) {return "default.xml";}

    }

    public void deleteFile(String deleteName, Properties properties) {
        File dir = new File(xmlDirectory(properties,""));
        if (!dir.exists())
            dir.mkdirs();
        File file = new File(xmlDirectory(properties,"") + "/" + deleteName);
        if (file.exists()) {
            file.delete();
        }
    }

    public String getAnyFile(Properties properties) {
        ArrayList<String> files = fileList("xml", properties);
        if (files.size() == 0)
            return "default.xml";
        return files.get(0);
    }

    public void syncFiles(ArrayList<String> filesInCloud, Properties properties) {
        try {
            File dir = new File(xmlDirectory(properties,""));
            if (!dir.exists())
                dir.mkdirs();
            boolean newFiles = false;
            for (String fileName:filesInCloud) {
                File file = new File(xmlDirectory(properties,"") + "/" + fileName);
                if (!file.exists()) {
                    file.createNewFile();
                    newFiles = true;
                }
            }
        }
        catch (FileNotFoundException e) { Log.e("syncFiles",e.getMessage()); }    catch (IllegalArgumentException e) { Log.e("syncFiles",e.getMessage()); }    catch (IllegalStateException e) { Log.e("syncFiles",e.getMessage());}    catch (IOException e) {Log.e("syncFiles",e.getMessage());}

    }

    public void shareFile(final Properties properties, MainActivity mainActivity) {
        if (GIIApplication.gii.ref.getAuth() == null) {
            AlertDialog alertDialog = new AlertDialog.Builder(mainActivity).create();
            alertDialog.setTitle(GIIApplication.gii.getContext().getString(R.string.shareFile));
            alertDialog.setMessage(GIIApplication.gii.getContext().getString(R.string.shareFileOfflineForbidden));
            alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    });
            alertDialog.show();
            return;
        }

        final EditText shareWithEmails = new EditText(mainActivity);

        shareWithEmails.setFocusable(true);
        shareWithEmails.setFocusableInTouchMode(true);
        shareWithEmails.setSingleLine(true);
        shareWithEmails.setText(properties.sharedWith);

        AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity);
        final AlertDialog dialog = builder.create();

        shareWithEmails.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
                }
            }
        });

        builder.setTitle(GIIApplication.gii.getContext().getString(R.string.shareFile))
                .setMessage(GIIApplication.gii.getContext().getString(R.string.enterEmails))
                .setView(shareWithEmails)
                .setPositiveButton(GIIApplication.gii.getContext().getString(R.string.shareFile), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        String emails = shareWithEmails.getText().toString();
                        //if (GIIApplication.gii.ref.getAuth() != null) {
                        //    GII.ref.child("maxflow/" + GII.ref.getAuth().getUid()+"/"+Properties.withoutXML(deleteName)).setValue(null);
                        //}
                        unShareWith(properties.sharedWith,emails);
                        properties.sharedWith = emails;
                        GII.ref.child("maxflow/" + gii.findOwner() + "/" + properties.computeFileNameWithoutXML() + "/properties/0").
                                setValue(properties);
                        shareWith(emails);
                    }
                }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
            }
        }).show();

    }


    private void shareWith(String emails) {
        if (GIIApplication.gii.ref.getAuth() != null) {
            for (String email:emails.split(",")) {
                email = email.trim();
                final String finalEmail = email;
                Log.w("share","trying to get email: " + email);
                GII.ref.child("users").orderByChild("email")
                        .equalTo(email)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChildren()) {
                            Map<String, String> map = new HashMap<String, String>();
                            DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                            Log.w("share","Found user id by email:" + firstChild.getKey().toString());
                            map.put("owner", GII.ref.getAuth().getUid());
                            map.put("ownerEmail",GII.ref.getAuth().getProviderData().get("email").toString());
                            map.put("permitTo", firstChild.getKey().toString());
                            map.put("permitToEmail", finalEmail);
                            map.put("filename",gii.properties.computeFileNameWithoutXML());
                            GII.ref.child("maxflow/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML() + "/shared/" + firstChild.getKey().toString()).setValue(map);
                            GII.ref.child("shared/" + firstChild.getKey().toString() + "/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML()).setValue(map);
                        } else
                            Log.e("Nothing found; ", dataSnapshot.toString());
                    }
                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Log.e("Firebase","The read failed 3: " + firebaseError.getMessage());
                    }
                });
 
                //GII.ref.child("maxflow/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML() + "/shared/" + email).setValue(GIIApplication.gii.properties);//or null in setValue
            }
        }
    }


    private void unShareWith(AccessRight accessRight) {
        if (GIIApplication.gii.ref.getAuth() != null) {
                    final String finalEmail = accessRight.permitToEmail;
                    Log.w("share", "unshare: trying to restrict access to: " + accessRight.permitToEmail);
                    GII.ref.child("users").orderByChild("email")
                            .equalTo(accessRight.permitToEmail)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChildren()) {
                                        DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                        Log.w("share", "Found user id by email to unshare:" + firstChild.getKey().toString());
                                        GII.ref.child("maxflow/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML() + "/shared/" + firstChild.getKey().toString()).setValue(null);
                                        GII.ref.child("shared/" + firstChild.getKey().toString() + "/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML()).setValue(null);
                                    } else
                                        Log.w("User not found x access", dataSnapshot.toString());
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Log.w("Firebase", "The read failed 3_2: " + firebaseError.getMessage());
                                }
                            });

                }
    }


    private void shareWith(final AccessRight accessRight) {
        if (GIIApplication.gii.ref.getAuth() != null) {
                final String finalEmail = accessRight.permitToEmail;
                Log.w("share","sharing access with: " + finalEmail);
                GII.ref.child("users").orderByChild("email")
                        .equalTo(finalEmail)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.hasChildren()) {
                                    DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                    Log.w("share","Found user id by email:" + firstChild.getKey().toString());
                                    accessRight.owner = GII.ref.getAuth().getUid();
                                    accessRight.ownerEmail = GII.ref.getAuth().getProviderData().get("email").toString();
                                    accessRight.permitTo = firstChild.getKey().toString();
                                    accessRight.permitToEmail = finalEmail;
                                    accessRight.filename = gii.properties.computeFileNameWithoutXML();
                                    GII.ref.child("maxflow/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML() + "/shared/" + firstChild.getKey().toString()).setValue(accessRight);
                                    GII.ref.child("shared/" + firstChild.getKey().toString() + "/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML()).setValue(accessRight);
                                } else
                                    Log.e("Nothing found; ", dataSnapshot.toString());
                            }
                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                Log.e("Firebase","The read failed 3_1: " + firebaseError.getMessage());
                            }
                        });
            }
    }



    private void unShareWith(String emails,String skipEmails) {
        if (GIIApplication.gii.ref.getAuth() != null) {
            for (String email:emails.split(",")) {
                email = email.trim();
                if (!skipEmails.contains(email)) {
                    final String finalEmail = email;
                    Log.w("share", "unshare: trying to get email: " + email);
                    GII.ref.child("users").orderByChild("email")
                            .equalTo(email)
                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.hasChildren()) {
                                        DataSnapshot firstChild = dataSnapshot.getChildren().iterator().next();
                                        Log.w("share", "Found user id by email to unshare:" + firstChild.getKey().toString());
                                        GII.ref.child("maxflow/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML() + "/shared/" + firstChild.getKey().toString()).setValue(null);
                                        GII.ref.child("shared/" + firstChild.getKey().toString() + "/" + GII.ref.getAuth().getUid() + "/" + GIIApplication.gii.properties.computeFileNameWithoutXML()).setValue(null);
                                    } else
                                        Log.w("Nothing found; ", dataSnapshot.toString());
                                }

                                @Override
                                public void onCancelled(FirebaseError firebaseError) {
                                    Log.w("Firebase", "The read failed 3: " + firebaseError.getMessage());
                                }
                            });

                }
            }
        }
    }
}
