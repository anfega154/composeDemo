package com.mantum.cmms.entity;

import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.demo.R;
import com.mantum.component.adapter.handler.ViewAdapter;
import com.mantum.component.adapter.handler.ViewGalleryAdapter;

import java.io.File;

import io.realm.RealmObject;

public class Adjuntos extends RealmObject implements ViewGalleryAdapter<Adjuntos>, ViewAdapter<Adjuntos> {

    private Long idfile;

    private String text;

    private String path;

    private boolean external;

    private String type;

    public Adjuntos() {
        this.external = true;
    }

    public Long getIdfile() {
        return idfile;
    }

    public void setIdfile(Long idfile) {
        this.idfile = idfile;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Nullable
    @Override
    public File getFile() {
        if (!isExternal()) {
            return new File(getPath());
        }
        return null;
    }

    @Nullable
    @Override
    public Uri getUri() {
        if (isExternal()) {
            return Uri.parse(getPath());
        }
        return null;
    }

    @NonNull
    @Override
    public Adjuntos getValue() {
        return this;
    }

    @Override
    public boolean compareTo(Adjuntos value) {
        return getIdfile().equals(value.getIdfile());
    }

    @NonNull
    @Override
    public String getTitle() {
        return getText();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        switch (getType()) {
            case "txt":
                return "text/plain";
            case "csv":
                return "text/csv";
            case "xml":
                return "text/xml";
            case "html":
                return "text/html";
            case "php":
                return "text/php";
            case "excel":
                return "Excel";
            case "word":
                return "Word";
        }
        return getType();
    }

    @Nullable
    @Override
    public String getSummary() {
        return null;
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        switch (getType()) {
            case "txt":
                return R.drawable.txt;
            case "pdf":
                return R.drawable.pdf;
            case "excel":
                return R.drawable.excel;
            case "word":
                return R.drawable.word;
            case "xml":
                return R.drawable.xml;
        }
        return R.drawable.actividad_mantenimiento;
    }
}