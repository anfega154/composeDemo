package com.mantum.component;

public interface OnResult<T> {

    void call(T entity, boolean value);
}