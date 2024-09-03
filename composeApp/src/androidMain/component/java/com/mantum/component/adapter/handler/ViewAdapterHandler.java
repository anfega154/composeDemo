package com.mantum.component.adapter.handler;

public interface ViewAdapterHandler<T extends ViewAdapterHandler> {

    boolean compareTo(T value);
}