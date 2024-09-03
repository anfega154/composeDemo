package com.mantum.component.adapter.handler;

public interface ViewSelectedAdapter<T extends ViewAdapter<T>> extends ViewAdapter<T> {
    Long getId();

    Boolean isSelected();

    void setSelected(Boolean value);
}