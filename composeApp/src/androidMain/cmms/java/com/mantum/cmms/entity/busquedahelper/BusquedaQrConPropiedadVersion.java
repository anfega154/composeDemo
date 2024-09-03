package com.mantum.cmms.entity.busquedahelper;

import com.google.gson.Gson;
import com.mantum.cmms.activity.BusquedaActivity;
import com.mantum.cmms.entity.ParsedJsonBusqueda;
import com.mantum.cmms.handler.IProcesarBusquedaQrStrategy;
import com.mantum.cmms.service.BusquedaServices;

public class BusquedaQrConPropiedadVersion implements IProcesarBusquedaQrStrategy {
    @Override
    public ParsedJsonBusqueda parser(String json) {
        Read read = new Gson().fromJson(json, Read.class);

        ParsedJsonBusqueda parsedJsonBusqueda = new ParsedJsonBusqueda();
        parsedJsonBusqueda.setCodigo(read.getEntitycode());
        parsedJsonBusqueda.setVersion(read.getVersion());

        return parsedJsonBusqueda;
    }

    @Override
    public void saveAndSearch(ParsedJsonBusqueda parsedJsonBusqueda, BusquedaActivity busquedaActivity, BusquedaServices busquedaServices) {
        searchEntity(parsedJsonBusqueda, busquedaActivity);
    }

    @Override
    public void saveEntity(ParsedJsonBusqueda parsedJsonBusqueda, BusquedaServices busquedaServices) {
        //No se guarda la entidad porque no hay suficiente información en el QR
    }

    @Override
    public void searchEntity(ParsedJsonBusqueda parsedJsonBusqueda, BusquedaActivity busquedaActivity) {
        busquedaActivity.search(parsedJsonBusqueda.getCodigo(), "qrcode");
    }

    private static class Read {

        private final String entitycode;

        private final Integer version;

        public Read(String entitycode, Integer version) {
            this.entitycode = entitycode;
            this.version = version;
        }

        public String getEntitycode() {
            return entitycode;
        }

        public Integer getVersion() {
            return version;
        }
    }
}
