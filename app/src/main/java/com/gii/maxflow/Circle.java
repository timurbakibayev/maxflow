package com.gii.maxflow;

import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.PointF;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Timur on 21-Aug-15.
 * The circles you see on screen
 */
public class Circle {

    @JsonIgnore
    public Map<String, Float> displayAmount = new HashMap<String, Float>();
    @JsonIgnore
    public Map<String, Float> displayAmountWidget = new HashMap<String, Float>();
    @JsonIgnore
    public Map<String, Float> displayAmountTextWidth = new HashMap<String, Float>();

    @JsonIgnore
    public Bitmap theIcon;

    public String photoString;

    public String getPhotoString() {
        return photoString;
    }

    @JsonIgnore
    public String theIconParams = "";

    public void setCoordinates(PointF coordinates, ArrayList<Circle> circle, String androidID) {
        PointF DxDy = new PointF(coordinates.x - this.coordinates.x, coordinates.y - this.coordinates.y);
        this.coordinates = coordinates;
        this.syncedWithCloud = false;
        this.lastChangeId = androidID;
        for (Circle _circle : circle)
            if (_circle.parentId.equals(this.id))
                _circle.setCoordinates(new PointF(_circle.coordinates.x + DxDy.x,_circle.coordinates.y + DxDy.y), circle, androidID);
    }

    public void moveDxDy(PointF DxDy) {
        this.coordinates = new PointF(this.coordinates.x + DxDy.x, this.currentCoordinates.y + DxDy.y);
        this.syncedWithCloud = false;
    }

    public void setRadius(float radius) {
        this.radius = radius;
        if (!GIIApplication.gii.prefs.getBoolean("circle_size_auto", false))
            this.syncedWithCloud = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        this.syncedWithCloud = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public void setParentId(String parentId) {
        if (this.parentId.equals(parentId))
            this.parentId = "";
        else
            this.parentId = parentId;
        syncedWithCloud = false;
    }


    public void setDeleted(boolean deleted, ArrayList<Circle> circle) {
        this.deleted = deleted;
        for (Circle _circle : circle) {
            if (_circle.parentId.equals(this.id))
                _circle.parentId = "";
        }
        //TODO: Show dialog and ask, whether the user wants to delete operations as well
    }


    public void setPicture(int picture) {
        this.picture = picture;
    }

    public void setColor(int color) {
        this.color = color;
    }


    public void setAmount(float amount) {
        this.amount = amount;
    }


    public void setAmountTotal(float amountTotal) {
        this.amountTotal = amountTotal;
    }




    public void setVisible(boolean visible) {
        this.visible = visible;
    }


    public void setSyncedWithCloud(boolean syncedWithCloud) {
        this.syncedWithCloud = syncedWithCloud;
    }


    public void setSentToCloud(boolean sentToCloud) {
        this.sentToCloud = sentToCloud;
    }


    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }


    public void setAmountText(String amountText) {
        this.amountText = amountText;
    }


    public void setAmountTextWidth(float amountTextWidth) {
        this.amountTextWidth = amountTextWidth;
    }


    public void setNameTextWidth(float nameTextWidth) {
        this.nameTextWidth = nameTextWidth;
    }

    public PointF coordinates = new PointF(0,0);

    public PointF getCoordinates(boolean forceMoveXY, PointF moveXY) {
        if (moving) {
            move();
            return (currentCoordinates);
        }
        if (forceMoveXY) {
            return (moveXY);
        }

        return (coordinates);
    }

    public String getLastChangeId() {
        return lastChangeId;
    }

    public void setLastChangeId(String lastChangeId) {
        this.lastChangeId = lastChangeId;
    }

    public boolean isMyMoney() {
        return myMoney;
    }

    public void setMyMoney(boolean myMoney) {
        this.myMoney = myMoney;
        syncedWithCloud = false;
    }

    @JsonIgnore
    private boolean isMyMoney; //firebase problem :( should be never used
                                //may be it will be safe to delete this after release
                                //because problem is only in test@gmail.com/default

    public String lastChangeId = "";
    public float radius;
    public String name = "";
    public String id;
    public String parentId = "";
    public boolean deleted;
    public int picture;
    public int color;
    public boolean myMoney = false;

    @JsonIgnore
    public float amount; //used only for interface, not saved to file

    public void setGoalAmount(float goalAmount) {
        this.goalAmount = goalAmount;
        this.syncedWithCloud = false;
    }

    public float getGoalAmount() {
        return goalAmount;
    }

    public float goalAmount = 0; //used only for interface, not saved to file

    @JsonIgnore
    public float showAmount = 0; //used only for interface, not saved to file

    @JsonIgnore
    public float dxShowAmount = 0; //used only for interface, not saved to file

    @JsonIgnore
    public float amountTotal; //used only for interface, not saved to file

    public boolean showChildren = false;
    public boolean visible = true;

    public boolean isLimitGoal() {
        return limitGoal;
    }

    public boolean limitGoal = false;

    public boolean syncedWithCloud;
    public boolean sentToCloud;
    public String cloudId;
    @JsonIgnore
    public String amountText = "";
    @JsonIgnore
    public float amountTextWidth = 0;
    @JsonIgnore
    public float nameTextWidth = 0;

    @JsonIgnore
    public ArrayList<String> childrenId = new ArrayList<>();

    public boolean moving = false;
    public PointF currentCoordinates;

    public Circle() {

    }

    public Circle(String param) {
        if (param.equals("none")) {
            //, "none", new PointF(0, 0), 0, "none",false,0,0,false,false,"",false,0)
            this.coordinates = new PointF(0,0);
            this.radius = 0;
            this.name = "none";
            this.id = "none";
            this.parentId = "";
            this.deleted = false;
            this.picture = 0;
            this.color = 0;
            this.syncedWithCloud = false;
            this.sentToCloud = false;
            this.cloudId = "";
            this.visible = false;
            this.showChildren = false;
            this.moving = false;
            this.myMoney = false;
            this.goalAmount = 0;
        }
    }

    public PointF getCoordinates() {
        return coordinates;
    }

    public float getRadius() {
        return radius;
    }

    public String getParentId() {
        return parentId;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public int getPicture() {
        return picture;
    }

    public int getColor() {
        return color;
    }

    public float getAmount() {
        return amount;
    }

    public float getAmountTotal() {
        return amountTotal;
    }

    //@JsonIgnore
    public boolean isShowChildren() {
        return showChildren;
    }

    public boolean isVisible() {
        return visible;
    }

    public boolean isSyncedWithCloud() {
        return syncedWithCloud;
    }

    public boolean isSentToCloud() {
        return sentToCloud;
    }

    public String getCloudId() {
        return cloudId;
    }

    @JsonIgnore
    public String getAmountText() {
        return amountText;
    }

    @JsonIgnore
    public float getAmountTextWidth() {
        return amountTextWidth;
    }

    @JsonIgnore
    public float getNameTextWidth() {
        return nameTextWidth;
    }

    @JsonIgnore
    public ArrayList<String> getChildrenId() {
        return childrenId;
    }

    public boolean isMoving() {
        return moving;
    }

    public PointF getCurrentCoordinates() {
        return currentCoordinates;
    }

    public Circle(Circle crc) { //copy constuctor
        this.coordinates= new PointF(crc.coordinates.x,crc.coordinates.y);
        this.radius=crc.radius;
        this.name=crc.name;
        this.id=crc.id;
        this.deleted=crc.deleted;
        this.picture=crc.picture;
        this.color=crc.color;
        this.amount=crc.amount; //used only for interface performance, not saved to file
        this.amountTotal=crc.amountTotal; //used only for interface performance, not saved to file
        this.syncedWithCloud=crc.syncedWithCloud;
        this.sentToCloud=crc.sentToCloud;
        this.cloudId=crc.cloudId;
        this.amountText=crc.amountText;
        this.amountTextWidth=crc.amountTextWidth;
        this.nameTextWidth=crc.nameTextWidth;
        this.myMoney =crc.myMoney;
    }

    public Circle(String id, String name) {
        this.id = id;
        this.name = name;
    }

    public Circle(String id, String parentId, PointF point, float radius,
                  String name, boolean deleted, int picture, int color, boolean sentToCloud,
                  boolean syncedWithCloud, String cloudId, boolean myMoney, boolean goal, float goalAmount) {
        this.limitGoal = goal;
        this.coordinates = point;
        this.radius = radius;
        this.name = name;
        this.id = id;
        this.parentId = parentId;
        this.deleted = deleted;
        this.picture = picture;
        this.color = color;
        this.syncedWithCloud = syncedWithCloud;
        this.sentToCloud = sentToCloud;
        this.cloudId = cloudId;
        this.visible = parentId.equals("");
        this.showChildren = false;
        this.moving = false;
        this.myMoney = myMoney;
        this.goalAmount = goalAmount;
        this.lastChangeId = GIIApplication.gii.prefs.getString("AndroidID","");
    }

    public void setShowChildren(int value, ArrayList<Circle> circle, String androidID) {
        if (childrenId.size() == 0)
            return;
        if (value == 0)
            showChildren = !showChildren;
        if (value == -1)
            showChildren = false;
        if (value == 1)
            showChildren = true;
        if (showChildren) {
            for (Circle _circle : circle) {
                if (childrenId.contains(_circle.id)) {
                    _circle.currentCoordinates = this.coordinates;
                    _circle.moving = true;
                    _circle.lastChangeId = androidID;
                }
            }
        }
        lastChangeId = androidID;
    }
    public void move() {
        if (!moving)
            return;
        currentCoordinates = new PointF(
                currentCoordinates.x + (coordinates.x - currentCoordinates.x) / 4,
                currentCoordinates.y + (coordinates.y - currentCoordinates.y) / 4
        );
        if (Math.abs(currentCoordinates.x - coordinates.x) < 2 &&
                Math.abs(currentCoordinates.y - coordinates.y) < 2) {
            moving = false;
            currentCoordinates = coordinates;
        }
    }
    public void recalculateChildren(ArrayList<Circle> circle) {
        this.childrenId = new ArrayList<>();
        int[] a = new int[circle.size()];
        boolean changes = false;
        for (int i = 0; i < circle.size(); i++) {
            if (circle.get(i).id.equals(this.id))
                a[i] = 1;
            else
                a[i] = 0;
            if (circle.get(i).parentId.equals(this.id))
                changes = true;
        }

        while (changes) {
            changes = false;
            for (int i = 0; i < circle.size(); i++)
                if (!circle.get(i).deleted && !circle.get(i).parentId.equals("") && a[i] == 0)
                    for (int j = 0; j < circle.size(); j++)
                        if (circle.get(i).parentId.equals(circle.get(j).id) && a[j] == 1) {
                            a[i] = 1;
                            this.childrenId.add(circle.get(i).id);
                            changes = true;
                        }
        }
    }
    public boolean isAChild(String checkId) {
        return (this.childrenId.contains(checkId));
    }

    public void resetDisplayAmount() {
        displayAmount = new HashMap<String, Float>();
    }
    public void resetDisplayAmountWidget() {
        displayAmountWidget = new HashMap<String, Float>();
    }

    public void addDisplayAmount(String currency, float amount) {
        if (currency.equals(""))
            currency = GIIApplication.gii.properties.defaultCurrency;
        displayAmount.put(currency,displayAmount.get(currency) == null?amount:displayAmount.get(currency) + amount);
        setDisplayAmountTextWidth(GIIApplication.gii.graphics.dkBlue);
    }

    public void addDisplayAmountWidget(String currency, float amount) {
        if (currency.equals(""))
            currency = GIIApplication.gii.properties.defaultCurrency;
        displayAmountWidget.put(currency,displayAmountWidget.get(currency) == null?amount:displayAmountWidget.get(currency) + amount);
    }

    public void setDisplayAmountTextWidth(Paint paint) {
        displayAmountTextWidth = new HashMap<String, Float>();
        for (Map.Entry<String, Float> entry : displayAmount.entrySet())
            if (!entry.getKey().equals(""))
                displayAmountTextWidth.put(entry.getKey(),paint.measureText(GII.df.format(entry.getValue()) + " " + entry.getKey()));
            else {
                if (Math.abs(entry.getValue()) > 10)
                    displayAmountTextWidth.put(entry.getKey(), paint.measureText(GII.df.format(entry.getValue())));
                else
                    displayAmountTextWidth.put(entry.getKey(), paint.measureText("10"));
            }
    }

    public void addDisplayAmountsFromCircle(Circle sourceCircle) {
        for (Map.Entry<String, Float> entry : sourceCircle.displayAmount.entrySet()) {
            displayAmount.put(entry.getKey(),displayAmount.get(entry.getKey()) == null?entry.getValue():
                    displayAmount.get(entry.getKey()) + entry.getValue());
            amount += GIIApplication.gii.exchangeRates.convert(entry.getValue(),entry.getKey(),
                    GIIApplication.gii.properties.defaultCurrency);
        }
    }

}
