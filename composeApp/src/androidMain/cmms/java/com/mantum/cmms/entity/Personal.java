package com.mantum.cmms.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.mantum.R;
import com.mantum.component.adapter.handler.ViewAdapter;
import com.mantum.component.mapped.IgnoreField;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Personal extends RealmObject implements Parcelable, Serializable, ViewAdapter<Personal> {

    private Long id;

    @PrimaryKey
    private String uuid;

    @IgnoreField
    private Cuenta cuenta;

    @SerializedName("cedula")
    private String cedula;

    @SerializedName("nombre")
    private String nombre;

    @SerializedName("grupopersonal")
    private String grupo;

    public Personal() {
    }

    protected Personal(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        uuid = in.readString();
        cedula = in.readString();
        nombre = in.readString();
        grupo = in.readString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Cuenta getCuenta() {
        return cuenta;
    }

    public void setCuenta(Cuenta cuenta) {
        this.cuenta = cuenta;
    }

    public String getCedula() {
        return cedula;
    }

    public void setCedula(String cedula) {
        this.cedula = cedula;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getNombre();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getCedula();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getGrupo();
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        return R.drawable.persona;
    }

    @Override
    public boolean compareTo(Personal value) {
        return getCedula().equals(value.getCedula());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(uuid);
        dest.writeString(cedula);
        dest.writeString(nombre);
        dest.writeString(grupo);
    }

    @NonNull
    @Override
    public String toString() {
        return "Personal{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", cuenta=" + cuenta +
                ", cedula='" + cedula + '\'' +
                ", nombre='" + nombre + '\'' +
                ", grupo='" + grupo + '\'' +
                '}';
    }

    public static final Creator<Personal> CREATOR = new Creator<Personal>() {
        @Override
        public Personal createFromParcel(Parcel in) {
            return new Personal(in);
        }

        @Override
        public Personal[] newArray(int size) {
            return new Personal[size];
        }
    };

    public static class Request implements Serializable {

        private Integer version;

        private List<Personal> listaPersonal;

        public Request() {
            this.listaPersonal = new ArrayList<>();
        }

        public List<Personal> getPersonal() {
            return listaPersonal;
        }

        public void setPersonal(List<Personal> personal) {
            this.listaPersonal = personal;
        }


        public Integer getVersion() {
            return version;
        }

        public void setVersion(Integer version) {
            this.version = version;
        }
    }
}