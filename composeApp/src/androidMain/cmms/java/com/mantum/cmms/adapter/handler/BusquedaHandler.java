package com.mantum.cmms.adapter.handler;

import com.mantum.component.adapter.handler.ViewAdapterHandler;

public interface BusquedaHandler<T extends BusquedaHandler> extends ViewAdapterHandler<T> {

    String getTitle();

    String getSubtitle();

    String getSummary();

    String getCompleteType();

    String getIcon();

    Integer getDrawable();
}