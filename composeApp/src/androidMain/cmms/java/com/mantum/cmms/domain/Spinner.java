package com.mantum.cmms.domain;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Spinner {

    private final String key;

    private final String value;

    private Long identidad;

    private String tipoentidad;

    public Spinner(@Nullable String key, @NonNull String value) {
        this.key = key;
        this.value = value;
    }

    @Nullable
    public String getKey() {
        return key;
    }

    @NonNull
    public String getValue() {
        return value;
    }

    @Nullable
    public Long getIdentidad() {
        return identidad;
    }

    public void setIdentidad(@NonNull Long identidad) {
        this.identidad = identidad;
    }

    @Nullable
    public String getTipoentidad() {
        return tipoentidad;
    }

    public void setTipoentidad(@NonNull String entidad) {
        this.tipoentidad = entidad;
    }

    @NonNull
    @Override
    public String toString() {
        return value;
    }
}
