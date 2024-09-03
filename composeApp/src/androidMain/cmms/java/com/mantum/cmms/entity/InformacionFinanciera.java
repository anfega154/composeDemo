package com.mantum.cmms.entity;

import io.realm.RealmList;
import io.realm.RealmObject;

public class InformacionFinanciera extends RealmObject {

    private String codigocontable;

    private String categoria;

    private RealmList<CentroCostoEquipo> centrocostos;
    private String annosInventario;

    public String getAnnosInventario() {
        return annosInventario;
    }

    public void setAnnosInventario(String annosInventario) {
        this.annosInventario = annosInventario;
    }

    public String getCodigocontable() {
        return codigocontable;
    }

    public void setCodigocontable(String codigocontable) {
        this.codigocontable = codigocontable;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public RealmList<CentroCostoEquipo> getCentrocostos() {
        return centrocostos;
    }

    public void setCentrocostos(RealmList<CentroCostoEquipo> centrocostos) {
        this.centrocostos = centrocostos;
    }

    @Override
    public String toString() {
        return "InformacionFinanciera{" +
                "codigocontable='" + codigocontable + '\'' +
                ", categoria='" + categoria + '\'' +
                ", centrocostos=" + centrocostos +
                ", annosInventario='" + annosInventario + '\'' +
                '}';
    }
}
