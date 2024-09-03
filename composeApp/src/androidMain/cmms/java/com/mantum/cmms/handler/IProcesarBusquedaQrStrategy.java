package com.mantum.cmms.handler;

import com.mantum.cmms.activity.BusquedaActivity;
import com.mantum.cmms.entity.ParsedJsonBusqueda;
import com.mantum.cmms.service.BusquedaServices;

public interface IProcesarBusquedaQrStrategy {

    ParsedJsonBusqueda parser(String json);

    void saveAndSearch(ParsedJsonBusqueda parsedJsonBusqueda, BusquedaActivity busquedaActivity, BusquedaServices busquedaServices);

    void saveEntity(ParsedJsonBusqueda parsedJsonBusqueda, BusquedaServices busquedaServices);

    void searchEntity(ParsedJsonBusqueda parsedJsonBusqueda, BusquedaActivity busquedaActivity);
}
