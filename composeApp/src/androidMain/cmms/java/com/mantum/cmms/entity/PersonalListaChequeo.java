package com.mantum.cmms.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.component.adapter.handler.ViewAdapter;

import io.realm.RealmObject;

public class PersonalListaChequeo extends RealmObject implements Parcelable, ViewAdapter<PersonalListaChequeo> {

    private Long id;
    private Long idplc;
    private String codigo;
    private String nombre;
    private String apellido;
    private String cargo;
    private String tipocargo;
    private  boolean seleccionado;

    public PersonalListaChequeo() {
    }

    protected PersonalListaChequeo(@NonNull Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        if (in.readByte() == 0) {
            idplc = null;
        } else {
            idplc = in.readLong();
        }
        codigo = in.readString();
        nombre = in.readString();
        apellido = in.readString();
        cargo = in.readString();
        tipocargo = in.readString();
        seleccionado = in.readByte() != 0;
    }

    public static final Creator<PersonalListaChequeo> CREATOR = new Creator<PersonalListaChequeo>() {
        @Override
        public PersonalListaChequeo createFromParcel(Parcel in) {
            return new PersonalListaChequeo(in);
        }

        @Override
        public PersonalListaChequeo[] newArray(int size) {
            return new PersonalListaChequeo[size];
        }
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdplc() {
        return idplc;
    }

    public void setIdplc(Long idplc) {
        this.idplc = idplc;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCargo() {
        return cargo;
    }

    public void setCargo(String cargo) {
        this.cargo = cargo;
    }

    public String getTipocargo() {
        return tipocargo;
    }

    public void setTipocargo(String tipocargo) {
        this.tipocargo = tipocargo;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(id);
        }
        if (idplc == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(idplc);
        }
        parcel.writeString(codigo);
        parcel.writeString(nombre);
        parcel.writeString(apellido);
        parcel.writeString(cargo);
        parcel.writeString(tipocargo);
        parcel.writeByte((byte) (seleccionado ? 1 : 0));
    }

    @NonNull
    @Override
    public String getTitle() {
        return String.format("%s %s", getNombre(), getApellido());
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getCargo();
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
    public boolean compareTo(@NonNull PersonalListaChequeo value) {
        return getId().equals(value.getId());
    }

    @NonNull
    @Override
    public String toString() {
        return "PersonalListaChequeo{" +
                "id=" + id +
                ", idplc=" + idplc +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", apellido='" + apellido + '\'' +
                ", cargo='" + cargo + '\'' +
                ", tipocargo='" + tipocargo + '\'' +
                ", seleccionado=" + seleccionado +
                '}';
    }
}