package com.mantum.cmms.adapter.handler;

import com.mantum.component.adapter.handler.ViewAdapterHandler;

public interface ParoHandler<T extends ParoHandler> extends ViewAdapterHandler<T> {

    String getHoraInicio();

    String getHoraFin();

    String getClasificacion();

    Long getTipo();
}
