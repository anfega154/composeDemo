package com.mantum.cmms.entity.busquedahelper;

import com.google.gson.Gson;
import com.mantum.cmms.activity.BusquedaActivity;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.ParsedJsonBusqueda;
import com.mantum.cmms.handler.IProcesarBusquedaQrStrategy;
import com.mantum.cmms.service.BusquedaServices;

public class BusquedaQrConPropiedadEntityId implements IProcesarBusquedaQrStrategy {

    @Override
    public ParsedJsonBusqueda parser(String json) {
        Read read = new Gson().fromJson(json, Read.class);

        ParsedJsonBusqueda parsedJsonBusqueda = new ParsedJsonBusqueda();
        parsedJsonBusqueda.setId(Long.valueOf(read.getEntityId()));
        parsedJsonBusqueda.setCodigo(read.getEntityCode());
        parsedJsonBusqueda.setNombre(read.getEntityName());
        parsedJsonBusqueda.setTipo(read.getEntityType());

        return parsedJsonBusqueda;
    }

    @Override
    public void saveAndSearch(ParsedJsonBusqueda parsedJsonBusqueda, BusquedaActivity busquedaActivity, BusquedaServices busquedaServices) {
        saveEntity(parsedJsonBusqueda, busquedaServices);
        searchEntity(parsedJsonBusqueda, busquedaActivity);
    }

    @Override
    public void saveEntity(ParsedJsonBusqueda parsedJsonBusqueda, BusquedaServices busquedaServices) {
        Entidad entidad = new Entidad();
        entidad.setId(parsedJsonBusqueda.getId());
        entidad.setCodigo(parsedJsonBusqueda.getCodigo());
        entidad.setNombre(parsedJsonBusqueda.getNombre());
        entidad.setTipo(parsedJsonBusqueda.getTipo());

        busquedaServices.guardarBusqueda(entidad);
    }

    @Override
    public void searchEntity(ParsedJsonBusqueda parsedJsonBusqueda, BusquedaActivity busquedaActivity) {
        busquedaActivity.search(parsedJsonBusqueda.getId(), parsedJsonBusqueda.getTipo());
    }

    private static class Read {

        private final String entitytype;

        private final String entityid;

        private final String entitycode;

        private final String entityname;

        public Read(String entitytype, String entityid, String entitycode, String entityname) {
            this.entitytype = entitytype;
            this.entityid = entityid;
            this.entitycode = entitycode;
            this.entityname = entityname;
        }

        public String getEntityType() {
            return entitytype;
        }

        public String getEntityId() {
            return entityid;
        }

        public String getEntityCode() {
            return entitycode;
        }

        public String getEntityName() {
            return entityname;
        }
    }
}
