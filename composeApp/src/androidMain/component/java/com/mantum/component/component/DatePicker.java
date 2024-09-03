package com.mantum.component.component;

import android.app.DatePickerDialog;
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

public class DatePicker extends Picker {

    private final static String TAG = DatePicker.class.getSimpleName();

    private DatePickerDialog datePickerDialog;

    private DatePickerDialog.OnDateSetListener onDateSetListener;

    private Integer theme;

    public DatePicker(@NonNull Context context, @IdRes int id) {
        super(context, id);
    }

    public DatePicker(@NonNull Context context, @NonNull EditText value) {
        super(context, value);
    }

    public void setTheme(int theme) {
        this.theme = theme;
    }


    @Override
    public void load() {
        load(false);
    }

    @Override
    public void load(boolean now) {
        if (onDateSetListener == null) {
            onDateSetListener = (view, year, month, dayOfMonth)
                    -> editText.setText(normalize(year, month + 1, dayOfMonth));
        }

        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        datePickerDialog = theme == null
                ? new DatePickerDialog(
                context,
                onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        ) : new DatePickerDialog(
                context,
                theme,
                onDateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
        );

        datePickerDialog.setCancelable(false);
        editText.setOnClickListener(v -> {
            if (isEnabled()) {
                if (isValid()) {
                    Calendar current = getCalendarFromDateString(getValue());
                    if (current != null) {
                        datePickerDialog.updateDate(
                                current.get(Calendar.YEAR),
                                current.get(Calendar.MONTH),
                                current.get(Calendar.DAY_OF_MONTH)
                        );
                    }
                }
                datePickerDialog.show();
            }
        });

        if (now) {
            setValue();
        }
    }

    @SuppressWarnings("unused")
    public void setOnDateSetListener(@Nullable DatePickerDialog.OnDateSetListener onDateSetListener) {
        this.onDateSetListener = onDateSetListener;
    }

    @Override
    public void setValue() {
        setValue(Calendar.getInstance(TimeZone.getDefault()));
    }

    @Override
    public void setValue(@NonNull Calendar calendar) {
        editText.setText(normalize(calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH)));
    }

    public void setValue(@NonNull String value) {
        Calendar calendar = getCalendarFromDateString(value);
        if (calendar != null) {
            setValue(calendar);
        }
    }

    @Override
    public void show() {
        if (datePickerDialog == null) {
            Log.w(TAG, "The load method has not been called");
            return;
        }
        datePickerDialog.show();
    }

    @NonNull
    private String normalize(int year, int month, int dayOfMonth) {
        return year + "-" + includeZero(month) + "-" + includeZero(dayOfMonth);
    }

    @Nullable
    private Calendar getCalendarFromDateString(String value) {
        try {
            if (value == null || value.isEmpty()) {
                throw new Exception("values is null or empty");
            }

            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date parse = formatter.parse(value);
            if (parse != null) {
                calendar.setTime(parse);
            }
            return calendar;
        } catch (Exception e) {
            return null;
        }
    }
}