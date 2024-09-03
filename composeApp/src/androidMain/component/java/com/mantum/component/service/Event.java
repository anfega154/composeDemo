package com.mantum.component.service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Event {

    private String title;

    private String description;

    private java.util.Calendar begin;

    private java.util.Calendar end;

    private boolean diaCompleto;

    private String color;

    public Event() {
        this.diaCompleto = false;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getBegin() {
        return begin.getTimeInMillis();
    }

    public void setBegin(Calendar begin) {
        this.begin = begin;
    }

    public void setBegin(SimpleDateFormat simpleDateFormat, String begin) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(simpleDateFormat.parse(begin));
        this.begin = calendar;
    }

    public long getEnd() {
        return end.getTimeInMillis();
    }

    public void setEnd(Calendar end) {
        this.end = end;
    }

    public void setEnd(SimpleDateFormat simpleDateFormat, String end) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(simpleDateFormat.parse(end));
        this.end = calendar;
    }

    public boolean isDiaCompleto() {
        return diaCompleto;
    }

    public void setDiaCompleto(boolean diaCompleto) {
        this.diaCompleto = diaCompleto;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}
