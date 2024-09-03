package com.mantum.cmms.domain;

public class Check {

    private final boolean modified;

    public Check(boolean modified) {
        this.modified = modified;
    }

    public boolean isModified() {
        return modified;
    }
}