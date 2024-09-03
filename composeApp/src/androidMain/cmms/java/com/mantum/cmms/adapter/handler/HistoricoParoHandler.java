package com.mantum.cmms.adapter.handler;

import com.mantum.component.adapter.handler.ViewAdapterHandler;

public interface HistoricoParoHandler<T extends HistoricoParoHandler> extends ViewAdapterHandler<T> {

    String getFechainicio();

    String getFechafin();

    String getDuracion();

    String getTipo();

    String getTipoparo();
}
