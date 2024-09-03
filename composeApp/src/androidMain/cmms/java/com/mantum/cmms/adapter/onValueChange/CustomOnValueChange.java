package com.mantum.cmms.adapter.onValueChange;

public interface CustomOnValueChange<T> {

    void onClick(T value, int position);
    void onFirstTextChange(String value, int position);
    void onSecondTextChange(String value, int position);
    void onThirdTextChange(String value, int position);
}
