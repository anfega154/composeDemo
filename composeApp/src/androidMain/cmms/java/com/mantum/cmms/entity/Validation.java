package com.mantum.cmms.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Validation extends RealmObject {
    @PrimaryKey
    private int id;
    private String code;
    private String name;
    private String expectedStartDate;
    private String expectedFinishDate;

    public Validation() {
    }

    public Validation(int id, String code, String name, String expectedStartDate, String expectedFinishDate) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.expectedStartDate = expectedStartDate;
        this.expectedFinishDate = expectedFinishDate;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setExpectedStartDate(String expectedStartDate) {
        this.expectedStartDate = expectedStartDate;
    }

    public void setExpectedFinishDate(String expectedFinishDate) {
        this.expectedFinishDate = expectedFinishDate;
    }

    public int getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getExpectedStartDate() {
        return expectedStartDate;
    }

    public String getExpectedFinishDate() {
        return expectedFinishDate;
    }

    public String getFechas(){
        return this.expectedStartDate + " | "+ this.expectedFinishDate;
    }
}
