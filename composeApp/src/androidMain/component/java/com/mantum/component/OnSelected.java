package com.mantum.component;

public interface OnSelected<T> {

    void onClick(T value, int position);

    boolean onLongClick(T value, int position);
}