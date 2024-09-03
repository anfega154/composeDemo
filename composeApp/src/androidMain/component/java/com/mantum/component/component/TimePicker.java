package com.mantum.component.component;

import android.app.TimePickerDialog;
import android.content.Context;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.widget.EditText;

import com.mantum.demo.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class TimePicker extends Picker {

    private final static String TAG = TimePicker.class.getSimpleName();

    private TimePickerDialog timePickerDialog;

    private TimePickerDialog.OnTimeSetListener onTimeSetListener;

    private final boolean is24HourView;

    private Integer theme;

    public TimePicker(@NonNull Context context, @IdRes int id) {
        super(context, id);
        this.is24HourView = true;
    }

    @SuppressWarnings("unused")
    public TimePicker(@NonNull Context context, @IdRes int id, boolean is24HourView) {
        super(context, id);
        this.is24HourView = is24HourView;
    }

    public TimePicker(@NonNull Context context, @NonNull EditText value) {
        super(context, value);
        this.is24HourView = true;
    }

    @SuppressWarnings("unused")
    public TimePicker(@NonNull Context context, @NonNull EditText value, boolean is24HourView) {
        super(context, value);
        this.is24HourView = is24HourView;
    }

    @Override
    public void load() {
        load(false);
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }


    @Override
    public void load(boolean now) {
        if (onTimeSetListener == null) {
            onTimeSetListener = (view, hourOfDay, minute)
                    -> editText.setText(normalize(hourOfDay, minute));
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        timePickerDialog = theme == null ? new TimePickerDialog(
                context,
                onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                is24HourView
        ) : new TimePickerDialog(
                context,
                theme,
                onTimeSetListener,
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                is24HourView
        );

        timePickerDialog.setCancelable(false);
        editText.setOnClickListener(v -> {
            if (isEnabled()) {
                if (isValid()) {
                    Calendar current = getCalendarFromTimeString(getValue());
                    if (current != null) {
                        timePickerDialog.updateTime(
                                current.get(Calendar.HOUR_OF_DAY),
                                current.get(Calendar.MINUTE)
                        );
                    }
                }
                timePickerDialog.show();
            }
        });

        if (now) {
            setValue();
        }
    }

    @SuppressWarnings("unused")
    public void setOnTimeSetListener(@Nullable TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        this.onTimeSetListener = onTimeSetListener;
    }

    @Override
    public void setValue() {
        setValue(Calendar.getInstance(TimeZone.getDefault()));
    }

    @Override
    public void setValue(@NonNull Calendar calendar) {
        editText.setText(normalize(
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE)
        ));
    }

    @Override
    public void show() {
        if (timePickerDialog == null) {
            Log.w(TAG, "The load method has not been called");
            return;
        }
        timePickerDialog.show();
    }

    @NonNull
    private String normalize(int hourOfDay, int minute) {
        if (!is24HourView) {
            int hour = hourOfDay == 0 ? 12 : hourOfDay > 12 ? hourOfDay - 12 : hourOfDay;
            return String.format("%s:%s %s",
                    includeZero(hour),
                    includeZero(minute),
                    hourOfDay > 11 ? "PM" : "AM"
            );
        }
        return includeZero(hourOfDay) + ":" + includeZero(minute);
    }

    @Nullable
    private Calendar getCalendarFromTimeString(String value) {
        try {
            if (value == null || value.isEmpty()) {
                throw new Exception("values is null or empty");
            }

            SimpleDateFormat formatter = is24HourView
                    ? new SimpleDateFormat("HH:mm", Locale.getDefault())
                    : new SimpleDateFormat("hh:mm a", Locale.US);

            Calendar calendar = Calendar.getInstance();
            Date parse = formatter.parse(value);
            if (parse != null) {
                calendar.setTime(parse);
            }

            return calendar;
        } catch (Exception e) {
            Log.e(TAG, "getCalendarFromTimeString: ", e);
            return null;
        }
    }
}