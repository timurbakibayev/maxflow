package com.gii.maxflow;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Timur on 03-Sep-15.
 */
public class Operation implements Comparable<Operation>{

    @Override
    public int compareTo(Operation _operation) {
        return (this.date.before(_operation.date)?1:this.date.after(_operation.date)?-1:0);
    }

    public Operation() {

    }

    @JsonIgnore
    public ArrayList<String> getCirclesWayOut() {
        return circlesWayOut;
    }

    @JsonIgnore
    public ArrayList<String> getCirclesWayIn() {
        return circlesWayIn;
    }

    @JsonIgnore
    public float absAmountInLocalCurrency = 0;

    public void setAbsAmountInLocalCurrency(float absAmountInLocalCurrency) {
        this.absAmountInLocalCurrency = absAmountInLocalCurrency;
    }

    public String getId() {
        return id;
    }

    public int getTransactionId() {
        return transactionId;
    }

    public String getDescription() {
        return description;
    }

    public String getFromCircle() {
        return fromCircle;
    }

    public String getToCircle() {
        return toCircle;
    }

    public int getPageNo() {
        return pageNo;
    }

    public float getAmount() {
        return amount;
    }

    public Date getDate() {
        return date;
    }

    public float getAlpha() {
        return alpha;
    }

    public boolean isSyncedWithCloud() {
        return syncedWithCloud;
    }

    public boolean isSentToCloud() {
        //return sentToCloud;
        return true;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public String getCloudId() {
        return cloudId;
    }

    public float getAmountTextWidth() {
        return amountTextWidth;
    }

    public void setTransactionId(int transactionId) {
        this.transactionId = transactionId;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFromCircle(String fromCircle) {
        this.fromCircle = fromCircle;
    }

    public void setToCircle(String toCircle) {
        this.toCircle = toCircle;
    }

    public void setPageNo(int pageNo) {
        this.pageNo = pageNo;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setAlpha(float alpha) {
        this.alpha = alpha;
    }

    public void setSyncedWithCloud(boolean syncedWithCloud) {
        this.syncedWithCloud = syncedWithCloud;
    }

    //public void setSentToCloud(boolean sentToCloud) {
    //    this.sentToCloud = sentToCloud;
    //}


    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public void setAmountTextWidth(float amountTextWidth) {
        this.amountTextWidth = amountTextWidth;
    }

    public String getCurrency() {
        return currency;
    }

    public float getExchangeRate() {
        return exchangeRate;
    }

    @JsonIgnore
    public ArrayList<String> circlesWayOut = new ArrayList<>();
    @JsonIgnore
    public ArrayList<String> circlesWayIn = new ArrayList<>();
    public String id;
    public int transactionId;
    public String description = "";
    public String fromCircle;
    public String toCircle;
    public int pageNo;
    public float amount;
    public Date date;
    public float alpha; //used only for displaying (displayOperaion), not saved to file

    @JsonIgnore
    public boolean syncedWithCloud = false;

    @JsonIgnore
    public boolean sentToCloud;

    public boolean deleted;
    public String cloudId;
    public float amountTextWidth = 0;

    public String currency = "";
    public float exchangeRate = 0;

    @JsonIgnore
    public boolean inFilter = true;

    @JsonIgnore
    public boolean twoWay = false;

    @JsonIgnore
    public boolean isIncome = false;

    @JsonIgnore
    public boolean isExpense = false;


    @JsonIgnore
    public String forSearch = "";

    @JsonIgnore
    public Circle abstractCircleForMultipleOperations = new Circle();

    @JsonIgnore
    public String amountText = "";

    public Operation(String id, String fromCircle, String toCircle, float amount, String currency, float exchangeRate, Date date, int transactionId, int pageNo, boolean sentToCloud, boolean syncedWithCloud, String cloudId, String description, boolean deleted) {
        this.id = id;
        this.fromCircle = fromCircle;
        this.toCircle = toCircle;
        this.amount = amount;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.transactionId = transactionId;
        this.pageNo = pageNo;
        this.syncedWithCloud = syncedWithCloud;
        //this.sentToCloud = sentToCloud;
        this.cloudId = cloudId;
        this.date = date;
        this.description = description;
        this.deleted = deleted;
        this.abstractCircleForMultipleOperations = new Circle();
        this.abstractCircleForMultipleOperations.resetDisplayAmount();
    }

    //public void addViaCircle(String fromCircle) {
    //    viaCircles.add(fromCircle);
    //}

    public void delete() {
        deleted = true;
        syncedWithCloud = false;
    }


    public Circle circleById(String id, ArrayList<Circle> circle) {
        for (Circle _circle : circle) {
            if (_circle.id.equals(id))
                return (_circle);
        }
        return (new Circle("none"));
    }

    public void calculatePath(ArrayList<Circle> circle) {
        circlesWayOut = new ArrayList<>();
        circlesWayIn = new ArrayList<>();
        forSearch = description.toUpperCase();
        int limit = 0;
        Circle currentCircle = circleById(fromCircle,circle);
        forSearch = forSearch + "," + currentCircle.name.toUpperCase();
        if (fromCircle.equals("none")) {
            circlesWayIn.add(toCircle);
            circlesWayOut.add(toCircle);
            currentCircle = circleById(fromCircle,circle);
            forSearch = forSearch + "," + currentCircle.name.toUpperCase();
            return;
        }
        if (toCircle.equals("none")) {
            circlesWayIn.add(fromCircle);
            circlesWayOut.add(fromCircle);
            forSearch = forSearch + "," + currentCircle.name.toUpperCase();
            return;
        }
        circlesWayOut.add(currentCircle.id);
        forSearch = forSearch + "," + currentCircle.name.toUpperCase();
        if (!currentCircle.childrenId.contains(toCircle)) {
            if (!currentCircle.name.equals("none")) {
                while (limit < 20 && !currentCircle.parentId.equals("")) {
                    currentCircle = circleById(currentCircle.parentId, circle);
                    if (currentCircle.equals("none"))
                        limit = 21;
                    else {
                        if (currentCircle.id.equals(toCircle)) {
                            circlesWayIn.add(currentCircle.id);
                            forSearch = forSearch + "," + currentCircle.name.toUpperCase();
                            return;
                        } else {
                            circlesWayOut.add(currentCircle.id);
                            forSearch = forSearch + "," + currentCircle.name.toUpperCase();
                        }
                    }
                    limit++;
                }
            }
        }
        limit = 0;
        currentCircle = circleById(toCircle,circle);
        circlesWayIn.add(currentCircle.id);
        forSearch = forSearch + "," + currentCircle.name.toUpperCase();
        while (limit < 20 && !currentCircle.parentId.equals("")) {
            currentCircle = circleById(currentCircle.parentId,circle);
            if (currentCircle.equals("none"))
                limit = 21;
            else {
                if (currentCircle.id.equals(fromCircle)) {
                    return;
                } else {
                    circlesWayIn.add(currentCircle.id);
                    forSearch = forSearch + "," + currentCircle.name.toUpperCase();
                }
            }
            limit++;
        }

    }

}
