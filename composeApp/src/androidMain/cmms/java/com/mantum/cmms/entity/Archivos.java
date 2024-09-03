package com.mantum.cmms.entity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.component.adapter.handler.ViewInformationChildrenAdapter;

import java.io.File;

public class Archivos implements ViewInformationChildrenAdapter<Archivos> {

    @Override
    public boolean compareTo(Archivos value) {
        return false;
    }

    @Nullable
    @Override
    public File getFile() {
        return null;
    }

    @NonNull
    @Override
    public String getName() {
        return "";
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return null;
    }

    @Nullable
    @Override
    public Integer getImageColor() {
        return null;
    }

    @Nullable
    @Override
    public Integer getTextColor() {
        return null;
    }
}