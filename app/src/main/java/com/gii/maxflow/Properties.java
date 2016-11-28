package com.gii.maxflow;

import android.graphics.PointF;
import android.util.Log;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties({ "fileNameWithoutXML" })
public class Properties {
    public Map<String, Boolean> getFields() {
        return fields;
    }

    @JsonIgnore
    public AccessRights accessRights = new AccessRights();
    //@JsonIgnore
    public boolean filtered = false;
    //@JsonIgnore
    public Date filterFrom = new Date();
    //@JsonIgnore
    public Date filterTo = new Date();
    //@JsonIgnore
    public String filterText = "";

    public boolean isFiltered() {
        return filtered;
    }

    public Date getFilterFrom() {
        return filterFrom;
    }

    public Date getFilterTo() {
        return filterTo;
    }

    public String getFilterText() {
        return filterText;
    }

    public String getFileName() {
        return fileName;
    }

    public Date getLastCloudUpdateCircle() {
        return lastCloudUpdateCircle;
    }

    public Date getLastCloudUpdateOperation() {
        return lastCloudUpdateOperation;
    }

    public int getCurrentPageNo() {
        return currentPageNo;
    }

    public int getZeroMonth() {
        return zeroMonth;
    }

    public PointF getBackgroundPosition() {
        return backgroundPosition;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public static String withoutXML(String fileName) {
        if (!fileName.toUpperCase().contains(".XML")) {
            return fileName.replace(".","_").replace("#","_").replace("$","_").replace("[","_").replace("]","_");
        }
        int n = fileName.toUpperCase().indexOf(".XML");
        return (fileName.substring(0,n).replace(".","_").replace("#","_").replace("$","_").replace("[","_").replace("]","_"));
    }

    public String getCurrency() {
        return currency;
    }

    public String getDefaultCurrency() {
        return defaultCurrency;
    }

    String fileName = ""; //XML file name

    @JsonIgnore
    String computeFileNameWithoutXML() {
        fileNameWithoutXML = withoutXML(fileName);
        return withoutXML(fileName);
    }

    String getFileNameWithoutXML() {
        return fileNameWithoutXML;
    }

    String fileNameWithoutXML = "";

    Date lastCloudUpdateCircle;
    Date lastCloudUpdateOperation;

    String currency = "";

    String defaultCurrency = "";

    public Map<String,Boolean> fields = new HashMap<String,Boolean>();

    public int version = 0;

    public int getVersion() {
        return version;
    }

    int currentPageNo = 0; //current month number relative to zeroMonth (may be negative)
    public int zeroMonth = 2015*12+10; //starting month.
    //Say starting month is October 2015. Then
    //if currentPageNo = 3, then zeroMonth+currentPageNo is January 2016
    public PointF backgroundPosition = new PointF(0, 0); //scroll
    public float scaleFactor = 1; //zoom
    //public String firebaseUserEmail = "";

    public String sharedWith = "";

    public String getSharedWith() {
        return sharedWith;
    }

    public Properties() {
        lastCloudUpdateCircle = new Date(0);
        lastCloudUpdateOperation = new Date(0);
    }

    @JsonIgnore
    public String owner = "";

    @JsonIgnore
    public boolean syncedWithCloud = true;

    @JsonIgnore
    public boolean loaded = false;

    public String lastChangeId = "";

    public String getLastChangeId() {
        return lastChangeId;
    }

    public void set(Properties post) {
        this.version = post.version;
        this.fileName = post.fileName;
        this.currentPageNo = post.currentPageNo;
        this.zeroMonth = post.zeroMonth;
        this.backgroundPosition = post.backgroundPosition;
        this.scaleFactor = post.scaleFactor;
        this.filtered = post.filtered;
        this.filterFrom = post.filterFrom;
        this.filterTo = post.filterTo;
        this.filterText = post.filterText;
        this.currency = post.currency;
        this.defaultCurrency = post.defaultCurrency;
        this.sharedWith = post.sharedWith;
        Log.w("properties","set,"+post.fileName+","+currentPageNo);
    }

    public boolean differentFrom(Properties lastProperties) {
        return (this.backgroundPosition.x != lastProperties.backgroundPosition.x ||
        this.backgroundPosition.y != lastProperties.backgroundPosition.y ||
        !this.fileName.equals(lastProperties.fileName) ||
        this.currentPageNo != lastProperties.currentPageNo ||
        this.scaleFactor != lastProperties.scaleFactor ||
        this.filtered != lastProperties.filtered ||
        !this.filterFrom.equals(lastProperties.filterFrom) ||
        !this.filterTo.equals(lastProperties.filterTo) ||
        !this.filterText.equals(lastProperties.filterText) ||
        !this.currency.equals(lastProperties.currency) ||
        !this.defaultCurrency.equals(lastProperties.defaultCurrency) ||
        !this.sharedWith.equals(lastProperties.sharedWith));
    }
}
