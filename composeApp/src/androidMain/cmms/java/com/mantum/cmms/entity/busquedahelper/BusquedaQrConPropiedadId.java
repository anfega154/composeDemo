package com.mantum.cmms.entity.busquedahelper;

import com.google.gson.Gson;
import com.mantum.cmms.activity.BusquedaActivity;
import com.mantum.cmms.entity.Entidad;
import com.mantum.cmms.entity.ParsedJsonBusqueda;
import com.mantum.cmms.handler.IProcesarBusquedaQrStrategy;
import com.mantum.cmms.service.BusquedaServices;

public class BusquedaQrConPropiedadId implements IProcesarBusquedaQrStrategy {

    @Override
    public ParsedJsonBusqueda parser(String json) {
        Read read = new Gson().fromJson(json, Read.class);

        ParsedJsonBusqueda parsedJsonBusqueda = new ParsedJsonBusqueda();
        parsedJsonBusqueda.setId(read.getId());
        parsedJsonBusqueda.setCodigo(read.getEntitycode());
        parsedJsonBusqueda.setNombre(read.getEntityname());
        parsedJsonBusqueda.setTipo(read.getEntitytype());

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

        private final Long id;

        private final String entitycode;

        private final String entityname;

        public Read(Long id, String entitycode, String entityname) {
            this.id = id;
            this.entitycode = entitycode;
            this.entityname = entityname;
        }

        public Long getId() {
            return id;
        }

        public String getEntitycode() {
            return entitycode;
        }

        public String getEntityname() {
            return entityname;
        }

        public String getEntitytype() {
            return "Equipo";
        }
    }
}
