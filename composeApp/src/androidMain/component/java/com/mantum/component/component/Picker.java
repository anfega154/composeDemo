package com.mantum.component.component;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.EditText;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public abstract class Picker {

    final EditText editText;

    private boolean enabled;

    protected final Context context;

    Picker(@NonNull Context context, @IdRes int id) {
        this.context = context;
        Activity activity = (Activity) context;
        this.editText = activity.findViewById(id);
        this.enabled = false;
    }

    Picker(@NonNull Context context, @NonNull EditText value) {
        this.context = context;
        this.editText = value;
        this.enabled = false;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Nullable
    public String getValue() {
        return editText != null ? editText.getText().toString() : null;
    }

    public boolean isValid() {
        return editText != null && !editText.getText().toString().isEmpty();
    }

    public abstract void load();

    public abstract void load(boolean now);

    public abstract void setValue();

    public abstract void setValue(@NonNull Calendar calendar);

    public abstract void show();

    public void setValue(@NonNull String value) {
        editText.setText(value);
    }

    @NonNull
    public static String get24HourView(@NonNull Date date) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        calendar.setTime(date);
        return String.format("%s:%s",
                includeZero(calendar.get(Calendar.HOUR_OF_DAY)),
                includeZero(calendar.get(Calendar.MINUTE))
        );
    }

    @NonNull
    static String includeZero(int number) {
        return String.valueOf(number <= 9 ? "0" + number : number);
    }
}