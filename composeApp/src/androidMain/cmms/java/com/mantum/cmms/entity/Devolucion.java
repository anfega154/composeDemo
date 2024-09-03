package com.mantum.cmms.entity;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.component.adapter.handler.ViewAdapter;

public class Devolucion implements Parcelable, ViewAdapter<Devolucion> {

    private String codigo;

    private String cantidad;

    private String descripcion;

    public static final Creator<Devolucion> CREATOR = new Creator<Devolucion>() {

        @Override
        public Devolucion createFromParcel(Parcel in) {
            return new Devolucion(in);
        }

        @Override
        public Devolucion[] newArray(int size) {
            return new Devolucion[size];
        }
    };

    public Devolucion() {}

    protected Devolucion(Parcel in) {
        codigo = in.readString();
        cantidad = in.readString();
        descripcion = in.readString();
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getCantidad() {
        return cantidad;
    }

    public void setCantidad(String cantidad) {
        this.cantidad = cantidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getCantidad();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getDescripcion();
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return null;
    }

    @Override
    public boolean compareTo(Devolucion value) {
        return false;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(codigo);
        dest.writeString(cantidad);
        dest.writeString(descripcion);
    }
}