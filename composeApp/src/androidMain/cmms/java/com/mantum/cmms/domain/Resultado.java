package com.mantum.cmms.domain;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.SparseArray;

import com.mantum.R;
import com.mantum.cmms.adapter.BusquedaAvanzadaResultadoAdapter;

import java.util.List;

public class Resultado implements BusquedaAvanzadaResultadoAdapter.ViewAdapter<Resultado>, Parcelable {

    private Long id;

    private String codigo;

    private String nombre;

    private String padre;

    private String tipo;

    private String familia1;

    private String familia2;

    private String familia3;

    private String equipopadre;

    public Resultado() {}

    protected Resultado(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readLong();
        }
        codigo = in.readString();
        nombre = in.readString();
        padre = in.readString();
        tipo = in.readString();
        familia1 = in.readString();
        familia2 = in.readString();
        familia3 = in.readString();
        equipopadre = in.readString();
    }

    public static final Creator<Resultado> CREATOR = new Creator<Resultado>() {
        @Override
        public Resultado createFromParcel(Parcel in) {
            return new Resultado(in);
        }

        @Override
        public Resultado[] newArray(int size) {
            return new Resultado[size];
        }
    };

    @NonNull
    public static SparseArray<Resultado> factory(@NonNull List<Resultado> values) {
        SparseArray<Resultado> results = new SparseArray<>();
        for (int i = 0; i < values.size(); i++) {
            results.append(i, values.get(i));
        }
        return results;
    }

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

    public String getPadre() {
        return padre;
    }

    public void setPadre(String padre) {
        this.padre = padre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEquipopadre() {
        return equipopadre;
    }

    public void setEquipopadre(String equipopadre) {
        this.equipopadre = equipopadre;
    }

    @Nullable
    @Override
    public String getFamilia1() {
        return familia1;
    }

    public void setFamilia1(String familia1) {
        this.familia1 = familia1;
    }

    @Nullable
    @Override
    public String getFamilia2() {
        return familia2;
    }

    public void setFamilia2(String familia2) {
        this.familia2 = familia2;
    }

    @Nullable
    @Override
    public String getFamilia3() {
        return familia3;
    }

    public void setFamilia3(String familia3) {
        this.familia3 = familia3;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeLong(id);
        }
        dest.writeString(codigo);
        dest.writeString(nombre);
        dest.writeString(padre);
        dest.writeString(tipo);
        dest.writeString(familia1);
        dest.writeString(familia2);
        dest.writeString(familia3);
        dest.writeString(equipopadre);
    }

    @NonNull
    @Override
    public String getTitle() {
        return codigo;
    }

    @Nullable
    @Override
    public String getSubtitle() {
        return nombre;
    }

    @Nullable
    @Override
    public String getSummary() {
        return padre;
    }

    @Nullable
    @Override
    public String getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Integer getDrawable() {
        switch (getTipo()) {
            case "Equipo":
                return R.drawable.equipo;
            case "InstalacionLocativa":
                return R.drawable.locativa;
            case "InstalacionProceso":
                return R.drawable.proceso;
            default:
                return null;
        }
    }

    @Override
    public boolean compareTo(Resultado value) {
        return getId().equals(value.getId()) && getTipo().equals(value.getTipo());
    }
}
