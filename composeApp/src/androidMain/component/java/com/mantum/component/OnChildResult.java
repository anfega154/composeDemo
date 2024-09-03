package com.mantum.component;

public interface OnChildResult<T, K> {
    void call(T parent, K child, boolean value);
}
