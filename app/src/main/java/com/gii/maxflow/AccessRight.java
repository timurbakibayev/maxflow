package com.gii.maxflow;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.ArrayList;

/**
 * Created by Timur_hnimdvi on 28-Nov-16.
 */
public class AccessRight {
    String filename = "";
    String owner = "";
    String ownerEmail = "";
    String permitTo = "";
    String permitToEmail = "";
    String filter = "";

    Boolean sentOk = false;

    @JsonIgnore
    Boolean delete = false;

    ArrayList<String> circleIds = new ArrayList<>();

    public AccessRight() {
        //For Firebsae
    }

    public String getFilename() {
        return filename;
    }

    public String getOwner() {
        return owner;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public String getPermitTo() {
        return permitTo;
    }

    public String getPermitToEmail() {
        return permitToEmail;
    }

    public Boolean getSentOk() {
        return sentOk;
    }

    public String getFilter() {
        return filter;
    }

    public ArrayList<String> getCircleIds() {
        return circleIds;
    }
}
