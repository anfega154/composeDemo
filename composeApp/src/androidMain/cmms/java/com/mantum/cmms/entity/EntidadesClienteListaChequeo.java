package com.mantum.cmms.entity;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mantum.component.adapter.handler.ViewAdapter;

import io.realm.RealmObject;

public class EntidadesClienteListaChequeo extends RealmObject implements Parcelable, ViewAdapter<EntidadesClienteListaChequeo> {
    private Long id;
    private String codigo;
    private String nombre;
    private String tipo;
    private boolean prerrequisito;
    private Long idcliente;
    private boolean seleccionado;

    public EntidadesClienteListaChequeo() {
    }

    protected EntidadesClienteListaChequeo(@NonNull Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        codigo = in.readString();
        nombre = in.readString();
        tipo = in.readString();
        prerrequisito = in.readByte() != 0;
        if (in.readByte() == 0) {
            idcliente = null;
        } else {
            idcliente = in.readLong();
        }
        seleccionado = in.readByte() != 0;
    }

    public static final Creator<EntidadesClienteListaChequeo> CREATOR = new Creator<EntidadesClienteListaChequeo>() {
        @Override
        public EntidadesClienteListaChequeo createFromParcel(Parcel in) {
            return new EntidadesClienteListaChequeo(in);
        }

        @Override
        public EntidadesClienteListaChequeo[] newArray(int size) {
            return new EntidadesClienteListaChequeo[size];
        }
    };

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isPrerrequisito() {
        return prerrequisito;
    }

    public void setPrerrequisito(boolean prerrequisito) {
        this.prerrequisito = prerrequisito;
    }

    public Long getIdcliente() {
        return idcliente;
    }

    public void setIdcliente(Long idcliente) {
        this.idcliente = idcliente;
    }

    public boolean isSeleccionado() {
        return seleccionado;
    }

    public void setSeleccionado(boolean seleccionado) {
        this.seleccionado = seleccionado;
    }

    @NonNull
    @Override
    public String getTitle() {
        return getCodigo();
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return getNombre();
    }

    @Nullable
    @Override
    public String getSummary() {
        return getTipo();
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
    public boolean compareTo(@NonNull EntidadesClienteListaChequeo value) {
        return getId().equals(value.getId());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel parcel, int i) {
        if (id == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(id);
        }
        parcel.writeString(codigo);
        parcel.writeString(nombre);
        parcel.writeString(tipo);
        parcel.writeByte((byte) (prerrequisito ? 1 : 0));
        if (idcliente == null) {
            parcel.writeByte((byte) 0);
        } else {
            parcel.writeByte((byte) 1);
            parcel.writeLong(idcliente);
        }
        parcel.writeByte((byte) (seleccionado ? 1 : 0));
    }

    @NonNull
    @Override
    public String toString() {
        return "EntidadesClienteListaChequeo{" +
                "id=" + id +
                ", codigo='" + codigo + '\'' +
                ", nombre='" + nombre + '\'' +
                ", tipo='" + tipo + '\'' +
                ", prerrequisito=" + prerrequisito +
                ", idcliente=" + idcliente +
                ", seleccionado=" + seleccionado +
                '}';
    }
}