package com.mantum.cmms.adapter.handler;

import com.mantum.component.adapter.handler.ViewAdapterHandler;

public interface ListadoConsumiblesHandler<T extends ListadoConsumiblesHandler> extends ViewAdapterHandler<T> {

    String getNombre();

    Double getCantidadreal();
}
