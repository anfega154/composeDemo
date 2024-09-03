package com.mantum.cmms.domain;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class BusquedaAvanzada {

    private String parent;

    private String code;

    private String external;

    private String name;

    private String family;

    private Double altitude;

    private Double longitude;

    private Double latitude;

    private String type;

    @Nullable
    public String getParent() {
        return parent;
    }

    public void setParent(@Nullable String parent) {
        this.parent = parent;
    }

    @Nullable
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
    }

    @Nullable
    public String getExternal() {
        return external;
    }

    public void setExternal(@Nullable String external) {
        this.external = external;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @Nullable
    public String getFamily() {
        return family;
    }

    public void setFamily(@Nullable String family) {
        this.family = family;
    }

    @Nullable
    public Double getAltitude() {
        return altitude;
    }

    public void setAltitude(@Nullable Double altitude) {
        this.altitude = altitude;
    }

    @Nullable
    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(@Nullable Double longitude) {
        this.longitude = longitude;
    }

    @Nullable
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(@Nullable Double latitude) {
        this.latitude = latitude;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public boolean isLocationValid() {
        return getLatitude() != null && getLongitude() != null;
    }

    public boolean isValid() {
        return (getParent() != null && !getParent().isEmpty())
                || (getCode() != null && !getCode().isEmpty())
                || (getExternal() != null && !getExternal().isEmpty())
                || (getName() != null && !getName().isEmpty())
                || (getFamily() != null && !getFamily().isEmpty());
    }

    public static class Request {

        private List<Resultado> entities;

        public Request() {
            entities = new ArrayList<>();
        }

        public List<Resultado> getEntities() {
            return entities;
        }

        public void setEntities(List<Resultado> entities) {
            this.entities = entities;
        }
    }
}
