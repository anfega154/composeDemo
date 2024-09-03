package com.mantum.cmms.adapter.onValueChange;

public interface ParoOnValueChange {
    void onClick(int position);
    void onTimeStartChange(String value, int position);
    void onTimeEndChange(String value, int position);
    void onClasificationChange(String value, int position);
    void onTypeChange(Long value, int position);
}
