package com.mantum.cmms.adapter.handler;

import com.mantum.component.adapter.handler.ViewAdapterHandler;

public interface TareaHandler<T extends TareaHandler> extends ViewAdapterHandler<T> {

    String getCodigo();

    String getTarea();

    String getDescripcion();

    boolean isCritica();

    boolean isEjecutada();

    String getEjecutor();

    String getFechaejecucion();
}
