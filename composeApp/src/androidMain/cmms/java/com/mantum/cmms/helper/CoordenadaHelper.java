package com.mantum.cmms.helper;

import com.mantum.cmms.domain.Coordenada;

public class CoordenadaHelper {

    private final Coordenada location;

    public CoordenadaHelper(Coordenada location) {
        this.location = location;
    }

    public Coordenada getLocation() {
        return location;
    }
}
