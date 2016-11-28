package com.gii.maxflow;

import java.util.ArrayList;

/**
 * Created by Timur_hnimdvi on 28-Nov-16.
 */
public class AccessRights {
    String filename = "";
    String owner = "";
    String ownerEmail = "";
    String permitTo = "";
    String permitToEmail = "";
    String filter = "";
    ArrayList<Circle> circles = new ArrayList<>();

    public AccessRights() {
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
}
