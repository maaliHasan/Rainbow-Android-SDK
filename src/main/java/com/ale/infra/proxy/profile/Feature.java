package com.ale.infra.proxy.profile;

import java.util.Date;

/**
 * Created by georges on 20/02/2017.
 */

public class Feature {


    private String id;
    private String uniqueRef;
    private String name;
    private String type;
    private boolean isEnabled;
    private int limitMin;
    private int limitMax;
    private Date addedDate;
    private Date lastUpdateDate;

    public Feature() {
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setUniqueRef(String uniqueRef) {
        this.uniqueRef = uniqueRef;
    }

    public String getUniqueRef() {
        return uniqueRef;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setIsEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public boolean isEnabled() {
        return isEnabled || this.limitMax > 0;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
    }

    public void setLimitMin(int limitMin) {
        this.limitMin = limitMin;
    }

    public int getLimitMin() {
        return limitMin;
    }

    public void setLimitMax(int limitMax) {
        this.limitMax = limitMax;
    }

    public int getLimitMax() {
        return limitMax;
    }

    public void setAddedDate(Date addedDate) {
        this.addedDate = addedDate;
    }

    public Date getAddedDate() {
        return addedDate;
    }

    public void setLastUpdateDate(Date lastUpdateDate) {
        this.lastUpdateDate = lastUpdateDate;
    }

    public Date getLastUpdateDate() {
        return lastUpdateDate;
    }
}
