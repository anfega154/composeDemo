package com.mantum.cmms.entity.busquedahelper;

import com.mantum.cmms.activity.BusquedaActivity;
import com.mantum.cmms.entity.ParsedJsonBusqueda;
import com.mantum.cmms.handler.IProcesarBusquedaQrStrategy;
import com.mantum.cmms.service.BusquedaServices;

public class BusquedaQrSimple implements IProcesarBusquedaQrStrategy {
    @Override
    public ParsedJsonBusqueda parser(String json) {
        ParsedJsonBusqueda parsedJsonBusqueda = new ParsedJsonBusqueda();
        parsedJsonBusqueda.setCodigo(json);

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
}
