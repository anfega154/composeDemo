package com.mantum.cmms.adapter.handler;

import com.mantum.component.adapter.handler.ViewAdapterHandler;

public interface CentroCostoHandler<T extends CentroCostoHandler> extends ViewAdapterHandler<T> {

    String getCodigo();

    String getNombre();

    float getPorcentaje();
}
