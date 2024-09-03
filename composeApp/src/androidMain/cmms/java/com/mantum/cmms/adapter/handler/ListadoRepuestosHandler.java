package com.mantum.cmms.adapter.handler;

import com.mantum.component.adapter.handler.ViewAdapterHandler;

public interface ListadoRepuestosHandler<T extends ListadoRepuestosHandler> extends ViewAdapterHandler<T> {

    String getSerial();

    String getNombre();

    String getSerialRetiro();
}
