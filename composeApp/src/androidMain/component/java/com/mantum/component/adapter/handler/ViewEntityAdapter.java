package com.mantum.component.adapter.handler;


public interface ViewEntityAdapter<T extends ViewAdapter<T>> extends ViewAdapter<T> {

    Long getId();
}
